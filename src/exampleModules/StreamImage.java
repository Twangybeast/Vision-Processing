package exampleModules;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.List;

import javax.imageio.ImageIO;

import com.github.sarxos.webcam.Webcam;

public class StreamImage implements Runnable
{
	ServerSocket serverSocket=null;
	Socket clientSocket=null;
	DataOutputStream out=null;
	DataInputStream in = null;
	public Webcam webcam=null;
	final static int portNumber = 5801;
	final static byte IMAGE_TITLE = 0x2e;
	long count=0;
	float fps=0.0f;
	public static void main(String[] args)
	{
		//Include webcam initialization
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
		StreamImage si = new StreamImage();
		si.initialize();
		si.webcam=webcam;
		Thread t = new Thread(si);
		t.start();
	}
	public void initialize()
	{
		openSockets();
	}
	public void exec(BufferedImage image)
	{
		long t1=System.currentTimeMillis();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try 
		{
			ImageIO.write(image, "jpeg", out);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		byte[] imageBytes = out.toByteArray();
		byte[] encodedBytes = new byte[imageBytes.length+5];
		ByteBuffer bb = ByteBuffer.wrap(encodedBytes);
		bb.put(IMAGE_TITLE);
		bb.putInt(imageBytes.length);
		bb.put(imageBytes);
		try 
		{
			this.out.write(encodedBytes);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		count++;
		System.out.printf("Send time: [%d]\t Image Number: [%d]\t FPS: [%f]\n", System.currentTimeMillis()-t1, count, fps);
	}
	private void openSockets()
	{
		try
		{
			serverSocket = new ServerSocket(portNumber);
			clientSocket = serverSocket.accept();
			System.out.println(clientSocket.getInetAddress().getHostAddress());
			out = new DataOutputStream(clientSocket.getOutputStream());
			in = new DataInputStream(clientSocket.getInputStream());
			byte[] introduction=new byte[4];
			in.read(introduction, 0, 4);
			out.write(introduction);
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	@Override
	public void run() 
	{
		float measurement = 0.0f;
		final float smoothing = 0.9f;
		while(true)
		{
			long t1=System.currentTimeMillis();
			BufferedImage bi=webcam.getImage();
			System.out.printf("Image Capture Time: [%d]ms", System.currentTimeMillis()-t1);
			exec(bi);
			try 
			{
				Thread.sleep(Math.max(0, 20-(System.currentTimeMillis()-t1)));
			} 
			catch (InterruptedException e) 
			{
				e.printStackTrace();
			}
			measurement = (measurement * smoothing) + ((System.currentTimeMillis() - t1) * (1.0f - smoothing));
			fps = ReadStream.round(1000.0f / measurement, 2);
		}
	}
}
