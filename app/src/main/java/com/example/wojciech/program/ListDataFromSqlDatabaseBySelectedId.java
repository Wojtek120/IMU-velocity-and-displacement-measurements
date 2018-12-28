package com.example.wojciech.program;

import android.content.Intent;
import android.database.Cursor;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

public class ListDataFromSqlDatabaseBySelectedId extends AppCompatActivity
{

    private static final String TAG = "ListDataActivity";
    DatabaseHelper mDatabaseHelper;
    private ListView mListView;
    private EditText editTextWithNewName;
    int ID;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_data_from_sql_database_by_selected_id);
        mListView = findViewById(R.id.listViewSqlData);
        editTextWithNewName = findViewById(R.id.editTextWithNewName);
        mDatabaseHelper = new DatabaseHelper(this, "aaa"); //TODO tutaj powinna byc przekazana nazwa databasu

        //pobierz ID do wyswietlenia
        Intent receivedIntent = getIntent();
        ID = receivedIntent.getIntExtra("selectedID", -1);

        listData(ID);
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
     * Wypisuje dane z bazy danych do ListView
     * @param IDinSQL - nazwa ID w bazie danych do ktorego ma zostac zwrocone ID, jesli -1 to zwrocone wszytskie dane
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
     * @param v
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
     * Przycisk usuwajacy wywietlony wpis
     * @param v
     */
    public void btnDeleteData(View v)
    {
        mDatabaseHelper.deleteExercise(ID);
        showMessage(this.getString(R.string.deleted));
        finish();
    }
}
