# Bot10

- copied from bot9_jj, experimenting with sages

## Changelog

### General
- troops don't avoid charge
- builder and lab count are now global

### Archons:
- first builds 3 miners
- builds a builder
- builds a lab
- builds a sage whenever possible
- build miners and labs once past a certain lead threshold, and the threshold should increase after every build (in progress)
### Labs:
- always try to generate gold per turn

### Sages:
- soldier code but updated action/vision radii
- different targetting that accounts for mass charge damage
- retreats if can sense bots in vision but action cooldown isn't up


- 31: Soldier Healing
- 32: Solider Healing
- 33: Soldier Healing
- 34: Soldier Healing
  When miners find a de 41: minorLocation (Where enemy is)  (Not Used Currently)
- 42: enemy seen
- 43: Archon Locations
- 44: global miner count
- 45: return to archon
- 46: soldier flag
- 47: soldier move central
  48 - used to tell friendly soldiers possible enemy archon locations
  15 - location of archon 0
  16 - location of archon 1
  49 - locations of archon 2
  50 - locations of archon 3