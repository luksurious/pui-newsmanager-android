package es.upm.hcid.newsmanager.tasks;

import android.os.AsyncTask;
import android.util.Log;

import es.upm.hcid.newsmanager.ArticleActivity;
import es.upm.hcid.newsmanager.assignment.Image;
import es.upm.hcid.newsmanager.assignment.exceptions.ServerCommunicationError;

/**
 * Task to upload a new picture to an article
 */
public class UploadPictureTask extends AsyncTask<Image, Integer, Image> {

    /**
     * Reference to the activity calling the task
     */
    private ArticleActivity activity;

    /**
     * Whether the uploading had the special thumbnail error
     */
    private boolean hasImageConversionError = false;

    public UploadPictureTask(ArticleActivity activity) {
        this.activity = activity;
    }

    @Override
    protected Image doInBackground(Image... pairs) {
        for (Image image : pairs) {
            try {
                image.save();
                return image;
            } catch (ServerCommunicationError e) {
                if (e.getMessage().contains("thumbnail conversion failed")) {
                    Log.w("UploadPicture", e.getMessage());
                    hasImageConversionError = true;
                    return image;
                } else {
                    Log.e("UploadPicture", e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        return null;
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
}
