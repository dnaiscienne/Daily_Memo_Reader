package com.example.ds.daily_memo_reader.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ds.daily_memo_reader.R;
import com.example.ds.daily_memo_reader.data.EntriesContract;
import com.example.ds.daily_memo_reader.data.EntryLoader;
import com.example.ds.daily_memo_reader.data.UpdaterService;
import com.squareup.picasso.Picasso;

/**
 * Created by DS on 2/10/2016.
 */
public class EntryListFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> , AppBarLayout.OnOffsetChangedListener{

    private  Toolbar mToolbar;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private AppBarLayout mAppBarLayout;
    private static final String PREF_FAVORITE = "favorite";
    private boolean mFavorite;
    private Activity mActivity;

    public interface Callback {
        public void onItemSelected(Bundle b);
    }
    public EntryListFragment(){
        setHasOptionsMenu(true);

    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = this.getActivity();
//        if(mToolbar == null && mActivity != null){
//            mToolbar = (Toolbar) mActivity.findViewById(R.id.toolbar);
//            ((AppCompatActivity) mActivity).setSupportActionBar(mToolbar);
//        }

    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mActivity = this.getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_entry_list, container, false);
        mToolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        if (mToolbar != null){
            ((AppCompatActivity) mActivity).setSupportActionBar(mToolbar);
            ((AppCompatActivity) mActivity).getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh_layout);

        mSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {

                        // This method performs the actual data-refresh operation.
                        // The method calls setRefreshing(false) when it's finished.
                        refresh();
                    }
                }
        );

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        getLoaderManager().initLoader(0, null, this);

        if (savedInstanceState == null) {
            refresh();
        }
        mAppBarLayout = (AppBarLayout) rootView.findViewById(R.id.toolbar_container);
        SharedPreferences pref = mActivity.getPreferences(0);
        mFavorite = pref.getBoolean(PREF_FAVORITE, false);
        Log.v("Show Favorite", Boolean.toString(mFavorite));

        return rootView;
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_main, menu);
        if(mFavorite){
            menu.getItem(0).getSubMenu().getItem(0).setTitle(R.string.setting_toggle_recent);
        }else{
            menu.getItem(0).getSubMenu().getItem(0).setTitle(R.string.setting_toggle_favorite);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        SharedPreferences pref = mActivity.getPreferences(0);

        //noinspection SimplifiableIfStatement

        if (id == R.id.toggle_favorite) {
            SharedPreferences.Editor editor = pref.edit();
            if(mFavorite){
                mFavorite = false;
                editor.putBoolean(PREF_FAVORITE, mFavorite);
                item.setTitle(R.string.setting_toggle_favorite);
                Toast.makeText(mActivity, "Recent View", Toast.LENGTH_SHORT).show();
            }else{
                mFavorite = true;
                editor.putBoolean(PREF_FAVORITE, mFavorite);
                item.setTitle(R.string.setting_toggle_recent);
                Toast.makeText(mActivity, "Favorites View", Toast.LENGTH_SHORT).show();
            }
            editor.apply();
            refreshList();
        }

        return super.onOptionsItemSelected(item);
    }
    private void refreshList(){getLoaderManager().restartLoader(0, null, this);}

    private void refresh() {
        mActivity.startService(new Intent(mActivity, UpdaterService.class));
    }

    @Override
    public void onStart() {
        super.onStart();
        mActivity.registerReceiver(mRefreshingReceiver,
                new IntentFilter(UpdaterService.BROADCAST_ACTION_STATE_CHANGE));
    }

    @Override
    public void onStop() {
        super.onStop();
        mActivity.unregisterReceiver(mRefreshingReceiver);
    }
    private boolean mIsRefreshing = false;
    private BroadcastReceiver mRefreshingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (UpdaterService.BROADCAST_ACTION_STATE_CHANGE.equals(intent.getAction())) {
                mIsRefreshing = intent.getBooleanExtra(UpdaterService.EXTRA_REFRESHING, false);
                updateRefreshingUI();
            }
        }
    };
    private void updateRefreshingUI() {
        mSwipeRefreshLayout.setRefreshing(mIsRefreshing);
    }
    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
        if (i == 0) {
            mSwipeRefreshLayout.setEnabled(true);
        } else {
            mSwipeRefreshLayout.setEnabled(false);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if(mFavorite){
            String selectionClause = EntriesContract.Entries.FAVORITE + " = ?";
            String[] selectionArgs = {"1"};
            return  new CursorLoader(mActivity,
                    EntriesContract.Entries.buildDirUri(),
                    EntryLoader.Query.PROJECTION,
                    selectionClause,
                    selectionArgs,
                    EntriesContract.Entries.DEFAULT_SORT);
        }else{
            return EntryLoader.newAllEntriesInstance(mActivity);

        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        Adapter adapter = new Adapter(cursor, mActivity);
        adapter.setHasStableIds(true);
        mRecyclerView.setAdapter(adapter);
        int columnCount = getResources().getInteger(R.integer.list_column_count);
        StaggeredGridLayoutManager sglm =
                new StaggeredGridLayoutManager(columnCount, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(sglm);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mRecyclerView.setAdapter(null);
    }

    private class Adapter extends RecyclerView.Adapter<ViewHolder> {
        private Cursor mCursor;
        private Context mContext;

        public Adapter(Cursor cursor, Context context) {
            mCursor = cursor;
            mContext = context;
        }

        @Override
        public long getItemId(int position) {
            mCursor.moveToPosition(position);
            return mCursor.getLong(EntryLoader.Query._ID);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = mActivity.getLayoutInflater().inflate(R.layout.list_item_entry, parent, false);
            final ViewHolder vh = new ViewHolder(view);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Bundle b = new Bundle();
//                    String uriString = EntriesContract.Entries.buildEntryUri(getItemId(vh.getAdapterPosition())).toString();
                    long itemId = getItemId(vh.getAdapterPosition());
                    b.putLong(EntryDetailFragment.ARG_ITEM_ID, itemId);
                    ((Callback) getActivity())
                            .onItemSelected(b);
//                    startActivity(new Intent(Intent.ACTION_VIEW,
//                            EntriesContract.Entries.buildEntryUri(getItemId(vh.getAdapterPosition()))));
                }
            });
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            mCursor.moveToPosition(position);
            holder.titleView.setText(mCursor.getString(EntryLoader.Query.TITLE));
            holder.subtitleView.setText(
                    DateUtils.getRelativeTimeSpanString(
                            mCursor.getLong(EntryLoader.Query.PUBLISHED_DATE),
                            System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                            DateUtils.FORMAT_ABBREV_ALL).toString()
                            + " by "
                            + mCursor.getString(EntryLoader.Query.AUTHOR));
            Log.v("PICS", mCursor.getString(EntryLoader.Query.THUMB_URL));

            Picasso.with(mContext).load(mCursor.getString(EntryLoader.Query.THUMB_URL)).into(holder.thumbnailView);
        }

        @Override
        public int getItemCount() {
            return mCursor.getCount();
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView thumbnailView;
        public TextView titleView;
        public TextView subtitleView;

        public ViewHolder(View view) {
            super(view);
            thumbnailView = (ImageView) view.findViewById(R.id.thumbnail);
            titleView = (TextView) view.findViewById(R.id.entry_title);
            subtitleView = (TextView) view.findViewById(R.id.entry_subtitle);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mAppBarLayout != null){
            mAppBarLayout.addOnOffsetChangedListener(this);

        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mAppBarLayout != null){
            mAppBarLayout.removeOnOffsetChangedListener(this);

        }
    }

}
