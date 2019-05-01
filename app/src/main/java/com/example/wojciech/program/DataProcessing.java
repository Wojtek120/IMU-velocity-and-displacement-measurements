package com.example.wojciech.program;

import android.content.Context;
import android.database.Cursor;
import android.util.Pair;

import java.util.Vector;

public class DataProcessing
{
    /** Kontekst */
    private Context context;

    /** Wektor z wartsciami czasu i predkosci w momentach przejsc z stanu statycvznego do ruchu i na odwrot */
    private Vector<Pair<Integer, Double>> changeStateTimesX;

    /** Wektor z wartsciami czasu i predkosci w momentach przejsc z stanu statycvznego do ruchu i na odwrot */
    private Vector<Pair<Integer, Double>> changeStateTimesY;

    /** Wektor z wartsciami czasu i predkosci w momentach przejsc z stanu statycvznego do ruchu i na odwrot */
    private Vector<Pair<Integer, Double>> changeStateTimesZ;

    private int IDofExercise;

    public DataProcessing(Context context)
    {
        this.context = context;
        changeStateTimesX = new Vector<>();
        changeStateTimesY = new Vector<>();
        changeStateTimesZ = new Vector<>();
    }


    public void processData()
    {
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        DatabaseHelperRPY databaseHelperRPY = new DatabaseHelperRPY(context);
        DatabaseHelperProcessedData databaseHelperProcessedData = new DatabaseHelperProcessedData(context);
        MadgwickFilter madgwickFilter = new MadgwickFilter();
        IDofExercise = databaseHelper.getLargestID();
        boolean first = true;

        ZeroVelocityUpdate zeroVelocity = new ZeroVelocityUpdate();

        Cursor data = databaseHelper.getData(IDofExercise);
        double yawAngle, pitchAngle, rollAngle;
        int firstControlNr = 1;

        Integral integralVelocityX = new Integral();
        Integral integralVelocityY = new Integral();
        Integral integralVelocityZ = new Integral();

        boolean previousState = true;


        while(data.moveToNext())
        {
            if(first)
            {
                firstControlNr = data.getInt(4);
                first = false;
            }

            //Filtracja filtrem Madgwicka
            madgwickFilter.filterUpdatedouble(data.getDouble(5), data.getDouble(6), data.getDouble(7),
                    data.getDouble(8), data.getDouble(9), data.getDouble(10),
                    data.getDouble(11), data.getDouble(12), data.getDouble(13));


            yawAngle = madgwickFilter.getYaw();
            pitchAngle = madgwickFilter.getPitch();
            rollAngle = madgwickFilter.getRoll();

            databaseHelperRPY.addData(IDofExercise, data.getString(2), data.getInt(4) - firstControlNr, yawAngle, pitchAngle, rollAngle);

            //Kompensacja grawitacji
            double []compensatedGravity = GravityCompensation.CompensateGravity
                    (new double[]{data.getDouble(5), data.getDouble(6), data.getDouble(7)},
                            madgwickFilter.getQuaternions());

            if(!data.isLast() && (data.getInt(4) - firstControlNr) >= 2000)
            {
                data.moveToNext();
                double nextTime = data.getInt(4);
                double []nextCompensatedGravity = GravityCompensation.CompensateGravity
                        (new double[]{data.getDouble(5), data.getDouble(6), data.getDouble(7)},
                                madgwickFilter.getQuaternions());
                data.moveToPrevious();

                //Detekcja stanu statycznego
                if (zeroVelocity.zeroVelocityUpdate(compensatedGravity, new double[]{data.getDouble(8), data.getDouble(9), data.getDouble(10)}))
                {
                    double velocityX = integralVelocityX.integrate(data.getInt(4) - firstControlNr, nextTime - firstControlNr, compensatedGravity[0], nextCompensatedGravity[0]);
                    double velocityY = integralVelocityY.integrate(data.getInt(4) - firstControlNr, nextTime - firstControlNr, compensatedGravity[1], nextCompensatedGravity[1]);
                    double velocityZ = integralVelocityZ.integrate(data.getInt(4) - firstControlNr, nextTime - firstControlNr, compensatedGravity[2], nextCompensatedGravity[2]);

                    //dodanie predkosci i czasow w momencie rozpoczecia ruchy
                    if(!previousState)
                    {
                        changeStateTimesX.add(new Pair<>(data.getInt(4) - firstControlNr, velocityX));
                        changeStateTimesY.add(new Pair<>(data.getInt(4) - firstControlNr, velocityY));
                        changeStateTimesZ.add(new Pair<>(data.getInt(4) - firstControlNr, velocityZ));
                    }
                    previousState = true;

                    databaseHelperProcessedData.addData(IDofExercise, data.getString(2), data.getString(3),
                            data.getInt(4) - firstControlNr, compensatedGravity, 1,
                            new double[]{velocityX, velocityY, velocityZ});

                } else
                {
                    double velocityX = integralVelocityX.integrate(data.getInt(4) - firstControlNr, nextTime - firstControlNr, compensatedGravity[0], nextCompensatedGravity[0]);
                    double velocityY = integralVelocityY.integrate(data.getInt(4) - firstControlNr, nextTime - firstControlNr, compensatedGravity[1], nextCompensatedGravity[1]);
                    double velocityZ = integralVelocityZ.integrate(data.getInt(4) - firstControlNr, nextTime - firstControlNr, compensatedGravity[2], nextCompensatedGravity[2]);

                    //dodanie predkosci i czasow w momencie zakonczenia ruchy
                    if(previousState)
                    {
                        changeStateTimesX.add(new Pair<>(data.getInt(4) - firstControlNr, velocityX));
                        changeStateTimesY.add(new Pair<>(data.getInt(4) - firstControlNr, velocityY));
                        changeStateTimesZ.add(new Pair<>(data.getInt(4) - firstControlNr, velocityZ));
                    }
                    previousState = false;

                    databaseHelperProcessedData.addData(IDofExercise, data.getString(2), data.getString(3),
                            data.getInt(4) - firstControlNr, compensatedGravity, 0,
                            new double[]{velocityX, velocityY, velocityZ});
                }
            }
        }

        data.close();
        databaseHelper.close();
        databaseHelperRPY.close();
        databaseHelperProcessedData.close();

        compensateVelocityAndComputeDisplacement();

    }

    private void compensateVelocityAndComputeDisplacement()
    {
        DatabaseHelperProcessedData databaseHelperProcessedData = new DatabaseHelperProcessedData(context);
        DatabaseHelperFinalData databaseHelperFinalData = new DatabaseHelperFinalData(context);

        Cursor data = databaseHelperProcessedData.getData(IDofExercise);

        boolean first = true;
        boolean previousState = false;

        int intervalsNr = 0;

        int[] xX = new int[2];
        int[] xY = new int[2];
        int[] xZ = new int[2];
        double[] yX = new double[2];
        double[] yY = new double[2];
        double[] yZ = new double[2];

        xX[0] = changeStateTimesX.get(intervalsNr).first;
        xY[0] = changeStateTimesY.get(intervalsNr).first;
        xZ[0] = changeStateTimesZ.get(intervalsNr).first;

        yX[0] = changeStateTimesX.get(intervalsNr).second;
        yY[0] = changeStateTimesY.get(intervalsNr).second;
        yZ[0] = changeStateTimesZ.get(intervalsNr).second;

        intervalsNr++;

        xX[1] = changeStateTimesX.get(intervalsNr).first;
        xY[1] = changeStateTimesY.get(intervalsNr).first;
        xZ[1] = changeStateTimesZ.get(intervalsNr).first;

        yX[1] = changeStateTimesX.get(intervalsNr).second;
        yY[1] = changeStateTimesY.get(intervalsNr).second;
        yZ[1] = changeStateTimesZ.get(intervalsNr).second;


        while(data.moveToNext())
        {
            if(first)
            {
                if(data.getInt(8) == 1)
                {
                    previousState = true;
                }
                else
                {
                    previousState = false;
                }
                first = false;
            }

            if(data.getInt(8) == 1)
            {
                databaseHelperFinalData.addData(IDofExercise, data.getString(2), data.getString(3),
                        data.getInt(4), new double[]{0, 0, 0});

                previousState = true;
            }
            else
            {
                if(previousState)
                {
                    intervalsNr++;

                    xX[0] = changeStateTimesX.get(intervalsNr).first;
                    xY[0] = changeStateTimesY.get(intervalsNr).first;
                    xZ[0] = changeStateTimesZ.get(intervalsNr).first;

                    yX[0] = changeStateTimesX.get(intervalsNr).second;
                    yY[0] = changeStateTimesY.get(intervalsNr).second;
                    yZ[0] = changeStateTimesZ.get(intervalsNr).second;

                    intervalsNr++;

                    xX[1] = changeStateTimesX.get(intervalsNr).first;
                    xY[1] = changeStateTimesY.get(intervalsNr).first;
                    xZ[1] = changeStateTimesZ.get(intervalsNr).first;

                    yX[1] = changeStateTimesX.get(intervalsNr).second;
                    yY[1] = changeStateTimesY.get(intervalsNr).second;
                    yZ[1] = changeStateTimesZ.get(intervalsNr).second;
                }

                previousState = false;

                databaseHelperFinalData.addData(IDofExercise, data.getString(2), data.getString(3), data.getInt(4),
                        new double[]{data.getDouble(9) - linearFunction(xX, yX, data.getInt(4)),
                                data.getDouble(10) - linearFunction(xY, yY, data.getInt(4)),
                                data.getDouble(11) - linearFunction(xZ, yZ, data.getInt(4))});

            }
        }

        data.close();
        databaseHelperFinalData.close();
        databaseHelperProcessedData.close();
    }

    private double linearFunction(int[] x, double[] y, double value)
    {
        double a = (y[1] - y[0])/(x[1] - x[0]);
        double b = y[0] - a * x[0];

        return (a*value + b);
    }
}
