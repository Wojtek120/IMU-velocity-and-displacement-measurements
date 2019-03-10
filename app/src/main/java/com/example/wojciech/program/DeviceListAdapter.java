package com.example.wojciech.program;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


import java.util.ArrayList;

/**
 * ArrayAdapter wykorzystywany przy listowaniu wyszukanych urzadzen z wlaczonym bluetooth
 */
public class DeviceListAdapter extends ArrayAdapter<BluetoothDevice>
{
    /** LayoutInflater */
    private LayoutInflater mLayoutInflater;

    /** ArrayList z urzadzeniami bluetooth, z nich pozyskiwana jest nazwa i adres MAC */
    private ArrayList<BluetoothDevice> mDevices;
    private int mViewResourceId;

    /**
     * Konstruktor TODO opisac to jest w wyszukiwaniu urzadzenpotrzebne w mBroadcastReceiver3
     * @param context - kontekst
     * @param tvResourceId - wskaznik (?) na plik xml gdzie wpisujemy do niego nazwe i adres urzadzenia
     * @param devices - lista z urzadzeniami bluetooth
     */
    public DeviceListAdapter(Context context, int tvResourceId, ArrayList<BluetoothDevice> devices)
    {
        super(context, tvResourceId, devices);
        this.mDevices = devices;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mViewResourceId = tvResourceId;
    }

    /**
     * TODO OPISAC j.w.
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    public View getView(int position, View convertView, ViewGroup parent)
    {
        convertView = mLayoutInflater.inflate(mViewResourceId, null); //TODO ogarnac

        BluetoothDevice device = mDevices.get(position);

        if(device != null)
        {
            TextView deviceName = convertView.findViewById(R.id.tvDeviceName); //TODO to nie jest raczej finalna lista
            TextView deviceAddress = convertView.findViewById(R.id.tvDeviceAddress); //TODO jw

            if (deviceName != null)
            {
                deviceName.setText(device.getName());
            }

            if (deviceAddress != null)
            {
                deviceAddress.setText(device.getAddress());
            }
        }

        return convertView;
    }
}
