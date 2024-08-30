//M. M. Kuttel 2024 mkuttel@gmail.com
//Class to represent a swim team - which has four swimmers
package medleySimulation;

import medleySimulation.Swimmer.SwimStroke;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

public class SwimTeam extends Thread {

	// Integer to keep track of which team member should enter the stadium.
	// Swimmer Thread's use conditional variables and this integer as a condition to check or wait
	private AtomicInteger toEnterOrder;
	// Instantiated in main method
	// Checks if all swimmers of the same team are ready before entering stadium
	private CyclicBarrier allSwimmersReadyBarrier;

	public static StadiumGrid stadium; //shared 
	private Swimmer [] swimmers;
	private int teamNo; //team number
	
	public static final int sizeOfTeam=4;
	
	SwimTeam( int ID, FinishCounter finish,PeopleLocation [] locArr, CyclicBarrier allSwimmersReadyBarrier ) {
		this.teamNo=ID;
		
		swimmers= new Swimmer[sizeOfTeam];
	    SwimStroke[] strokes = SwimStroke.values();  // Get all enum constants
		stadium.returnStartingBlock(ID);
		this.allSwimmersReadyBarrier = new CyclicBarrier(4);
		toEnterOrder = new AtomicInteger(1);

		for(int i=teamNo*sizeOfTeam,s=0;i<((teamNo+1)*sizeOfTeam); i++,s++) { //initialise swimmers in team
			locArr[i]= new PeopleLocation(i,strokes[s].getColour());
	      	int speed=(int)(Math.random() * (3)+30); //range of speeds 
			swimmers[s] = new Swimmer(i,teamNo,locArr[i],finish,speed,strokes[s], this.allSwimmersReadyBarrier, toEnterOrder, allSwimmersReadyBarrier); //hardcoded speed for now
		}

		CountDownLatch tempLatch1 = new CountDownLatch(1);
		// Releases latch for the first swimmer since there is no swimmer already swimming
		tempLatch1.countDown();
		// Creates and assigned latches to each swimmer in the team
		for(int i=0; i < swimmers.length; i++) {
			CountDownLatch tempLatch2 = new CountDownLatch(1);
			swimmers[i].assignLatches(tempLatch1, tempLatch2);
			tempLatch1 = tempLatch2;
		}
	}
	
	
	public void run() {
		try {	
			for(int s=0;s<sizeOfTeam; s++) { //start swimmer threads
				swimmers[s].start();
			}
			
			for(int s=0;s<sizeOfTeam; s++) swimmers[s].join();			//don't really need to do this;
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
	

