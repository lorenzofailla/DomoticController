package com.apps.lore_f.domoticcontroller;

import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.text.Editable;
import android.text.method.KeyListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by 105053228 on 08-Nov-17.
 */

public class SSHView extends AppCompatTextView {

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

    public void showKeyboard(){

        InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(this.getRootView(), InputMethodManager.SHOW_IMPLICIT);

    }

}
