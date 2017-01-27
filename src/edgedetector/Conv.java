package edgedetector;

import java.awt.Rectangle;

public class Conv 
{
	static int[][] SOBEL_X=
		{
			{-1, 0, 1},
			{-2, 0, 2},
			{-1, 0, 1}
		};
	static int[][] SOBEL_Y=
		{
			{1, 2, 1},
			{0, 0, 0},
			{-1, -2, -1}
		};
	public static double conv(double[][] map, double[][] mask, int x1, int y1)
	{
		if(mask.length==0)
		{
			return 0;
		}
		if(mask.length!=mask[0].length)
		{
			return 0;
		}
		int rad=(int) ((mask.length-1)/2.0);
		double result=0;
		for(int i=0;i<mask.length;i++)
		{
			for(int j=0;j<mask[0].length;j++)
			{
				int x=(j-rad)+x1;
				int y=(i-rad)+y1;
				double value;
				//If location is not in the map, default to the center value
				if(x>=0&&y>=0&&y<map.length&&x<map[0].length)
				{
					value=map[y][x];
				}
				else
				{
					value=map[y1][x1];
				}
				result=result+(value*mask[i][j]);
			}
		}
		return result;
	}
	//Gives conv value for a larger region
	public static double[][] conv(double[][] map, double[][] mask, Rectangle region)
	{
		Rectangle r=region.intersection(new Rectangle(0, 0, map[0].length, map.length));
		if(region!=r)
		{
			System.out.println("WARNING: Region did not fall completely inside the map.");
		}
		double[][] result=new double[(int) r.getHeight()][(int) r.getWidth()];
		for(int x1=r.x;x1<r.x+r.width;x1++)
		{
			for(int y1=r.y;y1<r.y+r.height;y1++)
			{
				int i=y1-r.y;
				int j=x1-r.x;
				result[i][j]=conv(map, mask, x1, y1);
			}
		}
		return result;
	}
	public static Rectangle[] divideRegions(int width, int height, int sectors)
	{
		Rectangle[] regions=new Rectangle[sectors];
		int normalWidth=(int) Math.floor((width*1.0)/sectors);
		int abnormalWidth=(int)((width-((sectors-1)*normalWidth)));
		for(int i=0;i<sectors-1;i++)
		{
			Rectangle r=new Rectangle();
			r.setLocation((i*normalWidth), 0);
			r.setSize(normalWidth, height);
			regions[i]=r;
		}
		regions[sectors-1]=new Rectangle(normalWidth*(sectors-1), 0, abnormalWidth, height);
		return regions;
	}
	public static Rectangle[] divideRegions(double[][] map, int sectors)
	{
		return divideRegions(map[0].length, map.length, sectors);
	}
	public static Rectangle[] divideRegions(int[][] map, int sectors)
	{
		return divideRegions(map[0].length, map.length, sectors);
	}
	public static double magnitude(double[][] xderiv, double[][] yderiv, int x, int y)
	{
		double value=Math.sqrt(Math.pow(xderiv[y][x], 2)+Math.pow(yderiv[y][x], 2));
		return value;
	}
	public static double[][] magnitude(double[][] xderiv, double[][] yderiv, Rectangle region)
	{
		Rectangle r=region.intersection(new Rectangle(0, 0, xderiv[0].length, yderiv.length));
		if(region!=r)
		{
			System.out.println("WARNING: Region did not fall completely inside the map.");
		}
		double[][] result=new double[(int) r.getHeight()][(int) r.getWidth()];
		for(int x1=r.x;x1<r.x+r.width;x1++)
		{
			for(int y1=r.y;y1<r.y+r.height;y1++)
			{
				int i=y1-r.y;
				int j=x1-r.x;
				result[i][j]=magnitude(xderiv, yderiv, x1, y1);
			}
		}
		return result;
	}
}
