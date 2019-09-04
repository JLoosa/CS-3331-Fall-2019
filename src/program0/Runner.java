package program0;

/**
 * 
 * @author Jacob
 * 
 *         My submission for Program 0 Due Wednesday, September 4, 2019
 *
 */
public class Runner {

    // Length of track in one direction
    public static final double trackLength = 300.0;
    // Do the players turn around and run back?
    public static final boolean roundTrip = true;
    // How many simulated seconds do we wait before printing the speeds?
    private static final double printTimestep = 10;

    private StagedRunner[] competitors;

    public Runner() {
	System.out.println("Runner\tMax Speed(f/s)\tAccelerations(f/s/s)");
	StagedRunner nelly = new StagedRunner("Nelly", 30.0, 8.0);
	StagedRunner steve = new StagedRunner("Steve", 8.8, 3.0);
	StagedRunner usain = new StagedRunner("Usain", 41.0, 11.0);
	this.competitors = new StagedRunner[] { nelly, steve, usain };
	System.out.println();
    }

    public void simulate() {
	double totalTime = 0;
	System.out.print("Time");
	for (StagedRunner local : competitors) {
	    totalTime = Math.max(totalTime, local.timeForTrack);
	    System.out.print("\t" + local.name);
	}
	System.out.println();
	double currentTime = 0;
	do {
	    System.out.printf("    %.1f",currentTime);
	    for (StagedRunner local : competitors) {
		System.out.printf("   \t%.1f", Math.abs(local.getDistanceTravelled(currentTime)));
	    }
	    System.out.println();
	    currentTime += printTimestep;
	} while (currentTime <= totalTime + printTimestep);
    }

    public static void main(String[] args) {
	System.out.printf("The track has a one-way length of %f feet\n", trackLength);
	System.out.printf("Players %s required to turn around and come back\n", roundTrip ? "are" : "are NOT");
	System.out.printf("Players distances will print in %f second intervals\n", printTimestep);
	System.out.println();
	Runner runner = new Runner();
	runner.simulate();
    }

}
