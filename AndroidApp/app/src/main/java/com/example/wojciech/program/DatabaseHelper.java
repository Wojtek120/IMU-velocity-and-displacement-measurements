package com.example.wojciech.program;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Klasa odpowiedzialna za obsluge wpisow danych pomiarowych do bazy SQLite,
 * w niej baza jest tworzona i wysylane sa zapytania do niej
 */
public class DatabaseHelper extends SQLiteOpenHelper
{
    /** Liczba kolumn */
    private static final Integer NUMBER_OF_COLUMNS = 13;

    /** TAG */
    private static final String TAG = "DatabaseHelper";

    /** Nazwa bazy danych */
    private static final String TABLE_NAME = "RawDataDatabase4";

    /** Nazwa kolumny 1 - nr kolumny*/
    private static String COL0_NUMBER = "number";

    /** Nazwa kolumny 2 - id */
    private static String COL1_ID = "id";

    /** Nazwa kolumny 3 - nazwa */
    private static String COL2_EXERCISE = "exercise";

    /** Nazwa kolumny 4 - czas */
    private static String COL3_TIME = "time";

    /** Nazwa kolumny 5 - nr kontrolny1 */
    private static String COL4_CONTROL_NR_1 = "control_nr_1";

    /** Nazwa kolumn od 6 do 14 - pomiary z czujnikow */
    private static String[] COL5_13 = {"accx", "acc_y", "acc_z", "gyro_x", "gyro_y", "gyro_z", "mag_x", "mag_y", "mag_z"};

    /** Nazwa kolumny 15 - nr kontrolny2 */
    private static String COL14_CONTROL_NR_2 = "control_nr_2";

    /** Kontekst */
    Context context;


    /**
     * Konstruktor
     *
     * @param context - kontekst
     */
    public DatabaseHelper(Context context)
    {
        super(context, TABLE_NAME, null, 1);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase)
    {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" + COL0_NUMBER + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL1_ID + " INTEGER, " +
                COL2_EXERCISE + " TEXT, " +
                COL3_TIME + " DATETIME DEFAULT(STRFTIME('%Y-%m-%d %H:%M:%f', 'NOW')), " +
                COL4_CONTROL_NR_1 + " INTEGER, " +
                COL5_13[0] + " REAL, " +
                COL5_13[1] + " REAL, " +
                COL5_13[2] + " REAL, " +
                COL5_13[3] + " REAL, " +
                COL5_13[4] + " REAL, " +
                COL5_13[5] + " REAL, " +
                COL5_13[6] + " REAL, " +
                COL5_13[7] + " REAL, " +
                COL5_13[8] + " REAL, " +
                COL14_CONTROL_NR_2 + " INTEGER)";

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
     * @param IDofExercise - id cwiczenia
     * @param nameOfExercise - nazwa cwiczenia
     * @param rawData        - dane z wynikami
     * @return - prawda gdy dodane poprawnie, w innym wypadku falsz
     */
    public boolean addData(long IDofExercise, String nameOfExercise, int controlNumber1, double[] rawData, int controlNumber2)
    {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL1_ID, IDofExercise);
        contentValues.put(COL2_EXERCISE, nameOfExercise);
        //Log.d(TAG, "addData: Adding " + nameOfExercise + " to " + TABLE_NAME);

        contentValues.put(COL4_CONTROL_NR_1, controlNumber1);

        for (int j = 0; j < 9; j++)
        {
            contentValues.put(COL5_13[j], rawData[j]);
            //Log.d(TAG, "addData: Adding " + String.valueOf(rawData[j]) + " to " + TABLE_NAME);
        }

        contentValues.put(COL14_CONTROL_NR_2, controlNumber2);

        long result = sqLiteDatabase.insert(TABLE_NAME, null, contentValues);

        return !(result == -1);
    }


    /**
     * Zwraca dane z bazy
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

        query = "SELECT " + COL1_ID + ", " + "min(" + COL3_TIME + "), " + COL2_EXERCISE +" FROM " + TABLE_NAME + " GROUP BY " + COL1_ID +
                " ORDER BY " + COL1_ID + " DESC";

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

        DatabaseHelperRPY databaseHelperRPY = new DatabaseHelperRPY(context);
        databaseHelperRPY.updateExerciseName(newName, id);
    }



    /**
     * Funkcja usuwajaca cwiczenie o danym ID
     * @param id - id rekordu, ktory ma byc usuniety
     */
    public void deleteExercise(int id)
    {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        String query = "DELETE FROM " + TABLE_NAME + " WHERE " + COL1_ID + " = '" + id + "'";
        Log.i(TAG, "updateName " + query);

        sqLiteDatabase.execSQL(query);

        DatabaseHelperRPY databaseHelperRPY = new DatabaseHelperRPY(context);
        databaseHelperRPY.deleteExercise(id);
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


    public int getLargestID()
    {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        String query = "SELECT MAX(" + COL1_ID +") FROM " + TABLE_NAME;

        Cursor data = sqLiteDatabase.rawQuery(query, null);

        int ID = 0;

        boolean first = true;
        while(data.moveToNext())
        {
            if(first)
            {
                ID = data.getInt(0);
                first = false;
            }

        }

        data.close();

        return ID;
    }


}
