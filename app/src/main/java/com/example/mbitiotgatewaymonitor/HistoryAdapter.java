package com.example.mbitiotgatewaymonitor;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class HistoryAdapter extends BaseAdapter {

    static final String LOG_TAG = "VINO_IOT:Adapter";
    private final Activity mContext;
    List<String> mRxTimeList = new ArrayList<String>();
    List<String> mRxPressList = new ArrayList<String>();
    List<String> mRxAccelList = new ArrayList<String>();
    List<String> mRxAltiList = new ArrayList<String>();
    List<String> mRxTempList = new ArrayList<String>();
    List<String> mRxHumList = new ArrayList<String>();

    public HistoryAdapter(Activity context, List<String> mRxTimeList, List<String> mRxPressList,
                          List<String> mRxAccelList, List<String> mRxAltiList,
                          List<String> mRxTempList, List<String> mRxHumList
                          ) {
        // TODO Auto-generated constructor stub
        Log.d(LOG_TAG, "HistoryAdapter");

        this.mContext = context;
        this.mRxTimeList = mRxTimeList;
        this.mRxPressList = mRxPressList;
        this.mRxAccelList = mRxAccelList;
        this.mRxAltiList = mRxAltiList;
        this.mRxTempList = mRxTempList;
        this.mRxHumList = mRxHumList;

        Utils.printDataList(mRxTimeList,mRxPressList,mRxAccelList,mRxAltiList,mRxTempList,mRxHumList);

    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return mRxTempList.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return mRxTempList.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }


    @Override
    public View getView(int position, View view, ViewGroup parent) {
        Log.d(LOG_TAG, "getView");
        LayoutInflater inflater=mContext.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.history_list, null,true);

        TextView timeText = (TextView) rowView.findViewById(R.id.history_textView);
        TextView pressText = (TextView) rowView.findViewById(R.id.history_pressure);
        TextView acceText = (TextView) rowView.findViewById(R.id.history_accel);
        TextView altiText = (TextView) rowView.findViewById(R.id.history_altidude);
        TextView tempText = (TextView) rowView.findViewById(R.id.history_temp);
        TextView humText = (TextView) rowView.findViewById(R.id.history_humidity);

        String time = mRxTimeList.get(position);
        String pressure = mRxPressList.get(position);
        String accel = mRxAccelList.get(position);
        String altitude = mRxAltiList.get(position);
        String temp = mRxTempList.get(position);
        String humidity = mRxHumList.get(position);

        Log.d(LOG_TAG, "time : " + time);
        Log.d(LOG_TAG, "pressure : " + pressure);
        Log.d(LOG_TAG, "accel : " + accel);
        Log.d(LOG_TAG, "altitude : " + altitude);
        Log.d(LOG_TAG, "temp : " + temp);
        Log.d(LOG_TAG, "humidity : " + humidity);

        timeText.setText(time);

        pressText.setText(pressure + " hPa");
        acceText.setText(accel);
        altiText.setText(altitude + " m");
        tempText.setText(temp + " \u2103");
        humText.setText(humidity + " %RH");

        return rowView;

    };

    /*private void printDataList (List mTimeHistoryList, List mPressureHistoryList, List mAccelHistoryList,
                                List mAltitudeHistoryList, List mTempHistoryList, List mHumidityHistoryList) {
        Log.d(LOG_TAG, "printDataList");

        ListIterator<String> lItr1 = mTimeHistoryList.listIterator();
        // hasNext() returns true if the list has more elements
        while (lItr1.hasNext())
        {
            // next() returns the next element in the iteration
            Log.d(LOG_TAG, "Time List :" + lItr1.next());
            //System.out.println(lItr.next());
        }

        ListIterator<String> lItr2 = mPressureHistoryList.listIterator();
        // hasNext() returns true if the list has more elements
        while (lItr2.hasNext())
        {
            // next() returns the next element in the iteration
            Log.d(LOG_TAG, "Pressure List :" + lItr2.next());
            //System.out.println(lItr.next());
        }

        ListIterator<String> lItr3 = mAccelHistoryList.listIterator();
        // hasNext() returns true if the list has more elements
        while (lItr3.hasNext())
        {
            // next() returns the next element in the iteration
            Log.d(LOG_TAG, "Accel List :" + lItr3.next());
            //System.out.println(lItr.next());
        }

        ListIterator<String> lItr4 = mAltitudeHistoryList.listIterator();
        // hasNext() returns true if the list has more elements
        while (lItr4.hasNext())
        {
            // next() returns the next element in the iteration
            Log.d(LOG_TAG, "Altitude List :" + lItr4.next());
            //System.out.println(lItr.next());
        }

        ListIterator<String> lItr5 = mTempHistoryList.listIterator();
        // hasNext() returns true if the list has more elements
        while (lItr5.hasNext())
        {
            // next() returns the next element in the iteration
            Log.d(LOG_TAG, "Temp List :" + lItr5.next());
            //System.out.println(lItr.next());
        }

        ListIterator<String> lItr6 = mHumidityHistoryList.listIterator();
        // hasNext() returns true if the list has more elements
        while (lItr6.hasNext())
        {
            // next() returns the next element in the iteration
            Log.d(LOG_TAG, "Humidity List :" + lItr6.next());
            //System.out.println(lItr.next());
        }
    } */
}


