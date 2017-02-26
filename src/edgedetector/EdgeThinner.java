package edgedetector;

import java.util.ArrayList;

import code2017.Particle;

public class EdgeThinner
{
	public static boolean evaluateEdgePoint(GradientAngle angle, float[][] local)//Returns true if point is edge
	{
		switch(angle)
		{
			case GA0:
				if(local[1][1]>local[1][0]&&local[1][1]>local[1][2])
				{
					return true;
				}
				else
				{
					return false;
				}
			case GA90:
				if(local[1][1]>local[0][1]&&local[1][1]>local[2][1])
				{
					return true;
				}
				else
				{
					return false;
				}
			case GA135:
				if(local[1][1]>local[0][0]&&local[1][1]>local[2][2])
				{
					return true;
				}
				else
				{
					return false;
				}
			case GA45:
				if(local[1][1]>local[0][2]&&local[1][1]>local[2][0])
				{
					return true;
				}
				else
				{
					return false;
				}
		}
		System.out.println("WARNING: Code improperly made. Unreachable position reached. Location: GradientAngle switch statement.");
		return false;
	}
	public static Particle thinEdge(Particle particle, float[][] mag, float[][] dx, float[][] dy)
	{
		for(int x=0;x<particle.getWidth();x++)
		{
			for(int y=0;y<particle.getHeight();y++)
			{
				if(particle.getLocalValue(x, y))
				{
					int x1=(int) (x+particle.getX());
					int y1=(int) (y+particle.getY());
					if(!evaluateEdgePoint(GradientAngle.getAngle(dx[y1][x1], dy[y1][x1]), EdgeThinner.getLocalArray(mag, x1, y1)))
					{
						particle.setLocalValue(x, y, false);
					}
				}
			}
		}
		particle.shorten();
		return particle;
	}
	public static ArrayList<Particle> thinEdge(ArrayList<Particle> particles, float[][] mag, float[][] dx, float[][] dy)
	{
		for(int i=0;i<particles.size();i++)
		{
			particles.set(i, thinEdge(particles.get(i), mag, dx, dy));
		}
		ArrayList<Particle> newList=new ArrayList<Particle>();
		for(int i=0;i<particles.size();i++)
		{
			ArrayList<Particle> parts=EdgeAlgorithm.findEdges(particles.get(i));
			switch(parts.size())
			{
				case 0:
					System.out.println("WARNING: no particles found in normal particle, could be good or bad particle");
					break;
				case 1:
					newList.add(particles.get(i));
					break;
				default:
					newList.addAll(parts);
					break;
			}
		}
		return newList;
	}
	public static boolean[][] findValidEdges(float[][] mag, float[][] dx, float[][] dy)
	{
		boolean[][] edges=new boolean[mag.length][mag[0].length];
		for(int i=0;i<mag.length;i++)
		{
			for(int j=0;j<mag[0].length;j++)
			{
				if(mag[i][j]>EdgeAlgorithm.THRESHOLD_LOW)
				{
					if(evaluateEdgePoint(GradientAngle.getAngle(dx[i][j], dy[i][j]), EdgeThinner.getLocalArray(mag, j, i)))
					{
						edges[i][j]=true;
					}
				}
			}
		}
		return edges;
	}
	public static float[][] getLocalArray(float[][] mag, int x, int y)
	{
		float[][] local=new float[3][3];
		for(int i=0;i<3;i++)
		{
			for(int j=0;j<3;j++)
			{
				int x1=x+j-1;
				int y1=y+i-1;
				local[i][j]=getFloatValue(mag, y1, x1, 0.0f);
			}
		}
		return local;
	}
	public static float getFloatValue(float[][] array, int i, int j, float defaultValue)
	{
		if(array==null)
		{
			return defaultValue;
		}
		if(array.length<1)
		{
			return defaultValue;
		}
		if(i<0||j<0)
		{
			return defaultValue;
		}
		if(i>=array.length||j>=array[0].length)
		{
			return defaultValue;
		}
		return array[i][j];
	}
	public static double getDoubleValue(double[][] array, int i, int j, double defaultValue)
	{
		if(array==null)
		{
			return defaultValue;
		}
		if(array.length<1)
		{
			return defaultValue;
		}
		if(i<0||j<0)
		{
			return defaultValue;
		}
		if(i>=array.length||j>=array[0].length)
		{
			return defaultValue;
		}
		return array[i][j];
	}
}
