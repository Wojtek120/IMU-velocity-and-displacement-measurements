package com.example.wojciech.program;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ExerciseListAdapter extends ArrayAdapter<String[]>
{
    /** LayoutInflater */
    private LayoutInflater mLayoutInflater;
    /** ArraYList z nazwami serii danych i datami */
    private ArrayList<String[]> mNames;
    private int mViewResourceId;

    public ExerciseListAdapter(Context context, int tvResourceId, ArrayList<String[]> names)
    {
        super(context, tvResourceId, names);

        this.mNames = names;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mViewResourceId = tvResourceId;

    }


    public View getView(int position, View convertView, ViewGroup parent)
    {
        convertView = mLayoutInflater.inflate(mViewResourceId, null); //TODO ogarnac

        String[] exerciseNameAndDate = mNames.get(position);

        if(exerciseNameAndDate != null)
        {
            TextView exerciseName = convertView.findViewById(R.id.tvExerciseName);
            TextView exerciseDate = convertView.findViewById(R.id.tvExerciseDate);

            if (exerciseName != null)
            {
                exerciseName.setText(exerciseNameAndDate[0]);
            }

            if (exerciseDate != null)
            {
                exerciseDate.setText(exerciseNameAndDate[1]);
            }
        }

        return convertView;
    }
}
