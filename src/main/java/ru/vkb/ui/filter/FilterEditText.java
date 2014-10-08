package ru.vkb.ui.filter;

import android.content.Context;
import android.widget.EditText;

import ru.vkb.model.FilterValue;

/**
 * Created by Zamuraev_av on 22.07.2014.
 */
public class FilterEditText extends EditText implements FilterValue {
    protected String Name;

    public FilterEditText(Context context) {
        super(context);
    }

    @Override
    public String getValue() {
        return "%" + getText().toString() + "%";
    }

    @Override
    public Boolean isEmpty() {
        return this.getText().length() == 0;
    }

    @Override
    public String getName() {
        return Name;
    }

    @Override
    public void setName(String name) {
        this.Name = name;
    }
}
