package com.example.ds.daily_memo_reader;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

import com.example.ds.daily_memo_reader.data.UpdaterService;

/**
 * Created by DS on 2/14/2016.
 */
public class Utility {
    /**
     * Returns true if the network is available or about to become available.
     *
     * @param c Context used to get the ConnectivityManager
     * @return true if the network is available
     */
    static public boolean isNetworkAvailable(Context c) {
        ConnectivityManager cm =
                (ConnectivityManager)c.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    static public int getEntryStatus(Context c){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c.getApplicationContext());
        return sp.getInt(c.getString(R.string.pref_entry_status_key), UpdaterService.ENTRY_STATUS_UNKNOWN);
    }
}
