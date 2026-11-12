# FlowPlay-Cross Round 0 — SPEC + toolchain + scaffold

You are working in /Volumes/mac/codex/FlowPlay-Cross (new git repo, branch main). Work autonomously; do not ask questions. Reply/report in Simplified Chinese in the final report file.

## Goal
Start FlowPlay for Android and Windows. NOT going to any store: keep ALL features of the Apple version, including ones removed/hidden there:
TVBox config import + JS spiders + TVBox live, Apple CMS (with offline download), trending/hot lists (TMDB), aggregate search, live TV (m3u/txt), offline download, casting/DLNA, Emby (login, libraries, playback progress report, server-permission download), local files, TMDB metadata (posters/detail/episodes), legal/about (contact heiyuan6@gmail.com, developer jiesen, feedback mail entry).

## Reference app (READ-ONLY — never modify, never git checkout/stash/commit there; it has uncommitted user changes)
/Volumes/mac/codex/FlowPlay (Swift, SwiftUI). Read: .orchestrator/SPEC.md, docs/, Packages/ (FlowPlayShared etc.), Design/AppIcon/, and git history. Removed TVBox/live code: use `git -C /Volumes/mac/codex/FlowPlay show 78b1e1c^:<path>` / `git show 78b1e1c` (read only). Trending: `git show 77a159e`. CMS download removal: 16af689. Use `git show`/`git log`, never checkout in that repo.

## Tech decisions (already made)
- Kotlin + Compose Multiplatform, one Gradle repo: `shared` (KMP: commonMain + androidMain + desktopMain/jvmMain), `androidApp`, `desktopApp` (JVM; targets Windows x64; must also run on macOS arm64 for runtime checks).
- Player: Android = Media3/ExoPlayer (+ FFmpeg decoder extension for audio codecs not supported by the platform; or justify libmpv). Desktop = pick ONE of libmpv via JNA or vlcj, justify in SPEC (codec coverage, Windows x64 bundling, license, Compose embedding), and plan how native libs get bundled for Windows x64. Round 0 only needs the player abstraction interface + plan; a real player integration can be a later milestone (but if cheap, a stub screen is fine — no fake content).
- Suggested libs: Ktor client, kotlinx.serialization, coroutines, SQLDelight or Room KMP for storage, multiplatform-settings or DataStore, Coil 3 for images, a QuickJS engine for TVBox JS spiders (Android: quickjs-android / Desktop: JVM option — evaluate and record in SPEC). Pick stable versions that actually resolve.

## Toolchain (authorized to install; NO sudo; Homebrew is NOT installed — do not install Homebrew)
- Home volume has only ~33 GB free; put large toolchains on /Volumes/mac: install under /Volumes/mac/codex/.toolchains/
  - JDK 21 (Eclipse Temurin aarch64 macOS tar.gz from adoptium API), set JAVA_HOME there.
  - Android commandline-tools (latest mac zip from dl.google.com), ANDROID_HOME=/Volumes/mac/codex/.toolchains/android-sdk. Then sdkmanager: platform-tools, emulator, platforms;android-35 (or what AGP needs), build-tools, one arm64-v8a google_apis system image. Accept licenses non-interactively (`yes | sdkmanager --licenses`). Create ONE AVD (e.g. `fp_pixel`) — AVD home may stay default (~/.android/avd) but keep it small.
  - Also create symlink ~/Library/Android/sdk -> the SDK dir if ~/Library/Android/sdk doesn't exist (convenience).
  - Gradle: generate a Gradle wrapper (download a Gradle distribution zip to the toolchains dir once to run `gradle wrapper`, or write wrapper files manually). Commit the wrapper (gradlew, gradle/wrapper/*).
- Write `scripts/env.sh` (tracked) that exports JAVA_HOME/ANDROID_HOME/PATH for these locations (no secrets), and document in README.md.
- local.properties holds sdk.dir (git-ignored).

## Secrets
- TMDB key: copy the MECHANISM, not the value. Read the value from the Apple repo's Config/Secrets.xcconfig (read-only) and write it into a git-ignored file in this repo (e.g. `secrets.properties` or local.properties key `TMDB_API_KEY`). Gradle injects it into BuildConfig / generated code at build time (Android BuildConfig; desktop via generated source or a resource excluded from git). Generated build outputs are fine, but NEVER put the key in tracked files, logs, commit messages, reports, or print it to stdout. Provide `secrets.properties.example` with an empty placeholder.
- Never write tokens/keys/passwords anywhere. Do not touch the user's Emby server or Apple app data.

## Round 0 deliverables
1. `.gitignore` (build/, .gradle/, local.properties, secrets.properties, logs/, *.jks, .idea/ etc.), `logs/` ignored.
2. `.orchestrator/SPEC.md`: feature parity matrix vs Apple version (every feature, Apple status incl. removed/hidden ones, Android plan, Windows plan, notes), architecture, module/package layout, data/storage model (sources, history, favorites, downloads, settings, caches incl. TMDB cache policy), networking, player plan per platform (chosen libs + why), casting/DLNA plan per platform, download engine (HLS m3u8 segment download + mp4), TVBox (config parsing, spider types: JS via QuickJS; note JAR/csp spiders feasibility on Android via DexClassLoader vs desktop), Windows packaging, milestones M1..Mn with clear scope each.
3. Scaffold that builds:
   - `./gradlew :androidApp:assembleDebug` succeeds.
   - `./gradlew :desktopApp:run` launches; `./gradlew :desktopApp:packageDistributionForCurrentOS` (on mac produces dmg/app — fine) and also configure `createDistributable`/a portable zip task usable for Windows; configure nativeDistributions targets Msi, Exe for Windows with packageName FlowPlay, vendor jiesen, icon .ico.
   - Real shell UI in Chinese: bottom nav (Android) / navigation rail or sidebar (desktop) with tabs 首页 / 搜索 / 直播 / 下载 / 我的. Each tab shows a real empty state (e.g. 首页「还没有添加任何来源」 with an 添加来源 action that at least navigates to a real (possibly minimal) add-source screen; no fake data, no buttons that do nothing — if a feature isn't implemented yet, don't show its button). 我的 shows 关于 page with app version, developer jiesen, contact heiyuan6@gmail.com, feedback mail (mailto) — these are real.
   - Theme roughly matching Apple version's look (check its colors/accent).
   - App icon from /Volumes/mac/codex/FlowPlay/Design/AppIcon/ (copy files into this repo): Android adaptive icon (foreground from transparent PNG + background color/gradient matching iOS version) + legacy mipmaps; desktop window icon + .icns (mac) + .ico (Windows) generated (use sips/iconutil on mac; for .ico, write a small script e.g. python3 with Pillow if available, else Kotlin/java script or ImageIO — no paid tools).
   - Package name / applicationId: `app.flowplay.cross` (or justify), app name FlowPlay.
4. Target 0 Kotlin compile warnings (fix them; Gradle deprecation notices from plugins may be listed in report).
5. Runtime checks (short, no tests):
   - Start the AVD headless or windowed (`emulator -avd fp_pixel -no-snapshot-save -no-audio -no-boot-anim &`), wait for boot, `adb install -r`, launch, `adb exec-out screencap -p > logs/android-home.png`, also screenshot 我的/关于 if easy. Then kill the emulator when done (`adb emu kill`).
   - Run desktop app on macOS, screenshot window to logs/desktop-home.png (`screencapture -x` or `-l` with window id; if screen capture permission blocks, use Compose's own offscreen render of the root composable via ImageComposeScene to logs/desktop-home.png and say so). Then quit the app.
   - Copy the final screenshots also to /Users/jiesen/fp-shots/cross/android-home.png and /Users/jiesen/fp-shots/cross/desktop-home.png.
6. Windows: on mac you can't produce MSI/EXE (jpackage needs Windows). Produce portable distributable config, document exact Windows packaging command in README, and propose a GitHub Actions windows-latest workflow file at `.github/workflows/windows-package.yml` (create the file locally, do NOT create remote repos or push).
7. Commit to main with a Chinese message (no secrets). Write report `.orchestrator/report-r0.md` (Chinese): commit hash, toolchain installed (versions, paths, disk used), chosen libs + why, milestone summary, build results (errors/warnings counts), runtime check results, screenshot paths, Windows packaging situation, blockers. Do not include secret values.

## Constraints
- No unit tests; only short runtime checks.
- Keep shell commands non-interactive; downloads may be large — use curl -L with retries. If a download fails repeatedly, record the blocker and continue with what's possible.
- Do not modify anything outside /Volumes/mac/codex/FlowPlay-Cross, /Volumes/mac/codex/.toolchains, ~/.android, ~/Library/Android (symlink), ~/.gradle, ~/fp-shots/cross.

## ADDENDUM (also read .orchestrator/STEER-tv.md): Android TV support is part of round 0.
