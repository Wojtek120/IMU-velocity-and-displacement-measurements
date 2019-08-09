package com.example.wojciech.program;

import android.content.Context;
import android.database.Cursor;
import android.util.Pair;

import java.util.Vector;

/**
 * Klasa w której są przetwarzane dany - z pomiarów pozyskiwane wyniki prędkości oraz przyspieszeń
 */
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

    /**
     * Konstruktor
     * @param context - kontekst
     */
    public DataProcessing(Context context)
    {
        this.context = context;
        changeStateTimesX = new Vector<>();
        changeStateTimesY = new Vector<>();
        changeStateTimesZ = new Vector<>();
    }

    /**
     * Wczytanie danych i obliczanie orientacji, predkosci, kompensacja jej i przemieszczen
     */
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

        double velocityX = 0;
        double velocityY = 0;
        double velocityZ = 0;

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
                    velocityX = integralVelocityX.integrate(data.getInt(4) - firstControlNr, nextTime - firstControlNr, compensatedGravity[0], nextCompensatedGravity[0]);
                    velocityY = integralVelocityY.integrate(data.getInt(4) - firstControlNr, nextTime - firstControlNr, compensatedGravity[1], nextCompensatedGravity[1]);
                    velocityZ = integralVelocityZ.integrate(data.getInt(4) - firstControlNr, nextTime - firstControlNr, compensatedGravity[2], nextCompensatedGravity[2]);

//                    velocityX = 0;
//                    velocityY = 0;
//                    velocityZ = 0;
//                    integralVelocityX.setToZero();
//                    integralVelocityY.setToZero();
//                    integralVelocityZ.setToZero();

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
                    velocityX = integralVelocityX.integrate(data.getInt(4) - firstControlNr, nextTime - firstControlNr, compensatedGravity[0], nextCompensatedGravity[0]);
                    velocityY = integralVelocityY.integrate(data.getInt(4) - firstControlNr, nextTime - firstControlNr, compensatedGravity[1], nextCompensatedGravity[1]);
                    velocityZ = integralVelocityZ.integrate(data.getInt(4) - firstControlNr, nextTime - firstControlNr, compensatedGravity[2], nextCompensatedGravity[2]);


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

            if(data.isLast())
            {
                if(changeStateTimesX.isEmpty())
                {
                    changeStateTimesX.add(new Pair<>(0, 0d));
                    changeStateTimesY.add(new Pair<>(0, 0d));
                    changeStateTimesZ.add(new Pair<>(0, 0d));
                    changeStateTimesX.add(new Pair<>(data.getInt(4) - firstControlNr, velocityX));
                    changeStateTimesY.add(new Pair<>(data.getInt(4) - firstControlNr, velocityY));
                    changeStateTimesZ.add(new Pair<>(data.getInt(4) - firstControlNr, velocityZ));
                }
                else if(changeStateTimesX.size() % 2 == 1)
                {
                    changeStateTimesX.add(new Pair<>(data.getInt(4) - firstControlNr, velocityX));
                    changeStateTimesY.add(new Pair<>(data.getInt(4) - firstControlNr, velocityY));
                    changeStateTimesZ.add(new Pair<>(data.getInt(4) - firstControlNr, velocityZ));
                }
            }
        }

        data.close();
        databaseHelper.close();
        databaseHelperRPY.close();
        databaseHelperProcessedData.close();

        compensateVelocityAndComputeDisplacement();

    }

    /**
     * Kompensacji predkosci i obliczanie przemieszczen
     */
    private void compensateVelocityAndComputeDisplacement()
    {
        DatabaseHelperProcessedData databaseHelperProcessedData = new DatabaseHelperProcessedData(context);
        DatabaseHelperFinalData databaseHelperFinalData = new DatabaseHelperFinalData(context);

        Cursor data = databaseHelperProcessedData.getData(IDofExercise);

        Integral integralDisplacementX = new Integral();
        Integral integralDisplacementY = new Integral();
        Integral integralDisplacementZ = new Integral();

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

            if(data.getInt(8) == 1 && !data.isLast())
            {
                data.moveToNext();
                double nextTime = data.getInt(4);
                data.moveToPrevious();

                double displacementX = integralDisplacementX.integrate(data.getInt(4), nextTime, 0, 0);
                double displacementY = integralDisplacementY.integrate(data.getInt(4), nextTime, 0, 0);
                double displacementZ = integralDisplacementZ.integrate(data.getInt(4), nextTime, 0, 0);

                databaseHelperFinalData.addData(IDofExercise, data.getString(2), data.getString(3),
                        data.getInt(4), new double[]{0, 0, 0}, new double[]{displacementX, displacementY, displacementZ});

                previousState = true;
            }
            else if(!data.isLast())
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

                //calkowanie
                double []CompensatedVelocity = new double[]{data.getDouble(9) - linearFunction(xX, yX, data.getInt(4)),
                        data.getDouble(10) - linearFunction(xY, yY, data.getInt(4)),
                        data.getDouble(11) - linearFunction(xZ, yZ, data.getInt(4))};
                data.moveToNext();
                double nextTime = data.getInt(4);
                double []nextCompensatedVelocity = new double[]{data.getDouble(9) - linearFunction(xX, yX, data.getInt(4)),
                        data.getDouble(10) - linearFunction(xY, yY, data.getInt(4)),
                        data.getDouble(11) - linearFunction(xZ, yZ, data.getInt(4))};
                data.moveToPrevious();

                double displacementX = integralDisplacementX.integrate(data.getInt(4), nextTime, CompensatedVelocity[0], nextCompensatedVelocity[0]);
                double displacementY = integralDisplacementY.integrate(data.getInt(4), nextTime, CompensatedVelocity[1], nextCompensatedVelocity[1]);
                double displacementZ = integralDisplacementZ.integrate(data.getInt(4), nextTime, CompensatedVelocity[2], nextCompensatedVelocity[2]);


//                CompensatedVelocity[2] = linearFunction(xX, yX, data.getInt(4));
//                databaseHelperFinalData.addData(IDofExercise, data.getString(2), data.getString(3), data.getInt(4),
//                        CompensatedVelocity, new double[]{displacementX, displacementY, displacementZ});
                databaseHelperFinalData.addData(IDofExercise, data.getString(2), data.getString(3), data.getInt(4),
                        CompensatedVelocity, new double[]{displacementX, displacementY, displacementZ});

            }
        }

        data.close();
        databaseHelperFinalData.close();
        databaseHelperProcessedData.close();
    }

    /**
     * Obliczenie wartosci funkcji liniowej na podstawie dwoch podanych punktow
     * @param x - wspolrzedne x punktow
     * @param y - wspolrzedne y punktow
     * @param value - wpolrzedna x punktu do obliczenia jego wartosci
     * @return wartosc y
     */
    private double linearFunction(int[] x, double[] y, double value)
    {
        double a = (y[1] - y[0])/(x[1] - x[0]);
        double b = y[0] - a * x[0];

        return (a*value + b);
    }
}
