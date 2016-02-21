package com.habosa.yoursquare.ui;

import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

public abstract class EnterListener implements TextView.OnEditorActionListener {

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        boolean doneAction = (actionId == EditorInfo.IME_ACTION_DONE);
        boolean hitEnter = (event != null &&
                event.getAction() == KeyEvent.ACTION_DOWN &&
                event.getKeyCode() == KeyEvent.KEYCODE_ENTER);

        if (doneAction || hitEnter) {
            onEnter();
            return true;
        }

        return false;
    }

    public abstract void onEnter();
}
