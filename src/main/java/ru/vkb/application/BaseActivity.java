package ru.vkb.application;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import com.foxykeep.datadroid.requestmanager.Request;
import com.foxykeep.datadroid.requestmanager.RequestManager;

import ru.vkb.application.preference.SettingsActivity;
import ru.vkb.common.messages;

/**
 * Created by Zamuraev_av on 07.04.2014.
 */
public class BaseActivity extends ActionBarActivity  implements LoaderManager.LoaderCallbacks<Cursor>, RequestManager.RequestListener, View.OnClickListener{
    protected SwipeRefreshLayout swipeRefresh;
    // датасет для отображения activity
    public SimpleCursorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem mi = menu.add(0,1,10,getString(R.string.Settings));
        mi.setIntent(new Intent(this, SettingsActivity.class));
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        init();
        createFilter();
        afterInit();
    }

    public void createFilter() {
    }

    public void init(){
        // процедура инициализации, здесь определяем все свои контролы и их инициализируем
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.SwipeRefresh);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                request();
            }
        });
        swipeRefresh.setColorScheme(android.R.color.white, android.R.color.holo_blue_light, android.R.color.white, android.R.color.holo_blue_light);
    }

    private void afterInit() {
        // после всех инициализаций загружаем данные из базы
        if (!getIntent().getBooleanExtra("fromNotify", false)){
            refresh();
        }
    }

    public void refresh(){
        // срабатывает когда необходимо запросить из базы

    }

    public void startRequest(){
        swipeRefresh.setRefreshing(true);
    }

    public void stopRequest(){
        swipeRefresh.setRefreshing(false);
    }

    protected void request(){
        // срабатывает когда необходимо послать сетевой запрос
        // и сохранить результаты в базу
        startRequest();
    }

    /*
    * обработка событий запроса к базе
    * */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // создание курсора в каждой активити возвращается свой курсор
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // подставляем адаптеру курсор после завершения если курсор пуст делаем сетевой запрос
        if (adapter != null)
            adapter.swapCursor(data);
        if ((data != null) && (data.getCount() == 0))
            request();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // при отмене запроса обнуляем адаптеру курсор
        if (adapter != null)
            adapter.swapCursor(null);
    }

    /*
    * обработка событий сетевого запроса
    * */
    @Override
    public void onRequestFinished(Request request, Bundle resultData) {
        stopRequest();
        if (request.getBoolean("withUpdate"))
            refresh();
    }

    @Override
    public void onRequestConnectionError(Request request, int statusCode) {
        stopRequest();
        messages.showError(this, getString(R.string.ConnectError));
    }

    @Override
    public void onRequestDataError(Request request) {
        stopRequest();
        messages.showError(this, getString(R.string.ErrorData));
    }

    @Override
    public void onRequestCustomError(Request request, Bundle resultData) {
        stopRequest();
        messages.showError(this, getString(R.string.UnknownError));
    }
    /*
    * конец обработки событий сетевого запроса
    * */

    /*
    * обработка нажатия кнопки
    * */
    @Override
    public void onClick(View v) {}
}
