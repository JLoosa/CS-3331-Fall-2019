package program3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

/**
 * NOTICE: I really wanted to use the symbol Δ in my comments, so this file
 * requires UTF-8 Encoding!
 * 
 * Submission for CS 3331 Project 3. Three cars, all with the same acceleration
 * but with different starting times, must drive around a 3 mile track while
 * obeying speed limits for each 1-mile segment. Unlike program 1, these track
 * speeds are configured using an XML input file.
 * 
 * How has the approach changed? With the ability to edit the max speeds via
 * XML, each segment of the race must now be split into three sections with
 * checks for if each of the sections is used. Essentially, the course is now
 * made out of individual segments that all have the exact same approach to
 * solving. This is wonderful, because it allows for two things that were barely
 * existent in the last version: graph symmetry and extensive code reuse!
 * 
 * The code keywords are as follows: Track: The entire track, start to finish
 * Segment: Each portion of the track, separated by speed and individually
 * specified by the XML Section: Each portion of a segment, Acceleration, Coast,
 * and Deceleration
 * 
 * <ul>
 * <li>9/13/2019: Start of code adaptation</li>
 * </ul>
 *
 * @author Jacob Loosa
 *
 */
public class Car {

    public static final float ACCELERATION = 15f; // Feet per second squared
    // Segments are of type Comparable<Segment>, so we can have Java handle putting
    // them in order automatically
    static List<Segment> segments;
    static float totalTrackTime;

    // How far does the car wait before driving
    float delay;

    Car(float delay) {
	this.delay = delay;
    }

    private float getSpeed(float time) {
	if (time <= delay)
	    return 0;
	time -= delay;
	return segments.get(0).getSpeed(time);
    }

    private float getDistance(float time) {
	if (time <= delay)
	    return 0;
	time -= delay;
	return segments.get(0).getDistance(time);
    }

    private String getPositionInfo(float time) {
	return String.format("%05.2f \t %05.2f", Kinematics.toMilesPerHour(getSpeed(time)), Kinematics.toMiles(getDistance(time)));
    }

    /**
     * Main method, used to create the cars, print the output headers, and run the
     * main loop of the simulation
     * 
     * @param args
     */
    public static void main(String[] args) {
	segments = new ArrayList<Segment>();

	String fileName = null;
	if (args.length > 0)
	    fileName = args[0];
	Scanner keyboard = new Scanner(System.in);
	if (fileName == null) {
	    System.out.print("Please provide the file location \n >> ");
	    fileName = keyboard.nextLine();
	}
	keyboard.close();
	// Load the segments from the provided file (if possible)
	new CarXMLReader(fileName);
	Collections.sort(segments);

	printDebug("Length of segment set is: " + segments.size());
	segments.forEach((s) -> Car.printDebug(s.toString()));
	totalTrackTime = segments.get(0).getSegmentTime();

	for (int i = 1; i < segments.size(); i++) {
	    if (!segments.get(i - 1).setNextSegment(segments.get(i)) || !segments.get(i).setPreviousSegment(segments.get(i - 1))) {
		System.err.println("Failed to create segment ordering. Was one missing?");
		System.exit(1);
	    }

	}

	segments.get(0).computeKinematicsRecursively();
	segments.forEach((s) -> totalTrackTime += s.getSegmentTime());

	Car carA = new Car(0);
	Car carB = new Car(60);
	Car carC = new Car(120);
	System.out.println("Time(s) \t \t Car A \t \t \t \t Car B \t \t \t \t Car C");
	System.out.println(" \t\t Speed \t Location \t Speed \t Location \t Speed \t Location");
	for (float currentTime = 0; !carC.isDone(currentTime) || !carB.isDone(currentTime) || !carA.isDone(currentTime); currentTime += 3f) {
	    System.out.printf("%05.1f \t %s \t \t %s \t \t %s\n", currentTime, carA.getPositionInfo(currentTime), carB.getPositionInfo(currentTime), carC.getPositionInfo(currentTime));
	}
    }

    private boolean isDone(float currentTime) {
	return currentTime >= totalTrackTime + delay;
    }

    public static boolean debug = true;

    public static void printDebug(String... output) {
	if (!debug)
	    return;
	for (String out : output)
	    System.out.printf("[DEBUG] %s \n", out);
    }

}
