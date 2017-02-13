package com.vortexwolf.chan2.interfaces;

public interface ICloudflareCheckListener {
    void onStart();
    void onSuccess();
    void onTimeout();
}
