package ru.vkb.common;

import android.app.AlertDialog;
import android.content.Context;

/**
 * Created by Zamuraev_av on 08.04.2014.
 */
public class messages {

    public static void showError(Context context, String text){
        if (context != null){
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.
                    setTitle(android.R.string.dialog_alert_title).
                    setMessage(text).
                    create().
                    show();
        }
    }
}
