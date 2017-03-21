import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import com.github.sarxos.webcam.Webcam;

public class AutomaticImages {
	public static void main(String[] args) throws InterruptedException
	{
		Webcam webcam;
		System.out.println("Getting webcams");
		List<Webcam>  webcams=Webcam.getWebcams();
		System.out.println("Got webcams");
		if(webcams.size()==0)
		{
			System.out.println("No webcams found. Exiting...");
			System.exit(1);
		}
		for(Webcam cam: webcams)
		{
			System.out.println("Webcam found: "+cam.getName());
		}
		webcam=webcams.get(2);
		System.out.println("Webcam gotten: "+webcam.getName());
		webcam.setViewSize(new Dimension(320, 240));
		webcam.open();
		System.out.println("Webcam opened.");
		Thread.sleep(5000);
		System.out.println("Done Sleeping");
		final int sleepInterval=1000;
		for(int i=0;i<50;i++)
		{
			saveImage(webcam.getImage(), i);
			System.out.printf("Image Number [%d] taken. Waiting for [%d] ms\n", i, sleepInterval);
			Thread.sleep(1000);
		}
		System.out.println("Completed image taking.");
	}
	public static void saveImage(BufferedImage image, int i)
	{
		try 
		{
			ImageIO.write(image, "png", new File("images\\image"+i+".png"));
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
}
