package program3;

/**
 * Segment object structure will be set to mimic that of a double-linked list.
 * This is because computations require information from both the previous and
 * child node (primarily speed limit information). Additionally, I still plan to
 * step through the stages as I did in the previous version, so the option to
 * call the next Segment in constant time is both nice, and readily available.
 * 
 * @author Jacob Loosa
 *
 */
public class Segment implements Comparable<Segment> {

    // Static fields for use in all Segments
    private final static int SECTION_ACCEL = 0;
    private final static int SECTION_COAST = 1;
    private final static int SECTION_DECEL = 2;

    // Double-Linked List information, although they may all be stored in a single
    // collection anyway.
    private Segment previousSegment = null;
    private Segment nextSegment = null;

    // Information from the XML file
    private final int segmentNumber;
    private final float segmentLength;
    private final float segmentSpeedLimit;

    // Information for determining exact positional data
    private float[] sectionTime = new float[3];
    private float[] sectionDistance = new float[3];
    private float[] sectionStartSpeed = new float[3];

    public Segment(int segmentNumber, float segmentLength, float segmentSpeedLimit) {
	this.segmentNumber = segmentNumber;
	this.segmentLength = segmentLength;
	this.segmentSpeedLimit = segmentSpeedLimit;
	this.sectionStartSpeed[SECTION_COAST] = this.segmentSpeedLimit;
	this.sectionStartSpeed[SECTION_DECEL] = this.segmentSpeedLimit;
    }

    public boolean setPreviousSegment(Segment segment) {
	// Allow null to delete the previous segment
	if (segment == null) {
	    this.previousSegment = segment;
	    Car.printDebug("Deleted previous segment. Total length is now: " + getRecursiveLength());
	    return true;
	}
	if (segment.segmentNumber != this.segmentNumber - 1)
	    return false;
	this.previousSegment = segment;
	Car.printDebug("Added previous segment. Total length is now: " + getRecursiveLength());
	return true;
    }

    public boolean setNextSegment(Segment segment) {
	// Allow null to delete the next segment
	if (segment == null) {
	    this.nextSegment = segment;
	    return true;
	}
	if (segment.segmentNumber != this.segmentNumber + 1)
	    return false;
	this.nextSegment = segment;
	return true;
    }

    @Override
    public int compareTo(Segment otherSegment) {
	return this.segmentNumber - otherSegment.segmentNumber;
    }

    public float getSegmentTime() {
	return sectionTime[SECTION_ACCEL] + sectionTime[SECTION_COAST] + sectionTime[SECTION_DECEL];
    }

    /**
     * The bread and butter of this class. Calculates all of the kinematics needed
     * to print out the speed and position of a car after a given amount of time
     * traveling. Really, it's just a big beautiful block of code that handles time
     * and distance calculations.
     */
    public void computeKinematicsRecursively() {
	Car.printDebug(String.format("Segment %d is now computing kinematics!", segmentNumber));
	this.sectionTime[SECTION_ACCEL] = Kinematics.accelerationTime(sectionStartSpeed[SECTION_ACCEL], sectionStartSpeed[SECTION_COAST], Car.ACCELERATION);
	this.sectionDistance[SECTION_ACCEL] = Kinematics.distanceTraveled(sectionStartSpeed[SECTION_ACCEL], Car.ACCELERATION, this.sectionTime[SECTION_ACCEL]);
	Car.printDebug(String.format("Segment %d Acceleration Time & Distance: %.2f, %.2f", segmentNumber, sectionTime[SECTION_ACCEL], sectionDistance[SECTION_ACCEL]));

	if (nextSegment != null) {
	    sectionTime[SECTION_DECEL] = Kinematics.accelerationTime(sectionStartSpeed[SECTION_DECEL], nextSegment.segmentSpeedLimit, -Car.ACCELERATION);
	    sectionDistance[SECTION_DECEL] = Kinematics.distanceTraveled(sectionStartSpeed[SECTION_DECEL], -Car.ACCELERATION, sectionTime[SECTION_DECEL]);
	    if (sectionDistance[SECTION_DECEL] < 0) {
		Car.printDebug(String.format("Segment %d Requires negative deceleration distance!: %.2f, %.2f", segmentNumber, sectionTime[SECTION_DECEL], sectionDistance[SECTION_DECEL]));
		sectionTime[SECTION_DECEL] = 0;
		sectionDistance[SECTION_DECEL] = 0;
	    }
	}
	Car.printDebug(String.format("Segment %d Deceleration Time & Distance: %.2f, %.2f", segmentNumber, sectionTime[SECTION_DECEL], sectionDistance[SECTION_DECEL]));

	sectionDistance[SECTION_COAST] = segmentLength - sectionDistance[SECTION_ACCEL] - sectionDistance[SECTION_DECEL];
	if (sectionDistance[SECTION_COAST] < 0)
	    throw new RuntimeException("Car is unable to reach the speed limit! Please consider this case!");
	sectionTime[SECTION_COAST] = Kinematics.inverseDistance(sectionStartSpeed[SECTION_COAST], 0, sectionDistance[SECTION_COAST]);
	Car.printDebug(String.format("Segment %d Coast Time & Distance: %.2f, %.2f", segmentNumber, sectionTime[SECTION_COAST], sectionDistance[SECTION_COAST]));

	// If we have a segment after us, we should tell them what we know and then have
	// them handle their own computations.
	if (nextSegment != null) {
	    nextSegment.sectionStartSpeed[SECTION_ACCEL] = Math.min(segmentSpeedLimit, nextSegment.segmentSpeedLimit);
	    nextSegment.computeKinematicsRecursively();
	}
    }

    @Override
    public String toString() {
	return String.format("Segment #%d: %.2f feet @ %.2f feet/second", segmentNumber, segmentLength, segmentSpeedLimit);
    }

    public float getSegmentLength() {
	return this.segmentLength;
    }

    public float getRecursiveLength() {
	return getPreviousLength() + getSegmentLength();
    }

    public float getPreviousLength() {
	if (previousSegment != null)
	    return previousSegment.getRecursiveLength();
	else
	    return 0;
    }

    /**
     * Searches each section sequentially, and passes time to next node if a
     * suitable segment is not found If no segments follow this, the velocity upon
     * reaching the end of this segment is returned (normally the max speed)
     * 
     * @param time
     * @return
     */
    public float getSpeed(float time) {
	if (time < sectionTime[SECTION_ACCEL])
	    return Kinematics.getVelocity(sectionStartSpeed[SECTION_ACCEL], Car.ACCELERATION, time);
	time -= sectionTime[SECTION_ACCEL];
	if (time < sectionTime[SECTION_COAST])
	    return sectionStartSpeed[SECTION_COAST];
	time -= sectionTime[SECTION_COAST];
	if (time < sectionTime[SECTION_DECEL])
	    return Kinematics.getVelocity(sectionStartSpeed[SECTION_DECEL], -Car.ACCELERATION, time);
	time -= sectionTime[SECTION_DECEL];
	if (nextSegment != null)
	    return nextSegment.getSpeed(time);
	return sectionStartSpeed[SECTION_COAST];
    }

    /**
     * Searches each section sequentially, and passes time to next node if a
     * suitable segment is not found If no segments follow this, the position upon
     * reaching the end of this segment is returned (normally the length of the track)
     * 
     * @param time
     * @return
     */
    public float getDistance(float time) {
	if (time < sectionTime[SECTION_ACCEL])
	    return getPreviousLength() + Kinematics.distanceTraveled(sectionStartSpeed[SECTION_ACCEL], Car.ACCELERATION, time);
	time -= sectionTime[SECTION_ACCEL];
	if (time < sectionTime[SECTION_COAST])
	    return getPreviousLength() + sectionDistance[SECTION_ACCEL] + Kinematics.distanceTraveled(sectionStartSpeed[SECTION_COAST], 0, time);
	time -= sectionTime[SECTION_COAST];
	if (time < sectionTime[SECTION_DECEL])
	    return getPreviousLength() + sectionDistance[SECTION_ACCEL] + sectionDistance[SECTION_COAST] + Kinematics.distanceTraveled(sectionStartSpeed[SECTION_DECEL], -Car.ACCELERATION, time);
	time -= sectionTime[SECTION_DECEL];
	if (nextSegment != null)
	    return nextSegment.getDistance(time);
	return getRecursiveLength();
    }

}
