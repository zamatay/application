package ru.vkb.model;

import android.content.Context;

import com.foxykeep.datadroid.requestmanager.RequestManager;

import ru.vkb.model.service.RestService;

/**
 * Created by Zamuraev_av on 26.02.14.
 */
public final class RestRequestManager extends RequestManager {
    private RestRequestManager(Context context) {
        super(context, RestService.class);
    }

    private static RestRequestManager sInstance;

    public static RestRequestManager from(Context context) {
        if (sInstance == null) {
            sInstance = new RestRequestManager(context);
        }

        return sInstance;
    }
}
