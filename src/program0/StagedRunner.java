package program0;

import java.util.function.Function;

/**
 * 
 * @author Jacob The approach to this runner is to split the entire run into
 *         "stages." The stages are as follows for the one-way trip:
 *         <ul>
 *         <li>(0-2) Assumed to have finished with a total distance passed of
 *         0</li>
 *         <li>(3) Accelerate</li>
 *         <li>(4) Run at full speed</li>
 *         <li>(5) Finish</li>
 *         </ul>
 *         because distance is covered while accelerating, it is possible to go
 *         from stage (1) to stage (3) without entering stage (2). This one-way
 *         trip will be calculated first as it is always going to be used. When
 *         the trip is two-way, the staging is a little different. It must be as
 *         follows:
 *         <ul>
 *         <li>(0) Accelerate</li>
 *         <li>(1) Run at full speed</li>
 *         <li>(2) Slow to stop</li>
 *         <li>(3) Accelerate</li>
 *         <li>(4) Run at full speed</li>
 *         <li>(5) Finish</li>
 *         </ul>
 *         This is the same as a one-way trip, but preceded by a trip in which
 *         the runner must slow. As such, we only need to know a few things for
 *         the stage distance calculations: max speed, acceleration, and time.
 *         Additionally, we may need inverse functions.
 *
 */
public class StagedRunner {

    String name;
    double maxSpeed, acceleration;

    double timeForTrack;

    double[] stageDistance, stageTime;
    Function<Double, Double>[] stageInstant;

    @SuppressWarnings("unchecked")
    StagedRunner(String name, double maxSpeed, double acceleration) {
	this.name = name;
	this.maxSpeed = maxSpeed;
	System.out.printf("%s \t%.1f\t\t\t\t%s\n", name, maxSpeed, acceleration);
	
	this.acceleration = acceleration;stageInstant = new Function[]
		    {
			    (t) -> accelDist((double) t),
			    (t) -> runDist((double) t),
			    (t) -> stageDistance[2] - accelDist((double) t),
			    (t) -> accelDist((double) t),
			    (t) -> runDist((double) t),
			    (t) -> 0
		    };

	stageDistance = new double[6];
	stageTime = new double[6];

	if (Runner.roundTrip) {
	    computeTwoWay();
	} else {
	    computeOneWay();
	}

	for (int i = 0; i < stageTime.length; i++)
	    timeForTrack += stageTime[i];
    }

    private void computeTwoWay() {
	double timeToAccelerateToMaxSpeed = maxSpeed / acceleration;
	double distanceToAccelerateToMaxSpeed = accelDist(timeToAccelerateToMaxSpeed);
	if (distanceToAccelerateToMaxSpeed > Runner.trackLength / 2d) {
	    stageDistance[0] = stageDistance[2] = Runner.trackLength / 2d;
	    stageTime[0] = stageTime[2] = accelTime(Runner.trackLength / 2d);
	} else {
	    stageDistance[0] = distanceToAccelerateToMaxSpeed;
	    stageDistance[1] = Runner.trackLength - distanceToAccelerateToMaxSpeed * 2;
	    stageDistance[2] = distanceToAccelerateToMaxSpeed;
	    stageTime[0] = timeToAccelerateToMaxSpeed;
	    stageTime[1] = runTime(stageDistance[1]);
	    stageTime[2] = timeToAccelerateToMaxSpeed;
	}
	computeOneWay();
    }

    private void computeOneWay() {
	// v = a * t;
	double timeToAccelerateToMaxSpeed = maxSpeed / acceleration;
	double distanceToAccelerateToMaxSpeed = accelDist(timeToAccelerateToMaxSpeed);
	if (distanceToAccelerateToMaxSpeed > Runner.trackLength) {
	    stageDistance[3] = Runner.trackLength;
	    stageTime[3] = accelTime(Runner.trackLength);
	} else {
	    stageDistance[3] = distanceToAccelerateToMaxSpeed;
	    stageDistance[4] = Runner.trackLength - distanceToAccelerateToMaxSpeed;
	    stageTime[3] = timeToAccelerateToMaxSpeed;
	    stageTime[4] = runTime(stageDistance[4]);
	}
    }

    public double accelDist(double dt) {
	// d = (1/2) * a * t^2
	return 0.5 * acceleration * (dt * dt);
    }

    public double accelTime(double dist) {
	// t = sqrt (2 * dist / accel)
	return Math.sqrt(2 * dist / acceleration);
    }

    public double runDist(double dt) {
	// d = v * t
	return maxSpeed * dt;
    }

    public double runTime(double dist) {
	// t = d / v
	return dist / maxSpeed;
    }

    private int i;
    public double getDistanceTravelled(double time) {
	double dist = 0;
	for (i = 0; i < 6; i++) {
	    if (time < stageTime[i]) return dist + stageInstant[i].apply(time) * (Runner.roundTrip && i > 2 ? -1: 1);
	    time -= stageTime[i];
	    dist += stageDistance[i] * (Runner.roundTrip && i > 2 ? -1: 1);
	}
	return dist;
    }

}
