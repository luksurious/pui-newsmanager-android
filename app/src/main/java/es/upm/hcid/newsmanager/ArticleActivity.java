package es.upm.hcid.newsmanager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.InputStream;

import es.upm.hcid.newsmanager.assignment.Article;
import es.upm.hcid.newsmanager.assignment.Image;
import es.upm.hcid.newsmanager.assignment.ModelManager;
import es.upm.hcid.newsmanager.assignment.exceptions.ServerCommunicationError;
import es.upm.hcid.newsmanager.models.MainPreferences;
import es.upm.hcid.newsmanager.models.ServiceFactory;
import es.upm.hcid.newsmanager.models.Utils;

public class ArticleActivity extends AppCompatActivity {
    /**
     * The app's preferences, for easy access
     */
    private MainPreferences preferences;
    /**
     * Connection provider to the server
     */
    private ModelManager connectionManager;

    private Article currentArticle;

    public final static String EXTRA_MESSAGE = "ARTICLE_EXTRA";

    static final int REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);

        preferences = new MainPreferences(getSharedPreferences(MainPreferences.NAME, Context.MODE_PRIVATE));
        ServiceFactory serviceFactory = new ServiceFactory(this, preferences);
        connectionManager = serviceFactory.createModelManager();

        Intent i = getIntent();
        currentArticle = i.getParcelableExtra(ArticleActivity.EXTRA_MESSAGE);
        // TODO: Useful?
        currentArticle.setModelManager(connectionManager);

        setupActionBar();

        fillWithContent(currentArticle);


        final Button button = findViewById(R.id.change_picture);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });
    }


    /**
     * Set matching content foreach UI element
     */
    private void fillWithContent(Article article) {
        TextView title = findViewById(R.id.title_a);
        title.setText(article.getTitleText());

        ImageView image = findViewById(R.id.image_a);
        image.setImageBitmap(Utils.StringToBitMap(article.getThumbnail()));

        TextView abstractText = findViewById(R.id.abstract_a);
        abstractText.setText(article.getAbstractText());

        TextView bodyText = findViewById(R.id.body_a);
        bodyText.setText(article.getAbstractText());

        TextView footerText = findViewById(R.id.footer_a);
        footerText.setText(article.getFooterText());
    }

    /**
     * Set up the custom {@link android.app.ActionBar} / toolbar
     */
    private void setupActionBar() {
        // Show the Up button in the action bar.
        Toolbar myToolbar = (Toolbar) findViewById(R.id.login_toolbar);
        setSupportActionBar(myToolbar);

        // enable back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        InputStream stream = null;
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            try {
                stream = getContentResolver().openInputStream(data.getData());
                Bitmap bitmap = BitmapFactory.decodeStream(stream);
                ImageView imageView = findViewById(R.id.image_a);
                imageView.setImageBitmap(bitmap);

                Image image = new Image(connectionManager, 0, "No description", currentArticle.getId(), es.upm.hcid.newsmanager.assignment.Utils.imgToBase64String(bitmap));
                image.save();
                currentArticle.setImage(image);
                currentArticle.save();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (ServerCommunicationError serverCommunicationError) {
                serverCommunicationError.printStackTrace();
            } finally {
                try {
                    if (stream != null)
                        stream.close();
                } catch (Exception e) {

                }
            }
        }
    }
}
