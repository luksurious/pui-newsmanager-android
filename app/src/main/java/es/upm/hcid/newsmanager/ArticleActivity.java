package es.upm.hcid.newsmanager;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import es.upm.hcid.newsmanager.assignment.Article;
import es.upm.hcid.newsmanager.assignment.Image;
import es.upm.hcid.newsmanager.assignment.ModelManager;
import es.upm.hcid.newsmanager.models.DownloadArticleById;
import es.upm.hcid.newsmanager.models.MainPreferences;
import es.upm.hcid.newsmanager.models.ServiceFactory;
import es.upm.hcid.newsmanager.models.UploadPictureTask;
import es.upm.hcid.newsmanager.models.Utils;

import static es.upm.hcid.newsmanager.assignment.Utils.imgToBase64String;

public class ArticleActivity extends AppCompatActivity implements ImageSourceListDialogFragment.Listener {
    /**
     * The app's preferences, for easy access
     */
    private MainPreferences preferences;
    /**
     * Connection provider to the server
     */
    private ModelManager connectionManager;

    private ProgressBar progressBar;

    private Article currentArticle;

    public final static String EXTRA_MESSAGE = "ARTICLE_EXTRA";

    private static String TAG = "ArticleActivity";

    static final int REQUEST_LOAD_IMAGE_CODE = 1;
    static final int REQUEST_IMAGE_CAPTURE = 2;

    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;

    private String mCurrentPhotoPath;
    private FloatingActionButton changePictureButton;
    private ImageView imageView;
    private CollapsingToolbarLayout toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);

        preferences = new MainPreferences(getSharedPreferences(MainPreferences.NAME, Context.MODE_PRIVATE));
        ServiceFactory serviceFactory = new ServiceFactory(this, preferences);
        connectionManager = serviceFactory.createModelManager();

        setupActionBar();

        progressBar = findViewById(R.id.progressBar2);
        progressBar.setVisibility(View.VISIBLE);

        changePictureButton = findViewById(R.id.change_picture_fab);
        changePictureButton.setVisibility(View.INVISIBLE);
        changePictureButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ImageSourceListDialogFragment.newInstance().show(getSupportFragmentManager(), "dialog");
            }
        });

        imageView = findViewById(R.id.image_a);

        toolbar = findViewById(R.id.collapsing_toolbar);

        Intent i = getIntent();
        int currentArticle_id = i.getIntExtra(ArticleActivity.EXTRA_MESSAGE, 0);
        new DownloadArticleById(connectionManager, this).execute(currentArticle_id);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    public boolean onSupportNavigateUp() {
        boolean result = super.onSupportNavigateUp();

        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

        return result;
    }

    public void updateWithArticleInfo(Article article) {
        progressBar.setVisibility(View.GONE);
        currentArticle = article;
        currentArticle.setModelManager(connectionManager);
        if (preferences.isUserLoggedIn()) {
            changePictureButton.setVisibility(View.VISIBLE);
        } else {
            changePictureButton.setVisibility(View.INVISIBLE);
        }
        fillWithContent(currentArticle);

        toolbar.setTitle(currentArticle.getTitleText());
    }

    /**
     * Set matching content foreach UI element
     */
    private void fillWithContent(Article article) {
        TextView title = findViewById(R.id.title_a);
        title.setText(article.getTitleText());

        ImageView image = findViewById(R.id.image_a);
        image.setImageBitmap(Utils.stringToBitMap(article.getImage().getImage()));

        TextView abstractText = findViewById(R.id.abstract_a);

        abstractText.setText(Utils.stripHtml(article.getAbstractText()));

        TextView bodyText = findViewById(R.id.body_a);
        bodyText.setText(Utils.stripHtml(article.getBodyText()));
    }

    /**
     * Set up the custom {@link android.app.ActionBar} / toolbar
     */
    private void setupActionBar() {
        // Show the Up button in the action bar.
        Toolbar myToolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(myToolbar);

        // enable back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_LOAD_IMAGE_CODE && resultCode == Activity.RESULT_OK) {
            try (InputStream stream = getContentResolver().openInputStream(data.getData())) {
                Bitmap bitmap = BitmapFactory.decodeStream(stream);

                if (bitmap == null) {
                    showErrorSnackbar("The selected image seems broken");
                } else {
                    updateArticleImage(bitmap);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            Bitmap imageBitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);
            updateArticleImage(imageBitmap);
        }
    }

    private void updateArticleImage(Bitmap bitmap) {
        imageView.setImageBitmap(bitmap);
        progressBar.setVisibility(View.VISIBLE);

        Image image = new Image(connectionManager, 0, "No description", currentArticle.getId(), imgToBase64String(bitmap));

        new UploadPictureTask(this).execute(new Pair<Article, Image>(currentArticle, image));
    }

    private void dispatchLoadImageIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, REQUEST_LOAD_IMAGE_CODE);
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(this, "Unfortunately, this only works if you give permission to write to the external storage", Toast.LENGTH_LONG).show();
            } else {
                // No explanation needed; request the permission
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            }
            return;
        }

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.i(TAG, "IOException");
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "es.upm.hcid.newsmanager.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    dispatchTakePictureIntent();
                } else {
                    Toast.makeText(this, "You will not be able to take pictures with the camera!", Toast.LENGTH_LONG).show();
                }
            }
        }
    }


    @Override
    public void onGalleryPhotoClicked() {
        dispatchLoadImageIntent();
    }

    @Override
    public void onCameraClicked() {
        dispatchTakePictureIntent();
    }

    public void notifyUploadSuccess(Image image) {
        progressBar.setVisibility(View.GONE);

        currentArticle.setImage(image);
    }

    public void notifyImageConversionError(Image image) {
        notifyUploadSuccess(image);

        showErrorSnackbar("The server did not correctly convert the image. The thumbnail will be broken.");
    }

    private void showErrorSnackbar(String msg) {
        final Snackbar loadingErrorSnackbar = Snackbar.make(
                findViewById(R.id.article_root_layout),
                msg,
                Snackbar.LENGTH_INDEFINITE
        );
        loadingErrorSnackbar.setAction("Dismiss", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingErrorSnackbar.dismiss();
            }
        });
        loadingErrorSnackbar.show();
    }

    public void notifyUploadFailure() {
        // revert image
        imageView.setImageBitmap(Utils.stringToBitMap(currentArticle.getImage().getImage()));
        progressBar.setVisibility(View.GONE);
        showErrorSnackbar("An error occurred updating the image.");
    }
}
