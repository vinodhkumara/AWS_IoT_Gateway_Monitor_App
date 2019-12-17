package com.example.mbitiotgatewaymonitor;

import android.util.Log;

import java.util.List;
import java.util.ListIterator;

public class Utils {

    static final String LOG_TAG = "VINO_IOT: Utils";
    public static void printDataList (List mTimeHistoryList, List mPressureHistoryList, List mAccelHistoryList,
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
    }
}
