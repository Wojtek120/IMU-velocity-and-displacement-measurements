package com.example.wojciech.program;

import android.os.Handler;
import android.widget.TextView;

import java.io.Serializable;

/**
 * Handler obslugujacy przychodzace dane z bluetooth
 */
public class IncomingMessageHandler extends Handler  implements Serializable
{
    private int handlerState;
    private StringBuilder recDataString = new StringBuilder();

    public IncomingMessageHandler(int handlerState)
    {
        this.handlerState = handlerState;
    }

    public void handleMessage(android.os.Message msg)
    {
        if (msg.what == handlerState) //sprawdzeneinie czy wiadomosc jest tym co chcemy
        {
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
