package program0;

public class LocalRunner {

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

	if (distanceForMaxSpeed >= Runner.trackLength / 2.0) {
	    timeAccelerating = timeToAccelerateOverDistance(Runner.trackLength / 2.0);
	    timeAtMax = 0;
	    System.out.printf(
		    "Runner '%s' is unable to hit max speed before needing to slow down to turn!(dist=%.2fft)\n", name,
		    Runner.trackLength / 2);
	} else {
	    timeAccelerating = timeForMaxSpeed;
	    timeAtMax = (Runner.trackLength - acceleration * sq(timeAccelerating)) / maxSpeed;
	    System.out.printf("Runner '%s' accelerates to max in %.2f seconds (dist=%.2fft)\n", name, timeForMaxSpeed,
		    distanceForMaxSpeed);
	}
	timeForTrack = 2 * timeAccelerating + timeAtMax;
    }

    public double getDistance(double time) {
	if (time < 0)
	    throw new IllegalArgumentException("Cannot compute negative time, please retry in " + time + " seconds");
	if (time >= 2 * timeForTrack)
	    return Runner.trackLength * 2;
	if (time >= timeForTrack)
	    return Runner.trackLength + getDistance(time - timeForTrack);
	if (time < timeAccelerating)
	    return 0.5 * acceleration * sq(time);
	if (time < timeAccelerating + timeAtMax)
	    return distanceForMaxSpeed + maxSpeed * (time - timeForMaxSpeed);
	return Runner.trackLength - 0.5 * acceleration * sq(timeForTrack - time);
    }

    public double getDisplacement(double time) {
	if (time < 0)
	    throw new IllegalArgumentException("Cannot compute negative time, please retry in " + time + " seconds");
	if (time >= 2 * timeForTrack)
	    return 0;
	if (time >= timeForTrack)
	    return Runner.trackLength - getDisplacement(time - timeForTrack);
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
