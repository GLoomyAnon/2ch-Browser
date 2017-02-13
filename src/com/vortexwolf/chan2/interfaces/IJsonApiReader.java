package com.vortexwolf.chan2.interfaces;

import com.vortexwolf.chan2.exceptions.HtmlNotJsonException;
import com.vortexwolf.chan2.exceptions.JsonApiReaderException;
import com.vortexwolf.chan2.models.domain.PostModel;
import com.vortexwolf.chan2.models.domain.SearchPostListModel;
import com.vortexwolf.chan2.models.domain.ThreadModel;

public interface IJsonApiReader {
    ThreadModel[] readCatalog(String boardName, int filter, IJsonProgressChangeListener listener, ICancelled task) throws JsonApiReaderException, HtmlNotJsonException;

    ThreadModel[] readThreadsList(String boardName, int page, boolean checkModified, IJsonProgressChangeListener listener, ICancelled task) throws JsonApiReaderException, HtmlNotJsonException;

    PostModel[] readPostsList(String boardName, String threadNumber, int fromNumber, boolean checkModified, IJsonProgressChangeListener listener, ICancelled task) throws JsonApiReaderException, HtmlNotJsonException;

    SearchPostListModel searchPostsList(String boardName, String searchQuery, IJsonProgressChangeListener listener, ICancelled task) throws JsonApiReaderException, HtmlNotJsonException;
}
