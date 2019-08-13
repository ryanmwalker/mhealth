package com.example.mHealth;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import java.util.Objects;

import static java.lang.System.currentTimeMillis;

public class NewFragment extends Fragment implements AdapterView.OnItemSelectedListener, View.OnClickListener {

    public static final String TAG = "NewFragment";

    MainActivity mainActivity;
    CoordinatorLayout coordinatorLayout;
    DBHelper dbHelper;


    TextInputLayout idWrapper;
    TextInputLayout firstWrapper;
    TextInputLayout lastWrapper;
    TextInputLayout feetWrapper;
    TextInputLayout inchesWrapper;
    TextInputLayout poundsWrapper;
    CheckBox        painWrapper;
    CheckBox        medWrapper;
    CheckBox        walkWrapper;
    CheckBox        tbiWrapper;
    CheckBox        asianWrapper;
    CheckBox        blackWrapper;
    CheckBox        whiteWrapper;
    CheckBox        otherWrapper;
    CheckBox        sixWrapper;
    CheckBox        multWrapper;
    CheckBox        hispanicWrapper;
    String gender = null;
    RadioGroup genderGroup;
    Button loginButton;


    public NewFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        coordinatorLayout = Objects.requireNonNull(getActivity()).findViewById(R.id.coordinator_layout);
        View view = inflater.inflate(R.layout.fragment_new, container, false);

        //Set the nav drawer item highlight
        mainActivity = (MainActivity) getActivity();
        mainActivity.navigationView.setCheckedItem(R.id.nav_new);
        //Set actionbar title
        mainActivity.setTitle("New Participant");

        //Get dbHelper
        dbHelper = DBHelper.getInstance(getActivity());
        //Get all text fields

        idWrapper = view.findViewById(R.id.input_id_wrapper);
        firstWrapper = view.findViewById(R.id.input_first_wrapper);
        lastWrapper = view.findViewById(R.id.input_last_wrapper);
        painWrapper = view.findViewById(R.id.input_pain_wrapper);
        medWrapper = view.findViewById(R.id.input_med_wrapper);
        walkWrapper = view.findViewById(R.id.input_walk_wrapper);
        tbiWrapper = view.findViewById(R.id.input_tbi_wrapper);
        sixWrapper = view.findViewById(R.id.input_sixmo_wrapper);
        multWrapper = view.findViewById(R.id.input_multiple_wrapper);
        asianWrapper = view.findViewById(R.id.input_asian_wrapper);
        blackWrapper = view.findViewById(R.id.input_black_wrapper);
        whiteWrapper = view.findViewById(R.id.input_white_wrapper);
        hispanicWrapper = view.findViewById(R.id.input_hispanic_wrapper);
        otherWrapper = view.findViewById(R.id.input_other_wrapper);
        feetWrapper = view.findViewById(R.id.input_feet_wrapper);
        inchesWrapper = view.findViewById(R.id.input_inches_wrapper);
        poundsWrapper = view.findViewById(R.id.input_pounds_wrapper);
        //Listener for create button
        loginButton = view.findViewById(R.id.input_submit);
        loginButton.setOnClickListener(this);

        //listener for tbi, prompts "if yes" questions
        tbiWrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tbiWrapper.isChecked()) {
                    sixWrapper.setVisibility(View.VISIBLE);
                    multWrapper.setVisibility(View.VISIBLE);
                } else {
                    sixWrapper.setVisibility(View.GONE);
                    multWrapper.setVisibility(View.GONE);
                }
            }
        });

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
        long ts = currentTimeMillis()/1000;
        String id = Objects.requireNonNull(idWrapper.getEditText()).getText().toString();
        String subID = Objects.requireNonNull(lastWrapper.getEditText()).getText().toString() + ts;
        String first = Objects.requireNonNull(firstWrapper.getEditText()).getText().toString();
        String last = lastWrapper.getEditText().getText().toString();
        String pain = String.valueOf(painWrapper.isChecked());
        String medication = String.valueOf(medWrapper.isChecked());
        String walking = String.valueOf(walkWrapper.isChecked());
        String tbi = String.valueOf(tbiWrapper.isChecked());
        String sixmonth = String.valueOf(sixWrapper.isChecked());
        String multiple = String.valueOf(multWrapper.isChecked());
        String asian = String.valueOf(asianWrapper.isChecked());
        String black = String.valueOf(blackWrapper.isChecked());
        String white = String.valueOf(whiteWrapper.isChecked());
        String other = String.valueOf(otherWrapper.isChecked());
        String hispanic = String.valueOf(hispanicWrapper.isChecked());
        TextView sexLabel = mainActivity.findViewById(R.id.input_label_gender);
        genderGroup = mainActivity.findViewById(R.id.input_gender);

        int sexID = genderGroup.getCheckedRadioButtonId();

        String height = Objects.requireNonNull(feetWrapper.getEditText()).getText().toString() + "'" + Objects.requireNonNull(inchesWrapper.getEditText()).getText().toString() + '"';

        if (height.equals("'" + '"')){
            height = null;
        }


        String weight = Objects.requireNonNull(poundsWrapper.getEditText()).getText().toString();

        if (sexID != -1) {
            View radioButton = genderGroup.findViewById(sexID);
            int radioId = genderGroup.indexOfChild(radioButton);
            RadioButton btn = (RadioButton) genderGroup.getChildAt(radioId);
            gender = (String) btn.getText();
            sexLabel.setTextColor(ContextCompat.getColor(Objects.requireNonNull(getContext()), R.color.colorSecondaryText));
        }

        //If all the validation passes, submit the form. Else, BOOLEANshow errors
        if (!isEmpty(first) & !isEmpty(last)) {
            //Turn all errors off
            firstWrapper.setError(null);
            lastWrapper.setError(null);
            //check if subject already exists in main persistent subject table
            if(!dbHelper.checkSubjectExists(subID)){
                //subject doesn't already exist

                //Insert subject into TEMP subject table
                MainActivity.subCreated = true;

                dbHelper.insertSubjectTemp(
                        id,
                        subID,
                        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date()),
                        first,
                        last,
                        pain,
                        medication,
                        tbi,
                        walking,
                        gender,
                        sixmonth,
                        multiple,
                        asian,
                        black,
                        white,
                        other,
                        hispanic,
                        weight,
                        height,
                        "0",
                        "0"
                );

                //Hide the keyboard on clickDBHelper dbHelper;
                showKeyboard(false, mainActivity);

                //Enable additional menu items/fragments for recording and saving sensor data
                mainActivity.navigationView.getMenu().findItem(R.id.nav_start).setEnabled(true);
                mainActivity.navigationView.getMenu().findItem(R.id.nav_save).setEnabled(true);
                mainActivity.navigationView.getMenu().findItem(R.id.nav_new).setTitle("Participant Info");


                Snackbar.make(coordinatorLayout, "Participant created", Snackbar.LENGTH_SHORT).show();
                mainActivity.logger.i(getActivity(),TAG, "Participant #" + subID + " created");

                //Change fragment to subject info screen. Do not add this fragment to the backstack
                mainActivity.addFragment(new StartFragment(), false);
                mainActivity.navigationView.getMenu().findItem(R.id.nav_new).setIcon(R.drawable.ic_person_24dp);
            } else {
                //subject exists. Set focus on subject number field
                Snackbar.make(coordinatorLayout,"Participant already exists...", Snackbar.LENGTH_SHORT).show();
                firstWrapper.requestFocus();
            }
        } else {
            if (isEmpty(first) || isEmpty(last)) {
                firstWrapper.setError("First name required");
                lastWrapper.setError("Last name required");
            } else {
                firstWrapper.setError(null);
                lastWrapper.setError(null);
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
