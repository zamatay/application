package ru.vkb.model.service;

import android.content.Context;
import android.os.Bundle;

import com.foxykeep.datadroid.requestmanager.Request;
import com.foxykeep.datadroid.requestmanager.RequestManager;

import ru.vkb.application.R;
import ru.vkb.common.messages;

/**
 * Created by Zamuraev_av on 21.04.2014.
 */
public class RequestListener implements RequestManager.RequestListener {

    private static Context mContext;
    private static RequestListener mInstance;

    public static RequestListener getRequestListener(Context context) {
        if (mInstance == null) {
            mInstance = new RequestListener();
        }
        mContext = context;
        return mInstance;
    }

    @Override
    public void onRequestFinished(Request request, Bundle resultData) {

    }

    @Override
    public void onRequestConnectionError(Request request, int statusCode) {
        messages.showError(mContext, mContext.getString(R.string.ConnectError));
    }

    @Override
    public void onRequestDataError(Request request) {
        messages.showError(mContext, mContext.getString(R.string.ErrorData));
    }

    @Override
    public void onRequestCustomError(Request request, Bundle resultData) {
        messages.showError(mContext, mContext.getString(R.string.UnknownError));
    }
}
