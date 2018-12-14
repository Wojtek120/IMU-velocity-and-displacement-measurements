package com.example.wojciech.program;

import android.bluetooth.BluetoothDevice;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
{
    //TESTY
    BluetoothConnection BT;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BT = new BluetoothConnection(this);


    }


    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        BT.unregisterBroadcastReceiver();
        BT.closeBluetoothSocket();
    }


    //TESTY
    public void btnEnableBluetooth(View v)
    {
        BT.enableBluetooth();
    }

    //TESTY
    public void btnEnableDisable_Discoverable(View v)
    {
        BT.enableDiscoverableMode();
    }

    //TESTY
    public void btndiscoverDevices(View v)
    {
        BT.discoverDevices();
    }

    //testy
    public void diodeOn(View v)
    {
        EditText et = findViewById(R.id.editText);
        String s = et.getText().toString();
        BT.write(s);
    }



}
