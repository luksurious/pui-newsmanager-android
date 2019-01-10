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
    // key for intent data
    public final static String EXTRA_MESSAGE = "ARTICLE_EXTRA";
    // tag for logging
    private static String TAG = "ArticleActivity";

    // codes for activities & requests
    private static final int REQUEST_LOAD_IMAGE_CODE = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;
    private static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;

    /**
     * The app's preferences, for easy access
     */
    private MainPreferences preferences;
    /**
     * Connection provider to the server
     */
    private ModelManager connectionManager;

    // UI elements
    private ProgressBar progressBar;
    private FloatingActionButton changePictureButton;
    private ImageView imageView;
    private CollapsingToolbarLayout toolbar;

    /**
     * Article open in this activity
     */
    private Article currentArticle;

    /**
     * If photo taken from the camera, holds the path to it
     */
    private String mCurrentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);

        // load main preferences
        preferences = new MainPreferences(getSharedPreferences(MainPreferences.NAME, Context.MODE_PRIVATE));
        // create connection to server
        ServiceFactory serviceFactory = new ServiceFactory(this, preferences);
        connectionManager = serviceFactory.createModelManager();

        setupActionBar();

        // show loading bar
        progressBar = findViewById(R.id.progressBar2);
        progressBar.setVisibility(View.VISIBLE);

        // set up button to change picture
        changePictureButton = findViewById(R.id.change_picture_fab);
        changePictureButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ImageSourceListDialogFragment.newInstance().show(getSupportFragmentManager(), "dialog");
            }
        });
        // hide by default, show only if logged in
        changePictureButton.setVisibility(View.INVISIBLE);

        imageView = findViewById(R.id.image_a);
        toolbar = findViewById(R.id.collapsing_toolbar);

        loadArticleDetails();
    }

    private void loadArticleDetails() {
        Intent i = getIntent();
        int currentArticle_id = i.getIntExtra(ArticleActivity.EXTRA_MESSAGE, 0);
        if (currentArticle_id == 0) {
            showErrorSnackbar(getString(R.string.article_loading_empty_id_error));
            return;
        }
        new DownloadArticleById(connectionManager, this).execute(currentArticle_id);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // show sliding transition if system's back button is pressed
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    public boolean onSupportNavigateUp() {
        boolean result = super.onSupportNavigateUp();

        // show sliding transition if back/up button in the toolbar is pressed
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

        return result;
    }

    /**
     * Update the UI after the article data has been loaded
     * @param article The fully loaded article
     */
    public void updateWithArticleInfo(Article article) {
        // hide progress bar
        progressBar.setVisibility(View.GONE);

        currentArticle = article;

        // show edit button if the user is logged in
        if (preferences.isUserLoggedIn()) {
            changePictureButton.setVisibility(View.VISIBLE);
        } else {
            changePictureButton.setVisibility(View.INVISIBLE);
        }
        fillWithContent(currentArticle);

        // update toolbar title with article title
        toolbar.setTitle(currentArticle.getTitleText());
    }

    /**
     * Set matching content for each UI element
     */
    private void fillWithContent(Article article) {
        TextView title = findViewById(R.id.title_a);
        title.setText(article.getTitleText());

        ImageView image = findViewById(R.id.image_a);
        image.setImageBitmap(Utils.stringToBitMap(article.getImage().getImage()));

        TextView abstractText = findViewById(R.id.abstract_a);
        // remove HTML from abstract
        abstractText.setText(Utils.stripHtml(article.getAbstractText()));

        TextView bodyText = findViewById(R.id.body_a);
        // remove HTML from body
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_LOAD_IMAGE_CODE && resultCode == Activity.RESULT_OK) {
            // handle result of loading image from the gallery
            Uri selectedImage = data.getData();
            try (InputStream stream = getContentResolver().openInputStream(selectedImage)) {
                Bitmap bitmap = BitmapFactory.decodeStream(stream);

                // check that the image could correctly be loaded
                if (bitmap == null) {
                    showErrorSnackbar(getString(R.string.broken_image_toast));
                } else {
                    updateArticleImage(bitmap);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading image from gallery: " + e.getLocalizedMessage());
                e.printStackTrace();
            }
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            // handle capturing an image with the camera
            Bitmap imageBitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);
            updateArticleImage(imageBitmap);
        }
    }

    /**
     * Update the article's image with the selected bitmap, sending it to the server.
     * Takes an optimistic approach, assuming that it will work, so the header will be replaced
     * directly.
     *
     * @param bitmap The new image
     */
    private void updateArticleImage(Bitmap bitmap) {
        // update header image
        imageView.setImageBitmap(bitmap);

        // show loading bar
        progressBar.setVisibility(View.VISIBLE);

        // create new image model
        Image image = new Image(connectionManager, 0, "", currentArticle.getId(), imgToBase64String(bitmap));

        // send to server
        new UploadPictureTask(this).execute(new Pair<>(currentArticle, image));
    }

    /**
     * Intent to select image from the gallery
     */
    private void dispatchLoadImageIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, REQUEST_LOAD_IMAGE_CODE);
    }

    /**
     * Intent to take a picture via the camera
     */
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // check for storage permissions, request if not given
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(this, getString(R.string.storage_permission_rationale), Toast.LENGTH_LONG).show();
            } else {
                // No explanation needed; request the permission
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            }
            return;
        }

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the file where the photo should go
            File photoFile = null;
            try {
                photoFile = createEmptyImageFileForCamera();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.e(TAG, "IOException: " + ex.getLocalizedMessage());
                ex.printStackTrace();
            }
            // Continue only if the file was successfully created
            if (photoFile != null) {
                // tell the camera where to store the picture
                Uri photoURI = FileProvider.getUriForFile(this,
                        "es.upm.hcid.newsmanager.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    /**
     * Create a unique name for a new image taken by the camera. The camera will store it here,
     * and then we can retrieve the taken picture from there.
     *
     * @return The file object with the new file name
     */
    private File createEmptyImageFileForCamera() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",   /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission is granted, retry the camera intent
                    dispatchTakePictureIntent();
                } else {
                    Toast.makeText(this, getString(R.string.storage_permission_denied_toast), Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    // ImageSourceListDialogFragment.Listener callbacks
    @Override
    public void onGalleryPhotoClicked() {
        dispatchLoadImageIntent();
    }
    @Override
    public void onCameraClicked() {
        dispatchTakePictureIntent();
    }

    // image upload callbacks
    public void notifyUploadSuccess(Image image) {
        progressBar.setVisibility(View.GONE);

        currentArticle.setImage(image);
    }

    /**
     * If the image was uploaded, but the thumbnail could not be created correctly, notify the user
     * but save the image as updated.
     * @param image The updated image
     */
    public void notifyImageConversionError(Image image) {
        notifyUploadSuccess(image);

        showErrorSnackbar(getString(R.string.upload_image_conversion_issue_toast));
    }

    /**
     * Uploading of the image failed for another reason
     */
    public void notifyUploadFailure() {
        // revert image
        imageView.setImageBitmap(Utils.stringToBitMap(currentArticle.getImage().getImage()));
        progressBar.setVisibility(View.GONE);
        showErrorSnackbar(getString(R.string.upload_image_error_toast));
    }

    /**
     * Helper function to show a dismissable snackbar with a message
     * @param msg The message to show in the snackbar
     */
    private void showErrorSnackbar(String msg) {
        final Snackbar loadingErrorSnackbar = Snackbar.make(
                findViewById(R.id.article_root_layout),
                msg,
                Snackbar.LENGTH_INDEFINITE
        );
        // add dismiss action
        loadingErrorSnackbar.setAction(getString(R.string.snackbar_dismiss_action), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingErrorSnackbar.dismiss();
            }
        });
        loadingErrorSnackbar.show();
    }
}
