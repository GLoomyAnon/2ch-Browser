package com.vortexwolf.chan.models.presentation;

import android.content.Context;
import android.media.AudioManager;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.vortexwolf.chan.R;
import com.vortexwolf.chan.common.controls.WebViewFixed;
import com.vortexwolf.chan.services.TimerService;

public class GalleryItemViewBag {
    public FrameLayout layout;
    public View loading;
    public View error;
    public TimerService timer;

    public void switchToErrorView(String errorMessage) {
        this.layout.setVisibility(View.GONE);
        this.loading.setVisibility(View.GONE);
        this.error.setVisibility(View.VISIBLE);

        TextView errorTextView = (TextView) this.error.findViewById(R.id.error_text);
        errorTextView.setText(errorMessage);
    }

    public void clear() {
        if (layout.getChildAt(0) instanceof WebViewFixed) {
            WebViewFixed webView = (WebViewFixed)layout.getChildAt(0);
            webView.loadUrl("about:blank");
        }

        layout.removeAllViews();

        if (timer != null) {
            timer.stop();
        }
    }
}
