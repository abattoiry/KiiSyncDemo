package com.kii.sync;

import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.ToggleButton;

/**
 * Created by yue on 15/7/23.
 */
public class CardItemViewHolder {

    public static final int layout_id = R.layout.card_item;

    public TextView title;

    public TextView color;

    public TextView brightness;

    public CardItemViewHolder(View view) {
        super();
        title = (TextView) view.findViewById(R.id.title);
        color = (TextView) view.findViewById(R.id.color);
        brightness = (TextView) view.findViewById(R.id.brightness);
    }

}
