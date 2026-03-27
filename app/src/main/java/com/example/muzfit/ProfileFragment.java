package com.example.muzfit;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import com.google.android.material.imageview.ShapeableImageView;

public class ProfileFragment extends Fragment {

    private ShapeableImageView ivAvatar;
    private TextView tvNomeUtente, tvEmailUtente, tvPeso, tvAltezza, tvEta;
    private TextView tvObiettivoKcal, tvCarbo, tvProteine, tvGrassi;
    
    private final ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    ivAvatar.setImageURI(uri);
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Inizializzazione viste profilo
        ivAvatar = view.findViewById(R.id.iv_avatar);
        tvNomeUtente = view.findViewById(R.id.tv_nome_utente);
        tvEmailUtente = view.findViewById(R.id.tv_email_utente);
        tvPeso = view.findViewById(R.id.tv_peso);
        tvAltezza = view.findViewById(R.id.tv_altezza);
        tvEta = view.findViewById(R.id.tv_eta);
        
        // Inizializzazione viste obiettivi
        tvObiettivoKcal = view.findViewById(R.id.tv_obiettivo_kcal);
        tvCarbo = view.findViewById(R.id.tv_carbo);
        tvProteine = view.findViewById(R.id.tv_proteine);
        tvGrassi = view.findViewById(R.id.tv_grassi);

        Button btnModificaProfilo = view.findViewById(R.id.btn_modifica_profilo);
        Button btnObiettivi = view.findViewById(R.id.btn_obiettivi);
        View btnSettings = view.findViewById(R.id.btn_settings);

        // Click sull'immagine per caricarne una nuova
        ivAvatar.setOnClickListener(v -> {
            pickMedia.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        });

        // Modifica i dati testuali tramite un Dialog
        btnModificaProfilo.setOnClickListener(v -> showEditDialog());

        // Modifica obiettivi tramite un Dialog
        btnObiettivi.setOnClickListener(v -> showObiettiviDialog());

        btnSettings.setOnClickListener(v -> {
            // Segnaposto come richiesto
        });

        return view;
    }

    private void showEditDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Modifica Profilo");

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText etNome = new EditText(getContext());
        etNome.setHint("Nome");
        etNome.setText(tvNomeUtente.getText());
        layout.addView(etNome);

        final EditText etEmail = new EditText(getContext());
        etEmail.setHint("Email");
        etEmail.setText(tvEmailUtente.getText());
        layout.addView(etEmail);

        final EditText etPeso = new EditText(getContext());
        etPeso.setHint("Peso (kg)");
        etPeso.setText(tvPeso.getText().toString().replace(" kg", ""));
        layout.addView(etPeso);

        final EditText etAltezza = new EditText(getContext());
        etAltezza.setHint("Altezza (cm)");
        etAltezza.setText(tvAltezza.getText().toString().replace(" cm", ""));
        layout.addView(etAltezza);

        final EditText etEta = new EditText(getContext());
        etEta.setHint("Età");
        etEta.setText(tvEta.getText().toString().replace(" anni", ""));
        layout.addView(etEta);

        builder.setView(layout);

        builder.setPositiveButton("Salva", (dialog, which) -> {
            tvNomeUtente.setText(etNome.getText().toString());
            tvEmailUtente.setText(etEmail.getText().toString());
            tvPeso.setText(etPeso.getText().toString() + " kg");
            tvAltezza.setText(etAltezza.getText().toString() + " cm");
            tvEta.setText(etEta.getText().toString() + " anni");
        });

        builder.setNegativeButton("Annulla", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void showObiettiviDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Imposta Obiettivi");

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText etKcal = new EditText(getContext());
        etKcal.setHint("Obiettivo Kcal");
        etKcal.setText(tvObiettivoKcal.getText().toString().replace("OB = ", "").replace(" Kcal", ""));
        layout.addView(etKcal);

        final EditText etCarbo = new EditText(getContext());
        etCarbo.setHint("Carbo (g)");
        etCarbo.setText(tvCarbo.getText().toString().replace("Carbo\n", "").replace(" g", ""));
        layout.addView(etCarbo);

        final EditText etProteine = new EditText(getContext());
        etProteine.setHint("Proteine (g)");
        etProteine.setText(tvProteine.getText().toString().replace("Proteine\n", "").replace(" g", ""));
        layout.addView(etProteine);

        final EditText etGrassi = new EditText(getContext());
        etGrassi.setHint("Grassi (g)");
        etGrassi.setText(tvGrassi.getText().toString().replace("Grassi\n", "").replace(" g", ""));
        layout.addView(etGrassi);

        builder.setView(layout);

        builder.setPositiveButton("Salva", (dialog, which) -> {
            tvObiettivoKcal.setText("OB = " + etKcal.getText().toString() + " Kcal");
            tvCarbo.setText("Carbo\n" + etCarbo.getText().toString() + " g");
            tvProteine.setText("Proteine\n" + etProteine.getText().toString() + " g");
            tvGrassi.setText("Grassi\n" + etGrassi.getText().toString() + " g");
        });

        builder.setNegativeButton("Annulla", (dialog, which) -> dialog.cancel());

        builder.show();
    }
}