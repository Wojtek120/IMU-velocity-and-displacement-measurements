package com.example.wojciech.program;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper
{
    private static final Integer NUMBER_OF_COLUMNS = 13;
    private static final String TAG = "DatabaseHelper";
    private static String TABLE_NAME = "RawDataDatabase3";
    private static String COL0_NUMBER = "number";
    private static String COL1_ID = "id";
    private static String COL2_EXERCISE = "exercise";
    private static String COL3_TIME = "time";
    private static String[] COL4_12 = {"accx", "acc_y", "acc_z", "gyro_x", "gyro_y", "gyro_z", "mag_x", "mag_y", "mag_z"};


    /**
     * Konstruktor
     *
     * @param context - kontekst
     * @param name    - nazwa tabeli
     */
    public DatabaseHelper(Context context, String name)
    {
        super(context, TABLE_NAME, null, 1); //TODO tablename nie jest uzywane
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase)
    {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" + COL0_NUMBER + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL1_ID + " INTEGER, " +
                COL2_EXERCISE + " TEXT, " +
                COL3_TIME + " DATETIME DEFAULT(STRFTIME('%Y-%m-%d %H:%M:%f', 'NOW')), " +
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
     *
     * @param nameOfExercise - nazwa cwiczenia
     * @param rawData        - dane z wynikami
     * @return - prawda gdy dodane poprawnie, w innym wypadku falsz
     */
    public boolean addData(long IDofExercise, String nameOfExercise, double[] rawData)
    {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL1_ID, IDofExercise);
        contentValues.put(COL2_EXERCISE, nameOfExercise);
        Log.d(TAG, "addData: Adding " + nameOfExercise + " to " + TABLE_NAME);

        for (int j = 0; j < 9; j++)
        {
            contentValues.put(COL4_12[j], rawData[j]);
            Log.d(TAG, "addData: Adding " + String.valueOf(rawData[j]) + " to " + TABLE_NAME);
        }

        long result = sqLiteDatabase.insert(TABLE_NAME, null, contentValues);

        if (result == -1)
        {
            return false;
        } else
        {
            return true;
        }
    }


    /**
     * Zwraca wszystkie dane z bazy
     *
     * @param IDinSQL - nazwa ID w bazie danych do ktorego ma zostac zwrocone ID, jesli -1 to zwrocone wszytskie dane
     * @return - wybrane dane
     */
    public Cursor getData(int IDinSQL)
    {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        String query;

        if(IDinSQL == -1)
        {
            query = "SELECT * FROM " + TABLE_NAME + " ORDER BY " + COL3_TIME;
        }
        else
        {
            query = "SELECT * FROM " + TABLE_NAME + " WHERE " + COL1_ID + " = " + Integer.toString(IDinSQL) + " ORDER BY " + COL3_TIME;
        }

        return sqLiteDatabase.rawQuery(query, null);
    }


    /**
     * Funkcja zwracajaca wszyatkie ID bez powtorzen, nazwy/komentarze oraz czas pierwszej
     *
     * @return - wszytskie ID oraz daty
     */
    public Cursor getIDS()
    {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        String query;

        query = "SELECT " + COL1_ID + ", " + "min(" + COL3_TIME + "), " + COL2_EXERCISE +" FROM " + TABLE_NAME + " GROUP BY " + COL1_ID;

        return sqLiteDatabase.rawQuery(query, null);
    }


    /**
     * Funkcja aktualizujaca nazwe cwiczenia
     * @param newName - nazwa do zaktualizowania
     * @param id - id rekordu ktory ma byc zaktualizowany
     */
    public void updateExerciseName(String newName, int id)
    {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        String query = "UPDATE " + TABLE_NAME + " SET " + COL2_EXERCISE + " = '" + newName +
                "' WHERE " + COL1_ID + " = '" + id + "'";
        Log.i(TAG, "updateName " + query);

        sqLiteDatabase.execSQL(query);
    }



    /**
     * Funkcja usuwajaca cwiczenie o danym ID
     * @param id - id rekordu ktory ma byc usuniety
     */
    public void deleteExercise(int id)
    {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        String query = "DELETE FROM " + TABLE_NAME + " WHERE " + COL1_ID + " = '" + id + "'";
        Log.i(TAG, "updateName " + query);

        sqLiteDatabase.execSQL(query);
    }





    /**
     * Zwraca liczbe wierszy tabeli
     *
     * @return - liczba wierszy
     */
    public int getNumberOfRows()
    {
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        return (int) DatabaseUtils.queryNumEntries(sqLiteDatabase, TABLE_NAME);
    }


    /**
     * Zwraca liczbe kolumn tabeli
     *
     * @return - liczba kolumn
     */
    public int getNumberOfColumns()
    {
        return NUMBER_OF_COLUMNS;
    }


}
