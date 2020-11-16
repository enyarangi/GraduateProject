package com.datalogerphone.frank.dataloger_phone;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.amplifyframework.AmplifyException;
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.storage.s3.AWSS3StoragePlugin;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements
        SensorEventListener {

    private ArrayList<SensorRecord> accelRecord, gyroRecord;
    float accel[];
    float gyro[];
    private long timestamp;
    private String delay;

    private TextView accelX, accelY, accelZ, gyroX, gyroY, gyroZ, txtStatus, recordView, storeAddressView;
    private ToggleButton recordToggleButton;
    private RadioButton delayFastestRadioButton, delayGameRadioButton, delayUiRadioButton, delayNormalRadioButton;
    private RadioGroup radioGroup;

    private SensorManager sensorManager;
    private Sensor accelerometer, gyroscope; // actual sensor objects
    private File root = Environment.getExternalStorageDirectory();


    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            accel[0] = event.values[0];
            accel[1] = event.values[1];
            accel[2] = event.values[2];
            java.util.Date date = new java.util.Date(); // current date for timestamp purposes
            if (recordToggleButton.isChecked()) {
                appendToUI(recordView, "Recording...");
                delayFastestRadioButton.setEnabled(false);
                delayGameRadioButton.setEnabled(false);
                delayUiRadioButton.setEnabled(false);
                delayNormalRadioButton.setEnabled(false);
                if (timestamp != date.getTime()) {
                    timestamp = date.getTime();
                    accelRecord.add(new SensorRecord(timestamp, accel));
                }
            } else if (!accelRecord.isEmpty()) {
                delayFastestRadioButton.setEnabled(true);
                delayGameRadioButton.setEnabled(true);
                delayUiRadioButton.setEnabled(true);
                delayNormalRadioButton.setEnabled(true);
                appendToUI(recordView, "Recording Stopped");
                saveFile(accelRecord, "Accelerometer_phone");

                accelRecord.clear();
            }
        }

        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            gyro[0] = event.values[0];
            gyro[1] = event.values[1];
            gyro[2] = event.values[2];

            java.util.Date date = new java.util.Date(); // current date for timestamp purposes
            if (recordToggleButton.isChecked()) {
                appendToUI(recordView, "Recording...");
                if (timestamp != date.getTime()) {
                    timestamp = date.getTime();
                    gyroRecord.add(new SensorRecord(timestamp, gyro));
                }
            } else if (!gyroRecord.isEmpty()) {
                appendToUI(recordView, "Recording Stopped");
                saveFile(gyroRecord, "Gyroscope_phone");
                gyroRecord.clear();
            }
        }
        displayData();

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        accelX = (TextView) findViewById(R.id.accelX);
        accelY = (TextView) findViewById(R.id.accelY);
        accelZ = (TextView) findViewById(R.id.accelZ);
        gyroX = (TextView) findViewById(R.id.gyroX);
        gyroY = (TextView) findViewById(R.id.gyroY);
        gyroZ = (TextView) findViewById(R.id.gyroZ);
        txtStatus = (TextView) findViewById(R.id.txtStatus);
        recordView = (TextView) findViewById(R.id.recordView);
        storeAddressView = (TextView) findViewById(R.id.storeAddressView);
        recordToggleButton = (ToggleButton) findViewById(R.id.recordToggleButton);
        delayFastestRadioButton = (RadioButton) findViewById(R.id.delayFastestRadioButton);
        delayGameRadioButton = (RadioButton) findViewById(R.id.delayGameRadioButton);
        delayUiRadioButton = (RadioButton) findViewById(R.id.delayUiRadioButton);
        delayNormalRadioButton = (RadioButton) findViewById(R.id.delayNormalRadioButton);
        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        initialVars();
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                sensorManager.unregisterListener(MainActivity.this);
                switch (checkedId) {
                    case R.id.delayFastestRadioButton:
                        sensorManager.registerListener(MainActivity.this, gyroscope, SensorManager.SENSOR_DELAY_FASTEST);
                        sensorManager.registerListener(MainActivity.this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
                        Toast.makeText(MainActivity.this, "Sensor delay changed to FASTEST", Toast.LENGTH_SHORT).show();
                        delay = "FASTEST";
                        break;
                    case R.id.delayGameRadioButton:
                        sensorManager.registerListener(MainActivity.this, gyroscope, SensorManager.SENSOR_DELAY_GAME);
                        sensorManager.registerListener(MainActivity.this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
                        Toast.makeText(MainActivity.this, "Sensor delay changed to GAME", Toast.LENGTH_SHORT).show();
                        delay = "GAME";
                        break;
                    case R.id.delayUiRadioButton:
                        sensorManager.registerListener(MainActivity.this, gyroscope, SensorManager.SENSOR_DELAY_UI);
                        sensorManager.registerListener(MainActivity.this, accelerometer, SensorManager.SENSOR_DELAY_UI);
                        Toast.makeText(MainActivity.this, "Sensor delay changed to UI", Toast.LENGTH_SHORT).show();
                        delay = "UI";
                        break;
                    case R.id.delayNormalRadioButton:
                        sensorManager.registerListener(MainActivity.this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
                        sensorManager.registerListener(MainActivity.this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
                        Toast.makeText(MainActivity.this, "Sensor delay changed to NORMAL", Toast.LENGTH_SHORT).show();
                        delay = "NORMAL";
                        break;
                }


            }
        });

        appendToUI(storeAddressView, "*Data saved at: " + root.toString());

        //Function to request storage permission
        ArrayList<String> arrPerm = new ArrayList<>();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            arrPerm.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (!arrPerm.isEmpty()) {
            String[] permissions = new String[arrPerm.size()];
            permissions = arrPerm.toArray(permissions);
            ActivityCompat.requestPermissions(this, permissions, 4);
        }

        //Initialize Amplify
        try {
            // Add these lines to add the AWSCognitoAuthPlugin and AWSS3StoragePlugin plugins
            Amplify.addPlugin(new AWSCognitoAuthPlugin());
            Amplify.addPlugin(new AWSS3StoragePlugin());
            Amplify.configure(getApplicationContext());

            Log.e("Data Logger", "Initialized Amplify");
//Sign up a user on AWS
//            Amplify.Auth.signUp(
//                    "dlogeruser001",
//                    "kenya2030",
//                    AuthSignUpOptions.builder().userAttribute(AuthUserAttributeKey.email(), "njengajohn37@gmail.com").build(),
//                    result -> Log.e("AuthQuickStart ", "Result: " + result.toString()),
//                    error -> Log.e("AuthQuickStart", "Sign up failed", error)
//            );

            //Confirm new  user Sign up
//            Amplify.Auth.confirmSignUp(
//                    "dlogeruser001",
//                    "442312",
//                    result -> Log.e("AuthQuickstart", result.isSignUpComplete() ? "Confirm signUp succeeded" : "Confirm sign up not complete"),
//                    error -> Log.e("AuthQuickstart", error.toString())
//            );


            //Authenticate User
            Amplify.Auth.signIn(
                    "dlogeruser001",
                    "kenya2030",
                    result -> Log.e("AuthQuickstart", result.isSignInComplete() ? "Sign in succeeded" : "Sign in not complete"),
                    error -> Log.e("AuthQuickstart", error.toString())
            );


        } catch (AmplifyException error) {
            Log.e("Data Logger", "Could not initialize Amplify", error);
        }

    }

    protected void onDestroy() {
        super.onDestroy();
    }

    private void initialVars() {

        /* Get the SensorManager from the system service */
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        /* Check if accelerometer is present on device */
        if (sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER).size() == 0) {
            txtStatus.setText("No accelerometer installed");
      }

        else if (sensorManager.getSensorList(Sensor.TYPE_GYROSCOPE).size() == 0) {
            txtStatus.setText("No gyroscope installed");
       }
       else { // register listeners - set sensor delays - default SENSOR_DELAY_UI
            accelerometer = sensorManager.getSensorList(
                    Sensor.TYPE_ACCELEROMETER).get(0);
            gyroscope = sensorManager.getSensorList(
                    Sensor.TYPE_GYROSCOPE).get(0);

            sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_UI);
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }

        accelRecord = new ArrayList<>();
        gyroRecord = new ArrayList<>();
        accel = new float[3];
        gyro = new float[3];
        delay = "UI";

//        accelX = (TextView) findViewById(R.id.accelX);
//        accelY = (TextView) findViewById(R.id.accelY);
//        accelZ = (TextView) findViewById(R.id.accelZ);
//        gyroX = (TextView) findViewById(R.id.gyroX);
//        gyroY = (TextView) findViewById(R.id.gyroY);
//        gyroZ = (TextView) findViewById(R.id.gyroZ);
//        txtStatus = (TextView) findViewById(R.id.txtStatus);
//        recordView = (TextView) findViewById(R.id.recordView);
//        storeAddressView = (TextView) findViewById(R.id.storeAddressView);
//        recordToggleButton = (ToggleButton) findViewById(R.id.recordToggleButton);
//        delayFastestRadioButton = (RadioButton) findViewById(R.id.delayFastestRadioButton);
//        delayGameRadioButton = (RadioButton) findViewById(R.id.delayGameRadioButton);
//        delayUiRadioButton = (RadioButton) findViewById(R.id.delayUiRadioButton);
//        delayNormalRadioButton = (RadioButton) findViewById(R.id.delayNormalRadioButton);
//        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
    }

    private void displayData() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                accelX.setText("X: " + accel[0]);
                accelY.setText("Y: " + accel[1]);
                accelZ.setText("Z: " + accel[2]);

                gyroX.setText("X: " + gyro[0]);
                gyroY.setText("Y: " + gyro[1]);
                gyroZ.setText("Z: " + gyro[2]);
            }
        });

    }

    private void saveFile(ArrayList<SensorRecord> dataRecord, String dataType) {

        /*
        Firstly, convert the records to string
         */
            StringBuilder stringRecords = new StringBuilder();
            String convertedRecord;

            for (int i = 0; i < dataRecord.size(); i++) {
                stringRecords.append(dataRecord.get(i).getSensor(0));
                stringRecords.append("," + dataRecord.get(i).getSensor(1));
                stringRecords.append("," + dataRecord.get(i).getSensor(2));
                stringRecords.append("," + dataRecord.get(i).getTimestamp() + "\n");
            }
            convertedRecord = stringRecords.toString();

        /*
        Secondly, save the record into a .csv file
         */
            String fileName;
            try {
                // If SD Card directory is accesible, write records to card
                if (root.canWrite()) {

                    //System.out.println("Can write");
                    Toast.makeText(MainActivity.this, "Can Write", Toast.LENGTH_SHORT).show();
                    java.util.Date date = new java.util.Date();
                    Timestamp ts = new Timestamp(date.getTime());

                    String datetime = new java.text.SimpleDateFormat("dd_MM_yyyy_HH_mm_ss").format(ts);
                    fileName = dataType + "_delay_" + delay + "_" + datetime + ".csv";


                    File gpxfile = new File(root, fileName);
                    FileWriter gpxwriter = new FileWriter(gpxfile);
                    BufferedWriter out = new BufferedWriter(gpxwriter);
                    out.write(convertedRecord);
                    out.close();

                    //Create a file to upload to amazon s3
                    File s3uploadFile = new File(getApplicationContext().getFilesDir(), fileName);
                    try {
                        BufferedWriter out1 = new BufferedWriter(new FileWriter(s3uploadFile));
                        out1.write(convertedRecord);
                        out1.close();
                    } catch (Exception exception) {
                        Log.e("Data Logger", "Upload failed", exception);
                    }

                    //Upload file to amazon s3
                    //To check uploaded files, check https://console.aws.amazon.com/s3/buckets/datalogerphonec634190842054aaf9bb91f056bd4090a174542-dev/public/?region=us-east-2&tab=overview
                    Amplify.Storage.uploadFile(
                            fileName,
                            s3uploadFile,
                            result -> Log.e("Data Logger", "Successfully uploaded  " + result.getKey()),
                            storageFailure -> Log.e("Data Logger", "Upload failed", storageFailure)
                    );



                } else { // SD Card is not available to write
                    //System.out.println("Can NOT write");
                    Toast.makeText(MainActivity.this, "Can NOT Write. Please ensure the app has storage permission", Toast.LENGTH_SHORT).show();

                }
            } catch (IOException e) {
                System.out.println("could not log readings");
            }

    }


    private void appendToUI(final TextView view, final String string) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                view.setText(string);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {

        // other 'case' lines to check for other
        // permissions this app might request

        switch (requestCode) {
            case 4: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length < 0) {
                    // Your app will not have this permission. Turn off all functions
                    // that require this permission or it will force close like your
                    Toast.makeText(MainActivity.this, "Sensor data will not be saved locally", Toast.LENGTH_SHORT).show();


                }
            }
        }
    }

}
