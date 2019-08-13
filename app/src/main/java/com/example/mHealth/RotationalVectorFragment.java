package com.example.mHealth;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.androidplot.xy.XYPlot;

public class RotationalVectorFragment extends Fragment implements SensorEventListener {

    SensorManager sensorManager;
    Sensor sensor;
    Sensor rotationVector;
    Handler handler;
    Runnable runnable;
    TextView textViewXAxis;
    TextView textViewYAxis;
    TextView textViewZAxis;

    float[] rotData;
    float[] plotData;

    XYPlot plot;
    DynamicLinePlot dynamicPlot;

    public RotationalVectorFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_rotation_vector, container, false);

        //Set the nav drawer item highlight
        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.navigationView.setCheckedItem(R.id.nav_rotation_vector);

        //Set actionbar title
        mainActivity.setTitle("Rotation Vector");

        //Get text views
        textViewXAxis = view.findViewById(R.id.value_x_axis);
        textViewYAxis = view.findViewById(R.id.value_y_axis);
        textViewZAxis = view.findViewById(R.id.value_z_axis);

        //Sensor manager
        sensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
        rotationVector = sensorManager.getDefaultSensor(MainActivity.TYPE_ROTATION);

        //Create graph
        rotData = new float[3];
        plotData = new float[3];

        plot = view.findViewById(R.id.plot_sensor);
        dynamicPlot = new DynamicLinePlot(plot, getContext(), "");
        dynamicPlot.setMaxRange(1);
        dynamicPlot.setMinRange(-1);
        dynamicPlot.addSeriesPlot("X", 0, ContextCompat.getColor(getContext(), R.color.red));
        dynamicPlot.addSeriesPlot("Y", 1, ContextCompat.getColor(getContext(), R.color.green));
        dynamicPlot.addSeriesPlot("Z", 2, ContextCompat.getColor(getContext(), R.color.blue));

        //Handler for graph plotting on background thread
        handler = new Handler();

        //Runnable for background plotting
        runnable = new Runnable()
        {
            @Override
            public void run() {
                handler.postDelayed(this, 10);
                plotData();
                updateRotationText();
            }
        };

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        sensorManager.registerListener(this, rotationVector, SensorManager.SENSOR_DELAY_FASTEST);
        handler.post(runnable);
    }

    @Override
    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        handler.removeCallbacks(runnable);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        sensor = event.sensor;

        int i = sensor.getType();


        if (i == MainActivity.TYPE_ROTATION) {
            rotData = event.values;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //Safe not to implement
    }


    private void plotData(){
        plotData = rotData;

        dynamicPlot.setData(plotData[0], 0);
        dynamicPlot.setData(plotData[1], 1);
        dynamicPlot.setData(plotData[2], 2);

        dynamicPlot.draw();
    }

    protected void updateRotationText(){
        // Update the acceleration data
        textViewXAxis.setText(String.format("%.2f", plotData[0]));
        textViewYAxis.setText(String.format("%.2f", plotData[1]));
        textViewZAxis.setText(String.format("%.2f", plotData[2]));
    }

}
