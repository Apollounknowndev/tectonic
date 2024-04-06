**Welcome to my pain and suffering! :)**

This project has all maintained Tectonic mod versions, which are split into different modules:

- `fabric-1-18` (Fabric/Quilt 1.18.2)
- `fabric-1-19` (Fabric/Quilt 1.19.2)
- `fabric-1-20` (Fabric/Quilt 1.19.4+)


- `forge-1-18` (Forge 1.18.2)
- `forge-1-19` (Forge 1.19.2)
- `forge-1-19-3` (Forge 1.19.3-1.20.1)
- `forge-1-20` (Forge 1.20.2+)


- `neoforge-1-20` (Neoforge 1.20.2+)


There's three additional modules for shared mod resources:

- `common`: *All* versions use this code. It contains the config system and some basic constants like the mod id.
- `dfs-1-18`: The 1.18 versions use this code. It contains the custom density functions, built for 1.18.2.
- `dfs-1-19`: The 1.19 and 1.20 versions use this code. It contains the custom density functions, built for 1.19+.

The actual files for the world generation are *not* in resources/data like a normal mod. 
Due to needing to load different files based on config options and whether Terralith is loaded, the files are split into four separate datapacks. All datapacks are version-agnostic and will load on all versions 1.18.2+.
- `increased_height` is enabled if increased height is enabled in the config. It raises the max build and generation elevation to y640.
- `legacy` is enabled if legacy mode is enabled in the config. It contains all the old biome files alongside a dimension file to swap the biome layout to the vanilla one.
- `tectonic` is enabled if Terralith is not installed. This has all files for regular Tectonic generation.
- `terratonic` is enabled if Terralith is installed. This has all files for regular Tectonic generation, with small changes to accommodate for Terralith support.