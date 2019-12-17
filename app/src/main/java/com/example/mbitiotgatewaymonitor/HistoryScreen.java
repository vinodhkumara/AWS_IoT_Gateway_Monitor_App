package com.example.mbitiotgatewaymonitor;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class HistoryScreen extends AppCompatActivity {

    static final String LOG_TAG = "VINO_IOT: HistoryScreen";
    ListView mHistoryListView;
    TextView mHistoryTextView;
    //private List<String> mRxHistoryList;
    List<String> mRxTempList = new ArrayList<String>();
    List<String> mRxHumList = new ArrayList<String>();
    List<String> mRxPressList = new ArrayList<String>();
    List<String> mRxAltiList = new ArrayList<String>();
    List<String> mRxAccelList = new ArrayList<String>();
    List<String> mRxTimeList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "OnCreate");
        setContentView(R.layout.activity_history_screen);

        mHistoryListView = (ListView) findViewById(R.id.history_listView);
        mHistoryTextView = (TextView) findViewById(R.id.history_textView);

        Intent intent = getIntent();
        mRxTimeList = (List<String>) intent.getSerializableExtra("time_list");
        mRxPressList = (List<String>) intent.getSerializableExtra("pressure_list");
        mRxAccelList = (List<String>) intent.getSerializableExtra("accel_list");
        mRxAltiList = (List<String>) intent.getSerializableExtra("altitude_list");
        mRxTempList = (List<String>) intent.getSerializableExtra("temp_list");
        mRxHumList = (List<String>) intent.getSerializableExtra("humidity_list");
        //printHistoryList(mRxHistoryList);
        Utils.printDataList(mRxTimeList,mRxPressList,mRxAccelList,mRxAltiList,mRxTempList,mRxHumList);

        /*final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, mRxHistoryList);
        mHistoryListView.setAdapter(adapter);*/

        HistoryAdapter adapter = new HistoryAdapter(this, mRxTimeList, mRxPressList, mRxAccelList, mRxAltiList, mRxTempList, mRxHumList);
        mHistoryListView.setAdapter(adapter);
    }

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
