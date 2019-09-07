package program1;

import processing.core.PApplet;

public class ProcessingVisualizer extends PApplet {

    public static void main(String[] args) {
	PApplet.main(ProcessingVisualizer.class);
    }

    Car car;
    float timeStart = 0, timeEnd = 375;
    float secondResolution, screenPadding = 15;
    float timePerSegment = 10f;
    float segmentCount;
    float segmentDistance;

    @Override
    public void settings() {
	size(1200, 300);
	noLoop();
    }

    @Override
    public void setup() {
	car = new Car(0);
	secondResolution = (width - 2 * screenPadding) / (timeEnd - timeStart + 1);
	segmentCount = (timeEnd - timeStart) / timePerSegment;
	segmentDistance = (width - 2 * screenPadding) / segmentCount;
    }

    @Override
    public void draw() {
	translate(0, 0);
	background(230);
	noFill();
	strokeWeight(2);
	beginShape();
	for (float second = timeStart; second <= timeEnd; second++) {
	    vertex(screenPadding + second * secondResolution,
		    map(car.getSpeed(second), min(Car.stage1Speed, Car.stage2Speed, Car.stage3Speed),
			    max(Car.stage1Speed, Car.stage2Speed, Car.stage3Speed), height - screenPadding,
			    screenPadding));
	}
	endShape();
	strokeWeight(1);
	stroke(255, 0, 0, 128);
	for (int i = 0; i <= segmentCount; i++)
	    line(screenPadding + segmentDistance * i, 0, screenPadding + segmentDistance * i, height);
    }

}
