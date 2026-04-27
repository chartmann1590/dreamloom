# Dreamloom

**Your dreams, decoded. Privately.**

Dreamloom is an Android dream journal. You record or type your dream, and on-device AI (Gemma through LiteRT-LM) helps interpret themes and symbols. **Your entries stay on your phone**—no account, no cloud sync. An optional one-time download brings the model onto the device; after that, core journaling works offline.

## Privacy

Journaling and interpretation are processed locally. Network use is limited to what you would expect: first-time model download, optional crash and usage analytics (with controls in the app), and ads if you use the ad-supported build. See the in-app privacy information and, when published, the privacy policy linked from the store listing.

## Get the app

Install from **Google Play** when the listing is live. (A store link can be added here for releases.)

## Build from source

You need [Android Studio](https://developer.android.com/studio) (recommended) or the Android command-line tools, plus **JDK 17**.

1. Clone this repository.
2. Open the project root in Android Studio and let it sync Gradle.
3. Ensure `local.properties` exists in the project root with `sdk.dir=`<path to your Android SDK> (Android Studio usually creates this).
4. Run the `app` run configuration on a device or emulator.

A release build also needs your own Firebase project, AdMob configuration, signing setup, and hosted model URLs. See [SETUP.md](SETUP.md) for that checklist.
