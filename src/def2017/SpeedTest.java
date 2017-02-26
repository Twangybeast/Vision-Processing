package def2017;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import code2017.Vision17;

public class SpeedTest
{
	static String imageFile="D:\\Downloads\\image (2).png";
	public static void main(String[] args)
	{
		try
		{
			BufferedImage image=ImageIO.read(new File("image.png"));
			Vision17 v=new Vision17();
			v.setImage(null, image);
			v.exec();
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
