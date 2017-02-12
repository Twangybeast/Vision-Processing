package piController;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

import edu.wpi.first.wpilibj.networktables.*;

public class Transmitter
{
	private boolean initialized= false;
	private NetworkTable table=null;
	public String ipAddress = "";
	public void init()
	{
		NetworkTable.setClientMode();
		findIP();
		NetworkTable.setIPAddress(ipAddress);
		table = NetworkTable.getTable("Vision2017Team2976ID0119");
		initialized=true;
	}
	public void transmitTarget(double x, double y, double angle, double distance)
	{
		checkInit();
		table.putNumber("XPosition", x);
		table.putNumber("YPosition", y);
		table.putNumber("TargetAngle", angle);
		table.putNumber("TargetDistance", distance);
	}
	public void transmitImage(BufferedImage image)
	{
		checkInit();
		transmitImage(image, "Image");
	}
	public void transmitImage(BufferedImage image, String key)
	{
		checkInit();
		transmitBytes(ImageByteConverter.getImageToBytes(image), key);
	}
	public void trasnmitBytes(byte[] bytes)
	{
		checkInit();
		transmitBytes(bytes, "Bytes");
	}
	public void transmitBytes(byte[] bytes, String key)
	{
		checkInit();
		table.putRaw(key, bytes);
	}
	public BufferedImage getImage()
	{
		checkInit();
		BufferedImage image=ImageByteConverter.getBytesToImage(table.getRaw("Image", null));
		return image;
	}
	//Module to find IP
	public String findIP()
	{
		
		return ipAddress;
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
