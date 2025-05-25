package com.ispc.mercadolibromobile.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ispc.mercadolibromobile.R;
import com.ispc.mercadolibromobile.models.Review;

import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private List<Review> reviewList;
    private Context context;
    private boolean showActions;
    private OnReviewActionListener reviewActionListener;

    public interface OnReviewActionListener {
        void onEditReview(Review review);
        void onDeleteReview(int reviewId);
    }

    public ReviewAdapter(List<Review> reviewList, Context context) {
        this.reviewList = reviewList;
        this.context = context;
        this.showActions = false;
    }

    public ReviewAdapter(List<Review> reviewList, Context context, boolean showActions, OnReviewActionListener listener) {
        this.reviewList = reviewList;
        this.context = context;
        this.showActions = showActions;
        this.reviewActionListener = listener;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_resena, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviewList.get(position);

        holder.tvBookTitleAssociated.setText(context.getString(R.string.review_item_book_title_display, review.getTituloLibroAsociado()));
        holder.tvReviewDate.setText(context.getString(R.string.review_item_date_display, review.getFecha()));
        holder.tvReviewContent.setText(review.getContenido());

        if (showActions) {
            holder.reviewActionsLayout.setVisibility(View.VISIBLE);
            holder.btnEditReview.setOnClickListener(v -> {
                if (reviewActionListener != null) {
                    reviewActionListener.onEditReview(review);
                }
            });
            holder.btnDeleteReviewItem.setOnClickListener(v -> {
                if (reviewActionListener != null) {
                    reviewActionListener.onDeleteReview(review.getIdResena());
                }
            });
        } else {
            holder.reviewActionsLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    public static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView tvReviewContent, tvBookTitleAssociated, tvReviewDate;
        View reviewActionsLayout;
        Button btnEditReview, btnDeleteReviewItem;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            tvReviewContent = itemView.findViewById(R.id.tvReviewContent);
            tvBookTitleAssociated = itemView.findViewById(R.id.tvBookTitleAssociated);
            tvReviewDate = itemView.findViewById(R.id.tvReviewDate);
            reviewActionsLayout = itemView.findViewById(R.id.review_actions_layout);
            btnEditReview = itemView.findViewById(R.id.btnEditReview);
            btnDeleteReviewItem = itemView.findViewById(R.id.btnDeleteReviewItem);
        }
    }
}
