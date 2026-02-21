# JMEM - JustNoone's Minecraft Transit Railway Extension

## Verification Required

The following features rely on the **Minecraft Transit Railway (MTR)** mod. Since the exact class names and structure of MTR are not publicly documented in a standard way accessible here, some assumptions were made in the code. Please verify the following:

### 1. Dependency
Ensure the dependency in `build.gradle` is correct for your version of Minecraft and Fabric.
```groovy
modImplementation "maven.modrinth:minecraft-transit-railway:FABRIC-4.0.3+1.20.1"
```
Check the Modrinth page for the exact version string if this fails.

### 2. Mixins (Depot Logic)
The `DepotMixin.java`, `DashboardScreenMixin.java`, and `TrainMixin.java` in `src/main/java/org/justnoone/jme/mixin` attempt to modify MTR logic.
- **Verify Class Names:** Open these files and check the `@Mixin` targets.
  - `mtr.data.Depot` -> Is this the correct class for Depot data?
  - `mtr.screen.DashboardScreen` -> Is this the correct screen class?
  - `mtr.data.Train` -> Is this the correct Train class?
- **Verify Method Names:** Check if `writePacket`/`readPacket` exist in `Depot`, `init` in `DashboardScreen`, and `getSpeed` in `Train`.
- **Implement Bridge:** You must implement logic to copy `maxSpeed` from `Depot` to the spawned `Train` (using `train.setMaxSpeedOverride()`). This likely belongs in a Mixin targeting the method where `Depot` spawns trains (e.g., `Depot.generateTrain` or `Siding.generateTrain`).

### 3. PIDS
The `PIDS_ODD` block is currently a standard block. To make it functional like MTR PIDS:
- Extend the `PIDSBlockEntity` class from MTR in a new `BlockEntity` class.
- Create a custom renderer for the 3-wide model.
- Update `ModBlocks.PIDS_ODD` to use this new entity.

### 4. Cutting Builder
The `CuttingBuilderItem` logic is implemented. Shift-Right-Click to cycle width (3, 5, 7, 9, 11). Right-Click on a block to build downwards.
