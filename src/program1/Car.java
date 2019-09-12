package program1;

/**
 * 
 * Submission for CS 3331 Project 2. Three cars, all with the same acceleration
 * but with different starting times, must drive around a 3 mile track while
 * obeying speed limits for each 1-mile segment.
 * 
 * <ul>
 * <li>pre-9/6: Multiple attempts that all failed</li>
 * <li>9/6/2019: Initial Release of staged variant</li>
 * <li>9/7/2019: Changed print to stop when the car is done. Made program end
 * when all cars finish</li>
 * <li>9/9/2019: Moved change log above the author annotation and made it into a
 * list</li>
 * <li>9/12/2019: Added more comments</li>
 * </ul>
 *
 * @author Jacob Loosa
 *
 */
public class Car {

    /**
     *
     */
    private enum State {
	ACCEL, COAST, DECEL
    }

    static final float stageDistance = 5280f; // 1 Mile, measured in feet
    static final float acceleration = 15f; // Feet per second squared
    // Speeds
    static final float stage1Speed = toFeetPerSecond(20); // Feet per Second
    static final float stage2Speed = toFeetPerSecond(60); // Feet per Second
    static final float stage3Speed = toFeetPerSecond(30); // Feet per Second

    // Stage 1
    static final float stage1AccelDistance = accelerationDistance(0, stage1Speed); // Feet
    static final float stage1AccelTime = accelerationTime(0, stage1Speed); // Seconds
    static final float stage1CoastDistance = (stageDistance - stage1AccelDistance); // Feet
    static final float stage1CoastTime = stage1CoastDistance / stage1Speed; // Seconds

    // Stage 2
    static final float stage2AccelDistance = accelerationDistance(stage1Speed, stage2Speed); // Feet
    static final float stage2AccelTime = accelerationTime(stage1Speed, stage2Speed); // Seconds
    static final float stage2DecelDistance = accelerationDistance(stage2Speed, stage3Speed); // Feet
    static final float stage2DecelTime = accelerationTime(stage2Speed, stage3Speed); // Seconds
    static final float stage2CoastDistance = (stageDistance - stage2AccelDistance - stage2DecelDistance); // Feet
    static final float stage2CoastTime = stage2CoastDistance / stage2Speed; // Seconds

    // Stage 3
    static final float stage3CoastDistance = stageDistance; // Feet
    static final float stage3CoastTime = stage3CoastDistance / stage3Speed; // Seconds

    // Arrayed Data
    static float[] stageDistances = new float[6];
    static float[] stageInitVelocities = new float[6];
    static float[] stageTimes = new float[6];
    static State[] stageStates = new State[6];

    // Initialize all static arrays
    static {
	// Speed of vehicle at the start of each segment
	stageInitVelocities[0] = 0;
	stageInitVelocities[1] = stage1Speed;
	stageInitVelocities[2] = stage1Speed;
	stageInitVelocities[3] = stage2Speed;
	stageInitVelocities[4] = stage2Speed;
	stageInitVelocities[5] = stage3Speed;
	// Distance for the individual segments
	stageDistances[0] = stage1AccelDistance;
	stageDistances[1] = stage1CoastDistance;
	stageDistances[2] = stage2AccelDistance;
	stageDistances[3] = stage2CoastDistance;
	stageDistances[4] = stage2DecelDistance;
	stageDistances[5] = stage3CoastDistance;
	// Time to travel through the segment
	stageTimes[0] = stage1AccelTime;
	stageTimes[1] = stage1CoastTime;
	stageTimes[2] = stage2AccelTime;
	stageTimes[3] = stage2CoastTime;
	stageTimes[4] = stage2DecelTime;
	stageTimes[5] = stage3CoastTime;
	// States - What the car is doing in the segment
	stageStates[0] = State.ACCEL;
	stageStates[1] = State.COAST;
	stageStates[2] = State.ACCEL;
	stageStates[3] = State.COAST;
	stageStates[4] = State.DECEL;
	stageStates[5] = State.COAST;
    }

    // How far does the car wait before driving
    float delay;
    boolean isDone;

    Car(float delay) {
	this.delay = delay;
	this.isDone = false;
    }

    /**
     * Gets the speed of the car and sitance travelled. This is calculated by
     * subtracting the time needed for each segment until we found a segment that
     * the car is still within. One we know the car's segment, we use basic
     * kinematics (determined by the type of segment, E.G. COAST) to determine the
     * instantaneous position and velocity
     * 
     * @param time how much time has elapsed
     * @return a formatted String containing the speed and position of the vehicle
     */
    String info(float time) {
	if (isDone)
	    return String.format("%s", " \t \t \t ");
	if (time <= delay || time == 0) {
	    return speedLocationFormat(0, 0);
	} else
	    time -= delay;
	final float tmpTime = time;
	float location = 0;
	// We use i < 7 here so that we can use i = 6 to detect that the lap is over
	for (int i = 0; i < 7; i++) {
	    if (i == 6) {
		isDone = true;
		return String.format("%s", " \t Done! \t ");
	    }
	    // This line checks if the current stage has been completed. If it has, we
	    // subtract the time for this stage and try the next.
	    if (time <= stageTimes[i]) {
		if (stageStates[i] == State.ACCEL)
		    return speedLocationFormat(stageInitVelocities[i] + acceleration * time,
			    location + stageInitVelocities[i] * time + 0.5f * acceleration * time * time);
		if (stageStates[i] == State.COAST)
		    return speedLocationFormat(stageInitVelocities[i], location + stageInitVelocities[i] * time);
		if (stageStates[i] == State.DECEL)
		    return speedLocationFormat(stageInitVelocities[i] - acceleration * time,
			    location + stageInitVelocities[i] * time + 0.5f * -acceleration * time * time);
		throw new IllegalStateException("Stage exists but lacks a state! i=" + i);
	    } else {
		time -= stageTimes[i];
		location += stageDistances[i];
	    }
	}
	throw new IllegalStateException("Failed to find state for time: " + tmpTime);
    }

    /**
     * Method for debugging, not used in this implementation.
     * 
     * @return the speed of the car at a given time
     */
    float getSpeed(float time) {
	if (time <= delay || time == 0) {
	    return 0;
	} else
	    time -= delay;
	final float tmpTime = time;
	// We use i < 7 here so that we can use i = 6 to detect that the lap is over
	for (int i = 0; i < 7; i++) {
	    if (i == 6)
		return stageInitVelocities[5];
	    if (time <= stageTimes[i]) {
		if (stageStates[i] == State.ACCEL)
		    return stageInitVelocities[i] + acceleration * time;
		if (stageStates[i] == State.COAST)
		    return stageInitVelocities[i];
		if (stageStates[i] == State.DECEL)
		    return stageInitVelocities[i] - acceleration * time;
		throw new IllegalStateException("Stage exists but lacks a state! i=" + i);
	    } else {
		time -= stageTimes[i];
	    }
	}
	throw new IllegalStateException("Failed to find state for time: " + tmpTime);
    }

    String speedLocationFormat(float speed, float location) {
	return String.format("%05.2f \t %04.2f", toMilesPerHour(speed), toMiles(location));
    }

    /**
     * Main method, used to create the cars, print the output headers, and run the
     * main loop of the simulation
     * 
     * @param args
     */
    public static void main(String[] args) {
	Car carA = new Car(0);
	Car carB = new Car(60);
	Car carC = new Car(120);
	System.out.println("Time(s) \t \t Car A \t \t \t \t Car B \t \t \t \t Car C");
	System.out.println(" \t\t Speed \t Location \t Speed \t Location \t Speed \t Location");
	for (float currentTime = 0; !carC.isDone || !carB.isDone || !carA.isDone; currentTime += 30f) {
	    System.out.printf("%05.1f \t %s \t \t %s \t \t %s\n", currentTime, carA.info(currentTime),
		    carB.info(currentTime), carC.info(currentTime));
	}
    }

    static float toFeetPerSecond(float mph) {
	return mph * 5280f / 3600f;
    }

    static float toMilesPerHour(float fps) {
	return fps * 3600f / 5280f;
    }

    static float toMiles(float feet) {
	return feet / 5280f;
    }

    static float accelerationTime(float initialVelocity, float finalVelocity) {
	float time = Math.abs((finalVelocity - initialVelocity) / acceleration);
	// DEBUG: System.out.printf("Time to go from %.1f to %.1f is %.3f seconds \n",
	// initialVelocity, finalVelocity, time);
	return time;
    }

    static float accelerationDistance(float initialVelocity, float finalVelocity) {
	float time = accelerationTime(initialVelocity, finalVelocity);
	// X = v0t + 1/2*at^2
	float distance = initialVelocity * time + 0.5f * acceleration * time * time;
	// DEBUG: System.out.printf("Distance to go from %.1f to %.1f is %.3f feet \n",
	// initialVelocity, finalVelocity, distance);
	return distance;
    }

}
