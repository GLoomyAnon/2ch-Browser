package com.vortexwolf.chan2.boards.makaba;

import com.vortexwolf.chan2.common.Factory;
import com.vortexwolf.chan2.interfaces.IUrlBuilder;
import com.vortexwolf.chan2.interfaces.IUrlParser;
import com.vortexwolf.chan2.interfaces.IWebsite;

import java.util.regex.Pattern;

public class MakabaWebsite implements IWebsite {
    public static final String NAME = "2ch";
    public static final Pattern URI_PATTERN = Pattern.compile("2ch|2-ch");

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public IUrlBuilder getUrlBuilder() {
        return Factory.resolve(MakabaUrlBuilder.class);
    }

    @Override
    public IUrlParser getUrlParser(){
        return Factory.resolve(MakabaUrlParser.class);
    }
}
