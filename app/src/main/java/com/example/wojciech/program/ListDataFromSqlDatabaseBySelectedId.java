package com.example.wojciech.program;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;

/**
 * Klasa sluzaca do wyswietlenia wszystkich danych pomiarowych posiadajacych okreslony ID,
 * w niej tez mozna zminiac nazwe serii pomiarow, badz usuwac ja
 */
public class ListDataFromSqlDatabaseBySelectedId extends AppCompatActivity
{
    /** TAG */
    private static final String TAG = "ListDataActivity";

    /** Database helper do obslugi bazy danych */
    DatabaseHelper mDatabaseHelper;

    /** ListView w ktorym wyswietlane sa pomiary */
    private ListView mListView;

    /** EditText w ktorym uzytkownik wpisuje nowa nazwe serii */
    private EditText editTextWithNewName;

    /** ID serii pomiarowej, ktora jest wyswietlana */
    int ID;

    /** Klasa aplikacji obslugujaca bluetooth */
    BluetoothConnection BT;

    /** Kontekst */
    private Context mContext = this;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        BT = (BluetoothConnection)getApplicationContext();

        setContentView(R.layout.activity_list_data_from_sql_database_by_selected_id);
        mListView = findViewById(R.id.listViewSqlData);
        editTextWithNewName = findViewById(R.id.editTextWithNewName);
        mDatabaseHelper = new DatabaseHelper(this, "aaa"); //TODO tutaj powinna byc przekazana nazwa databasu

        //pobierz ID do wyswietlenia
        Intent receivedIntent = getIntent();
        ID = receivedIntent.getIntExtra("selectedID", -1);

        listData(ID);
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

    /**
     * Funkcja wyswietlajaca wysrodkowana wiadomosc
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
     * Wypisuje dane z bazy danych do ListView
     * @param IDinSQL - ID w bazie danych serii pomiarowej której dane maja zostac zwrocone, jesli -1 to zwrocone wszytskie dane
     */
    private void listData(int IDinSQL)
    {
        Log.d(TAG, "listing all data");
        StringBuilder allDataToShow = new StringBuilder();

        int numberOfColumns = mDatabaseHelper.getNumberOfColumns();

        //wez dane i dolacz do listy
        Cursor data = mDatabaseHelper.getData(IDinSQL);
        ArrayList<String> listData = new ArrayList<>();
        while(data.moveToNext())
        {
            allDataToShow.delete(0, allDataToShow.length());;
            //dane z wszystkich kolumn wpisane do String i pozniej wypisane w listData
            for(int i = 0; i < numberOfColumns; i++)
            {
                allDataToShow.append(data.getString(i));
                allDataToShow.append("; ");
            }
            listData.add(allDataToShow.toString());
        }


        //list adapter
        ListAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listData);
        mListView.setAdapter(adapter);
    }


    /**
     * Przycisk potwierdzajacy zmiane nazwy cwiczenia
     */
    public void btnChangeName(View v)
    {
        String newName = editTextWithNewName.getText().toString();

        if(!newName.equals(""))
        {
            mDatabaseHelper.updateExerciseName(newName, ID);
            listData(ID);
        }
        else
        {
            showMessage(this.getString(R.string.you_must_enter_the_name));
        }
    }


    /**
     * Przycisk usuwajacy wywietlony wpis, po wcisnieciu pokazuje sie okno dialogowe, ktore pyta, czy na pewno chcesz usunac
     */
    public void btnDeleteData(View v)
    {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(this.getString(R.string.delete_ask));
        alertDialog.setMessage(this.getString(R.string.do_u_want_to_delete));
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, this.getString(R.string.delete), new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
                mDatabaseHelper.deleteExercise(ID);
                showMessage(mContext.getString(R.string.deleted));
                finish();
            }
        });

        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, this.getString(R.string.cancel), new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
                dialog.dismiss();
            }
        });
        alertDialog.show();

    }
}
