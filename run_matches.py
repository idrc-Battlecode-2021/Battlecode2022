import subprocess,sys,os

def parse_winner(str, p1, p2):
    ls = str.splitlines()
    for line in ls[-50:]: # save some looping time
        if "wins" in line:
            if len(p2) > len(p1):
                if p2 in line:
                    return p2
                else:
                    return p1
            else:
                if p1 in line:
                    return p1
                else:
                    return p2

def main():

    ## Fill in desired players and maps here!
    primaryPlayers = sys.argv[1:]
    #players = ['agrobot','bot1','bot2','bot3','bot3_EW','bot3_JJ','bot] # Bot names (i.e. examplefuncsplayer, should be folders in src/ directory)
    players = os.listdir('./src/')
    #maps = ["colosseum", "eckleburg", "fortress", "intersection", "jellyfish", "maptestsmall", "nottestsmall", "progress", "rivers", "sandwich", "squer", "uncomfortable", "underground", "valley"] # Maps
    maps = ["colosseum"]
    scaffold_directory = "./" # Battlefold Scaffold location ("./" if this file is in scaffold location)
    ##

    results = {}
    winCount = {}
    output = "";
    for i in range(len(primaryPlayers)):
        player = primaryPlayers[i]
        output += player + '\n'
        results[player] = {}
        if player not in winCount:
            winCount[player] = 0

        print(player + ": ")
        for j in range(len(players)):
            if (player == players[j]):
                continue
            
            opponent = players[j]
            if opponent not in winCount:
                winCount[opponent] = 0

            for map in maps:          
                match_result = subprocess.check_output(["gradle", "run",f"-PteamA={player}", f"-PpackageNameA={player}",f"-PteamB={opponent}", f"-PpackageNameB={opponent}",f"-Pmaps={map}"],cwd=scaffold_directory,shell=True).decode('UTF-8')

                winner = parse_winner(match_result, player, opponent)

                results[player][opponent] = winner
                winCount[winner] += 1
                redOut = f"Red {player} ({winCount[player]}) - {opponent} ({winCount[opponent]}) [{map}]: {winner}"
                output += redOut +'\n'
                print(redOut)
                
                match_result = subprocess.check_output(["gradle", "run",f"-PteamA={opponent}", f"-PpackageNameA={opponent}",f"-PteamB={player}", f"-PpackageNameB={player}",f"-Pmaps={map}"],cwd=scaffold_directory,shell=True).decode('UTF-8')

                winner = parse_winner(match_result, player, opponent)

                results[player][opponent] = winner
                winCount[winner] += 1
                blueOut = f"Blue {player} ({winCount[player]}) - {opponent} ({winCount[opponent]}) [{map}]: {winner}"
                output += blueOut + '\n'
                print(blueOut)
        print()
        break;
    output += '\n'
    output += 'Scoreboard: ' + '\n'
    print("Scoreboard: ")
    for k,v in winCount.items():
        score = f"{k}: {v}/{2*(len(players)-1) * len(maps)}"
        output += score + '\n'
        print(score)
    
    file = open("matchLogs.txt", "a")
    file.write(output)
    file.close()

if __name__ == "__main__":
    main()
