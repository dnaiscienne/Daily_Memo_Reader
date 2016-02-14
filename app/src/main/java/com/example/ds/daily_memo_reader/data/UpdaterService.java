package com.example.ds.daily_memo_reader.data;

import android.app.IntentService;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.util.Log;

import com.example.ds.daily_memo_reader.R;
import com.example.ds.daily_memo_reader.remote.RemoteEndpointUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by DS on 2/6/2016.
 */
public class UpdaterService  extends IntentService {
    private static final String TAG = "UpdaterService";

    public static final String BROADCAST_ACTION_STATE_CHANGE
            = "com.example.ds.daily_memo_reader.intent.action.STATE_CHANGE";
    public static final String EXTRA_REFRESHING
            = "com.example.ds.daily_memo_reader.intent.extra.REFRESHING";
    public static final String ACTION_DATA_UPDATED =
            "com.example.ds.daily_memo_reader.intent.extra.ACTION_DATA_UPDATED";
    public static final int ENTRY_STATUS_OK = 0;
    public static final int ENTRY_STATUS_SERVER_DOWN = 1;
    public static final int ENTRY_STATUS_SERVER_INVALID = 2;
    public static final int ENTRY_STATUS_UNKNOWN = 3;
    public static final int ENTRY_STATUS_INVALID = 4;

    public UpdaterService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Time time = new Time();

        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null || !ni.isConnected()) {
            Log.w(TAG, "Not online, not refreshing.");
            return;
        }

        sendStickyBroadcast(
                new Intent(BROADCAST_ACTION_STATE_CHANGE).putExtra(EXTRA_REFRESHING, true));

        // Don't even inspect the intent, we only do one thing, and that's fetch content.
        ArrayList<ContentProviderOperation> cpo = new ArrayList<ContentProviderOperation>();

        Uri dirUri = EntriesContract.Entries.buildDirUri();
        String selectionClause = EntriesContract.Entries.FAVORITE + " = ?";
        String[] selectionArgs = {"0"};
        // Delete all items
//        cpo.add(ContentProviderOperation.newDelete(dirUri).build());
        getContentResolver().delete(dirUri, selectionClause, selectionArgs);

        try {
            JSONArray array = RemoteEndpointUtil.fetchJsonArray();
            if (array == null) {
                setEntryStatus(this, ENTRY_STATUS_INVALID);
                throw new JSONException("Invalid parsed item array" );
            }

            for (int i = 0; i < array.length(); i++) {
                ContentValues values = new ContentValues();
                JSONObject object = array.getJSONObject(i);
                values.put(EntriesContract.Entries.ENTRY_ID, object.getString("id" ));
                values.put(EntriesContract.Entries.AUTHOR, object.getString("author" ));
                values.put(EntriesContract.Entries.TITLE, object.getString("title" ));
                values.put(EntriesContract.Entries.BODY, object.getString("body" ));
                values.put(EntriesContract.Entries.THUMB_URL, object.getString("thumb" ));
                values.put(EntriesContract.Entries.PHOTO_URL, object.getString("photo" ));
                time.parse3339(object.getString("published_date"));
                values.put(EntriesContract.Entries.PUBLISHED_DATE, time.toMillis(false));
                cpo.add(ContentProviderOperation.newInsert(dirUri).withValues(values).build());
            }

            getContentResolver().applyBatch(EntriesContract.CONTENT_AUTHORITY, cpo);
            updateWidgets(this);
        } catch (JSONException | RemoteException | OperationApplicationException e) {
            Log.e(TAG, "Error updating content.", e);
            setEntryStatus(this, ENTRY_STATUS_SERVER_DOWN);
        }

        sendStickyBroadcast(
                new Intent(BROADCAST_ACTION_STATE_CHANGE).putExtra(EXTRA_REFRESHING, false));
    }

    private void updateWidgets(Context context) {
        Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED)
                .setPackage(context.getPackageName());
        context.sendBroadcast(dataUpdatedIntent);
    }
    static private void setEntryStatus(Context c ,int entryStatus){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c.getApplicationContext());
        SharedPreferences.Editor spe = sp.edit();
        spe.putInt(c.getString(R.string.pref_entry_status_key), entryStatus);
        spe.commit();
    }
}
