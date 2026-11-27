# Solstice integrations with other apps

Solstice exposes a small number of Android app-to-app integrations. These are local Android integration mechanisms; they are not a Solstice cloud sync service or telemetry pipeline.

## Gadgetbridge weather broadcast

Solstice implements the Generic Weather broadcast action used by Gadgetbridge:

`nodomain.freeyourgadget.gadgetbridge.ACTION_GENERIC_WEATHER`

To enable it, open **Settings → Modules → Data sharing** and select the installed receiving package for Gadgetbridge. Solstice sends weather information in the Android broadcast payload, including place name, latitude/longitude, current conditions, forecast details, and available air-quality information.

Only enable a recipient you trust. The receiving application gets the location and weather fields in the payload and controls its own subsequent handling of that data. Review the receiving app's privacy policy as well.

## Location update notifier

Solstice also has an app-specific location-update broadcast. For packages explicitly selected in **Settings → Modules → Data sharing**, it sends updated location IDs and the list of saved location IDs. This notifier does not send the full weather payload by itself.

The action is based on the installed Solstice application ID:

`${applicationId}.ACTION_UPDATE_NOTIFIER`

## Read-only weather ContentProvider

Solstice exposes a read-only Android ContentProvider protected by an app-specific dangerous permission:

`${applicationId}.READ_PROVIDER`

A compatible receiving application must request the permission for the installed Solstice package and the user must grant it. The provider can expose saved locations, including their coordinates, and weather data. Treat this as a sensitive local data-sharing capability; only grant access to apps you trust.

The existing permission-prompt label and description still contain inherited Breezy Weather wording. Do not treat those strings as the final Solstice integration documentation; their rebranding and full disclosure review are tracked in the modernization checklist.

## Compatibility status

This document describes the integration mechanisms currently found in Solstice's source. It is not a compatibility certification for every third-party app.

Older upstream directories listed apps such as Kvaesitso, Smartspacer Generic Weather, and Wear Sync as Breezy Weather integrations. Their compatibility with Solstice's package name, permission, provider authority, and broadcast configuration has not been verified in this audit. They are therefore not listed as supported Solstice integrations here.

For integration support or to report a compatibility issue, use [Solstice Issues](https://github.com/INFORMATION89/Solstice/issues).
