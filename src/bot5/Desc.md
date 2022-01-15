# Bot 5
Uses bot 4 as a base

Added defense method back to archons
spawns miners in 2 to 1 ratio to soldiers, changes to 1 to 1 once enemy has been sighted

selectPriorityTargetting
miners and builders are prioritized if the turns that it takes to kill the archon>25 (and no other troops are present)

## Shared Array
12 - location of first enemy troop
13 - extension of 12
14 - average distance between archons
When miners find a deposit
- 31: minerFound 1
- 32: minerFound 2
- 33: minerFound 3
- 34: minerFound 4
Miner Attacked
- 40: attacked (Not Used Currently)
- 41: minorLocation (Where enemy is)  (Not Used Currently)
- 42: enemy seen
- 44: global miner count
48 - used to tell friendly soldiers possible enemy archon locations
49 - locations of archon 0-1
50 - locations of archon 2-3