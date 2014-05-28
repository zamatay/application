package ru.vkb.application;


import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import ru.vkb.common.utils;
import ru.vkb.model.RequestFactory;
import ru.vkb.model.RestRequestManager;
import ru.vkb.model.provider.Contract;

import static ru.vkb.model.RequestFactory.REQUEST_SEND_COMMENT;
import static ru.vkb.model.RequestFactory.getRequestByParam;


public class DisposalNoteActivity extends BaseActivity{

    public Integer disposalID;
    public ListView note_list;
    protected ImageView btn;
    protected EditText comment_text;

    @Override
    public void init() {
        disposalID = getIntent().getIntExtra("disposal_id", -1);
        super.init();
        note_list = (ListView) findViewById(R.id.disposals_note_list);
        adapter = new SimpleCursorAdapter(DisposalNoteActivity.this, R.layout.disposal_note_item,null,
                new String[]{Contract.disposal_comment.dateCreate, Contract.disposal_comment.userName, Contract.disposal_comment.note_text},
                new int[]{R.id.disposal_note_date_create, R.id.disposal_note_user_name, R.id.disposal_note_text},0);
        note_list.setAdapter(adapter);
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
                startRequest();
                String comment = comment_text.getText().toString();
                comment = utils.Encoding(comment);
                // отправляем на сервер коммент
                RestRequestManager.from(this).execute(getRequestByParam(REQUEST_SEND_COMMENT, "SendComment", new String[]{"disposal_id", "comment"}, new String[]{disposalID.toString(), comment}), this);
                // очищаем текст
                comment_text.setText("");
                // прячем клавиатуру
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(comment_text.getWindowToken(), 0);
            }
        }
    }

    @Override
    public void refresh() {
        super.refresh();
        refreshDisposalsNote();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.disposal_note_activity);
    }


    public void refreshDisposalsNote(){
        if (getSupportLoaderManager().getLoader(Contract.disposal_comment.PATH) == null)
            getSupportLoaderManager().initLoader(Contract.disposal_comment.PATH, null, this);
        else
            getSupportLoaderManager().restartLoader(Contract.disposal_comment.PATH, null, this);
    }

    public void requestDisposalsNote(){
        RestRequestManager.from(this).execute(RequestFactory.getDisposalNote(disposalID), this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.disposal_comment, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        super.onCreateLoader(id, args);
        return new CursorLoader(this, Contract.disposal_comment.CONTENT_URI,Contract.disposal_comment.PROJECTION,"disposal_id=?",new String[]{disposalID.toString()},null);
    }

    @Override
    protected void request() {
        super.request();
        requestDisposalsNote();
    }
}
