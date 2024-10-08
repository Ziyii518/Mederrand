package com.app.mederrand.utils.broadcost;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Objects;

public class TimeChangeBroadcastReceiver extends BroadcastReceiver {

    final OnDateChangeListener onDateChangeListener;

    public TimeChangeBroadcastReceiver(OnDateChangeListener onDateChangeListener) {
        this.onDateChangeListener = onDateChangeListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (Objects.equals(action, Intent.ACTION_DATE_CHANGED) || Objects.equals(action, Intent.ACTION_TIME_CHANGED)) {
            if (onDateChangeListener != null)
                onDateChangeListener.onDateChanged();
        }
    }

    public interface OnDateChangeListener {
        void onDateChanged();
    }
}
