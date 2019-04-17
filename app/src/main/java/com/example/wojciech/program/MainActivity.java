package com.example.wojciech.program;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


/**
 * Klasa z glowna aktywnoscia
 * @author Wojciech Buczko
 */
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

    /**
     * Obsluga wyboru elementu z menu aplikacji
     */
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
     * Funkcja obslugujaca przycisk uruchamiajacy zapis danych do bazy SQL,
     * sprawdza, czy jestesmy polaczeni z urzadzeniem przez bluetooth oraz czy wpisano nazwe cwiczenia
     * gdy brak polaczenia przenosi do aktwnosci, w ktorej mozna ustnowic polaczenie
     * @see BluetoothMenu
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

                AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                alertDialog.setTitle(this.getString(R.string.data_collecting));
                alertDialog.setMessage(this.getString(R.string.data_collecting));
                alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, this.getString(R.string.stop), new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        BT.collectDataStateChange();

                        //oblicz katy YPR w innym watku
                        Thread t = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                DatabaseHelperRPY databaseHelperRPY = new DatabaseHelperRPY(getApplicationContext());
                                databaseHelperRPY.calculateRPY();
                            }});

                        t.start();

                        //czekaj na skonczenie watku
                        try
                        {
                            t.join();
                        } catch (InterruptedException e)
                        {
                            e.printStackTrace();
                        }
                        //finish();
                    }
                });
                alertDialog.show();
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
    /*public void diodeOn(View v)
    {
        EditText et = findViewById(R.id.editText);
        String s = et.getText().toString();
        BT.write(s);
    }*/


    /**
     * Funkcja wyswietlajaca wysrodkowana wiadomosc
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
