package s20753794_and_21263128;
import java.util.*;
import cits3001_2016s2.Agent;



public class MonteCarloSearch {
	
	private Random rand;
	private int action_type;			    //a integer representing what type of action is to be investigated , and are also stored in the options arrays are,
										    //0 represent team nomination, 
										    //1 represent vote yes or no for the proposed team, 
										    //2 represents sabotage or not on the current mission
	private String[]  options;			    // a string array storing all the current available action that can be performed
	private int[]    score;			        // a array to keep track of the score for each option in the options array 
	private int[]    number_of_simulation;  // a array to keep track of the number of simulation each option has been run
	
	private int best_action;			             //index of the best action to take according to the options arrays
	private String[] mission_players_history;        // a string array containing the history of the real game
	private int[] mission_sabotage_number_history;   // a integer array containing the number of sabotages of each round observed in the real game
	private int current_round;						//the current round of game in the real game
	private int failed_round;						//the number of sabotaged round in real game
	private int number_of_players;			
	private String players;							
	private String spies;			
	private String resistances;						//a string representation of resistances members
	private String name;
	private static final int[] spyNum =         {2,2,3,3,3,4}; //spyNum[n-5] is the number of spies in an n player game
	private static final int[][] missionNum = { {2,3,2,3,3},
												{2,3,4,3,4},
												{2,3,3,4,4},
												{3,4,4,5,5},
												{3,4,4,5,5},
												{3,4,4,5,5}  };  
	
	
	private long stopwatch = 0;

	
	public MonteCarloSearch(        int action_type  , 
									String[] options , 
									String[] mission_players_history,  
									int[] mission_sabotage_number_history, 
									int current_round,
									int failed_round,
									int number_of_players,
									String players,
									String spies,
									String name
									                                        ){
		
		this.action_type = action_type;
		this.options = options;
		
		this.score = new int[this.options.length];
		this.number_of_simulation = new int[this.options.length];
		
		
		this.best_action = -1;			//default value of best_action
		this.mission_players_history = mission_players_history;
		this.mission_sabotage_number_history = mission_sabotage_number_history;
		this.current_round = current_round;
		this.failed_round = failed_round;
		this.number_of_players = number_of_players;
		this.players = players;
		this.spies = spies;
		this.resistances = "";
		this.name = name;
		for(char a : players.toCharArray()){
			if( spies.indexOf(a) ==-1   ){
				resistances += a;
			}
		}
		
		
		this.rand = new Random();
	    long seed = rand.nextLong();
	    rand.setSeed(seed);
		
	}
	
	
	/** 
	   * Starts a timer for Agent method calls
	   * */
	  private void stopwatchOn(){
	    stopwatch = System.currentTimeMillis();
	  }

	  /**
	   * Checks how if time limit exceed and if so, logs a violation against a player.
	   * @param limit the limit since stopwatch start, in milliseconds
	   * @param player the player who the violation will be recorded against.
	   * */
	  private long stopwatchOff(){
	    long delay = System.currentTimeMillis()-stopwatch;
	    return delay;
	  }
	
	/**
	 * a any time function that keeps running simulation for each available while the time spent has not exceeded 
	 * time_available 
	 * @param time_available   the time allowed for this function to execute. 
	 * 
	 */
	
	public void find_best_action( long time_available  ){
		//if the action is to find a best team nomination
		if(this.action_type == 0){
			
			stopwatchOn();
			while(stopwatchOff() < time_available   ){				// keeps checking if the time elapsed dones not exceed time allowed
				
				for(int j=0; j< this.options.length; j++){			//run one simulation for each option in the option array
					if(stopwatchOff() > time_available){			//break if the time is exceeded
						break;
					}
					
						this.number_of_simulation[j]++;				//increment the number of simulation that have been run for this option
						
						SimGame g = new SimGame(    				//creat the SimGame for game simulation
								
								this.name,
								this.current_round+1,
								this.failed_round,
								this.spies,
								this.options[j]
						);
						
						//create all the resistance agent required to simulate the game
						int number_of_resistence = this.number_of_players - MonteCarloSearch.spyNum[this.number_of_players - 5];
						
						assert number_of_resistence == this.resistances.length();
						
						ResistenceSimulationAgent[] r = new ResistenceSimulationAgent[  number_of_resistence    ];
						for(int i=0; i< number_of_resistence; i++){
						
							int randomNum = rand.nextInt((35 - 17) + 1) + 17;
							//creat the simulation bot, pass all the history observed up to now
							r[i] = new ResistenceSimulationAgent(	
																	this.mission_players_history,
																	this.mission_sabotage_number_history,
																	this.current_round,
																	this.failed_round,
																	this.number_of_players  ,
																	10.0/randomNum          ,
																	this.resistances.charAt(i),
																	this.players 
																 );
							
							g.addPlayer(r[i], this.resistances.charAt(i)+"");
							
							
						}
					    //create all the spies agent required to simulate the game
						int number_of_spies         = MonteCarloSearch.spyNum[this.number_of_players - 5];
						Expert[]      s = new Expert[number_of_spies];
						for(int i=0; i< number_of_spies; i++){
							
							s[i] = new Expert(     
																	this.spies.charAt(i)+"",
																	this.players,
																	true,
																	this.spies,
																
																	this.failed_round
													);
							
							g.addPlayer(s[i], this.spies.charAt(i)+"");
							
						}
						
						g.setup();
						int f = g.play();					//g.play returns the number of sabotages the simulated game have seen
					    this.score[j] += f;					//add up the number of sabotaged round, this is a indication of how good this option is for spies  
					    
					
					
					
				}
			
			}
		
		}
		
	}
	
	/**
	 * sort the score array for each option, and return the option with the highest score
	 * @return the index of best option according to the option array
	 */
	public int get_best_action(){
		
		
		Integer[] new_score = new Integer[  this.score.length];
		int i = 0;
		for (int value :  this.score ) {
			new_score[i++] = Integer.valueOf(value);
		}
		ArrayIndexComparator comparator = new ArrayIndexComparator( new_score );
		Integer[] indexes = comparator.createIndexArray();
		Arrays.sort(indexes, comparator);
		
		
		
		return indexes[ indexes.length -1  ];
		
	}
	
	
	
	//******************************************************************************************************************************************************
		public class ArrayIndexComparator implements Comparator<Integer>
		{
		    private final Integer[] array;
		    public ArrayIndexComparator(   Integer[] array   )
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
