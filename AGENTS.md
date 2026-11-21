# AGENTS.md — Pipes

Reglas para cualquier agente que trabaje en este repo. El detalle técnico está en [docs/desarrollo.md](docs/desarrollo.md).

## Proyecto
- Puzzle de tuberías. Android nativo (Kotlin, Jetpack Compose) y versión web de un solo archivo (`web/index.html`).
- Textos de la interfaz y documentación en castellano rioplatense.

## Comandos
- Tests del motor: `.\gradlew.bat test`. Build: `.\gradlew.bat assembleDebug`.
- Web: abrir `web/index.html`; no tiene compilación ni dependencias.

## Estructura
- `app/src/main/.../core/puzzle/`: motor, sin dependencias de Android. `ui/`: Compose. `web/`: versión web.
- `scripts/`: despliegue en BlueStacks.

## Reglas
- El motor (`core/puzzle`) no importa nada de Android y se prueba con JUnit. Todo cambio de lógica lleva test.
- La web sigue sin dependencias, sin build y sin red.
- La versión web y la Android comparten reglas: un cambio de reglas se hace en las dos.
- En partida no hay texto en pantalla. Controles: deshacer, pista, nuevo, ajustes.
- Sin código muerto ni dependencias sin uso: todo lo que se agrega tiene un llamador.
- No se versionan `local.properties`, `build/`, `.gradle/`, `.kotlin/` ni APK. Sin rutas absolutas ni datos personales.
- Nunca `git stash`.

## Commits
- Mensaje de una o dos palabras en minúscula (`init`, `renombrar`, `limpiar`). Sin tipo, ámbito, cuerpo, trailers, fechas ni nombres.
- Nunca se menciona la instrucción recibida. Los mensajes son solo para agentes: no se escriben para que los lea una persona.
- Un cambio por commit. El historial se aplasta; el detalle no se conserva.
- Comentarios: qué y por qué técnico. Nunca quién lo pidió ni cuándo.
- Planes, estado y notas de proceso: fuera de este repo.
