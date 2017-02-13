package com.vortexwolf.chan2.services.presentation;

import java.util.ArrayList;

import android.app.Activity;

import com.vortexwolf.chan2.common.utils.StringUtils;
import com.vortexwolf.chan2.db.HistoryDataSource;
import com.vortexwolf.chan2.interfaces.IWebsite;
import com.vortexwolf.chan2.models.presentation.OpenTabModel;
import com.vortexwolf.chan2.services.NavigationService;

public class OpenTabsManager {
    private final ArrayList<OpenTabModel> mTabs = new ArrayList<OpenTabModel>();

    private final HistoryDataSource mDataSource;
    private final NavigationService mNavigationService;

    public OpenTabsManager(HistoryDataSource dataSource, NavigationService navigationService) {
        this.mDataSource = dataSource;
        this.mNavigationService = navigationService;
    }

    public OpenTabModel add(OpenTabModel newTab) {
        // Не добавляем, если уже добавлено
        for (OpenTabModel openTab : this.mTabs) {
            if (openTab.isEqualTo(newTab)) {
                return openTab;
            }
        }

        this.mTabs.add(0, newTab);
        this.mDataSource.addHistory(newTab.getWebsite().name(), newTab.getBoard(), newTab.getThread(), newTab.getTitle());

        return newTab;
    }

    @SuppressWarnings("unchecked")
    public ArrayList<OpenTabModel> getOpenTabs() {
        return (ArrayList<OpenTabModel>) this.mTabs.clone();
    }

    public void remove(OpenTabModel tab) {
        this.mTabs.remove(tab);
    }

    public void removeAll() {
        this.mTabs.clear();
    }

    public void navigate(OpenTabModel tab, Activity activity) {
        if (StringUtils.isEmpty(tab.getThread())) {
            this.mNavigationService.navigateBoardPage(activity, null, tab.getWebsite().name(), tab.getBoard(), tab.getPage(), true);
        } else {
            this.mNavigationService.navigateThread(activity, null, tab.getWebsite().name(), tab.getBoard(), tab.getThread(), tab.getTitle(), null, true);
        }
    }

    public OpenTabModel getByUri(IWebsite website, String board, String thread, int page) {
        for (OpenTabModel model : this.mTabs) {
            if (model.isEqualTo(website, board, thread, page)) {
                return model;
            }
        }

        return null;
    }
}
