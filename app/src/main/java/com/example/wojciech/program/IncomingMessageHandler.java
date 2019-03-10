package com.example.wojciech.program;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.util.Log;


/**
 * Handler obslugujacy przychodzace dane z bluetooth,
 * sprawdza ich poprawnosc, komplentnosc,
 * nadaje unukalne ID do serii pomiarowe,
 * zapisuje dane w bazie SQL,
 * polaczony z watkiem
 * @see com.example.wojciech.program.BluetoothConnection.ConnectedThread
 */
public class IncomingMessageHandler extends Handler
{
    /** Identyfikator wiadomosci */
    private int handlerState;

    /** StringBuilder do obroki wiadomosci */
    private StringBuilder recDataString = new StringBuilder();

    /** Do oblsugi bay danych */
    private DatabaseHelper mDatabaseHelper;

    /** Kontekst do maina */
    private Context mContextMain;

    /** ID serii danych */
    private  long IDofExercise;

    BluetoothConnection BT;

    public IncomingMessageHandler(int handlerState, Context context)
    {
        this.handlerState = handlerState;
        this.mContextMain = context;
        this.IDofExercise = -1;

        BT = (BluetoothConnection)mContextMain.getApplicationContext();


        mDatabaseHelper = new DatabaseHelper(mContextMain, "aaa");
    }


    /**
     * Funkcja obslugujaca odbior danych z Bluetooth
     * Pobiera nazwe cwiczenia wpisana w telefonie, nastepnie nadaje unikalny ID, ktory jest wpisywany do SQL,
     * sluzy on do rozpoznania jednoznacznego danego cwiczenia, jest nadawany na podstawie aktualnego czasu,
     * Funkcja rozpoznaje wiadomosc na podstawie znakow specialnych - poczatek ~, koniec #, kazdy wpis jest oddzielony +
     * na tej podstawie rozdzielane sa pomiary z konkretnych czujnikow (i ich wspozednych) i wpisywane do bazy SQL
     * @param msg - wiadomosc bluetooth
     */
    public void handleMessage(android.os.Message msg)
    {
        boolean insertData;
        String nameOfExercise = BT.getExerciseName(); //pobieranie nazwy cwiczenia
        double[] RawData = new double[9];


        if (msg.what == handlerState && BT.getCollectDataState()) //sprawdzeneinie czy wiadomosc jest tym co chcemy
        {
            //nadawanie unikalnego ID
            if(BT.getCollectDataStateOnChange())
            {
                IDofExercise  = System.currentTimeMillis() / 1000;
                BT.setCollectDataStateOnChange(false);
            }


            String readMessage = (String) msg.obj; // msg.arg1 = bajty z connect thread
            recDataString.append(readMessage); //dodawaj do stringa az ~
            int endOfLineIndex = recDataString.indexOf("~"); //znajdz koniec
            if (endOfLineIndex > 0)
            {                                           //czy na pewno są dane przed ~
                String dataInPrint = recDataString.substring(0, endOfLineIndex);    //wyciagnij stringa
                //txtString.setText("Data Received = " + dataInPrint);
                int dataLength = dataInPrint.length(); //wez dlugosc
                //txtStringLength.setText("String Length = " + String.valueOf(dataLength));


                if (dataInPrint.charAt(0) == '#' && countOccurrencesOf(dataInPrint, '#') == 1 && countOccurrencesOf(dataInPrint, '+') == 11)                             //jesli zaczyna sie od # to na pewno to co chcecmy
                {
                    //szukanie indeksow plusow w celu oddzielenia zmiennych od siebie
                    int controlNr1 = -1;
                    int controlNr2 = -2;
                    int indexOfFirstSeparationMark = 0;
                    int indexOfSecondSeparationMark = dataInPrint.indexOf("+");


                    //Log.d("IncomingMessageHandler", "recDataString: " + dataInPrint);


                    //wyluskanie numeru kontrolnego
                    String controlNr1String = dataInPrint.substring(indexOfFirstSeparationMark + 1, indexOfSecondSeparationMark);
                    controlNr1 = Integer.valueOf(controlNr1String);


                    indexOfFirstSeparationMark = indexOfSecondSeparationMark;
                    indexOfSecondSeparationMark = dataInPrint.indexOf("+", indexOfFirstSeparationMark+1);



                    //Dane do bazy SQL
                    for (int i = 0; i < 9; i++)
                    {
                        String sensor = dataInPrint.substring(indexOfFirstSeparationMark + 1, indexOfSecondSeparationMark);

                        RawData[i] = Double.valueOf(sensor);

                        indexOfFirstSeparationMark = indexOfSecondSeparationMark;
                        indexOfSecondSeparationMark = dataInPrint.indexOf("+", indexOfFirstSeparationMark+1);
                    }

                    //wyluskanie numeru kontrolnego
                    String controlNr2String = dataInPrint.substring(indexOfFirstSeparationMark + 1, indexOfSecondSeparationMark);
                    controlNr2 = Integer.valueOf(controlNr2String);


                    insertData = mDatabaseHelper.addData(IDofExercise, nameOfExercise, controlNr1, RawData, controlNr2);

                    if(insertData)
                    {
                        //Log.i("Dodawanie do SQL", "Sukces");
                    }
                    else
                    {
                        //Log.e("Dodawanie do SQL", "PORAZKA");
                    }


                }
                recDataString.delete(0, recDataString.length()); //wyczysc
                // strIncom =" ";
                dataInPrint = " ";
            }
        }
    }

    /**
     * Zlicza wystapienie symbolu - uzywane do sprawdzenia czy wiadomosc nie jest za duza, posklejana z kilku - wystepowalo przy peirwszych
     * @param string - string w ktorym maja byc zliczane wystapienia
     * @param symbol - symbol
     * @return - liczba wystapien symbol'u
     */
    private int countOccurrencesOf(String string, char symbol)
    {
        int occurrence = 0;

        for(int i = 0; i < string.length(); i++)
        {
            if(string.charAt(i) == symbol)
                occurrence++;
        }

        return occurrence;
    }
}
