package com.example.wojciech.program;

import android.content.Context;
import android.database.Cursor;

public class DataProcessing
{
    /** Kontekst */
    Context context;

    public DataProcessing(Context context)
    {
        this.context = context;
    }


    public void processData()
    {
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        DatabaseHelperRPY databaseHelperRPY = new DatabaseHelperRPY(context);
        DatabaseHelperProcessedData databaseHelperProcessedData = new DatabaseHelperProcessedData(context);
        MadgwickFilter madgwickFilter = new MadgwickFilter();
        int IDofExercise = databaseHelper.getLargestID();
        boolean first = true;

        ZeroVelocityUpdate zeroVelocity = new ZeroVelocityUpdate();

        Cursor data = databaseHelper.getData(IDofExercise);
        double yawAngle, pitchAngle, rollAngle;
        int firstControlNr = 1;

        Integral integralVelocityX = new Integral();
        Integral integralVelocityY = new Integral();
        Integral integralVelocityZ = new Integral();

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

                    databaseHelperProcessedData.addData(IDofExercise, data.getString(2), data.getString(3),
                            data.getInt(4) - firstControlNr, compensatedGravity, 1,
                            new double[]{integralVelocityX.integrate(data.getInt(4) - firstControlNr, nextTime - firstControlNr, compensatedGravity[0], nextCompensatedGravity[0]),
                                    integralVelocityY.integrate(data.getInt(4) - firstControlNr, nextTime - firstControlNr, compensatedGravity[1], nextCompensatedGravity[1]),
                                    integralVelocityZ.integrate(data.getInt(4) - firstControlNr, nextTime - firstControlNr, compensatedGravity[2], nextCompensatedGravity[2])});

                } else
                {
                    databaseHelperProcessedData.addData(IDofExercise, data.getString(2), data.getString(3),
                            data.getInt(4) - firstControlNr, compensatedGravity, 0,
                            new double[]{integralVelocityX.integrate(data.getInt(4) - firstControlNr, nextTime - firstControlNr, compensatedGravity[0], nextCompensatedGravity[0]),
                                    integralVelocityY.integrate(data.getInt(4) - firstControlNr, nextTime - firstControlNr, compensatedGravity[1], nextCompensatedGravity[1]),
                                    integralVelocityZ.integrate(data.getInt(4) - firstControlNr, nextTime - firstControlNr, compensatedGravity[2], nextCompensatedGravity[2])});
                }
            }

            //calkowanie
        }

        data.close();
        databaseHelper.close();
        databaseHelperRPY.close();
        databaseHelperProcessedData.close();

    }

    private void compensateVelocity()
    {

    }
}
