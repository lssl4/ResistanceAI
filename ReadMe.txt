This project is completed by Tzi Siong Leong (20753794) and Yiyang Gao (21263128)

Please run these source files which are the actual bots: 

StatAgent.java
Expert.java

resistanceAI

AI platform for the card game resistance.

Description

This projects aims to provide a set of java classes and interface to facilliate agents playing Don Eskridge's card game: Resistance.

#Rules

##Rules - resistance Note these rules have been adjusted for AI bots. Player communication is deliberately limitted and mission leaders are chosen randomly. For normal rules, see:wikipedia

Resistance is a multiplayer game, requiring at least 5 players. One third of the players are selected to be government spies, and the remaining players are memebers of the resistance. The spies know who all the other spies are, but the resistance members are unable to distinguish the spies.

The game play consists of 5 rounds. In each round, a leader is randomly selected. That leader then proposes a group to be sent on a mission. The size of the group depends on the numbers of players and and the round. All players vote on the group. If players vote to accept the group, the players are sent on a mission. If players vote to reject a group, a new leader is randomly selected and the process repeats. If five groups are rejected in a row, the mission fails. When a group of players is selected for a mission, if one person betrays the group the mission will fail, otherwise it will succeed. Only spies can betray the group, but they may choose not to. The mission itself simply involves the players on the mission choosing whether or not to betry the group. This is done privately, and the only public information released is how many people betrayed the group.

If at least 3 missions succeed, the resistance wins. Otherwise the government wins.

##Rules - AI bots

AI bots must implement the provided agent interface, and we will add restrictions for the amount of computation, and system resources they can use.
As mentioned in the agent interface, get methods are limited to 0.1 second of compute time, do methods are limited to 1 second of compute time.
Agents may not use threading to access any additional computational resources.
Agents may not read or write from the file system. Agents should not print to the screen either.
Agents should have a parameterless constructor
Java classes should have unique names. Please append your student number to the end of each class name to avoid clashes.
The tournament will will be a selection of random games. Game.java has a sample tournament file.
Agents will be randomly selected to play in games of between 5 and 10 players. There may be two or more instances of the one agent in a game.
There will be a large number of plays to give all agents a chance to play both sides.
Agents will be ranked on the percentage of games they win, regardless of whetehr they are a spy or a resistance member.
Any attempt to cheat will result in immediate disqualification.
Any Agent who crashes, or gets stuck in an infinite loop will be removed from the tournament.
Agents who go over time, will be penalised or removed from the competition.
