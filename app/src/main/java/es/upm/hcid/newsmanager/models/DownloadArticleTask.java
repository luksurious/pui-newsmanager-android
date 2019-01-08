package es.upm.hcid.newsmanager.models;

import android.os.AsyncTask;
import android.util.Pair;
import android.view.View;

import java.util.List;

import es.upm.hcid.newsmanager.MainActivity;
import es.upm.hcid.newsmanager.assignment.Article;
import es.upm.hcid.newsmanager.assignment.ModelManager;
import es.upm.hcid.newsmanager.assignment.exceptions.ServerCommunicationError;

public class DownloadArticleTask extends AsyncTask<Pair<Integer, Integer>, Integer, List<Article>> {
    /**
     * Connection provider to the server
     */
    private ModelManager connectionManager;

    public DownloadArticleTask(ModelManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    protected void onProgressUpdate(Integer... progress) {

    }

    @Override
    protected List<Article> doInBackground(Pair<Integer, Integer>... pairs) {
        try {
            return connectionManager.getArticles();
        } catch (ServerCommunicationError serverCommunicationError) {
            serverCommunicationError.printStackTrace();
            System.out.println(serverCommunicationError);
        }
        return null;
    }

    protected void onPostExecute(List<Article> result) {
        ArticleAdapter adapter = new ArticleAdapter(MainActivity.ctx, result);
        MainActivity.ctx.getArticleRecyclerView().setAdapter(adapter);
        MainActivity.ctx.getLoadingTextView().setVisibility(View.INVISIBLE);

        // TODO : clean
        for (Article article : result
                ) {
            System.out.println(article.toString());
        }
    }
}
