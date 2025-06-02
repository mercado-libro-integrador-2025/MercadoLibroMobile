package com.ispc.mercadolibromobile.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ispc.mercadolibromobile.R;
import com.ispc.mercadolibromobile.adapters.ContactoAdapter;

public class FeedbackFragment extends Fragment {
    public final String TAG = FeedbackFragment.class.getSimpleName();

    private RecyclerView rvContactos;
    private ContactoAdapter contactoAdapter;
    private EditText etBuscarContacto;
    private Button btnAgregarContacto;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feedback, container, false);

        rvContactos = view.findViewById(R.id.rvContactos);
        etBuscarContacto = view.findViewById(R.id.etBuscarContacto);
        btnAgregarContacto = view.findViewById(R.id.btnAgregarContacto);

        rvContactos.setLayoutManager(new LinearLayoutManager(getContext()));

        contactoAdapter = new ContactoAdapter(getContext(), view);
        rvContactos.setAdapter(contactoAdapter);

        etBuscarContacto.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                contactoAdapter.filtrar(s.toString());
            }
        });

        btnAgregarContacto.setOnClickListener(v -> {
            Fragment contactoFragment = new ContactFragment(); // tu fragmento para crear contacto

            Bundle args = new Bundle();
            args.putBoolean(ContactFragment.ARG_FROM_FEEDBACK, true);
            contactoFragment.setArguments(args);

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, contactoFragment) // reemplaza con tu ID de contenedor
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }

}
