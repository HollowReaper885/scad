# Audio Cassettes – Fabric 1.20.1 port

Port of [Shoxie's Audio Cassettes](https://github.com/Shoxie12/Audiocassettes) (Forge 1.20.1, v1.2) to Fabric.
Original work © Shoxie, licensed LGPL-2.1 (see `LICENSE`); this port stays under the same license.
Credits from the original: McJty, Kenny Tutorials, and AshIndigo's Walkman mod.

**Status: written against the Fabric 1.20.1 APIs but NOT compiled or run yet** (the environment it was written in
could not reach Fabric/Mojang servers). Expect to fix a few compile errors on the first build.

## Building
Needs JDK 17.
```
gradle wrapper --gradle-version 8.7     # once, if you don't have gradlew (needs a local Gradle install)
./gradlew build                          # jar ends up in build/libs/
./gradlew runClient                      # test in a dev client
```
Mappings are Mojang's official names (same as the Forge source), Fabric API 0.92.2+1.20.1, Loader 0.15.11.
Mod Menu is optional (compile-only) and provides the settings screen.

## What changed vs. the Forge version
| Forge | Fabric |
|---|---|
| `@Mod`, `DistExecutor` proxy | `ModInitializer` + `ClientModInitializer`; client code in client-only classes (`proxy/ClientProxy`) |
| `DeferredRegister` / `RegistryObject` | `Registry.register` in `init/Init` (fields are plain objects, no `.get()`) |
| `SimpleChannel` + 16 packet classes | Fabric networking channels: `networking/Networking` (common) and `ClientNetworking` (client) |
| `NetworkHooks.openScreen`, `IForgeMenuType` | `ExtendedScreenHandlerFactory` / `ExtendedScreenHandlerType` |
| `ItemStackHandler`, `LazyOptional`, capabilities | `inventory/StackInventory` (vanilla container, same NBT layout) |
| Walkman item-handler **capability** | cassette stored in the Walkman stack's NBT under `walkman` (`inventory/WalkmanContainer`) |
| `ForgeConfigSpec` TOML | `config/audiocassettes-common.json` (same two options) |
| Forge config screen hook | Mod Menu entrypoint |
| `ForgeHooks.newChatWithLinks` | plain `Component.literal` |

## Behaviour fixes made on the way (they were bugs or Forge-specific hacks)
- Removing the cassette from a playing Boombox/Walkman now stops the music on the server (the old code only worked in singleplayer).
- Walkman always gets a stable id (the old code could produce an empty id); offhand Walkmen are found; Stop also clears the server-side "playing" flag.
- Config screen toggles take effect immediately (old code copied the values once at startup).
- Boombox shift-click no longer pushes items into inventory slot 1; Tape Deck shift-click can use the last hotbar slot.
- Walkman holds one cassette (stack limit 1 in that slot).
- Server validates block positions in packets; menus close when the block is removed/too far or the Walkman leaves your hands.
- Tape deck persists/syncs the cassette after a write finishes.

## Known limits
- Worlds/items from the Forge version: Boombox/Tape Deck data uses the same NBT layout, but a Walkman's inserted cassette
  was stored in Forge capability data (`ForgeCaps`) and will not carry over.
- Everything is untested in-game: please test Boombox, Tape Deck (write/erase), Walkman (play/next/prev/drop/offhand), multiplayer.
