# Solstice help and troubleshooting

Solstice is an Android weather application based on a Breezy Weather fork. Some inherited upstream implementation remains, but this guide describes Solstice's current settings and support channel. Do not use Breezy Weather releases, package identifiers, forums, or issue trackers to troubleshoot Solstice.

For a bug report, use [Solstice Issues](https://github.com/INFORMATION89/Solstice/issues). Include the app version, Android version/device, the affected location or a nearby example, the selected weather source, the exact error, and reproduction steps. Do not post API keys, precise home coordinates, or private logs containing personal information.

## Locations and sources

### Solstice shows “Current location” instead of a street or city name

An address label is not required for a forecast. Weather calculations use the location coordinates. Address lookup is a separate geocoding operation and can fail even when weather data works.

Check **Settings → Location** for the configured location source and address lookup options. You can also use a saved/search-selected place when you do not want to grant device location permission.

### How do I change the source for a location?

Open the saved location's edit controls and review the available source settings. Source availability depends on the selected feature, location, configuration, and provider coverage. A source shown for one feature or region may not be available for another.

You can also review global defaults and configurable provider options in **Settings → Weather sources**. Some providers require their own configuration or API key; use a key only when the app exposes a setting for it, and review that provider's terms and privacy policy first.

### “Weather source failed to find a matching location”

The app has coordinates for the place, but the selected provider could not match those coordinates to a supported location. Try another available provider for that feature or select a nearby place supported by the provider.

## Network and weather-data errors

### “Network unavailable”

This usually means the device could not maintain a connection while a request was running. Check the device connection, VPN, private DNS, firewall, or network filtering. A provider may also be temporarily unreachable.

### “Invalid or incomplete data received”, “Location search failed”, or “Weather data refresh failed”

The provider or geocoding service may be temporarily unavailable, may have returned incomplete data, or may have changed its response. Retry later. If the problem persists, report the selected source, affected feature, app version, and a nearby/example location.

### “Request timed out”

Check the network and try again later. VPNs, private DNS, firewalls, captive portals, and temporary provider outages can prevent a response from arriving before the timeout.

### “Required API key missing”, “API requests limit reached”, or “API access unauthorized”

Open the relevant source's settings and check whether it requires a key or account configuration. Only enter a key if Solstice provides a field for that provider. If a provider quota has been reached, wait for its reset or choose another available source. Do not share your key in an issue report.

### “Failed to parse weather data”

The provider may have returned malformed, changed, or unexpected data. Report the selected provider, affected feature, app version, and a nearby/example location. Avoid attaching raw logs that contain precise coordinates or other private data.

### “Secure connection failed”

Check the device date/time, Android version, network filtering, VPN, and whether the error affects one provider or all network requests. A single-provider failure may indicate a provider certificate or server problem. An error affecting many unrelated services may indicate a device/network trust problem. Do not install a certificate or disable TLS validation to work around this error.

## Background weather updates

### Background updates are delayed or do not run

Android and device manufacturers control when background work is scheduled. Battery saver, restricted app battery usage, disabled notifications, a paused work profile, or manufacturer-specific background restrictions may delay updates.

1. Open **Settings → Background updates** in Solstice and check the refresh-rate setting.
2. Review the option to skip updates when the battery is low.
3. Use the battery-optimization help entry on that screen to review Android's setting for Solstice.
4. Check the manufacturer's background-app guidance at [Don't Kill My App](https://dontkillmyapp.com/).
5. Use the Worker information entry in the same settings screen to inspect the most recent recorded update information.

Android may defer scheduled work even when a refresh interval is selected. A selected interval is not a guarantee that the device will refresh at an exact minute.

### I need fresh weather now

Use the app's manual refresh control. This requests an update when the app is in use; it does not change Android's background scheduling rules.

## App-to-app data sharing

Solstice has optional local integrations under **Settings → Modules → Data sharing**. Only select receiving packages you trust. Some integrations can receive saved-place identifiers; Gadgetbridge weather broadcasts can include place name, coordinates, current weather, forecast, and available air-quality data.

The read-only weather ContentProvider is protected by a Solstice-specific Android permission. Grant that permission only to an app you trust. See [3RD_PARTY.md](3RD_PARTY.md) for the currently documented integration surfaces.

## Privacy and data controls

Review [PRIVACY.md](PRIVACY.md) and the provider-specific policies before enabling a source or sharing data with another app. The root PRIVACY.md is a Solstice-specific notice based on currently identified data flows. The technical audit remains open; do not treat it as a completed audit of every dependency, provider, build variant, retention path, or legal requirement.

## Still need help?

Open a [Solstice issue](https://github.com/INFORMATION89/Solstice/issues) with the smallest useful reproduction and sanitized diagnostic details. For security or privacy issues, avoid posting sensitive data publicly; contact the maintainer through an appropriate private disclosure channel.
