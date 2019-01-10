package es.upm.hcid.newsmanager.tasks;

import android.os.AsyncTask;
import android.util.Log;

import java.util.List;

import es.upm.hcid.newsmanager.MainActivity;
import es.upm.hcid.newsmanager.assignment.Article;
import es.upm.hcid.newsmanager.assignment.ModelManager;
import es.upm.hcid.newsmanager.assignment.exceptions.ServerCommunicationError;

/**
 * Task which loads all articles from the server
 */
public class DownloadAllArticlesTask extends AsyncTask<Void, Integer, List<Article>> {
    /**
     * Connection provider to the server
     */
    private ModelManager connectionManager;

    /**
     * Reference to the activity calling the task
     */
    private MainActivity activity;

    public DownloadAllArticlesTask(ModelManager connectionManager, MainActivity activity) {
        this.connectionManager = connectionManager;
        this.activity = activity;
    }

    @Override
    protected List<Article> doInBackground(Void... pairs) {
        try {
            return connectionManager.getArticles();
        } catch (ServerCommunicationError e) {
            Log.e("DownloadAllArticles", "ServerCommunicationError: " + e.getLocalizedMessage());
            e.printStackTrace();
        }
        return null;
    }

    protected void onPostExecute(List<Article> result) {
        // call activity method to update the UI with the data
        activity.updateUIWithData(result);
    }
}
