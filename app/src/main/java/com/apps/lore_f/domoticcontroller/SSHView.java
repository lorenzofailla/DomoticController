package com.apps.lore_f.domoticcontroller;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.inputmethod.InputMethodManager;


/**
 * Created by 105053228 on 08-Nov-17.
 */

public class SSHView extends AppCompatImageView {

    private static final String TAG="SSHView";

    public SSHView(Context context) {
        super(context);
    }

    public SSHView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SSHView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void showKeyboard() {

        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(getRootView(), InputMethodManager.SHOW_IMPLICIT);

    }


}
