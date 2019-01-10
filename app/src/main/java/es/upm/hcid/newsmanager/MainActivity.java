package es.upm.hcid.newsmanager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import es.upm.hcid.newsmanager.assignment.Article;
import es.upm.hcid.newsmanager.assignment.ModelManager;
import es.upm.hcid.newsmanager.models.ArticleAdapter;
import es.upm.hcid.newsmanager.models.DownloadAllArticlesTask;
import es.upm.hcid.newsmanager.models.MainPreferences;
import es.upm.hcid.newsmanager.models.ServiceFactory;
import es.upm.hcid.newsmanager.models.User;

public class MainActivity extends AppCompatActivity {
    /**
     * The app's preferences, for easy access
     */
    private MainPreferences preferences;
    /**
     * Connection provider to the server
     */
    private ModelManager connectionManager;

    private RecyclerView articleRecyclerView;
    private TextView loadingTextView;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // set up common fields
        preferences = new MainPreferences(getSharedPreferences(MainPreferences.NAME, Context.MODE_PRIVATE));
        setupConnection();

        // set up custom action bar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        // Init the Recycler view for article
        articleRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        articleRecyclerView.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        articleRecyclerView.setLayoutManager(mLayoutManager);

        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                loadData();
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeColors(ResourcesCompat.getColor(getResources(), R.color.colorAccent, null));

        loadingTextView = findViewById(R.id.loading_text);
        progressBar = findViewById(R.id.progressBar);
        loadData();
    }

    private void loadData() {
        // display the loading
        loadingTextView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        // asynchronously load article from the server to the recycler view
        new DownloadAllArticlesTask(connectionManager, this).execute();
    }

    public void goToDetails(Article article) {
        Intent intent = new Intent(this, ArticleActivity.class);
        intent.putExtra(ArticleActivity.EXTRA_MESSAGE, article.getId());
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    public void updateUIWithData(List<Article> articleList) {
        swipeContainer.setRefreshing(false);
        loadingTextView.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.INVISIBLE);

        if (articleList == null) {
            Snackbar loadingErrorSnackbar = Snackbar.make(articleRecyclerView, "Failed to load data", Snackbar.LENGTH_INDEFINITE);
            loadingErrorSnackbar.setAction("Retry", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loadData();
                }
            });
            loadingErrorSnackbar.show();
            return;
        }

        if (articleRecyclerView.getAdapter() == null) {
            ArticleAdapter adapter = new ArticleAdapter(this, articleList);
            articleRecyclerView.setAdapter(adapter);
        } else {
            ArticleAdapter adapter = (ArticleAdapter) articleRecyclerView.getAdapter();

            adapter.clear();
            adapter.addAll(articleList);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // load the custom menu options
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // depending on whether the user is logged in or out, show the correct menu items
        MenuItem logoutAction = menu.findItem(R.id.action_logout);
        MenuItem loginAction = menu.findItem(R.id.action_login);
        MenuItem userInfo = menu.findItem(R.id.menu_user_info);
        if (preferences.isUserLoggedIn()) {
            User loggedInUser = preferences.getLoggedInUser();
            userInfo.setTitle("Logged in as " + loggedInUser.getUsername());

            loginAction.setVisible(false);
            logoutAction.setVisible(true);
            userInfo.setVisible(true);
        } else {
            userInfo.setTitle("Logged out");
            loginAction.setVisible(true);
            logoutAction.setVisible(false);
            userInfo.setVisible(false);
        }
        invalidateOptionsMenu();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_login:
                // open login activity
                Intent openLogin = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(openLogin);
                return true;

            case R.id.action_logout:
                // logout: remove saved user info
                connectionManager.logout();
                preferences.logout();
                invalidateOptionsMenu();

                Toast.makeText(this, "You are logged out!", Toast.LENGTH_SHORT).show();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    /**
     * Create a new connection manager
     */
    private void setupConnection() {
        ServiceFactory serviceFactory = new ServiceFactory(this, preferences);
        connectionManager = serviceFactory.createModelManager();
    }
}
