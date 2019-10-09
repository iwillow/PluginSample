package com.iwilliow.app.android.plugin;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class TimeCache {
    private static final String TAG = "TimeCache";
    public static Map<String, Long> sStartTime = new HashMap<>();
    public static Map<String, Long> sEndTime = new HashMap<>();

    public static void addStartTime(String methodName, long time) {
        sStartTime.put(methodName, time);
    }

    public static void addEndTime(String methodName, long time) {
        sEndTime.put(methodName, time);
    }

    public static void startCost(String methodName) {
        long start = sStartTime.get(methodName);
        long end = sEndTime.get(methodName);
        Log.d(TAG, "method: " + methodName + " main " + (end - start) + " ns");
    }
}
