package com.example.wojciech.program;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper
{
    private static final String TAG = "DatabaseHelper";
    private static String TABLE_NAME = "RawDataDatabase";
    private static String COL1_ID = "id";
    private static String COL2_EXERCISE = "exercise";
    private static String COL3_TIME = "time";
    private static String[] COL4_12 = {"accx", "acc_y", "acc_z", "gyro_x", "gyro_y", "gyro_z", "mag_x", "mag_y", "mag_z"};


    /**
     * Konstruktor
     * @param context - kontekst
     * @param name - nazwa tabeli
     */
    public DatabaseHelper(Context context, String name)
    {
        super(context, TABLE_NAME, null, 1); //TODO tablename nie jest uzywane
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase)
    {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL2_EXERCISE +" TEXT, " +
                COL3_TIME + " DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                COL4_12[0] + " REAL, " +
                COL4_12[1] + " REAL, " +
                COL4_12[2] + " REAL, " +
                COL4_12[3] + " REAL, " +
                COL4_12[4] + " REAL, " +
                COL4_12[5] + " REAL, " +
                COL4_12[6] + " REAL, " +
                COL4_12[7] + " REAL, " +
                COL4_12[8] + " REAL)";

        sqLiteDatabase.execSQL(createTable);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1)
    {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(sqLiteDatabase);
    }


    /**
     * Dodawanie danych do bazy danych
     * @param nameOfExercise - nazwa cwiczenia
     * @param rawData - dane z wynikami
     * @return - prawda gdy dodane poprawnie, w innym wypadku falsz
     */
    public boolean addData(String nameOfExercise, double[] rawData)
    {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL2_EXERCISE, nameOfExercise);
        Log.d(TAG, "addData: Adding " + nameOfExercise + " to " + TABLE_NAME);

        for(int j = 0; j < 9; j++)
        {
            contentValues.put(COL4_12[j], rawData[j]);
            Log.d(TAG, "addData: Adding " + String.valueOf(rawData[j]) + " to " + TABLE_NAME);
        }

        long result = sqLiteDatabase.insert(TABLE_NAME, null, contentValues);

        if(result == -1)
        {
            return false;
        }
        else
        {
            return true;
        }
    }


    /**
     * Zwraca wszystkie dane z bazy
     * @return - wszystkie dane
     */
    public Cursor getData()
    {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME;

        return sqLiteDatabase.rawQuery(query, null);
    }
}
