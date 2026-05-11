# Sport Activities Demo

Android assignment app: record a sport activity (name, location, start/end time, storage destination) and view a list of saved activities filtered by storage.

## Run

```bash
./gradlew :app:installDebug
```

`app/google-services.json` is committed and points at the demo Firebase project, so the **Remote** storage option works out of the box. If you remove the file, the app falls back to an in-memory fake remote — the build conditionally applies the google-services plugin based on whether the file is present.

## Pointing at your own Firebase project

If you fork this and want to use a different Firebase project, replace `app/google-services.json` with one from your own console, then mirror the setup:

1. **Authentication → Sign-in method → Anonymous → Enable**.
2. **Firestore → Rules**:

   ```
   rules_version = '2';
   service cloud.firestore {
     match /databases/{database}/documents {
       match /users/{uid}/sport_activities/{doc} {
         allow read, write: if request.auth != null && request.auth.uid == uid;
       }
     }
   }
   ```

On first remote write, the app signs in anonymously and stores documents under `users/{uid}/sport_activities`. Clearing app data or reinstalling starts a new anonymous user; the previous documents are orphaned (Firebase auto-expires anonymous accounts after ~30 days of inactivity).

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

## Not implemented

- `SavedStateHandle`-backed form persistence on Add — an in-progress form is lost on process death.
- Editing or deleting an activity.
- Per-user sign-in beyond per-install anonymous auth.
- Compose UI tests beyond a single happy path.
