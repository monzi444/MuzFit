package com.example.muzfit.ui.quick.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.muzfit.repository.quick.IQuickRepository;

public class QuickViewModel extends ViewModel {

    public static final String ACTION_QUICK_MEAL = "quick_meal";
    public static final String ACTION_START_WORKOUT = "start_workout";
    public static final String ACTION_LOG_WEIGHT = "log_weight";
    public static final String ACTION_UPDATE_GOAL = "update_goal";

    private final IQuickRepository repository;
    private final MutableLiveData<Boolean> overlayVisible = new MutableLiveData<>(false);
    private final MutableLiveData<String> selectedAction = new MutableLiveData<>();

    public QuickViewModel(IQuickRepository repository) {
        this.repository = repository;
    }

    public LiveData<Boolean> getOverlayVisible() {
        return overlayVisible;
    }

    public LiveData<String> getSelectedAction() {
        return selectedAction;
    }

    public void toggle() {
        Boolean current = overlayVisible.getValue();
        overlayVisible.setValue(current == null || !current);
    }

    public void hide() {
        overlayVisible.setValue(false);
    }

    public void show() {
        overlayVisible.setValue(true);
    }

    public void selectAction(String action) {
        selectedAction.setValue(action);
        selectedAction.setValue(null); // Reset after emission
        hide();
    }
}
