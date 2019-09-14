package program3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

/**
 * <p>
 * <b> NOTICE: I really wanted to use the symbol Δ in my comments, so this
 * program requires UTF-8 encoding! </b><br>
 * -- One may try to argue that it makes the program less usable, but it's Δ, so
 * I think it's worthwhile.
 * </p>
 * 
 * <p>
 * Submission for CS 3331 Project 3. Three cars, all with the same acceleration
 * but with different starting times, must drive around a 3 mile track while
 * obeying speed limits for each 1-mile segment. Unlike program 1, these track
 * speeds are configured using an XML input file.
 * </p>
 * 
 * <p>
 * How has the approach changed? With the ability to edit the max speeds via
 * XML, each segment of the race must now be split into three sections with
 * checks for if each of the sections is used. Essentially, the course is now
 * made out of individual segments that all have the exact same approach to
 * solving. This is wonderful, because it allows for two things that were barely
 * existent in the last version: graph symmetry and extensive code reuse!
 * 
 * The code keywords are as follows:
 * <ul>
 * <li>Track: The entire track, start to finish</li>
 * <li>Segment: Each portion of the track, separated by speed and individually
 * specified by the XML</li>
 * <li>Section: Each portion of a segment, Acceleration, Coast, and
 * Deceleration</li>
 * </ul>
 * </p>
 * 
 * <p>
 * Change log for this program:
 * <ul>
 * <li>9/13/2019: Start and finish of code adaptation. EZ</li>
 * <li>9/14/2019: Turned off debug more and cleaned up some comments! Almost all
 * of the code should come with an explanation of what it does! :) I also added
 * some HTML tags for some nice-ish JavaDocs!</li>
 * </ul>
 * </p>
 *
 * @author Jacob Loosa
 *
 */
public class Car {

    // Should we show debug in console?
    public static boolean debug = false;

    public static final float ACCELERATION = 15f; // Feet per second squared
    // Segments are of type Comparable<Segment>, so we can have Java handle putting
    // them in order automatically
    static List<Segment> segments;
    static float totalTrackTime;
    static float maxCarDelay = 0;

    // How far does the car wait before driving
    float delay;

    Car(float delay) {
	this.delay = delay;
	maxCarDelay = Math.max(maxCarDelay, delay);
    }

    /**
     * Get the speed of the car at a specific time
     * 
     * @param time
     * @return the speed in feet per second
     */
    float getSpeed(float time) {
	if (time <= delay)
	    return 0;
	time -= delay;
	return segments.get(0).getSpeed(time);
    }

    /**
     * Get the distance of the car at a specific time
     * 
     * @param time
     * @return the distance in feet
     */
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
	// I made them Comparable specifically so I could have Java put them in order
	Collections.sort(segments);
	if (segments.get(0).getSegmentNumber() != 1) {
	    System.err.println("Sorry, but the first segment is miraculously missing so a track cannot be formed.");
	    System.exit(1);
	}

	printDebug("Length of segment set is: " + segments.size());
	segments.forEach((s) -> Car.printDebug(s.toString()));
	totalTrackTime = segments.get(0).getSegmentTime();

	// This loop ensures that all of the segments can form a correct sequence
	for (int i = 1; i < segments.size(); i++) {
	    if (!segments.get(i - 1).setNextSegment(segments.get(i))) {
		if (segments.get(i).getSegmentNumber() < i+1) System.err.println("Error: Section #" + i + " is duplicated!");
		if (segments.get(i).getSegmentNumber() > i+1) System.err.println("Error: Section #" + (i+1) + " is missing!");
		if (segments.get(i).getSegmentNumber() == i+1) System.err.println("Error in linking segments!!");
		System.err.println("Failed to create segment ordering. Exiting.");
		System.exit(1);
	    }
	    segments.get(i).setPreviousSegment(segments.get(i - 1));
	}

	segments.get(0).computeKinematicsRecursively();
	segments.forEach((s) -> totalTrackTime += s.getSegmentTime());
	Car.printDebug("Time to complete track: " + totalTrackTime + "s");
	Car.printDebug("Track distance: " + Kinematics.toMiles(segments.get(segments.size() - 1).getRecursiveLength()) + "miles");

	Car carA = new Car(0);
	Car carB = new Car(60);
	Car carC = new Car(120);
	System.out.println("Time(s) \t \t Car A \t \t \t \t Car B \t \t \t \t Car C");
	System.out.println(" \t\t Speed \t Location \t Speed \t Location \t Speed \t Location");
	float currentTime = 0;
	float timeStep = 30;
	do {
	    System.out.printf("%06.1f \t %s \t \t %s \t \t %s\n", currentTime, carA.getPositionInfo(currentTime), carB.getPositionInfo(currentTime), carC.getPositionInfo(currentTime));
	    currentTime += timeStep;
	} while (currentTime <= totalTrackTime + maxCarDelay + timeStep); // This checks if all the cars are done (+1 print after). We can do this because
									  // we already know exactly how long it takes for the car to run the track!
    }

    public static void printDebug(String... output) {
	if (!debug)
	    return;
	for (String out : output)
	    System.out.printf("[DEBUG] %s \n", out);
    }

}
