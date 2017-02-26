package edgedetector;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;

import code2017.Particle;
import code2017.Vision17;
import visionCore.Vision;

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
	static int[][] GAUSS=
		{
			{2, 4, 5, 4, 2},
			{4, 9, 12, 9, 4},
			{5, 12, 15, 12, 5},
			{4, 9, 12, 9, 4},
			{2, 4, 5, 4, 2}
		};
	static int[][] GAUSS_2=
		{
			{1, 4, 7, 4, 1},
			{4, 16, 26, 16, 4},
			{7, 26, 41, 26, 7},
			{4, 16, 26, 16, 4},
			{1, 4, 7, 4, 1}
		};
	static int[][] IDENTITY=
		{
			{159}
		};
	static int[][] GAUSS_SMALL=
		{
			{9, 12, 9},
			{12, 15, 12},
			{9, 12, 9},
		};
	public static float convFree3x3(float[][] map, int[][] mask, int x1, int y1)//Assumes always in map
	{
		float result=0;
		//Go in order
		result = 			map	[y1-1]	[x1-1] 	* mask[0][0];
		result = result + 	map	[y1-1]	[x1] 	* mask[0][1];
		result = result + 	map	[y1-1]	[x1+1] 	* mask[0][2];
		result = result + 	map	[y1]	[x1-1] 	* mask[1][0];
		result = result + 	map	[y1]	[x1] 	* mask[1][1];
		result = result + 	map	[y1]	[x1+1] 	* mask[1][2];
		result = result + 	map	[y1+1]	[x1-1] 	* mask[2][0];
		result = result + 	map	[y1+1]	[x1] 	* mask[2][1];
		result = result + 	map	[y1+1]	[x1+1] 	* mask[2][2];
		return result;
	}
	public static float convFree5x5(float[][] map, int[][] mask, int x1, int y1)
	{
		float result;
		//Go in order
		result = 			map	[y1-2]	[x1-2] 	* mask[0][0];
		result = result + 	map	[y1-2]	[x1-1] 	* mask[0][1];
		result = result + 	map	[y1-2]	[x1] 	* mask[0][2];
		result = result + 	map	[y1-2]	[x1+1] 	* mask[0][3];
		result = result + 	map	[y1-2]	[x1+2] 	* mask[0][4];
		result = result + 	map	[y1-1]	[x1-2] 	* mask[1][0];
		result = result + 	map	[y1-1]	[x1-1] 	* mask[1][1];
		result = result + 	map	[y1-1]	[x1] 	* mask[1][2];
		result = result + 	map	[y1-1]	[x1+1] 	* mask[1][3];
		result = result + 	map	[y1-1]	[x1+2] 	* mask[1][4];
		result = result + 	map	[y1]	[x1-2] 	* mask[2][0];
		result = result + 	map	[y1]	[x1-1] 	* mask[2][1];
		result = result + 	map	[y1]	[x1] 	* mask[2][2];
		result = result + 	map	[y1]	[x1+1] 	* mask[2][3];
		result = result + 	map	[y1]	[x1+2] 	* mask[2][4];
		result = result + 	map	[y1+1]	[x1-2] 	* mask[3][0];
		result = result + 	map	[y1+1]	[x1-1] 	* mask[3][1];
		result = result + 	map	[y1+1]	[x1] 	* mask[3][2];
		result = result + 	map	[y1+1]	[x1+1] 	* mask[3][3];
		result = result + 	map	[y1+1]	[x1+2] 	* mask[3][4];
		result = result + 	map	[y1+2]	[x1-2] 	* mask[4][0];
		result = result + 	map	[y1+2]	[x1-1] 	* mask[4][1];
		result = result + 	map	[y1+2]	[x1] 	* mask[4][2];
		result = result + 	map	[y1+2]	[x1+1] 	* mask[4][3];
		result = result + 	map	[y1+2]	[x1+2] 	* mask[4][4];
		return result;
	}
	public static float convFree(float[][] map, int[][] mask, int x1, int y1)//Assumes always in map
	{
		int rad=((mask.length-1)/2);
		float result=0;
		for(int i=0;i<mask.length;i++)
		{
			for(int j=0;j<mask[0].length;j++)
			{
				int x=(j-rad)+x1;
				int y=(i-rad)+y1;
				float value=map[y][x];
				result=result+(value*mask[i][j]);
			}
		}
		return result;
	}
	public static float conv(float[][] map, int[][] mask, int x1, int y1, float defaultVal)
	{
		int rad=((mask.length-1)/2);
		float result=0;
		for(int i=0;i<mask.length;i++)
		{
			for(int j=0;j<mask[0].length;j++)
			{
				int x=(j-rad)+x1;
				int y=(i-rad)+y1;
				float value;
				//If location is not in the map, default to given value
				if(x>=0&&y>=0&&y<map.length&&x<map[0].length)
				{
					value=map[y][x];
				}
				else
				{
					value=defaultVal;
				}
				result=result+(value*mask[i][j]);
			}
		}
		return result;
	}
	public static float conv(float[][] map, int[][] mask, int x1, int y1)
	{
		int rad=((mask.length-1)/2);
		float result=0;
		for(int i=0;i<mask.length;i++)
		{
			for(int j=0;j<mask[0].length;j++)
			{
				int x=(j-rad)+x1;
				int y=(i-rad)+y1;
				float value;
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
	public static float[][] conv(float[][] map, int[][] mask, Rectangle region, float defaultVal)
	{
		Rectangle r=region;
		float[][] result=new float[(int) r.getHeight()][(int) r.getWidth()];
		for(int x1=r.x;x1<r.x+r.width;x1++)
		{
			for(int y1=r.y;y1<r.y+r.height;y1++)
			{
				int i=y1-r.y;
				int j=x1-r.x;
				result[i][j]=conv(map, mask, x1, y1, defaultVal);
			}
		}
		return result;
	}
	public static float[][] conv(float[][] map, int[][] mask, Rectangle region)
	{
		Rectangle r=region;
		float[][] result=new float[(int) r.getHeight()][(int) r.getWidth()];
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
	public static float[][] convFree(float[][] map, int[][] mask, Rectangle region)
	{
		Rectangle r=region;
		float[][] result=new float[(int) r.getHeight()][(int) r.getWidth()];
		for(int x1=r.x;x1<r.x+r.width;x1++)
		{
			for(int y1=r.y;y1<r.y+r.height;y1++)
			{
				int i=y1-r.y;
				int j=x1-r.x;
				result[i][j]=convFree(map, mask, x1, y1);
			}
		}
		return result;
	}
	public static float[][] convFree3x3(float[][] map, int[][] mask, Rectangle r)
	{
		float[][] result=new float[(int) r.getHeight()][(int) r.getWidth()];
		int xmax=r.x+r.width;
		int ymax=r.y+r.height;
		for(int x1=r.x;x1<xmax;x1++)
		{
			for(int y1=r.y;y1<ymax;y1++)
			{
				int i=y1-r.y;
				int j=x1-r.x;
				result[i][j]=convFree3x3(map, mask, x1, y1);
			}
		}
		return result;
	}
	/*
	public static float[][] convFree5x5(float[][] map, int[][] mask, Rectangle region)
	{
		Rectangle r=region;
		float[][] result=new float[(int) r.getHeight()][(int) r.getWidth()];
		for(int x1=r.x;x1<r.x+r.width;x1++)
		{
			for(int y1=r.y;y1<r.y+r.height;y1++)
			{
				int i=y1-r.y;
				int j=x1-r.x;
				result[i][j]=convFree5x5(map, mask, x1, y1);
			}
		}
		return result;
	}*/
	public static float[][] conv5x5(float[][] map, int[][] mask)
	{
		float[][] result=new float[map.length][map[0].length];
		for(int x1=2;x1<map[0].length-2;x1++)
		{
			for(int y1=2;y1<map.length-2;y1++)
			{
				result[y1][x1]=convFree5x5(map, mask, x1, y1);
			}
		}
		int yVal1=map.length-1;
		int yVal2=yVal1-1;
		for(int x=0;x<map[0].length;x++)
		{
			result[0][x]=conv(map, mask, x, 0);
			result[1][x]=conv(map, mask, x, 1);
			result[yVal1][x]=conv(map, mask, x, yVal1);
			result[yVal2][x]=conv(map, mask, x, yVal2);
		}
		int xVal1=map[0].length-1;
		int xVal2=xVal1-1;
		for(int y=2;y<map.length-2;y++)
		{
			result[y][0]=conv(map, mask, 0, y);
			result[y][1]=conv(map, mask, 1, y);
			result[y][xVal1]=conv(map, mask, xVal1, y);
			result[y][xVal2]=conv(map, mask, xVal2, y);
		}
		return result;
	}
	public static float[][] placeDefaultValues(float[][] map, float defaultvalue)
	{
		float[][] newMap=new float[map.length+2][map[0].length+2];
		for(int i=0;i<map.length;i++)
		{
			System.arraycopy(map[i], 0, newMap[i], 1, map[i].length);
		}
		int yVal=newMap.length-1;
		for(int x=0;x<newMap[0].length;x++)
		{
			newMap[0][x]=defaultvalue;
			newMap[yVal][x]=defaultvalue;
		}
		int xVal=newMap[0].length-1;
		for(int y=0;y<newMap.length;y++)
		{
			newMap[y][0]=defaultvalue;
			newMap[y][xVal]=defaultvalue;
		}
		return newMap;
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
	public static Rectangle[] divideRegions(float[][] map, int sectors)
	{
		return divideRegions(map[0].length, map.length, sectors);
	}
	public static Rectangle[] divideRegions(int[][] map, int sectors)
	{
		return divideRegions(map[0].length, map.length, sectors);
	}
	public static float[][] combineResults(float[][][] results)
	{
		float[][] combined;
		int width=0;
		for(int i=0;i<results.length;i++)
		{
			width=width+results[i][0].length;
		}
		combined=new float[results[0].length][];
		for(int y=0;y<combined.length;y++)
		{
			float[] row= new float[width];
			int position=0;
			for(int i=0;i<results.length;i++)
			{
				System.arraycopy(results[i][y], 0, row, position, results[i][y].length);
				position=position+results[i][y].length;
			}
			combined[y]=row;
		}
		return combined;
	}
	public static float magnitude(float[][] xderiv, float[][] yderiv, int x, int y)
	{
		float value=(float) Math.sqrt(Math.pow(xderiv[y][x], 2)+Math.pow(yderiv[y][x], 2));
		return value;
	}
	public static float[][] magnitude(float[][] xderiv, float[][] yderiv, Rectangle region)
	{
		Rectangle r=region;
		float[][] result=new float[(int) r.getHeight()][(int) r.getWidth()];
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
	public static double[][] generateDoubleMap(int[][][] rgb)
	{
		double[][] map=new double[rgb.length][rgb[0].length];
		for(int i=0;i<map.length;i++)
		{
			for(int j=0;j<map[0].length;j++)
			{
				map[i][j]=Conv.scoreRGB(rgb[i][j][0], rgb[i][j][1], rgb[i][j][2]);
			}
		}
		
		return map;
	}
	public static double[][] generateDoubleMap(BufferedImage image)
	{
		int[][][] rgb_map=Vision17.getArray(image);
		return generateDoubleMap(rgb_map);
	}
	public static float[][] generateFloatMap(int[][][] rgb)
	{
		long t1=System.currentTimeMillis();
		float[][] map=new float[rgb.length][rgb[0].length];
		for(int i=0;i<map.length;i++)
		{
			for(int j=0;j<map[0].length;j++)
			{
				map[i][j]=Conv.scoreRGBFloat(rgb[i][j][0], rgb[i][j][1], rgb[i][j][2]);
			}
		}
		System.out.printf("Float map generation time: [%d]\n", System.currentTimeMillis()-t1);
		return map;
	}
	public static float[][] generateFloatMap(BufferedImage image)
	{
		int[][][] rgb_map=Vision17.getArray(image);
		return generateFloatMap(rgb_map);
	}
	public static double scoreRGB(int red, int green, int blue)
	{
		double score=0.0;
		
		double r=red/255.0;
		double g=green/255.0;
		//double b=blue/255.0;
		
		score=(g*g*4)-(r*0.9);
		
		score=Math.min(score, 1.0);
		score=Math.max(score, 0.0);
		return score;
	}
	public static float scoreRGBFloat(int red, int green, int blue)
	{
		float score=0.0f;
		
		float r=(red/255.0f);
		float g=(green/255.0f);
		//float b=(float) (blue/255.0);
		
		score=((g*g*4)-(r*0.9f));
		
		score=Math.min(score, 1.0f);
		score=Math.max(score, 0.0f);
		return score;
	}
}
