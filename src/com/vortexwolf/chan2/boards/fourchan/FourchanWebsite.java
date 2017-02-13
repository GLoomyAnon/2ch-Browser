package com.vortexwolf.chan2.boards.fourchan;

import com.vortexwolf.chan2.common.Factory;
import com.vortexwolf.chan2.interfaces.IUrlBuilder;
import com.vortexwolf.chan2.interfaces.IUrlParser;
import com.vortexwolf.chan2.interfaces.IWebsite;

import java.util.regex.Pattern;

public class FourchanWebsite implements IWebsite {
    public static final String NAME = "4chan";
    public static final Pattern URI_PATTERN = Pattern.compile("4chan");

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public IUrlBuilder getUrlBuilder() {
        return Factory.resolve(FourchanUrlBuilder.class);
    }

    @Override
    public IUrlParser getUrlParser() {
        return Factory.resolve(FourchanUrlParser.class);
    }
}
