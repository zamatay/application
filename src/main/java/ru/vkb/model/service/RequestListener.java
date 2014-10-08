package ru.vkb.model.service;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.foxykeep.datadroid.requestmanager.Request;
import com.foxykeep.datadroid.requestmanager.RequestManager;
import com.handmark.pulltorefresh.library.PullToRefreshBase;

import java.util.EventListener;
import java.util.Timer;
import java.util.TimerTask;

import ru.vkb.common.messages;
import ru.vkb.task.R;
import ru.vkb.ui.BaseActivity;
import ru.vkb.ui.login.LoginActivity;

/**
 * Created by Zamuraev_av on 21.04.2014.
 */
public class RequestListener implements RequestManager.RequestListener {

    public static interface  RequestFinishListener extends EventListener {
        public void onRequestFinished(Request request, Bundle resultData);
    }

    private Context mContext;
    private PullToRefreshBase SwipeRefresh;
    private RequestListener mInstance;
    private Boolean isWorking = false;
    Handler handler;

    public static RequestListener getRequestListener(Context context) {
        final RequestListener Instance = new RequestListener();

        Instance.handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    if (!Instance.isWorking)
                        ((PullToRefreshBase) msg.obj).setRefreshing(true);
                }
        };

        Instance.mContext = context;

        if (context instanceof BaseActivity){
            Instance.SwipeRefresh = ((BaseActivity) context).getSwipeRefresh();
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    Message msg = new Message();
                    msg.obj = Instance.SwipeRefresh;
                    Instance.handler.sendMessage(msg);
                }
            }, 900, 1);
        }

        return Instance;
    }

    @Override
    public void onRequestFinished(Request request, Bundle resultData) {
        if (mContext != null && mContext instanceof RequestFinishListener){
            ((RequestFinishListener) mContext).onRequestFinished(request, resultData);
        } else if (mContext != null && mContext instanceof RequestManager.RequestListener){
            ((RequestManager.RequestListener) mContext).onRequestFinished(request, resultData);
        }
        stopRefresh();
    }

    private void stopRefresh() {
        if (mContext instanceof BaseActivity){
            ((BaseActivity) mContext).getSwipeRefresh().onRefreshComplete();
        }
        isWorking = true;
    }

    @Override
    public void onRequestConnectionError(Request request, int statusCode) {
        stopRefresh();
        if (mContext != null && statusCode == 403){
            Intent intent = new Intent(mContext, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.getApplicationContext().startActivity(intent);
            if (mContext instanceof Activity){
                ((Activity) mContext).finish();
            }
        } else if (mContext != null && mContext instanceof RequestManager.RequestListener) {
            ((RequestManager.RequestListener) mContext).onRequestConnectionError(request, statusCode);
        } else if (mContext != null) {
            messages.showError(mContext, mContext.getString(R.string.ConnectError));
        }

    }

    @Override
    public void onRequestDataError(Request request) {
        stopRefresh();
        if (mContext != null && mContext instanceof RequestManager.RequestListener) {
            ((RequestManager.RequestListener) mContext).onRequestDataError(request);
        } else if (mContext != null) {
            messages.showError(mContext, mContext.getString(R.string.ErrorData));
        }
    }

    @Override
    public void onRequestCustomError(Request request, Bundle resultData) {
        stopRefresh();
        if (mContext != null && mContext instanceof RequestManager.RequestListener) {
            ((RequestManager.RequestListener) mContext).onRequestCustomError(request, resultData);
        } else if (mContext != null) {
            messages.showError(mContext, mContext.getString(R.string.UnknownError));
        }
    }
}
