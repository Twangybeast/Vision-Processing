package robot;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.ds.v4l4j.V4l4jDriver;

public class WebcamTest 
{
	Webcam webcam=null;
	public static void main(String[] args)
	{
		WebcamTest wt=new WebcamTest();
		wt.init();
	}
	public void init()
	{
		System.out.println("Trying to open webcam...");
		//Webcam.setDriver(new V4l4jDriver());
		webcam=Webcam.getDefault();
		webcam.setViewSize(new Dimension(640, 480));
		webcam.open();
		System.out.println("Webcam successfully opened.");
	}
	public BufferedImage getImage()
	{
		return webcam.getImage();
	}
	public byte[] compressImageBytes(BufferedImage image)
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try 
		{
			ImageIO.write(image, "png", out);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		byte[] bytes=out.toByteArray();
		return bytes;
	}
	public BufferedImage extractImageFromBytes(byte[] bytes)
	{
		BufferedImage image=null;
		try
		{
			image=ImageIO.read(new ByteArrayInputStream(bytes));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return image;
	}
}
