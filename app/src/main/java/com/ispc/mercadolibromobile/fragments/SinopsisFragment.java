package com.ispc.mercadolibromobile.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.ispc.mercadolibromobile.R;

public class SinopsisFragment extends Fragment {

    private static final String TAG = "SinopsisFragment";
    public static SinopsisFragment newInstance(String title, String description, String imageUrl) {
        SinopsisFragment fragment = new SinopsisFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("description", description);
        args.putString("image", imageUrl);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sinopsis, container, false);

        String title = "";
        String description = "";
        String imageUrl = "";

        if (getArguments() != null) {
            title = getArguments().getString("title", "");
            description = getArguments().getString("description", "");
            imageUrl = getArguments().getString("image", "");
        }

        if (imageUrl != null && imageUrl.startsWith("image/upload/https://")) {
            imageUrl = imageUrl.replace("image/upload/", "");
        } else if (imageUrl != null && imageUrl.startsWith("image/upload/")) {

        }

        Log.d(TAG, "URL de la imagen: " + imageUrl);

        // Inicializar vistas
        TextView tvTitle = view.findViewById(R.id.book_title);
        TextView tvDescription = view.findViewById(R.id.book_synopsis);
        ImageView ivBookCover = view.findViewById(R.id.book_image);
        Button btnVolver = view.findViewById(R.id.btnvolversinopsis);

        // Establecer el texto y cargar la imagen
        tvTitle.setText(title);
        tvDescription.setText(description);

        // Cargar la imagen usando Glide
        Glide.with(this)
                .load(imageUrl)
                .timeout(10000)
                .into(ivBookCover);

        // Configurar el botón de volver
        btnVolver.setOnClickListener(v -> {
            requireActivity().onBackPressed();
        });

        return view;
    }
}
