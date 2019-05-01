package com.example.wojciech.program;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelperFinalData extends SQLiteOpenHelper
{
    /**
     * Nazwa bazy danych
     */
    private static final String TABLE_NAME = "FinalData";

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
     * Nazwa kolumny 6 - skompensowana predkosc w osi x
     */
    private static String COL5_VELX = "velx";

    /**
     * Nazwa kolumny 7 - skompensowana predkosc w osi y
     */
    private static String COL6_VELY = "vely";

    /**
     * Nazwa kolumny 8 - skompensowana predkosc w osi z
     */
    private static String COL7_VELZ = "velz";

    /**
     * Nazwa kolumny 9 - predkosc wypadkowa
     */
    private static String COL8_VEL_NORM = "velnorm";

    /**
     * Nazwa kolumny 10 - przemieszczenie w osi x
     */
    private static String COL9_DISPX = "dispx";

    /**
     * Nazwa kolumny 11 - przemieszczenie w osi y
     */
    private static String COL10_DISPY = "dispy";

    /**
     * Nazwa kolumny 12 - przemieszczenie w osi z
     */
    private static String COL11_DISPZ = "dispz";

    /**
     * Kontekst
     */
    Context context;

    /**
     * Konstruktor
     *
     * @param context - kontekst
     */
    public DatabaseHelperFinalData(Context context)
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
                COL5_VELX + " REAL, " +
                COL6_VELY + " REAL, " +
                COL7_VELZ + " REAL, " +
                COL8_VEL_NORM + " REAL, " +
                COL9_DISPX + " REAL, " +
                COL10_DISPY + " REAL, " +
                COL11_DISPZ + " REAL)";

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
     * @param controlNumber  - numer kontrolny
     * @return - prawda gdy dodane poprawnie, w innym wypadku falsz
     */
    public boolean addData(long IDofExercise, String nameOfExercise, String date, int controlNumber, double[] velocity, double[] displacement)
    {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(COL1_ID, IDofExercise);
        contentValues.put(COL2_EXERCISE, nameOfExercise);
        contentValues.put(COL3_TIME, date);
        contentValues.put(COL4_CONTROL_NR, controlNumber);
        contentValues.put(COL5_VELX, velocity[0]);
        contentValues.put(COL6_VELY, velocity[1]);
        contentValues.put(COL7_VELZ, velocity[2]);
        contentValues.put(COL8_VEL_NORM, Math.sqrt(Math.pow(velocity[0],2) + Math.pow(velocity[1],2) + Math.pow(velocity[2],2)));
        contentValues.put(COL9_DISPX, displacement[0]);
        contentValues.put(COL10_DISPY, displacement[1]);
        contentValues.put(COL11_DISPZ, displacement[2]);

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

        sqLiteDatabase.execSQL(query);
    }
}
