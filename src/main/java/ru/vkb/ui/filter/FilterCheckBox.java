package ru.vkb.ui.filter;

import android.content.Context;
import android.widget.CheckBox;

import ru.vkb.model.FilterValue;

/**
 * Created by Zamuraev_av on 22.07.2014.
 */
public class FilterCheckBox extends CheckBox implements FilterValue {
    protected String Name;

    public FilterCheckBox(Context context) {
        super(context);
    }

    @Override
    public String getValue() {
        return isChecked() ? "1" : "0";
    }

    @Override
    public Boolean isEmpty() {
        return !isChecked();
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
