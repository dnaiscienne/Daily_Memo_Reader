package com.example.ds.daily_memo_reader;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.example.ds.myapplication.backend.myApi.MyApi;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;

import java.io.IOException;

/**
 * Created by DS on 1/31/2016.
 */
public class FetchEntriesTask extends AsyncTask<Void, Void, String> {
    public interface OnTaskCompleted{
        void onTaskCompleted(String entries);
    }

    private final String LOG_TAG = FetchEntriesTask.class.getSimpleName();
    private OnTaskCompleted listener;

    public FetchEntriesTask(OnTaskCompleted listener){

        this.listener = listener;
        if (listener instanceof Activity){
            this.progressDialog = new ProgressDialog((Activity)listener);
        }
    }

    private static MyApi myApiService = null;

    ProgressDialog progressDialog;

    @Override
    protected void onPreExecute() {
        if (progressDialog != null){
            progressDialog.setMessage("Retrieving Entries");
            progressDialog.show();
        }
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(Void... params) {
        if(myApiService == null) {  // Only do this once
            MyApi.Builder builder = new MyApi.Builder(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null)
                    .setRootUrl("http://10.0.3.2:8080/_ah/api/") // 10.0.2.2 is localhost's IP address in Android emulator
                    .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
                        @Override
                        public void initialize(AbstractGoogleClientRequest<?> abstractGoogleClientRequest) throws IOException {
                            abstractGoogleClientRequest.setDisableGZipContent(true);
                        }
                    });

            myApiService = builder.build();
        }


        try {
            return myApiService.loadEntries().execute().getData();
        } catch (IOException e) {
            return e.getMessage();
        }
    }

    @Override
    protected void onPostExecute(String result) {
        if(progressDialog != null){
            if(progressDialog.isShowing()){
                progressDialog.dismiss();
            }
        }
        listener.onTaskCompleted(result);
    }
}
