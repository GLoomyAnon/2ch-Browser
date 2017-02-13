package com.vortexwolf.chan2.common.controls;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.vortexwolf.chan2.services.presentation.ClickListenersFactory;

public class ExtendedImageView extends ImageView {

    public ExtendedImageView(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.setOnLongClickListener(ClickListenersFactory.sIgnoreOnLongClickListener);
    }
}
