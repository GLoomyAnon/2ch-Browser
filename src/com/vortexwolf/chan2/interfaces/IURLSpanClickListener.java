package com.vortexwolf.chan2.interfaces;

import com.vortexwolf.chan2.common.controls.ClickableURLSpan;

import android.view.View;

public interface IURLSpanClickListener {
    void onClick(View v, ClickableURLSpan span, String url);
}
