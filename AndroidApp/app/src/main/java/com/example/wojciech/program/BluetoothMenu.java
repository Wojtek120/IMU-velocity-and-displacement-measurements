package com.example.wojciech.program;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


/**
 * Aktywnosc, w ktore wyswietlane sa sparowane urzadzenia oraz wyszukiwane
 */
public class BluetoothMenu extends AppCompatActivity
{
    BluetoothConnection BT;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_menu);

        BT = (BluetoothConnection)getApplicationContext();

        BT.setContextAndRegisterReceivers(this);
        BT.setTextViews();

        BluetoothConnection.enableBluetooth(this);

        BT.discoverDevices();
        BT.listPairedDevices();
    }


    @Override
    protected void onResume()
    {
        super.onResume();
        BT.setContextAndRegisterReceivers(this);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        BT.unregisterBroadcastReceiver();
    }

}
