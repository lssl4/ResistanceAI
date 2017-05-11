package s20753794_and_21263128;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import cits3001_2016s2.Agent;


public class StatAgent implements Agent{
	
	private String   name;
	private String   players;
	private char[]   palyers_array;
	private String   spies;
	private boolean  spy;
	private int      number_of_player;
	private int      current_round;
	private int      failed_rounds;
	private int      vote_round;				//count how many round of votes has been done for the current round of game
	private boolean  vote_for_proposed_mission;   // the decision of weather vote yes or no for the current proposed mission
	private boolean  amLeader = false;
	
	
	
	private String[] mission_players_history;
	private int[]    mission_sabotage_number_history;
	
	
	private double sabotage_failure_coeficient;  //a coefficient relating the sabotage probability with number of failed round in the game
	private double   sabotage_probability;	  // the probability of spy sabotaging in each round
	
	//probability of the history of event happening given that the corresponding entry in set_of_spies table are the true set of spies 
	//result is equivalent to the result of P(A|X)*P(X) for each entry in the set_of_spies table
	private double[] stat;					  
	
	//the actual probability of each possible set of spies in set_of_spies table being the true set of spies given all the history
	//that has been observed 
	private double[] probability;	
	private double[] player_suspicion_table;
	
	
	private int[]    set_of_spies;			  //integer representation of all possible set of spies 
	private char[][] set_of_spies_name;     //actuall combination of all possible set of spies presented by their name;
	private int[][]  combination_table;
	
	private long stopwatch = 0;
	
	private static final int[] spyNum =         {2,2,3,3,3,4}; //spyNum[n-5] is the number of spies in an n player game
	/*
	private static final int[][] missionNum = { {2,3,2,3,3},
												{2,3,4,3,4},
												{2,3,3,4,4},
												{3,4,4,5,5},
												{3,4,4,5,5},
												{3,4,4,5,5}  };  
						//missionNum[n-5][i] is the number to send on mission i in a  in an n player game
	*/
	private static final int number_of_games = 5;
	
	public StatAgent(){
	    this.current_round = 0;
		this.sabotage_probability = 1.0/2; 		//default all spy to sabotage 50% of the time whenever on a mission
		this.vote_round = 0;
		this.vote_for_proposed_mission = false;
		this.sabotage_failure_coeficient = 20/100.0;		//after experimenting, a sabotage_failure_coeficient of 0.2 is found to be optimum
		
	}
	
	/*
	 * a constructor with varied sabotag_probability, used only in the experiments to determine the optimum sabotage_probability
	 */
	public StatAgent(double p){
	    this.current_round = 0;
		this.sabotage_probability = p; 		//default all spy to sabotage 50% of the time whenever on a mission
		this.vote_round = 0;
		this.vote_for_proposed_mission = false;
		
	}
	
	/*
	 * a constructor with varied sabotage_failure_coeficient, used only in the experiments to determine the optimum sabotage_failure_coeficient
	 */
	public StatAgent(int c){
		
	    this.current_round = 0;
		this.sabotage_probability = 1.0/2; 		//default all spy to sabotage 50% of the time whenever on a mission
		
		this.sabotage_failure_coeficient = c/100.0;
		
		this.vote_round = 0;
		this.vote_for_proposed_mission = false;
		
	}
	
	
	 /** 
	   * Starts a timer for Agent method calls
	   * */
	  private void stopwatchOn(){
	    stopwatch = System.currentTimeMillis();
	  }

	  /**
	   * Checks how if timelimit exceed and if so, logs a violation against a player.
	   * @param limit the limit since stopwatch start, in milliseconds
	   * @param player the player who the violation will be recorded against.
	   * */
	  private long stopwatchOff(){
	    long delay = System.currentTimeMillis()-stopwatch;
	    return delay;
	  }
	
	
	
	
	
	// helper method to output the spies in the current mission
	private String containSpies(  String currTeam  ) {

		String s = "";

		for (int i = 0; i < this.spies.length(); i++) {

				// if the current mission team contains spies, add it to spies list
				if (currTeam.indexOf(  this.spies.charAt(i)) != -1  ) {
					s += this.spies.charAt(i);

				}

			}

			return s;
	}
	
	
	
	
	
	
	
	
	/**
	 * build a table of all possible combination of team member as spercified by number_of_players_on_mission
	 * @param a   
	 * @return		
	 */
	private String[] build_team_option_table(   int[] array , int number_of_players_on_mission){
		
		String[] option =     new String[ array.length ];
		int n = 1<< this.number_of_player;
		int a = 0;
		for(int i=1; i<= n; i++){
			option[a] = "";
			if( Integer.bitCount(i) == number_of_players_on_mission   ){
				array[a] = i;
				
				String format = "%" + (this.number_of_player)+ "s";
				String binaryStr = String.format(   format , Integer.toBinaryString(i)    ).replace(' ', '0');
				
				
				int length = binaryStr.length();
				for(int j=0; j<length; j++) {
					if( binaryStr.charAt(j)== '1' ){
						
						char name = this.palyers_array[j];
						option[a]+=name; 
						
					}
				}
				a++;
				if(a == array.length ){
					break;
				}
			}
		}
		return option;
		
	}
	
	
	
	
	/**
	 * sort player_suspicion_table while preserve the index of each entry, so that players can be 
	 * ranked according to their suspicion score
	 * @return indexes a Integer array that contains the index of players in the order of suspicion score
	 **/
	private Integer[] sort_player_suspicion_table(){
		
		Double[] array = new Double[this.player_suspicion_table.length]  ; 
		for(int i = 0; i < this.player_suspicion_table.length ; i++){
			array[i] = Double.valueOf( this.player_suspicion_table[i] )  ; 
		}
		ArrayIndexComparator comparator = new ArrayIndexComparator(array);
		Integer[] indexes = comparator.createIndexArray();
		Arrays.sort(indexes, comparator);
		
		return indexes;
		
		
	}
	
	
	
	
	private void update_palyer_suspicion_table(){
		
		Arrays.fill(player_suspicion_table, 0);
		for( int i=0; i < this.probability.length ; i++){
			for( char c : this.set_of_spies_name[i]){
				int index = this.players.indexOf(c);
				this.player_suspicion_table[index] += this.probability[i];
			}
		}
		
	}
	
	
	
	/**
	 * update the probability table once the stat table has been updated 
	 */
	private void update_probability_table(double sum){
		int chose_spies_out_of_players = this.combination_table[this.number_of_player][  spyNum[this.number_of_player-5] ];
		//double a = 0;
		for(int i =0; i <  chose_spies_out_of_players; i++		){
			this.probability[i]  =  this.stat[i]/sum;
		}

		update_palyer_suspicion_table();
	}
	
	/**
	 * update the stat_table once the information of the last completed round are obtained
	 */
	private void update_stat_table(){
		
		int    new_number_of_sabotage   = this.mission_sabotage_number_history[this.current_round]; //the newly obtained number of sabotage for the last completed round 
		String new_palyers_on_mission   = this.mission_players_history[this.current_round];   //the newly obtained list of players in the last completed round  
		
		int chose_spies_out_of_players  = this.combination_table[this.number_of_player][  spyNum[this.number_of_player-5] ];
		int number_of_spies;
		//summation for all the probability for each set of suspected spies. the result is the 
		//probability of event A happening
		double sum = 0.0; 					
		for(int i =0; i <  chose_spies_out_of_players; i++		){
			
			
			//first, check if the possible set of spies contain player itself
			//if so, then the probability of that set being the set of spies is 0
			char[] a = this.set_of_spies_name[i];
			
			if( String.valueOf( a ).indexOf( name.charAt(0) ) != -1  ){
				this.stat[i] = 0;
				continue;
			}
			else{
				number_of_spies = 0;			//a number to count the number of suspected spied according to the current set in this mission
				//second, check if the newly gained information has
				//rendered any combination of spies impossible
				//this is done by check the number of spies in the team according to each combination 
				//with the number of sabotages actually observed  
				for(char player :this.set_of_spies_name[i]  ){
					 if( new_palyers_on_mission.indexOf( String.valueOf(player) ) != -1  ){
						 number_of_spies++;
					 }
				}
				
				if( number_of_spies < new_number_of_sabotage  ){		
					//if the number of supposed spies on in the mission is less than the
					//number of sabotages, then that set of palyers being spies is zero
					this.stat[i] = 0;
					continue;
				}
				else{
					
					int c = this.combination_table[number_of_spies][new_number_of_sabotage];
					double p1 = Math.pow(   this.sabotage_probability ,         new_number_of_sabotage );
					double p2 = Math.pow( 1-this.sabotage_probability ,  number_of_spies - new_number_of_sabotage );
					this.stat[i] *= c*p1*p2;
					sum += this.stat[i];
					
				}
			}
			
		
			
		}
		
		update_probability_table(sum);
		
		//the probability of each set of palyers being spies is P(X|A) = { P(A|X) * P(X) }/P(A)
		
	}
	
	/**
	 * build a integer representation of all possible set of spies out of all players 
	 */
	private int init_set_of_spies_table(){
		int n = 1<<this.number_of_player;
		int a = 0;
		for(int i=1; i<= n; i++){
			if( Integer.bitCount(i) == spyNum[this.number_of_player-5]   ){
				this.set_of_spies[a] = i;
				
				String format = "%" + (this.number_of_player)+ "s";
				String binaryStr = String.format(   format , Integer.toBinaryString(i)    ).replace(' ', '0');
				
				
				int length = binaryStr.length();
				int b =0;
				for(int j=0; j<length; j++) {
					if( binaryStr.charAt(j)== '1' ){
						
						char name = this.palyers_array[j];
						this.set_of_spies_name[a][b] = name; 
						b++;
					}
				}
				Arrays.sort( this.set_of_spies_name[a] );
				a++;
				if(a == this.set_of_spies_name.length ){
					break;
				}
			}
		}
		return a;
	}
	
	/**
	 * build a lookup table of chosing n number out of m number
	 */
	private void build_combination_table(){
		this.combination_table = new int[this.number_of_player+1][this.number_of_player+1];
		for(int i =0; i < this.number_of_player+1; i++){
			this.combination_table[i][0] = 1;
			this.combination_table[i][i] = 1;
			for(int j = 1; j<i; j++){
				this.combination_table[i][j] = this.combination_table[i-1][j] + this.combination_table[i-1][j-1];
			}
		}	
	}
	
	
	private void agent_setup(String name, String players, String spies, int mission, int failures){
		
		this.name = name;
	    this.players = players;
	    this.palyers_array = players.toCharArray();
	    this.number_of_player = players.length();
	    this.mission_sabotage_number_history = new int[number_of_games];		//initialize the record array to store number of fails on each mission
	    this.mission_players_history = new String[number_of_games];		        //initialize the record array to store number of fails on each mission
	    
	    this.spy = spies.indexOf(name)!=-1;
	    
	    if(this.spy){
	    	this.spies = spies;
	    }
		build_combination_table();
		int chose_spies_out_of_players = this.combination_table[this.number_of_player][  spyNum[this.number_of_player-5] ];
		
		this.stat = new double[ chose_spies_out_of_players  ];
		double p = 1.0/chose_spies_out_of_players   ; //1.0 is used so that floating point arithmetic is used. since 1 and chose_spies_out_of_players  are bith integers, if 1 is used instead, integer arithmetic is used, give 0
		Arrays.fill(stat, p);							//fill the stat array with 1/chose(number of spies out of total number of player), this is the base probability P(X)
		this.probability = new double[chose_spies_out_of_players];
		this.player_suspicion_table = new double[this.number_of_player];
		this.set_of_spies = new int[chose_spies_out_of_players];
		this.set_of_spies_name = new char[chose_spies_out_of_players][ spyNum[this.number_of_player-5] ];
		
		//if not a spy, build the spies table to initialize baesian model calculation
		if(!spy){
			int a = init_set_of_spies_table();
		//check if the number of integer representation of spies sets are equal to the value of chose f out of s
			assert  a == chose_spies_out_of_players;
		}
		
		
	}
	
	
	@Override
	public void get_status(String name, String players, String spies, int mission, int failures) {
		
		assert mission==this.current_round+1;
		this.current_round = mission-1;
		this.failed_rounds = failures;
		//if this is the update for the first round, then use the info to setup initial parameters 
		if(mission ==1){
			agent_setup(name, players,spies, mission, failures);
		}
		else{
			this.sabotage_probability += this.sabotage_failure_coeficient * failures;
		}
	}
	
	@Override
	public String do_Nominate(int number) {
		
		this.amLeader = true;
		
		if(!this.spy){															//if not a spy		
			
			String team = "";
			int count = 0;
			Integer[] indexes = sort_player_suspicion_table();
			
			while( count < number     ){
				team +=  this.palyers_array[ indexes[count]  ] + "";
				count++;
			}
			return team;
		}
		//if is a spy
		else{
			stopwatchOn();
			String[] options;
			int chose_team_out_of_players = this.combination_table[this.number_of_player][  number ]    ;
			int[]   set_of_team_options   = new int[chose_team_out_of_players]                          ;
			options                       = build_team_option_table( set_of_team_options, number )      ;                                ;
			MonteCarloSearch m            = new MonteCarloSearch(   
																	0                                   ,  
																	options                             ,
																	this.mission_players_history        ,
																	this.mission_sabotage_number_history,
																	this.current_round                  ,
																	this.failed_rounds                  ,
																	this.number_of_player               ,
																	this.players                        ,
																	this.spies,
																	this.name
												                                                       );
			long time_left  = 1000 - stopwatchOff() - 100;
			m.find_best_action( time_left  );
			int index = m.get_best_action();
			return options[index];
		    
			
			
		}
		
		
	}

	@Override
	public void get_ProposedMission(String leader, String mission) {
		
		this.vote_round++;
		
		if(this.current_round == 0){											// if this is the first round of game, then always approve, since there is no information available yet
			this.vote_for_proposed_mission = true;
			return;
		}
		if( this.vote_round == 5){												//if this is the fifth round of voting, then vote yes no matter what
			this.vote_for_proposed_mission = true;
			return;
		}
		if(!this.spy){															//if not a spy										
			int n =  StatAgent.spyNum [this.number_of_player-5] ;
			Integer[] indexes = sort_player_suspicion_table();
			for(int i=0; i< n; i++){
				if(  mission.indexOf(     this.palyers_array[ indexes[i] ]   )!=-1                   ){
					this.vote_for_proposed_mission = false;
					return;
				}
			}
			
		}
		//if it is a spy
		else{
			
			// checking to see if have spy
						String haveSpiesOnMission = containSpies(mission);

						// if the proposed mission has 0 spies, vote no 
						if ( haveSpiesOnMission.length() == 0){
							this.vote_for_proposed_mission = false;

							// if nominated team has two or more spies, and it's < 7 
							// players game and it's not the 4th mission	
						} else if(haveSpiesOnMission.length() >= 2 && this.current_round+1 != 4 && players.length() < 7){
							
							
							this.vote_for_proposed_mission = false;
							
							// if nominated team has two or more spies, and it's a 7 or more
							// players game and it's the 4th mission	
						}else if (haveSpiesOnMission.length() >= 2 && this.current_round+1 == 4 && players.length() >= 7) {
							
							this.vote_for_proposed_mission = true;

							// vote for teams with more than one spies later on in the game
						} else if ( this.current_round+1  >= 4 && haveSpiesOnMission.length() >= 1) {
							
							this.vote_for_proposed_mission = true;
							
						//vote for teams with exactly one spy regardless the team size	
						} else if (haveSpiesOnMission.length() == 1) {
							
							this.vote_for_proposed_mission = true;
						}
						else{
							this.vote_for_proposed_mission = false;
						}
			
						return;
			
		}
		
		
		
	}

	@Override
	public boolean do_Vote() {
		
		return this.vote_for_proposed_mission;
	
	}

	@Override
	public void get_Votes(String yays) {
		
	}

	/**
	 * get the information on who is on the current mission, 
	 * and store it in the mission_players_history array
	 */
	public void get_Mission(String mission) {
		
		//if if function is called from the game class, it means that the voting has beeing done, so 
		//reset the vote_round count to 0
		this.vote_round = 0;
		this.mission_players_history[this.current_round] = mission;
	
	}

	@Override
	public boolean do_Betray() {
		//if not a spy
		if(!this.spy){
			
			return false;
			
		}
		//if it is a spy
		else{
			
			String currTeam = this.mission_players_history[this.current_round];
			String s = containSpies(  currTeam  );
			boolean answer;
			
			//if there's two failures already, fail the mission		
			if ( this.failed_rounds >= 2) {
				answer = true;

				// last missions, sabotage it
			} else if ( this.current_round+1 >= 4) {
				answer = true;
				
			//if the mission team has greater 3 players and I'm the leader, do betray	
			} else if (  currTeam.length() >= 3 &&  this.amLeader ) {
				answer = true;
				
				
			} else if (  this.current_round+1  == 4 && players.length() >= 7) {
				answer = true;
				// if there's 2 failed missions already, just sabatage it
			} else if (currTeam.length() >= 3 && s.length() <= 1 &&  this.current_round+1 >= 2 ) {

				answer = true;
			}
			else{
				answer = false;
			}
			
			this.amLeader = false;
			
			return answer;
			
		}
		
	}

	
	/**
	 * after getting the number of traitor on the last completed round, this function will call update_stat_table function to use the newly obtained information to update the probability 
	 */
	public void get_Traitors(int traitors) {
		
		this.mission_sabotage_number_history[this.current_round] = traitors;		//store the number of sabotages in the current mission
		
		if(  !this.spy   ){						//if not a spy, update the stat table for baesian's model	
			update_stat_table();
		}
	}

	@Override
	public String do_Accuse() {
		if(!spy){
			Integer[] indexes = sort_player_suspicion_table();
			String s = "";
			for(int i=0; i<StatAgent.spyNum[this.number_of_player -5] ; i++){
				s+= this.palyers_array[   indexes[  indexes.length-i-1     ]    ];
					
			}
			return s;
		}
		return "";
	}

	@Override
	public void get_Accusation(String accuser, String accused) {
		
	}
	
	
	//******************************************************************************************************************************************************
	public class ArrayIndexComparator implements Comparator<Integer>
	{
	    private final Double[] array;
	    public ArrayIndexComparator(Double[] array)
	    { this.array = array;       		      }

	    public Integer[] createIndexArray()
	    {
	        Integer[] indexes = new Integer[array.length];
	        for (int i = 0; i < array.length; i++)
	        {
	            indexes[i] = i; // Autoboxing
	        }
	        return indexes;
	    }
	    
	    public int compare(Integer index1, Integer index2)
	    {
	         // Autounbox from Integer to int to use as array indexes
	        return array[index1].compareTo(array[index2]);
	    }
	}
	//********************************************************************************************************************************************************
	

}
