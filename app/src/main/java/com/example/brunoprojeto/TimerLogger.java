package com.example.brunoprojeto;

import android.util.Log;

public class TimerLogger {

    private long tini;
    private long tfin;

    public TimerLogger() {
        tini = System.currentTimeMillis();
        tfin = System.currentTimeMillis();
    }

    public void finish() {
        Log.d("TimerLogger", "Tempo de início: " + tini + ", Tempo de fim: " + tfin);
    }

    public long getTini() {
        return tini;
    }

    public long getTfin() {
        return tfin;
    }
}
