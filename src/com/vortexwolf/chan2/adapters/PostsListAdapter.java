package com.vortexwolf.chan2.adapters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.content.res.Resources.Theme;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.text.Layout;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.vortexwolf.chan2.R;
import com.vortexwolf.chan2.activities.PostsListActivity;
import com.vortexwolf.chan2.common.Constants;
import com.vortexwolf.chan2.common.Factory;
import com.vortexwolf.chan2.common.controls.ClickableURLSpan;
import com.vortexwolf.chan2.common.library.MyLog;
import com.vortexwolf.chan2.common.utils.AppearanceUtils;
import com.vortexwolf.chan2.common.utils.CompatibilityUtils;
import com.vortexwolf.chan2.common.utils.StringUtils;
import com.vortexwolf.chan2.interfaces.IBusyAdapter;
import com.vortexwolf.chan2.interfaces.IURLSpanClickListener;
import com.vortexwolf.chan2.interfaces.IUrlBuilder;
import com.vortexwolf.chan2.interfaces.IUrlParser;
import com.vortexwolf.chan2.interfaces.IWebsite;
import com.vortexwolf.chan2.models.domain.PostModel;
import com.vortexwolf.chan2.models.presentation.AttachmentInfo;
import com.vortexwolf.chan2.models.presentation.IPostListEntity;
import com.vortexwolf.chan2.models.presentation.PostItemViewModel;
import com.vortexwolf.chan2.models.presentation.PostsViewModel;
import com.vortexwolf.chan2.models.presentation.StatusIndicatorEntity;
import com.vortexwolf.chan2.services.BrowserLauncher;
import com.vortexwolf.chan2.services.ThreadImagesService;
import com.vortexwolf.chan2.services.presentation.PostItemViewBuilder;
import com.vortexwolf.chan2.settings.ApplicationSettings;

public class PostsListAdapter extends ArrayAdapter<IPostListEntity> implements IURLSpanClickListener, IBusyAdapter {
    private static final String TAG = "PostsListAdapter";

    private static final int ITEM_VIEW_TYPE_POST = 0;
    private static final int ITEM_VIEW_TYPE_STATUS = 1;

    private final LayoutInflater mInflater;
    private final IWebsite mWebsite;
    private final String mBoardName;
    private final String mThreadNumber;
    private final String mUri;
    private final PostsViewModel mPostsViewModel;
    private final StatusIndicatorEntity mStatusViewModel;
    private final Theme mTheme;
    private final ApplicationSettings mSettings;
    private final ListView mListView;
    private final PostsListActivity mActivity;
    private final PostItemViewBuilder mPostItemViewBuilder;
    private final IUrlBuilder mUrlBuilder;
    private final Timer mLoadImagesTimer;
    private final ThreadImagesService mThreadImagesService;
    private final IUrlParser mUrlParser;
    private final ArrayList<PostModel> mOriginalPosts = new ArrayList<PostModel>();

    private StatusItemViewBag mStatusView;
    private boolean mIsUpdating = false;
    private boolean mIsBusy = false;
    private LoadImagesTimerTask mCurrentLoadImagesTask;

    public PostsListAdapter(PostsListActivity activity, IWebsite website, String boardName, String threadNumber, Theme theme, ListView listView) {
        super(activity.getApplicationContext(), 0);

        this.mWebsite = website;
        this.mBoardName = boardName;
        this.mThreadNumber = threadNumber;
        this.mInflater = LayoutInflater.from(activity);
        this.mTheme = theme;
        this.mPostsViewModel = new PostsViewModel(website, boardName, threadNumber);
        this.mStatusViewModel = new StatusIndicatorEntity();
        this.mSettings = Factory.resolve(ApplicationSettings.class);
        this.mListView = listView;
        this.mActivity = activity;
        this.mUrlBuilder = this.mWebsite.getUrlBuilder();
        this.mPostItemViewBuilder = new PostItemViewBuilder(this.mActivity, this.mWebsite, this.mBoardName, this.mThreadNumber, this.mSettings);
        this.mLoadImagesTimer = new Timer();
        this.mThreadImagesService = Factory.resolve(ThreadImagesService.class);
        this.mUrlParser = this.mWebsite.getUrlParser();
        this.mUri = this.mUrlBuilder.getThreadUrlHtml(this.mBoardName, this.mThreadNumber);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final IPostListEntity item = this.getItem(position);
        View view;

        if (item instanceof StatusIndicatorEntity) {
            view = convertView != null
                    ? convertView
                    : this.mInflater.inflate(R.layout.posts_list_status_item, null);

            StatusItemViewBag vb = (StatusItemViewBag)view.getTag();
            if (vb == null) {
                vb = new StatusItemViewBag();
                vb.model = (StatusIndicatorEntity)item;
                vb.hintView = view.findViewById(R.id.statusHintView);
                vb.loadingView = view.findViewById(R.id.statusLoadingView);
                view.setTag(vb);
            }
            this.mStatusView = vb;

            this.mStatusView.setLoading(this.mIsUpdating);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mActivity.refresh();
                }
            });
        }
        else {
            PostItemViewModel model = (PostItemViewModel)item;
            view = this.mPostItemViewBuilder.getView(model, convertView, this.mIsBusy);

            // cut long posts if necessary
            int maxPostHeight = this.mSettings.getLongPostsMaxHeight();
            if (maxPostHeight == 0 || model.isLongTextExpanded()) {
                this.mPostItemViewBuilder.removeMaxHeight(view);
            } else {
                this.mPostItemViewBuilder.setMaxHeight(view, maxPostHeight, this.mTheme);
            }
        }

        return view;
    }

    @Override
    public void onClick(View v, ClickableURLSpan span, String url) {

        Uri uri = Uri.parse(url);
        String pageName = this.mUrlParser.getThreadNumber(uri);

        // Если ссылка указывает на этот тред - перескакиваем на нужный пост,
        // иначе открываем в браузере
        if (this.mThreadNumber.equals(pageName)) {
            String postNumber = uri.getFragment();
            // Переходим на тот пост, куда указывает ссылка
            int position = postNumber != null ? this.findPostByNumber(postNumber) : Constants.OP_POST_POSITION;
            if (position == -1) {
                AppearanceUtils.showToastMessage(this.getContext(), this.getContext().getString(R.string.notification_post_not_found));
                return;
            }

            IPostListEntity item = this.getItem(position);
            if (!(item instanceof PostItemViewModel)) {
                return;
            }

            if (this.mSettings.isLinksInPopup()) {
                this.mPostItemViewBuilder.displayPopupDialog(
                        (PostItemViewModel) item,
                        this.mActivity, this.mTheme,
                        CompatibilityUtils.isTablet(this.mActivity) ? getSpanCoordinates(v, span) : null);
            } else {
                this.mListView.setSelection(position);
            }
        } else {
            BrowserLauncher.launchExternalBrowser(v.getContext(), this.mUrlBuilder.makeAbsolute(url));
        }
    }

    private Point getSpanCoordinates(View widget, ClickableURLSpan span) {
        TextView parentTextView = (TextView) widget;

        Rect parentTextViewRect = new Rect();

        // Initialize values for the computing of clickedText position
        SpannableString completeText = (SpannableString)(parentTextView).getText();
        Layout textViewLayout = parentTextView.getLayout();

        double startOffsetOfClickedText = completeText.getSpanStart(span);
        double endOffsetOfClickedText = completeText.getSpanEnd(span);
        double startXCoordinatesOfClickedText = textViewLayout.getPrimaryHorizontal((int)startOffsetOfClickedText);
        double endXCoordinatesOfClickedText = textViewLayout.getPrimaryHorizontal((int)endOffsetOfClickedText);


        // Get the rectangle of the clicked text
        int currentLineStartOffset = textViewLayout.getLineForOffset((int)startOffsetOfClickedText);
        int currentLineEndOffset = textViewLayout.getLineForOffset((int)endOffsetOfClickedText);
        boolean keywordIsInMultiLine = currentLineStartOffset != currentLineEndOffset;
        textViewLayout.getLineBounds(currentLineStartOffset, parentTextViewRect);


        // Update the rectangle position to his real position on screen
        int[] parentTextViewLocation = {0,0};
        parentTextView.getLocationOnScreen(parentTextViewLocation);

        double parentTextViewTopAndBottomOffset = (
            parentTextViewLocation[1] -
            parentTextView.getScrollY() +
            parentTextView.getCompoundPaddingTop()
        );

        Rect windowRect = new Rect();
        this.mActivity.getWindow().getDecorView().getWindowVisibleDisplayFrame(windowRect);
        parentTextViewTopAndBottomOffset -= windowRect.top;

        parentTextViewRect.top += parentTextViewTopAndBottomOffset;
        parentTextViewRect.bottom += parentTextViewTopAndBottomOffset;

        parentTextViewRect.left += (
            parentTextViewLocation[0] +
            startXCoordinatesOfClickedText +
            parentTextView.getCompoundPaddingLeft() -
            parentTextView.getScrollX()
        );
        parentTextViewRect.right = (int) (
            parentTextViewRect.left +
            endXCoordinatesOfClickedText -
            startXCoordinatesOfClickedText
        );

        int x = (parentTextViewRect.left + parentTextViewRect.right) / 2;
        int y = (parentTextViewRect.top + parentTextViewRect.bottom) / 2;
        if (keywordIsInMultiLine) {
            x = parentTextViewRect.left;
        }

        return new Point(x, y);
    }

    private int findPostByNumber(String postNumber) {
        PostItemViewModel vm = this.mPostsViewModel.getModel(postNumber);
        if (vm != null) {
            return vm.getPosition();
        }
        return -1;
    }

    /** Возвращает номер последнего сообщения */
    public int getLastPostNumber() {
        return this.mPostsViewModel.getLastPostNumber();
    }

    /** Обновляет адаптер полностью */
    public void setAdapterData(PostModel[] posts) {
        this.clear();
        this.mOriginalPosts.clear();
        this.mThreadImagesService.clearThreadImages(this.mUri);

        List<PostItemViewModel> models = this.mPostsViewModel.addModels(Arrays.asList(posts), this.mTheme, this, this.mActivity.getResources());
        for (PostItemViewModel model : models) {
            for (int i=0; i < Constants.MAX_ATTACHMENTS; i++) {
                AttachmentInfo attachment = model.getAttachment(i);
                this.mThreadImagesService.addThreadImage(this.mUri, attachment);
            }

            this.add(model);
        }

        this.mOriginalPosts.addAll(Arrays.asList(posts));

        this.add(this.mStatusViewModel);
    }

    public void scrollToPost(String postNumber) {
        if (StringUtils.isEmpty(postNumber)) {
            return;
        }

        int position = this.findPostByNumber(postNumber);
        if (position == -1) {
            AppearanceUtils.showToastMessage(this.getContext(), this.getContext().getString(R.string.notification_post_not_found));
            return;
        }

        this.mListView.setSelection(position);
    }

    public int updateAdapterData(int from, PostModel[] posts) {
        ArrayList<PostModel> newPosts = new ArrayList<PostModel>();
        for (PostModel pi : posts) {
            Integer currentNumber = !StringUtils.isEmpty(pi.getNumber()) ? Integer.parseInt(pi.getNumber()) : 0;
            if (currentNumber > from) {
                newPosts.add(pi);
            }
        }

        List<PostItemViewModel> newModels = this.mPostsViewModel.addModels(newPosts, this.mTheme, this, this.mActivity.getResources());
        for (PostItemViewModel model : newModels) {
            for (int i=0; i < Constants.MAX_ATTACHMENTS; i++) {
                AttachmentInfo attachment = model.getAttachment(i);
                this.mThreadImagesService.addThreadImage(this.mUri, attachment);
            }

            this.insert(model, this.getCount() - 1);
        }

        this.mOriginalPosts.addAll(newPosts);

        // обновить все видимые элементы, чтобы правильно перерисовался список
        // ссылок replies
        if (newPosts.size() > 0) {
            this.notifyDataSetChanged();
        }

        return newPosts.size();
    }

    @Override
    public void setBusy(boolean value, AbsListView listView) {
        if (this.mCurrentLoadImagesTask != null) {
            this.mCurrentLoadImagesTask.cancel();
        }

        if (this.mIsBusy == true && value == false) {
            this.mCurrentLoadImagesTask = new LoadImagesTimerTask();
            this.mLoadImagesTimer.schedule(this.mCurrentLoadImagesTask, 500);
        }

        this.mIsBusy = value;
    }

    private void loadListImages() {
        int count = this.mListView.getChildCount();
        for (int i = 0; i < count; i++) {
            View v = this.mListView.getChildAt(i);
            int position = this.mListView.getPositionForView(v);

            IPostListEntity item = this.getItem(position);
            if (item instanceof PostItemViewModel) {
                this.mPostItemViewBuilder.displayThumbnail(v, (PostItemViewModel)item);
            }
        }
    }

    public void setUpdating(boolean isUpdating) {
        this.mIsUpdating = isUpdating;
        if (this.mStatusView != null) {
            this.mStatusView.setLoading(isUpdating);
        }
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return this.getItem(position) instanceof StatusIndicatorEntity
                ? ITEM_VIEW_TYPE_STATUS
                : ITEM_VIEW_TYPE_POST;
    }

    @Override
    public boolean isEnabled(int position) {
        return this.getItem(position).isListItemEnabled();
    }

    public PostModel[] getOriginalPosts(){
        return this.mOriginalPosts.toArray(new PostModel[this.mOriginalPosts.size()]);
    }

    public List<String> getAllPostFiles() {
        ArrayList<String> filePaths = new ArrayList<String>();
        for (PostItemViewModel model : this.mPostsViewModel.getAllModels()) {
            for (int i = 0; i < model.getAttachmentsNumber(); i++) {
                AttachmentInfo attachment = model.getAttachment(i);
                filePaths.add(attachment.getSourceUrl());
            }
        }

        return filePaths;
    }

    private class LoadImagesTimerTask extends TimerTask {
        @Override
        public void run() {
            MyLog.d(TAG, "LoadImagesTimerTask");
            PostsListAdapter.this.mListView.post(new LoadImagesRunnable());
        }
    }

    private class LoadImagesRunnable implements Runnable {
        @Override
        public void run() {
            PostsListAdapter.this.loadListImages();
        }
    }

    private class StatusItemViewBag {
        public StatusIndicatorEntity model;

        public View hintView;
        public View loadingView;

        public void setLoading(boolean isLoading) {
            model.setLoading(isLoading);
            hintView.setVisibility(!isLoading ? View.VISIBLE : View.GONE);
            loadingView.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
    }
}
