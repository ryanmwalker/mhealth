package com.example.mHealth;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NewFragment extends Fragment implements AdapterView.OnItemSelectedListener, View.OnClickListener {

    public static final String TAG = "NewFragment";

    MainActivity mainActivity;
    CoordinatorLayout coordinatorLayout;
    DBHelper dbHelper;

    TextInputLayout raWrapper;
    TextInputLayout subIDWrapper;
    CheckBox        painWrapper;
    CheckBox        medWrapper;
    CheckBox        walkWrapper;
    String sex;
    RadioGroup sexGroup;
    Button loginButton;

    public NewFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        coordinatorLayout = (CoordinatorLayout) getActivity().findViewById(R.id.coordinator_layout);
        View view = inflater.inflate(R.layout.fragment_new, container, false);

        //Set the nav drawer item highlight
        mainActivity = (MainActivity) getActivity();
        mainActivity.navigationView.setCheckedItem(R.id.nav_new);

        //Set actionbar title
        mainActivity.setTitle("New Subject");

        //Get dbHelper
        dbHelper = DBHelper.getInstance(getActivity());
        //Get all text fields
        subIDWrapper = (TextInputLayout) view.findViewById(R.id.input_id_wrapper);
        painWrapper = (CheckBox) view.findViewById(R.id.input_pain_wrapper);
        medWrapper = (CheckBox) view.findViewById(R.id.input_med_wrapper);
        walkWrapper = (CheckBox) view.findViewById(R.id.input_walk_wrapper);
        //Listener for create button
        loginButton = (Button) view.findViewById(R.id.input_submit);
        loginButton.setOnClickListener(this);

        // Inflate the layout for this fragment
        return view;
    }

    //Height unit spinner item selection
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
    }

    //Height unit spinner item selection
    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        //Do nothing
    }

    //Create/submit button click
    @Override
    public void onClick(View v) {
        //Get input values
        String subID = subIDWrapper.getEditText().getText().toString();
        String pain = String.valueOf(painWrapper.isChecked());
        String medication = String.valueOf(medWrapper.isChecked());
        String walking = String.valueOf(walkWrapper.isChecked());
        TextView sexLabel = (TextView) mainActivity.findViewById(R.id.input_label_sex);
        sexGroup = (RadioGroup) mainActivity.findViewById(R.id.input_sex);
        int sexID = sexGroup.getCheckedRadioButtonId();

        if (sexID != -1) {
            View radioButton = sexGroup.findViewById(sexID);
            int radioId = sexGroup.indexOfChild(radioButton);
            RadioButton btn = (RadioButton) sexGroup.getChildAt(radioId);
            sex = (String) btn.getText();
            sexLabel.setTextColor(ContextCompat.getColor(getContext(), R.color.colorSecondaryText));
        }

        //If all the validation passes, submit the form. Else, show errors
        if (!isEmpty(subID) & sexID != -1) {
            //Turn all errors off
            subIDWrapper.setError(null);
            //check if subject already exists in main persistent subject table
            if(!dbHelper.checkSubjectExists(Short.parseShort(subID))){
                //subject doesn't already exist

                //Insert subject into TEMP subject table
                MainActivity.subCreated = true;

                dbHelper.insertSubjectTemp(
                        Short.parseShort(subID),
                        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date()),
                        pain,
                        medication,
                        walking,
                        sex,
                        0
                );

                //Hide the keyboard on click
                showKeyboard(false, mainActivity);

                //Enable additional menu items/fragments for recording and saving sensor data
                mainActivity.navigationView.getMenu().findItem(R.id.nav_start).setEnabled(true);
                mainActivity.navigationView.getMenu().findItem(R.id.nav_save).setEnabled(true);
                mainActivity.navigationView.getMenu().findItem(R.id.nav_new).setTitle("Subject Info");

                Snackbar.make(coordinatorLayout, "Subject created", Snackbar.LENGTH_SHORT).show();
                mainActivity.logger.i(getActivity(),TAG, "Subject #" + subID + " created");

                //Change fragment to subject info screen. Do not add this fragment to the backstack
                mainActivity.addFragment(new SubjectInfoFragment(), false);
            } else {
                //subject exists. Set focus on subject number field
                Snackbar.make(coordinatorLayout,"Subject number already exists...", Snackbar.LENGTH_SHORT).show();
                subIDWrapper.requestFocus();
            }
        } else {
            if (isEmpty(subID)) {
                subIDWrapper.setError("Subject ID required");
            } else {
                subIDWrapper.setError(null);
            }

            //If no radio button has been selected
            if (sexID == -1) {
                sexLabel.setTextColor(Color.RED);
            }
        }
    }

    //Check if a string is empty
    public boolean isEmpty(String string) {
        return string.equals("");
    }

    public void showKeyboard(Boolean show, MainActivity mainActivity){
        InputMethodManager imm = (InputMethodManager) mainActivity.getSystemService(Context.INPUT_METHOD_SERVICE);

        if (show){
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        } else {
            // check if no view has focus before hiding keyboard
            View v = mainActivity.getCurrentFocus();
            if (v != null){
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        }
    }
}
