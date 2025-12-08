# Cobblemon Battle UI

A client-side Fabric mod that adds helpful information displays to Cobblemon battles.

## What does it do?

During Cobblemon battles, this mod shows you information that's normally hard to track:

### Battle Info Panel

A small panel on your screen that displays:

- **Weather & Terrain** - Shows active weather (rain, sun, sandstorm, etc.) and terrain effects with remaining turn counters
- **Field Effects** - Trick Room, Gravity, and other field-wide conditions
- **Your Side's Effects** - Screens (Reflect, Light Screen), hazards on your side, Tailwind, etc.
- **Enemy's Effects** - Same as above, but for the opponent
- **Stat Changes** - Shows every Pokemon's stat boosts and drops with easy-to-read arrows

The panel shows turn ranges like "5-8" when we don't know if the opponent has items that extend duration (like Light Clay for screens or weather rocks).

### Team Pokeballs

Small pokeball indicators below each team's health bars showing:
- How many Pokemon each side has (opponent's team is revealed as they send them out)
- Which Pokemon have status conditions (colored pokeballs)
- Which Pokemon have fainted (gray pokeballs)

## Controls

- **V or Click header** - Show/hide the detailed panel
- **Drag the header** - Move the panel anywhere on screen
- **CTRL + Scroll wheel** - Scroll up to increase font size, scroll down to decrease it
- **Drag sides or corners** - Resize panel

Your preferences are saved automatically.

## Requirements

- Minecraft 1.21.1
- Fabric Loader 0.16.0+
- Fabric API
- Fabric Language Kotlin
- Cobblemon 1.7.0+

## Notes

- This is a **client-side only** mod - it works in singleplayer and on servers without the server needing it
- The opponent's full team isn't shown until they send out each Pokemon. This is intended.
- Weather/terrain/screen durations show ranges because we can't know if the opponent has duration-extending items

## License

MIT
