package com.abrarshakhi.rtemcs.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.abrarshakhi.rtemcs.model.StatRecord;

import java.util.ArrayList;
import java.util.List;

public class PowerConsumptionHistDb extends SQLiteOpenHelper {

    // Table
    public static final String TABLE_STATS = "stats";
    // Columns
    public static final String COL_ID = "_id";
    public static final String COL_TIMESTAMP = "timestamp_ms";
    public static final String COL_POWER = "power_kw";
    private static final String DB_NAME = "power_hist.db";
    private static final int DB_VERSION = 1;

    public PowerConsumptionHistDb(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable =
            "CREATE TABLE " + TABLE_STATS + " (" +
                COL_ID + " INTEGER NOT NULL, " +
                COL_TIMESTAMP + " INTEGER NOT NULL, " +
                COL_POWER + " REAL NOT NULL, " +
                "PRIMARY KEY(" + COL_ID + ", " + COL_TIMESTAMP + ")" +
                ");";

        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STATS);
        onCreate(db);
    }

    // -----------------------------
    // INSERT
    // -----------------------------
    public void insertRecord(StatRecord record) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_ID, record.getId());
        values.put(COL_TIMESTAMP, record.getTimestampMs());
        values.put(COL_POWER, record.getPowerKW());

        db.insert(TABLE_STATS, null, values);
    }

    // -----------------------------
    // GET ALL RECORDS
    // -----------------------------
    public List<StatRecord> getAllRecords() {
        List<StatRecord> list = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
            TABLE_STATS,
            null,   // all columns
            null,
            null,
            null,
            null,
            COL_TIMESTAMP + " ASC"
        );
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID));
                long ts = cursor.getLong(cursor.getColumnIndexOrThrow(COL_TIMESTAMP));
                double power = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_POWER));

                list.add(new StatRecord(id, ts, power));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    // -----------------------------
    // DELETE BY ID
    // -----------------------------
    public int deleteRecord(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_STATS, COL_ID + "=?", new String[]{String.valueOf(id)});
    }

    // -----------------------------
    // DELETE ALL
    // -----------------------------
    public void deleteAll() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_STATS, null, null);
    }

    public List<StatRecord> getRecordsInRange(int id, long startTime, long endTime) {
        List<StatRecord> list = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();

        String selection = COL_ID + "=? AND " + COL_TIMESTAMP + " BETWEEN ? AND ?";
        String[] selectionArgs = new String[]{
            String.valueOf(id),
            String.valueOf(startTime),
            String.valueOf(endTime)
        };

        Cursor cursor = db.query(
            TABLE_STATS,
            null,
            selection,
            selectionArgs,
            null,
            null,
            COL_TIMESTAMP + " ASC"
        );

        if (cursor.moveToFirst()) {
            do {
                int recordId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID));
                long ts = cursor.getLong(cursor.getColumnIndexOrThrow(COL_TIMESTAMP));
                double power = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_POWER));

                list.add(new StatRecord(recordId, ts, power));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

}

