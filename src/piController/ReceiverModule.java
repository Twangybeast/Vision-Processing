package piController;

import java.awt.image.BufferedImage;

import edu.wpi.first.wpilibj.networktables.NetworkTable;

//This Code is intended to be on the main robot, DO NOT use on Raspberry PI
public class ReceiverModule
{
	private boolean initialized=false;
	private NetworkTable table=null;
	public void init()
	{
		table=NetworkTable.getTable("Vision2017Team2976ID0119");
		
		initialized=true;
	}
	public double[] getTarget()
	{
		checkInit();
		double[] target= new double[4];
		target[0]=table.getNumber("XPosition", Double.NaN);
		target[1]=table.getNumber("YPosition", Double.NaN);
		target[2]=table.getNumber("TargetAngle", Double.NaN);
		target[3]=table.getNumber("TargetDistance", Double.NaN);
		return target;
	}
	public BufferedImage getImage()
	{
		checkInit();
		BufferedImage image=ImageByteConverter.getBytesToImage(table.getRaw("Image", null));
		return image;
	}
	private void checkInit()
	{
		if(!initialized)
		{
			System.out.printf("WARNING: Not initialized. Automatically initializing...\n");
			init();
			System.out.printf("INFO: Initialized\n");
		}
	}
}
