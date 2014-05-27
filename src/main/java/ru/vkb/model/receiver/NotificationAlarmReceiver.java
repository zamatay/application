package ru.vkb.model.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import ru.vkb.model.service.notificationService;

/**
 * Created by Zamuraev_av on 17.04.2014.
 */
public class NotificationAlarmReceiver extends BroadcastReceiver {
    public static final String ACTION_CHECK_DISPOSALS_ALARM = "ru.vkb.model.service.CHECK_DISPOSAL";
    @Override
    public void onReceive(Context context, Intent intent) {
        notificationService.startCheckDisposal(context);
    }
}
