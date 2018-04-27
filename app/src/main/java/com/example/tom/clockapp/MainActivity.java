package com.example.tom.clockapp;

import android.annotation.SuppressLint;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class MainActivity extends AppCompatActivity {

    @BindView(R.id.clock)  TextView text;
    @BindView(R.id.startBtn)  Button startButton;
    @BindView(R.id.layout)
    FrameLayout layout;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("start");


    private Handler clockHandler = new Handler();
    boolean isStarted = false;
    long startTime = 0L, timeInMs = 0L, timeSwapBuf = 0L, updateTime = 0L;

    Runnable r = new Runnable() {
        @Override
        public void run() {
            timeInMs = SystemClock.uptimeMillis() - startTime;
            updateTime = timeSwapBuf + timeInMs;
            int minutes = (int) (updateTime/60000);
            float seconds = updateTime%60000;
            text.setText(String.format("%02d:%05.2f", minutes, seconds/1000));
            clockHandler.postDelayed(this, 0);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        
        myRef.setValue("ready");
        
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                if(value.equals("started")){
                    Toast.makeText(MainActivity.this, "started", Toast.LENGTH_SHORT).show();
                    startTime = SystemClock.uptimeMillis();
                    clockHandler.postDelayed(r, 0);
                    isStarted = true;

                    startButton.setText("STOP");

                }
                if(value.equals("stopped")){
                    timeSwapBuf += timeInMs;
                    clockHandler.removeCallbacks(r);
                    isStarted = false;

                    startButton.setText("Start".toUpperCase());

                }
                if(value.equals("ready")){
                    startTime = 0L;
                    timeInMs = 0L;
                    timeSwapBuf = 0L;
                    updateTime = 0L;
                    text.setText(R.string.defaultTime);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
            }
        });
        text.setText(R.string.defaultTime);

        final GestureDetector gd = new GestureDetector(this,
                new GestureDetector.SimpleOnGestureListener(){


            //here is the method for double tap


            @Override
            public boolean onDoubleTap(MotionEvent e) {

               if(!isStarted){
                   startTime = 0L;
                   timeInMs = 0L;
                   timeSwapBuf = 0L;
                   updateTime = 0L;
                   text.setText(R.string.defaultTime);
                   myRef.setValue("ready");
               }


                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                super.onLongPress(e);

            }

            @Override
            public boolean onDoubleTapEvent(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }


        });

//here yourView is the View on which you want to set the double tap action

        layout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gd.onTouchEvent(event);
            }
        });


    }


    @Override
    protected void onStart() {
        super.onStart();
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    @OnClick(R.id.startBtn) void onStartClicked(){

        if(isStarted){
            myRef.setValue("stopped");
            timeSwapBuf += timeInMs;
            clockHandler.removeCallbacks(r);
        }
        else{
            myRef.setValue("started");
            startTime = SystemClock.uptimeMillis();
            clockHandler.postDelayed(r, 0);
        }

        isStarted = !isStarted;
        if(isStarted){
            startButton.setText("STOP");
        }
        else {
            startButton.setText("Start".toUpperCase());
        }

    }

    
}
