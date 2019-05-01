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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;


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

        DataPoint[] dataPointGyroX = new DataPoint[data2.getCount()];
        DataPoint[] dataPointGyroY = new DataPoint[data2.getCount()];
        DataPoint[] dataPointGyroZ = new DataPoint[data2.getCount()];

        countData = 0;
        int firstControlNr = 1;

        while(data2.moveToNext())
        {
            if(first)
            {
                firstControlNr = data2.getInt(4);
                first = false;
            }

            dataPointAccelX[countData] = new DataPoint(data2.getInt(4) - firstControlNr, data2.getDouble(5));
            dataPointAccelY[countData] = new DataPoint(data2.getInt(4) - firstControlNr, data2.getDouble(6));
            dataPointAccelZ[countData] = new DataPoint(data2.getInt(4) - firstControlNr, data2.getDouble(7));

            dataPointGyroX[countData] = new DataPoint(data2.getInt(4) - firstControlNr, data2.getDouble(8));
            dataPointGyroY[countData] = new DataPoint(data2.getInt(4) - firstControlNr, data2.getDouble(9));
            dataPointGyroZ[countData] = new DataPoint(data2.getInt(4) - firstControlNr, data2.getDouble(10));
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

        LineGraphSeries<DataPoint> seriesGyroX = new LineGraphSeries<DataPoint>(dataPointGyroX);
        seriesGyroX.setColor(Color.BLACK);
        LineGraphSeries<DataPoint> seriesGyroY = new LineGraphSeries<DataPoint>(dataPointGyroY);
        seriesGyroY.setColor(Color.BLACK);
        LineGraphSeries<DataPoint> seriesGyroZ = new LineGraphSeries<DataPoint>(dataPointGyroZ);
        seriesGyroZ.setColor(Color.BLACK);

        graph2.addSeries(seriesGyroX);
        graph2.addSeries(seriesGyroY);
        graph2.addSeries(seriesGyroZ);

        graph2.getViewport().setScalable(true);

        ////////////////////////////////////////////////SKOMPENSOWANE///////////////////////////////////////////////////////
        DatabaseHelperProcessedData databaseHelperProcessedData = new DatabaseHelperProcessedData(this);
        Cursor data3 = databaseHelperProcessedData.getData(IDinSQL);
        GraphView graph3 = findViewById(R.id.graph3);
        GraphView graph4 = findViewById(R.id.graph4);

        DataPoint[] dataPointAccelCompensatedX = new DataPoint[data3.getCount()];
        DataPoint[] dataPointAccelCompensatedY = new DataPoint[data3.getCount()];
        DataPoint[] dataPointAccelCompensatedZ = new DataPoint[data3.getCount()];
        DataPoint[] dataPointStaticIntervals = new DataPoint[data3.getCount()];

        DataPoint[] dataPointVelX = new DataPoint[data3.getCount()];
        DataPoint[] dataPointVelY = new DataPoint[data3.getCount()];
        DataPoint[] dataPointVelZ = new DataPoint[data3.getCount()];

        countData = 0;

        while(data3.moveToNext())
        {
            dataPointAccelCompensatedX[countData] = new DataPoint(data3.getInt(4), data3.getDouble(5));
            dataPointAccelCompensatedY[countData] = new DataPoint(data3.getInt(4), data3.getDouble(6));
            dataPointAccelCompensatedZ[countData] = new DataPoint(data3.getInt(4), data3.getDouble(7));
            dataPointStaticIntervals[countData] = new DataPoint(data3.getInt(4), data3.getDouble(8));

            dataPointVelX[countData] = new DataPoint(data3.getInt(4), data3.getDouble(9));
            dataPointVelY[countData] = new DataPoint(data3.getInt(4), data3.getDouble(10));
            dataPointVelZ[countData] = new DataPoint(data3.getInt(4), data3.getDouble(11));

            countData++;
        }

        data3.close();
        databaseHelperProcessedData.close();

        LineGraphSeries<DataPoint> seriesAccelCompensatedX = new LineGraphSeries<DataPoint>(dataPointAccelCompensatedX);
        LineGraphSeries<DataPoint> seriesAccelCompensatedY = new LineGraphSeries<DataPoint>(dataPointAccelCompensatedY);
        seriesAccelCompensatedY.setColor(Color.RED);
        LineGraphSeries<DataPoint> seriesAccelCompensatedZ = new LineGraphSeries<DataPoint>(dataPointAccelCompensatedZ);
        seriesAccelCompensatedZ.setColor(Color.GREEN);
        LineGraphSeries<DataPoint> seriesStaticIntervals = new LineGraphSeries<DataPoint>(dataPointStaticIntervals);
        seriesStaticIntervals.setColor(Color.BLACK);

        graph3.addSeries(seriesAccelCompensatedX);
        graph3.addSeries(seriesAccelCompensatedY);
        graph3.addSeries(seriesAccelCompensatedZ);
        graph3.addSeries(seriesStaticIntervals);

        graph3.getViewport().setScalable(true);

        LineGraphSeries<DataPoint> seriesVelX = new LineGraphSeries<DataPoint>(dataPointVelX);
        LineGraphSeries<DataPoint> seriesVelY = new LineGraphSeries<DataPoint>(dataPointVelY);
        seriesVelY.setColor(Color.RED);
        LineGraphSeries<DataPoint> seriesVelZ = new LineGraphSeries<DataPoint>(dataPointVelZ);
        seriesVelZ.setColor(Color.GREEN);

        graph4.addSeries(seriesVelX);
        graph4.addSeries(seriesVelY);
        graph4.addSeries(seriesVelZ);

        graph4.getViewport().setScalable(true);

        ////////////////////////////////////////////////PREDKOSCI SKOMPENSOWANE///////////////////////////////////////////////////////
        DatabaseHelperFinalData databaseHelperFinalData = new DatabaseHelperFinalData(this);
        Cursor data4 = databaseHelperFinalData.getData(IDinSQL);
        GraphView graph5 = findViewById(R.id.graph5);
        GraphView graph6 = findViewById(R.id.graph6);

        DataPoint[] dataPointVelCompensatedX = new DataPoint[data4.getCount()];
        DataPoint[] dataPointVelCompensatedY = new DataPoint[data4.getCount()];
        DataPoint[] dataPointVelCompensatedZ = new DataPoint[data4.getCount()];

        DataPoint[] dataDisplacementX = new DataPoint[data4.getCount()];
        DataPoint[] dataDisplacementY = new DataPoint[data4.getCount()];
        DataPoint[] dataDisplacementZ = new DataPoint[data4.getCount()];

        countData = 0;

        while(data4.moveToNext())
        {
            dataPointVelCompensatedX[countData] = new DataPoint(data4.getInt(4), data4.getDouble(5));
            dataPointVelCompensatedY[countData] = new DataPoint(data4.getInt(4), data4.getDouble(6));
            dataPointVelCompensatedZ[countData] = new DataPoint(data4.getInt(4), data4.getDouble(7));

            dataDisplacementX[countData] = new DataPoint(data4.getInt(4), data4.getDouble(9));
            dataDisplacementY[countData] = new DataPoint(data4.getInt(4), data4.getDouble(10));
            dataDisplacementZ[countData] = new DataPoint(data4.getInt(4), data4.getDouble(11));

            countData++;
        }

        data4.close();
        databaseHelperFinalData.close();


        LineGraphSeries<DataPoint> seriesVelCompensatedX = new LineGraphSeries<DataPoint>(dataPointVelCompensatedX);
        LineGraphSeries<DataPoint> seriesVelCompensatedY = new LineGraphSeries<DataPoint>(dataPointVelCompensatedY);
        seriesVelCompensatedY.setColor(Color.RED);
        LineGraphSeries<DataPoint> seriesVelCompensatedZ = new LineGraphSeries<DataPoint>(dataPointVelCompensatedZ);
        seriesVelCompensatedZ.setColor(Color.GREEN);

        LineGraphSeries<DataPoint> seriesDisplacementX = new LineGraphSeries<DataPoint>(dataDisplacementX);
        LineGraphSeries<DataPoint> seriesDisplacementY = new LineGraphSeries<DataPoint>(dataDisplacementY);
        seriesDisplacementY.setColor(Color.RED);
        LineGraphSeries<DataPoint> seriesDisplacementZ = new LineGraphSeries<DataPoint>(dataDisplacementZ);
        seriesDisplacementZ.setColor(Color.GREEN);

        graph5.addSeries(seriesVelCompensatedX);
        graph5.addSeries(seriesVelCompensatedY);
        graph5.addSeries(seriesVelCompensatedZ);

        graph5.getViewport().setScalable(true);

        graph6.addSeries(seriesDisplacementX);
        graph6.addSeries(seriesDisplacementY);
        graph6.addSeries(seriesDisplacementZ);

        graph6.getViewport().setScalable(true);
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
