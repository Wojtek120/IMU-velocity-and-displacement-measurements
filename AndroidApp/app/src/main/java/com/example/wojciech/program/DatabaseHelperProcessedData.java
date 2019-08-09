package com.example.wojciech.program;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.sql.Timestamp;

/**
 * Klasa odpowiedzialna za obsluge wpisow koncowych danych do bazy SQLite,
 * w niej baza jest tworzona i wysylane sa zapytania do niej
 */
public class DatabaseHelperProcessedData extends SQLiteOpenHelper
{
    /**
     * TAG
     */
    private static final String TAG = "DatabaseHelperRPY";

    /**
     * Nazwa bazy danych
     */
    private static final String TABLE_NAME = "ProcessedData";

    /**
     * Nazwa kolumny 1 - nr kolumny
     */
    private static String COL0_NUMBER = "number";

    /**
     * Nazwa kolumny 2 - id
     */
    private static String COL1_ID = "id";

    /**
     * Nazwa kolumny 3 - nazwa
     */
    private static String COL2_EXERCISE = "exercise";

    /**
     * Nazwa kolumny 4 - czas
     */
    private static String COL3_TIME = "time";

    /**
     * Nazwa kolumny 5 - nr kontrolny1
     */
    private static String COL4_CONTROL_NR = "control_nr_1";

    /**
     * Nazwa kolumny 6 - przyspieszenie z usunieta grawitacja w osi x
     */
    private static String COL5_ACCX = "accx";

    /**
     * Nazwa kolumny 7 - przyspieszenie z usunieta grawitacja w osi y
     */
    private static String COL6_ACCY = "accy";

    /**
     * Nazwa kolumny 8 - przyspieszenie z usunieta grawitacja w osi z
     */
    private static String COL7_ACCZ = "accz";

    /** Nazwa kolumny 9 - przyspieszenie z usunieta grawitacja w osi x */
    private static String COL8_STATIC_INTERVAL = "static";

    /**
     * Nazwa kolumny 10 - predkosc w osi x
     */
    private static String COL9_VELX = "velx";

    /**
     * Nazwa kolumny 11 - predkosc w osi y
     */
    private static String COL10_VELY = "vely";

    /**
     * Nazwa kolumny 12 - predkosc w osi z
     */
    private static String COL11_VELZ = "velz";

//    /** Nazwa kolumny 6 - przyspieszenie z usunieta grawitacja w osi x */
//    private static String COL8_ACCX = "velx";
//
//    /** Nazwa kolumny 7 - przyspieszenie z usunieta grawitacja w osi y */
//    private static String COL9_ACCY = "vely";
//
//    /** Nazwa kolumny 8 - przyspieszenie z usunieta grawitacja w osi z */
//    private static String COL10ACCZ = "velz";

    /**
     * Kontekst
     */
    Context context;

    /**
     * Konstruktor
     *
     * @param context - kontekst
     */
    public DatabaseHelperProcessedData(Context context)
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
                COL3_TIME + " DATETIME DEFAULT(STRFTIME('%Y-%m-%d %H:%M:%f')), " +
                COL4_CONTROL_NR + " INTEGER, " +
                COL5_ACCX + " REAL, " +
                COL6_ACCY + " REAL, " +
                COL7_ACCZ + " REAL, " +
                COL8_STATIC_INTERVAL + " INTEGER, " +
                COL9_VELX + " REAL, " +
                COL10_VELY + " REAL, " +
                COL11_VELZ + " REAL)";

        sqLiteDatabase.execSQL(createTable);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1)
    {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    /**
     * Dodawanie danych do bazy danych i usuwanie przyspieszenia zwiazanego z grawitacja
     *
     * @param IDofExercise   - id cwiczenia
     * @param nameOfExercise - nazwa cwiczenia
     * @param date           - data
     * @param controlNumber  - numer kontrolny
     * @param compensatedAcc - przyspieszenie w osi x, y, z
     * @return - prawda gdy dodane poprawnie, w innym wypadku falsz
     */
    public boolean addData(long IDofExercise, String nameOfExercise, String date, int controlNumber,
                           double[] compensatedAcc, int staticInterval, double[] velocity)
    {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(COL1_ID, IDofExercise);
        contentValues.put(COL2_EXERCISE, nameOfExercise);
        contentValues.put(COL3_TIME, date);
        contentValues.put(COL4_CONTROL_NR, controlNumber);
        contentValues.put(COL5_ACCX, compensatedAcc[0]);
        contentValues.put(COL6_ACCY, compensatedAcc[1]);
        contentValues.put(COL7_ACCZ, compensatedAcc[2]);
        contentValues.put(COL8_STATIC_INTERVAL, staticInterval);
        contentValues.put(COL9_VELX, velocity[0]);
        contentValues.put(COL10_VELY, velocity[1]);
        contentValues.put(COL11_VELZ, velocity[2]);

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

        if (IDinSQL == -1)
        {
            query = "SELECT * FROM " + TABLE_NAME + " ORDER BY " + COL3_TIME;
        } else
        {
            query = "SELECT * FROM " + TABLE_NAME + " WHERE " + COL1_ID + " = " + Integer.toString(IDinSQL) + " ORDER BY " + COL3_TIME;
        }

        return sqLiteDatabase.rawQuery(query, null);
    }

    /**
     * Funkcja aktualizujaca nazwe cwiczenia
     *
     * @param newName - nazwa do zaktualizowania
     * @param id      - id rekordu ktory ma byc zaktualizowany
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
     *
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
