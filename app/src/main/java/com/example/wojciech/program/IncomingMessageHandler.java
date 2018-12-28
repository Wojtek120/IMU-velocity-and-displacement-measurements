package com.example.wojciech.program;

import android.content.Context;
import android.os.Handler;
import android.util.Log;


/**
 * Handler obslugujacy przychodzace dane z bluetooth
 */
public class IncomingMessageHandler extends Handler
{
    private int handlerState;
    private StringBuilder recDataString = new StringBuilder();
    private DatabaseHelper mDatabaseHelper;
    private Context mContextMain;
    private  long IDofExercise;

    BluetoothConnection BT;

    public IncomingMessageHandler(int handlerState, Context context)
    {
        this.handlerState = handlerState;
        this.mContextMain = context;
        this.IDofExercise = -1;

        BT = (BluetoothConnection)mContextMain.getApplicationContext();


        mDatabaseHelper = new DatabaseHelper(mContextMain, "aaa");
    }

    public void handleMessage(android.os.Message msg)
    {
        boolean insertData;
        String nameOfExercise = "cwiczenie"; //TODO trzeba skas to pobierac
        double[] RawData = new double[9];


        if (msg.what == handlerState && BT.getCollectDataState()) //sprawdzeneinie czy wiadomosc jest tym co chcemy
        {
            //nadawanie unikalnego ID
            if(BT.getCollectDataStateOnChange())
            {
                IDofExercise  = System.currentTimeMillis() / 1000;
                BT.setCollectDataStateOnChange(false);
            }


            String readMessage = (String) msg.obj;                                                                // msg.arg1 = bajty z connect thread
            recDataString.append(readMessage);                                      //dodawaj do stringa az ~
            int endOfLineIndex = recDataString.indexOf("~");                    //znajdz koniec
            if (endOfLineIndex > 0)
            {                                           //czy na pewno są dane przed ~
                String dataInPrint = recDataString.substring(0, endOfLineIndex);    //wyciagnij stringa
                //txtString.setText("Data Received = " + dataInPrint);
                int dataLength = dataInPrint.length();                          //wez dlugosc
                //txtStringLength.setText("String Length = " + String.valueOf(dataLength));

                if (recDataString.charAt(0) == '#')                             //jesli zaczyna sie od # to na pewno to co chcecmy
                {
                    //TODO TESTOWE DANE DO SQL
                    String sensor1 = recDataString.substring(6, 10);            //same again...
                    String sensor2 = recDataString.substring(11, 15);
                    String sensor3 = recDataString.substring(16, 20);

                    for(int i = 0; i<9; i=i+3 )
                    {
                        RawData[i] = Double.valueOf(sensor1);
                        RawData[i+1] = Double.valueOf(sensor2);
                        RawData[i+2] = Double.valueOf(sensor3);
                    }
                    //KONIEC TESTOWYCH DANYCH

                    insertData = mDatabaseHelper.addData(IDofExercise, nameOfExercise, RawData);

                    if(insertData)
                    {
                        Log.i("Dodawanie do SQL", "Sukces");
                    }
                    else
                    {
                        Log.e("Dodawanie do SQL", "PORAZKA");
                    }





                    /*String sensor0 = recDataString.substring(1, 5);             //get sensor value from string between indices 1-5
                    String sensor1 = recDataString.substring(6, 10);            //same again...
                    String sensor2 = recDataString.substring(11, 15);
                    String sensor3 = recDataString.substring(16, 20);

                    sensorView0.setText(" Sensor 0 Voltage = " + sensor0 + "V");    //update the textviews with sensor values
                    sensorView1.setText(" Sensor 1 Voltage = " + sensor1 + "V");
                    sensorView2.setText(" Sensor 2 Voltage = " + sensor2 + "V");
                    sensorView3.setText(" Sensor 3 Voltage = " + sensor3 + "V");*/
                }
                recDataString.delete(0, recDataString.length());                    //wyczysc
                // strIncom =" ";
                dataInPrint = " ";
            }
        }
    }
}
