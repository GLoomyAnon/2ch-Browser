package com.vortexwolf.chan2.boards.fourchan;

import org.codehaus.jackson.JsonNode;

import android.content.res.Resources;

import com.vortexwolf.chan2.R;
import com.vortexwolf.chan2.boards.fourchan.models.FourchanCatalogPage;
import com.vortexwolf.chan2.boards.fourchan.models.FourchanThreadInfo;
import com.vortexwolf.chan2.boards.fourchan.models.FourchanThreadsList;
import com.vortexwolf.chan2.exceptions.HtmlNotJsonException;
import com.vortexwolf.chan2.exceptions.JsonApiReaderException;
import com.vortexwolf.chan2.interfaces.ICancelled;
import com.vortexwolf.chan2.interfaces.IJsonApiReader;
import com.vortexwolf.chan2.interfaces.IJsonProgressChangeListener;
import com.vortexwolf.chan2.interfaces.IUrlBuilder;
import com.vortexwolf.chan2.models.domain.PostModel;
import com.vortexwolf.chan2.models.domain.SearchPostListModel;
import com.vortexwolf.chan2.models.domain.ThreadModel;
import com.vortexwolf.chan2.services.http.JsonHttpReader;

public class FourchanApiReader implements IJsonApiReader {
    private final JsonHttpReader mJsonReader;
    private final IUrlBuilder mFourchanUriBuilder;
    private final FourchanModelsMapper mFourchanModelsMapper;
    private final Resources mResources;

    public FourchanApiReader(JsonHttpReader jsonReader, IUrlBuilder uriBuilder, FourchanModelsMapper modelsMapper, Resources resources) {
        this.mJsonReader = jsonReader;
        this.mFourchanUriBuilder = uriBuilder;
        this.mFourchanModelsMapper = modelsMapper;
        this.mResources = resources;
    }

    @Override
    public ThreadModel[] readCatalog(String boardName, int filter, IJsonProgressChangeListener listener, ICancelled task) throws JsonApiReaderException, HtmlNotJsonException {
        String uri = this.mFourchanUriBuilder.getCatalogUrlApi(boardName, filter);

        JsonNode json = this.mJsonReader.readData(uri, false, listener, task);
        if (json == null) {
            return null;
        }

        FourchanCatalogPage[] result = parseDataOrThrowError(json, FourchanCatalogPage[].class);
        ThreadModel[] models = this.mFourchanModelsMapper.mapCatalog(result);
        return models;
    }

    @Override
    public ThreadModel[] readThreadsList(String boardName, int page, boolean checkModified, IJsonProgressChangeListener listener, ICancelled task) throws JsonApiReaderException, HtmlNotJsonException {
        String uri = this.mFourchanUriBuilder.getPageUrlApi(boardName, page);

        JsonNode json = this.mJsonReader.readData(uri, checkModified, listener, task);
        if (json == null) {
            return null;
        }

        FourchanThreadsList result = parseDataOrThrowError(json, FourchanThreadsList.class);
        ThreadModel[] models = this.mFourchanModelsMapper.mapThreadModels(result);
        return models;
    }

    @Override
    public PostModel[] readPostsList(String boardName, String threadNumber, int fromNumber, boolean checkModified, IJsonProgressChangeListener listener, ICancelled task) throws JsonApiReaderException, HtmlNotJsonException {
        String uri = this.mFourchanUriBuilder.getThreadUrlApi(boardName, threadNumber);

        JsonNode json = this.mJsonReader.readData(uri, checkModified, listener, task);
        if (json == null) {
            return null;
        }

        FourchanThreadInfo result = parseDataOrThrowError(json, FourchanThreadInfo.class);
        PostModel[] models = this.mFourchanModelsMapper.mapThreadModel(result).getPosts();
        return models;
    }

    @Override
    public SearchPostListModel searchPostsList(String boardName, String searchQuery, IJsonProgressChangeListener listener, ICancelled task) throws JsonApiReaderException, HtmlNotJsonException {
        return null;
    }

    private <T> T parseDataOrThrowError(JsonNode json, Class<T> valueType) throws JsonApiReaderException {
        T result = this.mJsonReader.convertValue(json, valueType);
        if (result != null) {
            return result;
        }

        throw new JsonApiReaderException(this.mResources.getString(R.string.error_json_parse));
    }
}
