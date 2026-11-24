# Meridian M3: official alerts (CAP, WMO, FOSS Public Alert Server)

Status: **spec, clean room step 1** (2026-11-23), from the OASIS CAP 1.2 standard, WMO's Severe Weather
Information Centre pages and source list, Google's public CAP guidance, KDE's public pages, and the FOSS
Public Alert Server's OpenAPI description (read from KDE's GitHub mirror; only the API description, not
the server's code).

## Scope

For a place, return every official alert that covers it, from WMO's official feeds and the FOSS Public
Alert Server, merged so the same alert never shows twice, newest version only, ordered by importance.
Alerts are never reworded or blended.

## The alert model (CAP 1.2)

**Alert:** `identifier`, `sender`, `sent`, `status`, `msgType`, `scope`, `references`, `incidents`,
plus one or more **info** blocks.
- Keep only `status` = `Actual` and `scope` = `Public` (exercises, tests, drafts and restricted messages
  never show; Google's public alerts apply the same rule).
- `msgType`: `Alert` (new), `Update` (replaces the ones it references), `Cancel` (withdraws them), `Ack`
  and `Error` (ignored for display).
- `references`: a space-separated list of `sender,identifier,sent` triples.

**Info** (one per language, sometimes several per language for different events): `language`,
`category`, `event`, `responseType`, `urgency`, `severity`, `certainty`, `effective`, `onset`, `expires`,
`senderName`, `headline`, `description`, `instruction`, `web`, `contact`, `eventCode`, `parameter`, and
one or more **area** blocks.
- Language: the app's language, else the first block in the same language family, else the first block.

**Area:** `areaDesc`, any number of `polygon` (space-separated `lat,lon` pairs, first equals last),
`circle` (`lat,lon radius` in km), `geocode` (name and value), `altitude`, `ceiling`.

Meridian's alert type keeps all of these, plus the source and provenance.

## Sources

### WMO Severe Weather Information Centre
1. **Source list** (`https://severeweather.wmo.int/json/sources.json`), refreshed weekly: for each
   alerting authority its id, country (`authorityCountry`), whether it's official
   (`sourceIsOfficial`), registered, its CAP feed (`capAlertFeed`, an RSS list) and the feed's status
   (`capAlertFeedStatus`).
2. For a place: the authorities of the place's country that are official, registered and operating.
3. Read each feed (RSS), every refresh interval from configuration (start at 15 minutes). Each item
   links to one CAP message.
4. Fetch only messages not already cached (by link and identifier); parse; keep those that pass the
   area test below.
- **Terms:** free to reuse; credit the issuing agency (`senderName`, for Mexico "Servicio Meteorológico
  Nacional"); WMO and the agencies give no warranty.

### FOSS Public Alert Server (completed 2026-11-23 from its OpenAPI description, version 1.0.0)
- Aggregates hundreds of CAP feeds worldwide, including civil-protection feeds the WMO list doesn't
  carry (6 providers in Germany, Taiwan's NCDR, Japan's typhoon centre). Server: `https://alerts.kde.org`.
- **`GET /alert/area?min_lat=&max_lat=&min_lon=&max_lon=`**: all alerts for a bounding box (latitudes
  −90 to 90, longitudes −180 to 180; max must be larger than min, so a box can't cross the antimeridian:
  split it in two there). The box is the place plus a margin from configuration. Response: a JSON list
  of alerts (the schema describes CAP fields; record a real response before building and map from that).
- **`GET /alert/{UUID}`**: one alert as CAP 1.2 XML, for anything the list doesn't carry in full.
- **`GET /source/area_status?country_codes=`**: which feeds the server has for ISO 3166-1 country codes,
  with each feed's name and status. Used for the sources screen ("Alerts for Mexico: none on this
  server").
- **`GET /config/server_status`**: version, operator, links to its privacy policy and terms of service,
  and a **congestion state**. Meridian checks it at most daily and backs off while the server reports
  congestion.
- Not used: the subscription endpoints (they register a push endpoint; Meridian polls instead, so no push
  token ever leaves the phone).
- **Terms:** a Terms of Service page on KDE's wiki, linked from the server status. Still not found from
  here (searched 2026-11-24); read it from the app's first `/config/server_status` response on Saturday
  before the adapter is merged. The project's README says it's still in development and not ready for
  production; KDE's 2025 CAP workshop slides list Breezy Weather among the apps already using it.
- **Clean room:** Breezy Weather's own FPAS source code is in this repo; don't open it while building
  Meridian's adapter (RULES.md section 5).
- The API calls itself early testing and says it may change: the adapter checks `server_version` and
  treats unknown shapes as problems. Always paired with WMO, never alone.

## Does it cover the place?

1. **Polygons:** the place is inside any polygon (ray casting; points on an edge count as inside).
2. **Circles:** distance to the centre (great-circle) at most the radius.
3. **Only geocodes:** kept, but marked "area not precise" in provenance; shown with "may not include
   your exact area". Geocode lookup tables come later, per country, as data.
4. **Altitude and ceiling:** ignored for ground-level places.
5. A place near an edge: no buffer for now (a token exists, set to 0).

## Lifetime

- Shown from `effective` (or `sent`) until `expires`. With no `expires`: `onset` (or `sent`) plus a
  default duration from configuration, marked as an estimate.
- Expired alerts drop out at once; a `Cancel` removes what it references even before expiry.

## Merging (no alert twice)

1. **Same message, two routes** (WMO and the alert server both carry it): same `sender` and
   `identifier` → one alert; provenance lists both routes.
2. **Versions:** an `Update` replaces every alert it references; a `Cancel` removes them. Chains are
   followed to the newest version. A referenced alert we never saw is fine: the update stands alone.
3. **Different authorities, same hazard** (SMN and the city's civil protection, for example): both
   kept, grouped, the most local first. Their texts are never merged.
4. **Same authority, several info blocks for different events:** separate alerts in the result.

## Order

Severity (Extreme, Severe, Moderate, Minor, Unknown), then urgency (Immediate, Expected, Future, Past,
Unknown), then onset, earliest first. Ties: most local authority first.

## Problems

A feed that fails, a malformed message, an unknown value: a problem in the result for that source;
other sources and other messages still go through. Never an exception across the API.

## Credit

Each alert names its issuing agency (`senderName`, or the WMO source name), and the route ("via WMO",
"via FOSS Public Alert Server").

## Fixtures

Real CAP messages recorded from public feeds: Mexico (SMN), the US (NWS), Germany (DWD), a MeteoAlarm
country; an Update chain; a Cancel; a message in two languages; a geocode-only message; a circle; an
expired message; a Test message; one malformed file.

## Tests

- Parsing every element above, polygons and circles included.
- Status and scope filters; language choice.
- Point in polygon (including concave shapes and edges) and in circle.
- Update and Cancel chains; the same message from two routes; two authorities kept apart.
- Expiry and the default lifetime; ordering.
- A failing feed doesn't block the others.

## Done when

Recorded alerts for Mexico, the US and Germany come out merged, ordered and credited correctly; all tests
pass on CI; the alert server's terms of service are read and recorded.

## Consulted

OASIS CAP 1.2; WMO Severe Weather Information Centre (notes to users, source list, CAP feeds page); WMO
Register of Alerting Authorities RSS; Google Public Alerts CAP requirements (alert and area entities); KDE's
FOSS Public Alert Server pages, source status and OpenAPI description (`openAPI-docu.yaml`,
version 1.0.0); Volker Krause's posts on the alert server.
