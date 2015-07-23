package com.kii.sync.providers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by yue on 15/7/23.
 */
public class DBHelper extends SQLiteOpenHelper {

    public static final int DB_VERSION = 1;

    public static final String DB_NAME = "sync.sqlite";

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createDB(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    protected void createDB(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS lights ("
                        + "_id INTEGER PRIMARY KEY, "
                        + "MAC TEXT NOT NULL,"
                        + "ver INTEGER,"
                        + "ThingID TEXT,"
                        + "remote_pwd TEXT,"
                        + "admin_pwd TEXT,"
                        + "name TEXT,"
                        + "color TEXT NOT NULL,"
                        + "model TEXT,"
                        + "brightness INTEGER,"
                        + "CT INTEGER,"
                        + "mode INTEGER,"
                        + "IP INTEGER DEFAULT 0,"
                        + "subIP INTEGER DEFAULT 0,"
                        + "is_mine BOOL,"
                        + "state BOOL DEFAULT 0,"
                        + "connected BOOL,"
                        + "synced BOOL,"
                        + "owned_time INTEGER,"
                        + "last_active INTEGER,"
                        + "deleted INTEGER DEFAULT 0"
                        + ");"
        );
    }
}
