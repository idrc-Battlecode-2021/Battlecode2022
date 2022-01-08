# Second Bot

## Buildings
### Archons
#### Spawning
Tries to spawn evenly throughout all archons by informing each other of what spawn phase each one has passed
Spawn phases in order of priority:
- Miners: >=5
- - Increases # of miners spawned if Archon is near a lot of lead deposits
- Builders: 3
- 2 watchtowers for every lab until 1 lab is built
- Sages whenever there's enough gold and total # of watchtowers and soldiers >= 40
- alternate soldiers/watchtowers whenever there's enough lead
- repair nearby troops if none of the above

#### Changelog
- 1/5/2022 - decrease initial lab count 3 -> 2, added more spawn phases for more even spawning throughout all archons
- 1/8/2022
- - decrease miner count 10 -> 5
- - miner & builder counts are now differentiated by archon
- - much more even spawning for miners
- - lab count changes based on size of map
- - once a lab is built, archons build defensive watchtowers before building additional ones

### Labs
### Watchtowers
## Droids
### TODO
- Create a Better Exploration Method
- Create a Better Pathfinding algorithm
### Builders
- work on lattice stuff
#### Changelog
- 1/6/2022 - work on watchtower lattice.
### Miners
#### TODO
- Optimize resource locator
- Further Optimize Bytecode Usage
### Sages
### Soldiers
