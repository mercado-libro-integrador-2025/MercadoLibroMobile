package com.ispc.mercadolibromobile.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ispc.mercadolibromobile.R;
import com.ispc.mercadolibromobile.models.Review;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Date; // Asegúrate de importar java.util.Date

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private List<Review> reviews;
    private Context context;
    private boolean showActions;
    private OnReviewActionListener listener;

    public interface OnReviewActionListener {
        void onEditReview(Review review);
        void onDeleteReview(int reviewId);
    }

    public ReviewAdapter(List<Review> reviews, Context context, boolean showActions, OnReviewActionListener listener) {
        this.reviews = reviews;
        this.context = context;
        this.showActions = showActions;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_resena, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviews.get(position);

        holder.tvBookTitleAssociated.setText(review.getTituloLibroAsociado());
        holder.tvReviewContent.setText(review.getContenido());

        String fechaOriginal = review.getFecha();
        if (fechaOriginal != null && !fechaOriginal.isEmpty()) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault());
                Date date = inputFormat.parse(fechaOriginal);

                SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                String formattedDate = outputFormat.format(date);
                holder.tvReviewDate.setText(formattedDate);
            } catch (ParseException e) {
                e.printStackTrace();
                holder.tvReviewDate.setText(R.string.review_item_date_placeholder); // Fallback en caso de error
            }
        } else {
            holder.tvReviewDate.setText(R.string.review_item_date_placeholder);
        }

        if (showActions) {
            holder.reviewActionsLayout.setVisibility(View.VISIBLE);
            holder.btnEditReview.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditReview(review);
                }
            });
            holder.btnDeleteReviewItem.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteReview(review.getIdResena());
                }
            });
        } else {
            holder.reviewActionsLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    public static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView tvBookTitleAssociated;
        TextView tvReviewDate;
        TextView tvReviewContent;
        LinearLayout reviewActionsLayout;
        Button btnEditReview;
        Button btnDeleteReviewItem;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBookTitleAssociated = itemView.findViewById(R.id.tvBookTitleAssociated);
            tvReviewDate = itemView.findViewById(R.id.tvReviewDate);
            tvReviewContent = itemView.findViewById(R.id.tvReviewContent);
            reviewActionsLayout = itemView.findViewById(R.id.review_actions_layout);
            btnEditReview = itemView.findViewById(R.id.btnEditReview);
            btnDeleteReviewItem = itemView.findViewById(R.id.btnDeleteReviewItem);
        }
    }
}