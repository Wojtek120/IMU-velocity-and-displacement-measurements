package com.example.wojciech.program;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
{
    BluetoothConnection BT;
    private EditText editTextWithNewName;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BT = (BluetoothConnection)getApplicationContext();
        BT.SetContextMain(this);


        editTextWithNewName = findViewById(R.id.editTextExerciseName);
    }


    @Override
    protected void onResume()
    {
        super.onResume();
        BT.setContextAndRegisterReceivers(this);
        BluetoothConnection.enableBluetooth(this);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        BT.unregisterBroadcastReceiver();
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

        //gdy wybierzemy menu z liste danych z sql
        if (item.getItemId() == R.id.listOfDataFromSqlMenu)
        {
            Intent mIntentMenuBluetooth  = new Intent(this, ListDataFromSqlDatabase.class);
            startActivity(mIntentMenuBluetooth);
        }

        return true;
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }



    /**
     * Przycisk uruchamiajacy zapisywanie danych do bazy SQL
     * @param v
     */
    public void btnCollectData(View v)
    {
        //jesli polaczony
        if(BT.getConnectionStatus())
        {
            //jesli wprowadzono nazwe cwiczenia
            String nameOfExercise = editTextWithNewName.getText().toString();

            if(!nameOfExercise.equals(""))
            {
                BT.setExerciseName(nameOfExercise);
                BT.collectDataStateChange();
            }
            else
            {
                showMessage(this.getString(R.string.you_must_enter_the_name));
            }

        }
        else
        {
            showMessage(this.getString(R.string.connect_first));
            Intent mIntentMenuBluetooth  = new Intent(this, BluetoothMenu.class);
            startActivity(mIntentMenuBluetooth);
        }
    }

    //testy
    public void diodeOn(View v)
    {
        EditText et = findViewById(R.id.editText);
        String s = et.getText().toString();
        BT.write(s);
    }


    /**
     * Funkcja wyswietlajaca wysrodkowana wiadomosc
     *
     * @param message - wiadomosc do wyswietlenia
     */
    private void showMessage(String message)
    {
        Toast toast = Toast.makeText(this.getApplicationContext(), message, Toast.LENGTH_SHORT);
        TextView vi = toast.getView().findViewById(android.R.id.message);
        if (vi != null) vi.setGravity(Gravity.CENTER);
        toast.show();
    }

}
