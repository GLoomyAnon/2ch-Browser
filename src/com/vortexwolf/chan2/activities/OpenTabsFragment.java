package com.vortexwolf.chan2.activities;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.vortexwolf.chan2.R;
import com.vortexwolf.chan2.adapters.OpenTabsAdapter;
import com.vortexwolf.chan2.common.Constants;
import com.vortexwolf.chan2.common.Factory;
import com.vortexwolf.chan2.common.utils.AppearanceUtils;
import com.vortexwolf.chan2.common.utils.CompatibilityUtils;
import com.vortexwolf.chan2.db.FavoritesDataSource;
import com.vortexwolf.chan2.models.presentation.OpenTabModel;
import com.vortexwolf.chan2.services.presentation.OpenTabsManager;

public class OpenTabsFragment extends ListFragment {

    private OpenTabsAdapter mAdapter;
    private OpenTabsManager mTabsManager;
    private FavoritesDataSource mFavoritesDatasource;

    private Uri mCurrentUri;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.mTabsManager = Factory.getContainer().resolve(OpenTabsManager.class);
        this.mFavoritesDatasource = Factory.getContainer().resolve(FavoritesDataSource.class);

        Bundle extras = this.getArguments();
        if (extras != null && extras.containsKey(Constants.EXTRA_CURRENT_URL)) {
            this.mCurrentUri = Uri.parse(extras.getString(Constants.EXTRA_CURRENT_URL));
        }

        this.setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        this.mAdapter = new OpenTabsAdapter(this.getActivity(), this.mTabsManager.getOpenTabs(), this.mTabsManager);
        this.setListAdapter(this.mAdapter);

        this.registerForContextMenu(this.getListView());
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        OpenTabModel item = this.mAdapter.getItem(position);
        if (item.buildUrl().equals(this.mCurrentUri)) {
            this.getActivity().finish();
        } else {
            this.mTabsManager.navigate(item, this.getActivity());
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.opentabs, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_close_tabs_id:
                this.mTabsManager.removeAll();
                this.mAdapter.clear();
                break;
        }

        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        OpenTabModel item = this.mAdapter.getItem(info.position);

        menu.add(Menu.NONE, Constants.CONTEXT_MENU_COPY_URL, 0, this.getString(R.string.cmenu_copy_url));

        if (!this.mFavoritesDatasource.hasFavorites(item.getWebsite().name(), item.getBoard(), item.getThread())) {
            menu.add(Menu.NONE, Constants.CONTEXT_MENU_ADD_FAVORITES, 0, this.getString(R.string.cmenu_add_to_favorites));
        } else {
            menu.add(Menu.NONE, Constants.CONTEXT_MENU_REMOVE_FAVORITES, 0, this.getString(R.string.cmenu_remove_from_favorites));
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (!this.getUserVisibleHint()) {
            return false;
        }

        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        OpenTabModel model = this.mAdapter.getItem(menuInfo.position);

        switch (item.getItemId()) {
            case Constants.CONTEXT_MENU_COPY_URL: {
                String uri = model.buildUrl();

                CompatibilityUtils.copyText(this.getActivity(), uri, uri);
                AppearanceUtils.showToastMessage(this.getActivity(), uri);
                return true;
            }
            case Constants.CONTEXT_MENU_ADD_FAVORITES: {
                this.mFavoritesDatasource.addToFavorites(model.getWebsite().name(), model.getBoard(), model.getThread(), model.getTitle());
                return true;
            }
            case Constants.CONTEXT_MENU_REMOVE_FAVORITES: {
                this.mFavoritesDatasource.removeFromFavorites(model.getWebsite().name(), model.getBoard(), model.getThread());
                return true;
            }
        }

        return false;
    }
}
