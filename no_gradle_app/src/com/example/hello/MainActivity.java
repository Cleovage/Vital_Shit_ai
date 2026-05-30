package com.example.hello;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // We will manually set the content view if we have compiled resources,
        // but for a simple "no gradle" demo, we can just use code-based UI
        // or assume the resource ID will be available after aapt2.
        // For simplicity in this demo, let's just create a TextView in code.
        android.widget.TextView tv = new android.widget.TextView(this);
        tv.setText("Hello, No Gradle! (Built manually)");
        setContentView(tv);
    }
}