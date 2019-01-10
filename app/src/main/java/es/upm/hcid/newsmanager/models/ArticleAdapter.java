package es.upm.hcid.newsmanager.models;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import es.upm.hcid.newsmanager.MainActivity;
import es.upm.hcid.newsmanager.R;
import es.upm.hcid.newsmanager.assignment.Article;

public class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.ViewHolder> {
    private MainActivity mContext;
    private List<Article> articles;

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public TextView abstractView;
        public ImageView thumbnail;
        public CardView card;
        public TextView category;

        public ViewHolder(@NonNull View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.title);
            abstractView = (TextView) view.findViewById(R.id.abstractView);
            category = (TextView) view.findViewById(R.id.category);
            thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
            card = (CardView) view.findViewById(R.id.card_view);
        }
    }


    public ArticleAdapter(MainActivity mContext, List<Article> articles) {
        this.mContext = mContext;
        this.articles = articles;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        final Article article = articles.get(position);
        holder.title.setText(article.getTitleText());
        holder.abstractView.setText(Utils.stripHtml(article.getAbstractText()));
        holder.category.setText(article.getCategory().toUpperCase());
        Bitmap tn = Utils.stringToBitMap(article.getThumbnail());
        if (tn == null) {
            holder.thumbnail.setImageResource(R.drawable.ic_broken_image_black_24dp);
            holder.thumbnail.setAlpha(0.5f);
        } else {
            holder.thumbnail.setImageBitmap(tn);
            holder.thumbnail.setAlpha(1f);
        }

        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mContext.goToDetails(articles.get(position));
            }
        });
        holder.thumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mContext.goToDetails(articles.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        if (articles == null) {
            return 0;
        }

        return articles.size();
    }

    public void clear() {
        articles.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<Article> list) {
        articles.addAll(list);
        notifyDataSetChanged();
    }
}
