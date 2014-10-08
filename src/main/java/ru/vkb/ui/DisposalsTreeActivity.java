package ru.vkb.ui;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.SimpleCursorTreeAdapter;
import android.widget.Toast;

import com.foxykeep.datadroid.requestmanager.Request;
import com.foxykeep.datadroid.requestmanager.RequestManager;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshExpandableListView;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Arrays;

import ru.vkb.model.BaseCursorTreeAdapter;
import ru.vkb.model.RequestFactory;
import ru.vkb.model.RestRequestManager;
import ru.vkb.model.provider.Contract;
import ru.vkb.model.service.notificationService;
import ru.vkb.task.R;

import static ru.vkb.model.RequestFactory.REQUEST_DISPOSAL_LIST;
import static ru.vkb.model.RequestFactory.REQUEST_STAFF;
import static ru.vkb.model.RequestFactory.getRequestByParam;
import static ru.vkb.model.service.RequestListener.getRequestListener;


public class DisposalsTreeActivity extends BaseActivity {

    //protected ListView disposalListView;
    // для под параметров сетевого запроса
    public SimpleCursorTreeAdapter adapter;
    protected PullToRefreshExpandableListView swipeRefresh;
    protected Handler handler;

    private static final int REQUEST_CODE_SHOW_DISPOSAL_INFO = 0x0000f000;

    @Override
    public Uri getUri() {
        return Contract.URI_DISPOSALS;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.disposals_tree_activity);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getSwipeRefresh().setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
            @Override
            public void onChildViewAdded(View parent, View child) {
                Toast.makeText(getApplicationContext(), "onChildViewAdded", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onChildViewRemoved(View parent, View child) {
            }
        });

        getSwipeRefresh().setOnLastItemVisibleListener(new PullToRefreshBase.OnLastItemVisibleListener(){

            @Override
            public void onLastItemVisible() {
                Toast.makeText(getApplicationContext(), "onLastItemVisible", Toast.LENGTH_LONG).show();
            }
        });

        // проверка есть ли фильтр если есть устанавливаем его приходит от сервиса например
        String filter = getIntent().getStringExtra("filter");
        if (filter != null & getIntent().getBooleanExtra("fromNotify", false)){
            try {
                setFilterString(filter);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void init() {
        super.init();

        createAdapter();
    }

    private void createAdapter() {
        adapter = new DisposalSimpleTreeCursorAdapter(DisposalsTreeActivity.this, null,
                R.layout.disposal_tree_group_item,
                new String[]{"userName"},
                new int[]{R.id.receiver_value},
                R.layout.disposal_tree_item,
                new String[]{"number", "sender_id", "theme", "shortTask"},
                new int[]{R.id.number_value, R.id.sender_value, R.id.theme, R.id.task}
        );

        getSwipeRefresh().setAdapter(adapter);

        getSwipeRefresh().getRefreshableView().setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

                Cursor data = adapter.getChild(groupPosition, childPosition);
                String text = data.getString(data.getColumnIndex("task"));
                String number = data.getString(data.getColumnIndex("number"));
                // помечаем задачу прочитанной
                markTaskRead(id, groupPosition, childPosition);

                Intent filter = new Intent("ru.vkb.intent.action.SHOW_DISPOSALS_INFO");
                filter.putExtra("text", text);
                filter.putExtra("number", number);
                filter.putExtra("id", id);
                // запускаем activity с показом задачи
                startActivityForResult(filter, REQUEST_CODE_SHOW_DISPOSAL_INFO);
                return false;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            // после закрытия окна с показом задачи определяем что мы там нажали и выполняем соответствующую команду
            case REQUEST_CODE_SHOW_DISPOSAL_INFO:
                if (resultCode == RESULT_OK){
                    int result = data.getIntExtra("result", -1);
                    Long disposal_id = data.getLongExtra("id", -1);
                    switch (result){
                        case R.id.disposal_info_comment:
                            loadComments(disposal_id.intValue());
                            break;
                        case R.id.disposal_info_execute:
                            requestExecTask(disposal_id.intValue());
                            break;
                    }
                }
            break;

        }
    }

    private void markTaskRead(long id, int groupPosition, int childPosition) {
        // посылаем на сервер что посмотрели задачу
        Integer value = (Integer) getChildValue(groupPosition, childPosition, "readed");
        if (value == 0) {
            Request request = getRequestByParam(-1, "SetTaskRead", new String[]{"id"}, new String[]{Long.toString(id)});
            RestRequestManager.from(this).execute(request, null);
            ContentValues cv = new ContentValues();
            cv.put("readed", "1");
            // апдейтим в таблице
            getContentResolver().update(Contract.URI_DISPOSALS_CHILD, cv, "_ID = ?", new String[]{Long.toString(id)});
        }
    }

    @Override
    public void refresh(Request request) {
        super.refresh(request);
        refreshDisposalsGroup();
    }

    public Request requestDisposals(String[] paramStr, String[] paramArg){
        Request disposal_list_request = getRequestByParam(REQUEST_DISPOSAL_LIST, "getDisposalList", paramStr, paramArg);
        RequestFactory.setUpdateFlag(disposal_list_request);
        RestRequestManager.from(this).execute(disposal_list_request, getRequestListener(this));
        return disposal_list_request;
    }

    public Request requestDisposals(Request request){
        RestRequestManager.from(this).execute(request, getRequestListener(this));
        return request;
    }

    @Override
    public void afterRefresh() {
        super.afterRefresh();
    }

    public PullToRefreshExpandableListView getSwipeRefresh() {
        if (swipeRefresh  == null){
            swipeRefresh = (PullToRefreshExpandableListView) findViewById(R.id.SwipeRefresh);
            setDefaultParamGrid(swipeRefresh);
        }
        return swipeRefresh;
    }

    @Override
    protected Request getInternalRequest(Integer code) {
        return requestDisposals(mRequest, mRequestArg);
    }

    public void requestStaff(String[] paramStr, String[] paramArg){
        // запрос необходимого персонала
        Request disposal_list_request = getRequestByParam(REQUEST_STAFF, "getStaff", paramStr, paramArg);
        RequestFactory.setResyncFlag(disposal_list_request);
        RestRequestManager.from(this).execute(disposal_list_request, new RequestManager.RequestListener() {
            @Override
            public void onRequestFinished(Request request, Bundle resultData) {
                // после загрузки делаем обновление
                refresh(request);
            }

            @Override
            public void onRequestConnectionError(Request request, int statusCode) {

            }

            @Override
            public void onRequestDataError(Request request) {

            }

            @Override
            public void onRequestCustomError(Request request, Bundle resultData) {

            }
        });
    }

    @Override
    public void onRequestFinished(Request request, Bundle resultData) {

        // проверяем наличие персонала в справочнике по поступившим задачам
        //if (!request.getBoolean("withUpdate"))
        checkStaffOnTable();

        super.onRequestFinished(request, resultData);
    }

    void checkStaffOnTable() {
        // проверяем отсутствующий персонал в таблице
        getSupportLoaderManager().initLoader(0, null, new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                return new CursorLoader(getApplicationContext(), Contract.URI_DISTINCT_STAFF, Contract.PATH.ALL, null, null, null);
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                //  если когото нехватает то загружаем
                if (!data.isClosed() && data.getCount() > 0) {
                    String[] arg = new String[data.getCount()];
                    int Index = 0;
                    data.moveToPosition(-1);
                    while (data.moveToNext()){
                        arg[Index] = Integer.toString(data.getInt(0));
                        Index++;
                    }
                    if (arg.length > 0 & arg.length < 100)
                        requestStaff(new String[]{"id"}, new String[]{Arrays.toString(arg)});
                }
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {
            }
        });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id){
            // курсор группы
            case Contract.ID_DISPOSALS_GROUP:
                return new CursorLoader(getApplicationContext(),
                        Contract.URI_DISPOSALS_GROUP,
                        new String[]{"receiver"},
                        args.getString("Selection"),
                        args.getStringArray("SelectionArg"),
                        null);
            // курсор для подгрупп срабатывает для каждой подгруппы
            default:
                // формируем строку condition
                // есле выбрано что то то добавляем к подгруппе
                String selection = args.getString("Selection");
                if (selection == null)
                    selection = "receiver_id = ?";
                else
                    selection = "disposals." + selection + " and receiver_id = ?";

                // формируем аргументы для этой строки
                // если что то выбрано то копируем массив и добавляем соответственно значение с подгруппы иначе только подгруппа
                String[] selectionArg;
                String[] temp = args.getStringArray("SelectionArg");
                if  (temp == null){
                    // если в селектине ничего нет то просто берем id группы
                    selectionArg = new String[]{Long.toString(args.getLong("idGroup"))};
                } else {
                    // если есть копируем массив и добавляем в конец id группы
                    selectionArg = Arrays.copyOf(temp, temp.length + 1);
                    selectionArg[selectionArg.length - 1] = Long.toString(args.getLong("idGroup"));
                }

                return new CursorLoader(getApplicationContext(),
                        Contract.URI_DISPOSALS_CHILD,
                        Contract.staff.ALL,
                        selection,
                        selectionArg,
                        null);
        }
    }

    @Override
    protected void swapData(Loader<Cursor> loader, Cursor data) {
        // подставляем адаптеру курсор после завершения если курсор пуст делаем сетевой запрос
        if (adapter != null & loader.getId() == Contract.ID_DISPOSALS_GROUP) {
            adapter.setGroupCursor(data);
        } else if (adapter != null & loader.getId() <= 0) {
            adapter.setChildrenCursor(-loader.getId(), data);
        }
    }

    public void refreshDisposalsGroup(){
        refreshByID(Contract.ID_DISPOSALS_GROUP);
    }

    private void setFilterString(String result) throws JSONException {
        // срабатывает при поступление уведомления
        String[] ar = new String[]{Arrays.toString(getParamString(new JSONArray(result)))};
        Request disposal_list_request = getRequestByParam(REQUEST_DISPOSAL_LIST, "getDisposalList",
                new String[]{"id"}, ar);
        RequestFactory.setResyncFlag(disposal_list_request);
        RequestFactory.setUpdateFlag(disposal_list_request);
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
/*
* options menu*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.disposals, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        int id = item.getItemId();
        switch (id){
            case R.id.action_checkRead:
                notificationService.startCheckDisposal(this);
                return true;
            case R.id.action_refresh:
                refresh(null);
                return true;
            default: return false;
        }
    }


/*
    context menu
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.disposals_item_context, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) item.getMenuInfo();
        if (info == null)
            return false;
        int groupPosition = ExpandableListView.getPackedPositionGroup(info.packedPosition);
        int childPosition = ExpandableListView.getPackedPositionChild(info.packedPosition);
        Cursor data = adapter.getChild(groupPosition, childPosition);
        int id = (Integer) getValue(data, "_id");
        switch (item.getItemId()){
            case R.id.action_context_comment:
                loadComments(id);
                return true;
            case R.id.action_context_execute:
                requestExecTask(id);
                return true;
        }
        return  true;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.refreshButton){
            clearArgument();
        }
        super.onClick(v);
    }

    public void loadComments(Integer disposal_id){
        Intent intent = new Intent("ru.vkb.intent.action.SHOW_DISPOSALS_COMMENT");
        intent.putExtra("disposal_id", disposal_id);
        startActivity(intent);
    }

    public void requestExecTask(Integer id){
        RestRequestManager.from(this).execute(getRequestByParam(-1, "ExecuteDisposal", new String[]{"disposal_id"}, new String[]{id.toString()}), getRequestListener(this));
    }

    public Object getChildValue(int groupPosition, int childPosition, String columnName) {
        Cursor data = adapter.getChild(groupPosition, childPosition);

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
}

class DisposalSimpleTreeCursorAdapter extends BaseCursorTreeAdapter {

    public DisposalSimpleTreeCursorAdapter(Context context, Cursor cursor, int groupLayout, String[] groupFrom, int[] groupTo, int childLayout, String[] childFrom, int[] childTo) {
        super(context, cursor, groupLayout, groupFrom, groupTo, childLayout, childFrom, childTo);
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        View v =  super.getChildView(groupPosition, childPosition, isLastChild, convertView, parent);

        ImageView ib_context;
        ib_context = (ImageView) v.findViewById(R.id.imageButton_context);

        ib_context.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewGroup vg = (ViewGroup) v.getParent();
                vg.showContextMenuForChild(v);
            }
        });

        Integer isRead = 1;
        Integer isExecute = 0;

        isRead = (Integer) ((DisposalsTreeActivity) mContext).getChildValue(groupPosition, childPosition, "readed");
        isExecute = (Integer) ((DisposalsTreeActivity) mContext).getChildValue(groupPosition, childPosition, "isExecute");

        if (isRead == 0) {
            v.setBackgroundColor(mContext.getResources().getColor(android.R.color.holo_orange_light));
        }
        else if (isExecute == 1){
            v.setBackgroundColor(mContext.getResources().getColor(android.R.color.holo_green_dark));
        }
        else {
            v.setBackgroundColor(mContext.getResources().getColor(android.R.color.white));
        }
        return v;
    }

    @Override
    protected Cursor getChildrenCursor(Cursor groupCursor) {
        final long groupId = groupCursor.getLong(groupCursor.getColumnIndex("_id"));
        Bundle bundle = new Bundle();
        int groupPos = groupCursor.getPosition();
        bundle.putLong("idGroup", groupId);
        bundle.putInt("groupPos", groupPos);

        Log.d(((DisposalsTreeActivity) mContext).DEBUG_TAG, "getChildrenCursor() for groupPos " + groupPos);
        Log.d(((DisposalsTreeActivity) mContext).DEBUG_TAG, "getChildrenCursor() for groupId " + groupId);

        ((DisposalsTreeActivity) mContext).refreshByID(-groupPos, bundle);

        return  null;
    }
}
