# StrikesSTG

Paper plugin for Minecraft **1.21.11** that strikes players for swearing in chat, signs, and books.

**License:** [MIT](LICENSE)

## Features

- Whole-word detection only (`classic` will not match `ass`)
- **1 strike per matched word**
- Censors matched words to red `***`
- Vanilla on-screen title: `You got N strike(s)!`
- Configurable ban thresholds (`forever`, `7d`, `12h`, `30m`)
- YAML persistence (`strikes.yml`)
- Console logging for strikes and bans
- Commands and bypass permission

## Build

```bash
./gradlew build
```

Jar output: `build/libs/StrikesSTG-1.0.0.jar`

## Install

1. Put the jar in your Paper `plugins/` folder
2. Start the server once to generate `config.yml`
3. Edit banned words and ban thresholds
4. `/reload` is not recommended — restart the server after config changes

## Commands

| Command | Permission | Description |
|---------|------------|-------------|
| `/strikes` | `strikesstg.strikes` | View your strikes and reasons |
| `/strikes <player>` | `strikesstg.strikes.others` | View another player's strikes |
| `/strikes reset <player>` | `strikesstg.reset` | Clear a player's strikes |

## Permissions

| Permission | Default | Description |
|------------|---------|-------------|
| `strikesstg.strikes` | true | View own strikes |
| `strikesstg.strikes.others` | op | View others |
| `strikesstg.reset` | op | Reset strikes |
| `strikesstg.bypass` | false | Skip detection |
| `strikesstg.admin` | op | All of the above |

## Config example

```yaml
banned-words:
  - fuck
  - shit

ban-thresholds:
  - strikes: 3
    duration: 1d
  - strikes: 5
    duration: forever
```
