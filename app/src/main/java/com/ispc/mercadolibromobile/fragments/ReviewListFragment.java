package com.ispc.mercadolibromobile.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ispc.mercadolibromobile.R;
import com.ispc.mercadolibromobile.adapters.ReviewAdapter;
import com.ispc.mercadolibromobile.models.Review;
import com.ispc.mercadolibromobile.api.ApiService;
import com.ispc.mercadolibromobile.api.RetrofitClient;
import com.ispc.mercadolibromobile.utils.SessionUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReviewListFragment extends Fragment {

    private static final String ARG_BOOK_ID = "book_id";
    private static final String ARG_BOOK_TITLE = "book_title";
    private static final String TAG = "ReviewListFragment";

    private int bookId;
    private String bookTitle;
    private RecyclerView recyclerViewReviews;
    private ReviewAdapter reviewAdapter;
    private TextView tvBookTitleForReviews;
    private ProgressBar progressBar;
    private ApiService apiService;

    public static ReviewListFragment newInstance(int bookId, String bookTitle) {
        ReviewListFragment fragment = new ReviewListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_BOOK_ID, bookId);
        args.putString(ARG_BOOK_TITLE, bookTitle);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            bookId = getArguments().getInt(ARG_BOOK_ID);
            bookTitle = getArguments().getString(ARG_BOOK_TITLE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_review_list, container, false);

        tvBookTitleForReviews = view.findViewById(R.id.tvBookTitleForReviews);
        recyclerViewReviews = view.findViewById(R.id.recyclerViewReviews);
        progressBar = view.findViewById(R.id.progressBar);

        recyclerViewReviews.setLayoutManager(new LinearLayoutManager(getContext()));

        apiService = RetrofitClient.getApiService(requireContext());

        if (bookTitle != null && !bookTitle.isEmpty()) {
            tvBookTitleForReviews.setText(getString(R.string.reviews_for_book_display, bookTitle));
        } else {
            tvBookTitleForReviews.setText(getString(R.string.reviews_for_book_default));
        }

        loadReviews();

        return view;
    }

    private void loadReviews() {
        progressBar.setVisibility(View.VISIBLE);

        String token = SessionUtils.getAuthToken(requireContext());

        Call<List<Review>> call;
        if (token != null) {
            call = apiService.getAllReviews("Bearer " + token);
        } else {
            Log.w(TAG, "No se encontró token de autenticación para getAllReviews. Si el endpoint lo requiere, la llamada fallará.");
            call = apiService.getAllReviews(null);
        }


        call.enqueue(new Callback<List<Review>>() {
            @Override
            public void onResponse(@NonNull Call<List<Review>> call, @NonNull Response<List<Review>> response) {
                if (isAdded()) {
                    progressBar.setVisibility(View.GONE);
                    if (response.isSuccessful() && response.body() != null) {
                        List<Review> allReviews = response.body();
                        List<Review> filteredReviews = new ArrayList<>();

                        for (Review review : allReviews) {
                            if (review.getIdLibro() == bookId) {
                                filteredReviews.add(review);
                            }
                        }

                        reviewAdapter = new ReviewAdapter(filteredReviews, requireContext());
                        recyclerViewReviews.setAdapter(reviewAdapter);

                        if (filteredReviews.isEmpty()) {
                            Toast.makeText(requireContext(), getString(R.string.no_reviews_found_for_book), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, "Error al cargar reseñas del libro. Código: " + response.code() + ", Mensaje: " + response.message());
                        Toast.makeText(requireContext(), getString(R.string.error_loading_reviews_for_book, response.message()), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Review>> call, @NonNull Throwable t) {
                if (isAdded()) {
                    progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "Error de red al cargar reseñas del libro: " + t.getMessage(), t);
                    Toast.makeText(requireContext(), getString(R.string.error_network_connection_reviews), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
