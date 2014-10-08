package ru.vkb.model;

import android.view.View;

/**
 * Created by Zamuraev_av on 22.07.2014.
 */
public class FilterDescription {
    public String type;
    public View view_1;
    public View view_2;
    public String name;
    protected Boolean isTwoValue = false;

    public FilterDescription(String name, String type, View view){
        this.type = type;
        this.name = name;
        this.view_1 = view;
    }
    public FilterDescription(String name, String type, View view_1, View view_2){
        this.type = type;
        this.name = name;
        this.view_1 = view_1;
        this.view_2 = view_2;
        isTwoValue = true;
    }

    public Boolean isEmpty(){
        if (!isTwoValue)
            return ((FilterValue) view_1).isEmpty();
        else
            return ((FilterValue) view_1).isEmpty() & ((FilterValue) view_2).isEmpty();
    }

    public String getFilterValue() {
        /*
        JSONObject jo = new JSONObject();
        try {
            jo.put("name", ((FilterValue) view_1).getValue());
            if (isTwoValue){
                jo.put("name", ((FilterValue) view_2).getValue());
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jo.toString();
        */
        return ((FilterValue) view_1).getValue();
    }
}

