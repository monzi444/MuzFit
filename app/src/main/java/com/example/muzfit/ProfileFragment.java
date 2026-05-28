package com.example.muzfit;

import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
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

        ivAvatar = view.findViewById(R.id.iv_avatar);
        tvNomeUtente = view.findViewById(R.id.tv_nome_utente);
        tvEmailUtente = view.findViewById(R.id.tv_email_utente);
        tvPeso = view.findViewById(R.id.tv_peso);
        tvAltezza = view.findViewById(R.id.tv_altezza);
        tvEta = view.findViewById(R.id.tv_eta);
        
        tvObiettivoKcal = view.findViewById(R.id.tv_obiettivo_kcal);
        tvCarbo = view.findViewById(R.id.tv_carbo);
        tvProteine = view.findViewById(R.id.tv_proteine);
        tvGrassi = view.findViewById(R.id.tv_grassi);

        Button btnModificaProfilo = view.findViewById(R.id.btn_modifica_profilo);
        Button btnObiettivi = view.findViewById(R.id.btn_obiettivi);
        View btnSettings = view.findViewById(R.id.btn_settings);

        ivAvatar.setOnClickListener(v -> {
            pickMedia.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        });

        btnModificaProfilo.setOnClickListener(v -> showEditDialog());
        btnObiettivi.setOnClickListener(v -> showObiettiviDialog());

        return view;
    }

    private void showEditDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Modifica Profilo");

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(60, 40, 60, 20);

        final EditText etNome = createStyledEditText("Inserisci il tuo Nome", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        String currentNome = tvNomeUtente.getText().toString();
        if (!currentNome.equals("Nome Utente")) etNome.setText(currentNome);
        layout.addView(etNome);

        final EditText etEmail = createStyledEditText("Inserisci la tua Email", InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        String currentEmail = tvEmailUtente.getText().toString();
        if (!currentEmail.equals("utente@example.com")) etEmail.setText(currentEmail);
        layout.addView(etEmail);

        final EditText etPeso = createStyledEditText("Inserisci il Peso (kg)", InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        String pesoText = tvPeso.getText().toString().replace(" kg", "").trim();
        if (!pesoText.equals("--")) etPeso.setText(pesoText);
        layout.addView(etPeso);

        final EditText etAltezza = createStyledEditText("Inserisci l'Altezza (cm)", InputType.TYPE_CLASS_NUMBER);
        String altezzaText = tvAltezza.getText().toString().replace(" cm", "").trim();
        if (!altezzaText.equals("--")) etAltezza.setText(altezzaText);
        layout.addView(etAltezza);

        final EditText etEta = createStyledEditText("Inserisci l'Età", InputType.TYPE_CLASS_NUMBER);
        String etaText = tvEta.getText().toString().replace(" anni", "").trim();
        if (!etaText.equals("--")) etEta.setText(etaText);
        layout.addView(etEta);

        builder.setView(layout);

        builder.setPositiveButton("Salva", (dialog, which) -> {
            if (hasContent(etNome)) tvNomeUtente.setText(etNome.getText().toString());
            if (hasContent(etEmail)) tvEmailUtente.setText(etEmail.getText().toString());
            if (hasContent(etPeso)) tvPeso.setText(etPeso.getText().toString() + " kg");
            if (hasContent(etAltezza)) tvAltezza.setText(etAltezza.getText().toString() + " cm");
            if (hasContent(etEta)) tvEta.setText(etEta.getText().toString() + " anni");
        });

        builder.setNegativeButton("Annulla", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showObiettiviDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Imposta Obiettivi");

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(60, 40, 60, 20);

        final EditText etKcal = createStyledEditText("Inserisci Kcal (es. 2000)", InputType.TYPE_CLASS_NUMBER);
        String kcalText = tvObiettivoKcal.getText().toString().replace("OB = ", "").replace(" Kcal", "").trim();
        if (!kcalText.equals("--") && !kcalText.equals("2000")) etKcal.setText(kcalText);
        layout.addView(etKcal);

        final EditText etCarbo = createStyledEditText("Inserisci Carboidrati (g)", InputType.TYPE_CLASS_NUMBER);
        String carboText = tvCarbo.getText().toString().replace("Carbo\n", "").replace(" g", "").trim();
        if (!carboText.equals("--")) etCarbo.setText(carboText);
        layout.addView(etCarbo);

        final EditText etProteine = createStyledEditText("Inserisci Proteine (g)", InputType.TYPE_CLASS_NUMBER);
        String proteineText = tvProteine.getText().toString().replace("Proteine\n", "").replace(" g", "").trim();
        if (!proteineText.equals("--")) etProteine.setText(proteineText);
        layout.addView(etProteine);

        final EditText etGrassi = createStyledEditText("Inserisci Grassi (g)", InputType.TYPE_CLASS_NUMBER);
        String grassiText = tvGrassi.getText().toString().replace("Grassi\n", "").replace(" g", "").trim();
        if (!grassiText.equals("--")) etGrassi.setText(grassiText);
        layout.addView(etGrassi);

        builder.setView(layout);

        builder.setPositiveButton("Salva", (dialog, which) -> {
            if (hasContent(etKcal)) tvObiettivoKcal.setText("OB = " + etKcal.getText().toString() + " Kcal");
            if (hasContent(etCarbo)) tvCarbo.setText("Carbo\n" + etCarbo.getText().toString() + " g");
            if (hasContent(etProteine)) tvProteine.setText("Proteine\n" + etProteine.getText().toString() + " g");
            if (hasContent(etGrassi)) tvGrassi.setText("Grassi\n" + etGrassi.getText().toString() + " g");
        });

        builder.setNegativeButton("Annulla", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private EditText createStyledEditText(String hint, int inputType) {
        EditText editText = new EditText(getContext());
        editText.setHint(hint);
        editText.setInputType(inputType);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 10, 0, 10);
        editText.setLayoutParams(lp);
        return editText;
    }

    private boolean hasContent(EditText et) {
        return et.getText() != null && et.getText().toString().trim().length() > 0;
    }
}