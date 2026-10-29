# Camera Gesture

Touch the camera notch (top center) to run actions: single tap, double tap, long press, swipe left/right/down.
Actions: open camera, any installed app, Back/Home/Recents/Notifications/Quick settings/Lock/Screenshot.

## Build APK with GitHub
1. Create a new GitHub repo, upload all these files, push to `main`.
2. Open the **Actions** tab -> "Build APK" runs automatically.
3. Download `CameraGesture-debug-apk` from the run, unzip, install `app-debug.apk`.

## Use
1. Open app -> "Enable service" -> Accessibility -> Camera Gesture -> On.
2. Tap a gesture row to choose its action.
3. Tune the touch area size in `NotchService.kt` (`widthDp`, `heightDp`).
