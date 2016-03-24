import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Webcam {
	public BufferedImage getImage()
	{
		BufferedImage image = null;
		try{
			image = ImageIO.read(new File("testimage.jpg"));
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		
		return image;
	}
}
