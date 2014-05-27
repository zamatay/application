package ru.vkb.model.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

/**
 * Created by Zamuraev_av on 26.02.14.
 */
public class RestProvider extends ContentProvider{
    final String TAG = getClass().getSimpleName();

    private static final String DBNAME = "vkb";

    private static final String DB_NAME = DBNAME + ".db";
    private static final int DB_VERSION = 1;

    private DatabaseHelper mDatabaseHelper;

    class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String sql;
            sql = Contract.disposals.getCreateTableSql();
            db.execSQL(sql);

            sql = Contract.disposal_comment.getCreateTableSql();
            db.execSQL(sql);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }

    @Override
    public boolean onCreate() {
        mDatabaseHelper = new DatabaseHelper(getContext(), DB_NAME, null, DB_VERSION);
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Contract.PATH contract = Contract.getContractByUri(uri);
        String table_name = contract._CONTENT_PATH();
        Cursor cursor = mDatabaseHelper.getReadableDatabase().query(table_name, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), contract._CONTENT_URI());
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return Contract.getContractByUri(uri)._CONTENT_TYPE();
    }


    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Contract.PATH contract = Contract.getContractByUri(uri);
        long identity = mDatabaseHelper.getWritableDatabase().insert(contract._CONTENT_PATH(), null, values);
        Uri resultUri = ContentUris.withAppendedId(contract._CONTENT_URI(), identity);
        getContext().getContentResolver().notifyChange(resultUri, null);
        Log.d(TAG, "Identity " + identity);
        return Uri.withAppendedPath(uri, Long.toString(identity));
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Contract.PATH contract = Contract.getContractByUri(uri);
        Integer count = mDatabaseHelper.getWritableDatabase().delete(contract._CONTENT_PATH(), selection, selectionArgs);
        if (count != 0)
            getContext().getContentResolver().notifyChange(contract._CONTENT_URI(), null);
        Log.d(TAG, "Deleted " + count.toString());
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        Integer count = mDatabaseHelper.getWritableDatabase().update(Contract.getContractByUri(uri)._CONTENT_PATH(), values, selection, selectionArgs);
        if (count != 0)
            getContext().getContentResolver().notifyChange(uri, null);
        Log.d(TAG, "Update " + count.toString());
        return count;
    }
}
