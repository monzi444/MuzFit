package com.example.muzfit.repository.dashboard;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;

import com.example.muzfit.model.Result;
import com.example.muzfit.model.User;
import com.example.muzfit.model.WeightEntry;
import com.example.muzfit.source.firebase.FirestoreSyncDataSource;

import org.junit.Rule;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class DashboardRepositoryTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Test
    public void getMacroGoals_withoutDatabase_returnsError() {
        DashboardRepository repository = new DashboardRepository(mock(FirestoreSyncDataSource.class));

        LiveData<Result<User>> result = repository.getMacroGoals();

        assertTrue(result.getValue().isError());
        assertEquals(
                "Local database is not initialized",
                ((Result.Error<?>) result.getValue()).getMessage()
        );
    }

    @Test
    public void getWeights_withoutDatabase_returnsError() {
        DashboardRepository repository = new DashboardRepository(mock(FirestoreSyncDataSource.class));

        LiveData<Result<List<WeightEntry>>> result = repository.getWeights();

        assertTrue(result.getValue().isError());
        assertEquals(
                "Local database is not initialized",
                ((Result.Error<?>) result.getValue()).getMessage()
        );
    }

    @Test
    public void getConsumedCalories_withoutDatabase_returnsError() {
        DashboardRepository repository = new DashboardRepository(mock(FirestoreSyncDataSource.class));

        LiveData<Result<Float>> result = repository.getConsumedCalories(1_700_000_000_000L);

        assertTrue(result.getValue().isError());
        assertEquals(
                "Local database is not initialized",
                ((Result.Error<?>) result.getValue()).getMessage()
        );
    }
}
