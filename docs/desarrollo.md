# Desarrollo

## Stack

- **Web:** HTML, Canvas 2D y Web Audio, sin dependencias ni compilación (`web/index.html`).
- **Android:** Kotlin 2.0, Jetpack Compose, Canvas. Min SDK 24, target SDK 35.

## Motor del puzzle

Basado en la matemática de Simon Tatham's Net.

- Cada celda es una máscara de 4 bits (N=1, E=2, S=4, O=8). Girar 90° es una rotación cíclica de la máscara.
- **Generador:** Kruskal con DSU sobre las aristas de la grilla (con o sin toroide). El resultado es un árbol de
  expansión: `V − 1` aristas, sin ciclos. Después se mezclan las piezas respetando simetrías.
- **Solver deductivo:** comprueba que el tablero tenga solución única.
- **Flujo:** un BFS desde la fuente, en cada toque, energiza solo las celdas con puertos enfrentados. Se gana con todas
  las celdas energizadas y sin fugas.
- **Grilla adaptativa:** el jugador elige las columnas y las filas salen del alto disponible, con celdas cuadradas.
  La fuente queda en el centro.

## Estructura

```
app/src/main/java/com/pipes/game/
  core/puzzle/        Tile, Grid, PuzzleGenerator, DeductiveSolver, FlowEvaluator
  core/audio/         sonido sintetizado (AudioTrack)
  core/haptics/       vibración
  ui/                 pantalla de juego, canvas y ViewModel
app/src/test/         tests del motor (JUnit)
web/                  versión web
scripts/              despliegue en BlueStacks
```

## Compilar y probar

Necesitás JDK 17 y el Android SDK (Android Studio los trae). Creá `local.properties` con `sdk.dir=<ruta al SDK>`.

```powershell
.\gradlew.bat test
.\gradlew.bat assembleDebug
```

El APK queda en `app/build/outputs/apk/debug/app-debug.apk`.

## Emulador (BlueStacks 5, Windows)

Con BlueStacks abierto, `scripts\deploy_bluestacks.ps1` activa ADB, instala el APK y lanza el juego.
