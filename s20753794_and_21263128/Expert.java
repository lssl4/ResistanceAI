package s20753794_and_21263128;

import java.text.DecimalFormat;
import java.util.*;
import cits3001_2016s2.Agent;

public class Expert implements Agent {

	// A string consisting of a single letter, the (this) agent's name
	private String name;

	// Random class
	private Random rand;

	// Indicates if I am leader to propose mission
	private boolean amLeader;

	// List of all players
	private String players;

	// Indicates if agent is a spy
	private boolean spy;

	// List of spies or question marks
	private String spiesResult;

	// Mission number
	private int currMission;

	// Number of failures
	private int failures;

	// Current or proposed mission team
	private String currTeam;

	// Voting attempts
	private int voteAttempts;

	// Agents who accepted the mission
	private String approvedMission;

	// ArrayList Table of suspicion scores for each player
	private ArrayList<PlayerSuspicions> suspicionTable;
	



	/*
	 * A class that calculates the average values 
	 */
	private class AveragedValues {
		private int count;
		private double sum;
		private double average;

		private AveragedValues(double value) {
			count = 1;
			sum = value;
			average = sum / count;
		}

		private double addValue(double value) {
			count++;
			sum += value;
			average = sum / count;
			return average;
		}

		private double getAverage() {
			average = sum / count;

			return average;
		}

	}

	/*
	 *  A class of AveragedValues suspicion score for each opponent
	 */
	private class PlayerSuspicions implements Comparable<PlayerSuspicions> {

		AveragedValues suspicion;
		char player;

		private PlayerSuspicions(char oppo, AveragedValues score) {

			suspicion = score;
			player = oppo;

		}

		public int compareTo(PlayerSuspicions o) {
			return Double.compare(this.suspicion.average, o.suspicion.average);
		}

	}

	/*
	 * Helper method to output the spies in the current mission
	 * 
	 * @return the string of spies in the proposed mission, currTeam
	 */
	private String containSpies() {

		String spies = "";

		for (int i = 0; i < spiesResult.length(); i++) {

			// if the current mission team contains spies, add it to spies list
			if (currTeam.indexOf(spiesResult.charAt(i)) != -1) {
				spies += spiesResult.charAt(i);

			}

		}

		return spies;
	}

	/*
	 * Creates an instance of Expert
	 */
	public Expert(){
		init();
	}
	
	
	/*
	 * Creates a simulated instance of Expert given the state parameters.
	 * 
	 * @param simName name of the agent
	 * @param simPlayers the string of all players
	 * @param simSpy if the agent is a spy or not
	 * @param simSpiesResults the string of spies in the game
	 * @param simFailures number of failures
	 * 
	 */
	public Expert(String simName, String simPlayers, boolean simSpy, String simSpiesResults, int simFailures ){
		
		this.name = simName;
		this.players = simPlayers;
		this.spy = simSpy;
		this.spiesResult = simSpiesResults;
		this.failures = simFailures;
		
		init();
	}
	
	
	/*
	 * Initializes the leftover instance variables
	 */
	private void init(){
		
		rand = new Random();
		amLeader = false;
		
		suspicionTable = new ArrayList<>();
		
	}
	
	
	
	
	/**
	 * Reports the current status, inlcuding players name, the name of all
	 * players, the names of the spies (if known), the mission number and the
	 * number of failed missions
	 * 
	 * @param name
	 *            a string consisting of a single letter, the agent's names.
	 * @param players
	 *            a string consisting of one letter for everyone in the game.
	 * @param spies
	 *            a String consisting of the latter name of each spy, if the
	 *            agent is a spy, or n questions marks where n is the number of
	 *            spies allocated; this should be sufficient for the agent to
	 *            determine if they are a spy or not.
	 * @param mission
	 *            the next mission to be launched
	 * @param failures
	 *            the number of failed missions
	 * @return within 100ms
	 */
	public void get_status(String name, String players, String spies, int mission, int failures) {
		this.name = name;
		this.players = players;
		this.spy = spies.indexOf(name) != -1;
		this.spiesResult = spies;
		this.currMission = mission;
		this.failures = failures;

		// initializes suspicionTable ArrayList if haven't
		if (suspicionTable.isEmpty()) {
			for (int i = 0; i < players.length(); i++) {

				suspicionTable.add(new PlayerSuspicions(players.charAt(i), new AveragedValues(0.0)));

			}

		}

	

	}

	/**
	 * Nominates a group of agents to go on a mission. If the String does not
	 * correspond to a legitimate mission (<i>number</i> of distinct agents, in
	 * a String), a default nomination of the first <i>number</i> agents (in
	 * alphabetical order) will be reported, as if this was what the agent
	 * nominated.
	 * 
	 * @param number
	 *            the number of agents to be sent on the mission
	 * @return a String containing the names of all the agents in a mission,
	 *         within 1sec
	 */
	public String do_Nominate(int number) {
		// sorting arraylist before accessing it
		Collections.sort(suspicionTable);

		// always nominate yourself
		String nomination = name;

		// since I am nominating, change amLeader to true
		amLeader = true;

		if (spy) {

			// nominate other spies if it is mission 4 and >= 7 players playing
			// because the mission needs two spies to fail the mission
			if (currMission == 4 && players.length() >= 7) {

				// array of spies without me
				StringBuilder sb = new StringBuilder(spiesResult);
				sb.deleteCharAt(spiesResult.indexOf(name));

				// pick random spy

				nomination += Character.toString(sb.charAt(rand.nextInt(sb.length())));

				// if require more agents to go on mission, choose the rest as
				// resistance members randomly
				while (nomination.length() != number) {

					int j = rand.nextInt(players.length());

					// if it is not a spy and it is not me, add the member to
					// the nomination string
					if (spiesResult.indexOf(players.charAt(j)) == -1 && name.indexOf(players.charAt(j)) == -1) {
						nomination += players.charAt(j);
					}

				}

				// choose non spies randomly
			} else {

				while (nomination.length() != number) {

					int j = rand.nextInt(players.length());

					// if it is not a spy and it is not me, add the member to
					// the nomination string
					if (spiesResult.indexOf(players.charAt(j)) == -1 && name.indexOf(players.charAt(j)) == -1) {
						nomination += players.charAt(j);
					}

				}

			}

			// as a resistance member, nominate the least suspicious ones
		} else {

			int k = 0;
			while (nomination.length() != number && k < suspicionTable.size()) {

				if (Character.toString(suspicionTable.get(k).player) != name) {
					nomination += suspicionTable.get(k).player;
				}

				k++;
			}

		}

		return nomination;

	}

	/**
	 * Provides information of a given mission.
	 * 
	 * @param leader
	 *            the leader who proposed the mission
	 * @param mission
	 *            a String containing the names of all the agents in the mission
	 *            within 1sec
	 **/
	public void get_ProposedMission(String leader, String mission) {
		currTeam = mission;
		
		//changing the leader status
		if (leader.equals(name)) {
			amLeader = true;
		}else{
			amLeader = false;
		}


	};

	/**
	 * Gets an agents vote on the last reported mission
	 * 
	 * @return true, if the agent votes for the mission, false, if they vote
	 *         against it, within 1 sec
	 */
	public boolean do_Vote() {
		boolean answer = true;

		// increment voting attempts
		voteAttempts++;

		// if it's the first mission, always vote yes to accept
		if (currMission == 1) {
			return true;
		}

		// sorting arraylist
		Collections.sort(suspicionTable);


		if (spy) {

			// checking to see if have spy
			String haveSpiesOnMission = containSpies();

			// if the proposed mission has 0 spies, vote no 
			if ( haveSpiesOnMission.length() == 0){
				answer = false;

				// if nominated team has two or more spies, and it's < 7 
				// players game and it's not the 4th mission	
			} else if(haveSpiesOnMission.length() >= 2 && currMission != 4 && players.length() < 7){
				
				
				answer = false;
				// if nominated team has two or more spies, and it's a 7 or more
				// players game and it's the 4th mission	
			}else if (haveSpiesOnMission.length() >= 2 && currMission == 4 && players.length() >= 7) {
				answer = true;

				// vote for teams with more than one spies later on in the game
			} else if (currMission >= 4 && haveSpiesOnMission.length() >= 1) {
				answer = true;
				
			//vote for teams with exactly one spy regardless the team size	
			} else if (haveSpiesOnMission.length() == 1) {
				answer = true;
			}

		} else {

			for (int i = 0; i < currTeam.length(); i++) {
				for (int j = 0; j < suspicionTable.size(); j++) {
					PlayerSuspicions eachPlayer = suspicionTable.get(j);

					// when currTeam member is found in suspicionTable and has a high suspicious score, vote no
					if (eachPlayer.player == currTeam.charAt(i) && eachPlayer.suspicion.getAverage() >= 0.70) {
						answer = false;
					}
				}
			}

			// if it's the last voting attempt, accept the mission as a resistance player
			if (voteAttempts == 5) {
				answer = true;
			}

		}

		// if I am a leader, always vote yes for the mission
		if (amLeader) {
			answer = true;
		}

		

		return answer;

	};

	/**
	 * Reports the votes for the previous mission
	 * 
	 * @param yays
	 *            the names of the agents who voted for the mission
	 * @return within 100ms
	 **/
	public void get_Votes(String yays) {

		approvedMission = yays;

	}

	/**
	 * Reports the agents being sent on a mission. Should be able to be inferred
	 * from tell_ProposedMission and tell_Votes, but included for completeness.
	 * 
	 * @param mission
	 *            the Agents being sent on a mission
	 * @return within 100ms
	 **/
	public void get_Mission(String mission) {
		currTeam = mission;

		// reset voting attempts since mission is being conducted
		voteAttempts = 0;

	}

	/**
	 * Agent chooses to betray or not.
	 * 
	 * @return true if agent betrays, false otherwise, within 1 sec
	 **/
	public boolean do_Betray() {
		boolean answer = false;

		// sorting arraylist
		Collections.sort(suspicionTable);

		// if I'm a spy, I want to betray when currTeam >=3, mission number rounds left, if i'm the only spy
		if (spy) {
			String s = containSpies();

			//if there's two failures already, fail the mission		
			if ( failures >= 2) {
				answer = true;

				// last missions, sabotage it
			} else if (currMission >= 4) {
				answer = true;
				
			//if the mission team has greater 3 players and I'm the leader, do betray	
			} else if (currTeam.length() >= 3 && amLeader) {
				answer = true;
				
				
			} else if (currMission == 4 && players.length() >= 7) {
				answer = true;
				// if there's 2 failed missions already, just sabotage it
			} else if (currTeam.length() >= 3 && s.length() <= 1 && currMission >= 2) {

				answer = true;
			}
			
			

		}

		return answer;
	}

	/**
	 * Reports the number of people who betrayed the mission
	 * 
	 * @param traitors
	 *            the number of people on the mission who chose to betray (0 for
	 *            success, greater than 0 for failure)
	 * @return within 100ms
	 **/
	public void get_Traitors(int traitors) {

		int playersGreaterThanThreshold = 0;

		// sorting arraylist
		Collections.sort(suspicionTable);


		if (traitors > 0) {

			// increase the currTeam mission members suspicion scores if mission
			// failed
			for (int i = 0; i < currTeam.length(); i++) {

				// if it is me, continue
				if ((Character.toString(currTeam.charAt(i)).equals(name))) {
					continue;
				}

				for (int j = 0; j < suspicionTable.size(); j++) {

					// when it finds the player in the suspicionTable, increase
					// the suspicion score except mine
					if (currTeam.charAt(i) == suspicionTable.get(j).player) {

						// if it turns out that people on the mission with
						// suspicion score>= 0.8, increment the number of people
						// with that amount of score
						if (suspicionTable.get(j).suspicion.getAverage() >= 0.7) {
							playersGreaterThanThreshold++;
						}

						// increase each mission player suspicion score except
						// mine
						for (int k = 0; k < traitors; k++)
							suspicionTable.get(j).suspicion.addValue(1.0);
					}

				}

			}

			// since the number of players in the mission has greater then 0.7
			// as
			// their suspicion scores, increase the voters' suspicion score
			if (playersGreaterThanThreshold > 0) {
				// increase the voters suspicion score if there are mission
				// players have
				// suspicion scores greater than the threshold of 0.8
				for (int i = 0; i < approvedMission.length(); i++) {

					// if it is me, continue
					if ((Character.toString(approvedMission.charAt(i)).equals(name)))
						continue;

					for (int j = 0; j < suspicionTable.size(); j++) {

						// when it finds the voters in the suspicionTable,
						// increase the suspicion score
						if (approvedMission.charAt(i) == suspicionTable.get(j).player) {

							suspicionTable.get(j).suspicion.addValue(
									suspicionTable.get(j).suspicion.getAverage() + 0.2 * playersGreaterThanThreshold);
						}

					}

				}
			}

		}





	}

	/**
	 * Optional method to accuse other Agents of being spies. Default action
	 * should return the empty String. Convention suggests that this method only
	 * return a non-empty string when the accuser is sure that the accused is a
	 * spy. Of course convention can be ignored.
	 * 
	 * @return a string containing the name of each accused agent, within 1 sec
	 */
	public String do_Accuse() {
		return "";
	}

	/**
	 * Optional method to process an accusation.
	 * 
	 * @param accuser
	 *            the name of the agent making the accusation.
	 * @param accused
	 *            the names of the Agents being Accused, concatenated in a
	 *            String.
	 * @return within 100ms
	 */
	public void get_Accusation(String accuser, String accused) {

	}

}