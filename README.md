# Gate Controller for Android

The [Gate Controller](https://gatecontroller.oshanrube.com/) web app on the Play Store. It is a
**Trusted Web Activity**: Chrome shows the site full screen, as the app, with its own sign-in,
offline support and push notifications. There is no app code to keep in step with the site;
every change to the web app is in the Android app the moment it is deployed.

What this project adds on top of the site:

- A launcher icon (the web app's house) and a splash screen.
- Links to `gatecontroller.oshanrube.com` (invitation emails, the watch's sign-in QR code) open
  in the app.
- The web app's push alerts appear under the app's name and icon, with Android 13's
  notification permission asked for on the site's behalf.
- Without a browser that supports Trusted Web Activities, it falls back to a Custom Tab.

## Removing the address bar: Digital Asset Links

Chrome only shows the site without its URL bar once the site vouches for the app. The
gate-controller server serves `/.well-known/assetlinks.json` from two environment variables:

```
ANDROID_APP_PACKAGE=com.oshanrube.gatecontroller
ANDROID_APP_SHA256_FINGERPRINTS=<app signing key SHA-256>,<upload key SHA-256>
```

Both fingerprints are in Play Console → **Test and release → App integrity → App signing**
once the first bundle is uploaded. Until they are set, the app works but shows the URL bar.
Check with [Google's statement tester](https://developers.google.com/digital-asset-links/tools/generator).

## Publishing to Google Play

CI signs the bundle with the upload key, and uploads it to the **internal testing** track, only
on pushes to `master` and manual runs of the Build workflow; the upload also waits for the
`PLAY_PUBLISH` repository variable to be `true`. Pull requests and other branches build with the
debug key and never see these secrets, since they run Gradle scripts a branch can change.

| Secret | Value |
| --- | --- |
| `UPLOAD_KEYSTORE_BASE64` | `base64 -w0 upload.jks` (the Wear app's upload key can be reused) |
| `UPLOAD_KEYSTORE_PASSWORD` | keystore password |
| `UPLOAD_KEY_ALIAS` | key alias (optional, defaults to `upload`) |
| `UPLOAD_KEY_PASSWORD` | key password (optional, defaults to the keystore password) |
| `PLAY_SERVICE_ACCOUNT_JSON` | the same service account, given access to this app in Play Console |

The first bundle has to be uploaded by hand (the Play API cannot create an app). Set the
upload-key secrets first, then take the `gate-controller-bundle` artifact from a `master` or
manual Build run: other runs are signed with the debug key, which Play refuses.

Privacy policy: https://gatecontroller.oshanrube.com/privacy — data safety answers:
https://gatecontroller.oshanrube.com/data-safety (the web app's).

## Build

Requires JDK 17 and the Android SDK (API 36).

```sh
./gradlew assembleDebug
```
