package ru.vkb.common;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.CursorWrapper;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.Arrays;

import ru.vkb.model.FilterValue;

/**
 * Created by Zamuraev_av on 04.07.2014.
 */
public class MultiSpinner extends Spinner implements
        DialogInterface.OnMultiChoiceClickListener, DialogInterface.OnCancelListener, FilterValue {

    protected String Name;

    private SimpleCursorAdapter adapter;
    private boolean[] selected;
    private final String AllSelectText = "Все";
    private final String NothingSelText = "Пусто";
    private MultiSpinnerListener listener;
    private Integer nameIndex;
    public Integer[] IDs;

    public MultiSpinner(Context context) {
        super(context);
    }

    public MultiSpinner(Context arg0, AttributeSet arg1) {
        super(arg0, arg1);
    }

    public MultiSpinner(Context arg0, AttributeSet arg1, int arg2) {
        super(arg0, arg1, arg2);
    }

    @Override
    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
        if (isChecked)
            selected[which] = true;
        else
            selected[which] = false;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        // refresh text on spinner
        StringBuilder spinnerBuffer = new StringBuilder();
        Integer Index = 0;
        IDs = new Integer[getItemsCount()];
        boolean someUnselected = false;
        for (int i = 0; i < adapter.getCount(); i++) {
            if (selected[i]) {
                spinnerBuffer.append(((CursorWrapper) adapter.getItem(i)).getString(nameIndex));
                IDs[Index++] = ((CursorWrapper) adapter.getItem(i)).getInt(adapter.getCursor().getColumnIndex("_id"));
                spinnerBuffer.append(",");
            } else {
                someUnselected = true;
            }
        }
        String spinnerText;
        if (someUnselected) {
            spinnerText = spinnerBuffer.toString();
            if (spinnerText.length() > 2)
                spinnerText = spinnerText.substring(0, spinnerText.length() - 2);
            if (spinnerText.isEmpty())
                spinnerText = NothingSelText;
        } else {
            spinnerText = AllSelectText;
        }
        ArrayAdapter<String> ad = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item,
                new String[] { spinnerText });
        setAdapter(ad);
        listener.onItemsSelected(selected);
    }

    private int getItemsCount() {
        Integer count = 0;
        for (int i=0; i<selected.length;i++){
            if (selected[i])
                count++;
        }
        return count;
    }

    @Override
    public boolean performClick() {
        String[]  entries;
        entries = utils.cursorToArray(adapter.getCursor(), nameIndex);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMultiChoiceItems(
                entries, selected, this);
        builder.setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        builder.setOnCancelListener(this);
        builder.show();
        return true;
    }

    public void setItems(SimpleCursorAdapter adapter, Integer NameIndex, MultiSpinnerListener listener) {
        this.adapter = adapter;
        this.listener = listener;
        this.nameIndex = NameIndex;

        // all selected by default
        selected = new boolean[adapter.getCount()];
        for (int i = 0; i < selected.length; i++)
            selected[i] = false;

        // all text on the spinner
        ArrayAdapter<String> ad = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item, new String[] { NothingSelText });
        setAdapter(ad);
    }

    @Override
    public String getValue() {
        return Arrays.toString(IDs);
    }

    @Override
    public Boolean isEmpty() {
        return (IDs == null) || (IDs.length == 0);
    }

    @Override
    public String getName() {
        return Name;
    }

    @Override
    public void setName(String name) {
        this.Name = name;
    }

    public interface MultiSpinnerListener {
        public void onItemsSelected(boolean[] selected);
    }
}