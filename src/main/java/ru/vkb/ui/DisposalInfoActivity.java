package ru.vkb.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import ru.vkb.task.R;


public class DisposalInfoActivity extends Activity implements View.OnClickListener {

    private View button_execute;
    private View button_comment;
    private Long disposal_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.disposal_info_activity);
        button_execute = (View) findViewById(R.id.disposal_info_comment);
        button_execute.setOnClickListener(this);
        button_comment = (View) findViewById(R.id.disposal_info_execute);
        button_comment.setOnClickListener(this);
        Intent intent = getIntent();
        TextView tv = (TextView) findViewById(R.id.disposal_text);
        disposal_id = intent.getLongExtra("id", -1);
        tv.setText(intent.getStringExtra("text"));
        setTitle(getString(R.string.taskNumber) + " " + intent.getStringExtra("number"));
    }


    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        intent.putExtra("result", v.getId());
        intent.putExtra("id", disposal_id);
        setResult(RESULT_OK, intent);
        finish();
    }
}
