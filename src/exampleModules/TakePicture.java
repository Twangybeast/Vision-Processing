package exampleModules;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

import javax.imageio.ImageIO;

import com.github.sarxos.webcam.Webcam;

public class TakePicture 
{
	public static void main(String[] args)
	{
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
		Webcam webcam=webcams.get(0);
		System.out.println("Webcam gotten: "+webcam.getName());
		/*
		Dimension[] sizes=webcam.getViewSizes();
		for(Dimension d: sizes)
		{
			System.out.printf("Possible size (w x h): [%d] x [%d]\n", d.width, d.height);
		}
		*/
		webcam.setViewSize(new Dimension(320, 240));
		webcam.open();
		System.out.println("Webcam opened.");
		Scanner s=new Scanner(System.in);
		s.nextLine();
		BufferedImage image=webcam.getImage();
		System.out.println("Image captured");
		try {
			ImageIO.write(image, "png", new File("capture.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
