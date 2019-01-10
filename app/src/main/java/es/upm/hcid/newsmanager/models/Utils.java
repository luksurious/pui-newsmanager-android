package es.upm.hcid.newsmanager.models;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Html;
import android.util.Base64;

public class Utils {
    public static Bitmap stringToBitMap(String encodedString){
        try {
            byte [] encodeByte= Base64.decode(encodedString, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch(Exception e) {
            e.getMessage();
            return null;
        }
    }

    public static String stripHtml(String text) {
        return Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY).toString();
    }
}
