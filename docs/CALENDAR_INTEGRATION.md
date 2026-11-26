# Calendar-aware weather

**Status:** approved for planning on 2026-11-26; not implemented.

## Goal

Show an event together with the weather expected at that event's physical location and time. This should work with Outlook when its events are exposed through Android's system calendar, without a provider-specific account integration.

## Required integration

Use Android's built-in Calendar Provider API:

- Query events through `CalendarContract` / `ContentResolver`.
- Request `READ_CALENDAR` only after the user explicitly enables calendar weather.
- Important permission limitation: `READ_CALENDAR` is a broad Android app permission, not a per-event or per-calendar grant. Once granted, the app can query calendar data exposed by the provider. Keep the query constrained to upcoming visible calendars, a bounded time window, and the minimal field projection; do not imply that Android grants access only to Outlook or to a single selected calendar.
- Read only calendars marked visible by the provider and only upcoming occurrences needed by the feature. No full calendar-history scan.
- Keep access read-only. Do not request `WRITE_CALENDAR`.
- Query only a bounded future window and the fields required by the feature: event title, start/end time, all-day state, time zone where available, and location.
- Use the `Instances` view for occurrences in the requested time window, so recurring events are represented as individual upcoming occurrences.
- Do not read event descriptions, attendees, reminders, attachments, emails, or unrelated calendar metadata.

There is no Microsoft-specific connection. Do not add Microsoft Graph, an Outlook SDK, OAuth, Microsoft sign-in, refresh tokens, or a Solstice calendar server.

## Outlook compatibility

Outlook for Android can optionally synchronize its calendar data with the native Android Calendar app. Solstice sees those events only when the user has enabled that sync and the account or organization policy permits it. If Outlook events are not present in Android's Calendar Provider, Solstice must not attempt a separate Outlook connection.

The same provider-based approach can work with other calendar accounts that sync into Android's calendar provider.

## Event and location rules

- An event is eligible for weather only when its own location field resolves to a real physical place.
- Resolve an address or recognizable place using Solstice's existing location/geocoding path. Do not infer a location from the event title, description, organizer, or the user's current position.
- If the location is absent, virtual, ambiguous, or cannot be resolved confidently, show the event without weather.
- Use the event's actual start/end time and time zone to select the relevant forecast period. For all-day events, use the forecast for that local calendar day.
- Fetch weather through Solstice's existing weather-source boundary. Send only the resolved weather location needed for the forecast; never send the event title or calendar identity to a weather provider.

## User control and privacy

- Calendar weather remains optional and independent of core weather functionality.
- Explain the requested access before the Android permission prompt.
- If permission is denied or later revoked, hide calendar-derived information and keep the rest of Solstice working normally.
- Provide an in-app switch to disable the feature. No event content is retained longer than needed for the displayed upcoming forecast.

## Acceptance checks

Before release, verify permission granted/denied/revoked states; events with and without locations; ambiguous and virtual locations; recurring events; all-day events; time-zone boundaries; canceled/deleted events; and Outlook events both with sync enabled and with sync unavailable.

## Platform references

- [Android Calendar Provider overview](https://developer.android.com/identity/providers/calendar-provider)
- [CalendarContract API reference](https://developer.android.com/reference/android/provider/CalendarContract)
- [Android READ_CALENDAR permission](https://developer.android.com/reference/android/Manifest.permission#READ_CALENDAR)
- [Microsoft: Outlook calendar synchronization and organization controls](https://learn.microsoft.com/exchange/clients-and-mobile-in-exchange-online/outlook-for-ios-and-android/outlook-for-ios-and-android-configuration-with-microsoft-intune)
