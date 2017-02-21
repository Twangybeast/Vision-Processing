package exampleModules;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamException;
import com.github.sarxos.webcam.ds.v4l4j.V4l4jDriver;

import clientController.Client;
import code2017.Target;
import code2017.Vision17;

public class PISample 
{
	Client client;
	Webcam webcam=null;
	final static String PATH = "D:"+File.separator+"images"+File.separator;
	public static void main(String[] args) 
	{
		Webcam webcam=Webcam.getDefault();
		webcam.setViewSize(new Dimension(640, 480));
		webcam.open();
		webcam.getImage();
		PISample pi=new PISample();
		pi.init();
		try 
		{
			ImageIO.write(pi.webcam.getImage(), "png", new File(PISample.PATH+"test.png"));
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		//pi.exec();
	}
	public void init()
	{
		//client=new Client();
		//client.init();
		//Webcam.setDriver(new V4l4jDriver());
		webcam=Webcam.getDefault();
		webcam.setViewSize(new Dimension(640, 480));
		webcam.open();
	}
	public void exec()
	{
		while(true)
		{
			if(client.waitToProcess(1000))//This number doesn't matter
			{
				Vision17 v=new Vision17();
				BufferedImage image1=null;
				BufferedImage image2=webcam.getImage();
				v.setImage(image1, image2);
				Target target=v.exec();
				client.transmitTarget(target);
				long t=System.currentTimeMillis();
				//saveImage(image1, "image1."+t);
				saveImage(image2, "image2."+t);
			}
			//Don't need to wait since the waitToProcess already does
		}
	}
	public void saveImage(BufferedImage image, String name)
	{
		File folder=new File(PATH);
		try
		{
			folder.mkdir();
			ImageIO.write(image, "jpg", new File(PATH+name+".jpg"));
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
