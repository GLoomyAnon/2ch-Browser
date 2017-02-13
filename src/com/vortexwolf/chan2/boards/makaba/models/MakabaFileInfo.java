package com.vortexwolf.chan2.boards.makaba.models;

import org.codehaus.jackson.annotate.JsonProperty;

/** Can be an image as well as a webm video, both have the same set of properties. 
 * */
public class MakabaFileInfo {
    @JsonProperty("width")
    public int width;
    
    @JsonProperty("height")
    public int height;
    
    @JsonProperty("size")
    public int size;
    
    @JsonProperty("thumbnail")
    public String thumbnail;

    @JsonProperty("path")
    public String path;
}