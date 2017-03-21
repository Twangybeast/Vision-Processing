package exampleModules;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Scanner;

//Run this on the robot
public class ServerExample
{
	Socket socket=null;
	DataOutputStream out=null;
	DataInputStream in=null;
	ServerSocket serverSocket=null;
	Socket clientSocket=null;
	static final String hostName=
			//"169.254.139.222";
			"10.29.76.68";
	static final int portNumber=1735;
	static final byte[] INTRODUCTION = 
		{
			0x63,
			0x6a,
			0x10,
			0x28
		};
	static final byte[] REQUEST = 
		{
			0x77,
		};
	static final byte TARGET_ID = 0x3A;
	//Return variables
	double x=0.0;
	double y=0.0;
	double angle=0.0;
	double distance=0.0;
	boolean nullTarget=true;
	boolean singleTarget=true;
	public static void main(String[] args)
	{
		ServerExample ce=new ServerExample();
		ce.initialize();
		Scanner s=new Scanner(System.in);//Only use for checking connection
		//while(true)
		{
			s.nextLine();//Wait until input. Testing code, don't actually need
			ce.exec();
			System.out.printf("Target Found\nX: [%f]\n Y: [%f]\nAngle: [%f]\nDistance: [%f]\n", ce.x, ce.y, ce.angle, ce.distance);
			if(ce.nullTarget)
			{
				System.out.println("Null target");
			}
			if(ce.singleTarget)
			{
				System.out.println("Single target");
			}
		}
		//ONLY CLOSE WHEN YOU ARE FOR SURE DONE! Doing so will terminate the connection on the server side as well, which won't recreate the connection
		ce.close();
	}
	public void initialize()
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
			
		} catch (UnknownHostException e)
		{
			e.printStackTrace();
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	public void exec()
	{
		try
		{
			out.write(REQUEST);
			byte[] target=new byte[34];
			/*
			 * Byte format: 34 bytes total
			 * 0 	: Identifies information as a target
			 * 1 	: Identifies target type (0x00 is null) (0x11 is single) (0x22 is complete)
			 * 2-9 	: Double representing x
			 * 10-17: Double representing y
			 * 18-25: Double representing angle
			 * 26-33: Double representing distance
			 */
			in.read(target, 0, target.length);
			ByteBuffer bb=ByteBuffer.wrap(target);
			if(bb.get()==TARGET_ID)
			{
				switch(bb.get())
				{
					case 0x11:
						nullTarget=false;
						singleTarget=true;
						break;
					case 0x22:
						nullTarget=false;
						singleTarget=false;
						break;
					case 0x00:
					default://Same behavior as null target
						nullTarget=true;
						singleTarget=false;
						x = 2.0;
						y = 2.0;
						distance = 0.0;
						angle = 0.0;
						return;
				}
				x=bb.getDouble();
				y=bb.getDouble();
				angle=bb.getDouble();
				distance=bb.getDouble();
			}
			else
			{
				System.out.println("Target ID not verified. Trying again...");
				exec();//Try again, idk what went wrong
			}
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	public void close()
	{
		try
		{
			socket.close();
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
		out=null;
		in=null;
	}
}
