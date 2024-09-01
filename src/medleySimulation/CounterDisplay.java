//M. M. Kuttel 2024 mkuttel@gmail.com
// Simple Thread class to update the display of a text field
package medleySimulation;

import java.awt.Color;

import javax.swing.JLabel;

//You don't need to change this class
public class CounterDisplay  implements Runnable {
	
	private FinishCounter results;
	private JLabel win;
		
	CounterDisplay(JLabel w, FinishCounter score) {
        this.win=w;
        this.results=score;
    }
	
	public void run() { //this thread just updates the display of a text field
        while (true) {
        	//test changes colour when the race is won
        	if (results.isRaceWon()) {
        		win.setForeground(Color.RED);
				String first = "1st: (Team " + results.getWinningTeam()[0] + ")";
				String second = "";
				String third = "";
				if (results.getWinningTeam()[1] != -1) {
					second = "  2nd: (Team " + results.getWinningTeam()[1] + ")";
				}
				if (results.getWinningTeam()[2] != -1) {
					third = "  3rd: (Team " + results.getWinningTeam()[2] + ")";
				}
               	win.setText(first + second + third + "!!");
        	}
        	else {
        		win.setForeground(Color.BLACK);
        		win.setText("------"); 
        	}	
        }
    }
}
