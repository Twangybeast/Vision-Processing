package exampleModules;

import java.awt.image.BufferedImage;

import clientController.RobotModule;

public class RobotPISample 
{
	RobotModule rm;
	public RobotPISample()
	{
		RobotModule rm=new RobotModule();
		rm.init();
	}
	//Note: image1 is light off, image2 is light on
	public void moveToTarget()
	{
		double distance;
		double x;
		double y;
		double angle;
		//Magic
		//You can reverse the order of taking the images, but make sure to send with image2 as light on
		//If only one image is used, send image 1 as null
		while(true)
		{
			rm.process();
			while(true)
			{
				boolean finished=rm.waitToFinish(1000);//Should take less than 0.5 seconds, average 0.33
				if(finished)
				{
					break;
				}
			}
			double[] target=rm.getTarget();//Get the target, see format below
			if(target[4]==0.0)//Checks to make sure no failure
			{
				x=target[0];
				y=target[1];
				angle=target[2];
				distance=target[3];//Ask me if this is in inches or feet
				break;
			}
			if(target[4]==0.5)
			{
				x=target[0];
				y=target[1];
				angle=target[2];
				distance=target[3];//Ask me if this is in inches or feet
				//Partial Target Move Code. Ask for clarification.
				//TODO
			}
		}
		//End Magic
		//Move Code
		//See above for your variables.
		//TODO
	}
	public void ledLight(boolean on)
	{
		//Turns green LED on or off
		//TODO
	}
}
