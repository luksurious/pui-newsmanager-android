package es.upm.hcid.newsmanager.models;

import android.os.AsyncTask;

import java.util.List;

import es.upm.hcid.newsmanager.MainActivity;
import es.upm.hcid.newsmanager.assignment.Article;
import es.upm.hcid.newsmanager.assignment.ModelManager;
import es.upm.hcid.newsmanager.assignment.exceptions.ServerCommunicationError;

public class DownloadAllArticlesTask extends AsyncTask<Void, Integer, List<Article>> {
    /**
     * Connection provider to the server
     */
    private ModelManager connectionManager;
    private MainActivity activity;

    public DownloadAllArticlesTask(ModelManager connectionManager, MainActivity activity) {
        this.connectionManager = connectionManager;
        this.activity = activity;
    }

    @Override
    protected List<Article> doInBackground(Void... pairs) {
        try {
            return connectionManager.getArticles();
        } catch (ServerCommunicationError serverCommunicationError) {
            serverCommunicationError.printStackTrace();
        }
        return null;
    }

    protected void onPostExecute(List<Article> result) {
        activity.updateUIWithData(result);
    }
}
