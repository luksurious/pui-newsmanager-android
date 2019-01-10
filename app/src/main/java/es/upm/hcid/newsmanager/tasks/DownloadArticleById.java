package es.upm.hcid.newsmanager.tasks;

import android.os.AsyncTask;
import android.util.Log;

import es.upm.hcid.newsmanager.ArticleActivity;
import es.upm.hcid.newsmanager.assignment.Article;
import es.upm.hcid.newsmanager.assignment.ModelManager;
import es.upm.hcid.newsmanager.assignment.exceptions.ServerCommunicationError;

/**
 * Task to download all details of a single task
 */
public class DownloadArticleById extends AsyncTask<Integer, Integer, Article> {
    /**
     * Connection provider to the server
     */
    private ModelManager connectionManager;

    /**
     * Reference to the activity calling the task
     */
    private ArticleActivity activity;

    public DownloadArticleById(ModelManager connectionManager, ArticleActivity activity) {
        this.connectionManager = connectionManager;
        this.activity = activity;
    }

    @Override
    protected Article doInBackground(Integer... article_id) {
        try {
            return connectionManager.getArticle(article_id[0]);
        } catch (ServerCommunicationError e) {
            Log.e("DownloadArticle", "ServerCommunicationError: " + e.getLocalizedMessage());
            e.printStackTrace();
        }
        return null;
    }

    protected void onPostExecute(Article result) {
        // update UI with data
        activity.updateWithArticleInfo(result);
    }
}
