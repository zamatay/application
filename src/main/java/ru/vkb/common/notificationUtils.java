package ru.vkb.common;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import java.util.HashMap;

import ru.vkb.task.R;
import ru.vkb.ui.DisposalsTreeActivity;

/**
 * Created by Zamuraev_av on 16.04.2014.
 */
public class notificationUtils {

    private static final String TAG = notificationUtils.class.getSimpleName();

    private static notificationUtils instance;

    private static Context context;
    private NotificationManager manager; // Системная утилита, упарляющая уведомлениями
    private int lastId = 0; //постоянно увеличивающееся поле, уникальный номер каждого уведомления
    private HashMap<Integer, Notification> notifications; //массив ключ-значение на все отображаемые пользователю уведомления


    //приватный контструктор для Singleton
    private notificationUtils(Context context) {
        this.context = context;
        manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notifications = new HashMap<Integer, Notification>();
    }

    /**
     * Получение ссылки на синглтон
     */
    public static notificationUtils getInstance(Context context) {
        if (instance == null) {
            instance = new notificationUtils(context);
        } else {
            instance.context = context;
        }
        return instance;
    }

    protected NotificationCompat.Builder getBuilder(String message, String data, String IntentAction){
        //notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        Intent resultIntent = new Intent(IntentAction);
        resultIntent.putExtra("filter", data);
        resultIntent.putExtra("fromNotify", true);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
// Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(DisposalsTreeActivity.class);
// Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);

        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder nb = new NotificationCompat.Builder(context).setSmallIcon(R.drawable.ic_launcher) //иконка уведомления
                .setAutoCancel(true) //уведомление закроется по клику на него
                .setTicker(message) //текст, который отобразится вверху статус-бара при создании уведомления
                .setContentText(message) // Основной текст уведомления
                .setContentIntent(resultPendingIntent)
                .setWhen(System.currentTimeMillis()) //отображаемое время уведомления
                .setContentTitle(context.getString(R.string.app_name)) //заголовок уведомления
                .setDefaults(Notification.DEFAULT_ALL); // звук, вибро и диодный индикатор выставляются по умолчанию
        return nb;
    }

    public void createInfoNotification(String message, Integer ID, String data, String IntentAction){
        NotificationCompat.Builder nb = getBuilder(message, data, IntentAction);

        Notification notification = nb.build(); //генерируем уведомление
        manager.cancel(ID);
        manager.notify(ID, notification); // отображаем его пользователю.
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public int createInfoNotification(String message, String data, String IntentAction){
        NotificationCompat.Builder nb = getBuilder(message, data, IntentAction);

        Notification notification = nb.build(); //генерируем уведомление
        manager.notify(lastId, notification); // отображаем его пользователю.
        notifications.put(lastId, notification); //теперь мы можем обращаться к нему по id
        return lastId++;
    }

    /**
     * Создание уведомления с прогрессбаром о загрузке
     * @param fileName - текст, отображённый в заголовке уведомления.
     */
    /*
    public int createDownloadNotification(String fileName){
        String text = context.getString(R.string.notification_downloading).concat(" ").concat(fileName); //текст уведомления
        RemoteViews contentView = createProgressNotification(text, context.getString(R.string.notification_downloading)); //View уведомления
        //contentView.setImageViewResource(R.id.notification_download_layout_image, R.drawable.ic_launcher); // иконка уведомления
        return lastId++; //увеличиваем id, которое будет соответствовать следующему уведомлению
    }
    */

    /**
     * генерация уведомления с ProgressBar, иконкой и заголовком
     *
     * @param text заголовок уведомления
     * @param topMessage сообщение, уотображаемое в закрытом статус-баре при появлении уведомления
     * @return View уведомления.
     */
    /*
    private RemoteViews createProgressNotification(String text, String topMessage) {
        Notification notification = new Notification(R.drawable.ic_launcher, topMessage, System.currentTimeMillis());
        RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.notification_download_layout);
        contentView.setProgressBar(R.id.notification_download_layout_progressbar, 100, 0, false);
        contentView.setTextViewText(R.id.notification_download_layout_title, text);

        notification.contentView = contentView;
        notification.flags = Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT | Notification.FLAG_ONLY_ALERT_ONCE;

        //Intent notificationIntent = new Intent(context, NotificationUtils.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
        notification.contentIntent = contentIntent;

        manager.notify(lastId, notification);
        notifications.put(lastId, notification);
        return contentView;
    }*/
}