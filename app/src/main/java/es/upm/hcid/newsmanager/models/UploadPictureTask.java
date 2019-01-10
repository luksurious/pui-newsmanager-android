package es.upm.hcid.newsmanager.models;

import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;

import es.upm.hcid.newsmanager.ArticleActivity;
import es.upm.hcid.newsmanager.assignment.Article;
import es.upm.hcid.newsmanager.assignment.Image;
import es.upm.hcid.newsmanager.assignment.exceptions.ServerCommunicationError;

public class UploadPictureTask extends AsyncTask<Pair<Article, Image>, Integer, Image> {

    /**
     * Connection provider to the server
     */
    private ArticleActivity activity;

    private boolean hasImageConversionError = false;

    public UploadPictureTask(ArticleActivity activity) {
        this.activity = activity;
    }

    protected void onPostExecute(Image result) {
        if (result == null) {
            activity.notifyUploadFailure();
        } else if (hasImageConversionError) {
            activity.notifyImageConversionError(result);
        } else {
            activity.notifyUploadSuccess(result);
        }
    }

    @Override
    protected Image doInBackground(Pair<Article, Image>... pairs) {
        for (Pair<Article, Image> pair: pairs) {
            try {
                pair.second.save();
                return pair.second;
            } catch (ServerCommunicationError serverCommunicationError) {
                if (serverCommunicationError.getMessage().contains("thumbnail conversion failed")) {
                    Log.w("UploadPicture", serverCommunicationError.getMessage());
                    hasImageConversionError = true;
                    return pair.second;
                } else {
                    serverCommunicationError.printStackTrace();
                }
            }
        }
        return null;
    }
}
