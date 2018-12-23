package com.example.wojciech.program;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class BluetoothMenu extends AppCompatActivity
{
    BluetoothConnection BT;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_menu);

        BT = new BluetoothConnection(this);


        BT.discoverDevices();
        BT.listPairedDevices();
    }

    @Override
    protected void onDestroy()
    {
        BT.unregisterBroadcastReceiver();
        BT.closeBluetoothSocket();

        super.onDestroy();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
    }
}
