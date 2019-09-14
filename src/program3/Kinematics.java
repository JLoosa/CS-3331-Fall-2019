package program3;

/**
 * This class is dedicated to housing all of the equations that will be used in
 * computing the car kinematics
 * 
 * @author Jacob Loosa
 *
 */
public class Kinematics {

    private Kinematics() {
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

    static float toFeet(float miles) {
	return miles * 5280f;
    }

    static float accelerationTime(float initialVelocity, float finalVelocity, float acceleration) {
	float time = (finalVelocity - initialVelocity) / acceleration;
	return time;
    }

    static float accelerationDistance(float initialVelocity, float finalVelocity, float acceleration) {
	float time = accelerationTime(initialVelocity, finalVelocity, acceleration);
	float distance = distanceTraveled(initialVelocity, acceleration, time);
	return distance;
    }

    /**
     * Computes the kinematic equation for distance ΔX = V(ΔT) + 0.5A(ΔT)^2
     * 
     * @param initialVelocity      Speed at start of segment
     * @param constantAcceleration Change in speed over time
     * @param deltaTime            Amount of time that has passed
     * @return displacement from starting point
     */
    static float distanceTraveled(float initialVelocity, float constantAcceleration, float deltaTime) {
	return initialVelocity * deltaTime + 0.5f * constantAcceleration * deltaTime * deltaTime;
    }

    /**
     * Computes the kinematic inverse for distance ΔX = V(ΔT) + 0.5A(ΔT)^2. This
     * method will take in the velocity, acceleration, and distance as parameters,
     * and return the time to accomplish this as the result. 0 = 0.5A(ΔT)^2 + V(ΔT)
     * ΔT = (-V + sqrt (V^2 - 2AΔX) ) / (A)
     * 
     * @param initialVelocity      Speed at start of segment
     * @param constantAcceleration Change in speed over time
     * @param distance             The distance that must be covered
     * @return the time required to pass this distance
     */
    public static float inverseDistance(float initialVelocity, int constantAcceleration, float distance) {
	if (constantAcceleration == 0)
	    return distance / initialVelocity;
	float discriminant = initialVelocity * initialVelocity - 2 * constantAcceleration * distance;
	if (discriminant < 0)
	    throw new RuntimeException("Non-real solution to inverse distance formula. Are you sure your arguments are correct?");
	return (float) (Math.sqrt(discriminant) / (2 * constantAcceleration));
    }

    /**
     * Computes the formula V = V0 + AΔT
     * 
     * @param initialVelocity
     * @param constantAcceleration
     * @param deltaTime
     * @return
     */
    public static float getVelocity(float initialVelocity, float constantAcceleration, float deltaTime) {
	return initialVelocity + constantAcceleration * deltaTime;
    }

}
