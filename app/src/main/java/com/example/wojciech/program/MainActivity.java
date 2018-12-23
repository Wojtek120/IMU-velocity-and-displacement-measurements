package com.example.wojciech.program;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
{
    BluetoothConnection BT;




    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        BluetoothConnection.enableBluetooth(this);

        //BT = new BluetoothConnection(this);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item)
    {
        //gdy wybierzemy menu z polaczeniem bluetooth
        if (item.getItemId() == R.id.bluetoothConnectionMenu)
        {
            Intent mIntentMenuBluetooth  = new Intent(this, BluetoothMenu.class);
            startActivity(mIntentMenuBluetooth);
        }

        return true;
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }


    //TESTY
    public void btnEnableBluetooth(View v)
    {
        BluetoothConnection.enableBluetooth(this);
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
