package com.vortexwolf.chan2.boards.makaba.models;

import org.codehaus.jackson.annotate.JsonProperty;

public class MakabaError {
    @JsonProperty("Code")
    public int code;
    @JsonProperty("Error")
    public String error;
}