package com.eagleeye.app;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView tv = new TextView(this);
        tv.setText("Eagle Eye\nPrivacy & Security Monitor");
        tv.setTextSize(18);
        setContentView(tv);
    }
}
