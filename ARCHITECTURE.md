# Architecture Guidelines: MVVM + Repository

This document is the team's single source of truth for how the app is structured.
Everyone follows it regardless of which editor or AI tool they use.

These guidelines define the **architecture only**. `<Feature>` is a placeholder for a
real feature name. A feature's remote source may be a REST API (Retrofit),
Firebase, both, or neither; include only the sources that feature actually needs.

The app is split into two layers. NEVER skip a layer or let a layer reach past its
direct neighbor.

```
UI layer:    Activity / Fragment  -->  ViewModel
Data layer:  Repository  -->  RemoteDataSource (Retrofit Service -> API, Firebase)
                          -->  LocalDataSource  (Room)
```

## Dependency direction (one-way only)

`Activity/Fragment` → `ViewModel` → `I<Feature>Repository` → `Base...DataSource`
→ `service` (Retrofit) / Firebase / Room DAO.

- View NEVER touches Retrofit, Firebase, Room, or a DataSource directly.
- ViewModel NEVER touches network/DB directly — only the repository interface.
- Repository depends on DataSource **abstractions** (`Base...DataSource`), never on
  concrete Firebase/Retrofit classes.
- Lower layers never import UI classes.

## Package layout (under the app's base package)

| Package                | Contents                                                 |
|------------------------|----------------------------------------------------------|
| `model`                | POJOs / Room `@Entity` / `Result`                        |
| `ui.<feature>`         | `Activity`, `fragment/`, `viewmodel/`                    |
| `repository.<feature>` | `I<Feature>Repository`, `<Feature>Repository`, callbacks |
| `source.<feature>`     | `Base...DataSource` + concrete remote/local data sources |
| `service`              | Retrofit interfaces (`<Feature>APIService`)              |
| `database`             | Room `@Dao`, `RoomDatabase`, `TypeConverter`             |
| `utils`                | `ServiceLocator`, `Constants`, helpers                   |
| `adapter`              | RecyclerView / Spinner adapters                          |

## Results & constants

- Wrap async data in `Result<T>` (`Result.Success` / `Result.Error` / `Result.Loading`)
  exposed through `LiveData`. Do not pass raw exceptions/booleans up to the UI.
- All URLs, endpoints, keys, and error-message strings live in `utils.Constants`.
  Never hard-code literals in data sources or views.

## Wiring

- `ServiceLocator` (double-checked singleton) is the ONLY place that constructs
  Retrofit services, Room DAOs, data sources, and repositories.
- Obtain dependencies via `ServiceLocator.getInstance().get<Feature>Repository(...)`.
  Do not `new` a repository or data source anywhere else.
- Use a `debugMode` flag in `ServiceLocator` to swap a `...MockDataSource` for the
  real local/remote source.

---

# UI Layer

## Views (Activity / Fragment)

- Hold UI + presentation logic only. NO business logic, NO direct data access.
- Get the repository from `ServiceLocator`, build the ViewModel with its Factory,
  then observe `LiveData`:

```
I<Feature>Repository repository = ServiceLocator.getInstance()
        .get<Feature>Repository(requireActivity().getApplication());
viewModel = new ViewModelProvider(requireActivity(),
        new <Feature>ViewModelFactory(repository)).get(<Feature>ViewModel.class);

viewModel.get<Feature>LiveData(...).observe(getViewLifecycleOwner(), result -> {
    if (result.isSuccess()) { /* render ((Result.Success) result).getData() */ }
    else { /* show ((Result.Error) result).getMessage() */ }
});
```

## ViewModel

- Extends `androidx.lifecycle.ViewModel`.
- Constructor takes the repository **interface** (`I<Feature>Repository`), stored `final`.
- Exposes state as `MutableLiveData<Result<...>>`; delegates all work to the repository.
- No Android framework / network / DB imports beyond `lifecycle`.

## ViewModelFactory

- Every ViewModel with a custom constructor needs a `<Feature>ViewModelFactory`
  implementing `ViewModelProvider.Factory` that injects the repository interface.

```
public class <Feature>ViewModelFactory implements ViewModelProvider.Factory {
    private final I<Feature>Repository repository;
    public <Feature>ViewModelFactory(I<Feature>Repository repository) {
        this.repository = repository;
    }
    @NonNull @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new <Feature>ViewModel(repository);
    }
}
```

---

# Data Layer

## Repository

- Define an interface `I<Feature>Repository` and an implementation `<Feature>Repository`.
- The implementation depends on `Base...DataSource` abstractions injected via the
  constructor (kept `final`) — never on concrete Firebase/Retrofit/Room classes.
- Returns `MutableLiveData<Result<...>>`; mediates between remote and local sources.
- Receives results from data sources through a callback interface
  (`<Feature>ResponseCallback` / `<Feature>Callback`) and maps them to `Result`.

## Data sources (`source.<feature>`)

- For each source family define an abstract `Base...DataSource` exposing the
  operations + a `set...Callback(...)` method; concrete classes extend it.
- Naming by origin:
  - Remote API: `<Feature>APIDataSource` (uses a Retrofit `service` interface)
  - Firebase: `<Feature>FirebaseDataSource`
  - Local/Room: `<Feature>LocalDataSource` (uses a DAO)
  - Debug/fake: `<Feature>MockDataSource`
- Data sources report back ONLY via their callback; they never reference ViewModels.

## Retrofit services (`service`)

- A `service` is an interface with Retrofit annotations (`@GET`, `@Query`, ...).
- Endpoints and query keys come from `utils.Constants` (static imports), not literals.

## Room (`database`)

- Entities are `@Entity` classes in `model`; access via `@Dao` interfaces.
- The `RoomDatabase` is an abstract class exposing the DAO and built through a
  double-checked-locking `getDatabase(Context)` singleton.
