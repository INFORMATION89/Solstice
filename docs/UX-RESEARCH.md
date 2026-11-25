# UX research

Status: documentary research and preliminary product benchmark. This is not a usability study of Solstice.
Research record: 2026-11-25.

## Purpose and scope

This document records the evidence used to evaluate Solstice's weather experience: forecast comprehension, thermal comfort, warnings, adaptive preparation, notifications, widgets, privacy and usability. It separates published findings from product decisions and from questions that still require testing.

The sources below are a focused evidence base, not a systematic review. Studies differ in population, method, date and context; results should not be treated as universal design laws. Several studies concern television forecasts, general mobile notifications or recommender systems rather than weather apps specifically.

## Findings

### 1. Weather-app usability is more than visual appeal

Khamaj, Kang and Argyle's user-centred study of smartphone weather-app usability identified cognitive load, effectiveness, efficiency and user perceptions as recurring themes. The work supports designing around the decisions people need to make, not maximizing the number of displayed variables.

A separate study of college students' smartphone weather-app use found that people valued information and detail, while ease of use, understandability and graphics were also cited by people who switched apps. These findings support Solstice's principle: actionable information first, with additional detail available on demand.

**Implication:** retain the product principle “inform, don't inundate”. Evaluate whether people can answer practical questions quickly; do not use screen density or feature count as a proxy for usefulness.

Sources:
- [Khamaj, Kang & Argyle (2019), Users’ Perceptions of Smartphone Weather Applications’ Usability](https://doi.org/10.1177/1071181319631098)
- [Weather on the Go: An Assessment of Smartphone Mobile Weather Application Use among College Students](https://doi.org/10.1175/BAMS-D-18-0020.1)

### 2. Forecast timing and probability need context

Reed and colleagues studied extended forecast graphics with 885 respondents. Modified layouts that reduced the number of days shown, emphasized timing and intensity, and removed some probability information performed better on comprehension measures than the conventional graphic tested. The paper does not establish that every app should show only three days. It does show that long-range detail and probability can be misunderstood when presented with the same visual certainty as near-term conditions.

An earlier study found substantial difficulty identifying when precipitation would occur and how much was expected when the graphic did not communicate timing and amount clearly.

**Implication:** preserve the full Outlook horizon where useful, but progressively reduce specificity as lead time increases. Put timing and expected impact ahead of decorative daily summaries. Explain probabilities in context and avoid making distant forecasts look as certain as near-term ones.

Sources:
- [Reed et al. (2021), Modifying the Extended Forecast Graphic to Improve Comprehension](https://doi.org/10.1175/WCAS-D-20-0086.1) — online public survey, n=885.
- [Reed & Senkbeil (2020), Perception and Comprehension of the Extended Forecast Graphic](https://doi.org/10.1175/BAMS-D-19-0078.1)

### 3. “Feels like” is useful, but the index must be identified

Thermal-comfort values are modelled indices, not direct measurements of an individual's subjective experience. The name “feels like” can refer to different calculations:

- Heat Index combines air temperature and relative humidity and is intended for hot conditions. NOAA notes that its standard values are for shade; full sunshine can raise the apparent heat burden substantially.
- Wind chill estimates cooling effects from wind and air temperature in cold conditions.
- Apparent-temperature formulations vary in the variables and assumptions they use.
- UTCI estimates outdoor thermal stress using air temperature, humidity, wind and radiant temperature, with a defined reference person and activity.

These values are not interchangeable. A source's displayed “feels like” value must not be relabelled as UTCI, Heat Index or another index unless its method supports that claim.

**Implication:** Solstice's agreed hierarchy is to show both the feels-like value and actual air temperature, giving feels-like the visual lead. Preserve the source and method where known. Explanations may mention only factors actually represented in the calculation or available data. Do not claim that sun exposure, wind or humidity explains a difference unless the relevant input is known. Do not imply an exact prediction of an individual's experience.

Sources:
- [NOAA: Heat Index](https://www.noaa.gov/jetstream/synoptic/heat-index)
- [NOAA/NWS: Heat Index and heat safety](https://www.weather.gov/ctp/heat)
- [ECMWF: Heat and cold indices, mean radiant temperature and UTCI](https://confluence.ecmwf.int/spaces/FCST/pages/462912535/New+parameters+heat+and+cold+indices+mean+radiant+temperature+and+globe+temperature)
- [Copernicus: ERA5-HEAT data and methods](https://climate.copernicus.eu/GCH2025-about-data-and-methods)

### 4. Warnings should communicate likelihood and impact

A 2023 experiment with 550 UK residents compared warning formats and found that communicating likelihood and impact affected perceived severity, concern, trust and intended protective response. In particular, a low-likelihood/high-impact warning could be perceived as less concerning when probability information was presented without adequate impact context.

**Implication:** severe-weather cards and alerts should communicate what may happen, how likely it is when reliable probability exists, what impact is expected and what action is appropriate. Colour must not carry severity alone. Avoid hiding a high-impact possibility behind a low probability number.

Source:
- [The effect of likelihood and impact information on public response to severe weather warnings (2023)](https://pubmed.ncbi.nlm.nih.gov/37743536/) — experiment, n=550.

### 5. Adaptive notification timing can help, but evidence is not weather-specific

A large in-the-wild study integrated interruptibility detection into Yahoo! JAPAN's Android app. Across more than 680,000 users over three weeks, delaying many notifications until a detected interruptible moment reduced response time by 49.7% compared with immediate delivery. The study used news notifications and its own sensing/model setup; it does not prove the same effect for weather alerts or justify delaying urgent warnings.

**Implication:** adapt routine recommendations and non-urgent notifications where safe. Official severe warnings and time-critical hazards must follow safety-first delivery rules and must not be suppressed merely because the user is predicted to be busy. Measure annoyance, usefulness and missed-event rates, not just opens.

Source:
- [Okoshi et al. (2017), Attention and engagement-awareness in the wild: A large-scale study with adaptive notifications](https://doi.org/10.1109/PerCom.2017.7917856) — more than 680,000 users.

### 6. Widgets are constrained by the operating system

Android's AppWidget documentation states that periodic updates requested through `updatePeriodMillis` are not delivered more frequently than once every 30 minutes. Other update paths exist, but remain subject to execution, battery and background-work constraints.

**Implication:** separate the decision about *what* a widget should show from the mechanism and timing used to refresh its data. The intelligent widget may prioritize important events, timely recommendations and useful conditions, but Solstice must not promise minute-by-minute rotation or real-time refresh. Keep a timestamp and honest stale-data state.

Sources:
- [Android Developers: Create an advanced widget — update frequency](https://developer.android.com/develop/ui/views/appwidgets/advanced)
- [Android API: AppWidgetProviderInfo.updatePeriodMillis](https://developer.android.com/reference/android/appwidget/AppWidgetProviderInfo#updatePeriodMillis)

### 7. Personalization needs understandable controls

A field study of a mobile-sensing privacy dashboard (n=227) found that presenting raw logs alone could deter use, while giving users control over collection could compensate for that effect. Research on recommender-system mental models also found uncertainty and confusion about how personalization works.

**Implication:** Solstice should explain adaptation in plain language, expose useful controls and avoid dumping raw behavioural logs on people. The agreed Quiet and Explainable modes can change how often learning is explained, but neither may hide that learning exists or remove the ability to inspect, correct, pause or disable it.

Sources:
- [Bemmann et al. (2022), The Influence of Transparency and Control on the Willingness of Data Sharing in Adaptive Mobile Apps](https://doi.org/10.1145/3546724) — field study, n=227.
- [Ngo, Kunkel & Ziegler (2020), Exploring Mental Models for Transparent and Controllable Recommender Systems](https://doi.org/10.1145/3340631.3394841)

## Preliminary product benchmark

This is a directional review of product patterns, not a scored feature-by-feature comparison or a claim that current releases have been exhaustively tested on every platform.

| Product | Useful pattern to study | Risk Solstice should avoid |
|---|---|---|
| Apple Weather | Clear forecast hierarchy, hourly/daily views and weather details | Copying visual patterns without validating their purpose on Android |
| The Weather Channel | Prominent severe-weather information and broad forecast coverage | Too many modules competing for attention |
| AccuWeather | Feels-like/RealFeel framing and short-term precipitation presentation | Treating a branded index as a universal or personally exact measurement |
| Windy | Deeper model and map exploration for advanced users | Exposing specialist complexity in the primary everyday flow |
| CARROT Weather | User-facing customization and configurable alerts | Making core usability depend on extensive setup |
| Google Weather | Familiar, compact everyday weather presentation | Depending on a product surface whose distribution and availability can change |

A proper visual benchmark still requires dated screenshots and hands-on checks of the versions available to the team. The table above is a research prompt, not proof of current feature parity.

## Evidence quality and limitations

- The weather-app usability evidence is relatively small and qualitative; it identifies themes, not universal rankings.
- Extended-forecast studies concern particular graphic formats and survey populations. They support caution about long-range specificity, not a mandatory three-day product limit.
- The adaptive-notification result is large-scale but comes from a non-weather app and cannot justify delaying emergency warnings.
- The warning experiment concerns UK participants and warning scenarios; wording and risk perception should be tested with Mexican users and local warning conventions.
- Thermal indices are method-dependent. Solstice must preserve provenance and avoid false precision.
- Android documentation describes platform constraints, not guaranteed scheduling or delivery on every device.
- No published study here validates Solstice's exact card layout, routine-learning model, widget rotation strategy or feels-like visual hierarchy. Those require product-specific usability tests.

## Research conclusions

The current product direction is broadly consistent with the evidence when implemented carefully:

1. Keep the everyday experience concise and action-oriented.
2. Make forecast timing and impact easier to understand than raw data density.
3. Show feels-like and air temperature together, with the agreed visual priority, while identifying the underlying method honestly.
4. Distinguish near-term forecast detail from longer-range trends.
5. Adapt non-urgent preparation and notifications cautiously; protect urgent warning delivery.
6. Treat widgets as constrained surfaces with explicit freshness states.
7. Make learning inspectable and controllable without overwhelming users with raw logs.

See [UX-AUDIT.md](UX-AUDIT.md) for the decision-by-decision assessment and [UX-VALIDATION-PLAN.md](UX-VALIDATION-PLAN.md) for the tests still required.
