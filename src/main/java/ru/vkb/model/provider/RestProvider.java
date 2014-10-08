package ru.vkb.model.provider;

import android.content.ContentProvider;
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

    public DatabaseHelper mDatabaseHelper;

    class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String sql;
            // таблица задач
            sql = Contract.ContractFactory.Disposals.getCreateTableSql();
            db.execSQL(sql);

            // таблица коментариев
            sql = Contract.ContractFactory.DisposalsComment.getCreateTableSql();
            db.execSQL(sql);

            // таблица пользователей
            sql = Contract.ContractFactory.Staff.getCreateTableSql();
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
        Cursor cursor;
        switch (Contract.sUriMatcher.match(uri)){
            case Contract.ID_DISPOSALS_GROUP:
                cursor = fetchReceiverGroup(selection, selectionArgs);
                break;
            case Contract.ID_DISPOSALS_CHILD:
                cursor = getDisposals(selection, selectionArgs);
                break;
            case Contract.ID_DISTINCT_STAFF:
                cursor =  getStaffIDs();
                break;
            default:
                cursor = mDatabaseHelper.getReadableDatabase().query(
                        Contract.ContractFactory.getContractByUri(uri).getTableName(),
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        //return Contract.getContractByUri(uri).CONTENT_TYPE();
        return "vnd.android.cursor.dir/vnd.";
    }


    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long identity = mDatabaseHelper.getWritableDatabase().insert(Contract.ContractFactory.getContractByUri(uri).getTableName(), null, values);
        //Uri resultUri = ContentUris.withAppendedId(Contract.getContractByUri(uri).CONTENT_URI(), identity);
        getContext().getContentResolver().notifyChange(uri, null);
        Log.d(TAG, "Identity " + identity);
        return Uri.withAppendedPath(uri, Long.toString(identity));
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Integer count = mDatabaseHelper.getWritableDatabase().delete(Contract.ContractFactory.getContractByUri(uri).getTableName(), selection, selectionArgs);
        if (count != 0)
            getContext().getContentResolver().notifyChange(uri, null);
        Log.d(TAG, "Deleted " + count.toString());
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        Integer count;
        switch (Contract.sUriMatcher.match(uri)){
            case Contract.ID_DISPOSALS_CHILD:
                count = mDatabaseHelper.getWritableDatabase().update("disposals", values, selection, selectionArgs);
                break;
            default:
                count = mDatabaseHelper.getWritableDatabase().update(Contract.ContractFactory.getContractByUri(uri).getTableName(), values, selection, selectionArgs);
                break;
        }

        if (count != 0)
            getContext().getContentResolver().notifyChange(uri, null);

        Log.d(TAG, "Update " + count.toString());
        return count;
    }

    public Cursor fetchReceiverGroup(String selection, String[] selectionArgs) {
        StringBuilder sb = new StringBuilder("SELECT distinct [d].[receiver_id] _id, [s].[userName] " +
                "FROM disposals d " +
                "left join staff s on [s].[_id] = [d].[receiver_id]");

        if (selection != null & selectionArgs != null) {
            sb.append(" WHERE ");
            sb.append("[d].");
            sb.append(selection);
        }

        return mDatabaseHelper.getReadableDatabase().rawQuery(sb.toString(), selectionArgs);
    }

    public Cursor getDisposals(String selection, String[] selectionArgs){
        StringBuilder query =
        new StringBuilder("SELECT disposals._id, number, s.userName sender_id, theme, shortTask, task, readed, isExecute " +
                "FROM disposals " +
                "LEFT JOIN staff s on s._id = sender_id " +
                "LEFT JOIN staff r on r._id = receiver_id ");
        if (selection != null) {
            query.append(" WHERE ");
            query.append(selection);
        }

        return mDatabaseHelper.getReadableDatabase().rawQuery(query.toString(), selectionArgs);
    }

    public Cursor getStaffIDs(){
        String query = "select id FROM (SELECT sender_id id FROM disposals UNION select receiver_id FROM disposals) a WHERE not exists (select _id from staff where _id = a.id)";
        return mDatabaseHelper.getReadableDatabase().rawQuery(query, null);
    }
}
