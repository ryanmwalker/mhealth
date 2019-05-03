package com.example.mHealth;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import au.com.bytecode.opencsv.CSVWriter;

public class DBHelper extends SQLiteOpenHelper {
    public static final String TAG = "DBHelper";

    SQLiteDatabase db;
    CSVWriter csvWrite;
    Cursor curCSV;
    static Handler messageHandler;
    private static DBHelper sInstance;

    //Constants for db name and version
    public static final String DATABASE_NAME = "mHealth.db";
    private static final int DATABASE_VERSION = 1;
    public String databasePath = "";

    //Constants for identifying subject table and fields
    public static final String SUBJECTS_TABLE_NAME = "subjects";
    public static final String SUBJECTS_TABLE_NAME_TEMP = "subjects_temp";
    public static final String SUBJECTS_ID = "id";
    public static final String SUBJECTS_KEY = "subID";
    public static final String SUBJECTS_FIRST_NAME = "first";
    public static final String SUBJECTS_LAST_NAME = "last";
    public static final String SUBJECTS_PAIN = "pain";
    public static final String SUBJECTS_MEDICATION = "medication";
    public static final String SUBJECTS_WALKING = "walking";
    public static final String SUBJECTS_TBI = "tbi";
    public static final String SUBJECTS_SEX = "sex";
    public static final String SUBJECTS_MULTIPLE = "multiple_tbi";
    public static final String SUBJECTS_MONTHS = "six_months";
    public static final String SUBJECTS_ASIAN = "asian";
    public static final String SUBJECTS_BLACK = "black";
    public static final String SUBJECTS_WHITE = "white";
    public static final String SUBJECTS_OTHER = "other";
    public static final String SUBJECTS_HISPANIC = "hispanic";
    public static final String SUBJECTS_WEIGHT = "weight";
    public static final String SUBJECTS_HEIGHT = "height";
    public static final String SUBJECTS_DATE = "date";
    public static final String SUBJECTS_START_TIME = "starttime";
    public static final String SUBJECTS_VALIDATION = "valid";
    //Subject table structure
    private static final String SUBJECTS_TABLE_STRUCTURE = "(" +
                    SUBJECTS_ID + " TEXT, " +
                    SUBJECTS_KEY + " TEXT PRIMARY KEY, " +
                    SUBJECTS_DATE + " TEXT default CURRENT_TIMESTAMP, " +
                    SUBJECTS_FIRST_NAME + " TEXT, " +
                    SUBJECTS_LAST_NAME + " TEXT, " +
                    SUBJECTS_PAIN + " TEXT, " +
                    SUBJECTS_MEDICATION + " TEXT, " +
                    SUBJECTS_WALKING + " TEXT, " +
                    SUBJECTS_TBI + " TEXT, " +
                    SUBJECTS_MONTHS + " TEXT, " +
                    SUBJECTS_MULTIPLE + " TEXT, " +
                    SUBJECTS_SEX + " TEXT, " +
                    SUBJECTS_ASIAN + " TEXT, " +
                    SUBJECTS_BLACK + " TEXT, " +
                    SUBJECTS_WHITE + " TEXT, " +
                    SUBJECTS_OTHER + " TEXT, " +
                    SUBJECTS_HISPANIC + " TEXT, " +
                    SUBJECTS_WEIGHT + " TEXT, " +
                    SUBJECTS_HEIGHT + " TEXT, " +
                    SUBJECTS_START_TIME + " INTEGER, " +
                    SUBJECTS_VALIDATION + " TEXT " +
                    ")";

    //SQL to create subject table
    private static final String SUBJECTS_TABLE_CREATE =
            "CREATE TABLE " + SUBJECTS_TABLE_NAME + SUBJECTS_TABLE_STRUCTURE;

    //SQL to create TEMP subject table
    private static final String SUBJECTS_TABLE_CREATE_TEMP =
            "CREATE TEMP TABLE subjects_temp" + SUBJECTS_TABLE_STRUCTURE;

    //Constants for identifying data table and fields
    public static final String DATA_TABLE_NAME = "data";
    public static final String DATA_TABLE_NAME_TEMP = "data_temp";
    public static final String DATA_SUBJECT = "id";
    public static final String DATA_TIME = "time";
    public static final String DATA_ACCX = "accX";
    public static final String DATA_ACCY = "accY";
    public static final String DATA_ACCZ = "accZ";
    public static final String DATA_GYROX = "gyroX";
    public static final String DATA_GYROY = "gyroY";
    public static final String DATA_GYROZ = "gyroZ";
    public static final String DATA_ROT1 = "rot1";
    public static final String DATA_ROT2 = "rot2";
    public static final String DATA_ROT3 = "rot3";
    public static final String DATA_ROT4 = "rot4";
    public static final String DATA_ROT5 = "rot5";
    public static final String DATA_ROT6 = "rot6";
    public static final String DATA_ROT7 = "rot7";
    public static final String DATA_ROT8 = "rot8";
    public static final String DATA_ROT9 = "rot9";

    //Data table structure
    private static final String DATA_TABLE_STRUCTURE =
            " (" + DATA_SUBJECT + " INTEGER, " +
                    DATA_TIME + " INTEGER, " +
                    DATA_ACCX + " REAL, " +
                    DATA_ACCY + " REAL, " +
                    DATA_ACCZ + " REAL, " +
                    DATA_GYROX + " REAL, " +
                    DATA_GYROY + " REAL, " +
                    DATA_GYROZ + " REAL, " +
                    DATA_ROT1 + " REAL, " +
                    DATA_ROT2 + " REAL, " +
                    DATA_ROT3 + " REAL, " +
                    DATA_ROT4 + " REAL, " +
                    DATA_ROT5 + " REAL, " +
                    DATA_ROT6 + " REAL, " +
                    DATA_ROT7 + " REAL, " +
                    DATA_ROT8 + " REAL, " +
                    DATA_ROT9 + " REAL, " +
                    "FOREIGN KEY(" + DATA_SUBJECT + ") REFERENCES " + SUBJECTS_TABLE_NAME + "(" + SUBJECTS_KEY + ")" +
                    ")";

    //SQL to create data table
    private static final String DATA_TABLE_CREATE =
            "CREATE TABLE " + DATA_TABLE_NAME + DATA_TABLE_STRUCTURE;

    //SQL to create TEMP data table
    private static final String DATA_TABLE_CREATE_TEMP =
            "CREATE TEMP TABLE " + DATA_TABLE_NAME_TEMP + DATA_TABLE_STRUCTURE;

    /**
     The static getInstance() method ensures that only one DatabaseHelper will ever exist at any given time.
     If the sInstance object has not been initialized, one will be created.
     If one has already been created then it will simply be returned.
     You should not initialize your helper object using with new DatabaseHelper(context)!
     Instead, always use DatabaseHelper.getInstance(context), as it guarantees that only
     one database helper will exist across the entire application’s lifecycle.
     */
    public static synchronized DBHelper getInstance(Context context) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new DBHelper(context.getApplicationContext());
            Log.d(TAG, "New DBHelper created");
        }

        return sInstance;
    }

    public static synchronized DBHelper getInstance(Context context, Handler handler) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new DBHelper(context.getApplicationContext());
            Log.d(TAG, "New DBHelper created");
        }

        messageHandler = handler;
        return sInstance;
    }

    /**
     * Constructor should be private to prevent direct instantiation.
     * make call to static method "getInstance()" instead.
     */
    private DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        databasePath = context.getDatabasePath(DATABASE_NAME).getPath();
        db = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "onCreate called");

        //Create persistent tables
        db.execSQL(SUBJECTS_TABLE_CREATE);
        db.execSQL(DATA_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "onUpgrade called");

        //Drop persistent tables
        db.execSQL("DROP TABLE IF EXISTS " + SUBJECTS_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DATA_TABLE_NAME);

        //Recreate tables
        onCreate(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        createTempTables(db);
    }

    public void closeDB(){
        if (db != null){
            Log.d(TAG, "closeDB: closing db");
            db.close();
            db = null;
        }

        if (sInstance != null){
            Log.d(TAG, "closeDB: closing DBHelper instance");
            sInstance.close();
            sInstance = null;
        }
    }

    public void createTempTables(SQLiteDatabase db){
        Log.d(TAG, "Creating Temp tables");
        //Create temp tables
        db.execSQL(SUBJECTS_TABLE_CREATE_TEMP);
        db.execSQL(DATA_TABLE_CREATE_TEMP);
    }

    public boolean checkSubjectExists(String subID) throws SQLException {
        //Check if subject exists in persistent subject table
        String query = "SELECT * FROM " + SUBJECTS_TABLE_NAME + " WHERE " + SUBJECTS_KEY + "=" + "'" + subID + "'";

        Cursor c = db.rawQuery(query, null);
        boolean exists = (c.getCount() > 0);
        c.close();

        return exists;
    }

    public boolean checkSubjectDataExists(String subID) throws SQLException {
        //Check if sensor data, for this subject, exists in the temp data table
        String query = "SELECT * FROM " + DATA_TABLE_NAME_TEMP + " WHERE " + DATA_SUBJECT + "=" + "'" + subID + "'";

        Cursor c = db.rawQuery(query, null);
        boolean exists = (c.getCount() > 0);
        c.close();

        return exists;
    }

    public void insertSubjectTemp(String id, String subID, String date, String first, String last, String pain,
                                  String medication, String tbi, String walking, String sex, String six_months, String multiple_tbi,
                                  String asian, String black, String white, String other,String hispanic, String weight,
                                  String height, long time) throws SQLException {

        ContentValues subjectData = new ContentValues();
        subjectData.put(SUBJECTS_ID, id);
        subjectData.put(SUBJECTS_KEY, subID);
        subjectData.put(SUBJECTS_DATE, date);
        subjectData.put(SUBJECTS_FIRST_NAME, first);
        subjectData.put(SUBJECTS_LAST_NAME, last);
        subjectData.put(SUBJECTS_PAIN, pain);
        subjectData.put(SUBJECTS_MEDICATION, medication);
        subjectData.put(SUBJECTS_WALKING, walking);
        subjectData.put(SUBJECTS_TBI, tbi);
        subjectData.put(SUBJECTS_SEX, sex);
        subjectData.put(SUBJECTS_MONTHS, six_months);
        subjectData.put(SUBJECTS_MULTIPLE, multiple_tbi);
        subjectData.put(SUBJECTS_ASIAN, asian);
        subjectData.put(SUBJECTS_BLACK, black);
        subjectData.put(SUBJECTS_WHITE, white);
        subjectData.put(SUBJECTS_OTHER, other);
        subjectData.put(SUBJECTS_HISPANIC, hispanic);
        subjectData.put(SUBJECTS_WEIGHT, weight);
        subjectData.put(SUBJECTS_HEIGHT, height);
        subjectData.put(SUBJECTS_START_TIME, time);
        subjectData.put(SUBJECTS_VALIDATION, "TRUE");
        db.insert(SUBJECTS_TABLE_NAME_TEMP, null, subjectData);
    }

    public void setStartTime(String subID, long time) throws SQLException {
        ContentValues cv = new ContentValues();
        cv.put(SUBJECTS_START_TIME, time);
        db.update(SUBJECTS_TABLE_NAME_TEMP, cv, SUBJECTS_KEY + " = " + "'" + subID + "'", null);
    }

    public String getTempSubInfo(String type) throws SQLException {
        String result = "";

        String query = "SELECT * FROM " + SUBJECTS_TABLE_NAME_TEMP;
        Cursor c = db.rawQuery(query, null);

        if(c.getCount() > 0){
            c.moveToFirst();
            switch(type){
                case "id":
                    result = c.getString(c.getColumnIndex(SUBJECTS_ID));
                    break;
                case "subID":
                    result = c.getString(c.getColumnIndex(SUBJECTS_KEY));
                    break;
                case "date":
                    result = c.getString(c.getColumnIndex(SUBJECTS_DATE));
                    break;
                case "first":
                    result = c.getString(c.getColumnIndex(SUBJECTS_FIRST_NAME));
                    break;
                case "last":
                    result = c.getString(c.getColumnIndex(SUBJECTS_LAST_NAME));
                    break;
                case "pain":
                    result = c.getString(c.getColumnIndex(SUBJECTS_PAIN));
                    break;
                case "medication":
                    result = c.getString(c.getColumnIndex(SUBJECTS_MEDICATION));
                    break;
                case "walking":
                    result = c.getString(c.getColumnIndex(SUBJECTS_WALKING));
                    break;
                case "tbi":
                    result = c.getString(c.getColumnIndex(SUBJECTS_TBI));
                    break;
                case "sex":
                    result = c.getString(c.getColumnIndex(SUBJECTS_SEX));
                    break;
                case "six_months":
                    result = c.getString(c.getColumnIndex(SUBJECTS_MONTHS));
                    break;
                case "multiple_tbi":
                    result = c.getString(c.getColumnIndex(SUBJECTS_MULTIPLE));
                    break;
                case "asian":
                    result = c.getString(c.getColumnIndex(SUBJECTS_ASIAN));
                    break;
                case "black":
                    result = c.getString(c.getColumnIndex(SUBJECTS_BLACK));
                    break;
                case "white":
                    result = c.getString(c.getColumnIndex(SUBJECTS_WHITE));
                    break;
                case "other":
                    result = c.getString(c.getColumnIndex(SUBJECTS_OTHER));
                    break;
                case "hispanic":
                    result = c.getString(c.getColumnIndex(SUBJECTS_HISPANIC));
                    break;
                case "weight":
                    result = c.getString(c.getColumnIndex(SUBJECTS_WEIGHT));
                    break;
                case "height":
                    result = c.getString(c.getColumnIndex(SUBJECTS_HEIGHT));
                    break;
                case "time":
                    result = c.getString(c.getColumnIndex(SUBJECTS_START_TIME));
                    break;
                case "valid":
                    result = c.getString(c.getColumnIndex(SUBJECTS_VALIDATION));
                    break;
            }
        }
        c.close();

        return result;
    }

    public void deleteSubjectTemp() throws SQLException {
        //Delete subject rows from temp subject and data tables. Table is not removed.
        db.delete(SUBJECTS_TABLE_NAME_TEMP, null, null);
        db.delete(DATA_TABLE_NAME_TEMP, null,null);
    }

    public void validateSubject(String subID, Boolean validate) throws SQLException {
        ContentValues cv = new ContentValues();
        cv.put(SUBJECTS_VALIDATION, validate.toString());
        db.update(SUBJECTS_TABLE_NAME_TEMP, cv, SUBJECTS_KEY + " = " + "'" + subID + "'", null);
    }

    public void resetSubjectData() throws SQLException {
        //Delete subject rows from temp data tables. Table is not removed.
        db.delete(DATA_TABLE_NAME_TEMP, null,null);
        MainActivity.dataRecordStarted = false;
    }

    public void insertDataTemp(String subID, long time,
                              float[] acc,
                              float[] gyro,
                              float[] rot) throws SQLException {

        ContentValues sensorValues = new ContentValues();

        sensorValues.put(DATA_SUBJECT, subID);
        sensorValues.put(DATA_TIME, time);
        sensorValues.put(DATA_ACCX, acc[0]);
        sensorValues.put(DATA_ACCY, acc[1]);
        sensorValues.put(DATA_ACCZ, acc[2]);
        sensorValues.put(DATA_GYROX, gyro[0]);
        sensorValues.put(DATA_GYROY, gyro[1]);
        sensorValues.put(DATA_GYROZ, gyro[2]);
        sensorValues.put(DATA_ROT1, rot[0]);
        sensorValues.put(DATA_ROT2, rot[1]);
        sensorValues.put(DATA_ROT3, rot[2]);
        sensorValues.put(DATA_ROT4, rot[3]);
        sensorValues.put(DATA_ROT5, rot[4]);
        sensorValues.put(DATA_ROT6, rot[5]);
        sensorValues.put(DATA_ROT7, rot[6]);
        sensorValues.put(DATA_ROT8, rot[7]);
        sensorValues.put(DATA_ROT9, rot[8]);

        db.insertOrThrow(DATA_TABLE_NAME_TEMP, null, sensorValues);
    }

    public void copyTempData() throws SQLException{
        String copySubjectSQL = "INSERT INTO " + SUBJECTS_TABLE_NAME + " SELECT * FROM " + SUBJECTS_TABLE_NAME_TEMP;
        db.execSQL(copySubjectSQL);

        String copyDataSQL = "INSERT INTO " + DATA_TABLE_NAME + " SELECT * FROM " + DATA_TABLE_NAME_TEMP;
        db.execSQL(copyDataSQL);
    }

    public void exportTrackingSheet(File outputFile) throws SQLException, IOException {

        csvWrite = new CSVWriter(new FileWriter(outputFile));

        curCSV = db.rawQuery("SELECT * FROM " + SUBJECTS_TABLE_NAME, null);

        csvWrite.writeNext(curCSV.getColumnNames());

        while (curCSV.moveToNext()) {

            String arrStr[] = {curCSV.getString(0), curCSV.getString(1), curCSV.getString(2),
                    curCSV.getString(3), curCSV.getString(4), curCSV.getString(5),
                    curCSV.getString(6), curCSV.getString(7), curCSV.getString(8),
                    curCSV.getString(9), curCSV.getString(10), curCSV.getString(11),
                    curCSV.getString(12), curCSV.getString(13), curCSV.getString(14),
                    curCSV.getString(15), curCSV.getString(16), curCSV.getString(17),
                    curCSV.getString(18), curCSV.getString(19), curCSV.getString(20)};

            csvWrite.writeNext(arrStr);
        }

        csvWrite.close();
        curCSV.close();
    }

    public void exportSubjectData(File outputFile, String subID) throws IOException, SQLException {

        csvWrite = new CSVWriter(new FileWriter(outputFile));

        curCSV = db.rawQuery("SELECT * FROM " + DATA_TABLE_NAME + " WHERE id = " + "'" + subID + "'", null);

        csvWrite.writeNext(curCSV.getColumnNames());

        Integer writeCounter = 0;
        Integer numRows = curCSV.getCount();

        while (curCSV.moveToNext()) {
            writeCounter++;

            String arrStr[] = {curCSV.getString(0), curCSV.getString(1), curCSV.getString(2),
                    curCSV.getString(3), curCSV.getString(4), curCSV.getString(5),
                    curCSV.getString(6), curCSV.getString(7), curCSV.getString(8),
                    curCSV.getString(9), curCSV.getString(10), curCSV.getString(11),
                    curCSV.getString(12), curCSV.getString(13), curCSV.getString(14),
                    curCSV.getString(15), curCSV.getString(16)};

            csvWrite.writeNext(arrStr);

            if ((writeCounter % 1000) == 0){
                csvWrite.flush();
            }

            Double progressPercent = Math.ceil(((float) writeCounter / (float) numRows)*100);
            Message msg = Message.obtain();
            msg.obj = progressPercent;
            msg.setTarget(messageHandler);
            msg.sendToTarget();
        }

        csvWrite.close();
        curCSV.close();
    }
}
