package com.ispc.mercadolibromobile.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ispc.mercadolibromobile.R;
import com.ispc.mercadolibromobile.utils.SessionUtils;
import com.ispc.mercadolibromobile.adapters.ReviewAdapter;
import com.ispc.mercadolibromobile.models.Review;
import com.ispc.mercadolibromobile.api.ApiService;
import com.ispc.mercadolibromobile.api.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyReviewsFragment extends Fragment implements ReviewAdapter.OnReviewActionListener {

    private RecyclerView recyclerViewMyReviews;
    private Button btnCreateNewReview;
    private ReviewAdapter reviewAdapter;
    private ProgressBar progressBar;
    private ApiService apiService;

    private static final String TAG = "MyReviewsFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_reviews, container, false);

        recyclerViewMyReviews = view.findViewById(R.id.recyclerViewMyReviews);
        btnCreateNewReview = view.findViewById(R.id.btnCreateNewReview);
        progressBar = view.findViewById(R.id.progressBar);
        recyclerViewMyReviews.setLayoutManager(new LinearLayoutManager(getContext()));

        apiService = RetrofitClient.getApiService(requireContext());

        btnCreateNewReview.setOnClickListener(v -> {
            if (isAdded()) {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, CreateEditReviewFragment.newInstance(0, null))
                        .addToBackStack(null)
                        .commit();
            }
        });

        loadMyReviews();

        return view;
    }

    private void loadMyReviews() {
        String token = SessionUtils.getAuthToken(getContext());

        if (token == null) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(requireContext(), getString(R.string.error_not_logged_in_review), Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        apiService.getMyReviews("Bearer " + token).enqueue(new Callback<List<Review>>() {
            @Override
            public void onResponse(@NonNull Call<List<Review>> call, @NonNull Response<List<Review>> response) {
                if (isAdded()) {
                    progressBar.setVisibility(View.GONE);
                    if (response.isSuccessful() && response.body() != null) {
                        List<Review> myReviews = response.body();

                        reviewAdapter = new ReviewAdapter(myReviews, requireContext(), true, MyReviewsFragment.this);
                        recyclerViewMyReviews.setAdapter(reviewAdapter);
                        if (myReviews.isEmpty()) {
                            Toast.makeText(requireContext(), getString(R.string.no_reviews_found), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, "Error al cargar tus reseñas: " + response.code() + " - " + response.message());
                        try {
                            String errorBody = response.errorBody() != null ? response.errorBody().string() : "No error body";
                            Log.e(TAG, "Error body: " + errorBody);
                        } catch (Exception e) {
                            Log.e(TAG, "Error reading error body: " + e.getMessage());
                        }
                        Toast.makeText(requireContext(), getString(R.string.error_loading_my_reviews, response.message()), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Review>> call, @NonNull Throwable t) {
                if (isAdded()) {
                    progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "Error de red al cargar reseñas: " + t.getMessage(), t);
                    Toast.makeText(requireContext(), getString(R.string.error_network_connection_reviews), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onEditReview(Review review) {
        if (isAdded()) {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, CreateEditReviewFragment.newInstance(review.getIdResena(), review.getIdLibro()))
                    .addToBackStack(null)
                    .commit();
        }
    }

    @Override
    public void onDeleteReview(int reviewId) {
        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.confirm_delete_review))
                .setMessage(getString(R.string.confirm_delete_review_id, reviewId))
                .setPositiveButton(getString(R.string.dialog_confirm_delete_yes_button), (dialog, which) -> {
                    String token = SessionUtils.getAuthToken(requireContext());
                    if (token != null) {
                        apiService.deleteReview("Bearer " + token, reviewId).enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                                if (isAdded()) {
                                    if (response.isSuccessful()) {
                                        Toast.makeText(requireContext(), getString(R.string.success_review_deleted), Toast.LENGTH_SHORT).show();
                                        loadMyReviews(); // Recargar la lista después de eliminar
                                    } else {
                                        Log.e(TAG, "Error al eliminar reseña. Código: " + response.code() + ", Mensaje: " + response.message());
                                        Toast.makeText(requireContext(), getString(R.string.error_deleting_review, response.message()), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                                if (isAdded()) {
                                    Log.e(TAG, "Fallo de red al eliminar reseña: " + t.getMessage(), t);
                                    Toast.makeText(requireContext(), getString(R.string.error_network_connection_delete_review), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        Toast.makeText(requireContext(), getString(R.string.error_token_not_found_reviews), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(getString(R.string.dialog_confirm_delete_no_button), (dialog, which) -> dialog.dismiss())
                .show();
    }
}