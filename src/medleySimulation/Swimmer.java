//M. M. Kuttel 2024 mkuttel@gmail.com
//Class to represent a swimmer swimming a race
//Swimmers have one of four possible swim strokes: backstroke, breaststroke, butterfly and freestyle
package medleySimulation;

import java.awt.Color;

import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;


public class Swimmer extends Thread {

	// shared objects below

	// Barrier to check if all team members have arrived - passed by SwimTeam
	private final CyclicBarrier teamArrivedBarrier;

	// Integer which determines the order in which the swimmers enter the stadium
	// Atomic Integer to prevent data races
	// One Integer for each team
	private final AtomicInteger toEnterOrder;
	// Barrier which checks if all swim members of same stroke are ready before diving - passed by MedleySimulation
	private final CyclicBarrier allSwimmersReadyBarrier;
	// Latch releases when swimmer's team member finishes to prompt the next team member to dive
	private CountDownLatch currentLatch;
	// Access to the next team member's latch for release when current swimmer finishes
	private CountDownLatch nextTeamLatch;

	public static StadiumGrid stadium; //shared 
	private final FinishCounter finish; //shared
		
	GridBlock currentBlock;
	private Random rand;
	private int movingSpeed;
	
	private PeopleLocation myLocation;
	private int ID; //thread ID 
	private int team; // team ID
	private GridBlock start;

	public enum SwimStroke { 
		Backstroke(1,2.5,Color.black),
		Breaststroke(2,2.1,new Color(255,102,0)),
		Butterfly(3,2.55,Color.magenta),
		Freestyle(4,2.8,Color.red);
	    	
	     private final double strokeTime;
	     private final int order; // in minutes
	     private final Color colour;   

	     SwimStroke( int order, double sT, Color c) {
	            this.strokeTime = sT;
	            this.order = order;
	            this.colour = c;
	        }
	  
	        public int getOrder() {return order;}

	        public  Color getColour() { return colour; }
	    }  
	    private final SwimStroke swimStroke;
	
	//Constructor
	Swimmer(int ID, int t, PeopleLocation loc, FinishCounter f, int speed, SwimStroke s, CyclicBarrier teamArrivedBarrier, AtomicInteger toEnterOrder, CyclicBarrier allSwimmersReadyBarrier) {
		this.swimStroke = s;
		this.ID=ID;
		movingSpeed=speed; //range of speeds for swimmers
		this.myLocation = loc;
		this.team=t;
		start = stadium.returnStartingBlock(team);
		finish=f;
		rand=new Random();
		this.teamArrivedBarrier = teamArrivedBarrier;
		this.toEnterOrder = toEnterOrder;
		this.allSwimmersReadyBarrier = allSwimmersReadyBarrier;
	}

	// method that assigns the latches after swimmers in the team are created - called in swim team
	public void assignLatches(CountDownLatch currentLatch, CountDownLatch nextTeamLatch) {
		this.currentLatch = currentLatch;
		this.nextTeamLatch = nextTeamLatch;
	}
	
	//getter
	public  int getX() { return currentBlock.getX();}
	
	//getter
	public   int getY() {	return currentBlock.getY();	}
	
	//getter
	public  int getSpeed() { return movingSpeed; }

	
	public SwimStroke getSwimStroke() {
		return swimStroke;
	}

	//!!!You do not need to change the method below!!!
	//swimmer enters stadium area
	public void enterStadium() throws InterruptedException {
		currentBlock = stadium.enterStadium(myLocation);  //
		sleep(200);  //wait a bit at door, look around
	}
	
	//!!!You do not need to change the method below!!!
	//go to the starting blocks
	//printlns are left here for help in debugging
	public void goToStartingBlocks() throws InterruptedException {		
		int x_st= start.getX();
		int y_st= start.getY();
	//System.out.println("Thread "+this.ID + " has start position: " + x_st  + " " +y_st );
	// System.out.println("Thread "+this.ID + " at " + currentBlock.getX()  + " " +currentBlock.getY() );
	 while (currentBlock!=start) {
		//	System.out.println("Thread "+this.ID + " has starting position: " + x_st  + " " +y_st );
		//	System.out.println("Thread "+this.ID + " at position: " + currentBlock.getX()  + " " +currentBlock.getY() );
			sleep(movingSpeed*3);  //not rushing 
			currentBlock=stadium.moveTowards(currentBlock,x_st,y_st,myLocation); //head toward starting block
		//	System.out.println("Thread "+this.ID + " moved toward start to position: " + currentBlock.getX()  + " " +currentBlock.getY() );
		}
	System.out.println("-----------Thread "+this.ID + " at start " + currentBlock.getX()  + " " +currentBlock.getY() );
	}
	
	//!!!You do not need to change the method below!!!
	//dive in to the pool
	private void dive() throws InterruptedException {
		int x= currentBlock.getX();
		int y= currentBlock.getY();
		currentBlock=stadium.jumpTo(currentBlock,x,y-2,myLocation);
	}
	
	//!!!You do not need to change the method below!!!
	//swim there and back
	private void swimRace() throws InterruptedException {
		int x= currentBlock.getX();
		while((boolean) ((currentBlock.getY())!=0)) {
			currentBlock=stadium.moveTowards(currentBlock,x,0,myLocation);
			//System.out.println("Thread "+this.ID + " swimming " + currentBlock.getX()  + " " +currentBlock.getY() );
			sleep((int) (movingSpeed*swimStroke.strokeTime)); //swim
			System.out.println("Thread "+this.ID + " swimming  at speed" + movingSpeed );	
		}

		while((boolean) ((currentBlock.getY())!=(StadiumGrid.start_y-1))) {
			currentBlock=stadium.moveTowards(currentBlock,x,StadiumGrid.start_y,myLocation);
			//System.out.println("Thread "+this.ID + " swimming " + currentBlock.getX()  + " " +currentBlock.getY() );
			sleep((int) (movingSpeed*swimStroke.strokeTime));  //swim
		}
		
	}
	
	//!!!You do not need to change the method below!!!
	//after finished the race
	public void exitPool() throws InterruptedException {		
		int bench=stadium.getMaxY()-swimStroke.getOrder(); 			 //they line up
		int lane = currentBlock.getX()+1;//slightly offset
		currentBlock=stadium.moveTowards(currentBlock,lane,currentBlock.getY(),myLocation);
	   while (currentBlock.getY()!=bench) {
		 	currentBlock=stadium.moveTowards(currentBlock,lane,bench,myLocation);
			sleep(movingSpeed*3);  //not rushing 
		}
	}
	
	public void run() {
		try {
			//Swimmer arrives
			sleep(movingSpeed+(rand.nextInt(10))); //arriving takes a while
			myLocation.setArrived();

			// barrier to await for all other team members
			teamArrivedBarrier.await();

			// synchronization mechanism for swimmers to enter in order
			// Lock on Atomic Integer since there's a compound action on the atomic integer
			// Checks Swimmer Thread checks Atomic Integer to see if it's the requested to enter the stadium
			// Else wait until woken up by a thread that has successfully entered the stadium
			// Swimmer that enters would update the Integer to request next swimmer in order to enter
			synchronized (toEnterOrder) {
				// waits if not requested
				while (toEnterOrder.get() < getSwimStroke().getOrder()) {
					toEnterOrder.wait();
				}
				// proceeds if requested
				enterStadium();
				toEnterOrder.incrementAndGet();
				toEnterOrder.notifyAll();
			}

			goToStartingBlocks();

			// barrier to wait for all other swimmers of the same swim stroke to arrive at starting block
			allSwimmersReadyBarrier.await();

			// waits for the already swimming team member to finish before diving and swimming
			// if it's the first team member then their latch has already been released at instantiation in SwimTeam's constructor
			currentLatch.await();
			dive(); 
				
			swimRace();

			if(swimStroke.order==4) {
				finish.finishRace(ID, team); // finish line
			}
			else {
				//System.out.println("Thread "+this.ID + " done " + currentBlock.getX()  + " " +currentBlock.getY() );

				// Signal for the next team member to dive and swim the race.
				nextTeamLatch.countDown();
				exitPool();//if not last swimmer leave pool
			}
			
		} catch (InterruptedException e1) {  //do nothing
		} catch (BrokenBarrierException e) {
            throw new RuntimeException(e);
        }
    }

}
