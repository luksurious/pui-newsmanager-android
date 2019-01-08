package es.upm.hcid.newsmanager.models;

import android.os.AsyncTask;
import android.util.Pair;

import java.util.List;

import es.upm.hcid.newsmanager.ArticleActivity;
import es.upm.hcid.newsmanager.assignment.Article;
import es.upm.hcid.newsmanager.assignment.Image;
import es.upm.hcid.newsmanager.assignment.ModelManager;
import es.upm.hcid.newsmanager.assignment.exceptions.ServerCommunicationError;

public class UploadPictureTask extends AsyncTask<Pair<Article, Image>, Integer, Image> {

    /**
     * Connection provider to the server
     */
    private ArticleActivity activity;

    public UploadPictureTask(ArticleActivity activity) {
        this.activity = activity;
    }

    protected void onPostExecute(Image result) {
        activity.updatePic(result);
    }

    @Override
    protected Image doInBackground(Pair<Article, Image>... pairs) {
        for (Pair<Article, Image> pair: pairs
             ) {
            try {
                ///pair.second.save();
                pair.first.save();
                return pair.second;
            } catch (ServerCommunicationError serverCommunicationError) {
                serverCommunicationError.printStackTrace();
            }
        }
        return null;
    }
}
