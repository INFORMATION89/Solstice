# Solstice

Solstice is a weather application focused on clear forecasts, useful context and practical preparation. It brings current conditions, detailed forecasts and a forward-looking Outlook into one readable experience.

The application is being developed for Android. Its architecture separates the product experience from reusable components and the weather engine, allowing each part to evolve independently.

## Project documentation

- [Product direction and decisions](docs/PLAN.md)
- [Development rules](docs/RULES.md)
- [Reconstruction strategy](docs/RECONSTRUCTION.md)
- [Current work and next steps](docs/CONTINUE.md)

## Testing builds

Every push to `main` publishes a test build to the **[testing pre-release](https://github.com/INFORMATION89/Solstice/releases/tag/testing)**.

The release uses the `profile` build variant. It installs alongside other Solstice variants and preserves the data and settings of an existing installation of the same variant.

1. Open the [testing pre-release](https://github.com/INFORMATION89/Solstice/releases/tag/testing) on your Android device.
2. Download the latest `solstice-testing-<commit>.apk`.
3. If Android asks, allow your browser to install apps from this source, then open the downloaded APK.
4. Install the update. Your existing places and settings for that build variant are retained.

To receive new testing builds automatically, add this repository to [Obtainium](https://github.com/ImranR98/Obtainium) and enable **Include pre-releases**.

## License

Solstice includes components with different licensing and attribution requirements. See the repository's license and notice files for the terms that apply to each component.
