package com.example.mHealth;

import android.content.Intent;
import android.database.SQLException;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Objects;

public class StartFragment extends Fragment implements View.OnClickListener {

    public static final String TAG = "StartFragment";


    Button startButton;
    CoordinatorLayout coordinatorLayout;
    DBHelper dbHelper;            //World coordinate system transformation - do in post processing instead

    TextView recordProgressMessage;
    MainActivity mainActivity;
    ImageView mImageViewWalk;
    ImageView mImageViewCheck;
    AnimationDrawable walkAnimation;
    public StartFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_start, container, false);

        coordinatorLayout = Objects.requireNonNull(getActivity()).findViewById(R.id.coordinator_layout);

        //Set the nav drawer item highlight
        mainActivity = (MainActivity) getActivity();
        mainActivity.navigationView.setCheckedItem(R.id.nav_start);

        //DBHelper    float[] results = new float[1];

        dbHelper = DBHelper.getInstance(getActivity());

        //Set actionbar title
        mainActivity.setTitle(dbHelper.getTempSubInfo("first") + " " + dbHelper.getTempSubInfo("last"));

        //Get form text view element and set
        recordProgressMessage = view.findViewById(R.id.start_recording_progress);
        recordProgressMessage.setText(R.string.start_recording_ready);

        //Set onclick listener for save button
        startButton = view.findViewById(R.id.startButton);
        startButton.setOnClickListener(this);

        //Set button state depending on whether recording has been started and/or stopped
        if (MainActivity.dataRecordStarted) {
            if (MainActivity.dataRecordCompleted) {
                //started and completed: disable button completely
                startButton.setEnabled(false);
                startButton.setVisibility(View.GONE);
                recordProgressMessage.setText(R.string.start_recording_complete);
            } else {
                //started and not completed: enable STOP button
                startButton.setEnabled(true);
                startButton.setText(R.string.start_button_label_stop);
                startButton.setBackgroundColor(ContextCompat.getColor(Objects.requireNonNull(getContext()), R.color.graphX));
            }
        } else {
            //Haven't started: enable START button
            startButton.setEnabled(true);
            startButton.setText(R.string.start_button_label_start);
            startButton.setBackgroundColor(ContextCompat.getColor(Objects.requireNonNull(getContext()), R.color.graphY));
        }

        mImageViewWalk = view.findViewById(R.id.walk);
        walkAnimation = (AnimationDrawable) mImageViewWalk.getBackground();
        mImageViewCheck = view.findViewById(R.id.check);

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onClick(View v) {

        mImageViewWalk.setVisibility(View.VISIBLE);
        walkAnimation.start();

        if (!MainActivity.dataRecordStarted) {
            try {
                //Disable the drawer swipes while recording
                mainActivity.drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

                //Set recording progress message
                recordProgressMessage.setText(R.string.start_recording_progress);
                MainActivity.dataRecordStarted = true;
                startButton.setText(R.string.start_button_label_stop);
                startButton.setBackgroundColor(ContextCompat.getColor(Objects.requireNonNull(getContext()), R.color.graphX));

                //Insert start time of recording
                dbHelper.setStartTime(dbHelper.getTempSubInfo("subID"), System.currentTimeMillis());

                //Start the service
                Intent startService = new Intent(mainActivity, SensorService.class);
                getContext().startService(startService);

                Snackbar.make(coordinatorLayout, R.string.start_recording, Snackbar.LENGTH_SHORT).show();

            } catch (SQLException e) {
                mainActivity.logger.e(getActivity(), TAG, "SQL error insertSubject()", e);
            }
        } else {
            MainActivity.dataRecordCompleted = true;
            walkAnimation.stop();
            mImageViewWalk.setVisibility(View.INVISIBLE);
            mImageViewCheck.setVisibility(View.VISIBLE);
            startButton.setEnabled(false);
            startButton.setVisibility(View.GONE);
            recordProgressMessage.setText(R.string.start_recording_complete);

            //Stop the service
            mainActivity.stopService(new Intent(mainActivity, SensorService.class));

            //Re-enable the hamburger, and swipes, after recording
            mainActivity.drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);

            //Show snackbar message for recording complete
            Snackbar.make(coordinatorLayout, R.string.start_recording_complete, Snackbar.LENGTH_SHORT).show();//Change fragment to subject info screen. Do not add this fragment to the backstack
        }
    }
}
