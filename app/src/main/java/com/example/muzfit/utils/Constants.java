package com.example.muzfit.utils;

public final class Constants {

    public static final String BASE_URL =
            "https://fabricate.tonic.ai/api/v1/workspaces/monzi%20444/projects/My%20Project/api/";
    public static final String API_KEY = "an3WkP5TFacbU7A1ekw9DoEp";
    public static final String EXERCISE_DB_BASE_URL = "https://oss.exercisedb.dev/api/v1/";
    public static final int EXERCISE_CATALOG_SEARCH_LIMIT = 50;

    public static final String OPEN_FOOD_FACTS_BASE_URL = "https://world.openfoodfacts.org/";
    public static final String OFF_SEARCH_PATH = "cgi/search.pl";
    public static final String OFF_QUERY_SEARCH_TERMS = "search_terms";
    public static final String OFF_QUERY_SEARCH_SIMPLE = "search_simple";
    public static final String OFF_QUERY_ACTION = "action";
    public static final String OFF_QUERY_JSON = "json";
    public static final String OFF_QUERY_PAGE_SIZE = "page_size";
    public static final String OFF_SEARCH_ACTION_PROCESS = "process";
    public static final int OFF_SEARCH_SIMPLE = 1;
    public static final int OFF_SEARCH_JSON = 1;
    public static final int OFF_FOOD_SEARCH_LIMIT = 20;
    public static final int OFF_FOOD_SEARCH_MIN_QUERY_LENGTH = 3;
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
    public static final String ERROR_AUTH_SIGN_OUT = "Sign out failed";

    private Constants() {
    }
}
