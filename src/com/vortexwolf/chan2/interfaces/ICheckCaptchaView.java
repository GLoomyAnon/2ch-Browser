package com.vortexwolf.chan2.interfaces;

public interface ICheckCaptchaView {
    void beforeCheck();

    void showSuccess();
    
    void showError(String message);
}
