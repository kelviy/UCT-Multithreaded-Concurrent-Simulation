//M. M. Kuttel 2024 mkuttel@gmail.com
//Class to represent a swim team - which has four swimmers
package medleySimulation;

import medleySimulation.Swimmer.SwimStroke;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

public class SwimTeam extends Thread {

	private AtomicInteger leader;
	public static StadiumGrid stadium; //shared 
	private Swimmer [] swimmers;
	private int teamNo; //team number
	private CyclicBarrier prepared;

	
	public static final int sizeOfTeam=4;
	
	SwimTeam( int ID, FinishCounter finish,PeopleLocation [] locArr, CyclicBarrier allSwimmersReadyBarrier ) {
		this.teamNo=ID;
		
		swimmers= new Swimmer[sizeOfTeam];
	    SwimStroke[] strokes = SwimStroke.values();  // Get all enum constants
		stadium.returnStartingBlock(ID);
		prepared = new CyclicBarrier(4);
		leader = new AtomicInteger(1);

		for(int i=teamNo*sizeOfTeam,s=0;i<((teamNo+1)*sizeOfTeam); i++,s++) { //initialise swimmers in team
			locArr[i]= new PeopleLocation(i,strokes[s].getColour());
	      	int speed=(int)(Math.random() * (3)+30); //range of speeds 
			swimmers[s] = new Swimmer(i,teamNo,locArr[i],finish,speed,strokes[s], prepared, leader, allSwimmersReadyBarrier); //hardcoded speed for now
		}
		CountDownLatch tempLatch1 = new CountDownLatch(1);
		tempLatch1.countDown();
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
	

