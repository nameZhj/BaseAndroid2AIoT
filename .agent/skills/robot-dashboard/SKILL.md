# Robot Dashboard Skill

Load this skill for robot, robot-dog, AGV, patrol, mission, map, telemetry, event, camera or control-console work.

## Task entry points

- Screen and adaptive composition: \`app/src/main/kotlin/com/base/iot/feature/robot/RobotDashboardScreen.kt\`
- Immutable UI snapshots and sample state: \`RobotDashboardModels.kt\`
- Map viewport and overlay drawing: \`components/RobotMapCard.kt\`
- Global theme tokens: \`core/src/main/kotlin/com/base/iot/core/ui/theme/AppTheme.kt\`
- Shared device transports: \`core/src/main/kotlin/com/base/iot/core/iot/IotHub.kt\` and the protocol manager interfaces below it
- Main app mount: \`app/src/main/kotlin/com/base/iot/MainActivity.kt\`

## Data and safety boundaries

- Keep robot business models, repositories, ViewModels and screens in \`:app\`; keep reusable transport abstractions and protocol drivers in \`:core\`.
- UI consumes immutable state. Repository/transport updates flow into a ViewModel, then into Compose through lifecycle-aware StateFlow collection.
- Sample data must remain explicitly labelled and must never be reported as live device telemetry.
- Do not dispatch movement, mission, emergency-stop or actuator commands from sample state. Before adding control actions, require a resolved device identity, a connected session, command validation, explicit operator intent, timeout handling and a visible acknowledgement/error result.
- Do not infer battery, localization, task completion, obstacle clearance or event resolution from missing values. Model unavailable, stale and disconnected states separately.
- Keep endpoints, ports, credentials, timeouts, retry policies and topic names in the project's configuration authority, not in UI or sample models.

## Map integration

- The current Canvas is a schematic UI placeholder, not a PGM/PCD parser, SLAM renderer or navigation stack.
- For occupancy grids, preserve resolution, origin pose, map dimensions, occupancy thresholds and row orientation. Define and test one world-to-screen transform shared by the map, robot pose, route, waypoint and event overlays.
- For PCD/point-cloud views, parse and transform off the main thread, bound memory, decimate at an explicit quality level and cache by source/version.
- Keep map aspect ratio and coordinate scaling consistent. Do not stretch the map to fill an arbitrary panel.
- Distinguish map loading, invalid map, no localization, stale pose and live localization states.
- Add tests for origin offsets, rotated origins, axis direction, pixel-to-world conversion and map resize.

## Device, mission and event integration

- Normalize device identity, connection state, battery, signal, pose freshness and last-seen time in the repository layer.
- Mission state should represent queued, dispatched, running, paused, completed, failed and cancelled. Progress must be bounded and derived from acknowledged waypoint/task updates.
- Events should carry stable ID, severity, timestamp, point/world location, description, media reference and resolution state. Avoid hardcoding event labels or point IDs into drawing code.
- Image evidence should use a lifecycle-aware image loader with an empty/error placeholder and bounded thumbnail dimensions.

## Video integration

- Keep camera metadata and stream lifecycle out of composables.
- Use a dedicated provider abstraction for RTSP/WebRTC and a lifecycle-aware rendering surface.
- Do not create or reconnect stream sessions from composition. Release playback resources when the screen stops or the device changes.
- Show connection, buffering, unavailable and playback-error states distinctly.

## Adaptive layout and validation

- Use current app-window constraints, not physical device identity or orientation alone.
- Compact: below 600dp. Medium: 600dp to 839dp. Expanded: 840dp and above.
- At Expanded width, prioritize the map and keep device/mission context visible beside it; place statistics and event triage in a companion region.
- At Medium width, use a scrollable layout and only use two-column cards when their minimum readable width is preserved.
- At Compact width, use one scrollable column. Keep controls at least 48dp and protect labels from pushing status/action elements away.
- Review at 360x800, 600x960, 840x600, 1280x800 and in split-window widths around 600dp and 840dp.
- Verify large font scales, long device IDs, long event descriptions, disconnected/stale states, empty event lists and map load failures.
- Run \`./gradlew :app:compileDebugKotlin :app:assembleDebug :core:testDebugUnitTest :app:testDebugUnitTest\` before delivery.
