package com.vortexwolf.chan2.interfaces;

public interface ICheckPasscodeView {
    void onPasscodeRemoved();
    void onPasscodeChecked(boolean isSuccess, String errorMessage);
}
