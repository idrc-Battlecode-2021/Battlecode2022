import subprocess,sys,os

def parse_winner(str, p1, p2):
    ls = str.splitlines()
    returnStr = "";
    roundNum = "";
    for line in ls[-20:]: # save some looping time
        if "wins" in line:
            returnStr = "str"
            if len(p2) > len(p1):
                if p2 in line:
                    returnStr = p2
                else:
                    returnStr = p1
            else:
                if p1 in line:
                    returnStr = p1
                else:
                    returnStr = p2
            roundNum = line.split(" ")[-1][:-1]
        elif "Reason" in line:
            return (returnStr,roundNum,line)
def main():

    ## 
    primaryPlayers = sys.argv[1:]
    #players = ['agrobot','bot1','bot2','bot3','bot3_EW','bot3_JJ','bot] # Bot names (i.e. examplefuncsplayer, should be folders in src/ directory)
    players = os.listdir('./src/')
    #players = ['dummybot']
    maps = ["colosseum", "eckleburg", "fortress", "intersection", "jellyfish", "maptestsmall", "nottestsmall", "progress", "rivers", "sandwich", "squer", "uncomfortable", "underground", "valley"] # Maps
    #maps = ["uncomfortable"]
    scaffold_directory = "./" # Battlefold Scaffold location ("./" if this file is in scaffold location)
    ##

    results = {}
    winCount = {}
    mapLosses = {}
    output = "";
    for i in range(len(primaryPlayers)):
        player = primaryPlayers[i]
        output += player + '\n'
        results[player] = {}
        if player not in winCount:
            winCount[player] = 0

        print(player + ": ")
        for j in range(1,len(primaryPlayers)):
            if (player == primaryPlayers[j]):
                continue
            
            opponent = primaryPlayers[j]
            if opponent not in winCount:
                winCount[opponent] = 0

            for map in maps:
                if map not in mapLosses:
                    mapLosses[map] = 0
                
                match_result = subprocess.check_output(["gradlew", "run",f"-PteamA={player}", f"-PpackageNameA={player}",f"-PteamB={opponent}", f"-PpackageNameB={opponent}",f"-Pmaps={map}"],cwd=scaffold_directory,shell=True).decode('UTF-8')
                winner,roundNumber,reason = parse_winner(match_result, player, opponent)
                results[player][opponent] = winner
                winCount[winner] += 1
                if winner != player:
                    mapLosses[map] += 1
                redOut = f"Red  {player} ({winCount[player]}) - {opponent} ({winCount[opponent]}) [{map}]: {winner} | Rounds: {roundNumber}" + '\n' + reason
                output += redOut +'\n'
                print(redOut)
                
                match_result = subprocess.check_output(["gradlew", "run",f"-PteamA={opponent}", f"-PpackageNameA={opponent}",f"-PteamB={player}", f"-PpackageNameB={player}",f"-Pmaps={map}"],cwd=scaffold_directory,shell=True).decode('UTF-8')

                winner,roundNumber,reason = parse_winner(match_result, player, opponent)

                results[player][opponent] = winner
                winCount[winner] += 1
                if winner != player:
                    mapLosses[map] += 1
                blueOut = f"Blue {player} ({winCount[player]}) - {opponent} ({winCount[opponent]}) [{map}]: {winner} | Rounds: {roundNumber}" + '\n' + reason
                output += blueOut + '\n' + '\n'
                print(blueOut)
                print()
        print()
        break;
    output += '\n'
    output += 'Scoreboard: ' + '\n'
    print("Scoreboard: ")
    for k,v in winCount.items():
        score = ""
        if k == primaryPlayers[0]:
            score = f"{k}: {v}/{2*(len(primaryPlayers)-1) * len(maps)}"
        else:
            score = f"{k}: {v}/{2* len(maps)}"
        output += score + '\n'
        print(score)
    print()
    print("Map Losses")
    output += '\n' + "Map Losses" + '\n'
    for map,losses in mapLosses.items():
        score = f"{map}: {losses}/{2*(len(primaryPlayers)-1)}"
        output += score + '\n'
        print(score)
    file = open("scriptMatchLogs/matchLogs"+primaryPlayers[0]+".txt", "w")
    file.write(output+'\n')
    file.close()

if __name__ == "__main__":
    main()
