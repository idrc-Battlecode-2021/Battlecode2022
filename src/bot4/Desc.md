# Bot 4
Uses bot_mc_2 as base

Attempt at a more aggressive strategy

Archon:
spawns miners until an ally troop has detected enemy
once the archon detected an enemy, continually spawn soldiers and sends them to 1 of 4 locations where the enemy is spotted
NO WATCHTOWERS

Soldier:
Using selectPriorityTargetting 

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
48 - used to tell friendly soldiers possible enemy archon locations
49 - locations of archon 0-1
50 - locations of archon 2-3