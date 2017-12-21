package com.scn.ui;

/**
 * Created by imurvai on 2017-12-21.
 */

public interface OnListItemClickListener<T> {

    public enum ItemClickAction {
        CLICK,
        REMOVE,
        EDIT
    }

    void onClick(T item, ItemClickAction itemClickAction, Object data);
}
