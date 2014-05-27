package ru.vkb.model.operations;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.os.RemoteException;

import com.foxykeep.datadroid.network.NetworkConnection;
import com.foxykeep.datadroid.requestmanager.Request;

import java.util.ArrayList;

import ru.vkb.model.provider.Contract;

/**
 * Created by Zamuraev_av on 09.04.2014.
 */
public class dbDisposalNotifyOperation extends dbOperations {
    protected Integer disposal_id;

    @Override
    protected void getParam(Request request, NetworkConnection connection) {
        disposal_id = request.getInt("disposal_id");
        super.getParam(request, connection);
    }

    @Override
    public void insertValues(Context context, ContentValues[] values) {
        // список комманд
        ArrayList<ContentProviderOperation> list = new ArrayList<ContentProviderOperation>();
        // комманда на удаление
        list.add(ContentProviderOperation.newDelete(getContentUri()).withSelection(Contract.disposal_comment.disposal_id + "=?", new String[]{disposal_id.toString()}).build());
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
}
