# Third Bot Variant (JJ4)
- When 1 miner finds a deposit, another is built
- Heavily Priorities Soldiers
- Labs only if a stupid amount of lead is avaliable
- Watchtowers are always offensive
- Doubles builder count if there is a good surplus of lead
- Hardcoded Miner Limit
- Soldiers close to other units until at least one enemy location is known
- Builders always at least 2 block away from archon
- Miners change lead position if lead is <1

# IDs
- Archon ID's are all +1

#Shared Array

When miners find a deposit
- 31: minerFound 1
- 32: minerFound 2
- 33: minerFound 3
- 34: minerFound 4
- 36: minerAlert (WIP)
- 37: minerAlert (WIP)
- 38: minerAlert (WIP)
- 39: minerAlert (WIP)
- 40: attacked
- 41: minorLocation (Where enemy is)  
- 42: soldierBuild Start
- 43: Archon Location  
- 44: GlobalMinerCount  
- All Other ids the same

#Bugs
- Miners get stuck in corners
- Sometimes Miners don't go to lead deposits
- one archon gets most of the soldiers