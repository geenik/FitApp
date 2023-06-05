package com.example.fitapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "StepCounter";
    private static final int REQUEST_OAUTH_REQUEST_CODE = 0x1001;
    private TextView dailyTextView;
    private TextView weeklyTextView;
    private TextView monthlyTextView;
    private Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dailyTextView = findViewById(R.id.daily);
        weeklyTextView = findViewById(R.id.weekely);
        monthlyTextView = findViewById(R.id.monthly);
        btn=findViewById(R.id.btn);

        FitnessOptions fitnessOptions = FitnessOptions.builder()
                .addDataType(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA)
                .build();

        if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(this), fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                    this,
                    REQUEST_OAUTH_REQUEST_CODE,
                    GoogleSignIn.getLastSignedInAccount(this),
                    fitnessOptions
            );
        } else {
            subscribe();
        }
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               daily();
               weekly();
               monthly();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_OAUTH_REQUEST_CODE) {
            subscribe();
        }
    }

    private void subscribe() {
        Fitness.getRecordingClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .subscribe(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.i(TAG, "Successfully subscribed!");
                        daily();
                        weekly();
                        monthly();
                    } else {
                        Log.w(TAG, "There was a problem subscribing.", task.getException());
                        showError("Failed to subscribe. Please try again.");
                    }
                });
    }

    private void daily() {
        Fitness.getHistoryClient(this, Objects.requireNonNull(GoogleSignIn.getLastSignedInAccount(this)))
                .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
                .addOnSuccessListener(dataSet -> {
                    long total = dataSet.isEmpty() ? 0 : dataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();
                    Log.i(TAG, "Total steps daily: " + total);
                    dailyTextView.setText("Daily: " + total);
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "There was a problem getting the step count.", e);
                    showError("Failed to retrieve daily step count. Please try again.");
                });
    }

    private void weekly() {
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
                .addOnSuccessListener(dataReadResponse -> {
                    long total = 0;
                    if (dataReadResponse.getBuckets().size() > 0) {
                        for (Bucket bucket : dataReadResponse.getBuckets()) {
                            List<DataSet> dataSets = bucket.getDataSets();
                            for (DataSet dataSet : dataSets) {
                                total += dataSet.isEmpty() ? 0 : dataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();
                            }
                        }
                    }
                    Log.i(TAG, "Total steps for weekly: " + total);
                    weeklyTextView.setText("Weekly: " + total);
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "There was a problem getting the step count.", e);
                    showError("Failed to retrieve weekly step count. Please try again.");
                });
    }

    private void monthly() {
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
                .addOnSuccessListener(dataReadResponse -> {
                    long total = 0;
                    if (dataReadResponse.getBuckets().size() > 0) {
                        for (Bucket bucket : dataReadResponse.getBuckets()) {
                            List<DataSet> dataSets = bucket.getDataSets();
                            for (DataSet dataSet : dataSets) {
                                total += dataSet.isEmpty() ? 0 : dataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();
                            }
                        }
                    }
                    Log.i(TAG, "Total steps for monthly: " + total);
                    monthlyTextView.setText("Monthly: " + total);
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "There was a problem getting the step count.", e);
                    showError("Failed to retrieve monthly step count. Please try again.");
                });
    }

    private void showError(String errorMessage) {
        // Display an error message to the user
        // You can customize this to show an error dialog, toast, etc.
        Toast.makeText(this,errorMessage,Toast.LENGTH_SHORT).show();
    }
}
