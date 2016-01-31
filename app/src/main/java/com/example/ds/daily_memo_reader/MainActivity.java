package com.example.ds.daily_memo_reader;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements FetchEntriesTask.OnTaskCompleted{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {super.onStart();
       fetchEntries();
    }
    private void fetchEntries(){
        FetchEntriesTask fetchTask = new FetchEntriesTask(this);
        fetchTask.execute();
    }
    @Override
    public void onTaskCompleted(String entries) {
        TextView tv = (TextView) findViewById(R.id.test);
        tv.setText(entries);

    }
}
