package com.example.wojciech.program;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
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

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

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
        mDatabaseHelper = new DatabaseHelper(this);

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
        DatabaseHelperRPY databaseHelperRPY = new DatabaseHelperRPY(this);
        Cursor data = databaseHelperRPY.getData(IDinSQL);



        GraphView graph = findViewById(R.id.graph);

        DataPoint[] dataPointYaw = new DataPoint[data.getCount()];
        DataPoint[] dataPointPitch = new DataPoint[data.getCount()];
        DataPoint[] dataPointRoll = new DataPoint[data.getCount()];



        int countData = 0;
        boolean first = true;
        int firstNumber = 0;


        while(data.moveToNext())
        {
            dataPointYaw[countData] = new DataPoint(data.getInt(4), data.getDouble(5));
            dataPointPitch[countData] = new DataPoint(data.getInt(4), data.getDouble(6));
            dataPointRoll[countData] = new DataPoint(data.getInt(4), data.getDouble(7));
            countData++;
        }

        data.close();
        databaseHelperRPY.close();



        LineGraphSeries<DataPoint> seriesYaw = new LineGraphSeries<DataPoint>(dataPointYaw);
        LineGraphSeries<DataPoint> seriesPitch = new LineGraphSeries<DataPoint>(dataPointPitch);
        seriesPitch.setColor(Color.RED);
        LineGraphSeries<DataPoint> seriesRoll = new LineGraphSeries<DataPoint>(dataPointRoll);
        seriesRoll.setColor(Color.GREEN);

        graph.addSeries(seriesYaw);
        graph.addSeries(seriesPitch);
        graph.addSeries(seriesRoll);

        graph.getViewport().setScalable(true);


        ////////////////////////////////////////////////POMIARY///////////////////////////////////////////////////////
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        Cursor data2 = databaseHelper.getData(IDinSQL);
        GraphView graph2 = findViewById(R.id.graph2);

        DataPoint[] dataPointAccelX = new DataPoint[data2.getCount()];
        DataPoint[] dataPointAccelY = new DataPoint[data2.getCount()];
        DataPoint[] dataPointAccelZ = new DataPoint[data2.getCount()];

//        DataPoint[] dataPointGyroX = new DataPoint[data2.getCount()];
//        DataPoint[] dataPointGyroY = new DataPoint[data2.getCount()];
//        DataPoint[] dataPointGyroZ = new DataPoint[data2.getCount()];

        countData = 0;

        while(data2.moveToNext())
        {
            dataPointAccelX[countData] = new DataPoint(data2.getInt(4), data2.getDouble(5));
            dataPointAccelY[countData] = new DataPoint(data2.getInt(4), data2.getDouble(6));
            dataPointAccelZ[countData] = new DataPoint(data2.getInt(4), data2.getDouble(7));

//            dataPointGyroX[countData] = new DataPoint(countData, data2.getDouble(8));
//            dataPointGyroY[countData] = new DataPoint(countData, data2.getDouble(9));
//            dataPointGyroZ[countData] = new DataPoint(countData, data2.getDouble(10));
            countData++;
        }

        data2.close();
        databaseHelper.close();

        LineGraphSeries<DataPoint> seriesAccelX = new LineGraphSeries<DataPoint>(dataPointAccelX);
        LineGraphSeries<DataPoint> seriesAccelY = new LineGraphSeries<DataPoint>(dataPointAccelY);
        seriesAccelY.setColor(Color.RED);
        LineGraphSeries<DataPoint> seriesAccelZ = new LineGraphSeries<DataPoint>(dataPointAccelZ);
        seriesAccelZ.setColor(Color.GREEN);

        graph2.addSeries(seriesAccelX);
        graph2.addSeries(seriesAccelY);
        graph2.addSeries(seriesAccelZ);

//        LineGraphSeries<DataPoint> seriesGyroX = new LineGraphSeries<DataPoint>(dataPointGyroX);
//        seriesGyroX.setColor(Color.RED);
//        LineGraphSeries<DataPoint> seriesGyroY = new LineGraphSeries<DataPoint>(dataPointGyroY);
//        seriesGyroY.setColor(Color.RED);
//        LineGraphSeries<DataPoint> seriesGyroZ = new LineGraphSeries<DataPoint>(dataPointGyroZ);
//        seriesGyroZ.setColor(Color.RED);

//        graph2.addSeries(seriesGyroX);
//        graph2.addSeries(seriesGyroY);
//        graph2.addSeries(seriesGyroZ);

        graph2.getViewport().setScalable(true);

        ////////////////////////////////////////////////SKOMPENSOWANE///////////////////////////////////////////////////////
        DatabaseHelperFinalData databaseHelperFinalData = new DatabaseHelperFinalData(this);
        Cursor data3 = databaseHelperFinalData.getData(IDinSQL);
        GraphView graph3 = findViewById(R.id.graph3);

        DataPoint[] dataPointAccelCompensatedX = new DataPoint[data3.getCount()];
        DataPoint[] dataPointAccelCompensatedY = new DataPoint[data3.getCount()];
        DataPoint[] dataPointAccelCompensatedZ = new DataPoint[data3.getCount()];

        countData = 0;

        while(data3.moveToNext())
        {
            dataPointAccelCompensatedX[countData] = new DataPoint(data3.getInt(4), data3.getDouble(5));
            dataPointAccelCompensatedY[countData] = new DataPoint(data3.getInt(4), data3.getDouble(6));
            dataPointAccelCompensatedZ[countData] = new DataPoint(data3.getInt(4), data3.getDouble(7));

            countData++;
        }

        data3.close();
        databaseHelperFinalData.close();

        LineGraphSeries<DataPoint> seriesAccelCompensatedX = new LineGraphSeries<DataPoint>(dataPointAccelCompensatedX);
        LineGraphSeries<DataPoint> seriesAccelCompensatedY = new LineGraphSeries<DataPoint>(dataPointAccelCompensatedY);
        seriesAccelCompensatedY.setColor(Color.RED);
        LineGraphSeries<DataPoint> seriesAccelCompensatedZ = new LineGraphSeries<DataPoint>(dataPointAccelCompensatedZ);
        seriesAccelCompensatedZ.setColor(Color.GREEN);

        graph3.addSeries(seriesAccelCompensatedX);
        graph3.addSeries(seriesAccelCompensatedY);
        graph3.addSeries(seriesAccelCompensatedZ);

        graph3.getViewport().setScalable(true);

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
