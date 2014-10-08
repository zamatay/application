package ru.vkb.ui.filter;

import android.content.Context;

/**
 * Created by Zamuraev_av on 12.09.2014.
 */
public class FilterInteger extends FilterEditText {
    public FilterInteger(Context context) {
        super(context);
        setInputType(8192);
    }

    @Override
    public String getValue() {
        return getText().toString();
    }
}
