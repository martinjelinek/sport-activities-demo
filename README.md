# Sport Activities Demo

Android assignment app: record a sport activity (sport, location, start/end time, storage destination) and view a list of saved activities filtered by storage.

## Run

```bash
./gradlew :app:installDebug
```

## Modules

```
:app ─┬──► :ui   ──► :domain
      ├──► :data ──► :domain
      └──► :domain
```

- `:domain` — Kotlin/JVM. Models + repository interface.
- `:data` — Room, Firestore, repository implementation, Hilt bindings. Depends on `:domain`.
- `:ui` — Compose screens, ViewModels, navigation, components, theme. Depends on `:domain`. Does **not** depend on `:data`.
- `:app` — Application class + `MainActivity`. Depends on all three; Hilt aggregates bindings here.

## Architecture

- ViewModels expose `StateFlow<UiState>` and a single `onEvent(Event)` entry point.
- `SportActivityRepository` (in `:domain`) is implemented in `:data` and merges a `LocalDataSource` (Room) with a `RemoteDataSource` (Firestore or in-memory fake). Storage type is per-record.
- Compose Navigation 2.8 with `@Serializable` typed routes.
- The Add screen returns its result by writing onto the previous back-stack entry's `SavedStateHandle`; `ListScreen` consumes it once and shows a snackbar.

## Tests

```bash
./gradlew test                                          # unit tests across :domain, :data, :ui
./gradlew :ui:connectedDebugAndroidTest                 # one Compose UI test (Add screen)
```

