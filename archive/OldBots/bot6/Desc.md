# Bot 6
Uses bot 5 as a base
- Builds 6 miners, then prioritizes soldiers
- Added repairing for low health soldiers
- Archon spawns on low rubble areas
selectPriorityTargetting
miners and builders are prioritized if the turns that it takes to kill the archon>25 (and no other troops are present)
also makes soldiers move to low passability before attacking

soldiers move back to archon to heal
soldiers have better explore methods

## Shared Array
12 - location of first enemy troop
13 - extension of 12
14 - average distance between archons
When miners find a deposit
Miner Attacked
- 40: attacked (Not Used Currently)
- 41: minorLocation (Where enemy is)  (Not Used Currently)
- 42: enemy seen
- 43: Archon Locations
- 44: global miner count
  48 - used to tell friendly soldiers possible enemy archon locations
  49 - locations of archon 0-1
  50 - locations of archon 2-3