package net.csu333.surrogate.logic;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import net.csu333.surrogate.R;
import net.csu333.surrogate.backend.RuleBackend;
import net.csu333.surrogate.common.PackageRules;
import net.csu333.surrogate.frontend.MainActivity;

/**
 * Created by jp on 4/01/17.
 */

public class Helper {

    private static final String TAG = Helper.class.getCanonicalName();

    public static int importRules(Context context, RuleBackend backend){
        InputStream is = context.getResources().openRawResource(R.raw.rules);
        int importedPackages = 0;
        try {
            Reader isr = new InputStreamReader(is, "UTF-8");
            Type type = new TypeToken<List<PackageRules>>(){}.getType();
            List<PackageRules> array = new Gson().fromJson(isr, type);

            importedPackages = backend.addPackages(array);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return importedPackages;
    }

    //From: http://stackoverflow.com/questions/3028306/download-a-file-with-android-and-showing-the-progress-in-a-progressdialog
    public void importRulesFromInternet(Context context, RuleBackend backend){
        // execute this when the downloader must be fired
        final DownloadTask downloadTask = new DownloadTask(context, backend);
        downloadTask.execute(getRuleSetUrl());
    }

    // usually, subclasses of AsyncTask are declared inside the activity class.
    // that way, you can easily modify the UI thread from here
    private class DownloadTask extends AsyncTask<String, Integer, String> {

        private final Context mContext;
        private final RuleBackend mBackend;

        DownloadTask(Context context, RuleBackend backend) {
            mContext = context;
            mBackend = backend;
        }

        @Override
        protected String doInBackground(String... sUrl) {
            InputStream input;
            StringBuilder output = new StringBuilder();
            HttpURLConnection connection = null;
            try {
                URL url = new URL(sUrl[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage();
                }

                // this will be useful to display download percentage
                // might be -1: server did not report the length
                int fileLength = connection.getContentLength();

                // download the file
                input = connection.getInputStream();

                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    // allow canceling with back button
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    total += count;
                    // publishing the progress....
                    if (fileLength > 0) // only if total length is known
                        publishProgress((int) (total * 100 / fileLength));

                    output.append(new String(data, 0, count, "UTF-8"));
                }
            } catch (Exception e) {
                Log.e(TAG, e.toString());
                return null;
            } finally {
                if (connection != null)
                    connection.disconnect();
            }
            return output.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                Log.d(TAG, result);
                Type type = new TypeToken<List<PackageRules>>(){}.getType();
                List<PackageRules> array = new Gson().fromJson(result, type);

                int importedPackages = mBackend.addPackages(array);

                // Force refresh list view
                ((MainActivity) mContext).reloadPackageList();
                Snackbar.make(((Activity) mContext).findViewById(R.id.package_list), "Imported packages: " + importedPackages, Snackbar.LENGTH_LONG).show();
            } else
                Snackbar.make(((Activity) mContext).findViewById(R.id.package_list), "Nothing downloaded", Snackbar.LENGTH_LONG).show();
        }

    }

    private static String getRuleSetUrl() {
        return "https://raw.githubusercontent.com/csu333/Surrogate/master/app/src/main/res/raw/rules.json";
    }
}
