# Fix KSP Plugin Sync Error

The project is failing to sync because it's attempting to use an invalid KSP version (`2.2.10-1.0.24`). KSP versions must strictly match the Kotlin version and be a valid release. For Kotlin `2.2.10`, the recommended KSP version is `2.2.10-2.0.2`.

Additionally, the `settings.gradle.kts` contains a repository filter that explicitly excludes `com.google.devtools.ksp` from the `google()` repository, which could cause resolution issues if the plugin is hosted there.

## Proposed Changes

### Build Configuration

#### [MODIFY] [libs.versions.toml](file:///C:/Users/pc/OneDrive/Documents/Project/Cello-POS/gradle/libs.versions.toml)
- Update `kotlin` version to `2.2.10`.
- Update `ksp` version to `2.2.10-2.0.2`.

#### [MODIFY] [settings.gradle.kts](file:///C:/Users/pc/OneDrive/Documents/Project/Cello-POS/settings.gradle.kts)
- Remove the negative lookahead filter for `com.google.devtools.ksp` in the `google()` repository configuration to ensure the plugin can be resolved from all configured repositories.

## Verification Plan

### Automated Tests
- Run Gradle sync to verify the plugin is found and the project builds successfully.
- Command: `gradlew help` (basic sync check).

### Manual Verification
- Verify that the "Sync" tab in Android Studio shows no errors.
