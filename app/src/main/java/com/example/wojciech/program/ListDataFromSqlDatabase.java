package com.example.wojciech.program;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

/**
 * Klasa, ktora sluzy do wyswietlenia wszystkich serii pomiarow (po jednym zapisie) aby moc wybrac ktorys z nich
 * w celu dalszego przejzenia
 */
public class ListDataFromSqlDatabase extends AppCompatActivity
{
    /** TAG */
    private static final String TAG = "ListDataActivity";

    /** Database helper do obslugi bazy danych */
    DatabaseHelper mDatabaseHelper;

    /** ListView w ktorym wyswietlane sa pomiary */
    private ListView mListView;

    /** Klasa aplikacji obslugujaca bluetooth */
    BluetoothConnection BT;

    /** List adapter do wypisywania cwiczen i ich dat */
    private ExerciseListAdapter mExerciseListAdapter;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        BT = (BluetoothConnection)getApplicationContext();

        setContentView(R.layout.list_data_from_sql_layout);
        mListView = findViewById(R.id.listViewSqlData);

        mDatabaseHelper = new DatabaseHelper(this, "aaa"); //TODO tutaj powinna byc przekazana nazwa databasu
    }


    @Override
    protected void onResume()
    {
        super.onResume();
        BT.setContextAndRegisterReceivers(this);
        listIDS();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        BT.unregisterBroadcastReceiver();
    }

    /**
     * Funkcja wyswietlajaca wysrodkowana wiadomosc
     *
     * @param message - wiadomosc do wyswietlenia
     */
    private void showMessage(String message)
    {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        TextView vi = toast.getView().findViewById(android.R.id.message);
        if (vi != null) vi.setGravity(Gravity.CENTER);
        toast.show();
    }


    /**
     * Wypisuje wszytskie pojawiajace sie ID raz, aby mozna bylo dalej wybrac to ktore chcemy obejzec
     */
    private void listIDS()
    {
        Log.d(TAG, "listing ID's");

        //wez dane i dolacz do listy
        Cursor data = mDatabaseHelper.getIDS();
        final ArrayList<String[]> listData = new ArrayList<>();
        while(data.moveToNext())
        {
            String[] nameAndDate = new String[3];
            //dane z wszystkich kolumn wpisane do String i pozniej wypisane w listData
            nameAndDate[0] = data.getString(2);
            nameAndDate[1] = data.getString(1);
            nameAndDate[2] = data.getString(0); //ID
            listData.add(nameAndDate);
        }


        mExerciseListAdapter = new ExerciseListAdapter(this, R.layout.records_adapter_view, listData);
        mListView.setAdapter(mExerciseListAdapter);



        //gdy kilkniemy
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
            {
                //pozyskanie id
                String ID = listData.get(i)[2];

                Log.i(TAG, "Kliknieto " + ID);

                //uruchamianie nowej aktywnosci gdzie wyswietlone klikniete dane
                Intent listDataScreenIntent = new Intent(ListDataFromSqlDatabase.this, ListDataFromSqlDatabaseBySelectedId.class);
                listDataScreenIntent.putExtra("selectedID", Integer.parseInt(ID));
                startActivity(listDataScreenIntent);
            }
        });
    }


}
