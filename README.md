# Solstice

Unofficial fork of [Breezy Weather](https://github.com/breezy-weather/breezy-weather), redesigned in
Material You Expressive. Not affiliated with or endorsed by Breezy Weather.

Based on upstream Breezy Weather at `5ec3b9b` (2026-11-17), without its git history. Product decisions:
[docs/PLAN.md](docs/PLAN.md). Setup and current app status:
[docs/CONTINUE.md](docs/CONTINUE.md).

License: GNU LGPL-3.0, with the additional terms inherited from Breezy Weather.

## Testing on your phone

CI is configured to publish a rolling testing build to the **[testing pre-release](https://github.com/INFORMATION89/Solstice/releases/tag/testing)** after a successful push to `main`, provided the test-signing secrets are configured. **No testing release is currently published.** The profile signing and update path have not yet been verified end to end; treat future builds as experimental and back up important data.

1. On the phone, open the link above and download `solstice-testing-<commit>.apk`. You don't need to
   sign in.
2. The first time, Android asks you to allow your browser to install unknown apps. Allow it, then tap
   the downloaded file to install it.
3. It installs as **Solstice**.

To get each new build automatically, add the repo to [Obtainium](https://github.com/ImranR98/Obtainium)
with "include pre-releases" on.
