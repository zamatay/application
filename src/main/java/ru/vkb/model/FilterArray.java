package ru.vkb.model;

import java.util.ArrayList;

/**
 * Created by Zamuraev_av on 04.08.2014.
 */
public class FilterArray<E> extends ArrayList<E> {
    public Integer getCount() {
        Integer count = 0;
        for (int i=0; i<size(); i++){
            if (!((FilterDescription) get(i)).isEmpty()) count++;
        }
        return count;
    }
}
