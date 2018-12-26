package com.example.wojciech.program;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class ListDataFromSqlDatabase extends AppCompatActivity
{
    private static final String TAG = "ListDataActivity";
    DatabaseHelper mDatabaseHelper;
    private ListView mListView;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_data_from_sql_layout);
        mListView = findViewById(R.id.listViewSqlData);
        mDatabaseHelper = new DatabaseHelper(this, "aaa");

        listAllData();
    }

    private void listAllData()
    {
        Log.d(TAG, "listing all data");

        //wez dane i dolacz do listy
        Cursor data = mDatabaseHelper.getData();
        ArrayList<String> listData = new ArrayList<>();
        while(data.moveToNext())
        {
            //dane z pierwszej kolumny dodane do ArrayList
            listData.add(data.getString(3));
        }


        //list adapter
        ListAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listData);
        mListView.setAdapter(adapter);
    }
}
