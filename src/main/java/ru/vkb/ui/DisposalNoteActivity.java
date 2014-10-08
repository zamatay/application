package ru.vkb.ui;


import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import com.foxykeep.datadroid.requestmanager.Request;

import ru.vkb.task.R;
import ru.vkb.model.RequestFactory;
import ru.vkb.model.RestRequestManager;
import ru.vkb.model.provider.Contract;

import static ru.vkb.model.RequestFactory.REQUEST_SEND_COMMENT;
import static ru.vkb.model.RequestFactory.getRequestByParam;
import static ru.vkb.model.service.RequestListener.getRequestListener;


public class DisposalNoteActivity extends BaseActivity{

    public Integer disposalID;
    //public ListView note_list;
    protected ImageView btn;
    protected EditText comment_text;

    @Override
    public void init() {
        disposalID = getIntent().getIntExtra("disposal_id", -1);
        super.init();
        //note_list = (ListView) findViewById(R.id.disposals_note_list);
        adapter = new SimpleCursorAdapter(DisposalNoteActivity.this, R.layout.disposal_note_item,null,
                new String[]{"dateCreate", "userName", "note_text"},
                new int[]{R.id.disposal_note_date_create, R.id.disposal_note_user_name, R.id.disposal_note_text},0);
        swipeRefresh.setAdapter(adapter);
        btn = (ImageView) findViewById(R.id.topmenu_rightBtn);
        btn.setOnClickListener(this);

        comment_text = (EditText) findViewById(R.id.comment_text);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        if (v.getId() == btn.getId()){
            // кнопка отправить комментарий
            if (comment_text.getText().toString() != "") {
                // запускаем обновление UI
                //startRequest();
                String comment = comment_text.getText().toString();
                //comment = utils.Encoding(comment);
                // отправляем на сервер коммент
                RestRequestManager.from(this).execute(getRequestByParam(REQUEST_SEND_COMMENT, "SendComment", new String[]{"disposal_id", "comment"}, new String[]{disposalID.toString(), comment}), getRequestListener(DisposalNoteActivity.this));
                // очищаем текст
                comment_text.setText("");
                // прячем клавиатуру
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(comment_text.getWindowToken(), 0);
            }
        }
    }

    @Override
    public void refresh(Request request) {
        super.refresh(request);
        refreshDisposalsNote();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.disposal_note_activity);
    }

    @Override
    public Uri getUri() {
        return Contract.URI_DISPOSALS_COMMENT;
    }

    public void refreshDisposalsNote(){
        if (getSupportLoaderManager().getLoader(Contract.ContractFactory.DisposalsComment.getID()) == null)
            getSupportLoaderManager().initLoader(Contract.ContractFactory.DisposalsComment.getID(), null, this);
        else
            getSupportLoaderManager().restartLoader(Contract.ContractFactory.DisposalsComment.getID(), null, this);
    }

    public Request requestDisposalsNote(Integer code){
        Request request = RequestFactory.getDisposalNote(disposalID);
        RestRequestManager.from(this).execute(request, getRequestListener(this));
        return request;
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        super.onCreateLoader(id, args);
        return new CursorLoader(
                this,
                Contract.URI_DISPOSALS_COMMENT,
                Contract.ContractFactory.DisposalsComment.getColumnsName(),"disposal_id=?",
                new String[]{disposalID.toString()},
                "dateCreate desc");
    }

    @Override
    protected Request getInternalRequest(Integer code) {
        return requestDisposalsNote(code);
    }
}
