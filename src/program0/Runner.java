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
    private static final double trackLength = 300.0;
    // Do the players turn around and run back?
    private static final boolean roundTrip = true;
    // How many simulated seconds do we wait before printing the speeds?
    private static final double printTimestep = 10;

    private LocalRunner[] competitors;

    public Runner() {
	LocalRunner nelly = new LocalRunner("Nelly", 30.0, 8.0);
	LocalRunner steve = new LocalRunner("Steve", 8.8, 3.0);
	LocalRunner usain = new LocalRunner("Usain", 41.0, 11.0);
	this.competitors = new LocalRunner[] { nelly, steve, usain };
	System.out.println();
    }

    public void simulate() {
	double totalTime = 0;
	System.out.print("Time");
	for (LocalRunner local : competitors) {
	    totalTime = Math.max(totalTime, local.timeForTrack);
	    System.out.print("\t" + local.name);
	}
	System.out.println();
	if (roundTrip)
	    totalTime *= 2;
	double currentTime = 0;
	do {
	    System.out.printf("%dm%ds", (int) currentTime / 60, (int) currentTime % 60);
	    for (LocalRunner local : competitors) {
		System.out.printf("\t%.2f", local.getDistance(currentTime));
	    }
	    System.out.println();
	    currentTime += printTimestep;
	} while (currentTime <= totalTime + printTimestep);
    }

    class LocalRunner {

	String name;
	double maxSpeed, acceleration;
	double distanceForMaxSpeed; // 0.5at^2
	double timeForMaxSpeed; // maxV/a

	double timeAccelerating, timeAtMax, timeForTrack;

	LocalRunner(String name, double maxSpeed, double acceleration) {
	    this.name = name;
	    this.maxSpeed = maxSpeed;
	    this.acceleration = acceleration;
	    this.timeForMaxSpeed = maxSpeed / acceleration;
	    this.distanceForMaxSpeed = 0.5 * acceleration * sq(timeForMaxSpeed);

	    if (distanceForMaxSpeed >= trackLength / 2.0) {
		timeAccelerating = timeToAccelerateOverDistance(trackLength / 2.0);
		timeAtMax = 0;
		System.out.printf(
			"Runner '%s' is unable to hit max speed before needing to slow down to turn!(dist=%.2fft)\n",
			name, trackLength / 2);
	    } else {
		timeAccelerating = timeForMaxSpeed;
		timeAtMax = (trackLength - acceleration * sq(timeAccelerating)) / maxSpeed;
		System.out.printf("Runner '%s' accelerates to max in %.2f seconds (dist=%.2fft)\n", name,
			timeForMaxSpeed, distanceForMaxSpeed);
	    }
	    timeForTrack = 2 * timeAccelerating + timeAtMax;
	}

	public double getDistance(double time) {
	    if (time < 0)
		throw new IllegalArgumentException(
			"Cannot compute negative time, please retry in " + time + " seconds");
	    if (time >= 2 * timeForTrack)
		return trackLength * 2;
	    if (time >= timeForTrack)
		return trackLength + getDistance(time - timeForTrack);
	    if (time < timeAccelerating)
		return 0.5 * acceleration * sq(time);
	    if (time < timeAccelerating + timeAtMax)
		return distanceForMaxSpeed + maxSpeed * (time - timeForMaxSpeed);
	    return trackLength - 0.5 * acceleration * sq(timeForTrack - time);
	}

	public double getDisplacement(double time) {
	    if (time < 0)
		throw new IllegalArgumentException(
			"Cannot compute negative time, please retry in " + time + " seconds");
	    if (time >= 2 * timeForTrack)
		return 0;
	    if (time >= timeForTrack)
		return trackLength - getDisplacement(time - timeForTrack);
	    return getDistance(time);
	}

	/**
	 * Computers the square of the input
	 * 
	 * @param a the value to square
	 * @return the square of value a
	 */
	private double sq(double a) {
	    return a * a;
	}

	/**
	 * Provided a distance, this function will compute the time needed for the
	 * runner to reach that distance. This assumes that a max speed is never
	 * achieved
	 * 
	 * @param distance How much distance must be covered
	 * @return time taken to reach provided distance given constant acceleration
	 */
	private double timeToAccelerateOverDistance(double distance) {
	    return Math.sqrt(2 * distance / acceleration);
	}
    }

    public static void main(String[] args) {
	System.out.printf("The track has a one-way length of %f feet\n", trackLength);
	System.out.printf("Players %s required to turn around and come back\n", roundTrip ? "are" : "are NOT");
	System.out.printf("Players speeds will print in %f second intervals\n", printTimestep);
	System.out.println();
	Runner runner = new Runner();
	runner.simulate();
    }

}
