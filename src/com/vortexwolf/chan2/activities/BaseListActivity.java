package com.vortexwolf.chan2.activities;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.vortexwolf.chan2.R;
import com.vortexwolf.chan2.asynctasks.CheckCloudflareTask;
import com.vortexwolf.chan2.asynctasks.DisplayImageUriTask;
import com.vortexwolf.chan2.common.Factory;
import com.vortexwolf.chan2.common.MainApplication;
import com.vortexwolf.chan2.common.utils.AppearanceUtils;
import com.vortexwolf.chan2.interfaces.ICheckCaptchaView;
import com.vortexwolf.chan2.interfaces.IWebsite;
import com.vortexwolf.chan2.models.domain.CaptchaEntity;
import com.vortexwolf.chan2.settings.ApplicationSettings;
import com.vortexwolf.chan2.settings.SettingsEntity;

public abstract class BaseListActivity extends ListActivity implements SwipeRefreshLayout.OnRefreshListener {

    private enum ViewType {
        LIST, LOADING, ERROR, CAPTCHA
    };

    private View mLoadingView = null;
    private View mErrorView = null;
    private View mCaptchaView = null;
    private SwipeRefreshLayout mRefreshView = null;
    private ViewType mCurrentView = null;
    private Button mCaptchaSendButton;
    private CheckCloudflareTask mCurrentCheckTask = null;
    private final ApplicationSettings mSettings = Factory.resolve(ApplicationSettings.class);
    private SettingsEntity settingsEntity = mSettings.getCurrentSettings();

    protected boolean mVisible = false;

    @Override
    public void onRefresh() {
        BaseListActivity.this.refresh();
    }

    protected void hideRefreshView() {
        mRefreshView.setRefreshing(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_PROGRESS);
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.mVisible = false;
    }

    @Override
    protected void onResume() {

        if(settingsEntity.compareTo(mSettings.getCurrentSettings()) != 0){
            //settings has been changed
            settingsEntity = mSettings.getCurrentSettings();
            resetUI();
        }

        super.onResume();
        this.mVisible = true;
    }

    /** Returns the layout resource Id associated with this activity */
    protected abstract int getLayoutId();

    protected abstract void refresh();

    /** Reloads UI on the page */
    protected void resetUI() {
        // setting of the theme goes first
        this.setTheme(mSettings.getTheme());

        // completely reload the root view, get loading and error views
        this.setContentView(this.getLayoutId());
        this.mLoadingView = this.findViewById(R.id.loadingView);
        this.mErrorView = this.findViewById(R.id.error);
        this.mCaptchaView = this.findViewById(R.id.captchaView);

        this.mRefreshView = (SwipeRefreshLayout) this.findViewById(R.id.refreshView);
        this.mRefreshView.setOnRefreshListener(this);
        this.mRefreshView.setEnabled(mSettings.isSwipeToRefresh());

        this.switchToView(this.mCurrentView);
    }

    /** Returns the main class of the application */
    protected MainApplication getMainApplication() {
        return (MainApplication) super.getApplication();
    }

    /** Shows the loading indicator */
    protected void switchToLoadingView() {
        this.switchToView(ViewType.LOADING);
    }

    /** Shows the normal list */
    protected void switchToListView() {
        this.switchToView(ViewType.LIST);
    }

    /** Shows the error message */
    protected void switchToErrorView(String message) {
        this.switchToView(ViewType.ERROR);

        TextView errorTextView = (TextView) this.mErrorView.findViewById(R.id.error_text);
        errorTextView.setText(message != null ? message : this.getString(R.string.error_unknown));
    }

    protected void switchToCaptchaView(final IWebsite website, final CaptchaEntity captcha) {
        if (this.mCaptchaView == null) {
            this.switchToErrorView("The captcha view is not designed yet.");
            return;
        }

        this.switchToView(ViewType.CAPTCHA);

        ImageView captchaImage = (ImageView) this.mCaptchaView.findViewById(R.id.cloudflare_captcha_image);
        final EditText captchaAnswer = (EditText) this.mCaptchaView.findViewById(R.id.cloudflare_captcha_input);
        this.mCaptchaSendButton = (Button) this.mCaptchaView.findViewById(R.id.cloudflare_send_button);

        captchaAnswer.setText("");
        captchaAnswer.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(captchaAnswer.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);

                    if (mCurrentCheckTask != null) {
                        mCurrentCheckTask.cancel(true);
                    }

                    mCurrentCheckTask = new CheckCloudflareTask(website, captcha, captchaAnswer.getText().toString(), new CheckCaptchaView());
                    mCurrentCheckTask.execute();
                    return true;
                }
                return false;
            }
        });

        this.mCaptchaSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrentCheckTask != null) {
                    mCurrentCheckTask.cancel(true);
                }

                mCurrentCheckTask = new CheckCloudflareTask(website, captcha, captchaAnswer.getText().toString(), new CheckCaptchaView());
                mCurrentCheckTask.execute();
            }
        });

        // display captcha image
        DisplayImageUriTask task = new DisplayImageUriTask(captcha.getUrl(), captchaImage);
        task.execute();
    }

    protected void showToastIfVisible(String message) {
        if (this.mVisible) {
            AppearanceUtils.showToastMessage(this, message);
        }
    }

    /** Switches the page between the list view, loading view and error view */
    private void switchToView(ViewType vt) {
        this.mCurrentView = vt;

        if (vt == null) {
            return;
        }

        switch (vt) {
            case LIST:
                this.getListView().setVisibility(View.VISIBLE);
                this.mLoadingView.setVisibility(View.GONE);
                this.mErrorView.setVisibility(View.GONE);
                this.setCaptchaViewVisibility(View.GONE);
                this.mRefreshView.setRefreshing(false);
                break;
            case LOADING:
                this.getListView().setVisibility(View.GONE);
                this.mLoadingView.setVisibility(View.VISIBLE);
                this.mErrorView.setVisibility(View.GONE);
                this.setCaptchaViewVisibility(View.GONE);
                this.mRefreshView.setRefreshing(false);
                break;
            case ERROR:
                this.getListView().setVisibility(View.GONE);
                this.mLoadingView.setVisibility(View.GONE);
                this.mErrorView.setVisibility(View.VISIBLE);
                this.setCaptchaViewVisibility(View.GONE);
                this.mRefreshView.setRefreshing(false);
                break;
            case CAPTCHA:
                this.getListView().setVisibility(View.GONE);
                this.mLoadingView.setVisibility(View.GONE);
                this.mErrorView.setVisibility(View.GONE);
                this.setCaptchaViewVisibility(View.VISIBLE);
                this.mRefreshView.setRefreshing(false);
                break;
        }
    }

    private void setCaptchaViewVisibility(int visibility) {
        if (this.mCaptchaView != null) {
            this.mCaptchaView.setVisibility(visibility);
        }
    }

    private class CheckCaptchaView implements ICheckCaptchaView {
        @Override
        public void beforeCheck() {
            mCaptchaSendButton.setEnabled(false);
        }

        @Override
        public void showSuccess() {
            mCaptchaSendButton.setEnabled(true);
            BaseListActivity.this.refresh();
        }

        @Override
        public void showError(String message) {
            message = message != null ? message : "Incorrect captcha.";
            AppearanceUtils.showToastMessage(getApplicationContext(), message);

            mCaptchaSendButton.setEnabled(true);
            BaseListActivity.this.refresh();
        }
    }
}
