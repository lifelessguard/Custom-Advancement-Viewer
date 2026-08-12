# CustomAdvancementViewer

A Paper/Spigot plugin that searches for every player who has earned a given
advancement and shows them in a double-chest GUI of player heads.

## Building

Requires Java 17+ and Maven. Paper's repository is declared in `pom.xml`:

```
mvn clean package
```

Drop the resulting `target/CustomAdvancementViewer-1.0.0.jar` into your
server's `plugins/` folder and restart.

## Usage

```
/customadvsee <advancement>
```

Accepts either a bare path (`story/mine_stone`, defaults to the `minecraft:`
namespace) or a fully namespaced key (`minecraft:story/mine_stone`,
`mypack:custom/thing` for datapack advancements). Tab completion suggests
every advancement currently registered on the server.

If anyone has earned it, a GUI opens with one player head per player. Hovering
over a head shows:

```
{player name}
{advancement name}
{time earned}
```

More than 45 matches spill onto additional pages, navigable with arrow buttons
in the bottom row. The GUI is read-only - nothing can be taken out of it.

## How player data is found

There's no built-in Bukkit API to check an *offline* player's advancement
progress, so this plugin reads each player's advancement file directly from
`<world>/advancements/<uuid>.json` (the same folder vanilla itself writes to,
sitting next to `playerdata` and `stats` in your main world's save folder).

For players who are currently online, it checks live via the Bukkit API
instead, which gives an exact awarded timestamp without waiting on a data
flush to disk. Offline players, and the only option for advancement keys that
aren't currently registered on the server (e.g. from a datapack that's since
been removed), come from the file scan.

One caveat: an online player's very latest advancement might not be reflected
in the file scan until their data next saves - but since online players are
always checked live, this doesn't cause them to be missed.

## Permissions

- `customadvsee.use` (default: op)
