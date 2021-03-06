package com.vortexwolf.chan.interfaces;

public interface IPostSendView {

    void showSuccess(String redirectedPage);

    void showError(String error, boolean isRecaptcha);

    void showPostLoading();

    void hidePostLoading();
}
