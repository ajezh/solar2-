# Solar Monitor (Android)

A single Android app with three tabs (bottom navigation), each a full embedded
browser view logged into one of your solar portals:

- **Solarman** — https://globalpro.solarmanpv.com/
- **GoodWe SEMS+** — https://hk-semsplus.goodwe.com/
- **DeyeCloud** — https://india.deyecloud.com/business/maintain/plant

Switching tabs does not reload the page — all three sites stay loaded in the
background, so it feels instant and you stay logged in. Cookies are saved to
disk, so normally you only need to log in to each site once (the same as
"remember me" in a normal browser).

## Why source code, not a ready-made APK

This project was generated in an environment without the Android SDK,
Gradle's network access, or Google's Maven repositories, so it's not possible
to compile the installable `.apk` file directly here. Building it is a
5-minute step in Android Studio (see below), and after that you can install
it on your phone like any other app or generate a release build to share.

## Get a ready-made .apk (no installs needed) — via GitHub Actions

This project includes `.github/workflows/build.yml`, which builds the APK
automatically on GitHub's servers. Steps:

1. Create a free account at github.com if you don't have one.
2. Create a new **public or private** repository (any name, e.g. `solar-monitor`).
3. Upload/push everything in this `SolarMonitor` folder to that repo (on
   github.com you can literally drag-and-drop the unzipped folder into
   "Add file → Upload files", or use `git push` if you're comfortable with git).
4. Go to the **Actions** tab of your repo → you should see "Build APK"
   running (or click "Run workflow" if it didn't start automatically).
5. Wait ~2–3 minutes for it to finish (green checkmark).
6. Click into the finished run → scroll to **Artifacts** → download
   `SolarMonitor-debug-apk` → unzip it to get `app-debug.apk`.
7. Transfer that `.apk` to your phone (email it to yourself, Google Drive,
   USB, etc.) and tap it to install — you'll need to allow "install from
   unknown sources" for whichever app you use to open it, since it's not
   from the Play Store.

This gives you a real, installable `.apk` without installing Android Studio
or the Android SDK on your own machine — GitHub's servers do the compiling.

## Alternative: build it yourself in Android Studio

1. Install **Android Studio** (free, from developer.android.com/studio) on a
   Windows/Mac/Linux computer.
2. Open Android Studio → **Open** → select this `SolarMonitor` folder.
3. Let it sync (Android Studio will download Gradle + the Android SDK
   components automatically the first time — needs internet).
4. Plug in your Android phone via USB with **USB debugging** enabled
   (Settings → About phone → tap "Build number" 7 times → Developer options →
   USB debugging), or use an emulator.
5. Click the green **Run ▶** button. The app installs and launches on your
   device.

To get a shareable `.apk` file instead: **Build → Build Bundle(s)/APK(s) →
Build APK(s)**, then grab it from `app/build/outputs/apk/debug/`.

## Notes / things you may want to tweak

- **Login**: each tab is a real embedded browser (WebView), so just log in
  normally the first time you open each tab — no credentials are stored by
  me, only standard browser cookies, same as your phone's normal browser.
- **Icons/branding**: `app/src/main/res/drawable/ic_solar.xml` and
  `ic_launcher_foreground.xml` are simple placeholder icons — swap in your
  own logo/icons if you like.
- **Notifications / alerts** (e.g. "alert me if a plant goes offline") are
  *not* included — that would require each vendor's real API and
  credentials rather than just loading their website, since a background
  WebView can't reliably poll while the app is closed. If you want that, the
  next step would be looking at whether Solarman, GoodWe, and Deye publish a
  public monitoring API (Solarman does have a business API), and building a
  background-sync/notification layer against it — let me know if you'd like
  help with that.
- **Back button** navigates back within the currently open site's history
  before exiting the app.
- Pull down on any tab to refresh that site.
