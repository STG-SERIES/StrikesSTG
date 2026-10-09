# StrikesSTG

Paper plugin that strikes players for swearing in chat, signs, and books.

**License:** [MIT](LICENSE)

## Supported versions

Separate builds live under `versions/`:

| Folder | Paper API | `api-version` | Java |
|--------|-----------|---------------|------|
| `versions/1.21.11` | `1.21.11-R0.1-SNAPSHOT` | `1.21` | 21+ |
| `versions/26.1` | `26.1.1.build.29-alpha` | `26.1` | 25+ |
| `versions/26.1.2` | `26.1.2.build.74-stable` | `26.1` | 25+ |
| `versions/26.2` | `26.2.build.132-stable` | `26.2` | 25+ |
| `versions/26.3` | `26.3.build.168-beta` | `26.3` | 25+ |

Note: Paper has no bare `26.1` API artifact; that folder targets the 26.1.1 line.

## Build all

```bash
./gradlew buildAll
```

Jars:

- `versions/1.21.11/build/libs/StrikesSTG-1.1.0-1.21.11.jar`
- `versions/26.1/build/libs/StrikesSTG-1.1.0-26.1.jar`
- `versions/26.1.2/build/libs/StrikesSTG-1.1.0-26.1.2.jar`
- `versions/26.2/build/libs/StrikesSTG-1.1.0-26.2.jar`
- `versions/26.3/build/libs/StrikesSTG-1.1.0-26.3.jar`

Build one version:

```bash
./gradlew :1.21.11:build
```

## Features

- Whole-word detection only (`classic` will not match `ass`)
- **1 strike per matched word**
- Censors matched words to red `***`
- Vanilla on-screen title: `You got N strike(s)!`
- Configurable ban thresholds (`forever`, `7d`, `12h`, `30m`)
- YAML persistence (`strikes.yml`)
- Console logging for strikes and bans
- Commands and bypass permission

## Install

1. Download the jar matching your Paper version
2. Put it in `plugins/`
3. Restart and edit `config.yml`

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
