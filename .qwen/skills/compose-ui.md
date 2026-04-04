---
title: Jetpack Compose UI - Official API Reference
description: Official Android Developer documentation for Jetpack Compose
source: https://developer.android.com/jetpack/compose
---

# Jetpack Compose - Official API Reference

## Source: Android Developer Documentation

### Runtime - State Management

```kotlin
// Official imports from androidx.compose.runtime
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.DisposableEffect
```

**State Delegation (MUST have BOTH imports):**
```kotlin
// ✅ CORRECT - both imports required
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

var count by remember { mutableStateOf(0) }

// ❌ WRONG - missing imports
var count by remember { mutableStateOf(0) }  // Compile error without getValue/setValue
```

### Layout - Foundation

```kotlin
// Official imports from androidx.compose.foundation.layout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Alignment
```

**Modifier Extension Functions (NOT standalone):**
```kotlin
// ✅ CORRECT - chained on Modifier
Modifier
    .fillMaxWidth()
    .padding(16.dp)
    .weight(1f)  // Only valid inside Column/Row scope

// ❌ WRONG - standalone calls
fillMaxWidth()  // "Unresolved reference"
weight(1f)      // "Cannot be invoked as a function"
```

### Unit (from `androidx.compose.ui.unit`)

```kotlin
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.em
```

### Material3

```kotlin
// Official imports
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Surface
import androidx.compose.material3.ListItem
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Switch
import androidx.compose.material3.RadioButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.TabRow
import androidx.compose.material3.Tab
```

### Icons

```kotlin
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.Search
```

### Image & Shape

```kotlin
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
```

### Animation

```kotlin
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.runtime.rememberInfiniteTransition
```

### Common Mistakes

| ❌ WRONG | ✅ CORRECT |
|----------|-----------|
| `fillMaxWidth()` | `Modifier.fillMaxWidth()` |
| `weight(1f)` outside Column/Row | `Modifier.weight(1f)` inside `Column { }` |
| `var x by mutableStateOf(0)` without imports | Add `getValue` and `setValue` imports |
| `@Composable fun f() { launch { } }` | Use `rememberCoroutineScope().launch { }` |
| `Icon(Icons.Filled.Add, null)` without import | `import androidx.compose.material.icons.filled.Add` |
