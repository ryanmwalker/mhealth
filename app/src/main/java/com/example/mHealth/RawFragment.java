package com.example.mHealth;


import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class RawFragment extends Fragment implements SensorEventListener {
    final short POLL_FREQUENCY = 200; //in milliseconds
    private long lastUpdate = -1;
    private SensorManager sensorManager;
    Sensor sensor;
    private Sensor accelerometer;
    private Sensor gyroscope;
    private Sensor gravity;
    private Sensor magnetic;
    private Sensor rotation;

    MainActivity mainActivity;
    TextView accX;
    TextView accY;
    TextView accZ;
    TextView gyroX;
    TextView gyroY;
    TextView gyroZ;
    TextView rot1;
    TextView rot2;
    TextView rot3;
    TextView rot4;
    TextView rot5;
    TextView rot6;
    TextView rot7;
    TextView rot8;
    TextView rot9;
    TextView rotX;
    TextView rotY;
    TextView rotZ;

    float[] accelerometerMatrix = new float[3];
    float[] gyroscopeMatrix = new float[3];
    float[] gravityMatrix = new float[3];
    float[] magneticMatrix = new float[3];
    float[] rotationMatrix = new float[9];
    float[] rotationVectorMatrix = new float [3];

    public RawFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_raw, container, false);

        //Set the nav drawer item highlight
        mainActivity = (MainActivity)getActivity();
        mainActivity.navigationView.setCheckedItem(R.id.nav_raw);

        //Set actionbar title
        mainActivity.setTitle("Sensor Metrics");

        //Sensor manager
        sensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(MainActivity.TYPE_ACCELEROMETER);
        gyroscope = sensorManager.getDefaultSensor(MainActivity.TYPE_GYROSCOPE);
        gravity = sensorManager.getDefaultSensor(MainActivity.TYPE_GRAVITY);
        magnetic = sensorManager.getDefaultSensor(MainActivity.TYPE_MAGNETIC);
        rotation = sensorManager.getDefaultSensor(MainActivity.TYPE_ROTATION);
        //Get text fields
        accX = view.findViewById(R.id.raw_value_acc_x);
        accY = view.findViewById(R.id.raw_value_acc_y);
        accZ = view.findViewById(R.id.raw_value_acc_z);
        gyroX = view.findViewById(R.id.raw_value_gyro_x);
        gyroY = view.findViewById(R.id.raw_value_gyro_y);
        gyroZ = view.findViewById(R.id.raw_value_gyro_z);
        rot1 = view.findViewById(R.id.raw_value_rot_1);
        rot2 = view.findViewById(R.id.raw_value_rot_2);
        rot3 = view.findViewById(R.id.raw_value_rot_3);
        rot4 = view.findViewById(R.id.raw_value_rot_4);
        rot5 = view.findViewById(R.id.raw_value_rot_5);
        rot6 = view.findViewById(R.id.raw_value_rot_6);
        rot7 = view.findViewById(R.id.raw_value_rot_7);
        rot8 = view.findViewById(R.id.raw_value_rot_8);
        rot9 = view.findViewById(R.id.raw_value_rot_9);
        rotX = view.findViewById(R.id.raw_value_rot_x);
        rotY = view.findViewById(R.id.raw_value_rot_y);
        rotZ = view.findViewById(R.id.raw_value_rot_z);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, gravity, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, magnetic, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, rotation, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        sensor = event.sensor;

        int i = sensor.getType();
        if (i == MainActivity.TYPE_ACCELEROMETER) {
            accelerometerMatrix = event.values;
        } else if (i == MainActivity.TYPE_GYROSCOPE) {
            gyroscopeMatrix = event.values;
        } else if (i == MainActivity.TYPE_GRAVITY) {
            gravityMatrix = event.values;
        } else if (i == MainActivity.TYPE_MAGNETIC) {
            magneticMatrix = event.values;
        } else if (i == MainActivity.TYPE_ROTATION) {
            rotationVectorMatrix = event.values;
        }

        long curTime = System.currentTimeMillis();
        long diffTime = (curTime - lastUpdate);

        // only allow one update every POLL_FREQUENCY.
        if(diffTime > POLL_FREQUENCY) {
            lastUpdate = curTime;

            SensorManager.getRotationMatrix(rotationMatrix, null, gravityMatrix, magneticMatrix);

            accX.setText(String.format("%.2f", accelerometerMatrix[0]));
            accY.setText(String.format("%.2f", accelerometerMatrix[1]));
            accZ.setText(String.format("%.2f", accelerometerMatrix[2]));
            gyroX.setText(String.format("%.2f", gyroscopeMatrix[0]));
            gyroY.setText(String.format("%.2f", gyroscopeMatrix[1]));
            gyroZ.setText(String.format("%.2f", gyroscopeMatrix[2]));
            rot1.setText(String.format("%.2f", rotationMatrix[0]));
            rot2.setText(String.format("%.2f", rotationMatrix[1]));
            rot3.setText(String.format("%.2f", rotationMatrix[2]));
            rot4.setText(String.format("%.2f", rotationMatrix[3]));
            rot5.setText(String.format("%.2f", rotationMatrix[4]));
            rot6.setText(String.format("%.2f", rotationMatrix[5]));
            rot7.setText(String.format("%.2f", rotationMatrix[6]));
            rot8.setText(String.format("%.2f", rotationMatrix[7]));
            rot9.setText(String.format("%.2f", rotationMatrix[8]));
            rotX.setText(String.format("%.2f", rotationVectorMatrix[0]));
            rotY.setText(String.format("%.2f", rotationVectorMatrix[1]));
            rotZ.setText(String.format("%.2f", rotationVectorMatrix[2]));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //safe not to implement
    }
}
