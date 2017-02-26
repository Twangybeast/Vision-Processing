package edgedetector;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import code2017.Particle;
import code2017.Vision17;

public class EdgeDetector
{
	float[][] map=null;
	Tasker[] taskers;
	Thread[] threads;
	Rectangle[] regions=null;
	private boolean initialized;
	private ArrayList<Particle> edges=null;
	private float[][] mag=null;
	public EdgeDetector(int numOfThreads)
	{
		this.threads=new Thread[numOfThreads];
		initialized=false;
	}
	public void init(float[][] map)
	{
		this.map=map;
		initialized=true;
	}
	public void execSingle()
	{
		if(!initialized)
		{
			System.out.println("WARNING: EdgeDetector not initialized. Imminent errors.");
		}
		long t1=System.currentTimeMillis();
		Rectangle full=new Rectangle(0, 0, map[0].length, map.length);
		long t2=System.currentTimeMillis();
		float[][] map_smoothed=Conv.conv5x5(map, Conv.GAUSS);
		//float[][] map_smoothed=Conv.conv(map, Conv.GAUSS, full);
		System.out.printf("Edge Detector - Smoothen Time: [%d] ms\n", (System.currentTimeMillis()-t2));
		t2=System.currentTimeMillis();
		/*
		full  = new Rectangle(1, 1, map[0].length, map.length);
		map_smoothed=Conv.placeDefaultValues(map_smoothed, 0.0f);
		float[][] dx=Conv.convFree(map_smoothed, Conv.SOBEL_X, full);
		float[][] dy=Conv.convFree(map_smoothed, Conv.SOBEL_Y, full);
		*/
		full=new Rectangle(1, 1, map[0].length-2, map.length-2);
		float[][] dx=Conv.convFree3x3(map_smoothed, Conv.SOBEL_X, full);//, 0.0f);
		float[][] dy=Conv.convFree3x3(map_smoothed, Conv.SOBEL_Y, full);//, 0.0f);
		System.out.printf("Edge Detector - Derivative Time: [%d] ms\n", (System.currentTimeMillis()-t2));
		t2=System.currentTimeMillis();
		full = new Rectangle(0, 0, map[0].length-2,map.length-2);
		mag=Conv.magnitude(dx, dy, full);
		System.out.printf("Edge Detector - Magnitude Time: [%d] ms\n", (System.currentTimeMillis()-t2));
		System.out.printf("Canny time: [%d]ms\n", (System.currentTimeMillis()-t1));
		
		t1=System.currentTimeMillis();
		boolean[][] validEdgePoints=EdgeThinner.findValidEdges(mag, dx, dy);
		System.out.printf("Valid edge generation time: [%d]ms\n", (System.currentTimeMillis()-t1));
		t1=System.currentTimeMillis();
		ArrayList<Particle> edges=EdgeAlgorithm.findEdges(mag, validEdgePoints);
		System.out.printf("Edge find time: [%d]\n", (System.currentTimeMillis()-t1));
		
		//edges=EdgeThinner.thinEdge(edges, mag, dx, dy);
		//System.out.printf("Edge thin generation time: [%d]\n", (System.currentTimeMillis()-t1));
		this.edges=edges;
	}
	public void exec()
	{
		if(!initialized)
		{
			System.out.println("WARNING: EdgeDetector not initialized. Imminent errors.");
		}
		regions=Conv.divideRegions(map, threads.length);
		long t1=System.currentTimeMillis();
		long t2;
		prepareThreads(Assignment.SMOOTHEN, map);
		t2=System.currentTimeMillis();
		//runThreads();
		System.out.printf("Smoothen wait time: [%d]ms\n", System.currentTimeMillis()-t2);
		waitForThreads();
		float[][] map_smoothed=getResult();
		System.out.printf("Smoothen total time: [%d]ms\n", System.currentTimeMillis()-t1);
		
		prepareThreads(Assignment.XDERIV, map_smoothed);
		//runThreads();
		waitForThreads();
		float[][] dx=getResult();
		
		prepareThreads(Assignment.YDERIV, map_smoothed);
		//runThreads();
		waitForThreads();
		float[][] dy=getResult();
		
		prepareThreads(Assignment.MAGNITUDE, dx, dy);
		//runThreads();
		waitForThreads();
		mag=getResult();
		System.out.println("Canny Time: \t   "+(System.currentTimeMillis()-t1));
		
		t1=System.currentTimeMillis();
		boolean[][] validEdgePoints=EdgeThinner.findValidEdges(mag, dx, dy);
		System.out.printf("Valid edge generation time: [%d]\n", (System.currentTimeMillis()-t1));
		t1=System.currentTimeMillis();
		ArrayList<Particle> edges=EdgeAlgorithm.findEdges(mag, validEdgePoints);
		System.out.printf("Edge find time: [%d]\n", (System.currentTimeMillis()-t1));
		this.edges=edges;
	}
	private void prepareThreads(Assignment assignment, float[][] input)
	{
		prepareThreads(assignment, input, null);
	}
	private void prepareThreads(Assignment assignment, float[][] input, float[][] input2)
	{
		taskers=new Tasker[threads.length];
		for(int i=0;i<threads.length;i++)
		{
			taskers[i]=new Tasker(assignment, regions[i]);
			taskers[i].input=input;
			taskers[i].input2=input2;
			threads[i]= new Thread(taskers[i]);
			threads[i].run();
		}
	}
	private void runThreads()
	{
		for(int i=0;i<threads.length;i++)
		{
			threads[i].run();
		}
	}
	private void waitForThreads()
	{
		for(int i=0;i<threads.length;i++)
		{
			try
			{
				threads[i].join();
			} 
			catch (InterruptedException e)
			{
				System.err.println("Thread interrupted, result likely invalid.");
				e.printStackTrace();
			}
		}
	}
	private float[][] getResult()
	{
		float[][][] results=new float[threads.length][][];
		for(int i=0;i<threads.length;i++)
		{
			results[i]=taskers[i].result;
		}
		return Conv.combineResults(results);
	}
	public ArrayList<Particle> getEdges()
	{
		return edges;
	}
	public float[][] getMag()
	{
		return mag;
	}
	private static void saveMag(float[][] mag)
	{
		BufferedImage image=new BufferedImage(mag[0].length, mag.length, BufferedImage.TYPE_INT_RGB);
		Graphics g=image.getGraphics();
		for(int i=0;i<mag.length;i++)
		{
			for(int j=0;j<mag[0].length;j++)
			{
				Color c=Color.BLACK;
				if(mag[i][j]>60)
				{
					if(mag[i][j]>=100)
					{
						c=Color.BLUE;
					}
					else
					{
						c=Color.YELLOW;
					}
				}
				g.setColor(c);
				g.fillRect(j, i, 1, 1);
			}
		}
		try
		{
			ImageIO.write(image, "png", new File("magS.png"));
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
