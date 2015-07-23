package com.kii.sync.providers;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

/**
 * Created by yue on 15/7/23.
 */
public class SyncProvider extends ContentProvider {

    public static final String AUTHORITY = "com.kii.sync";

    public static final UriMatcher uriMatcher;

    public static final String TABLE_LIGHTS = "lights";

    public static final Uri URI_LIGHTS = Uri.parse("content://" + AUTHORITY + "/lights");

    private static final int ID_LIGHTS = 2;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, "lights", ID_LIGHTS);
    }

    private DBHelper mDBHelper;

    public SyncProvider() {
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        SQLiteDatabase database = mDBHelper.getWritableDatabase();
        switch (uriMatcher.match(uri)) {
            case ID_LIGHTS: {
                Cursor c = database.query(TABLE_LIGHTS, projection, selection, selectionArgs,
                        null, null, sortOrder);
                c.setNotificationUri(getContext().getContentResolver(), uri);
                return c;
            }
        }
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int ret = 0;
        SQLiteDatabase database = mDBHelper.getWritableDatabase();
        switch (uriMatcher.match(uri)) {
            case ID_LIGHTS:
                ret = database.delete(TABLE_LIGHTS,
                        selection, selectionArgs);
                getContext().getContentResolver().notifyChange(uri, null);
                return ret;
        }
        throw new UnsupportedOperationException("Not yet implemented");

    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase database = mDBHelper.getWritableDatabase();
        long cid = 0;
        switch (uriMatcher.match(uri)) {
            case ID_LIGHTS:
                cid = database.insertWithOnConflict(TABLE_LIGHTS, null, values,
                        SQLiteDatabase.CONFLICT_REPLACE);
                getContext().getContentResolver().notifyChange(uri, null);
                return Uri.withAppendedPath(uri, String.valueOf(cid));
        }
        throw new UnsupportedOperationException("Not yet implemented");

    }

    @Override
    public boolean onCreate() {
        mDBHelper = new DBHelper(getContext());
        return true;
    }



    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int ret = 0;
        SQLiteDatabase database = mDBHelper.getWritableDatabase();
        switch (uriMatcher.match(uri)) {
            case ID_LIGHTS:
                ret = database
                        .update(TABLE_LIGHTS, values, selection, selectionArgs);
                getContext().getContentResolver().notifyChange(uri, null);
                return ret;
        }
        throw new UnsupportedOperationException("Not yet implemented");
    }

}