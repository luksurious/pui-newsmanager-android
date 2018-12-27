package es.upm.hcid.newsmanager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import es.upm.hcid.newsmanager.assignment.Article;

public class ArticleActivity extends AppCompatActivity {

    public final static String EXTRA_MESSAGE = "ARTICLE_EXTRA";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);

        Intent i = getIntent();
        Article article = (Article) i.getParcelableExtra(ArticleActivity.EXTRA_MESSAGE);

        setupActionBar();

        fillWithContent(article);
    }


    /**
     * Set matching content foreach UI element
     */
    private void fillWithContent(Article article) {
        TextView title = findViewById(R.id.title_a);
        title.setText(article.getTitleText());
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
}
