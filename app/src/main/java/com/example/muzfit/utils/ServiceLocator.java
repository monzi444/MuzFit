package com.example.muzfit.utils;

import android.content.Context;

import com.example.muzfit.database.MuzFitDatabase;
import com.example.muzfit.database.MuzFitDatabaseSeeder;
import com.example.muzfit.repository.auth.AuthRepository;
import com.example.muzfit.repository.auth.IAuthRepository;
import com.example.muzfit.repository.dashboard.DashboardRepository;
import com.example.muzfit.repository.dashboard.IDashboardRepository;
import com.example.muzfit.repository.diet.DietRepository;
import com.example.muzfit.repository.diet.IDietRepository;
import com.example.muzfit.repository.profile.IProfileRepository;
import com.example.muzfit.repository.profile.ProfileRepository;
import com.example.muzfit.source.auth.AuthFirebaseDataSource;
import com.example.muzfit.repository.training.ITrainingRepository;
import com.example.muzfit.repository.training.TrainingRepository;
import com.example.muzfit.service.ExerciseApiService;
import com.example.muzfit.service.MuzFitApiService;
import com.example.muzfit.service.OpenFoodFactsApiService;
import com.example.muzfit.source.diet.openfoodfacts.OpenFoodFactsApiDataSource;
import com.example.muzfit.source.profile.ProfileApiDataSource;
import com.example.muzfit.source.training.TrainingApiDataSource;
import com.example.muzfit.source.training.catalog.ExerciseCatalogApiDataSource;
import com.example.muzfit.source.training.firebase.TrainingFirebaseDataSource;
import com.google.firebase.auth.FirebaseAuth;

import java.util.concurrent.Future;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class ServiceLocator {

    private static volatile ServiceLocator instance;

    private final MuzFitApiService muzFitApiService;
    private MuzFitDatabase database;
    private Future<?> seedFuture;
    private final IDietRepository dietRepository;
    private final ITrainingRepository trainingRepository;
    private final IProfileRepository profileRepository;
    private final IDashboardRepository dashboardRepository;
    private final IAuthRepository authRepository;

    private ServiceLocator() {
        muzFitApiService = createMuzFitApiService();
        ExerciseApiService exerciseApiService = createExerciseApiService();
        OpenFoodFactsApiService openFoodFactsApiService = createOpenFoodFactsApiService();
        dietRepository = new DietRepository(new OpenFoodFactsApiDataSource(openFoodFactsApiService));
        trainingRepository = new TrainingRepository(
                new TrainingApiDataSource(muzFitApiService),
                new ExerciseCatalogApiDataSource(exerciseApiService),
                new TrainingFirebaseDataSource()
        );
        profileRepository = new ProfileRepository(new ProfileApiDataSource(muzFitApiService));
        dashboardRepository = new DashboardRepository();
        authRepository = new AuthRepository(new AuthFirebaseDataSource(FirebaseAuth.getInstance()));
    }

    public static ServiceLocator getInstance() {
        if (instance == null) {
            synchronized (ServiceLocator.class) {
                if (instance == null) {
                    instance = new ServiceLocator();
                }
            }
        }
        return instance;
    }

    public static ServiceLocator getInstance(Context context) {
        ServiceLocator serviceLocator = getInstance();
        serviceLocator.initDatabase(context);
        return serviceLocator;
    }

    public void initDatabase(Context context) {
        if (database == null && context != null) {
            synchronized (this) {
                if (database == null) {
                    database = MuzFitDatabase.getInstance(context);
                    seedFuture = MuzFitDatabaseSeeder.seedBrunoMoretti(database);
                    if (dashboardRepository instanceof DashboardRepository) {
                        ((DashboardRepository) dashboardRepository).setLocalDatabase(database);
                        ((DashboardRepository) dashboardRepository).setSeedFuture(seedFuture);
                    }
                    if (profileRepository instanceof ProfileRepository) {
                        ((ProfileRepository) profileRepository).setLocalDatabase(database);
                        ((ProfileRepository) profileRepository).setSeedFuture(seedFuture);
                    }
                    if (dietRepository instanceof DietRepository) {
                        ((DietRepository) dietRepository).setLocalDatabase(database);
                        ((DietRepository) dietRepository).setSeedFuture(seedFuture);
                    }
                }
            }
        }
    }

    public MuzFitDatabase getDatabase() {
        return database;
    }

    public IDietRepository getDietRepository() {
        return dietRepository;
    }

    public ITrainingRepository getTrainingRepository() {
        return trainingRepository;
    }

    public IProfileRepository getProfileRepository() {
        return profileRepository;
    }

    public IDashboardRepository getDashboardRepository() {
        return dashboardRepository;
    }

    public IAuthRepository getAuthRepository() {
        return authRepository;
    }

    private static MuzFitApiService createMuzFitApiService() {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request request = chain.request().newBuilder()
                            .header("Authorization", "Bearer " + Constants.API_KEY)
                            .build();
                    return chain.proceed(request);
                })
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit.create(MuzFitApiService.class);
    }

    private static ExerciseApiService createExerciseApiService() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.EXERCISE_DB_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit.create(ExerciseApiService.class);
    }

    private static OpenFoodFactsApiService createOpenFoodFactsApiService() {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request request = chain.request().newBuilder()
                            .header("User-Agent", Constants.OFF_USER_AGENT)
                            .build();
                    return chain.proceed(request);
                })
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.OPEN_FOOD_FACTS_BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit.create(OpenFoodFactsApiService.class);
    }
}
