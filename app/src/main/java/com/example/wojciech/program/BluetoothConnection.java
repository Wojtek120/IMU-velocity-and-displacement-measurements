package com.example.wojciech.program;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

/**
 * Klasa obslugujaca polaczenie Bluetooth z telefonem
 */
public class BluetoothConnection extends Application implements AdapterView.OnItemClickListener
{
    /** Kontekst */
    private Context mContext;

    /** Kontekst do main */
    private Context mContextMain;

    /** Activity z mContext */
    private Activity mActivity;

    /** BluetoothAdapter */
    private BluetoothAdapter mBluetoothAdapter = null;

    /** Gniazdo bluetooth */
    private BluetoothSocket mBluetoothSocket = null;

    /** Wyjsciowy stream */
    private OutputStream mOutputStream = null;

    /** Lista przetrzymujaca urzadzenia Bluetooth, ktore zostaly wyszukane */
    public ArrayList<BluetoothDevice> mBluetoothDevices = new ArrayList<>();

    /** Lista z sparowanymi urzadzeniami */
    public ArrayList<BluetoothDevice> mBluetoothPairedDevices;

    /** DeviceListAdapter - klasa do wyswietlania danych */
    public DeviceListAdapter mDeviceListAdapter;

    /** DeviceListAdapter dla sparowanych urzadzen - klasa do wyswietlania danych */
    public DeviceListAdapter mDevicePairedListAdapter;

    /** ListView z wyszukanymi urzadzeniami */
    private ListView lvNewDevices;

    /** ListView z sparowanymi urzadzeniami */
    private ListView lvPairedDevices;

    /** UUID - identyfikator potrzebny do stworzenia polaczenia */
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    /** Urzadzenie wykorzytstywane w watku */
    private BluetoothDevice mmDevice;

    /** Watek obslugujacy laczenie */
    private ConnectionThread mConnectionThread;

    /** Watek utrzymujacy polaczenie */
    private ConnectedThread mConnectedThread = null;

    /** Handler do klasy IncomingMessage handler
     * @see com.example.wojciech.program.IncomingMessageHandler */
    Handler bluetoothIn = null;

    /** Identyfikacja handlera */
    final int handlerState = 0;

    /** Okno dialogowe oczekiwania */
    private ProgressDialog mProgressDialog;

    /** Informacja czy dane maja byc zbierane */
    private boolean mCollectDataState;

    /** Prawda gdy zostal zmieniony, wykorzystywane przy zapisie danych - przypisywaniu identyfikatora */
    private boolean mCollectDataStateOnChange;

    /** Status polaczenia */
    private  boolean mConnectionStatus;

    /** Nazwa serii danych */
    private String mExerciseName;


    /**
     * Tworzenie BroadcastReceiver, ktory sledzi zmiany stanu Bluetooth
     * Uzywany przez metode enableBluetooth()
     * @see com.example.wojciech.program.BluetoothConnection#enableBluetooth(Context )
     */
    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();

            if (action != null && action.equals(BluetoothAdapter.ACTION_STATE_CHANGED))
            {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                switch (state)
                {
                    case BluetoothAdapter.STATE_OFF:
                        Log.i("BluetoothConnection", "onReceive: STATE OFF");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.i("BluetoothConnection", "mBroadcastReceiver1: STATE TURNING OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.i("BluetoothConnection", "mBroadcastReceiver1: STATE ON");
                        discoverDevices();
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.i("BluetoothConnection", "mBroadcastReceiver1: STATE TURNING ON");
                        break;
                }
            }
        }
    };


    /**
     * Tworzenie BroadcastReceiver, ktory sledzi zmiany statusu Discoverable (urzadzenie mozna wyszukac przez inne)
     * Uzywany przez metode enableDiscoverableMode()
     * @see com.example.wojciech.program.BluetoothConnection#enableDiscoverableMode( )
     */
    private final BroadcastReceiver mBroadcastReceiver2 = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();

            if (action != null && action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED))
            {
                int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);

                switch (mode)
                {
                    //Urzadzenie w Discoverable Mode
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.i("BluetoothConnection", "mBroadcastReceiver2: Discoverability Enabled");
                        break;
                    //Urzadzenie nie jest w Discoverable Mode
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.i("BluetoothConnection", "mBroadcastReceiver2: Discoverability Disabled. Able to receive connections");
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.i("BluetoothConnection", "mBroadcastReceiver2: Discoverability Disabled. Not able to receive connections");
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        Log.i("BluetoothConnection", "mBroadcastReceiver2: Connecting...");
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.i("BluetoothConnection", "mBroadcastReceiver2: Connected");
                        break;
                }
            }
        }
    };


    /**
     * Tworzenie BroadcastReceiver, ktory sledzi stan wyszukiwania urzadzen, ktore nie sa sparowane i dodanie ich do listy mBluetoothDevices
     * Uzywany przez metode discoverDevices()
     * @see com.example.wojciech.program.BluetoothConnection#discoverDevices()
     */
    private final BroadcastReceiver mBroadcastReceiver3 = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();

            if (action != null && action.equals(BluetoothDevice.ACTION_FOUND))
            {
                //Jesli znajdzie urzadzenie to dodaje go do listy mBluetoothDevices
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mBluetoothDevices.add(device);
                Log.i("BluetoothConnection", "mBroadcastReceiver3:" + device.getName() + ": " + device.getAddress());
                mDeviceListAdapter = new DeviceListAdapter(mContext, R.layout.device_adapter_view, mBluetoothDevices); //tutaj jest R.layout.device_adapter_view - ktory ejst plikeim xml gdzie wpisujemy nazwe urzadzenia i jego adres mac
                lvNewDevices.setAdapter(mDeviceListAdapter);
            }
        }
    };


    /**
     * Tworzenie BroadcastReceiver, ktory wykrywa zmiane statusu bond (parowanie)
     * Uzywany w konstruktorze
     */
    private final BroadcastReceiver mBroadcastReceiver4 = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();

            if (action != null && action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED))
            {
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                {
                    //urzadzenie juz sparowane
                    if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED)
                    {
                        Log.i("BluetoothConnection", "mBroadcastReceiver4: BOND_BONDED");
                        showMessage(mContext.getString(R.string.bonded));
                    }
                    //tworzenie polaczenia
                    if (mDevice.getBondState() == BluetoothDevice.BOND_BONDING)
                    {
                        Log.i("BluetoothConnection", "mBroadcastReceiver4: BOND_BONDING");
                        showMessage(mContext.getString(R.string.bonding));
                    }
                    //zerwanie polaczenia
                    if (mDevice.getBondState() == BluetoothDevice.BOND_NONE)
                    {
                        Log.i("BluetoothConnection", "mBroadcastReceiver4: BOND_NONE");
                        showMessage(mContext.getString(R.string.bond_none));
                    }
                }
            }
        }
    };









    /**
     * BroadcastReceiver, ktory nasluchuje bluetooth broadcasts, informuje gdy polaczono lub rozlaczono z urzadzeniem
     */
    private final BroadcastReceiver mBroadcastReceiver5 = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();

            if(action != null)
            {
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                {
                    //Laczenie
                    if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action))
                    {
                        Log.i("BluetoothConnection", "Polaczono");
                        mConnectionStatus = true;
                        showMessage(mContext.getString(R.string.connected));
                    }
                    //Rozlaczono
                    if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action))
                    {
                        Log.i("BluetoothConnection", "Rozlaczono");
                        mConnectionStatus = false;
                        showMessage(mContext.getString(R.string.disconnected));
                    }
                }
            }
        }
    };








    /**
     * Konstruktor -
     *
     */
    public BluetoothConnection()
    {
        mCollectDataState = false;
        mConnectionStatus = false;
    }


    /**
     * przypisuje kontekst do zmiennej mContext context i tworzy mActivity, ustawia register receivery
     * @param context - kontekst
     */
    public void setContextAndRegisterReceivers(Context context)
    {
        mContext = context;
        mActivity = getActivity(mContext);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //sledzenie zmiany stanu Bluetooth do mBroadcastReceiver1
        IntentFilter BluetoothIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        mContext.registerReceiver(mBroadcastReceiver1, BluetoothIntent);

        //sledzenie zmiany stanu do mBroadcastReceiver2 - obsluguje discoverable
        IntentFilter intentFilter = new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        mContext.registerReceiver(mBroadcastReceiver2, intentFilter);

        //Broadcast od wyszukiwania urzadzen
        IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        mContext.registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);

        //Bradcast kiedy zmieni sie stan bond np. parowanie
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        mContext.registerReceiver(mBroadcastReceiver4, filter);

        //BroadcastReceiver ktory mowi o laczeniu urzadzenia
        IntentFilter filter1 = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter1.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        filter1.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        mContext.registerReceiver(mBroadcastReceiver5, filter1);


        //handler do kolejkowania przychodziacych wiadomosci
        bluetoothIn = new IncomingMessageHandler(handlerState, mContextMain);
    }


    /**
     * Ustawia TextViews w ktorych beda wyswietlane wyszunake urzadzenia oraz sparowane
     */
    public void setTextViews()
    {

        lvNewDevices = mActivity.findViewById(R.id.lvNewDevices); //TODO to prawdopodobnie nie jest ostateczna lista urzadzen
        mBluetoothDevices = new ArrayList<>();
        lvNewDevices.setOnItemClickListener(BluetoothConnection.this);

        lvPairedDevices = mActivity.findViewById(R.id.lvPairedDevices); //TODO to prawdopodobnie nie jest ostateczna lista urzadzen
        mBluetoothPairedDevices = new ArrayList<>();
        lvPairedDevices.setOnItemClickListener(BluetoothConnection.this);
    }


    /**
     * Setter kontekstu do maina
     */
    public void SetContextMain(Context context)
    {
        mContextMain = context;
    }



    /**
     * Funkcja wypisujaca sparowane urzadzenia
     */
    public void listPairedDevices()
    {
        Set<BluetoothDevice> all_devices = mBluetoothAdapter.getBondedDevices();
        if (all_devices.size() > 0)
        {
            for (BluetoothDevice currentDevice : all_devices)
            {
                Log.i("PairedDevices", "PairedDevices:" + currentDevice.getName() + ": " + currentDevice.getAddress());
                mDevicePairedListAdapter = new DeviceListAdapter(mContext, R.layout.device_adapter_view, mBluetoothPairedDevices);
                mBluetoothPairedDevices.add(currentDevice);
                lvPairedDevices.setAdapter(mDevicePairedListAdapter);
            }
        }
    }






    /**
     * Funkcja wlaczajaca Bluetooth
     */
    public static void enableBluetooth(Context context)
    {
        BluetoothAdapter mmBluetoothAdapter  = BluetoothAdapter.getDefaultAdapter();
        Activity mmActivity = getActivity(context);

        //Jesli urzadzenie nie posiada Bluetooth
        if (mmBluetoothAdapter == null)
        {
            //Wyswietl informacje o braku Bluetooth
            Log.e("Bluetooth", "Brak bluetooth w urzadzeniu");
        } else if (!mmBluetoothAdapter.isEnabled())
        {
            //Zapytaj uzytkownika czy wlaczyc Bluetooth
            Intent turnBluetoothOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (mmActivity != null)
                mmActivity.startActivityForResult(turnBluetoothOn, 1);

        }

    }








    /**
     * Funkcja ustawiajaca urzadzenie Discoverable (widoczne dla innych urzadzen) na 30s
     */
    public void enableDiscoverableMode()
    {
        //Wlaczanie Discoverable na 30s
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 30);
        mContext.startActivity(discoverableIntent);


    }








    /**
     * Funkcja wyszukujaca wlaczone urzadzenia Bluetooth
     */
    public void discoverDevices()
    {
        if (mBluetoothAdapter.isDiscovering())
        {
            //zatrzymaj skanowanie
            mBluetoothAdapter.cancelDiscovery();

            //sprawdz pozwolenia
            checkBluetoothPermissions();

            //zacznij skanowac ponownie
            mBluetoothAdapter.startDiscovery();

        } else
        {
            //sprawdz pozwolenia
            checkBluetoothPermissions();

            //zacznij skanowac
            mBluetoothAdapter.startDiscovery();

        }
    }








    /**
     * Funkcja sprawdzajaca pozwolenia do wyszukiwania urzadzen przez Bluetooth, wymagana na wszystkich urzadzeniach z API23+.
     * Android musi porgramowo sprawdzic pozwolenia i dac je do manifest'u jesli ich nie ma
     */
    @TargetApi(23)
    private void checkBluetoothPermissions()
    {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP)
        {
            int permissionCheck = mContext.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += mContext.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permissionCheck != 0)
            {
                mActivity.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001);
            }
        }
    }








    /**
     * Funkcja wyswietlajaca wysrodkowana wiadomosc
     *
     * @param message - wiadomosc do wyswietlenia
     */
    private void showMessage(String message)
    {
        Toast toast = Toast.makeText(mContext.getApplicationContext(), message, Toast.LENGTH_SHORT);
        TextView vi = toast.getView().findViewById(android.R.id.message);
        if (vi != null) vi.setGravity(Gravity.CENTER);
        toast.show();
    }









    /**
     * Funckja zwracajaca Activity z Context'u
     *
     * @param context - context z którego ma być zwrocona activity
     * @return Activity lub null w przypadku bledu
     */
    private static Activity getActivity(Context context)
    {
        if (context == null)
        {
            return null;
        } else if (context instanceof ContextWrapper)
        {
            if (context instanceof Activity)
            {
                return (Activity) context;
            } else
            {
                return getActivity(((ContextWrapper) context).getBaseContext());
            }
        }

        return null;
    }







    /**
     * Zwolnij BroadcastReceiver
     */
    public void unregisterBroadcastReceiver()
    {
        mContext.unregisterReceiver(mBroadcastReceiver1);
        mContext.unregisterReceiver(mBroadcastReceiver2);
        mContext.unregisterReceiver(mBroadcastReceiver3);
        mContext.unregisterReceiver(mBroadcastReceiver4);
        mContext.unregisterReceiver(mBroadcastReceiver5);
    }






    /**
     * Zamknij bluetooth socket
     */
    public void closeBluetoothSocket()
    {
        try
        {
            if(mBluetoothSocket != null)
                mBluetoothSocket.close();
        } catch (IOException e2)
        {
            Log.e("SetConnection", "ERROR - Failed to close Bluetooth socket");
        }
    }







    /**
     * Gdy klikniemy urzadzenie na liscie to paruje je z nim
     */
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
    {
        if(adapterView.getId() == R.id.lvNewDevices)
        {
            //zatrzymaj skanowanie
            mBluetoothAdapter.cancelDiscovery();

            //Pobier nazwe i adres kliknietego urzadzenia
            String deviceName = mBluetoothDevices.get(i).getName();
            String deviceAddress = mBluetoothDevices.get(i).getAddress();

            Log.i("BluetoothConnection", "Wybrane urzadzenie: " + deviceName);
            Log.i("BluetoothConnection", "Wybrane urzadzenie: " + deviceAddress);

            //Paruj urzadzenie TODO Dzia;a tylko na nowszych urzadzeniach, sprawdz czy mozna inaczej
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2)
            {
                startClient(mBluetoothDevices.get(i));
            }
        }

        //klikniecie na liste ze sparowanymi urzadzeniami
        else
        {
            //zatrzymaj skanowanie
            mBluetoothAdapter.cancelDiscovery();

            //Pobier nazwe i adres kliknietego urzadzenia
            String deviceName = mBluetoothPairedDevices.get(i).getName();
            String deviceAddress = mBluetoothPairedDevices.get(i).getAddress();

            Log.i("BluetoothConnection", "Wybrane urzadzenie: " + deviceName);
            Log.i("BluetoothConnection", "Wybrane urzadzenie: " + deviceAddress);

            //Paruj urzadzenie TODO Dzia;a tylko na nowszych urzadzeniach, sprawdz czy mozna inaczej
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2)
            {
                startClient(mBluetoothPairedDevices.get(i));
            }
        }
    }







    /**
     * Uruchamia watek ConnectionThread do nawiazania polaczenia
     * @see ConnectionThread
     **/
    private void startClient(BluetoothDevice device)
    {
        Log.d("startClient", "startClient: Started.");

        //uruchamia progress dialog
        mProgressDialog = ProgressDialog.show(mContext, mContext.getString(R.string.connectingProgress), mContext.getString(R.string.please_wait), true);

        mConnectionThread = new ConnectionThread(device);
        mConnectionThread.start();
    }







    /**
     * Watek odpiwiedzialny za ustanawianie polaczenia, w nim jest tworzony socket a następnie uruchamiana funkcja connected
     * @see #connected(BluetoothSocket, BluetoothDevice)
     */
    private class ConnectionThread extends Thread
    {
        private BluetoothSocket mmBluetoothSocket;


        /**
         * konstruktor
         * @param device - urzadzenie, z ktoryma ma zostac nawiazane polaczenie
         */
        public ConnectionThread(BluetoothDevice device)
        {
            Log.i("ConnectedThread", "Uruchomiono");

            mmDevice = device;
        }


        /**
         * W tej funkcji watek tworzy polaczenie, a nastepnie uruchamia funkcje connected
         * @see BluetoothConnection#connected(BluetoothSocket, BluetoothDevice)
         */
        public void run()
        {
            BluetoothSocket tmp = null;


            //showMessage(mContext.getString(R.string.connecting));


            //Tworzenie bluetooth socket dla polaczenia z okreslonym urzadzeniem
            try
            {
                tmp = mmDevice.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e1)
            {
                Log.e("SetConnection", "Could not create Bluetooth socket");
            }


            mmBluetoothSocket = tmp;


            //Zawsze kasujemy discovery poniewaz bardzo zwalnia ono polaczenie
            mBluetoothAdapter.cancelDiscovery();

            //Utworz polaczenie
            try
            {
                mmBluetoothSocket.connect();
            } catch (IOException e)
            {
                try
                {
                    //Jesli blad IO wystapi to proba zamkniecia socket'a
                    mmBluetoothSocket.close();
                } catch (IOException e2)
                {
                    Log.e("SetConnection", "ERROR - Could not close Bluetooth socket");
                }
            }


            connected(mmBluetoothSocket, mmDevice);
        }
    }





    /**
     * Uruchamia watek do zarzadzania polaczeniem i wysylania danych
     */
    private void connected(BluetoothSocket mmSocket, BluetoothDevice mmDevice)
    {
        Log.d("connected", "connected: Starting.");
        mConnectedThread = new ConnectedThread(mmSocket);
        mConnectedThread.start();
    }






    /**
     * Watek odpowiedzialny za utrzymanie polaczenia, wysylanie danych i ich odbieranie
     **/
    private class ConnectedThread extends Thread
    {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;


        /**
         * Konstruktor
         * @param socket - socket ktory ma zostac obsluzonu
         */
        public ConnectedThread(BluetoothSocket socket)
        {
            Log.d("ConnectedThread", "ConnectedThread: Starting.");

            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            //usun dialog ladowania kiedy sie polaczymy
            try
            {
                mProgressDialog.dismiss();
            } catch (NullPointerException e)
            {
                e.printStackTrace();
            }


            try
            {
                tmpIn = mmSocket.getInputStream();
                tmpOut = mmSocket.getOutputStream();
            } catch (IOException e)
            {
                e.printStackTrace();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }


        /**
         * Obsluga komunikacij
         */
        public void run()
        {
            byte[] buffer = new byte[1024];  // buffer dla stream

            int bytes; // bytes zwrocony przez read()

            // Nasluchuj do InputStream az exception wystapi
            while (true)
            {
                //Czytaj z InputStream
                try
                {
                    bytes = mmInStream.read(buffer);
                    String incomingMessage = new String(buffer, 0, bytes);
                    //Log.d("ConnectedThread", "InputStream: " + incomingMessage);
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, incomingMessage).sendToTarget();
                } catch (IOException e)
                {
                    Log.e("ConnectedThread", "write: Error reading Input Stream. " + e.getMessage());
                    break;
                }
            }
        }


        /**
         * Funkcja wysylajaca dane
         * @param bytes - dane do wyslania
         */
        public void write(byte[] bytes)
        {
            String text = new String(bytes, Charset.defaultCharset());
            Log.d("ConnectedThread", "write: Writing to outputstream: " + text);
            try
            {
                mmOutStream.write(bytes);
            } catch (IOException e)
            {
                Log.e("ConnectedThread", "write: Error writing to output stream. " + e.getMessage());
            }
        }

        /**
         * Funkcja zamykajaca polaczenie
         */
        public void cancel()
        {
            try
            {
                mmSocket.close();
            } catch (IOException e)
            {
            }
        }
    }





    /**
     * Funkcja wysylajaca wiadomosc, piszaca do ConnectedThread
     *
     * @param message dane do napisania
     * @see ConnectedThread#write(byte[])
     */
    public void write(String message)
    {
        Log.d("write", "write: Write Called.");
        //string to byte
        byte[] messageBuffer = message.getBytes();
        //napisz
        if(mConnectedThread != null)
            mConnectedThread.write(messageBuffer);
    }


    /**
     * Getter statusu polaczone. True - gdy polaczony, w innym przypadku false
     * @return - mConnectionStatus
     */
    public boolean getConnectionStatus()
    {
        return mConnectionStatus;
    }

    /**
     * Funkcja zmieniajaca status zbierania danych - czy dane maja byc zapisywane do bazy danych
     */
    public void collectDataStateChange()
    {
        if(mCollectDataState)
        {
            mCollectDataState = false;
            showMessage(mContext.getString(R.string.data_collecting_stopped));
        }
        else
        {
            mCollectDataState = true;
            showMessage(mContext.getString(R.string.data_collecting_started));
        }

        mCollectDataStateOnChange = true;
    }


    /**
     * Getter statusu zbierania danych - czy dane maja byc zapisywane do bazy danych
     * @return - mCollectDataState
     */
    public boolean getCollectDataState()
    {
        return mCollectDataState;
    }



    /**
     * Getter statusu zmiany zbierania danych - ustawiany na true w momencie zmiany, wykorzystywany do nadawania unikalnego ID cwiczeniom
     * @return - mCollectDataState
     */
    public boolean getCollectDataStateOnChange()
    {
        return mCollectDataStateOnChange;
    }


    /**
     * Setter statusu zmiany zbierania danych - ustawiany na true w momencie zmiany, wykorzystywany do nadawania unikalnego ID cwiczeniom
     * @param mCollectDataStateOnChange - ustawiany na falsz gdy zostanie nadany identyfikator
     */
    public void setCollectDataStateOnChange(boolean mCollectDataStateOnChange)
    {
        this.mCollectDataStateOnChange = mCollectDataStateOnChange;
    }


    /**
     * getter nazwy cwiczenia przekazywany dalej przy obsludze przychodzacych danych
     * @return - nazwa cwiczenia
     */
    public String getExerciseName()
    {
        return mExerciseName;
    }


    /**
     * setter nazwy cwiczenia
     * @param mExerciseName - nazwa cwiczenia
     */
    public void setExerciseName(String mExerciseName)
    {
        this.mExerciseName = mExerciseName;
    }
}


