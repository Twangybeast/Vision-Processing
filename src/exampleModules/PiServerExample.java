package exampleModules;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.List;

import javax.imageio.ImageIO;

import com.github.sarxos.webcam.Webcam;

import code2017.Target;
import code2017.Vision17;

//NOTE: Terminate client program prior to terminating this program. 
public class PiServerExample
{
	static final boolean enableSocketRefinding=false;
	ServerSocket serverSocket=null;
	Socket clientSocket=null;
	DataOutputStream out=null;
	DataInputStream in=null;
	Webcam webcam=null;
	BufferedImage capture;
	static final int portNumber=1735;
	static final byte[] REQUEST = 
		{
			0x77
		};
	static final byte TARGET_ID = 0x3A;
	public static void main(String[] args)
	{
		PiServerExample pie=new PiServerExample();
		System.out.println("Test");
		pie.initialize();
		pie.exec();
	}
	/*
	 * Process
	 * Robot <-> Pi 	Establish connection, send introductory packet that verifies integrity.
	 * 
	 * Robot 			Does stuff until vision is needed
	 * Pi				Wait for signal
	 * Robot -> Pi		Send request signal.
	 * Pi				Take picture and calculate information
	 * Robot 			Wait for completion
	 * Pi -> Robot		Send all information in single array
	 * 					Repeat
	 */
	public void initialize()
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
		webcam=webcams.get(0);
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
		openSockets();
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
	public void exec()
	{
		int failConsecutive=0;
		while(true)
		{
			byte[] req=new byte[1];
			try
			{
				if(in.read(req, 0, req.length)>0)
				{
					if(req[0]==REQUEST[0])
					{
						System.out.println("Request received.");
						Target target=process();
						System.out.println("Target gotten.");
						byte[] bytes=new byte[34];
						ByteBuffer bb=ByteBuffer.wrap(bytes);
						bb.put(TARGET_ID);
						byte targetType;
						if(target.singleTarget)
						{
							targetType=0x11;
						}
						else
						{
							targetType=0x22;
						}
						if(target.nullTarget)
						{
							targetType=0x00;
						}
						bb.put(targetType);
						bb.putDouble(target.x);
						bb.putDouble(target.y);
						bb.putDouble(target.angle);
						bb.putDouble(target.distance);
						out.write(bytes, 0, bytes.length);
						System.out.println("Target sent.");
						saveImage();
					}
				}
				else
				{
					System.out.println("Got [-1] when reading from bytes, breaking from loop and closing webcam.");
					break;
				}
				failConsecutive=0;
			} 
			catch (IOException e)
			{
				failConsecutive++;
				e.printStackTrace();
				if(failConsecutive>3)
				{
					System.out.println("Too many errors. Exiting loop...");
					break;
				}
			}
		}
		if(enableSocketRefinding)
		{
			openSockets();
			exec();
		}
		webcam.close();
	}
	public Target process()
	{
		Vision17 v=new Vision17();
		capture =webcam.getImage();
		v.setImage(null, capture);
		Target target=v.exec();
	
		return target;
		//return new Target(0.1, -0.1, 0.005, 20.0);
		//return new Target();
	}
	public void saveImage()
	{
		try 
		{
			ImageIO.write(this.capture, "png", new File("capture.png"));
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
}
