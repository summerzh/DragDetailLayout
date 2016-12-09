package com.it.dragdetaillayout;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DragDetailLayout2 dragDetailLayout2 = (DragDetailLayout2) findViewById(R.id.dl_container);

        Log.d("result", "onCreate ");
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.fl_top, TopFragment.getInstance(dragDetailLayout2));
        transaction.replace(R.id.fl_bottom, new BottomFragment());
        transaction.commit();
    }

}
