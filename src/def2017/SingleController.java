package def2017;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import code2017.DownloadImages;

public class SingleController 
{
	String imagePath="C:\\Users\\Twangybeast\\workspace\\capture.png";
	public static void main(String[] args)
	{
		SingleController sc=new SingleController();
		sc.control();
	}
	public void control()
	{
		PictureTester frame=new PictureTester(getImage(new File(imagePath)));
		
	}
	public static BufferedImage getImage(File imageFile)
	{
		BufferedImage image=null;
		File file=imageFile;
		if(!file.exists())
		{
			System.out.println("WARNING: File does not exist. Exiting...");
			System.exit(1);
		}
		try
		{
			image=ImageIO.read(file);
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return image;
	}
}
