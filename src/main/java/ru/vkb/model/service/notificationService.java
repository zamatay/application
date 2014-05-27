package ru.vkb.model.service;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import com.foxykeep.datadroid.requestmanager.Request;
import com.foxykeep.datadroid.requestmanager.RequestManager;

import org.json.JSONArray;

import ru.vkb.application.R;
import ru.vkb.common.notificationUtils;
import ru.vkb.model.RestRequestManager;
import ru.vkb.model.operations.dbOperations;
import ru.vkb.model.provider.Contract;
import ru.vkb.model.receiver.NotificationAlarmReceiver;

import static ru.vkb.model.RequestFactory.getRequestByParam;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class notificationService extends Service {
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static AlarmManager alarms;
    private static PendingIntent alarmIntent;
    private static String login;
    private static String password;
    private static Boolean autoUpdate;
    private static int updateFreq;
    private int NotifyID = -1;
    private Integer lastID;


    @Override
    public void onCreate() {
        alarms = (AlarmManager)getSystemService(Context.ALARM_SERVICE);

        Intent intentToFire = new Intent(NotificationAlarmReceiver.ACTION_CHECK_DISPOSALS_ALARM);
        alarmIntent = PendingIntent.getBroadcast(this, 0, intentToFire, 0);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        login = prefs.getString(getString(R.string.key_login), "");
        password = prefs.getString(getString(R.string.key_password), "");
        autoUpdate = prefs.getBoolean(getString(R.string.key_check_notification), true);
        updateFreq = Integer.parseInt(prefs.getString(getString(R.string.key_notification_list), "1"));
    }

    public static void startCheckDisposal(Context context) {
        Intent intent = new Intent(context, notificationService.class);
        context.startService(intent);
    }

    private void checkDisposals() {

        RestRequestManager.from(this).execute(getRequestByParam(-1, "getNotifyTask", null, null), new RequestManager.RequestListener() {

            @Override
            public void onRequestFinished(Request request, Bundle resultData) {
                String text = resultData.getString("result", "");
                JSONArray[] ar = new JSONArray[2];
                dbOperations.parseJSONObject(text, ar);
                if (ar[1].length()>0) {
                    // обновляем непрочитанные сообщения
                    String message = getString(R.string.notReadedTask) + " (" + ar[1].length() + ")";
                    // оповещаем пользователя
                    //if (NotifyID == -1)
                    //    NotifyID = notificationUtils.getInstance(notificationService.this).createInfoNotification(message, ar[1].toString(), "ru.vkb.intent.action.SHOW_DISPOSALS");
                    //else
                        notificationUtils.getInstance(notificationService.this).createInfoNotification(message, NotifyID, ar[1].toString(), "ru.vkb.intent.action.SHOW_DISPOSALS");
                }
            }

            private ContentValues getContentValues() {
                ContentValues item = new ContentValues();
                item.put(Contract.disposals.PROJECTION[Contract.disposals.readed], "1");
                return item;
            }

            @Override
            public void onRequestConnectionError(Request request, int statusCode) {
                stopSelf(lastID);
                //messages.showError(notificationService.this, getString(R.string.ConnectError));
            }

            @Override
            public void onRequestDataError(Request request) {
                stopSelf(lastID);
//                messages.showError(notificationService.this, getString(R.string.ErrorData));
            }

            @Override
            public void onRequestCustomError(Request request, Bundle resultData) {
                stopSelf(lastID);
//                messages.showError(notificationService.this, getString(R.string.UnknownError));
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        checkDisposals();
        lastID = startId;
        alarms.cancel(alarmIntent);
        if (autoUpdate) {
            int alarmType = AlarmManager.ELAPSED_REALTIME_WAKEUP;
            long timeToRefresh = SystemClock.elapsedRealtime() + updateFreq * 60 * 1000;
            alarms.set(alarmType, timeToRefresh, alarmIntent);
        }

        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
