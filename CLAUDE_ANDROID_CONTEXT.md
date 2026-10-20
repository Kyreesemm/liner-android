# Liner для Android: контекст для подготовки AGENTS.md

Этот форк — десктопный Liner, адаптированный для Android. Исходный проект в основном на React/TypeScript/Vite, Electron обслуживает desktop-сборку. Android-оболочка сделана на Capacitor с нативным Java-кодом. Нужна именно мобильная версия; desktop должен продолжать собираться.

## Ветки и синхронизация с оригиналом

- Репозиторий форка: `Kyreesemm/liner-android` (`origin`). На данный момент у него нет отдельного remote оригинала.
- Ветка `main` содержит историю оригинального проекта и Android-патчи. Не переписывать историю и не удалять Android-коммиты из `main`.
- Ветка `android` создана от текущего `main` после Android-изменений и отправлена в GitHub. Сейчас обе ветки совпадают на `ced4867` (`Improve Android player and playlist layouts`). Новую Android-работу вести в `android`.
- Будущая синхронизация: добавлять remote оригинального репозитория только когда известен корректный URL; обновления оригинала сначала интегрировать в `main`, затем переносить/сливать их в `android`. Общие React-компоненты могут конфликтовать с мобильными адаптациями, поэтому проверять merge внимательно.

## Что уже изменено под Android

- Capacitor-проект расположен в `android/`, package id `fun.tryliner.music`.
- Android-часть дополнена Java foreground service и MediaSession для системной карточки плеера, уведомления, метаданных трека/обложки и управления воспроизведением из шторки. Связанный WebView-код находится в `src/features/player/engine/playerRuntime.ts`.
- Android-интерфейс частично адаптирован в общих React-компонентах, с платформенными ветками через `Capacitor.isNativePlatform()`: основная навигация/оболочка, мини-плеер, экран плейлиста, полноэкранный плеер и lyrics. Это не отдельная копия приложения; изменения тех же компонентов могут пересекаться с upstream.
- Android-specific изменения и важные коммиты:
  - `add9a84` — initial Capacitor app;
  - `3c1b3bc` — mobile UI and launcher icon;
  - `54cc22e` — Android YouTube playback fix;
  - `85d3622` — Android MediaSession/foreground playback и иконка;
  - `ced4867` — мобильные layout-изменения.
- Текущая версия приложения: `versionCode 4`, `versionName 1.0.3` в `android/app/build.gradle`.
- APK игнорируются Git. Локальный файл для установки/передачи: `artifacts/Liner-Android-debug.apk`; он не коммитится. `no-public/` — пользовательские скриншоты/частные материалы, не добавлять их в Git.

## Сборка и установка

Из корня репозитория, используя pnpm:

```bash
pnpm build:android:web
pnpm exec cap sync android
```

Затем собрать debug APK:

```bash
cd android
JAVA_HOME=/usr/lib/jvm/java-21-openjdk \
ANDROID_HOME=/home/doskaree/Android/Sdk \
./gradlew assembleDebug
```

Результат: `android/app/build/outputs/apk/debug/app-debug.apk`. Для установки на подключённый телефон:

```bash
adb install -r android/app/build/outputs/apk/debug/app-debug.apk
```

Чтобы сделать удобную копию, положить её в `artifacts/Liner-Android-debug.apk` (эта папка/ APK в `.gitignore`). Для обновления установленного приложения сохранять package id, увеличивать `versionCode` и использовать ту же debug signing key. Сертификат текущей debug-подписи SHA-256: `ec4f3393d61eb054fd129448a0827e0003a5f5d7644fa77d4f8df220626318ec`.

В этой среде последняя сборка выполнялась с Java 21 и Android SDK по указанным путям. Наличие телефона проверить через `adb devices -l`; установка поверх предыдущей версии ранее успешно проверялась на Xiaomi.

## Рабочие правила для будущего AGENTS.md

Попроси Claude оформить в `AGENTS.md` краткие инструкции на основе этого контекста. В частности, зафиксировать:

- не ломать Electron/Desktop при мобильной адаптации;
- проверять платформу через Capacitor и оставлять desktop-поведение прежним;
- системные media controls должны обновляться из реального состояния плеера, а команды из notification передаваться WebView;
- перед Android APK выполнить web build, Capacitor sync и Gradle assemble;
- при выдаче обновляемого APK поднимать `versionCode`, сохранять application id и signing key;
- не коммитить пользовательские данные из `no-public/`, сборочные выходы и APK;
- запускать проверки/build, относящиеся к изменённым файлам, и сообщать, что именно реально проверено;
- вести Android-разработку в ветке `android`, а обновления upstream интегрировать аккуратно, особенно в общие React UI-файлы.

Создание/редактирование `AGENTS.md` оставь Claude на основании просьбы владельца проекта; этот файл — исходный контекст для чата.
