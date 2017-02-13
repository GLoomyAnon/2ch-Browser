package com.vortexwolf.chan2.asynctasks;

import android.os.AsyncTask;

import com.vortexwolf.chan2.common.Factory;
import com.vortexwolf.chan2.interfaces.ICaptchaView;
import com.vortexwolf.chan2.interfaces.IPostSendView;
import com.vortexwolf.chan2.interfaces.IWebsite;
import com.vortexwolf.chan2.models.domain.SendPostModel;
import com.vortexwolf.chan2.models.domain.SendPostResult;
import com.vortexwolf.chan2.services.PostSender;

public class SendPostTask extends AsyncTask<Void, Long, SendPostResult> {

    private final PostSender mPostSender = Factory.resolve(PostSender.class);
    private final IPostSendView mView;
    private final ICaptchaView mCaptchaView;

    private final IWebsite mWebsite;
    private final String mBoardName;
    private final String mThreadNumber;
    private final SendPostModel mEntity;

    public SendPostTask(IPostSendView view, ICaptchaView captchaView, IWebsite website, String boardName, String threadNumber, SendPostModel entity) {
        this.mView = view;
        this.mCaptchaView = captchaView;

        this.mWebsite = website;
        this.mBoardName = boardName;
        this.mThreadNumber = threadNumber;
        this.mEntity = entity;
    }

    @Override
    protected SendPostResult doInBackground(Void... args) {
        SendPostResult result = this.mPostSender.sendPost(this.mWebsite, this.mBoardName, this.mEntity);
        return result;
    }

    @Override
    public void onPreExecute() {
        this.mView.showPostLoading();
    }

    @Override
    protected void onPostExecute(final SendPostResult result) {
        this.mView.hidePostLoading();
        if (result.isSuccess) {
            this.mView.showSuccess(result.location);
        } else {
            this.mView.showError(result.error, result.isRecaptcha);
        }
    }
}
