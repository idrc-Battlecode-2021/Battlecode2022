# Agro Bot

## Buildings
### Archons
#### Spawning
Implements FirstBot's spawning methods with Soldiers
Spawn phases in order of priority:
- Miners: >=10
- - Increases # of miners spawned if Archon is near a lot of lead deposits
- Builders: >=3
- Soldiers >=4
- 2 watchtowers for every 1 lab until 3 labs are built (idt this works for this bot)
- Sages whenever there's enough gold
- watchtowers whenever there's enough lead
- repair nearby troops if none of the above

#### Changelog
### Labs
- Check functionality (might not be converting)
### Watchtowers
- Can now travel to locations with enemy soldiers to attack
## Droids
### TODO
- Create a Better Exploration Method
- Create a Better Pathfinding algorithm
### Builders
- does not use a lattice, relies on watchtowers for offense
#### Changelog
- 1/6/2022 - work on watchtower lattice.
### Miners
### Sages
- Add Abyss anomaly when near enemy Archon
### Soldiers
- Scout area at beginning of map
- Call other soldiers/watchtowers for backup when there are lots of troops
