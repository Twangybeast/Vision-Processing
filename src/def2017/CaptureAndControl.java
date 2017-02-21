package def2017;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.github.sarxos.webcam.Webcam;

public class CaptureAndControl
{
	public static void main(String[] args)
	{
		CaptureAndControl cac=new CaptureAndControl();
		cac.control();
	}
	public void control()
	{
		Webcam webcam=Webcam.getDefault();
		webcam.setViewSize(new Dimension(640, 480));
		webcam.open();
		BufferedImage image=webcam.getImage();
		PictureTester frame=new PictureTester(image);
		try
		{
			ImageIO.write(image, "png", new File("capture.png"));
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
		webcam.close();
	}
}
