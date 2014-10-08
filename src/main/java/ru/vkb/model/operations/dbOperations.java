package ru.vkb.model.operations;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.os.RemoteException;

import com.foxykeep.datadroid.exception.DataException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import ru.vkb.model.RequestFactory;
import ru.vkb.model.provider.Contract;

/**
 * Created by Zamuraev_av on 03.04.2014.
 */
public class dbOperations extends BaseOperations {
    String selection;
    String[] selectionArgs;

    public static void parseJSONObject(String result, JSONArray[] data){
        JSONObject tmp;
        try {
            tmp = new JSONObject(result);
            // данные
            data[0] = tmp.getJSONArray("Columns");
            // метаданные
            data[1] = tmp.getJSONArray("Data");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public JSONObject getContentValues(String сonnectionResult) throws JSONException, DataException {
        JSONObject result =  super.getContentValues(сonnectionResult);
        try {
            // данные
            JSONArray data = result.getJSONArray("result").getJSONObject(0).getJSONArray("Data");
            // метаданные
            JSONArray column = result.getJSONArray("result").getJSONObject(0).getJSONArray("Columns");
            // массив для вставки
            ContentValues[] values = new ContentValues[data.length()];

            // через цикл заполняем массив для вставки
            for (int i = 0; i < data.length(); ++i) {
                ContentValues item = new ContentValues();
                JSONArray row = data.getJSONArray(i);
                for (int j = 0; j < column.length(); j++) {
                    if ("id".compareToIgnoreCase(column.getJSONObject(j).getString("Title")) == 0)
                        item.put("_ID", row.getString(j));
                    else if ("rownumber".compareToIgnoreCase(column.getJSONObject(j).getString("Title")) == 0){
                        continue;
                    } else
                        item.put(column.getJSONObject(j).getString("Title"), row.getString(j));
                }
                values[i] = item;
            }
            // сама вставка
            insertValues(mContext, values);
        } catch (JSONException e) {
            throw new DataException(e.getMessage());
        }
        return result;


    }

    public void insertValues(Context context, ContentValues[] values){
        // список комманд
            ArrayList<ContentProviderOperation> list = getPrepareCommand(values);

        // добавляем комманды на вставку
        for (int i = 0; i < values.length; i++){
            list.add(ContentProviderOperation.newInsert(getContentUri()).withValues(values[i]).build());
        }
        try {
            // непосредственно выполнение комманд
            context.getContentResolver().applyBatch(Contract.Base.AUTHORITY, list);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }
    }

    protected ArrayList<ContentProviderOperation> getPrepareCommand(ContentValues[] values) {
        ArrayList<ContentProviderOperation> list = new ArrayList<ContentProviderOperation>();
        // комманда на удаление, если есть флаг не удалять или resync то не удаляем
        if (!RequestFactory.isNotDeleteFlag(mRequest) & !RequestFactory.isResyncFlag(mRequest))
            list.add(ContentProviderOperation.newDelete(getContentUri()).build());
        if (RequestFactory.isResyncFlag(mRequest) & values.length > 0) {
            initArgs(values);
            list.add(ContentProviderOperation.newDelete(getContentUri()).withSelection(selection, selectionArgs).build());
        }
        return list;
    }

    private void initArgs(ContentValues[] values) {
        StringBuilder sb = new StringBuilder("_ID IN (");
        selectionArgs = new String[values.length];
        for (int i=0; i<values.length; i++){
            selectionArgs[i]=values[i].getAsString("_ID");
            sb.append("?");
            if (i < values.length - 1)
                sb.append(",");
        }
        sb.append(")");
        selection = sb.toString();
    }


}
