# Privacy Policy — Dreamloom

This is the source-of-truth policy. Cursor: render this as static HTML and host on GitHub Pages at `https://<user>.github.io/dreamloom/privacy.html`. Use the URL in the Play Console.

Effective date: **<DATE OF FIRST PUBLIC RELEASE>**

---

## The short version

- **Your dream entries never leave your phone.** Period.
- We do not collect, store, transmit, or share the contents of your dreams, your voice recordings, the photos you attach, your interpretations, or your daily intentions. None of it.
- We do not require an account.
- We do not sync to the cloud, because we deliberately built the app without cloud storage.
- The app downloads a single AI model file from GitHub Releases the first time you open it, then runs entirely offline.
- We collect anonymous app-usage analytics (e.g., "a dream was saved") that do **not** include any of the content you wrote. You can turn these off in Settings.
- We collect crash reports to fix bugs. You can turn these off in Settings.
- We show ads via Google AdMob. AdMob may collect device identifiers as described below. You can opt out using your device's Ad ID settings.

If you uninstall Dreamloom, every byte we've ever stored on your phone — dreams, interpretations, the AI model itself — is gone. There is no backup we keep.

---

## What we collect

### Things we never collect

- Dream entries (text or voice recordings)
- Photos you attach
- AI-generated interpretations, intentions, weekly insights, oracle responses
- Your name, email, phone number, address — we never ask
- Your contacts, calendar, location, photo library

### Anonymous usage analytics (Firebase Analytics — opt-out)

We log anonymous events that help us understand how the app is used:
- That a dream was saved (not what was in it)
- The mood tag chosen (one of: serene, anxious, joyful, lost, fierce, skip)
- That the AI model finished generating an interpretation
- Which screens are opened
- Approximate device class (phone, tablet) and country (from IP, not stored)

These events are sent to Google's Firebase Analytics, anonymized with a randomly-generated install ID. You can disable analytics entirely in Settings → Privacy.

### Crash reports (Firebase Crashlytics — opt-out)

When the app crashes we send Google a stack trace and basic device info. Crash reports do not contain any of your dream content. You can disable in Settings → Privacy.

### Ad data (Google AdMob)

We use Google AdMob to show ads. AdMob may collect:
- Your device's advertising ID (Ad ID)
- Approximate location (country/region)
- Ad interaction events (impression, click)

You can reset or disable your Ad ID in your device's Settings → Privacy → Ads. In the European Economic Area, the UK, and Switzerland, we ask for your consent before AdMob collects this data, via Google's User Messaging Platform.

### Network requests we make

The complete list of outbound network calls Dreamloom makes:
1. Downloading the AI model file from GitHub Releases (one time, on first launch)
2. Sending anonymous analytics events to Firebase (opt-out)
3. Sending crash reports to Crashlytics (opt-out)
4. Loading and reporting on ads via Google AdMob

That is the complete list. We do not call any other server, ever.

---

## How long we keep things

- **Your dreams**: as long as you want them, on your phone. Tap "Wipe everything" in Settings to erase.
- **Anonymous analytics**: retained by Firebase per Google's standard retention (currently up to 14 months).
- **Crash reports**: retained by Crashlytics per Google's standard retention (90 days).

---

## Your rights

- **Erase**: tap Settings → Wipe Everything. Or uninstall the app. Either way, every dream is gone.
- **Disable analytics**: Settings → Privacy.
- **Disable crash reports**: Settings → Privacy.
- **Opt out of personalized ads**: device-level Ad ID setting, plus the in-app consent dialog (EU/UK/Swiss users).

If you live in the EU, UK, EEA, Switzerland, California, or other jurisdictions with a right to access/delete data: because we do not collect personally identifiable information, we have nothing to access or delete server-side. Your dream data is on your phone, and the wipe button will erase it. If you have questions, email us at the address below.

---

## Children

Dreamloom is rated for 13+. We do not knowingly collect data from children under 13. If you believe a child under 13 is using the app, please email us.

---

## Third parties we use

- **Google Firebase Analytics & Crashlytics** — for anonymous analytics and crash reporting. Privacy policy: https://policies.google.com/privacy
- **Google AdMob** — for ads. Privacy policy: https://policies.google.com/privacy
- **GitHub** — for hosting the AI model file download. Privacy policy: https://docs.github.com/en/site-policy/privacy-policies/github-general-privacy-statement
- **Google Cloud Speech / On-device Speech Recognizer** — used by Android's built-in `SpeechRecognizer` API. On Pixel and most Samsung devices in 2026, transcription runs entirely on-device. On other devices, Android may send audio to Google's servers per its system settings; we do not control or store that audio.

---

## Changes to this policy

If we change this policy meaningfully, we will update the date at the top and post a notice in the app the next time you open it.

---

## Contact

Questions: **hello@dreamloom.app**

---

*We built Dreamloom so your dream content stays on your device, and we are clear about how ads and analytics work. If you believe you have found a privacy issue, write to us — we will treat it as a critical bug.*
