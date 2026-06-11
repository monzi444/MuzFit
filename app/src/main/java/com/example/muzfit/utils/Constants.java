package com.example.muzfit.utils;

public final class Constants {

    public static final String BASE_URL =
            "https://fabricate.tonic.ai/api/v1/workspaces/monzi%20444/projects/My%20Project/api/";
    public static final String API_KEY = "an3WkP5TFacbU7A1ekw9DoEp";
    public static final String EXERCISE_DB_BASE_URL = "https://oss.exercisedb.dev/api/v1/";
    public static final int EXERCISE_CATALOG_SEARCH_LIMIT = 50;

    /** Search-a-licious: recommended full-text search (replaces legacy cgi/search.pl and v2 for keywords). */
    public static final String SEARCH_A_LICIOUS_BASE_URL = "https://search.openfoodfacts.org/";
    public static final String SAL_SEARCH_PATH = "search";
    public static final String SAL_QUERY_Q = "q";
    public static final String SAL_QUERY_PAGE_SIZE = "page_size";
    public static final String SAL_QUERY_LANGS = "langs";
    public static final String SAL_QUERY_FIELDS = "fields";
    public static final String SAL_SEARCH_FIELDS = "code,product_name,generic_name,nutriments";
    public static final String SAL_SEARCH_LANGS = "it,en";
    public static final int OFF_FOOD_SEARCH_LIMIT = 20;
    public static final int OFF_FOOD_SEARCH_MIN_QUERY_LENGTH = 3;
    public static final long OFF_FOOD_SEARCH_DEBOUNCE_MS = 600L;
    public static final String OFF_USER_AGENT = "MuzFit/1.0 (Android; fitness app)";
    public static final String DEFAULT_USERNAME = "bruno.moretti";

    public static final String PATH_UTENTE = "utente";
    public static final String PATH_UTENTE_SINGLE = "utente/single";
    public static final String PATH_PASTO = "pasto";
    public static final String PATH_PASTO_SINGLE = "pasto/single";
    public static final String PATH_PASTO_UTENTE = "pasto-utente";
    public static final String PATH_PASTO_UTENTE_SINGLE = "pasto-utente/single";
    public static final String PATH_ALLENAMENTO = "allenamento";
    public static final String PATH_ALLENAMENTO_SINGLE = "allenamento/single";
    public static final String PATH_DESCRIZIONE_ESERCIZIO = "descrizione-esercizio";
    public static final String PATH_DESCRIZIONE_ESERCIZIO_SINGLE = "descrizione-esercizio/single";
    public static final String PATH_ALLENAMENTO_ESERCIZIO = "allenamento-esercizio";
    public static final String PATH_ALLENAMENTO_ESERCIZIO_SINGLE = "allenamento-esercizio/single";
    public static final String PATH_SERIE = "serie";
    public static final String PATH_SERIE_SINGLE = "serie/single";
    public static final String PATH_PESO = "peso";
    public static final String PATH_PESO_SINGLE = "peso/single";

    public static final String ERROR_NETWORK = "Network request failed";
    public static final String ERROR_FOOD_SEARCH = "Food search failed";
    public static final String ERROR_FOOD_SEARCH_NO_RESULTS = "No foods found";
    public static final String ERROR_NOT_SUPPORTED = "Operation not supported by the API";
    public static final String ERROR_USER_NOT_FOUND = "User not found";
    public static final String ERROR_WORKOUT_NOT_FOUND = "Workout not found";
    public static final String ERROR_FIREBASE_NOT_IMPLEMENTED = "Firebase not implemented yet";
    public static final String ERROR_DATABASE = "Local database error";
    public static final String ERROR_MEAL_IN_USE =
            "You can't remove meals that you have eaten";
    public static final String ERROR_AUTH_SIGN_OUT = "Sign out failed";

    private Constants() {
    }
}
