package ru.vkb.model.operations;

import android.content.ContentValues;
import android.content.Context;

/**
 * Created by Zamuraev_av on 09.04.2014.
 */
public class dbDisposalOperation extends dbOperations {
    protected Integer disposal_id;

    @Override
    public void insertValues(Context context, ContentValues[] values) {
        /*
        for (int i = 0; i < values.length; i++) {
            for (int j = 0; j < Contract.disposals.columnsName.length; j++){
                //if
            }
            //Contract.disposals.columnsMeta
        }
        */
        super.insertValues(context, values);
    }
}
