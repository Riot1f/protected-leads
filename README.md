# Protected Leads

**Minecraft 26.2 · Fabric · server-side**

Whoever ties the first lead to a block owns that block and its leash knot. Not the
person who tamed the animal — the person who attached the lead. If Player B ties
Player A's dog to an unclaimed fence, Player B owns it.

Built for private survival servers where somebody always decides your horse looks
better in their base.

---

## What it does

For the owner (and any operator) everything behaves exactly like vanilla. For
everybody else, on that one block:

| Attempt | Result |
|---|---|
| detach a lead / take the lead item | blocked |
| right click or punch the leash knot | blocked |
| break the block | blocked |
| attach their own animal to it | blocked |
| burn it (fire or lava) | blocked |
| blow it up | blocked |
| push or crush it with a piston | blocked |
| destroy it by any other `destroyBlock` route (withers, ravagers, mod machinery) | blocked |

Denials are silent — no chat spam. The block simply does nothing.

Explosions are **not** cancelled. Only the protected position is dropped from the
blast's block list, so a creeper next to a claimed fence still detonates and still
destroys everything else around it.

Ownership is stored per **block position**, so it works on anything a lead can be
tied to, not just fences.

## Ownership lifetime

Ownership is created **only** when a lead is really attached to an unprotected
block — not when somebody right clicks an empty fence.

It then lasts:

* across server restarts and chunk unloads
* while the owner is offline
* after the animal is removed, unloaded or killed
* **after the lead is taken off.** The block stays that player's block with no
  animal on it, and nobody else can claim it by waiting for it to be empty.

The only things that clear a claim:

* the **owner** breaking the block
* an **operator** breaking it, or running `/protectedleads remove`

## Commands

Operator only (`COMMANDS_GAMEMASTER`, i.e. the old permission level 2):

* `/protectedleads info` — is the block you are looking at protected, and by whom
* `/protectedleads remove` — clear protection from the block you are looking at

Both work on the block itself or on a leash knot attached to it.

## Installing

1. Fabric Loader on the server
2. Fabric API
3. drop the jar in `mods/`
4. start

Only the host installs it. Clients join with a normal client — the mod registers no
blocks, items, entities, menus or packets, so there is nothing for registry sync to
disagree about. Works on dedicated servers, LAN worlds and client-hosted worlds
(including Essential's world hosting).

## How it hooks in

| Concern | Hook |
|---|---|
| create ownership | mixin on `LeadItem.bindPlayerMobs` (return) — the one method that actually attaches a lead, so a claim only happens on a real attach |
| deny foreign attach | same method, head |
| knot interact / attack | Fabric `UseEntityCallback` / `AttackEntityCallback` |
| knot damage from anything else | mixin on `BlockAttachedEntity.hurtServer` |
| player block break | Fabric `PlayerBlockBreakEvents.BEFORE`, and `AFTER` to release the owner's own claim |
| explosions | mixin on `ServerExplosion.calculateExplodedPositions` |
| fire | mixin on `FireBlock.checkBurnOut` |
| pistons | mixin on `PistonStructureResolver.resolve` |
| everything else | mixin on `Level.destroyBlock` |

Every hook is a no-op for unclaimed positions, so nothing else in the world behaves
differently.

Claims live in a real per-dimension `SavedData`, written with the world save at
`<world>/dimensions/<namespace>/<dimension>/data/protectedleads/claims.dat`. Nothing
is keyed on the animal and nothing is memory-only.

## Compatibility

Fabric only. Not a Forge/NeoForge mod and not a Bukkit/Paper plugin; Quilt is
untested. Requires Fabric API and Java 25 (whatever Minecraft 26.2 already needs).

Because it hooks explosion block lists, fire burnout, piston movement and
`Level.destroyBlock`, another protection or grief-prevention mod touching the same
systems could interact with it. Test them together before running on a busy server.

## Building

The published jar is built by `build.sh`, an offline javac build against a directory
of Minecraft 26.2 + Fabric jars — see [BUILD_NOTES.md](BUILD_NOTES.md). That is the
build that produced the release.

```
./build.sh /path/to/dir-with-mc-and-fabric-jars
```

A standard Loom `build.gradle` is also included for anyone who wants to build it the
normal way:

```
./gradlew build
```

Heads up: the Gradle path has **not** been run by the author (the release was built
offline), so the Loom version in `build.gradle` may need bumping to whatever supports
26.2 in your toolchain. The source itself is the same either way.

## Testing

`TEST-LOG-server-boot.txt` is the raw log from a real dedicated-server boot: clean
startup, zero mixin injection failures, saved-data persistence verified across a
restart, and explosion filtering verified with controls (claimed block survived,
three unclaimed controls destroyed).

## License

MIT — see [LICENSE](LICENSE). Use it, fork it, learn from it. If you ship
something built on it, a link back is appreciated but not required.
