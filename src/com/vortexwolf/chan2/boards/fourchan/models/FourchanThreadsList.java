package com.vortexwolf.chan2.boards.fourchan.models;

import org.codehaus.jackson.annotate.JsonProperty;

public class FourchanThreadsList {
    @JsonProperty("threads")
    public FourchanThreadInfo[] threads;
}
