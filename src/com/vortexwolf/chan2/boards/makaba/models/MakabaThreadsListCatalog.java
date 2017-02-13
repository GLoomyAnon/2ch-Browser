package com.vortexwolf.chan2.boards.makaba.models;

import org.codehaus.jackson.annotate.JsonProperty;

public class MakabaThreadsListCatalog {
    @JsonProperty("threads")
    public MakabaThreadInfoCatalog[] threads;
}
