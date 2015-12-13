package com.noopinion.haste.noopinion.model.db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by Ivan Gusev on 01.12.2015.
 */
public final class NewsContentProvider extends ContentProvider {

    public static final String AUTHORITY = "com.noopinion.haste.noopinion.provider.news";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/news");

    private DBOpenHelper mOpenHelper;

    @Override
    public boolean onCreate() {
        mOpenHelper = new DBOpenHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull final Uri uri, final String[] projection, final String selection, final String[] selectionArgs,
                        final String sortOrder) {
        final SQLiteDatabase database = mOpenHelper.getWritableDatabase();
        if (database.isOpen()) {
            final Cursor c = database.query(
                    uri.getLastPathSegment(),
                    projection, null,
                    null, null, null,
                    sortOrder, null
            );

            if (getContext() != null) {
                c.setNotificationUri(getContext().getContentResolver(), CONTENT_URI);
            }

            return c;
        }

        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull final Uri uri, final ContentValues values) {
        final SQLiteDatabase database = mOpenHelper.getWritableDatabase();
        if (database.isOpen()) {
            final Cursor c = database.query(
                    uri.getLastPathSegment(),
                    new String[]{BaseColumns._ID},
                    BaseColumns._ID + " = ?",
                    new String[]{values.getAsString(BaseColumns._ID)},
                    null, null, null
            );
            try {
                if (c != null && c.moveToFirst()) {
                    database.update(uri.getLastPathSegment(), values, BaseColumns._ID + " = ?", new String[]{values.getAsString(BaseColumns._ID)});
                    final Uri resultUri = ContentUris.withAppendedId(uri, values.getAsInteger(BaseColumns._ID));
                    if (getContext() != null) {
                        getContext().getContentResolver().notifyChange(resultUri, null);
                    }
                    return resultUri;
                } else {
                    final long id = database.insert(uri.getLastPathSegment(), null, values);
                    if (id != -1) {
                        final Uri resultUri = ContentUris.withAppendedId(uri, id);
                        if (getContext() != null) {
                            getContext().getContentResolver().notifyChange(resultUri, null);
                        }
                        return resultUri;
                    }
                }
            } finally {
                if (c != null) {
                    c.close();
                }
            }
        }

        return null;
    }

    @Override
    public int delete(@NonNull final Uri uri, final String selection, final String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int update(@NonNull final Uri uri, final ContentValues values, final String selection, final String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

    @Nullable
    @Override
    public String getType(@NonNull final Uri uri) {
        throw new UnsupportedOperationException();
    }
}

final class DBOpenHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "news.db";
    public static final int DB_VERSION = 2;

    public static final String CREATE_TABLE = "" +
            "CREATE TABLE news (\n" +
            "_id INTEGER PRIMARY KEY,\n" +
            "txt TEXT,\n" +
            "link TEXT,\n" +
            "date INTEGER,\n" +
            "image TEXT);";

    public DBOpenHelper(final Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(final SQLiteDatabase db) {
        db.beginTransaction();
        try {
            db.execSQL(CREATE_TABLE);

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
         if (newVersion>oldVersion){
             db.execSQL("DROP TABLE IF EXISTS news");
             db.execSQL(CREATE_TABLE);
         }
    }
}
