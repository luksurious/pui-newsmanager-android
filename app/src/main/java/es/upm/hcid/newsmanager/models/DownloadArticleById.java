package es.upm.hcid.newsmanager.models;

import android.os.AsyncTask;

import es.upm.hcid.newsmanager.ArticleActivity;
import es.upm.hcid.newsmanager.assignment.Article;
import es.upm.hcid.newsmanager.assignment.ModelManager;
import es.upm.hcid.newsmanager.assignment.exceptions.ServerCommunicationError;

public class DownloadArticleById extends AsyncTask<Integer, Integer, Article> {
    /**
     * Connection provider to the server
     */
    private ModelManager connectionManager;
    private ArticleActivity activity;

    public DownloadArticleById(ModelManager connectionManager, ArticleActivity activity) {
        this.connectionManager = connectionManager;
        this.activity = activity;
    }

    protected void onProgressUpdate(Integer... progress) {

    }

    @Override
    protected Article doInBackground(Integer... article_id) {
        try {
            return connectionManager.getArticle(article_id[0]);
        } catch (ServerCommunicationError serverCommunicationError) {
            serverCommunicationError.printStackTrace();
            System.out.println(serverCommunicationError);
        }
        return null;
    }

    protected void onPostExecute(Article result) {
        result.setModelManager(connectionManager);
        activity.updateWithArticleInfo(result);
    }
}
