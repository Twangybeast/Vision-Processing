package code2017;

import com.github.sarxos.webcam.Webcam;

public class TestPICode 
{
	public static void main(String[] args)
	{
		Vision17 vision = new Vision17();
		Webcam webcam=Webcam.getDefault();
		webcam.open();
		vision.setImage(webcam.getImage());
		Target target = vision.exec();
		System.out.printf("\nCoordinates: (%f, %f) \nAngle: [%f] \nDistance: [%f]\n", target.x, target.y, target.angle, target.distance);
	}
}
