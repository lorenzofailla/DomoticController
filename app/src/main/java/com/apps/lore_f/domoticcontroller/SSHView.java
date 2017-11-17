package com.apps.lore_f.domoticcontroller;

import android.content.Context;
import android.os.Handler;
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
    private char cursor='|';
    private long cursorBlinkOn=250L;
    private long cursorBlinkOff=750L;

    private int cursorCol;
    private int cursorRow;

    private String formattedText;
    private String cursorText;

    private Handler handler = new Handler();

    private Runnable showPlainText = new Runnable() {
        @Override
        public void run() {
            setText(formattedText);
            handler.postDelayed(showCursorText, cursorBlinkOff);
        }
    };

    private Runnable showCursorText = new Runnable() {
        @Override
        public void run() {
            setText(cursorText);
            handler.postDelayed(showPlainText, cursorBlinkOn);
        }
    };

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

    public void setFormattedText(String formattedText) {
        this.formattedText = formattedText;

        handler.removeCallbacks(showPlainText);
        handler.removeCallbacks(showCursorText);

        setText(formattedText);
        this.cursorText=formattedText;
        //cursorText = composeCursorText(formattedText);
        handler.postDelayed(showPlainText, cursorBlinkOn);

    }

    public void setCursor(int col, int row){

        cursorCol=col;
        cursorRow=row;

        Log.d(TAG, "Cursor position - col: "+cursorCol + " row: "+cursorRow);

        if(formattedText!=null) {
            cursorText = composeCursorText(formattedText);
        }
    }

    private String composeCursorText(String input){

        String[] rows=formattedText.split("\n");
        char[] line =  rows[cursorRow].toCharArray();
        line[cursorCol] = cursor;

        StringBuilder output = new StringBuilder();
        for(int i=0;i<rows.length;i++){

            if(i==cursorRow){

                output.append(line);
                output.append("\n");

            } else {

                output.append(rows[i]+"\n");

            }

        }

        Log.d(TAG, output.toString());

        return output.toString();

    }


}
