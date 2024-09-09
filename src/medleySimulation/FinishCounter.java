// Simple class to record when someone has crossed the line first and wins
package medleySimulation;

import javax.swing.*;

public class FinishCounter {
	private boolean firstAcrossLine; //flag
	private final int[] winners; //who won
	private final int[] winningTeams; //counter for patrons who have left the club
	private int count;
	private Timer stopwatch;
	
	FinishCounter() {
		firstAcrossLine= true;//no-one has won at start
		winners= new int[3];
		winningTeams = new int[3];
		for(int i = 0; i < 3; i++) {
			winners[i] = -1; //no-one has won at start
			winningTeams[i] =-1; //no-one has won at start
		}
		count = 0;
	}

	public void setStopwatch(Timer stopwatch) {
		this.stopwatch = stopwatch;
	}
		
	//This is called by a swimmer when they touch the finish line
	public synchronized void finishRace(int swimmer, int team) {
		if(firstAcrossLine) {
			firstAcrossLine=false;
			}
		if (count < 3) {
			winners[count]=swimmer;
			winningTeams[count]=team;
		}
		if (count == 9) {
			stopwatch.stopRecord();
			PodiumStand podiumStand = new PodiumStand(600, 500, winningTeams);
		}
		count++;
	}
	
	//Has race been won?
	public boolean isRaceWon() {
		return !firstAcrossLine;
	}

	public int[] getWinner() { return winners; }
	
	public int[] getWinningTeam() { return winningTeams;}


}
