# Bot MC 2
Uses bot3 as a base

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
49 - locations of archon 0-1
50 - locations of archon 2-3