package com.example.ds.daily_memo_reader.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.example.ds.daily_memo_reader.R;
import com.example.ds.daily_memo_reader.data.EntriesContract;

/**
 * Created by DS on 2/6/2016.
 */
public class EntryListActivity extends AppCompatActivity implements  EntryListFragment.Callback {

    private boolean mTwoPane;
    private static final String ENTRYDETAILFRAGMENT_TAG = "EDFTAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_list);

        if (findViewById(R.id.entry_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }else{
            mTwoPane = false;
        }

    }

    @Override
    public void onItemSelected(Bundle b) {
        long itemId = b.getLong(EntryDetailFragment.ARG_ITEM_ID);

        if(mTwoPane){
            EntryDetailFragment fragment = EntryDetailFragment.newInstance(itemId, true);
            getFragmentManager().beginTransaction()
                    .replace(R.id.entry_detail_container, fragment, ENTRYDETAILFRAGMENT_TAG)
                    .commit();
        }else{
            startActivity(new Intent(Intent.ACTION_VIEW,
                    EntriesContract.Entries.buildEntryUri(itemId)));
        }

    }
}
