package code2017;

import java.awt.Point;

public class Target 
{
	/*
	 * Format 17.0:
	 * 	-Coordinate system where (0,0) is the center of screen.
	 * 	-Angle is radians clockwise
	 * 	-Target distance in inches
	 */
	public double x;
	public double y;
	public double angle;
	public double distance;
	public boolean nullTarget=false;
	public Target(double x, double y, double angle, double distance)
	{
		this.x=x;
		this.y=y;
		this.angle=angle;
		this.distance=distance;
	}
	//Sets as NULL target
	public Target()
	{
		x = 2.0;
		y = 2.0;
		angle = 0.0;
		distance = 0.0;
		nullTarget=true;
	}
	public static Target getNullTarget()
	{
		Target nullTarget = new Target();
		return nullTarget;
	}
}
