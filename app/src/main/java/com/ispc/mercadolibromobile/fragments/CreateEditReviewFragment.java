package com.ispc.mercadolibromobile.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import androidx.appcompat.app.AlertDialog;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ispc.mercadolibromobile.R;
import com.ispc.mercadolibromobile.api.ApiService;
import com.ispc.mercadolibromobile.api.RetrofitClient;
import com.ispc.mercadolibromobile.models.Book;
import com.ispc.mercadolibromobile.models.Review;
import com.ispc.mercadolibromobile.utils.SessionUtils; // Assuming SessionUtils provides getUserEmail

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateEditReviewFragment extends Fragment {

    private static final String ARG_REVIEW_ID = "review_id";
    private static final String ARG_BOOK_ID = "book_id";

    private static final String TAG = "CreateEditReviewFrag";

    private int reviewId;
    private int bookId;

    private EditText etReviewContent;
    private Button btnSaveReview, btnDeleteReview;
    private TextView tvReviewFormTitle, tvBookTitleReview;
    private Spinner spinnerBookSelection;
    private ProgressBar progressBar;

    private ApiService apiService;

    private List<Book> availableBooks;
    private ArrayAdapter<String> bookSpinnerAdapter;

    public static CreateEditReviewFragment newInstance(int reviewId, @Nullable Integer bookId) {
        CreateEditReviewFragment fragment = new CreateEditReviewFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_REVIEW_ID, reviewId);
        if (bookId != null && bookId != 0) {
            args.putInt(ARG_BOOK_ID, bookId);
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            reviewId = getArguments().getInt(ARG_REVIEW_ID, 0);
            bookId = getArguments().getInt(ARG_BOOK_ID, 0); //
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_edit_review, container, false);

        apiService = RetrofitClient.getApiService(requireContext());

        tvReviewFormTitle = view.findViewById(R.id.tvReviewFormTitle);
        tvBookTitleReview = view.findViewById(R.id.tvBookTitleReview);
        etReviewContent = view.findViewById(R.id.etReviewContent);
        btnSaveReview = view.findViewById(R.id.btnSaveReview);
        btnDeleteReview = view.findViewById(R.id.btnDeleteReview);
        spinnerBookSelection = view.findViewById(R.id.spinnerBookSelection);
        progressBar = view.findViewById(R.id.progressBar);

        availableBooks = new ArrayList<>();

        if (reviewId != 0) {
            tvReviewFormTitle.setText(getString(R.string.edit_review_title));
            btnDeleteReview.setVisibility(View.VISIBLE);
            tvBookTitleReview.setVisibility(View.VISIBLE);
            spinnerBookSelection.setVisibility(View.GONE);
            loadReviewData(reviewId);
        } else {
            tvReviewFormTitle.setText(getString(R.string.create_review_title));
            btnDeleteReview.setVisibility(View.GONE);

            if (bookId != 0) {
                apiService.getBookById(bookId).enqueue(new Callback<Book>() {
                    @Override
                    public void onResponse(Call<Book> call, Response<Book> response) {
                        if (isAdded() && response.isSuccessful() && response.body() != null) {
                            tvBookTitleReview.setText(getString(R.string.review_item_book_title_display, response.body().getTitulo()));
                            tvBookTitleReview.setVisibility(View.VISIBLE);
                        } else {
                            tvBookTitleReview.setText(getString(R.string.book_title_placeholder_review_id, bookId)); // Fallback
                            tvBookTitleReview.setVisibility(View.VISIBLE);
                            Log.e(TAG, "Error al cargar título del libro: " + response.code() + " - " + response.message());
                        }
                    }

                    @Override
                    public void onFailure(Call<Book> call, Throwable t) {
                        if (isAdded()) {
                            tvBookTitleReview.setText(getString(R.string.book_title_placeholder_review_id, bookId)); // Fallback
                            tvBookTitleReview.setVisibility(View.VISIBLE);
                            Log.e(TAG, "Error de red al cargar título del libro: " + t.getMessage());
                        }
                    }
                });
                spinnerBookSelection.setVisibility(View.GONE);
            } else {
                tvBookTitleReview.setVisibility(View.GONE);
                spinnerBookSelection.setVisibility(View.VISIBLE);
                loadBooksIntoSpinner();
            }
        }

        btnSaveReview.setOnClickListener(v -> saveReview());
        btnDeleteReview.setOnClickListener(v -> confirmDeleteReview());

        return view;
    }

    private void loadBooksIntoSpinner() {
        progressBar.setVisibility(View.VISIBLE);
        apiService.getBooks().enqueue(new Callback<List<Book>>() {
            @Override
            public void onResponse(@NonNull Call<List<Book>> call, @NonNull Response<List<Book>> response) {
                if (isAdded()) {
                    progressBar.setVisibility(View.GONE);
                    if (response.isSuccessful() && response.body() != null) {
                        availableBooks.clear();
                        availableBooks.addAll(response.body());
                        List<String> bookTitles = new ArrayList<>();
                        bookTitles.add(getString(R.string.hint_select_book_spinner));
                        for (Book book : availableBooks) {
                            bookTitles.add(book.getTitulo());
                        }

                        bookSpinnerAdapter = new ArrayAdapter<>(requireContext(),
                                android.R.layout.simple_spinner_item, bookTitles);
                        bookSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerBookSelection.setAdapter(bookSpinnerAdapter);

                        spinnerBookSelection.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                if (position > 0) {
                                    bookId = availableBooks.get(position - 1).getIdLibro();
                                } else {
                                    bookId = 0;
                                }
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {
                                bookId = 0;
                            }
                        });

                    } else {
                        Log.e(TAG, "Error al cargar libros para el spinner: " + response.code() + " - " + response.message());
                        Toast.makeText(requireContext(), getString(R.string.error_loading_books_for_spinner), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Book>> call, @NonNull Throwable t) {
                if (isAdded()) {
                    progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "Error de red al cargar libros para el spinner: " + t.getMessage(), t);
                    Toast.makeText(requireContext(), getString(R.string.error_network_connection_books), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadReviewData(int reviewId) {
        String token = SessionUtils.getAuthToken(requireContext());
        if (token == null) {
            Toast.makeText(requireContext(), getString(R.string.error_not_logged_in_review), Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        apiService.getReviewById("Bearer " + token, reviewId).enqueue(new Callback<Review>() {
            @Override
            public void onResponse(@NonNull Call<Review> call, @NonNull Response<Review> response) {
                if (isAdded()) {
                    progressBar.setVisibility(View.GONE);
                    if (response.isSuccessful() && response.body() != null) {
                        Review review = response.body();
                        etReviewContent.setText(review.getContenido());
                        tvBookTitleReview.setText(getString(R.string.review_item_book_title_display, review.getTituloLibroAsociado()));
                        bookId = review.getIdLibro();
                    } else {
                        Log.e(TAG, "Error al cargar la reseña para editar. Código: " + response.code() + ", Mensaje: " + response.message());
                        Toast.makeText(requireContext(), getString(R.string.error_loading_review_edit), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Review> call, @NonNull Throwable t) {
                if (isAdded()) {
                    progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "Error de red al cargar reseña para editar: " + t.getMessage(), t);
                    Toast.makeText(requireContext(), getString(R.string.error_network_connection_reviews), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void saveReview() {
        String content = etReviewContent.getText().toString().trim();
        String userEmail = SessionUtils.getUserEmail(requireContext());
        String token = SessionUtils.getAuthToken(requireContext());

        if (content.isEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.error_review_empty_fields), Toast.LENGTH_SHORT).show();
            return;
        }

        if (userEmail == null || token == null) {
            Toast.makeText(requireContext(), getString(R.string.error_not_logged_in_review), Toast.LENGTH_SHORT).show();
            return;
        }

        if (bookId == 0 && reviewId == 0) {
            Toast.makeText(requireContext(), getString(R.string.error_review_no_book_selected), Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        if (reviewId == 0) {
            Review newReview = new Review(content, bookId, userEmail);
            apiService.createReview("Bearer " + token, newReview).enqueue(new Callback<Review>() {
                @Override
                public void onResponse(@NonNull Call<Review> call, @NonNull Response<Review> response) {
                    if (isAdded()) {
                        progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null) {
                            Toast.makeText(requireContext(), getString(R.string.success_review_created), Toast.LENGTH_SHORT).show();
                            getParentFragmentManager().popBackStack(); // Regresar al fragment anterior
                        } else {
                            Log.e(TAG, "Error al crear reseña. Código: " + response.code() + ", Mensaje: " + response.message());
                            Toast.makeText(requireContext(), getString(R.string.error_creating_review, response.message()), Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Review> call, @NonNull Throwable t) {
                    if (isAdded()) {
                        progressBar.setVisibility(View.GONE);
                        Log.e(TAG, "Fallo de red al crear reseña: " + t.getMessage(), t);
                        Toast.makeText(requireContext(), getString(R.string.error_network_connection_reviews), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            Review updatedReview = new Review(reviewId, content, bookId, null, null, userEmail); // <--- IMPORTANT CHANGE

            apiService.updateReview("Bearer " + token, reviewId, updatedReview).enqueue(new Callback<Review>() {
                @Override
                public void onResponse(@NonNull Call<Review> call, @NonNull Response<Review> response) {
                    if (isAdded()) {
                        progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null) {
                            Toast.makeText(requireContext(), getString(R.string.success_review_updated), Toast.LENGTH_SHORT).show();
                            getParentFragmentManager().popBackStack();
                        } else {
                            Log.e(TAG, "Error al actualizar reseña. Código: " + response.code() + ", Mensaje: " + response.message());
                            Toast.makeText(requireContext(), getString(R.string.error_updating_review, response.message()), Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Review> call, @NonNull Throwable t) {
                    if (isAdded()) {
                        progressBar.setVisibility(View.GONE);
                        Log.e(TAG, "Fallo de red al actualizar reseña: " + t.getMessage(), t);
                        Toast.makeText(requireContext(), getString(R.string.error_network_connection_reviews), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void confirmDeleteReview() {
        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.confirm_delete_review_title))
                .setMessage(getString(R.string.confirm_delete_review_message))
                .setPositiveButton(getString(R.string.dialog_confirm_delete_yes_button), (dialog, which) -> {
                    String token = SessionUtils.getAuthToken(requireContext());
                    if (token != null) {
                        progressBar.setVisibility(View.VISIBLE);
                        apiService.deleteReview("Bearer " + token, reviewId).enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                                if (isAdded()) {
                                    progressBar.setVisibility(View.GONE);
                                    if (response.isSuccessful()) {
                                        Toast.makeText(requireContext(), getString(R.string.success_review_deleted), Toast.LENGTH_SHORT).show();
                                        getParentFragmentManager().popBackStack(); // Regresar al fragment anterior
                                    } else {
                                        Log.e(TAG, "Error al eliminar reseña. Código: " + response.code() + ", Mensaje: " + response.message());
                                        Toast.makeText(requireContext(), getString(R.string.error_deleting_review, response.message()), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                                if (isAdded()) {
                                    progressBar.setVisibility(View.GONE);
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