package com.harjeet.missedcallduration;

/**
 * Created by HARJEET on 05-May-17.
 */

import java.util.ArrayList;
import java.util.List;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class MissedCallDataSource {

    // Database fields
    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;
    private String[] allColumns = { MySQLiteHelper.COLUMN_ID,
            MySQLiteHelper.COLUMN_NUMBER, MySQLiteHelper.COLUMN_START,
            MySQLiteHelper.COLUMN_DURATION,};

    private static final int RECORDS_LIMIT = 100;

    public MissedCallDataSource(Context context) {
        dbHelper = new MySQLiteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public long insertData(String number, String start, String duration) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_NUMBER, number);
        values.put(MySQLiteHelper.COLUMN_START, start);
        values.put(MySQLiteHelper.COLUMN_DURATION, duration);

        long insertId = database.insert(MySQLiteHelper.TABLE_MISSEDCALL, null,
                values);
        int rowsCount = (int) DatabaseUtils.queryNumEntries(database, MySQLiteHelper.TABLE_MISSEDCALL);
        if (rowsCount > RECORDS_LIMIT) {
            int diff = rowsCount - RECORDS_LIMIT;
            String deleteExcess = "DELETE FROM " + MySQLiteHelper.TABLE_MISSEDCALL + " WHERE " + MySQLiteHelper.COLUMN_ID + " IN (SELECT " + MySQLiteHelper.COLUMN_ID + " FROM " + MySQLiteHelper.TABLE_MISSEDCALL + " ORDER BY " + MySQLiteHelper.COLUMN_ID + " ASC LIMIT " + diff + ");";
            database.execSQL(deleteExcess);
        }

        return insertId;
    }

    //Delete all records in table
    public void deleteData(long id) {
        database.delete(MySQLiteHelper.TABLE_MISSEDCALL, MySQLiteHelper.COLUMN_ID
                + " = " + id, null);
    }

    public void deleteAllData() {
        database.delete(MySQLiteHelper.TABLE_MISSEDCALL, null, null);
    }

    public List<MissedData> getAllData(int limit) {
        open();
        List<MissedData> myList = new ArrayList<MissedData>();

        int i=limit;
        Cursor cursor = database.query(MySQLiteHelper.TABLE_MISSEDCALL,
                allColumns, null, null, null, null, null);

        cursor.moveToLast();

        while (!cursor.isBeforeFirst() && i>0) {
            MissedData data = cursorToMissedData(cursor);
            myList.add(data);
            cursor.moveToPrevious();
            i--;
        }
        // make sure to close the cursor
        cursor.close();
        return myList;
    }

    private MissedData cursorToMissedData(Cursor cursor) {
        MissedData data = new MissedData();
        data.setId(cursor.getLong(cursor.getColumnIndex(MySQLiteHelper.COLUMN_ID)));
        data.setNumber(cursor.getString(cursor.getColumnIndex(MySQLiteHelper.COLUMN_NUMBER)));
        data.setStart(cursor.getString(cursor.getColumnIndex(MySQLiteHelper.COLUMN_START)));
        data.setDuration(Double.parseDouble(cursor.getString(cursor.getColumnIndex(MySQLiteHelper.COLUMN_DURATION))));

        return data;
    }
}
