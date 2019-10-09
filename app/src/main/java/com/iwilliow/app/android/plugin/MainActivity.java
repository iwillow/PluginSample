package com.iwilliow.app.android.plugin;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.iwilliow.annotation.Trace;

public class MainActivity extends AppCompatActivity {
    @Trace
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Trace
    @Override
    protected void onStart() {
        super.onStart();
    }

    @Trace
    @Override
    protected void onResume() {
        super.onResume();
    }

    @Trace
    @Override
    protected void onStop() {
        super.onStop();
    }
}
