package es.upm.hcid.newsmanager.models;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Html;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.Nullable;

/**
 * Miscellaneous utility methods
 */
public class Utils {
    /**
     * Converts a base64 encoded string to bitmap
     *
     * @param encodedString The base64 encoded string
     * @return The image as a bitmap or null on failure
     */
    @Nullable
    public static Bitmap base64StringToBitmap(String encodedString) {
        try {
            byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
        } catch (Exception e) {
            Log.e("base64StringToBitmap", e.getLocalizedMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Strips HTML tags from a text
     *
     * @param text The text with HTML tags
     * @return Text without HTML tags
     */
    public static String stripHtml(String text) {
        return Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY).toString();
    }
}
