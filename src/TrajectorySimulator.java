import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import javax.swing.JComponent;
import javax.swing.JFrame;

public class TrajectorySimulator  {


	
	private static double timer = System.currentTimeMillis();
	
	// Trajectory Algorithm (in cm) ***VALUES ARE NOT CURRENTLY ACCURATE***
	private static double changeInTime; /* The time (in seconds) added to the time at every
								  		 * iteration, can also be considered the
								  		 * resolution of the algorithm where
								  		 * resolution is indirectly proportional
								  		 * to this number
								  		 */
	
	private static double shooterX; /* The distance of the shooter
									 * (specifically the point at which the ball leaves it)
									 * to the left wall of the field (in relation to the robot
									 */
	private static double shooterY; /* The distance of the shooter
						   			 * (specifically the point at which the ball leaves it)
						   			 * to the ground.
						   			 */
	private static double shooterZ; /* The distance of the shooter
						  			 * (specifically the point at which the ball leaves it)
						  			 * to the left wall of the field (in relation to the robot
						  			 */
	
	private static double goalX; /* The distance of the goal
				 				  * (specifically the point at which the ball leaves it)
				 				  * to the left wall of the field (in relation to the robot
				 				  */
	private static double goalY; /* The distance of the goal
				   				  * (specifically the point at which the ball leaves it)
				   				  * to the ground.
				   				  */
	private static double goalZ; /* The distance of the goal
						 		  * (specifically the point at which the ball leaves it)
						 		  * to the left wall of the field (in relation to the robot
						 		  */
	
	// The distance between the shooter and the goal (forward and backward axis)
	private static double distGoalX;

	// The distance between the shooter and the goal (up and down axis)
	private static double distGoalY;

	private static double angleOfShooter; // In degrees
	private static double velocity; // the velocity of the ball (in cm/s)
	private static double velocityX;
	private static double velocityY;
	private static double time; // time (in seconds) since the ball has been launched
	private static double x; // the position of the ball relative to its start (x is forward and backward)
	private static double y; // the position of the ball relative to its start (y is up and down)
	private static double accelerationX; // the acceleration of the ball where forward is positive (in cm/s)
	private static double accelerationY; // the acceleration of the ball where upward is positive (in cm/s)
	private static double ballWeight; // Ball weight (in g)
	private static double ballArea; // Area of ball's silhouette (in square centimeters)
	private static double airDensity; // Density of air (in g/[cube centimeters])
	private static double dragCoefficient; // Drag coefficient of a sphere (no unit)
	private static double dragConstant; // D, drag constant
	
	// These are for resetting values
	private static double initialVelocity;
	
	// These are used for analyzing the values
	private static double[] bestAttemptValues;
	private static double beforePreviousAngle;
	private static double previousAngle;
	private static double angleRateOfChange;
	private static double minDistance;
	private static double previousDistance;
	private static boolean distanceDecreasing;
	
	private static Random rand;
	
	private static int width;
	private static int height;
	private static JFrame frame = new JFrame();
	
	
	
	public static void main(String[] args) throws InterruptedException {
		width = 1600;
		height = 500;
		frame.setSize(width, height);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		changeInTime = 0.005;
		shooterX = 451;
		shooterY = 10;
		shooterZ = 350;		
		goalX = 451;
		goalY = 241.3;
		goalZ = 520;
		distGoalX = Math.sqrt(Math.pow(goalX - shooterX, 2) + Math.pow(goalZ - shooterZ, 2));
		distGoalY = goalY - shooterY;
		frame.add(new goalDrawing(distGoalX, distGoalY, 20, 88.9));
		frame.setVisible(true);
		angleOfShooter = 10;
		velocity = 1000;
		velocityX = velocity * Math.cos(Math.toRadians(angleOfShooter));
		velocityY = velocity * Math.sin(Math.toRadians(angleOfShooter));
		time = 0;
		x = 0;
		y = 0;
		ballWeight = 295;
		ballArea = 506.71;
		airDensity = 0.001225;
		dragCoefficient = 0.47;
		dragConstant = (airDensity * ballArea * dragCoefficient) / 2;
		initialVelocity = velocity;
		rand = new Random();
		minDistance = -1;
		timer = System.currentTimeMillis();
		bestAttemptValues = new double[2];
		beforePreviousAngle = -1;
		previousAngle = -1;
		angleRateOfChange = 0.5;
		previousDistance = -1;
		distanceDecreasing = true;
		
		for (int i = 0; true; i++) {
			
			initialVelocity = 1642;
			boolean velocityTuned = false;
			boolean foundValue = true;
			for (int j = 0; !velocityTuned && foundValue; j++) {
				
				// TODO: Calculate actual starting position of ball (changes with angle of shooter)
				
				// Calculate trajectory of ball until y-velocity is 0 or less
				calculateTajectory(false, false);
				
				
				
				// Change initial velocity based on guessed error
				if (x > distGoalX + 5) {
					initialVelocity -= x - distGoalX;
				} else if (x < distGoalX - 2) {
					initialVelocity += distGoalX - x - 0.25;
				} else {
					if (initialVelocity <= 1700) velocityTuned = true;
				}
				if (j > 100 || (j > 10 && initialVelocity > 1700)) foundValue = false;
			}
			if (foundValue) {
				double guessDistance = Math.sqrt(Math.pow(distGoalX - x, 2) + Math.pow(distGoalY - 44.45 + 13 - y, 2));
				if (guessDistance < minDistance || minDistance == -1) {
					minDistance = guessDistance;
					bestAttemptValues[0] = angleOfShooter;
					bestAttemptValues[1] = initialVelocity;
//					System.out.println("[Angle, Velocity]: " + "[" + angleOfShooter + ", " + velocity + "]");
//					System.out.println("[x, y]: " + "[" + x + ", " + y + "]");
				}
//				System.out.println("ANGLE, DISTANCE: " + angleOfShooter + ", " + guessDistance);
//				Thread.sleep(300);
//				point(angleOfShooter, guessDistance + 200, new Color(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255)));
				if (guessDistance > 3) {
					
					double initialAngle = angleOfShooter;
					if (distanceDecreasing) {
						angleOfShooter += 5;
						if (guessDistance - previousDistance > 0 && previousDistance != -1) {
							angleOfShooter = beforePreviousAngle;
							distanceDecreasing = false;
						}
						previousDistance = guessDistance;
					} else {
						int sign = 1;
						if (angleOfShooter < 0 || guessDistance > previousDistance) sign = -1;
						if (guessDistance > 30) angleRateOfChange = 2 * sign;
						else if (guessDistance > 20) angleRateOfChange = 1.5 * sign;
						else if (guessDistance > 10) angleRateOfChange = 1 * sign;
						else if (guessDistance > 7) angleRateOfChange = 0.5 * sign;
						angleOfShooter += angleRateOfChange;
					}
					
					beforePreviousAngle = previousAngle;
					previousAngle = initialAngle;
				} else break;
			} else angleOfShooter += 0.1;
			if (i > 200) break;
		}
		System.out.println("Time (in milliseconds): " + (System.currentTimeMillis() - timer));
		if (minDistance == -1)
			System.out.println("Velocity would need to be higher than shooter can handle.");
		else {
			angleOfShooter = bestAttemptValues[0];
			initialVelocity = bestAttemptValues[1];
			System.out.println("Distance from goal: " + minDistance);
			calculateTajectory(true, false);
		}
	}
	
	private static void calculateTajectory(boolean plotPoints, boolean logValues) {

		x = 0;
		y = 0;
		velocity = initialVelocity;
		velocityX = velocity * Math.cos(Math.toRadians(angleOfShooter));
		velocityY = velocity * Math.sin(Math.toRadians(angleOfShooter));
		time = 0;
		Color color = new Color(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255));
		
		for (; velocityY > 0; time += changeInTime) { // This is the target condition
			if (logValues) {
				System.out.println("x: " + x);
				System.out.println("y: " + y);
				System.out.println("velocityX: " + velocityX);
				System.out.println("velocityY: " + velocityY);
				System.out.println("time: " + time);
				System.out.println();
			}
			if (plotPoints)	point(x, y, color);
			
			accelerationX = -(dragConstant / ballWeight) * velocity * velocityX;
			accelerationY = -980 - ((dragConstant / ballWeight) * velocity * velocityY);
			velocityX += accelerationX * changeInTime;
			velocityY += accelerationY * changeInTime;
			velocity = Math.sqrt(Math.pow(velocityX, 2) + Math.pow(velocityY, 2));
			x += (velocityX * changeInTime) + (0.5 * accelerationX * Math.pow(changeInTime, 2));
			y += (velocityY * changeInTime) + (0.5 * accelerationY * Math.pow(changeInTime, 2));
			
		}
	}
	
	private static void point(double x, double y, Color color) {
		frame.add(new mirrorPoint(x, y, color));
		frame.setVisible(true);
	}
}

class goalDrawing extends JComponent {

	private static final long serialVersionUID = -6725559481831399216L;
	
	private double x, y, width, height;
	
	public goalDrawing(double x, double y, double width, double height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	
	public void paintComponent(Graphics g) {
		g.setColor(Color.BLUE);
		g.fillRect((int) Math.round(x), (int) Math.round(-y + getHeight()), (int) Math.round(width), (int) Math.round(height));
	}
	
}

// Credit to Daniel Wart of Prospect Robotics 2813 for the code below
class mirrorPoint extends JComponent{
	
	private static final long serialVersionUID = 3714765966168198206L;
	
	private double x;
	private double y;
	private Color color;
	
	public mirrorPoint(double x, double y, Color color){
		this.x = x;
		this.y = y;
		this.color = color;
	}
	
	public void paintComponent(Graphics g){

		double[] ellipsePos = {this.x,this.y};
		double[] ellipseSize = {10,10};
		double[] flippedCoords = flipXY(ellipsePos[0] - ellipseSize[0]/2, ellipsePos[1] - ellipseSize[1]/2, getWidth() , getHeight());
		g.setColor(this.color);
		((Graphics2D) g).fill(new Ellipse2D.Double(flippedCoords[0], flippedCoords[1], ellipseSize[0], ellipseSize[1]));

	}
	
	private double[] flipXY(double x, double y,int width,int height){
		
		return new double[] {x,-y+height};
		
	}
	
}
