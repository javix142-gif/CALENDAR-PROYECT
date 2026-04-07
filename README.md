# Agenda Clara (MVP Android)

App Android nativa (Kotlin + Jetpack Compose) para calendario, tareas y recordatorios 100% local.

## Stack
- Kotlin
- Jetpack Compose + Material 3
- MVVM simple
- Room
- DataStore
- AlarmManager + NotificationManager + BroadcastReceiver

## Funcionalidades MVP implementadas
1. **Hoy**: lista de ítems del día, marcar realizado y editar.
2. **Próximos**: lista cronológica de próximos 7 días.
3. **Agregar rápido**: texto libre en español + botón interpretar.
4. **Confirmar/Editar**: edición de título, fecha/hora sugerida, notas, duración, tipo, repetición y recordatorios.
5. **Ajustes**: recordatorio 1 y 2, duración por defecto, alto contraste y texto grande.

## Parser inicial (es-CL básico)
- Soporta expresiones como `jueves`, `mañana`, `mañana en la mañana`, `mañana en la tarde`, horas `HH:mm`, y repetición simple.
- Regla de jueves futuro cumplida: si hoy es jueves, usa jueves de la próxima semana.
- Si no hay hora exacta, propone hora sugerida editable.

## Alarmas y notificaciones
- Recordatorios suaves por defecto 30 y 10 minutos antes.
- Alarma exacta a la hora del evento (si hay hora exacta).
- Para tareas/recordatorios sin hora exacta, se generan alarmas cada 2 horas hasta fin de día.
- Acciones: **Visto**, **Realizado** y **Posponer** (solo exacta).
- Reprogramación tras reinicio mediante `BOOT_COMPLETED`.
- Canales de notificación y solicitud de permiso `POST_NOTIFICATIONS`.

## Estructura
- `data/`: Room + repositorios + modelos.
- `domain/`: parser y scheduler.
- `ui/`: navegación + pantallas Compose.
- `receivers/` + `notifications/`: alarmas y acciones.

## Cómo ejecutar
1. Abrir en Android Studio (Koala o superior).
2. Sincronizar Gradle.
3. Ejecutar módulo `app` en Android 8+.
4. Otorgar permiso de notificaciones en Android 13+.

## Supuestos adoptados
- Zona horaria del dispositivo para programación de alarmas.
- Edición avanzada de fecha/hora usa propuesta inicial editable por campos de texto simples.
- Repetición se guarda en la entidad para expansión futura de ocurrencias.

## Mejoras futuras sugeridas
- Selector visual de fecha/hora (DatePicker/TimePicker).
- Manejo completo de ocurrencias repetidas (instancias por fecha).
- Pantalla de detalle dedicada por item.
- Internacionalización y parser más robusto para lenguaje natural.
- Tests instrumentados de notificaciones y alarmas exactas por versión Android.
