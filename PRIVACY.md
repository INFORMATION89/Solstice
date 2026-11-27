# Solstice Privacy Notice

**Last reviewed:** 27 November 2026  
**Applies to:** the Solstice Android application and the features described below. Providers and options can vary by build, region, device, and your choices.

Solstice is a weather application. To provide forecasts and location-aware features, it processes information on your device and sends requests to external services. This notice describes data flows identified in the current Solstice code review. It does not claim that no personal data is processed, and it does not mean Solstice controls what an independent provider does with a request after receiving it.

## Information Solstice uses

**Location and saved places.** If you grant Android location permission and use current-location features, Solstice receives a location from Android location services. This can include latitude and longitude. You can deny location permission and select a place manually. Saved places and associated weather information are used to display forecasts and manage your locations.

**IP-based location.** In builds where the IP.SB location option is available, choosing it sends a request to IP.SB. The service can see the public IP address of your internet connection and returns an approximate location. This is separate from GPS or other device location permission; it is not a request for Solstice to upload your GPS coordinates. See the [IP.SB Privacy Policy](https://ip.sb/privacy-policy/).

**Location search and place names.** Location search uses GeoNames in the reviewed source configuration. A search can send the text you enter and language or locale parameters to GeoNames; the service also receives ordinary network information such as your IP address. Reverse geocoding may use Android Geocoder or Nominatim, depending on build and device. A Nominatim lookup can send coordinates and language information to the configured service. See [GeoNames](https://www.geonames.org/), the [OpenStreetMap Foundation Privacy Policy](https://osmfoundation.org/wiki/Privacy_Policy/), and your device provider's privacy information for Android location services.

**Weather, air-quality, pollen, and alert requests.** Solstice requests information needed for the selected weather features and sources. The reviewed source configuration includes Open-Meteo, MET Norway, NCEI, OpenWeather, Pirate Weather, FOSS Public Alert Server, WMO Severe Weather, Environment and Climate Change Canada, and the US National Weather Service. Availability depends on coverage, feature, settings, and build. Not every provider is contacted for every refresh. Requests can contain coordinates or provider-specific place identifiers, requested forecast variables and time ranges, and ordinary network metadata such as your IP address. The provider receiving a request can process and retain it under its own terms and privacy policy. Solstice cannot control that provider's retention or onward use.

**Optional update check.** The app-update check is disabled by default in the reviewed settings. If you enable it in a build that supports it, Solstice contacts the configured GitHub Releases API to check for a release. GitHub receives the request and ordinary network metadata, including your IP address. The app compares returned release information locally. The exact schedule and request-header details may vary by version. See [GitHub's Privacy Statement](https://docs.github.com/en/site-policy/privacy-policies/github-general-privacy-statement).

## Sharing with other apps on your device

Solstice includes Android integrations that can share weather or location information with other apps on the same device:

- **Gadgetbridge weather broadcast:** when you configure receiving apps in Data sharing, the selected receiver can receive saved place names, coordinates, current conditions, and available forecast/weather fields.
- **Location update notifier:** configured receiver apps can receive updated saved-location identifiers.
- **Weather ContentProvider:** another Android app can request saved places and weather information, including precise coordinates and forecast details, if Android grants it Solstice's protected read permission. Review the permission prompt and only grant access to apps you trust.

These are local app-to-app disclosures, not uploads to a Solstice cloud account. After another app receives information, that app's own privacy policy and handling apply. You can avoid these disclosures by not configuring or granting access to receiving apps.

## Diagnostics and files you choose to share

The reviewed error-log export is started by the user. It creates a local file that can include error-level system logs, Solstice version/build information, Android version, and device manufacturer/model details. Solstice presents the file for you to open or share; the reviewed path does not automatically upload it. Logs can contain additional context, so inspect a file before sending it. The recipient and any service used to share it will process it under their own policies.

## Storage, retention, and account information

The reviewed Solstice features do not require you to create a Solstice account. Weather and place information is used by the app on your device, and requests are sent to the external service needed for the feature you use. External providers set their own retention periods. A complete audit of every dependency's diagnostics behavior, Android backup behavior, and all local-data deletion paths is still in progress; this notice therefore does not make a blanket promise about every SDK, device service, or third-party retention practice.

## Analytics, advertising, and optional contributions

Solstice is a non-commercial project. It is not intended to generate revenue through advertising, subscriptions, paid features, or in-app purchases. This product decision does not by itself establish that every dependency is free of analytics or diagnostics; those technical data flows remain subject to the audit described above. The planned optional contribution of weather-error data is not represented as an active feature by this notice. Any future remote contribution, analytics, or other non-essential data sharing must be separately reviewed, explained before activation, and governed by an explicit user choice. Enabling one local integration does not enable another form of data sharing.

## Your choices

- Deny Android location permission and select a place manually.
- Choose which location and weather sources are enabled where Solstice exposes those settings.
- Leave the optional update check disabled.
- Do not configure Gadgetbridge or other receiving apps, and do not grant the weather provider permission to apps you do not trust.
- Review log files before sharing them.

## Changes and contact

This notice may change when Solstice's code, providers, or integrations change. The repository's release notes and this file are the source for the current notice. For questions or to report a privacy concern, use the Solstice project's public [GitHub repository](https://github.com/INFORMATION89/Solstice). Do not include precise location data, logs, credentials, or other sensitive information in a public issue.

---

This notice explains currently identified app behavior; it is not legal advice. Provider terms and privacy practices can change independently. Consult linked provider notices for their handling of requests.