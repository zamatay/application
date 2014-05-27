package ru.vkb.application;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.foxykeep.datadroid.requestmanager.Request;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Arrays;

import ru.vkb.application.preference.SettingsActivity;
import ru.vkb.model.RestRequestManager;
import ru.vkb.model.provider.Contract;
import ru.vkb.model.service.notificationService;

import static ru.vkb.application.R.color.GREEN;
import static ru.vkb.application.R.color.WHITE;
import static ru.vkb.model.RequestFactory.REQUEST_DISPOSAL_LIST;
import static ru.vkb.model.RequestFactory.getRequestByParam;


public class DisposalsActivity extends BaseActivity {

    protected ListView disposalListView;
    // фильтрация из базы1
    protected String mSelection = null;
    protected String[] mSelectionArg = null;
    // для под параметров сетевого запроса
    protected String[] mRequest = null;
    protected String[] mRequestArg = null;

    @Override
    public void createFilter() {
        super.createFilter();
        LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        TextView tv = new TextView(this);
        LinearLayout ll = (LinearLayout) findViewById(R.id.filter);
        tv.setText("Залупа");
        ll.addView(tv, lParams);
    }

    @Override
    public void init() {
        super.init();
        disposalListView = (ListView) findViewById(R.id.disposalList);

        adapter =  new DisposalSimpleCursorAdapter(DisposalsActivity.this,R.layout.disposal_item,null,
                new String[]{Contract.disposals.PROJECTION[Contract.disposals.number], Contract.disposals.PROJECTION[Contract.disposals.receiver],
                        Contract.disposals.PROJECTION[Contract.disposals.sender], Contract.disposals.PROJECTION[Contract.disposals.theme],
                        Contract.disposals.PROJECTION[Contract.disposals.shortTask]},
                new int[]{R.id.number_value, R.id.receiver_value, R.id.sender_value, R.id.theme, R.id.task},
                0);

        disposalListView.setAdapter(adapter);
        disposalListView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String text = adapter.getCursor().getString(Contract.disposals.task);
                        String number = adapter.getCursor().getString(Contract.disposals.number);
                        String _id = adapter.getCursor().getString(Contract.disposals.id);
                        markTaskRead(_id);
                        Intent filter = new Intent("ru.vkb.intent.action.SHOW_DISPOSALS_INFO");
                        filter.putExtra("text", text);
                        filter.putExtra("number", number);
                        startActivity(filter);
                    }
                }
        );
        mRequest = new String[]{"isExecute"};
        mRequestArg = new String[]{"0"};
    }

    private void markTaskRead(String id) {
        // посылаем на сервер что посмотрели задачу
        Integer value = adapter.getCursor().getInt(Contract.disposals.readed);
        if (value.compareTo(0) == 0) {
            Request request = getRequestByParam(-1, "SetTaskRead", new String[]{"id"}, new String[]{id.toString()});
            RestRequestManager.from(DisposalsActivity.this).execute(request, null);
            ContentValues cv = new ContentValues();
            cv.put(Contract.disposals.PROJECTION[Contract.disposals.readed], "1");
            // апдейтим в таблице
            getContentResolver().update(Contract.disposals.CONTENT_URI, cv, "_ID = ?", new String[]{id.toString()});
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void refresh() {
        super.refresh();
        refreshDisposals();
    }

    public void requestDisposals(String[] paramStr, String[] paramArg){
        Request disposal_list_request = getRequestByParam(REQUEST_DISPOSAL_LIST, "getDisposalList", paramStr, paramArg);
        RestRequestManager.from(getApplicationContext()).execute(disposal_list_request, this);
    }

    public void requestDisposals(Request request){
        RestRequestManager.from(getApplicationContext()).execute(request, this);
    }

    @Override
    protected void request() {
        super.request();
        requestDisposals(mRequest, mRequestArg);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Loader<Cursor> result = new CursorLoader(getApplicationContext(), Contract.disposals.CONTENT_URI,Contract.disposals.PROJECTION,mSelection,mSelectionArg,null);
        clearArgument();
        return result;
    }

    private void clearArgument() {
        mSelection = null;
        mSelectionArg = null;
    }

    public void refreshDisposals(){
        if (getSupportLoaderManager().getLoader(Contract.disposals.PATH) == null)
            getSupportLoaderManager().initLoader(Contract.disposals.PATH, null, this);
        else
            getSupportLoaderManager().restartLoader(Contract.disposals.PATH, null, this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.disposals_activity);

        String filter = getIntent().getStringExtra("filter");
        if (filter != null & getIntent().getBooleanExtra("fromNotify", false)){
            try {
                setFilterString(filter);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void setFilterString(String result) throws JSONException {
        Request disposal_list_request = getRequestByParam(REQUEST_DISPOSAL_LIST, "getDisposalList",
                new String[]{"id"},
                new String[]{Arrays.toString(getParamString(new JSONArray(result)))});
        disposal_list_request.put("withUpdate", true);
        disposal_list_request.put("resync", true);
        startRequest();
        requestDisposals(disposal_list_request);
        // удаляем что бы при повороте экрана опять сюда не зашло
        getIntent().removeExtra("fromNotify");
        getIntent().removeExtra("filter");
    }


    private String[] getParamString(JSONArray jsonArray) throws JSONException {
        mSelectionArg = new String[jsonArray.length()];
        StringBuilder sb = new StringBuilder("_id IN (");
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONArray row = jsonArray.getJSONArray(i);
            mSelectionArg[i] = row.getString(0);
            sb.append("?");
            if (i < jsonArray.length() - 1)
                sb.append(",");
        }
        sb.append(")");
        mSelection = sb.toString();
        return mSelectionArg;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.a_main, menu);
        super.onCreateOptionsMenu(menu);
        menu.add(0,R.id.action_refresh,0,getString(R.string.refresh));
        menu.add(0, R.id.action_checkRead, 1, getString(R.string.checkRead));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        int id = item.getItemId();
        switch (id){
            case R.id.action_settings: return true;
            case R.id.action_checkRead:
                notificationService.startCheckDisposal(this);
                return true;
            case R.id.action_refresh:
                clearArgument();
                refresh();
                return true;
            default: return true;
        }
    }

    public void loadComments(Integer disposal_id){
        Intent intent = new Intent("ru.vkb.intent.action.SHOW_DISPOSALS_COMMENT");
        intent.putExtra("disposal_id", disposal_id);
        startActivity(intent);
    }

    public void requestExecTask(Integer id){
        startRequest();
        RestRequestManager.from(this).execute(getRequestByParam(-1, "ExecuteDisposal", new String[]{"disposal_id"}, new String[]{id.toString()}), this);
    }

    @Override
    public void onRequestFinished(Request request, Bundle resultData) {
        super.onRequestFinished(request, resultData);
    }
}

class DisposalSimpleCursorAdapter extends SimpleCursorAdapter {
    private DisposalsActivity mContext;

    public DisposalSimpleCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);
        mContext = (DisposalsActivity) context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = super.getView(position, convertView, parent);
        Integer id = getCursor().getColumnIndex(Contract.disposals._ID);

        Button button;

        button = (Button) v.findViewById(R.id.comment);
        button.setTag(getCursor().getInt(id));
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button b = (Button) v.findViewById(R.id.comment);
                mContext.loadComments((Integer) b.getTag());
            }
        });

        button = (Button) v.findViewById(R.id.execute);
        button.setTag(getCursor().getInt(id));
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContext.requestExecTask((Integer) v.getTag());
            }
        });
        int value = getCursor().getInt(Contract.disposals.readed);
        if (value == 0) {
            v.setBackgroundResource(GREEN);
        }
        else {
            v.setBackgroundResource(WHITE);
        }
        return v;
    }


}
