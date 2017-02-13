package com.vortexwolf.chan2.interfaces;

public interface IWebsite {
    String name();

    IUrlBuilder getUrlBuilder();

    IUrlParser getUrlParser();
}
