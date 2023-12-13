package com.example.finalmobileproject;

import static android.Manifest.permission.RECORD_AUDIO;

import static java.lang.Math.log10;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentValues;
import android.content.pm.PackageManager;
//import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    // Initialising all variable from xml
    private TextView decibelVal, decibelLAeq, decibelLmax, decibelLoud;
    private ImageButton btnStart, btnStop;

    // Initialising linechart
    private LineChart decibelChart;

    // creating a variable for media recorder
    private MediaRecorder mediaRecorder;
    // audio permission code
    private static final int PERMISSION_REQUEST_CODE = 21;
    private boolean isRecording;

    private double currentDecibel = 0;
    private double currentLAeq = 0;
    private double currentLmax = 0;



    // This will LAeq calculation over period
    double totalTime = 0;
    double totalEnergy = 0;
    private ArrayList<Entry> soundLevelVal = new ArrayList<>();
    private SQLiteDatabase myDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        decibelVal = findViewById(R.id.decibelValue);
        decibelLAeq = findViewById(R.id.lAeqDecibel);
        decibelLmax = findViewById(R.id.lMaxDecibel);
        decibelLoud = findViewById(R.id.loudDecibel);
        decibelChart = findViewById(R.id.decibel_lineChart);

        btnStart = findViewById(R.id.startBtn);
        btnStop = findViewById(R.id.stopBtn);

        // Initialise database
        DecibelDBHelper dbHelper = new DecibelDBHelper(this);
        myDB = dbHelper.getWritableDatabase();
        ArrayList<DecibelDB> allDecibels = dbHelper.getAllItems();

        // Do something with the retrieved data
        for (DecibelDB decibel : allDecibels) {
            // Do something with each DecibelDB object
            Log.d("DecibelDB", "ID: " + decibel.getID() + ", Current Decibel: " + decibel.getCurrentDecibel());
        }

        // Start recording button
        btnStart.setOnClickListener(view -> {
            // calling start recording method
            startRecording();
        });

        // Stop recording button
        btnStop.setOnClickListener(view -> {
            // calling stop recording method
            stopRecording();
        });

    }

    // this handle the Request Permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // this will check if the request code matches the permission request code
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {
                    // Checks if the permission to use the microphone is granted
                    boolean permissionToRecord = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    // this will display a Toast message on the phone if permission is granted or not
                    if (permissionToRecord) {
                        Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }

    // This checks if the application has the necessary permission to record
    public boolean CheckPermissions() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED;
    }


    // Request permission to record
    private void RequestPermissions() {
        ActivityCompat.requestPermissions(MainActivity.this, new String[] {RECORD_AUDIO}, PERMISSION_REQUEST_CODE);
    }



    private void startRecording() {
        // this check if the permission required is granted
        if (CheckPermissions()) {
            // initialising new variable of MediaRecorder
            mediaRecorder = new MediaRecorder();

            // source for using a mic
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            // the audio output format
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            // audio encoder
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            // Output file location
            mediaRecorder.setOutputFile(getFilesDir().getAbsolutePath() + "/audio_record.3gp");

            // Use the try and catch method
            try {
                // Prepare and start recording
                mediaRecorder.prepare();
                mediaRecorder.start();
                isRecording = true;
            } catch (IOException e) {
                // This will handle exception ig prepare() fails
                Log.e("LOG_TAG", "prepare() failed");
                e.printStackTrace();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }

            // calling continous following methods for continous updates
            updateDecibelValue();
            updatingLeqValue();
            updatingLmaxValue();
            updatingChart();

            // This method will insert value into the database
            insertDecibelData(currentDecibel, currentLAeq, currentLmax, decibelLoud.getText().toString());

        } else {
            // calling the request method if permission is not granted
            RequestPermissions();
        }
    }

    private void stopRecording() {
        // Checks mediaRecorder is initialised
        if (mediaRecorder != null) {
            // Attempt to stop recording
            try {
                mediaRecorder.stop();
            } catch (RuntimeException runtimeException){
                // This handle a potential runtime exception
                runtimeException.printStackTrace();
            }

            mediaRecorder.release();
            mediaRecorder = null;

            // Set recording booleen to false
            isRecording = false;
        }
    }


    private void updateDecibelValue() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Checks if it recording
            if (isRecording) {
                // Get the amplitide value and use that to calculate the sound level
                double amplitude = mediaRecorder.getMaxAmplitude();
                double soundlevel = 20*log10(amplitude / 32767.0);

                // update the currents sound level and display the result to the application UI
                currentDecibel = soundlevel;
                decibelVal.setText(String.format("Sound level: %.2f dB", currentDecibel));

//                if (maxDecibel > currentDecibel){
//                    decibelMax.setText(String.format("Sound level: %.2f dB", maxDecibel));
//                }
//
//                if (minDecibel < currentDecibel) {
//                    decibelMin.setText(String.format("Sound level: %.2f dB", minDecibel));
//                }

                // calling recursive for contionous update
                updateDecibelValue();
            }
        }, 500); // Setting the interval for updates

    }

    private void updatingLeqValue() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isRecording) {
                double amplitude = mediaRecorder.getMaxAmplitude();
                double soundlevel = 20*log10(amplitude / 32767.0);

                // Calculate the energy level
                double eq_energy = Math.pow(10, (soundlevel / 10));
                totalEnergy += eq_energy;
                totalTime += 1;

                // Calculating LAeq and updates
                double laeq = 10 * Math.log10(totalEnergy / totalTime);
                currentLAeq = laeq;
                decibelLAeq.setText(String.format("LAeq: %.2f dB", currentLAeq));

                // calling recursive for contionous update
                updatingLeqValue();
            }
        }, 5000);
    }

    private void updatingLmaxValue() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isRecording) {
                // Calculate the Lmax value in decibel unit
                double amplitude = mediaRecorder.getMaxAmplitude();
                double lmax = 20 * Math.log10(1 / amplitude);

                // this will update current Lmax value in the application UI
                currentLmax = lmax;
                decibelLmax.setText(String.format("Lmax: %.2f dB", currentLmax));
            }
        }, 1000);
    }




    private void updatingChart() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isRecording) {
                // Add the current decibel entry into a dataset
                soundLevelVal.add(new Entry(soundLevelVal.size(), (float) currentDecibel));

                // this will limit the number of entries to avoid performance issues
                if (soundLevelVal.size() > 50) {
                    soundLevelVal.remove(0);
                }

                // Update the chart
                LineDataSet dataSet = new LineDataSet(soundLevelVal, "Sound Level");
                dataSet.setDrawCircles(false);
                dataSet.setDrawValues(false);
                ArrayList<ILineDataSet> dataSets = new ArrayList<>();
                dataSets.add(dataSet);


                LineData lineData = new LineData(dataSets);
                decibelChart.setData(lineData);
                decibelChart.notifyDataSetChanged();
                decibelChart.invalidate();

                // Recursive call for continuous updates
                //updatingChart();

//                decibelChart.getDescription().setEnabled(false);
//                decibelChart.setDrawGridBackground(false);
//                decibelChart.setDrawBorders(false);
//
//                // Enabling touch gesture
//                decibelChart.setTouchEnabled(true);
//                decibelChart.setDragEnabled(true);
//                decibelChart.setScaleEnabled(true);
//
//                // Setting up X - axis
//                XAxis xAxis = decibelChart.getXAxis();
//                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
//                xAxis.setDrawAxisLine(false);
//                xAxis.setDrawGridLines(false);
//
//                // Setting up lef Y - axis
//                YAxis leftYAxis = decibelChart.getAxisLeft();
//                leftYAxis.setDrawAxisLine(true);
//                leftYAxis.setDrawGridLines(true);
//
//                // Setting up right Y - axis
//                YAxis rightYAxis = decibelChart.getAxisRight();
//                rightYAxis.setEnabled(false);
//
//                // Setting up legends
//                Legend legend = decibelChart.getLegend();
//                legend.setEnabled(false);
//
//                soundLevelVal.add(new Entry(System.currentTimeMillis(), (float) currentDecibel));
//
//                LineDataSet dataSet = new LineDataSet(soundLevelVal, "Sound Level");
//                dataSet.setDrawCircles(false);
//                dataSet.setDrawCircles(false);
//                dataSet.setDrawValues(false);
//                dataSet.setFillColor(Color.rgb(132, 255, 204));
//
//                LineData lineData = new LineData(dataSet);
//                decibelChart.setData(lineData);
//                decibelChart.notifyDataSetChanged();
//
//                // Refreshes the chart
//                decibelChart.invalidate();

                updatingChart();
            }
        }, 500);
    }


    @Override
    protected void onStop() {
        super.onStop();
        stopRecording();
    }


    private void insertDecibelData(double currentDecibel, double currentLAeq, double currentLmax, String loudlevel) {
        ContentValues cv = new ContentValues();

        // putting the dato into a contentValues
        cv.put(DecibelDBHelper.COLUMN_CURRENT_DECIBEL, currentDecibel);
        cv.put(DecibelDBHelper.COLUMN_LAEQ_DECIBEL, currentLAeq);
        cv.put(DecibelDBHelper.COLUMN_LMAX_DECIBEL, currentLmax);
        cv.put(DecibelDBHelper.COLUMN_LOUD_LEVEL, loudlevel);

        // Insert the data into the table
        long dbId = myDB.insert(DecibelDBHelper.TABLE_NAME, null, cv);
       // Checking if the data insertion was successful
        if (dbId != -1) {
            Log.d("Database", "Data inserted successfully");
        } else {
            Log.e("Database", "Error occured while inserting data");
        }

        // Insert the data into the table
        //myDB.insert(DecibelDBHelper.TABLE_NAME, null, cv);
    }





}