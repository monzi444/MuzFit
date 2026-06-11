package com.example.muzfit.ui.diet;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.muzfit.R;
import com.example.muzfit.adapter.FoodSearchAdapter;
import com.example.muzfit.model.Meal;
import com.example.muzfit.model.MealCategory;
import com.example.muzfit.model.Result;
import com.example.muzfit.service.dto.openfoodfacts.OpenFoodFactsMapper;
import com.example.muzfit.ui.diet.viewmodel.DietViewModel;
import com.example.muzfit.utils.Constants;
import com.example.muzfit.utils.MuzFitToast;

import eightbitlab.com.blurview.BlurAlgorithm;
import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderEffectBlur;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DietDialogHelper {

    private final FragmentActivity activity;
    private final DietViewModel viewModel;
    private final LifecycleOwner lifecycleOwner;

    private AlertDialog chooseMealDialog;
    private AlertDialog addFoodDialog;
    private final Handler foodSearchHandler = new Handler(Looper.getMainLooper());
    private Runnable pendingFoodSearchRunnable;
    private String pendingFoodSearchQuery = "";
    private List<Meal> activeSearchResults;
    private FoodSearchAdapter activeSearchAdapter;
    private View activeSearchLoadingContainer;

    public DietDialogHelper(FragmentActivity activity, DietViewModel viewModel, LifecycleOwner lifecycleOwner) {
        this.activity = activity;
        this.viewModel = viewModel;
        this.lifecycleOwner = lifecycleOwner;
    }

    public void showChooseMealDialog() {
        showChooseMealDialog(null);
    }

    /**
     * Overload that opens the meal picker with a preselected category.
     * Currently behaves like the no-arg version (UI logic is unchanged),
     * but the parameter is preserved so the caller can express intent
     * and we can wire the preselection later without touching call sites.
     */
    public void showChooseMealDialog(@Nullable MealCategory preselectedCategory) {
        List<Meal> masterMeals = new ArrayList<>();
        // Basic static placeholders
        masterMeals.add(new Meal(0, "Apple", 95, 25, 1, 0));
        masterMeals.add(new Meal(0, "Pasta with tomato sauce", 350, 70, 10, 5));
        masterMeals.add(new Meal(0, "Chicken breast", 165, 0, 31, 4));

        Map<Integer, Meal> catalog = viewModel.getMealsById().getValue();
        if (catalog != null) {
            masterMeals.addAll(catalog.values());
        }

        View dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_choose_meal, null);
        // Wire the BlurView (Android 12+). On older devices it stays transparent
        // and the translucent glass background still gives a soft frosted look.
        setupDialogBlur(dialogView);
        ListView listView = dialogView.findViewById(R.id.lvChooseMeal);
        TextView emptyView = dialogView.findViewById(R.id.tvChooseMealEmpty);
        EditText searchField = dialogView.findViewById(R.id.etChooseMealSearch);
        AutoCompleteTextView sortField = dialogView.findViewById(R.id.actvChooseMealSort);

        chooseMealDialog = styledDialogBuilder()
                .setView(dialogView)
                .create();
        AlertDialog dialog = chooseMealDialog;

        dialogView.findViewById(R.id.btnAddFoodFromPicker).setOnClickListener(v -> {
            dismissChooseMealDialog();
            showAddFoodDialog();
        });
        dialogView.findViewById(R.id.btnCancelChooseMeal).setOnClickListener(v -> dismissChooseMealDialog());

        final MealCatalogSort[] selectedSort = {MealCatalogSort.NAME_ASC};
        String[] sortLabels = new String[]{
                activity.getString(R.string.choose_meal_sort_name_asc),
                activity.getString(R.string.choose_meal_sort_name_desc),
                activity.getString(R.string.choose_meal_sort_calories_asc),
                activity.getString(R.string.choose_meal_sort_calories_desc)
        };
        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(
                activity,
                android.R.layout.simple_dropdown_item_1line,
                sortLabels
        );
        sortField.setAdapter(sortAdapter);
        sortField.setText(sortLabels[0], false);

        ArrayAdapter<Meal>[] listAdapterRef = new ArrayAdapter[1];
        listAdapterRef[0] = new ArrayAdapter<Meal>(activity, R.layout.item_choose_meal, new ArrayList<>()) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_choose_meal, parent, false);
                }
                Meal meal = getItem(position);
                TextView nameTv = convertView.findViewById(R.id.mealItemName);
                TextView kcalTv = convertView.findViewById(R.id.mealItemKcal);

                if (meal != null) {
                    nameTv.setText(meal.getFoodName() != null ? meal.getFoodName() : "");
                    kcalTv.setText(String.format(Locale.getDefault(), "%d kcal", Math.round(meal.getCalories())));

                    convertView.setOnClickListener(v -> {
                        dialog.dismiss();
                        chooseMealDialog = null;
                        showCategorySelectionDialog(meal);
                    });
                }
                return convertView;
            }
        };

        sortField.setOnItemClickListener((parent, view, position, id) -> {
            selectedSort[0] = MealCatalogSort.values()[position];
            refreshChooseMealList(masterMeals, listAdapterRef[0], searchField, selectedSort[0], emptyView, listView);
        });

        listView.setAdapter(listAdapterRef[0]);
        searchField.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                refreshChooseMealList(masterMeals, listAdapterRef[0], searchField, selectedSort[0], emptyView, listView);
            }
        });

        refreshChooseMealList(masterMeals, listAdapterRef[0], searchField, selectedSort[0], emptyView, listView);
        chooseMealDialog.setOnDismissListener(d -> chooseMealDialog = null);
        chooseMealDialog.show();
        applyLargeDialogWindowStyle(chooseMealDialog);
    }

    private void refreshChooseMealList(List<Meal> masterMeals, ArrayAdapter<Meal> adapter, EditText searchField, MealCatalogSort sort, TextView emptyView, ListView listView) {
        String query = searchField.getText() != null ? searchField.getText().toString().trim().toLowerCase(Locale.ROOT) : "";
        List<Meal> filtered = new ArrayList<>();
        for (Meal meal : masterMeals) {
            if (meal != null && (query.isEmpty() || meal.getFoodName().toLowerCase(Locale.ROOT).contains(query))) {
                filtered.add(meal);
            }
        }
        Comparator<Meal> comparator;
        if (sort == MealCatalogSort.NAME_DESC) {
            comparator = Comparator.comparing(m -> m.getFoodName().toLowerCase(Locale.ROOT), Comparator.reverseOrder());
        } else if (sort == MealCatalogSort.CALORIES_ASC) {
            comparator = Comparator.comparing(Meal::getCalories);
        } else if (sort == MealCatalogSort.CALORIES_DESC) {
            comparator = Comparator.comparing(Meal::getCalories).reversed();
        } else {
            comparator = Comparator.comparing(m -> m.getFoodName().toLowerCase(Locale.ROOT));
        }
        Collections.sort(filtered, comparator);
        adapter.clear();
        adapter.addAll(filtered);
        adapter.notifyDataSetChanged();
        boolean isEmpty = filtered.isEmpty();
        emptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        listView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void showCategorySelectionDialog(Meal template) {
        showCategorySelectionDialog(template, false);
    }

    private void showCategorySelectionDialog(Meal template, boolean backToAddFood) {
        dismissAddFoodDialog();
        View dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_meal_category, null);
        // Wire the BlurView for the dialog backdrop (Android 12+).
        setupCategoryDialogBlur(dialogView);
        MaterialCardView cardColazione = dialogView.findViewById(R.id.cardColazione);
        MaterialCardView cardPranzo = dialogView.findViewById(R.id.cardPranzo);
        MaterialCardView cardCena = dialogView.findViewById(R.id.cardCena);

        final MealCategory[] selectedCategory = {MealCategory.COLAZIONE};
        updateCategoryCardSelection(cardColazione, cardPranzo, cardCena, selectedCategory[0]);

        cardColazione.setOnClickListener(v -> {
            selectedCategory[0] = MealCategory.COLAZIONE;
            updateCategoryCardSelection(cardColazione, cardPranzo, cardCena, selectedCategory[0]);
        });
        cardPranzo.setOnClickListener(v -> {
            selectedCategory[0] = MealCategory.PRANZO;
            updateCategoryCardSelection(cardColazione, cardPranzo, cardCena, selectedCategory[0]);
        });
        cardCena.setOnClickListener(v -> {
            selectedCategory[0] = MealCategory.CENA;
            updateCategoryCardSelection(cardColazione, cardPranzo, cardCena, selectedCategory[0]);
        });

        AlertDialog categoryDialog = styledDialogBuilder().setView(dialogView).create();
        dialogView.findViewById(R.id.btnCategoryConfirm).setOnClickListener(v -> {
            categoryDialog.dismiss();
            logMealAndCloseDialogs(template, selectedCategory[0]);
        });
        dialogView.findViewById(R.id.btnCategoryBack).setOnClickListener(v -> {
            categoryDialog.dismiss();
            if (backToAddFood) showAddFoodDialog(); else showChooseMealDialog();
        });
        applyDialogWindowStyle(categoryDialog);
        categoryDialog.show();
    }

    private void updateCategoryCardSelection(MaterialCardView cardColazione, MaterialCardView cardPranzo, MaterialCardView cardCena, MealCategory selected) {
        styleCategoryCard(cardColazione, selected == MealCategory.COLAZIONE);
        styleCategoryCard(cardPranzo, selected == MealCategory.PRANZO);
        styleCategoryCard(cardCena, selected == MealCategory.CENA);
    }

    private void styleCategoryCard(MaterialCardView card, boolean selected) {
        card.setStrokeWidth(selected ? 2 : 1);
        int color = ContextCompat.getColor(activity, selected ? R.color.muz_primary_lime : R.color.muz_glass_border);
        card.setStrokeColor(ColorStateList.valueOf(color));
        // Recolor the leading icon: walk into the card's first child (the row LinearLayout)
        // and pick its first child (the ImageView). Lime when selected, on-surface-variant otherwise.
        ImageView icon = findLeadingIcon(card);
        if (icon != null) {
            icon.setColorFilter(ContextCompat.getColor(activity,
                    selected ? R.color.muz_primary_lime : R.color.muz_on_surface_variant));
        }
    }

    /**
     * Walks the card tree to find the leading icon (the first ImageView in the first
     * child LinearLayout of the card). Returns null if the structure is unexpected.
     */
    private ImageView findLeadingIcon(MaterialCardView card) {
        if (card.getChildCount() < 1) return null;
        View row = card.getChildAt(0);
        if (!(row instanceof ViewGroup)) return null;
        if (((ViewGroup) row).getChildCount() < 1) return null;
        View first = ((ViewGroup) row).getChildAt(0);
        return (first instanceof ImageView) ? (ImageView) first : null;
    }

    private void showAddFoodDialog() {
        View dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_add_food_search, null);
        setupSearchDialogBlur(dialogView);
        EditText etSearchFood = dialogView.findViewById(R.id.etSearchFood);
        RecyclerView rvFoodResults = dialogView.findViewById(R.id.rvFoodResults);
        activeSearchLoadingContainer = dialogView.findViewById(R.id.searchLoadingContainer);

        activeSearchResults = new ArrayList<>();
        activeSearchAdapter = new FoodSearchAdapter(activeSearchResults, meal ->
                showFoodConfirmDialog(meal, () -> showCategorySelectionDialog(meal, true)));
        rvFoodResults.setLayoutManager(new LinearLayoutManager(activity));
        rvFoodResults.setAdapter(activeSearchAdapter);

        viewModel.getFoodSearchResults().observe(lifecycleOwner, foodSearchObserver);

        etSearchFood.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                scheduleFoodSearch(s.toString().trim());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        addFoodDialog = styledDialogBuilder().setView(dialogView).create();
        dialogView.findViewById(R.id.btnAddFoodManual).setOnClickListener(v -> {
            dismissAddFoodDialog();
            showManualFoodDialog();
        });
        dialogView.findViewById(R.id.btnCancelSearch).setOnClickListener(v -> dismissAddFoodDialog());
        addFoodDialog.setOnDismissListener(d -> {
            cancelPendingFoodSearch();
            viewModel.getFoodSearchResults().removeObserver(foodSearchObserver);
            activeSearchResults = null;
            activeSearchAdapter = null;
            activeSearchLoadingContainer = null;
            addFoodDialog = null;
        });
        applyDialogWindowStyle(addFoodDialog);
        addFoodDialog.show();
    }

    private final Observer<Result<List<Meal>>> foodSearchObserver = result -> {
        if (activeSearchResults == null || activeSearchAdapter == null || addFoodDialog == null || !addFoodDialog.isShowing()) return;
        if (result.isLoading()) { setFoodSearchLoading(true); return; }
        setFoodSearchLoading(false);
        activeSearchResults.clear();
        if (result.isSuccess()) {
            List<Meal> apiData = ((Result.Success<List<Meal>>) result).getData();
            if (apiData != null) activeSearchResults.addAll(apiData);
        }
        activeSearchAdapter.notifyDataSetChanged();
    };

    private void setFoodSearchLoading(boolean loading) {
        if (activeSearchLoadingContainer != null) activeSearchLoadingContainer.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void scheduleFoodSearch(String query) {
        pendingFoodSearchQuery = query;
        if (pendingFoodSearchRunnable != null) foodSearchHandler.removeCallbacks(pendingFoodSearchRunnable);
        if (query.length() < Constants.OFF_FOOD_SEARCH_MIN_QUERY_LENGTH) {
            setFoodSearchLoading(false);
            if (activeSearchResults != null && activeSearchAdapter != null) {
                activeSearchResults.clear();
                activeSearchAdapter.notifyDataSetChanged();
            }
            viewModel.searchFoods("");
            return;
        }
        setFoodSearchLoading(true);
        pendingFoodSearchRunnable = () -> { if (query.equals(pendingFoodSearchQuery)) viewModel.searchFoods(query); };
        foodSearchHandler.postDelayed(pendingFoodSearchRunnable, Constants.OFF_FOOD_SEARCH_DEBOUNCE_MS);
    }

    private void cancelPendingFoodSearch() {
        if (pendingFoodSearchRunnable != null) {
            foodSearchHandler.removeCallbacks(pendingFoodSearchRunnable);
            pendingFoodSearchRunnable = null;
        }
        pendingFoodSearchQuery = "";
        setFoodSearchLoading(false);
        viewModel.searchFoods("");
    }

    private void showManualFoodDialog() {
        View dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_add_food_manual, null);
        setupManualFoodDialogBlur(dialogView);
        EditText nameEt = dialogView.findViewById(R.id.editTextFoodName);
        EditText calEt = dialogView.findViewById(R.id.editTextCalories);
        EditText carbEt = dialogView.findViewById(R.id.editTextCarbs);
        EditText protEt = dialogView.findViewById(R.id.editTextProtein);
        EditText fatEt = dialogView.findViewById(R.id.editTextFat);

        AlertDialog manualDialog = styledDialogBuilder().setView(dialogView).create();
        dialogView.findViewById(R.id.btnSaveManualFood).setOnClickListener(v -> {
            if (saveManualFood(nameEt, calEt, carbEt, protEt, fatEt)) manualDialog.dismiss();
        });
        dialogView.findViewById(R.id.btnBackManualFood).setOnClickListener(v -> {
            manualDialog.dismiss();
            showAddFoodDialog();
        });
        applyDialogWindowStyle(manualDialog);
        manualDialog.show();
    }

    private void showFoodConfirmDialog(Meal meal, Runnable onConfirm) {
        View dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_food_confirm, null);
        setupFoodConfirmDialogBlur(dialogView);
        TextView tvName = dialogView.findViewById(R.id.tvConfirmFoodName);
        TextView tvDetails = dialogView.findViewById(R.id.tvConfirmFoodDetails);
        tvName.setText(meal.getFoodName());
        tvDetails.setText(OpenFoodFactsMapper.formatSearchSubtitle(meal));

        AlertDialog confirmDialog = styledDialogBuilder().setView(dialogView).create();
        dialogView.findViewById(R.id.btnConfirmAdd).setOnClickListener(v -> {
            confirmDialog.dismiss();
            onConfirm.run();
        });
        dialogView.findViewById(R.id.btnConfirmCancel).setOnClickListener(v -> confirmDialog.dismiss());
        applyDialogWindowStyle(confirmDialog);
        confirmDialog.show();
    }

    private String formatMealEntry(Meal meal) {
        return meal.getFoodName() + " (" + Math.round(meal.getCalories()) + " kcal)";
    }

    private CharSequence resolveCatalogDeleteError(String message) {
        return message != null ? message : "Errore catalogo";
    }

    private void dismissChooseMealDialog() {
        if (chooseMealDialog != null && chooseMealDialog.isShowing()) chooseMealDialog.dismiss();
        chooseMealDialog = null;
    }

    private void dismissAddFoodDialog() {
        if (addFoodDialog != null && addFoodDialog.isShowing()) addFoodDialog.dismiss();
        addFoodDialog = null;
    }

    private void dismissAllMealDialogs() {
        dismissAddFoodDialog();
        dismissChooseMealDialog();
    }

    private void logMealAndCloseDialogs(Meal meal, MealCategory category) {
        dismissAllMealDialogs();
        viewModel.logMealForSelectedDay(meal, category).observe(lifecycleOwner, result -> {
            if (result.isError()) {
                MuzFitToast.showError(activity, ((Result.Error<?>) result).getMessage());
                return;
            }
            viewModel.getMealCatalog(); // refresh
            MuzFitToast.show(activity, R.string.food_logged_toast);
        });
    }

    private boolean saveManualFood(EditText nameEt, EditText calEt, EditText carbEt, EditText protEt, EditText fatEt) {
        String name = nameEt.getText() != null ? nameEt.getText().toString().trim() : "";
        String calS = calEt.getText() != null ? calEt.getText().toString().trim() : "";
        if (name.isEmpty() || calS.isEmpty()) {
            MuzFitToast.showError(activity, activity.getString(R.string.food_name_required_toast));
            return false;
        }
        Meal meal = new Meal(0, name, Float.parseFloat(calS), parseOptionalFloat(carbEt), parseOptionalFloat(protEt), parseOptionalFloat(fatEt));
        showCategorySelectionDialog(meal, true);
        return true;
    }

    private static float parseOptionalFloat(EditText editText) {
        String value = editText.getText().toString().trim();
        return value.isEmpty() ? 0f : Float.parseFloat(value);
    }

    private AlertDialog.Builder styledDialogBuilder() {
        return new AlertDialog.Builder(activity, R.style.Theme_MuzFit_Dialog);
    }

    private void applyDialogWindowStyle(AlertDialog dialog) {
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
    }

    /**
     * Clips the given BlurView to a 28dp rounded rectangle. The XML
     * `bg_dialog_blur_rounded` background + clipToOutline combo is
     * unreliable for BlurView (which extends ConstraintLayout) on
     * some devices/emulators, so we also set a programmatic outline
     * provider. The two are belt-and-braces and both yield the same
     * 28dp corner radius as the glass card on top.
     */
    private void applyRoundedOutline(BlurView blurView) {
        if (blurView == null) return;
        final float radiusPx = 28f * blurView.getResources().getDisplayMetrics().density;
        blurView.setOutlineProvider(new android.view.ViewOutlineProvider() {
            @Override
            public void getOutline(android.view.View view, android.graphics.Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), radiusPx);
            }
        });
        blurView.setClipToOutline(true);
    }

    /**
     * Wires the dialog's BlurView to the activity's content root so the backdrop
     * of the dialog shows a real gaussian blur of whatever is behind it.
     * Uses RenderEffectBlur on Android 12+ (API 31+). On older devices the
     * BlurView remains transparent and the translucent glass background
     * ({@code muzfit_dialog_glass_translucent}) still gives a soft frosted look.
     */
    private void setupDialogBlur(View dialogView) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return;
        BlurView blurView = dialogView.findViewById(R.id.choose_meal_blur);
        if (blurView == null) return;
        applyRoundedOutline(blurView);
        ViewGroup rootView = activity.findViewById(android.R.id.content);
        if (!(rootView instanceof ViewGroup)) return;
        BlurAlgorithm algorithm = new RenderEffectBlur();
        blurView.setupWith(rootView, algorithm)
                .setBlurRadius(30f)
                .setBlurAutoUpdate(true);
    }

    /**
     * Same as {@link #setupDialogBlur(View)} but for the meal-category dialog.
     * Same blur radius (30f) for visual consistency across the two dialogs.
     */
    private void setupCategoryDialogBlur(View dialogView) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return;
        BlurView blurView = dialogView.findViewById(R.id.meal_category_blur);
        if (blurView == null) return;
        applyRoundedOutline(blurView);
        ViewGroup rootView = activity.findViewById(android.R.id.content);
        if (!(rootView instanceof ViewGroup)) return;
        BlurAlgorithm algorithm = new RenderEffectBlur();
        blurView.setupWith(rootView, algorithm)
                .setBlurRadius(30f)
                .setBlurAutoUpdate(true);
    }

    /** Blur for dialog_add_food_search (Open Food Facts search). */
    private void setupSearchDialogBlur(View dialogView) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return;
        BlurView blurView = dialogView.findViewById(R.id.add_food_search_blur);
        if (blurView == null) return;
        applyRoundedOutline(blurView);
        ViewGroup rootView = activity.findViewById(android.R.id.content);
        if (!(rootView instanceof ViewGroup)) return;
        BlurAlgorithm algorithm = new RenderEffectBlur();
        blurView.setupWith(rootView, algorithm)
                .setBlurRadius(30f)
                .setBlurAutoUpdate(true);
    }

    /** Blur for dialog_add_food_manual (manual food entry). */
    private void setupManualFoodDialogBlur(View dialogView) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return;
        BlurView blurView = dialogView.findViewById(R.id.add_food_manual_blur);
        if (blurView == null) return;
        applyRoundedOutline(blurView);
        ViewGroup rootView = activity.findViewById(android.R.id.content);
        if (!(rootView instanceof ViewGroup)) return;
        BlurAlgorithm algorithm = new RenderEffectBlur();
        blurView.setupWith(rootView, algorithm)
                .setBlurRadius(30f)
                .setBlurAutoUpdate(true);
    }

    /** Blur for dialog_food_confirm (confirm a food). */
    private void setupFoodConfirmDialogBlur(View dialogView) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return;
        BlurView blurView = dialogView.findViewById(R.id.food_confirm_blur);
        if (blurView == null) return;
        applyRoundedOutline(blurView);
        ViewGroup rootView = activity.findViewById(android.R.id.content);
        if (!(rootView instanceof ViewGroup)) return;
        BlurAlgorithm algorithm = new RenderEffectBlur();
        blurView.setupWith(rootView, algorithm)
                .setBlurRadius(30f)
                .setBlurAutoUpdate(true);
    }

    private void applyLargeDialogWindowStyle(AlertDialog dialog) {
        applyDialogWindowStyle(dialog);
        Window window = dialog.getWindow();
        if (window != null) {
            DisplayMetrics metrics = activity.getResources().getDisplayMetrics();
            window.setLayout((int) (metrics.widthPixels * 0.92f), (int) (metrics.heightPixels * 0.78f));
        }
    }

    private enum MealCatalogSort { NAME_ASC, NAME_DESC, CALORIES_ASC, CALORIES_DESC }
}
