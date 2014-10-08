package ru.vkb.model.operations;

import android.content.ContentProviderOperation;
import android.content.ContentValues;

import com.foxykeep.datadroid.network.NetworkConnection;
import com.foxykeep.datadroid.requestmanager.Request;

import java.util.ArrayList;

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
    protected ArrayList<ContentProviderOperation> getPrepareCommand(ContentValues[] values) {
        if (mRequest.contains("resync")){
            return super.getPrepareCommand(values);
        } else{
            ArrayList<ContentProviderOperation> list = new ArrayList<ContentProviderOperation>();
            list.add(ContentProviderOperation.newDelete(getContentUri()).withSelection(
                            "disposal_id=?",
                            new String[]{disposal_id.toString()}).build());
            return list;
        }
    }
}
