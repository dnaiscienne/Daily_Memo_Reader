package com.example.ds.daily_memo_reader.widget;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.example.ds.daily_memo_reader.R;
import com.example.ds.daily_memo_reader.data.EntriesContract;
import com.example.ds.daily_memo_reader.data.EntryLoader;


/**
 * Created by DS on 2/13/2016.
 */

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class MemoWidgetRemoteViewsService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {
                // Nothing to do
            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }
                final long identityToken = Binder.clearCallingIdentity();
                data = getContentResolver().query(
                        EntriesContract.Entries.buildDirUri(),
                        null,
                        null,
                        null,
                        EntriesContract.Entries.DEFAULT_SORT);
                Binder.restoreCallingIdentity(identityToken);
                Log.v("Widget", Integer.toString(data.getCount()));
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.widget_memo_list_item);

                String title = data.getString(data.getColumnIndex(EntriesContract.Entries.TITLE));
                String subtitle = " by " + data.getString(EntryLoader.Query.AUTHOR);


                views.setTextViewText(R.id.widget_entry_title, title);
                views.setTextViewText(R.id.widget_entry_subtitle, subtitle);

                final Intent fillInIntent = new Intent();
                long itemId = data.getLong(data.getColumnIndex(EntriesContract.Entries._ID));
                Uri entryUri = EntriesContract.Entries.buildEntryUri(itemId);
                fillInIntent.setData(entryUri);
                views.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);
                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_memo_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position))
                    return data.getInt(data.getColumnIndex(EntriesContract.Entries._ID));
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
