package com.example.mHealth;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;

import java.io.File;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final String TAG = "MainActivity";

    NavigationView navigationView;
    Toolbar toolbar;
    DrawerLayout drawer;
    LayoutInflater inflater;
    ActionBarDrawerToggle hamburger;
    FragmentManager fragmentManager;
    SensorManager mSensorManager;
    DBHelper dbHelper;
    public Logger logger;

    //App flags
    public static Boolean dataRecordStarted;
    public static Boolean dataRecordCompleted;
    public static Boolean subCreated;

    //Set sensors used in app
    public final static short TYPE_ACCELEROMETER = Sensor.TYPE_ACCELEROMETER;
    public final static short TYPE_GYROSCOPE = Sensor.TYPE_GYROSCOPE;
    public final static short TYPE_GRAVITY = Sensor.TYPE_GRAVITY;
    public final static short TYPE_MAGNETIC = Sensor.TYPE_MAGNETIC_FIELD;

    final String[] PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WAKE_LOCK
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Check if this is a fresh app launch, or a launcher click after app minimize
        if (!isTaskRoot()
                && getIntent().hasCategory(Intent.CATEGORY_LAUNCHER)
                && getIntent().getAction() != null
                && getIntent().getAction().equals(Intent.ACTION_MAIN)) {
            finish();
            return;
        }

        Log.d(TAG, "onCreate called");
        setContentView(R.layout.activity_main);
        inflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        //Create the logger
        String pathToExternalStorage = Environment.getExternalStorageDirectory().toString();
        File logFileDir = new File(pathToExternalStorage, "/mHealth/");
        logger = new Logger(logFileDir);

        //Get fragment manager
        fragmentManager = getSupportFragmentManager();

        //Set the toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Set navigation drawer
        drawer = findViewById(R.id.drawer_layout);
        hamburger = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(hamburger);
        hamburger.syncState();
        hamburger.setDrawerIndicatorEnabled(false);
        //Disable menu items that should display when a user exists
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().findItem(R.id.nav_start).setEnabled(false).setVisible(false);
        navigationView.getMenu().findItem(R.id.nav_save).setEnabled(true);

        //First fragment is blank. So set initial fragment to 'new' instead
        addFragment(new NewFragment(), true);
        toolbar.setTitle("New Participant");

        //Create dbHelper
        dbHelper = DBHelper.getInstance(this);

        //Set app flags on create/recreate
        dataRecordStarted = false;
        dataRecordCompleted = false;
        subCreated = false;

        //If any of the permission have not been granted, request
        if (hasPermission(PERMISSIONS[0]) || hasPermission(PERMISSIONS[1]) ||
                hasPermission(PERMISSIONS[2])) {

            ActivityCompat.requestPermissions(this, PERMISSIONS, 10);
        }
    }

    public boolean hasPermission(String permission){
        return (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        //If at least one permission has been denied, show snackbar
        if (grantResults.length == 0 || !arePermissionsGranted(grantResults)){
            Snackbar.make(findViewById(android.R.id.content), R.string.permission_explain, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.snackbar_request_permission, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS, 10);
                        }
                    })
                    .show();
        }
    }

    public boolean arePermissionsGranted(int[] grantResults) {
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy called");
        dbHelper.closeDB();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            //If we just logged in, prevent back button
            //Allowing back button here will destroy the app, as MainActivity is the only activity
            if (fragmentManager.getBackStackEntryCount() != 0) {
                fragmentManager.popBackStack();
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_new:
                //If a subject is created, go to Subject Info, otherwise, go to New fragment
                if (!subCreated) {
                    addFragment(new NewFragment(), true);
                } else {
                    addFragment(new SubjectInfoFragment(), true);
                }
                break;
            case R.id.nav_start:
                addFragment(new StartFragment(), true);
                break;
            case R.id.nav_save:
                addFragment(new SaveFragment(), true);
                break;
            case R.id.nav_raw:
                addFragment(new RawFragment(), true);
                break;
            case R.id.nav_accelerometer:
                addFragment(new AccelerometerFragment(), true);
                break;
            case R.id.nav_gyroscope:
                addFragment(new GyroscopeFragment(), true);
                break;
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void addFragment(Fragment fragment, Boolean addToBackStack) {
        if (fragment != null) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            //Check getFragments() == null to prevent the initial blank
            //fragment (before 'New' fragment is displayed) from being added to the backstack
            fragmentManager.getFragments();
            if (!addToBackStack) {
                fragmentTransaction.replace(R.id.fragment_container, fragment, fragment.getClass().getSimpleName())
                        .commit();
            } else {
                fragmentTransaction.replace(R.id.fragment_container, fragment, fragment.getClass().getSimpleName())
                        .addToBackStack(null)
                        .commit();
            }
        }
    }

    public String getSensorAvailable(short sensor_type){
        Sensor curSensor = mSensorManager.getDefaultSensor(sensor_type);
        if (curSensor != null){
            return("Yes  " + "(" + curSensor.getVendor() + ")");
        } else {
            return("No");
        }
    }
}
