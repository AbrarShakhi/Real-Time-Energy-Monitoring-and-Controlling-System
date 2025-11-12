package com.abrarshakhi.rtemcs.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;

import com.abrarshakhi.rtemcs.model.DeviceInfo;

import java.util.ArrayList;
import java.util.List;

public class DeviceInfoDb extends SQLiteOpenHelper {

    public static final String TABLE_NAME = "DEVICE_INFO";
    public static final String COL_ID = "_id";
    public static final String COL_DEVICE_NAME = "device_name";
    public static final String COL_DEVICE_ID = "device_id";
    public static final String COL_ACCESS_ID = "access_id";
    public static final String COL_ACCESS_SECRET = "access_secret";

    private static final String DB_NAME = "RTEMCS.DB";
    private static final int DB_VERSION = 1;

    public DeviceInfoDb(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(@NonNull SQLiteDatabase db) {
        String createTableSQL = "CREATE TABLE " + TABLE_NAME + " (" +
            COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_DEVICE_NAME + " TEXT NOT NULL, " +
            COL_DEVICE_ID + " TEXT NOT NULL, " +
            COL_ACCESS_ID + " TEXT NOT NULL, " +
            COL_ACCESS_SECRET + " TEXT NOT NULL" +
            ");";
        db.execSQL(createTableSQL);
    }

    @Override
    public void onUpgrade(@NonNull SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean insertDevice(DeviceInfo deviceInfo) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_DEVICE_NAME, deviceInfo.getDeviceName());
        values.put(COL_DEVICE_ID, deviceInfo.getDeviceId());
        values.put(COL_ACCESS_ID, deviceInfo.getAccessId());
        values.put(COL_ACCESS_SECRET, deviceInfo.getAccessSecret());
        long result = db.insert(TABLE_NAME, null, values);
        db.close();
        return result != -1;
    }

    public List<DeviceInfo> getAllDevices() {
        List<DeviceInfo> devices = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID));
                String deviceName = cursor.getString(cursor.getColumnIndexOrThrow(COL_DEVICE_NAME));
                String deviceId = cursor.getString(cursor.getColumnIndexOrThrow(COL_DEVICE_ID));
                String accessId = cursor.getString(cursor.getColumnIndexOrThrow(COL_ACCESS_ID));
                String accessSecret = cursor.getString(cursor.getColumnIndexOrThrow(COL_ACCESS_SECRET));

                DeviceInfo device = new DeviceInfo(id, deviceName, deviceId, accessId, accessSecret);
                devices.add(device);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return devices;
    }

    public int deleteDevice(String deviceId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete(TABLE_NAME, COL_DEVICE_ID + "=?", new String[]{deviceId});
        db.close();
        return rowsDeleted;
    }

    public int updateDevice(DeviceInfo deviceInfo) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_DEVICE_NAME, deviceInfo.getDeviceName());
        values.put(COL_DEVICE_ID, deviceInfo.getDeviceId());
        values.put(COL_ACCESS_ID, deviceInfo.getAccessId());
        values.put(COL_ACCESS_SECRET, deviceInfo.getAccessSecret());
        int rowsUpdated = db.update(TABLE_NAME, values, COL_ID + "=?", new String[]{String.valueOf(deviceInfo.getId())});
        db.close();
        return rowsUpdated;
    }
}
