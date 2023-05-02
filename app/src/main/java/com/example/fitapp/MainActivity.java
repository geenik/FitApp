package com.example.fitapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * This sample demonstrates combining the Recording API and History API of the Google Fit platform
 * to record steps, and display the daily current step count. It also demonstrates how to
 * authenticate a user with Google Play Services.
 */
public class MainActivity extends AppCompatActivity {

    public static final String TAG = "StepCounter";
    private static final int REQUEST_OAUTH_REQUEST_CODE = 0x1001;
    TextView daily;
    TextView weekely;
    TextView monthly;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        daily=findViewById(R.id.daily);
        weekely=findViewById(R.id.weekely);
        monthly=findViewById(R.id.monthly);
        // This method sets up our custom logger, which will print all log messages to the device
        // screen, as well as to adb logcat.
        FitnessOptions fitnessOptions =
                FitnessOptions.builder()
                        .addDataType(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                        .addDataType(DataType.TYPE_STEP_COUNT_DELTA)
                        .build();
        if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(this), fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                    this,
                    REQUEST_OAUTH_REQUEST_CODE,
                    GoogleSignIn.getLastSignedInAccount(this),
                    fitnessOptions);
        } else {
            subscribe();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_OAUTH_REQUEST_CODE) {
                subscribe();
            }
        }
    }

    public void subscribe() {
        // To create a subscription, invoke the Recording API. As soon as the subscription is
        // active, fitness data will start recording.
        Fitness.getRecordingClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .subscribe(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                .addOnCompleteListener(
                        new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.i(TAG, "Successfully subscribed!");
                                    daily();
                                    weekely();
                                    monthly();
                                } else {
                                    Log.w(TAG, "There was a problem subscribing.", task.getException());
                                }
                            }
                        });
    }

    private void daily() {
        Fitness.getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
                .addOnSuccessListener(
                        new OnSuccessListener<DataSet>() {
                            @Override
                            public void onSuccess(DataSet dataSet) {
                                long total =
                                        dataSet.isEmpty()
                                                ? 0
                                                : dataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();
                                Log.i(TAG, "Total steps daily: " + total);
                                daily.setText("Daily : " +Integer.toString((int) total));
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "There was a problem getting the step count.", e);
                            }
                        });
    }
    void  weekely() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.WEEK_OF_YEAR, -1);
        long startTime = cal.getTimeInMillis();

        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();

        Fitness.getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .readData(readRequest)
                .addOnSuccessListener(
                        new OnSuccessListener<DataReadResponse>() {
                            @Override
                            public void onSuccess(DataReadResponse dataReadResponse) {
                                if (dataReadResponse.getBuckets().size() > 0) {
                                    long total= 0;
                                    for (Bucket bucket : dataReadResponse.getBuckets()) {
                                        List<DataSet> dataSets = bucket.getDataSets();
                                        for (DataSet dataSet : dataSets) {
                                             total +=
                                                    dataSet.isEmpty()
                                                            ? 0
                                                            : dataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();
                                        }
                                    }
                                    Log.i(TAG, "Total steps for weekely"+ total);
                                    weekely.setText("Weekly : " +Integer.toString((int) total));
                                }


                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "There was a problem getting the step count.", e);
                            }
                        });
    }
    void monthly(){
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.MONTH, -1);
        long startTime = cal.getTimeInMillis();

        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();

        Fitness.getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .readData(readRequest)
                .addOnSuccessListener(
                        new OnSuccessListener<DataReadResponse>() {
                            @Override
                            public void onSuccess(DataReadResponse dataReadResponse) {
                                long total= 0;
                                if (dataReadResponse.getBuckets().size() > 0) {
                                    for (Bucket bucket : dataReadResponse.getBuckets()) {
                                        List<DataSet> dataSets = bucket.getDataSets();
                                        for (DataSet dataSet : dataSets) {
                                            total +=
                                                    dataSet.isEmpty()
                                                            ? 0
                                                            : dataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();
                                        }
                                    }
                                }
                                Log.i(TAG, "Total steps for month" + total);
                                monthly.setText("Monthly : " +Integer.toString((int) total));
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "There was a problem getting the step count.", e);
                            }
                        });
    }
}
