package com.example.tom.clockapp;

import android.os.CountDownTimer;

public abstract class CountUpTimer extends CountDownTimer {
    private static final long INTERVAL_MS = 1000;
    private final long duration;

    protected CountUpTimer(long durationMs) {
        super(durationMs, INTERVAL_MS);
        this.duration = durationMs;
    }

    public abstract void onTick(int second);

    @Override
    public void onTick(long msUntilFinished) {
        int ms = (int) ((duration - msUntilFinished));
        onTick(ms);
    }

    @Override
    public void onFinish() {
        onTick(duration / 1000);
    }
}