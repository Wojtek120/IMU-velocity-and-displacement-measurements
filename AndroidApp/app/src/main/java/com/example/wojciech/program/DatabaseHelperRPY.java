package com.example.wojciech.program;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Klasa odpowiedzialna za obsluge wpisow katow RPY do bazy SQLite,
 * w niej baza jest tworzona i wysylane sa zapytania do niej
 */
public class DatabaseHelperRPY extends SQLiteOpenHelper
{

    /** TAG */
    private static final String TAG = "DatabaseHelperRPY";

    /** Nazwa bazy danych */
    private static final String TABLE_NAME = "RPYDataDatabase";

    /** Nazwa kolumny 1 - nr kolumny*/
    private static String COL0_NUMBER = "number";

    /** Nazwa kolumny 2 - id */
    private static String COL1_ID = "id";

    /** Nazwa kolumny 3 - nazwa */
    private static String COL2_EXERCISE = "exercise";

    /** Nazwa kolumny 4 - czas */
    private static String COL3_TIME = "time";

    /** Nazwa kolumny 5 - nr kontrolny1 */
    private static String COL4_CONTROL_NR = "control_nr_1";

    /** Nazwa kolumny 6 - kat yaw */
    private static String COL5_YAW = "yaw";

    /** Nazwa kolumny 7 - kat pitch */
    private static String COL6_PITCH = "pitch";

    /** Nazwa kolumny 8 - kat roll */
    private static String COL7_ROLL = "roll";

    /** Kontekst */
    Context context;



    /**
     * Konstruktor
     *
     * @param context - kontekst
     */
    public DatabaseHelperRPY(Context context)
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
                COL4_CONTROL_NR + " INTEGER, " +
                COL5_YAW + " REAL, " +
                COL6_PITCH + " REAL, " +
                COL7_ROLL + " REAL)";

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
     * @param controlNumber - numer kontrolny
     * @param yawAngle - kat yaw
     * @param pitchAngle - kat pitch
     * @param rollAngle - kat roll
     * @return - prawda gdy dodane poprawnie, w innym wypadku falsz
     */
    public boolean addData(long IDofExercise, String nameOfExercise, int controlNumber, double yawAngle, double pitchAngle, double rollAngle)
    {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(COL1_ID, IDofExercise);
        contentValues.put(COL2_EXERCISE, nameOfExercise);
        contentValues.put(COL4_CONTROL_NR, controlNumber);
        contentValues.put(COL5_YAW, yawAngle);
        contentValues.put(COL6_PITCH, pitchAngle);
        contentValues.put(COL7_ROLL, rollAngle);

        long result = sqLiteDatabase.insert(TABLE_NAME, null, contentValues);

        return !(result == -1);
    }


    /**
     * Zwraca dane z bazy
     *
     * @param IDinSQL - nazwa ID w bazie danych do ktorego maja zostac zwrocone, jesli -1 to zwrocone wszytskie dane
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
     * @param id - id rekordu, ktory ma byc usuniety
     */
    public void deleteExercise(int id)
    {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        String query = "DELETE FROM " + TABLE_NAME + " WHERE " + COL1_ID + " = '" + id + "'";
        Log.i(TAG, "updateName " + query);

        sqLiteDatabase.execSQL(query);
    }
}
