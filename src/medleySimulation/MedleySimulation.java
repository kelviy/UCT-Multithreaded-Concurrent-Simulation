//M. M. Kuttel 2024 mkuttel@gmail.com
// MedleySimulation main class, starts all threads
package medleySimulation;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

public class MedleySimulation {
	// Latch connected to start button which releases swimmers into the stadium
	static final CountDownLatch startRaceLatch = new CountDownLatch(1);

	static final int numTeams=10;

	// Barrier that makes swimmers wait until all members of the same swim stroke are ready before entering the stadium
	// passed to each swim team
	static final CyclicBarrier allSwimmersReadyBarrier = new CyclicBarrier(numTeams);

   	static int frameX=300; //frame width
	static int frameY=620;  //frame height
	static int yLimit=400;
	static int max=5;

	static int gridX=50 ; //number of x grid points
	static int gridY=120; //number of y grid points 
		
	static SwimTeam[] teams; // array for team threads
	static PeopleLocation [] peopleLocations;  //array to keep track of where people are
	static StadiumView stadiumView; //threaded panel to display stadium
	static StadiumGrid stadiumGrid; // stadium on a discrete grid
	static Timer stopWatch; // custom stopWatch
	
	static FinishCounter finishLine; //records who won
	static CounterDisplay counterDisplay ; //threaded display of counter
	

	//Method to setup all the elements of the GUI
	public static void setupGUI(int frameX,int frameY) {
		// Frame initialize and dimensions
    	JFrame frame = new JFrame("Swim medley relay animation"); 
    	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	frame.setSize(frameX, frameY);
    	
      	JPanel g = new JPanel();
        g.setLayout(new BoxLayout(g, BoxLayout.Y_AXIS)); 
      	g.setSize(frameX,frameY);
		// additional time recorder
		JLabel t = new JLabel();
		g.add(t);
		t.setAlignmentX(Component.CENTER_ALIGNMENT);
		stopWatch = new Timer(t);

		stadiumView = new StadiumView(peopleLocations, stadiumGrid);
		stadiumView.setSize(frameX,frameY);
	    g.add(stadiumView);
	    
	    //add text labels to the panel - this can be extended
	    JPanel txt = new JPanel();
	    txt.setLayout(new BoxLayout(txt, BoxLayout.LINE_AXIS));
	    JLabel winner =new JLabel("");
	    txt.add(winner);
	    g.add(txt);
	    counterDisplay = new CounterDisplay(winner,finishLine);      //thread to update score
	    
	    //Add start and exit buttons
	    JPanel b = new JPanel();
        b.setLayout(new BoxLayout(b, BoxLayout.LINE_AXIS)); 
        
        JButton startB = new JButton("Start");
		// add the listener to the jbutton to handle the "pressed" event
		startB.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e)  {
				// starts all swimmer team threads
				startRaceLatch.countDown();
				stopWatch.startRecord(); // Additional time recorder
		    }
		   });
	
		JButton endB = new JButton("Quit");
				// add the listener to the jbutton to handle the "pressed" event
				endB.addActionListener(new ActionListener() {
			      public void actionPerformed(ActionEvent e) {
			    	  System.exit(0);
			      }
			    });

		b.add(startB);
		b.add(endB);
		g.add(b);

      	frame.setLocationRelativeTo(null);  // Center window on screen.
      	frame.add(g); //add contents to window
        frame.setContentPane(g);     
        frame.setVisible(true);	
	}
	
	
//Main method - starts it all
	public static void main(String[] args) throws InterruptedException {

	    finishLine = new FinishCounter(); //counters for people inside and outside club
	 
		stadiumGrid = new StadiumGrid(gridX, gridY, numTeams,finishLine); //setup stadium with size     
		SwimTeam.stadium = stadiumGrid; //grid shared with class
		Swimmer.stadium = stadiumGrid; //grid shared with class
	    peopleLocations = new PeopleLocation[numTeams*SwimTeam.sizeOfTeam]; //four swimmers per team
		teams = new SwimTeam[numTeams];
		for (int i=0;i<numTeams;i++) {
        	teams[i]=new SwimTeam(i, finishLine, peopleLocations, allSwimmersReadyBarrier);
		}
		setupGUI(frameX, frameY);  //Start Panel thread - for drawing animation
		finishLine.setStopwatch(stopWatch);

		//start viewer thread
		Thread view = new Thread(stadiumView);
		view.start();
       
      	//Start counter thread - for updating results
      	Thread results = new Thread(counterDisplay);  
      	results.start();

	  	// Start custom timer thread
		Thread stopWatchThread = new Thread(stopWatch);
		stopWatchThread.start();
      	
      	//start teams, which start swimmers.
      	for (int i=0;i<numTeams;i++) {
			  // makes all swimmers wait until start button is pressed
			  startRaceLatch.await();
			  teams[i].start();
		}
	}
}
