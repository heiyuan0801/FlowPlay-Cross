# STEER: Android TV support (user addition)
Also support Android TV (TV boxes / smart TVs):
- Same androidApp APK (or a separate tvApp module only if justified in SPEC) with LEANBACK_LAUNCHER intent filter + banner 320x180 generated from the app icon (/Volumes/mac/codex/FlowPlay/Design/AppIcon/).
- Manifest: <uses-feature android:name="android.software.leanback" android:required="false"/> and <uses-feature android:name="android.hardware.touchscreen" android:required="false"/> so it installs on phones AND TVs.
- D-pad/remote focus navigation everywhere (androidx.tv:tv-material / Compose focus handling); detect TV (UiModeManager UI_MODE_TYPE_TELEVISION) and use a 10-foot layout: side navigation drawer (首页/搜索/直播/下载/我的), large poster rows, visible focus states.
- Player on TV (later milestone): remote control — OK play/pause, back exits/hides controls, left/right seek, menu/up for tracks (audio/subtitle/quality), TVBox/live channel switching with up/down on remote.
- Add Android TV column/rows to SPEC.md parity matrix, architecture notes and milestones.
- Toolchain: install an Android TV arm64 system image (e.g. system-images;android-34;android-tv;arm64-v8a or google_tv variant that exists) and create AVD `fp_tv` (tv_1080p device).
- Round 0 runtime check: install the APK on the TV emulator, launch via leanback launcher activity, D-pad focus works on the shell; screenshot to logs/android-tv-home.png and copy to /Users/jiesen/fp-shots/cross/android-tv-home.png. Kill emulator after.
