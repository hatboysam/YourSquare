package com.habosa.yoursquare;

import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;

/**
 * Created by samstern on 11/25/15.
 */
public abstract class DebouncingWatcher implements TextWatcher {

    private int mDelayMillis;
    private String mText;
    private final Handler mHandler = new Handler();
    private final Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            onNewText(mText);
        }
    };

    public DebouncingWatcher(int delayMillis) {
        mDelayMillis = delayMillis;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {}

    @Override
    public void afterTextChanged(Editable s) {
        mText = s.toString();
        mHandler.removeCallbacks(mRunnable);
        mHandler.postDelayed(mRunnable, mDelayMillis);
    }

    public abstract void onNewText(String text);

}
