package com.example.ds.daily_memo_reader.data;

import android.content.ContentResolver;
import android.net.Uri;

/**
 * Created by DS on 2/1/2016.
 */
public class EntriesContract {
    public static final String CONTENT_AUTHORITY = "com.example.ds.daily_memo_reader";
    public static final Uri BASE_URI = Uri.parse("content://com.example.ds.daily_memo_reader");

    interface EntriesColumns {
        /** Type: INTEGER PRIMARY KEY AUTOINCREMENT */
        String _ID = "_id";
        /** Type: TEXT */
        String ENTRY_ID = "entry_id";
        /** Type: TEXT NOT NULL */
        String TITLE = "title";
        /** Type: TEXT NOT NULL */
        String AUTHOR = "author";
        /** Type: TEXT NOT NULL */
        String BODY = "body";
        /** Type: TEXT NOT NULL */
        String THUMB_URL = "thumb_url";
        /** Type: TEXT NOT NULL */
        String PHOTO_URL = "photo_url";
        /** Type: INTEGER NOT NULL DEFAULT 0 */
        String PUBLISHED_DATE = "published_date";
    }

    public static class Entries implements EntriesColumns {
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + ".entries";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + ".entries";

        public static final String DEFAULT_SORT = PUBLISHED_DATE + " DESC";


        /** Matches: /entries/ */
        public static Uri buildDirUri() {
            return BASE_URI.buildUpon().appendPath("entries").build();
        }

        /** Matches: /entries/[_id]/ */
        public static Uri buildEntryUri(long _id) {
            return BASE_URI.buildUpon().appendPath("entries").appendPath(Long.toString(_id)).build();
        }

        /** Read entry ID detail URI. */
        public static long getEntryId(Uri entryUri) {
            return Long.parseLong(entryUri.getPathSegments().get(1));
        }
    }

    private EntriesContract() {
    }
}
