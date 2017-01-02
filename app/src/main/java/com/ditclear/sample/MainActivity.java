package com.ditclear.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity{





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }


    public void inRxJava(View view) {
        startActivity(RxjavaActivity.class);
    }



    public void inExecutor(View view) {
        startActivity(ExecutorActivity.class);
    }

    public void inHandler(View view) {
        startActivity(HandlerActivity.class);
    }

    private void startActivity(Class aClass) {
        startActivity(new Intent(this,aClass));
    }
}
