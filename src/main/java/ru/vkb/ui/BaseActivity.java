package ru.vkb.ui;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.foxykeep.datadroid.requestmanager.Request;
import com.handmark.pulltorefresh.library.PullToRefreshAdapterViewBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import ru.vkb.model.service.RequestListener;
import ru.vkb.task.R;
import ru.vkb.common.MultiSpinner;
import ru.vkb.model.FilterArray;
import ru.vkb.model.FilterDescription;
import ru.vkb.model.RequestFactory;
import ru.vkb.model.provider.Contract;
import ru.vkb.ui.filter.FilterCheckBox;
import ru.vkb.ui.filter.FilterEditText;
import ru.vkb.ui.filter.FilterInteger;
import ru.vkb.ui.preference.SettingsActivity;

/**
 * Created by Zamuraev_av on 07.04.2014.
 */
public class BaseActivity extends ActionBarActivity
        implements LoaderManager.LoaderCallbacks<Cursor>,
        RequestListener.RequestFinishListener,
        View.OnClickListener, MultiSpinner.MultiSpinnerListener{


    protected final String DEBUG_TAG = getClass().getSimpleName().toString();

    protected String[] mRequest = null;
    protected String[] mRequestArg = null;

    public PullToRefreshListView swipeRefresh;
    public FilterArray<FilterDescription> filterArray = new FilterArray<FilterDescription>();
    protected ImageButton refreshButton;

    // датасет для отображения activity
    public SimpleCursorAdapter adapter;

    protected String mSelection = null;
    protected String[] mSelectionArg = null;

    protected final Integer REQUEST_FROM_LV = 0;
    protected final Integer REQUEST_FROM_FILTER = 1;
    protected final Integer REQUEST_FROM_DB_REFRESH = 2;

    protected Request lastRequest;
    protected SlidingUpPanelLayout slidingUpPanel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    public Uri getUri(){
        return null;
    }


    public void refreshByID(int path) {
        refreshByID(path, null);
    }

    public void refreshByID(int path, Bundle argument) {
        if (argument == null)
            argument = new Bundle();

        argument.putString("Selection", mSelection);
        argument.putStringArray("SelectionArg", mSelectionArg);

        Loader<Cursor> loader = getSupportLoaderManager().getLoader(path);
        if (loader != null && !loader.isReset())
            getSupportLoaderManager().restartLoader(path, argument, this);
        else
            getSupportLoaderManager().initLoader(path, argument, this);
    }

    protected void clearArgument() {
        mSelection = null;
        mSelectionArg = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.base, menu);
        menu.findItem(R.id.action_Setting).setIntent(new Intent(this, SettingsActivity.class));
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

        filterArray.clear();
        Contract.Base path =  Contract.ContractFactory.getContractByUri(getUri());

        LinearLayout parent_layout = (LinearLayout) findViewById(R.id.filter);
        if (parent_layout == null) return;
        parent_layout.setOrientation(LinearLayout.VERTICAL);
        for (int i=0;i<path.getColumns().size();i++){
            if (!path.getColumns().get(i).filterEnabled)
                continue;

            LinearLayout ll = new LinearLayout(this);

            //ToggleButton tb = new ToggleButton(this);
            //ll.addView(tb, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT));

            TextView tv = new TextView(this);
            tv.setText(path.getColumns().get(i).Caption);
            ll.addView(tv, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT));
            String s = path.getColumns().get(i).Meta;

            if (s.equalsIgnoreCase("text")){
                EditText et = new FilterEditText(this);
                filterArray.add(new FilterDescription(path.getColumns().get(i).Name, "text", et));
                if (path.getColumns().get(i).defaultValues != null && path.getColumns().get(i).defaultValues.length > 0){
                    et.setText((String) path.getColumns().get(i).defaultValues[0]);
                }
                ll.addView(et, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT));
            } else if (s.equalsIgnoreCase("bit")){
                //ThreeStateCheckBox cb = new ThreeStateCheckBox(this);
                tv = new TextView(this);
                tv.setText(R.string.Yes);
                tv.setPadding(5,0,0,0);
                ll.addView(tv, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT));

                FilterCheckBox cb_1 = new FilterCheckBox(this);
                ll.addView(cb_1, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT));

                tv = new TextView(this);
                tv.setText(R.string.No);
                ll.addView(tv, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT));

                FilterCheckBox cb_2 = new FilterCheckBox(this);
                ll.addView(cb_2, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT));
                filterArray.add(new FilterDescription(path.getColumns().get(i).Name, "bit", cb_1, cb_2));

                if (path.getColumns().get(i).defaultValues != null && path.getColumns().get(i).defaultValues.length > 0){
                    for (int j = 0; j < path.getColumns().get(i).defaultValues.length; j++) {
                        if ((Boolean) path.getColumns().get(i).defaultValues[j])
                            cb_1.setChecked(true);
                        else {
                            cb_2.setChecked(true);
                        }
                    }
                }

            } else if ("link".equalsIgnoreCase(s.substring(0, 4))){
                final String table_name = s.substring(5);
                final Uri uri = Contract.ContractFactory.getUriByTable(table_name);

                MultiSpinner sp = new MultiSpinner(this);

                adapter = new SimpleCursorAdapter(getApplicationContext(),android.R.layout.simple_spinner_dropdown_item, null,
                        new String[]{Contract.ContractFactory.getContractByUri(uri).getColumns().get(1).Name},
                        new int[]{android.R.id.text1},
                        0);

                sp.setAdapter(adapter);

                adapter.swapCursor(getContentResolver().query(uri, Contract.PATH.ALL, null, null, null));
                sp.setItems(adapter, 1, this);
                ll.addView(sp, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT));
                filterArray.add(new FilterDescription(Contract.ContractFactory.getContractByUri(uri).getColumns().get(1).Name, "link", sp));

            }
            parent_layout.addView(ll);
        }
        LinearLayout ll = new LinearLayout(this);

        TextView tv = new TextView(this);
        tv.setText("Загружать C");
        ll.addView(tv, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT));

        EditText et = new FilterInteger(this);
        filterArray.add(new FilterDescription("FromNum", "text", et));
        et.setText("1");
        ll.addView(et, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT));


        tv = new TextView(this);
        tv.setText("По");
        ll.addView(tv, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT));

        et = new FilterInteger(this);
        filterArray.add(new FilterDescription("ToNum", "text", et));
        et.setText("100");
        ll.addView(et, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT));
        parent_layout.addView(ll);

    }

    public void init(){

        getSwipeRefresh().setOnPullEventListener(new PullToRefreshBase.OnPullEventListener<ListView>() {
            @Override
            public void onPullEvent(PullToRefreshBase refreshView, PullToRefreshBase.State state, PullToRefreshBase.Mode direction) {
                // с такими флагами будет вызвана только когда сам потянул и отпустил
                if (state == PullToRefreshBase.State.REFRESHING && direction == PullToRefreshBase.Mode.PULL_FROM_START)
                    request(REQUEST_FROM_LV);
                Log.i(DEBUG_TAG, "onPullEvent " + state.toString() + " " + direction.toString());
            }
        });

        registerForContextMenu(getSwipeRefresh().getRefreshableView());

        refreshButton = (ImageButton) findViewById(R.id.refreshButton);
        if (refreshButton != null) {
            refreshButton.setOnClickListener(this);
        }

        slidingUpPanel = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        if (slidingUpPanel != null){
            slidingUpPanel.setShadowDrawable(getResources().getDrawable(R.drawable.above_shadow));
            slidingUpPanel.setAnchorPoint(0.2f);

            slidingUpPanel.setPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
                @Override
                public void onPanelSlide(View panel, float slideOffset) {
                    Log.i(DEBUG_TAG, "onPanelSlide, offset " + slideOffset);
                    // пока закоментарил так как не показывает содержимое грида, потом разберусь.
/*
                if (slideOffset < 0.15) {
                    if (getActionBar().isShowing()) {
                        getActionBar().hide();
                    }
                } else {
                    if (!getActionBar().isShowing()) {
                        getActionBar().show();
                    }
                }*/
                }

                @Override
                public void onPanelExpanded(View panel) {
                    Log.i(DEBUG_TAG, "onPanelExpanded");
                }

                @Override
                public void onPanelCollapsed(View panel) {
                    Log.i(DEBUG_TAG, "onPanelCollapsed");
                    slidingUpPanel.invalidate();
                }

                @Override
                public void onPanelAnchored(View panel) {
                    Log.i(DEBUG_TAG, "onPanelAnchored");

                }
            });
        }

        setDefaultFilter();

    }

    protected void setDefaultFilter() {
    }

    protected void afterInit() {
        // после всех инициализаций загружаем данные из базы
        if (!getIntent().getBooleanExtra("fromNotify", false)){
            refresh(null);
        }
    }

    public void refresh(Request request){
        // срабатывает когда необходимо запросить из базы
        lastRequest = request;
    }

    public void afterRefresh(){
        // срабатывает поcле обновления
        clearArgument();
    }

    public PullToRefreshBase getSwipeRefresh() {
        if (swipeRefresh == null) {
            swipeRefresh = (PullToRefreshListView) findViewById(R.id.SwipeRefresh);
            setDefaultParamGrid(swipeRefresh);
        }
        return swipeRefresh;
    }

    public void setDefaultParamGrid(PullToRefreshAdapterViewBase swipeRefresh) {
        setEmptyViewGrid(swipeRefresh);
        swipeRefresh.setShowIndicator(true);
    }

    public void setEmptyViewGrid(PullToRefreshAdapterViewBase swipeRefresh) {
        if (swipeRefresh.getRefreshableView() instanceof AdapterView) {
            View v = getLayoutInflater().inflate(R.layout.empty_view, null);
            ((AdapterView) swipeRefresh.getRefreshableView()).setEmptyView(v);
        }
    }

    protected Request request(Integer code){
        // срабатывает когда необходимо послать сетевой запрос
        // и сохранить результаты в базу
        // code = 0: Запрос пришел от ListView, когда потянули и отпустили
        // 1: от кнопки фильтра
        // 2: от запроса из базы когда делали запрос ничего не вернулось идет запрос к серверу
        fillFilter();
        Request request = getInternalRequest(code);
        request.put("code", code);
        return request;
    }

    protected Request getInternalRequest(Integer code){
        return null;
    }

    private void fillFilter() {
        Integer index = 0;
        mRequest = new String[filterArray.getCount()];
        mRequestArg = new String[mRequest.length];
        for (int i=0; i<filterArray.size(); i++){
            if (!filterArray.get(i).isEmpty()){
                mRequest[index] = filterArray.get(i).name;
                mRequestArg[index] = filterArray.get(i).getFilterValue();
                index++;
            }
        }
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
        swapData(loader, data);
        afterRefresh();
    }

    protected void swapData(Loader<Cursor> loader, Cursor data) {
        if (adapter != null)
            adapter.swapCursor(data);
        if (!RequestFactory.isUpdateFlag(lastRequest) && (data != null) && (data.getCount() == 0))
            request(REQUEST_FROM_DB_REFRESH);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // при отмене запроса обнуляем адаптеру курсор
        if (adapter != null)
            adapter.swapCursor(null);
    }

    protected Object getValue(Cursor data, String columnName) {

        int columnIndex = data.getColumnIndex(columnName);

        switch ( data.getType(columnIndex) ){
            case Cursor.FIELD_TYPE_INTEGER:
                return data.getInt(columnIndex);
            case Cursor.FIELD_TYPE_FLOAT:
                return data.getFloat(columnIndex);
            case Cursor.FIELD_TYPE_STRING:
                return data.getString(columnIndex);
            case Cursor.FIELD_TYPE_BLOB:
                return data.getBlob(columnIndex);
            default:
                return null;
        }
    }

    /*
    * обработка событий сетевого запроса
    * */
    @Override
    public void onRequestFinished(Request request, Bundle resultData) {
        lastRequest = null;
        if (RequestFactory.isUpdateFlag(request))
            refresh(request);
    }

    /*
    * обработка нажатия кнопки
    * */
    @Override
    public void onClick(View v) {
        // кнопка на панели фильтра
        if (v.getId() == R.id.refreshButton){
            request(REQUEST_FROM_FILTER);
        }
    }

    @Override
    public void onItemsSelected(boolean[] selected) {

    }
}

