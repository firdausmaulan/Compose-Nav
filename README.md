# Jetpack Navigation 3 — ComposeNav

---

## Section 1 · Quick Reference

### The mental model in one sentence
> Navigation 3 is just a **mutable list of keys**. You add/remove keys. The UI follows.

### 4 things you need

| Thing | What it is | This project |
|---|---|---|
| **Key** | Any type that identifies a screen | `sealed class AppKey` |
| **Back stack** | `mutableStateListOf<YourKey>` | `mutableStateListOf<AppKey>(AppKey.ProductList)` |
| **Entry provider** | Function that maps key → composable | `entryProvider { entry<AppKey.X> { ... } }` |
| **NavDisplay** | Composable that watches the back stack and renders it | `NavDisplay(backStack, onBack, entryProvider)` |

### Navigate forward / back

```kotlin
// Forward — push a key
backStack.add(AppKey.ProductDetail(productId = 42))

// Back — pop the last key
backStack.removeLastOrNull()
```

That's it. No `NavController`, no routes, no `navigate()` calls.

### Minimal working example

```kotlin
val backStack = remember { mutableStateListOf<Any>(HomeKey) }

NavDisplay(
    backStack = backStack,
    onBack = { backStack.removeLastOrNull() },
    entryProvider = entryProvider {
        entry<HomeKey> {
            HomeScreen(onGoToDetail = { backStack.add(DetailKey(it)) })
        }
        entry<DetailKey> { key ->
            DetailScreen(id = key.id, onBack = { backStack.removeLastOrNull() })
        }
    }
)
```

### Dependencies (libs.versions.toml)

```toml
[versions]
nav3Core = "1.0.1"
lifecycleViewmodelNav3 = "2.11.0-alpha02"
kotlinxSerializationCore = "1.9.0"

[libraries]
androidx-navigation3-runtime = { module = "androidx.navigation3:navigation3-runtime", version.ref = "nav3Core" }
androidx-navigation3-ui      = { module = "androidx.navigation3:navigation3-ui",      version.ref = "nav3Core" }
androidx-lifecycle-viewmodel-navigation3 = { module = "androidx.lifecycle:lifecycle-viewmodel-navigation3", version.ref = "lifecycleViewmodelNav3" }
kotlinx-serialization-core   = { module = "org.jetbrains.kotlinx:kotlinx-serialization-core", version.ref = "kotlinxSerializationCore" }

[plugins]
jetbrains-kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
```

---

## Section 2 · Deep Dive

### 2.1 Why Nav 3 exists

Nav 2 (NavController + NavHost) was designed for fragments and retrofitted for Compose. It has two main pain points:

1. **You don't own the back stack.** You call `navController.navigate("route")` and the library manages state internally. Inspecting or modifying the stack requires workarounds.
2. **Single-pane only by default.** Building adaptive layouts that show multiple destinations side-by-side (e.g., list-detail on tablet) requires a lot of glue code.

Nav 3 solves both: the back stack is a plain Kotlin `SnapshotStateList` you own, and `NavDisplay` can be given scene strategies to show multiple entries at once.

---

### 2.2 Keys

A key is the **identity** of a destination. It can be any type — a data object, a data class, even a string — but data classes/objects are recommended because:

- They are structurally equal by default (`data class` auto-generates `equals`/`hashCode`).
- They can be annotated with `@Serializable` for back stack persistence.

```kotlin
// Singleton destination (no arguments)
@Serializable
data object ProductList : AppKey()

// Destination with arguments — embed them in the key
@Serializable
data class ProductDetail(val productId: Int) : AppKey()
```

Using a **sealed class** as the base type narrows the back stack to `List<AppKey>` and makes `when` expressions exhaustive — the compiler tells you when you forget a destination.

#### Why @Serializable?
With `@Serializable` keys, Nav 3 can save/restore the back stack across **process death** using `rememberSaveableStateHolder`. Without it the back stack resets on process kill. For a demo it doesn't matter; for production it does.

---

### 2.3 Back Stack

```kotlin
// Creation — specify the start destination
val backStack = remember { mutableStateListOf<AppKey>(AppKey.ProductList) }
```

`mutableStateListOf` is Compose's observable list. Any mutation triggers recomposition of `NavDisplay`. The list is the single source of truth for navigation state.

**Common operations:**

```kotlin
backStack.add(key)              // navigate forward
backStack.removeLastOrNull()    // go back one step
backStack.clear()               // pop everything
backStack.removeAll { it is AppKey.History }  // pop to a specific point
backStack[0] = AppKey.ProductList             // replace root
```

Because it is a plain list, you can do things Nav 2 made difficult — like popping multiple entries, replacing the whole stack on deep-link, or reading which screens are currently stacked.

---

### 2.4 Entry Provider

The entry provider is a function `(Key) -> NavEntry`. It answers: *"given this key, what composable should I show?"*

Two styles — both equivalent:

**Lambda style** (explicit `when`):
```kotlin
entryProvider = { key ->
    when (key) {
        is AppKey.ProductList   -> NavEntry(key) { ProductListScreen(...) }
        is AppKey.History       -> NavEntry(key) { HistoryScreen(...) }
        is AppKey.ProductDetail -> NavEntry(key) { ProductDetailScreen(key.productId) }
    }
}
```

**DSL style** (cleaner, built-in fallback error if key is missing):
```kotlin
entryProvider = entryProvider {
    entry<AppKey.ProductList>   { ProductListScreen(...) }
    entry<AppKey.History>       { HistoryScreen(...) }
    entry<AppKey.ProductDetail> { key -> ProductDetailScreen(key.productId) }
}
```

`entry<T>` uses reified generics to match by type. The lambda receives the key as `it` (or a named param), so you can read arguments like `key.productId`.

---

### 2.5 NavDisplay

`NavDisplay` is the composable that connects back stack → UI. It observes the list, picks the top entry (by default), and renders it.

```kotlin
NavDisplay(
    backStack = backStack,
    onBack    = { backStack.removeLastOrNull() },   // called on back gesture / button
    entryProvider = entryProvider { ... }
)
```

`onBack` is just a lambda — you decide what "back" means. Typically it pops the last key. If the back stack has one entry, calling `removeLastOrNull()` is a no-op; the system back gesture then exits the app.

#### Entry decorators (ViewModel scoping)
By default, a `viewModel()` call inside an entry is scoped to the **Activity**. To scope ViewModels to individual back stack entries (so they are cleared when the entry is removed), add the `viewModelStoreNavEntryDecorator`:

```kotlin
// Add to NavDisplay
val decorators = listOf(rememberNavEntryDecorator(viewModelStoreNavEntryDecorator))

NavDisplay(
    backStack       = backStack,
    entryDecorators = decorators,
    onBack          = { backStack.removeLastOrNull() },
    entryProvider   = entryProvider { ... }
)

// Then inside an entry, use navEntryViewModel() instead of viewModel()
entry<AppKey.ProductDetail> { key ->
    val vm: ProductDetailViewModel = navEntryViewModel(
        factory = ProductDetailViewModel.Factory(key.productId)
    )
    ProductDetailScreen(viewModel = vm)
}
```

This project uses `viewModel()` (activity-scoped) with a unique `key` parameter for `ProductDetail` — sufficient for a demo but switch to `navEntryViewModel()` for production to avoid ViewModel leaks.

---

### 2.6 Data flow summary

```
User taps a button
      │
      ▼
ViewModel emits Effect (NavigateToDetail)
      │
      ▼
Screen collects Effect → calls onNavigateToDetail(id)
      │
      ▼
MainActivity: backStack.add(AppKey.ProductDetail(id))
      │
      ▼
NavDisplay recomposes → entryProvider(AppKey.ProductDetail(id))
      │
      ▼
NavEntry returned → ProductDetailScreen rendered
```

---

### 2.7 Nav 2 vs Nav 3 cheat sheet

| | Nav 2 | Nav 3 |
|---|---|---|
| Navigate | `navController.navigate("detail/{id}")` | `backStack.add(DetailKey(id))` |
| Back | `navController.popBackStack()` | `backStack.removeLastOrNull()` |
| Arguments | Route strings / `savedStateHandle` | Fields on the key data class |
| Back stack ownership | Library | **You** |
| ViewModel scoping | Automatic via `hiltViewModel` / `navGraphViewModel` | Opt-in via `viewModelStoreNavEntryDecorator` |
| Adaptive layouts | Manual workaround | Built-in `SceneStrategy` |
| State survival | `@Serializable` routes | `@Serializable` keys |
| Maturity | Stable | Alpha (1.0.x) |
