package s20753794_and_21263128;
import java.util.*;

import javax.security.auth.login.FailedLoginException;
import java.io.*;
import cits3001_2016s2.Agent;


/**
 * A Class to represent a game simulation given certain state parameters
 * 
 */

public class SimGame {

	private Map<Character, Agent> players;
	private Set<Character> spies;
	private String playerString = "";
	private String spyString = "";
	private String resString = "";
	private int numPlayers = 0;
	private static final int[] spyNum = { 2, 2, 3, 3, 3, 4 }; // spyNum[n-5] is
																// the number of
																// spies in an n
																// player game
	private static final int[][] missionNum = { { 2, 3, 2, 3, 3 }, { 2, 3, 4, 3, 4 }, { 2, 3, 3, 4, 4 },
			{ 3, 4, 4, 5, 5 }, { 3, 4, 4, 5, 5 }, { 3, 4, 4, 5, 5 } };
	// missionNum[n-5][i] is the number to send on mission i in a in an n player
	// game
	private Random rand;
	private File logFile;
	private boolean started = false;
	
	
	//game global variables
	int fails;
	int numOfMissions;
	String player;
	String stringLeader;
	String proposedMission;
	boolean customMission;
	int initialFails;

	/**
	 * Creates a simulated game given the state parameters
	 * 
	 * @param agentLeader current leader for mission nomination
	 * @param missNumber current mission number
	 * @param failures number of failures so far
	 * @param stringSpies string of spies of game
	 * @param propMission the proposed mission by agentLeader
 	 * 
	 */
	public SimGame( String agentLeader, int missNumber, int failures, String stringSpies, String propMission) {
		
		//Adding spies to hashset
		spies = new HashSet<Character>();
		for(int i =0; i< stringSpies.length(); i++){
			spies.add(stringSpies.charAt(i));
		}
		
		numOfMissions = missNumber;
		initialFails = failures;
		fails = failures;
		stringLeader = agentLeader;
		proposedMission = propMission;
		customMission = true;
		init();
	}

	

	/**
	 * Initializes the data structures for the game
	 */
	private void init() {
		players = new HashMap<Character, Agent>();
		//spies = new HashSet<Character>();
		rand = new Random();
		long seed = rand.nextLong();
		rand.setSeed(seed);
		
	}

	

	/**
	 * Adds a player to a game. Once a player is added they cannot be removed
	 * 
	 * @param a the agent to be added
	 * @param name the name of the agent player
	 */
	public void addPlayer(Agent a, String name) {
		if (numPlayers > 9)
			throw new RuntimeException("Too many players");
		else if (started)
			throw new RuntimeException("Game already underway");
		else {
			
			players.put(name.charAt(0), a);
			numPlayers++;
			
		}
	}

	/**
	 * Sets up the game and informs all players of their status. This involves
	 * assigning players as spies according to the rules.
	 */
	public void setup() {
		if (numPlayers < 5)
			throw new RuntimeException("Too few players");
		else if (started)
			throw new RuntimeException("Game already underway");
		else {
			
			for (Character c : players.keySet())
				playerString += c;
			for (Character c : spies) {
				spyString += c;
				resString += '?';
			}
			statusUpdate(1, 0);
			started = true;
			
		}
	}





	/**
	 * Sends a status update to all players. The status includes the players
	 * name, the player string, the spys (or a string of ? if the player is not
	 * a spy, the number of rounds played and the number of rounds failed)
	 * 
	 * @param round
	 *            the current round
	 * @param fails
	 *            the number of rounds failed
	 **/
	private void statusUpdate(int round, int fails) {
		for (Character c : players.keySet()) {
			if (spies.contains(c)) {
				players.get(c).get_status("" + c, playerString, spyString, round, fails);
			} else {
				players.get(c).get_status("" + c, playerString, resString, round, fails);
			}
		}
	}

	/**
	 * This method picks a random leader for the next round and has them
	 * nominate a mission team. If the leader does not pick a legitimate mission
	 * team (wrong number of agents, or agents that are not in the game) a
	 * default selection is given instead.
	 * 
	 * @param round
	 *            the round in the game the mission is for.
	 * @return a String containing the names of the agents being sent on the
	 *         mission
	 */
	private String nominate(int round, Character leader) {
		int mNum = missionNum[numPlayers - 5][round - 1];
		String team;
		
		if(customMission){
			team = proposedMission;
			customMission = false;
		}else{
			team = players.get(leader).do_Nominate(mNum);

		}
		
		char[] tA = team.toCharArray();
		Arrays.sort(tA);
		boolean legit = tA.length == mNum;
		for (int i = 0; i < mNum && legit; i++) {
			if (!players.keySet().contains(tA[i]))
				legit = false;
			if (i > 0 && tA[i] == tA[i - 1])
				legit = false;
		}
		if (!legit) {
			team = "";
			for (int i = 0; i < mNum; i++)
				team += (char) (65 + i);
		}
		for (Character c : players.keySet()) {
			players.get(c).get_ProposedMission(leader + "", team);
		}
		return team;
	}

	/**
	 * This method requests votes from all players on the most recently proposed
	 * mission teams, and reports whether a majority voted yes. It counts the
	 * votes and reports a String of all agents who voted in favour to the each
	 * agent.
	 * 
	 * @return true if a strict majority supported the mission.
	 */
	private boolean vote() {
		int votes = 0;
		String yays = "";
		for (Character c : players.keySet()) {
			if (players.get(c).do_Vote()) {
				votes++;
				yays += c;
			}
		}
		for (Character c : players.keySet()) {
			players.get(c).get_Votes(yays);
		}
		return (votes > numPlayers / 2);
	}

	/**
	 * Polls the mission team on whether they betray or not, and reports the
	 * result. First it informs all players of the team being sent on the
	 * mission. Then polls each agent who goes on the mission on whether or not
	 * they betray the mission. It reports to each agent the number of
	 * betrayals.
	 * 
	 * @param team
	 *            A string with one character for each member of the team.
	 * @return the number of agents who betray the mission.
	 */
	public int mission(String team) {
		for (Character c : players.keySet()) {
			players.get(c).get_Mission(team);
		}
		int traitors = 0;
		for (Character c : team.toCharArray()) {
			if (spies.contains(c) && players.get(c).do_Betray())
				traitors++;
		}
		for (Character c : players.keySet()) {
			players.get(c).get_Traitors(traitors);
		}
		return traitors;
	}

	/**
	 * Conducts the game play, consisting of 5 rounds, each with a series of
	 * nominations and votes, and the eventual mission. It logs the result of
	 * the game at the end. Returns the number of failed mission
	 * 
	 * @return the number of failures minus the initial state number of failures after the game finished
	 */
	public int play() {
		
		int leader= playerString.indexOf(stringLeader.charAt(0));
		for ( int round = numOfMissions; round <= 5; round++) {
			
			String team = nominate(round, playerString.charAt(leader++ % numPlayers));
			
			leader %= numPlayers;
			int voteRnd = 0;
			while (voteRnd++ < 5 && !vote())
				team = nominate(round, playerString.charAt(leader++ % numPlayers));
			int traitors = mission(team);
			if (traitors != 0 && (traitors != 1 || round != 4 || numPlayers < 7)) {
				fails++;
			} else
			statusUpdate(round + 1, fails);
			HashMap<Character, String> accusations = new HashMap<Character, String>();
			for (Character c : players.keySet()) {
				accusations.put(c, players.get(c).do_Accuse());
			}
			for (Character c : players.keySet()) {
				for (Character a : players.keySet()) {
					players.get(a).get_Accusation(c + "", accusations.get(c));
				}
			}
		}
		
		return fails-initialFails;
		
			
		
	}

	
	
}
