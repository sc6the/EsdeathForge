# EsdeathForge

A fork and revival of **EsdeathClient** — an old Minecraft client (based on 1.8.x) that was
slightly popular for a short while in the German BedWars scene before being abandoned.

EsdeathForge brings the client back to life as a Forge **1.8.9** mod and adds a large number of new
features on top of the original.

Original EsdeathClient created by **Txb1**.

## Highlights

- The original Esdeath HUD, modules, cosmetics and custom menus, rebuilt on Forge
- Account manager with token / cookie / Microsoft-refresh-token login
- In-game skin changer, force skin, custom capes and a full cosmetics system
- Custom main menu, server list, resource-pack manager and art/background gallery (with Wallhaven search)
- Themes (custom colour, button styles, snow/particle overlay) and lots of HUD/visual modules
- Built-in voice chat that connects to **LabyMod's voice chat servers**
- Bundled quality-of-life mods (perspective, smooth font, sound sliders, and more)

## Building

Requires **Java 8** (e.g. [Zulu 8](https://www.azul.com/downloads/)).

```bash
./gradlew build        # output: build/libs/EsdeathForge-1.0.jar
./gradlew installMod   # copies the jar into %APPDATA%/.minecraft/mods/1.8.9
```

## Credits

- **Txb1** — original EsdeathClient
- Bundled third-party mods belong to their respective authors

> This is a community revival/fork for educational and preservation purposes.
