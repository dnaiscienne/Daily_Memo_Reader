package com.example.ds.daily_memo_reader.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.ShareCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.ShareActionProvider;
import android.text.Html;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.example.ds.daily_memo_reader.R;
import com.example.ds.daily_memo_reader.data.EntriesContract;
import com.example.ds.daily_memo_reader.data.EntryLoader;

/**
 * Created by DS on 2/7/2016.
 */
public class EntryDetailFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "EntryDetailFragment";

    public static final String ARG_ITEM_ID = "item_id";
    public static final String ARD_TWO_PANE = "two_pane";
    private static final float PARALLAX_FACTOR = 1.25f;

    private ShareActionProvider mShareActionProvider;
    private Cursor mCursor;
    private long mItemId;
    private View mRootView;
    private int mMutedColor = 0xFF333333;
    private ObservableScrollView mScrollView;
    private ScrollView mNormalScrollView;
    private DrawInsetsFrameLayout mDrawInsetsFrameLayout;
    private ColorDrawable mStatusBarColorDrawable;

    private boolean mIsFavorite = false;
    private android.support.design.widget.FloatingActionButton mFavoriteFab;

    private int mTopInset;
    private View mPhotoContainerView;
    private ImageView mPhotoView;
    private int mScrollY;
    private boolean mIsCard = false;
    private int mStatusBarFullOpacityBottom;
    private ProgressBar mLoadingPanel;

    private boolean mTwoPane = false;

    private Activity mActivity;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public EntryDetailFragment() {
        setHasOptionsMenu(true);
    }

    public static EntryDetailFragment newInstance(long itemId) {
        Bundle arguments = new Bundle();
        arguments.putLong(ARG_ITEM_ID, itemId);
        EntryDetailFragment fragment = new EntryDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }
    public static EntryDetailFragment newInstance(long itemId, boolean twoPane) {
        Bundle arguments = new Bundle();
        arguments.putLong(ARG_ITEM_ID, itemId);
        arguments.putBoolean(ARD_TWO_PANE, twoPane);
        EntryDetailFragment fragment = new EntryDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mItemId = getArguments().getLong(ARG_ITEM_ID);
        }
        if (getArguments().containsKey(ARD_TWO_PANE)){
            mTwoPane = true;
        }

        mIsCard = getResources().getBoolean(R.bool.detail_is_card);
        mStatusBarFullOpacityBottom = getResources().getDimensionPixelSize(
                R.dimen.detail_card_top_margin);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_detailfragment, menu);
        MenuItem menuItem = menu.findItem(R.id.action_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
        mShareActionProvider.setShareIntent(createIntent());
    }
    public Intent createIntent() {
        // When you want to share set the share intent.

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        if(mCursor != null){
            String content = Html.fromHtml(mCursor.getString(EntryLoader.Query.BODY)).toString();
            shareIntent.putExtra(Intent.EXTRA_TEXT, content);
        }
        return shareIntent;
    }

    public EntryDetailActivity getActivityCast() {
        return (EntryDetailActivity) getActivity();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // In support library r8, calling initLoader for a fragment in a FragmentPagerAdapter in
        // the fragment's onCreate may cause the same LoaderManager to be dealt to multiple
        // fragments because their mIndex is -1 (haven't been added to the activity yet). Thus,
        // we do this in onActivityCreated.
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_entry_detail, container, false);
        mDrawInsetsFrameLayout = (DrawInsetsFrameLayout)
                mRootView.findViewById(R.id.draw_insets_frame_layout);
        mDrawInsetsFrameLayout.setOnInsetsCallback(new DrawInsetsFrameLayout.OnInsetsCallback() {
            @Override
            public void onInsetsChanged(Rect insets) {
                mTopInset = insets.top;
            }
        });
        if(mTwoPane){
           mNormalScrollView = (ScrollView) mRootView.findViewById(R.id.scrollview);
        }else{
            mScrollView = (ObservableScrollView) mRootView.findViewById(R.id.scrollview);
            mScrollView.setCallbacks(new ObservableScrollView.Callbacks() {
                @Override
                public void onScrollChanged() {
                    mScrollY = mScrollView.getScrollY();
                    getActivityCast().onUpButtonFloorChanged(mItemId, EntryDetailFragment.this);
                    mPhotoContainerView.setTranslationY((int) (mScrollY - mScrollY / PARALLAX_FACTOR));
                    updateStatusBar();
                }
            });
        }


        mPhotoView = (EntryDetailImageView) mRootView.findViewById(R.id.photo);
        mPhotoContainerView = mRootView.findViewById(R.id.photo_container);

        mStatusBarColorDrawable = new ColorDrawable(0);
        if(!mTwoPane){
            mRootView.findViewById(R.id.share_fab).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(getActivity())
                            .setType("text/plain")
                            .setText("Some sample text")
                            .getIntent(), getString(R.string.action_share)));
                }
            });
        }


        mRootView.findViewById(R.id.favorite_fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mActivity != null) {
                    mIsFavorite = mCursor.getInt(EntryLoader.Query.FAVORITE) == 1 ? true : false;
                    Log.v("FAV", Boolean.toString(mIsFavorite));
                    mIsFavorite = !mIsFavorite;
                    ContentValues cv = new ContentValues();
                    cv.put(EntriesContract.Entries.FAVORITE, mIsFavorite ? 1 : 0);
                    mActivity.getContentResolver().update(EntriesContract.Entries.buildEntryUri(mItemId), cv, null, null);
                    updateFavoriteFab();
                }
            }
        });
        mLoadingPanel = (ProgressBar) mRootView.findViewById(R.id.loadingPanel);
        bindViews();
        updateStatusBar();
        if(mShareActionProvider != null){
            mShareActionProvider.setShareIntent(createIntent());

        }
        return mRootView;
    }

    private void updateFavoriteFab(){
        mIsFavorite = mCursor.getInt(EntryLoader.Query.FAVORITE) == 1 ? true : false;
        if (mIsFavorite){
            mFavoriteFab.setImageResource(R.drawable.ic_bookmark_border_white);
            mFavoriteFab.setContentDescription(getString(R.string.action_remove_favorite));
        }else{
            mFavoriteFab.setImageResource(R.drawable.ic_bookmark_white);
            mFavoriteFab.setContentDescription(getString(R.string.action_favorite));
        }
        Log.v("UpdateFAV", Boolean.toString(mIsFavorite));
    }

    private void updateStatusBar() {
        int color = 0;
        if (mPhotoView != null && mTopInset != 0 && mScrollY > 0) {
            float f = progress(mScrollY,
                    mStatusBarFullOpacityBottom - mTopInset * 3,
                    mStatusBarFullOpacityBottom - mTopInset);
            color = Color.argb((int) (255 * f),
                    (int) (Color.red(mMutedColor) * 0.9),
                    (int) (Color.green(mMutedColor) * 0.9),
                    (int) (Color.blue(mMutedColor) * 0.9));
        }
//        getActivity().getWindow().setStatusBarColor(color);
        if (mActivity != null){
            mActivity.getWindow().setStatusBarColor(color);
        }
        mStatusBarColorDrawable.setColor(color);
        mDrawInsetsFrameLayout.setInsetBackground(mStatusBarColorDrawable);
    }

    static float progress(float v, float min, float max) {
        return constrain((v - min) / (max - min), 0, 1);
    }

    static float constrain(float val, float min, float max) {
        if (val < min) {
            return min;
        } else if (val > max) {
            return max;
        } else {
            return val;
        }
    }

    private void bindViews() {
        if (mRootView == null) {
            return;
        }

        TextView titleView = (TextView) mRootView.findViewById(R.id.article_title);
        TextView bylineView = (TextView) mRootView.findViewById(R.id.article_byline);
        bylineView.setMovementMethod(new LinkMovementMethod());
        TextView bodyView = (TextView) mRootView.findViewById(R.id.article_body);
        bodyView.setTypeface(Typeface.createFromAsset(getResources().getAssets(), "Rosario-Regular.ttf"));
        mFavoriteFab =
                (android.support.design.widget.FloatingActionButton) mRootView.findViewById(R.id.favorite_fab);

        if (mCursor != null) {
            mRootView.setAlpha(0);
            mRootView.setVisibility(View.VISIBLE);
            mRootView.animate().alpha(1);
            titleView.setText(mCursor.getString(EntryLoader.Query.TITLE));
            bylineView.setText(Html.fromHtml(
                    DateUtils.getRelativeTimeSpanString(
                            mCursor.getLong(EntryLoader.Query.PUBLISHED_DATE),
                            System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                            DateUtils.FORMAT_ABBREV_ALL).toString()
                            + " by <font color='#ffffff'>"
                            + mCursor.getString(EntryLoader.Query.AUTHOR)
                            + "</font>"));
            bodyView.setText(Html.fromHtml(mCursor.getString(EntryLoader.Query.BODY)));
            ImageLoaderHelper.getInstance(getActivity()).getImageLoader()
                    .get(mCursor.getString(EntryLoader.Query.PHOTO_URL), new ImageLoader.ImageListener() {
                        @Override
                        public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
                            Bitmap bitmap = imageContainer.getBitmap();

                            if (bitmap != null) {
                                Palette p = Palette.generate(bitmap, 12);
                                mMutedColor = p.getDarkMutedColor(0xFF333333);
                                mPhotoView.setImageBitmap(imageContainer.getBitmap());
                                mRootView.findViewById(R.id.meta_bar)
                                        .setBackgroundColor(mMutedColor);
                                mLoadingPanel.setVisibility(View.GONE);
                                updateStatusBar();
                            }
                        }

                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            mLoadingPanel.setVisibility(View.GONE);
                        }
                    });
            updateFavoriteFab();
        } else {
            mRootView.setVisibility(View.GONE);
            titleView.setText("N/A");
            bylineView.setText("N/A" );
            bodyView.setText("N/A");
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return EntryLoader.newInstanceForEntryId(getActivity(), mItemId);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (!isAdded()) {
            if (cursor != null) {
                cursor.close();
            }
            return;
        }

        mCursor = cursor;
        if (mCursor != null && !mCursor.moveToFirst()) {
            Log.e(TAG, "Error reading item detail cursor");
            mCursor.close();
            mCursor = null;
        }
        if(mShareActionProvider != null){
            mShareActionProvider.setShareIntent(createIntent());

        }
        bindViews();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mCursor = null;
        bindViews();
    }

    public int getUpButtonFloor() {
        if (mPhotoContainerView == null || mPhotoView.getHeight() == 0) {
            return Integer.MAX_VALUE;
        }

        // account for parallax
        return mIsCard
                ? (int) mPhotoContainerView.getTranslationY() + mPhotoView.getHeight() - mScrollY
                : mPhotoView.getHeight() - mScrollY;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (mActivity == null){
            mActivity = activity;
        }
    }
}
