package com.example.ds.daily_memo_reader.data;

import android.content.Context;
import android.content.CursorLoader;
import android.net.Uri;

/**
 * Created by DS on 2/6/2016.
 */
public class EntryLoader extends CursorLoader {
    public static EntryLoader newAllEntriesnstance(Context context) {
        return new EntryLoader(context, EntriesContract.Entries.buildDirUri());
    }

    public static EntryLoader newInstanceForEntryId(Context context, long entryId) {
        return new EntryLoader(context, EntriesContract.Entries.buildEntryUri(entryId));
    }

    private EntryLoader(Context context, Uri uri) {
        super(context, uri, Query.PROJECTION, null, null, EntriesContract.Entries.DEFAULT_SORT);
    }

    public interface Query {
        String[] PROJECTION = {
                EntriesContract.Entries._ID,
                EntriesContract.Entries.TITLE,
                EntriesContract.Entries.PUBLISHED_DATE,
                EntriesContract.Entries.AUTHOR,
                EntriesContract.Entries.THUMB_URL,
                EntriesContract.Entries.PHOTO_URL,
                EntriesContract.Entries.BODY,
        };

        int _ID = 0;
        int TITLE = 1;
        int PUBLISHED_DATE = 2;
        int AUTHOR = 3;
        int THUMB_URL = 4;
        int PHOTO_URL = 5;
        int BODY = 6;
    }
}
