// Simple class to record when someone has crossed the line first and wins
package medleySimulation;

import javax.swing.*;

public class FinishCounter {
	private boolean firstAcrossLine; //flag
	private final int[] winners; //who won
	private final int[] winningTeams; //counter for patrons who have left the club
	private int count;
//	private JFrame frame;
	
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
		
	//This is called by a swimmer when they touch the finish line
	public synchronized void finishRace(int swimmer, int team) {
		//boolean won =false;
		if(firstAcrossLine) {
			firstAcrossLine=false;
			//won = true;
			}
		if (count < 3) {
			winners[count]=swimmer;
			winningTeams[count]=team;
		}
		if (count == 9) {
			PodiumStand podiumStand = new PodiumStand(600, 500, winningTeams);
//            try {
//                wait(1000);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//            frame.setVisible(false);
		}
		count++;
	}

//	public void initFrameRef(JFrame jframe) {
//		this.frame = jframe;
//	}
	
	//Has race been won?
	public boolean isRaceWon() {
		return !firstAcrossLine;
	}

	public int[] getWinner() { return winners; }
	
	public int[] getWinningTeam() { return winningTeams;}


}
