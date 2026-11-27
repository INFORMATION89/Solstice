# Install Solstice

**Current distribution status (2026-11-27):** No testing pre-release is currently published. CI is configured to publish a rolling Android testing build after a successful push to `main` when the test-signing secrets are configured. The signing and update path have not yet been verified end to end. This is a pre-release/testing channel, not a statement that Solstice has been published to Google Play or F-Droid.

## Requirements

- Android 10 (API 29) or newer.
- The published testing APK is built for ARM64 Android devices (`arm64-v8a`).

## Install a testing build when one is available

1. Open the [Solstice testing pre-release](https://github.com/INFORMATION89/Solstice/releases/tag/testing). If no release is listed, there is no published APK to install yet.
2. When a release is available, download its `solstice-testing-<commit>.apk` asset. You do not need a GitHub account to download it.
3. Open the downloaded APK. The first time, Android may ask you to allow your browser or file manager to install unknown apps. Allow installation for that app, then open the APK again.
4. Confirm the Android installation prompt.

The testing build uses Solstice's Profile variant. It can be installed alongside a separate Solstice build. It also includes a second **Motion Lab** launcher icon for motion experiments; the app's Settings → Debug area also provides access to the lab.

## Receive testing updates automatically

Once a testing release is available, use [Obtainium](https://github.com/ImranR98/Obtainium):

1. Add `https://github.com/INFORMATION89/Solstice` as the app source.
2. Enable **Include pre-releases**.
3. Save the source and let Obtainium check for newer testing releases.

Solstice's in-app GitHub update check is a separate optional setting and is disabled by default. You do not need to enable it to install or update through Obtainium.

## Important notes

- Do not use Breezy Weather's releases, F-Droid repositories, package identifiers, or Obtainium source as Solstice update channels. Solstice is a separate app and uses its own releases.
- A testing pre-release may change frequently and may contain unfinished functionality. Back up important information before installing experimental builds.
- If Android refuses to update an existing installation, do not uninstall immediately if you need to preserve its local data. First confirm that the APK is from the same Solstice signing channel and report the installation error through [Solstice Issues](https://github.com/INFORMATION89/Solstice/issues).
- No stable store distribution channel is documented here. This guide will be updated when a separately validated distribution channel is available.
