package algorithm;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;

import visionCore.Cell;
import visionCore.Particle;

public class RectangleAlgorithm
{
	final double difference = 2.0 / 25.0;
	final Point[] allSurround = { new Point(0, -1), new Point(1, -1), new Point(1, 0), new Point(1, 1), new Point(0, 1), new Point(-1, 1), new Point(-1, 0), new Point(-1, -1) };
	public ArrayList<Point> allCorners = new ArrayList<Point>();
	private double[][] gKernel;

	public RectangleAlgorithm()
	{
		generateGauss(1, 5);
	}

	private void generateGauss(double sd, int size)
	{
		if (size % 2 == 0)
		{
			size++;
		}
		double[][] kernel = new double[size][size];
		for (int i = 0; i < size; i++)
		{
			for (int j = 0; j < size; j++)
			{
				int x = j - ((size - 1) / 2);
				int y = i - ((size - 1) / 2);
				kernel[i][j] = (1.0 / (2.0 * Math.PI * sd * sd)) * Math.exp(((x * x) + (y * y)) / (-2.0 * sd * sd));
			}
		}
		gKernel = kernel;
	}
	
	public int[][] findCorners(double[][] map)
	{
		int[][] corners = new int[map.length][map[0].length];
		double[][] Ix, Iy;
		long t1=System.nanoTime();
		final int[][] xMask = { { 3, 2, 1, 0, -1 - 2 - 3 }, { 4, 3, 2, 0, -2 - 3 - 4 }, { 5, 4, 3, 0, -3 - 4 - 5 }, { 6, 5, 4, 0, -4 - 5 - 6 }, { 5, 4, 3, 0, -3 - 4 - 5 }, { 4, 3, 2, 0, -2 - 3 - 4 }, { 3, 2, 1, 0, -1 - 2 - 3 } };
		final int[][] yMask = { { 3, 4, 5, 6, 5, 4, 3 }, { 2, 3, 4, 5, 4, 3, 2 }, { 1, 2, 3, 4, 3, 2, 1 }, { 0, 0, 0, 0, 0, 0, 0 }, { -1, -2, -3, -4, -3, -2, -1 }, { -2, -3, -4, -5, -4, -3, -2 }, { -3, -4, -5, -6, -5, -4, -3 } };
		Ix = conv2(map, xMask);
		Iy = conv2(map, yMask);
		System.out.printf("Time for derivatives: [%d]\n",((System.nanoTime()-t1)/1000000));
		double[][] Ixx = new double[Ix.length][Ix[0].length];
		double[][] Iyy = new double[Ix.length][Ix[0].length];
		double[][] Ixy = new double[Ix.length][Ix[0].length];
		;
		for (int i = 0; i < Ix.length; i++)
		{
			for (int j = 0; j < Ix[0].length; j++)
			{
				Ixx[i][j] = Ix[i][j] * Ix[i][j];
				Iyy[i][j] = Iy[i][j] * Iy[i][j];
				Ixy[i][j] = Ix[i][j] * Iy[i][j];
			}
		}
		t1=System.nanoTime();
		for (int x = 0; x < map[0].length; x++)
		{
			for (int y = 0; y < map.length; y++)
			{
				//Apply a guassian kernel at our current pixel
				double ixx = conv(Ixx, gKernel, x, y);
				double iyy = conv(Iyy, gKernel, x, y);
				double ixy = conv(Ixy, gKernel, x, y);
				double tr = ixx + iyy;
				double det = (ixx * iyy) - (ixy * ixy);
				double value = det - (0.04f * (tr * tr));
				final double threshold = 10000;
				if (value > threshold)
				{
					corners[y][x] = (int) value;
				}
			}
		}
		System.out.printf("Time for gaussian: [%d]\n",((System.nanoTime()-t1)/1000000));
		return corners;
	}

	public static ArrayList<Point> filterCorners(int[][] input)
	{
		final boolean[][] searchMask=
			{
				{false,	false,	true,	false,	false},
				{false,	true,	true,	true,	false},
				{true,	true,	true,	true,	true},
				{false,	true,	true,	true,	false},
				{false,	false,	true,	false,	false}
			};
		int[][] in = RectangleAlgorithm.copyOf(input);
		boolean[][] map2=new boolean[in.length][in[0].length];
		ArrayList<Point> corners = new ArrayList<Point>();
		for (int i = 0; i < in.length; i++)
		{
			for (int j = 0; j < in[0].length; j++)
			{
				if (in[i][j] > 0)
				{
					//Considers each point and adds only if all adjacent points are below it.
					int value=in[i][j];
					boolean valid=true;
					OuterLoop:
					for(int k=0;k<3;k++)
					{
						for(int l=0;l<3;l++)
						{
							int x=j+l-1;
							int y=i+k-1;
							if(x>=0&&y>=0&&x<in[0].length&&y<in.length)
							{
								if(in[y][x]>value)
								{
									valid=false;
									break OuterLoop;
								}
							}
						}
					}
					if(valid)
					{
						map2[i][j]=true;
						//Use this code if below code removed
						//corners.add(new Point(j,i));
					}
				}
			}
		}
		//Weird code to avoid that weird column
		final int width=30;
		final int height=60;
		final int maxHeight=map2.length/3;
		boolean[][] columnMask=new boolean[height][width];
		for(int i=0;i<columnMask.length;i++)
		{
			for(int j=0;j<columnMask[0].length;j++)
			{
				columnMask[i][j]=true;
			}
		}
		for(int i=0;i<map2.length;i++)
		{
			for(int j=0;j<map2[0].length;j++)
			{
				if(map2[i][j])
				{
					map2[i][j]=false;
					ArrayList<Point> column=new ArrayList<>();
					column.add(new Point(j,i));
					boolean[][] search=new boolean[map2.length][map2[0].length];
					search=applySearch(search,columnMask,j-(width/2),i);
					boolean change=true;
					while(change)
					{
						boolean[][] nextSearch=new boolean[search.length][search[0].length];
						change=false;
						for(int k=0;k<search.length;k++)
						{
							for(int l=0;l<search[0].length;l++)
							{
								if(search[k][l])
								{
									if(map2[k][l])
									{
										map2[k][l]=false;
										column.add(new Point(l,k));
										nextSearch=applySearch(nextSearch,columnMask,l-(width/2),k);
										change=true;
									}
								}
							}
						}
						search=nextSearch;
					}
					//if(column.get(column.size()-1).y-column.get(0).y<maxHeight)
					if(column.size()<20)
					{
						System.out.println("Column Size: "+column.size());
						corners.addAll(column);
					}
				}
			}
		}
		return corners;
	}
	public static boolean[][] applySearch(boolean[][] origin, boolean[][] mask, int x, int y)
	{
		for(int i=0;i<mask.length;i++)
		{
			for(int j=0;j<mask[0].length;j++)
			{
				if(mask[i][j])
				{
					int x1=j+x;
					int y1=i+y;
					if(x1>=0&&y1>=0&&x1<origin[0].length&&y1<origin.length)
					{
						origin[y1][x1]=true;
					}
				}
			}
		}
		return origin;
	}
	public static boolean[][] applyMask(boolean[][] origin, boolean[][] mask, int x, int y)
	{
		int rad=(mask.length-1)/2;
		for(int i=0;i<mask.length;i++)
		{
			for(int j=0;j<mask[0].length;j++)
			{
				if(mask[i][j])
				{
					int x1=j+x-rad;
					int y1=i+y-rad;
					if(x1>=0&&y1>=0&&x1<origin[0].length&&y1<origin.length)
					{
						origin[y1][x1]=true;
					}
				}
			}
		}
		return origin;
	}
	
	public static int equivRect(Particle particle, ArrayList<Point> interests)
	{
		int score = 0;
		double width=0;
		double height=0;
		Point[] corner=new Point[4];
		ArrayList<Point> corners=new ArrayList<Point>();
		
		for(Point p: interests)
		{
			if(p.x>=particle.getX()&&p.y>=particle.getY()&&p.x<particle.getX()+particle.getWidth()&&p.y<particle.getY()+particle.getHeight())
			{
				corners.add(p);
			}
		}
		
		//Find points closest to the corners of particle
		Point[] fixed = {new Point(particle.getWidth(),0),new Point(0,0),new Point(0,particle.getHeight()),new Point(particle.getWidth(),particle.getHeight())};
		Double[] record={9999.0,9999.0,9999.0,9999.0};
		//Goes in quadrant order
		for(Point point: corners)
		{
			for(int i=0;i<2;i++)
			{
				int quadrant=i;
				if(point.getY()>particle.getHeight()/2)
				{
					quadrant=quadrant+2;
				}
				double distance=distance(point,fixed[quadrant]);
				if(distance<record[quadrant])
				{
					record[quadrant]=distance;
					corner[quadrant]=point;
				}
			}
		}
		//*/
		//Finishing code, always keep
		if(corner[0]==null)
		{
			corner[0]=new Point(particle.getWidth(),0);
		}
		if(corner[1]==null)
		{
			corner[1]=new Point(0,0);
		}
		if(corner[2]==null)
		{
			corner[2]=new Point(0, particle.getHeight());
		}
		if(corner[3]==null)
		{
			corner[3]=new Point(particle.getWidth(),particle.getHeight());
		}
		width=(distance(corner[0],corner[1])+distance(corner[2],corner[3]))/2.0;
		height=(distance(corner[0],corner[3])+distance(corner[1],corner[2]))/2.0;
		particle.corners=corner;
		particle.tLocation=new Point((corner[0].x+corner[1].x)/2,(corner[0].y+corner[1].y)/2);
		particle.setTWidth((int) width);
		particle.setTHeight((int) height);
		particle.setAngle(Math.atan((corner[3].y-corner[2].y)/(1.0*corner[3].x-corner[2].x)));
		double ratio = (width * 1.0) / (height * 1.0) * 1.0;
		double ideal = 1.6;
		score = (int) (100.0 * (Math.abs(ratio - ideal) / (0.9)));
		if (score > 100)
		{
			score = 100;
		}
		return score;
	}

	private double conv(double[][] input, double[][] mask, int x, int y)
	{
		double value = 0;
		for (int i = 0; i < mask.length; i++)
		{
			for (int j = 0; j < mask[0].length; j++)
			{
				int x1 = x + j - ((mask[0].length - 1) / 2);
				int y1 = y + i - ((mask.length - 1) / 2);
				if (x1 >= 0 && y1 >= 0 && x1 < input[0].length && y1 < input.length)
				{
					value = value + (mask[i][j] * input[y1][x1]);
				}
			}
		}
		return value;
	}

	public double[][] toMat(double x, double y)
	{
		double[][] mat = new double[2][2];
		mat[0][0] = x * x;
		mat[1][1] = y * y;
		mat[0][1] = x * y;
		mat[1][0] = mat[0][1];
		return mat;
	}

	private double[][] conv2(double[][] input, int[][] mask)
	{
		double[][] result = new double[input.length][input[0].length];
		for (int i = 0; i < input.length; i++)
		{
			for (int j = 0; j < input[0].length; j++)
			{
				double value = 0;
				for (int k = 0; k < mask.length; k++)
				{
					for (int l = 0; l < mask[0].length; l++)
					{
						int y = i + k - ((mask.length - 1) / 2);
						int x = j + l - ((mask[0].length - 1) / 2);
						if (x >= 0 && y >= 0 && x < input[0].length && y < input.length)
						{
							value = value + (input[y][x] * mask[k][l]);
						}
					}
				}
				result[i][j] = value;
			}
		}
		return result;
	}

	private double[][] conv2(Particle image, int[][] mask)
	{
		int[][] input = new int[image.getHeight()][image.getWidth()];
		for (int i = 0; i < image.map.length; i++)
		{
			for (int j = 0; j < image.map[0].length; j++)
			{
				if (image.map[i][j])
				{
					input[i][j] = 1;
				}
			}
		}
		double[][] result = new double[input.length][input[0].length];
		for (int i = 0; i < input.length; i++)
		{
			for (int j = 0; j < input[0].length; j++)
			{
				double value = 0;
				for (int k = 0; k < mask.length; k++)
				{
					for (int l = 0; l < mask[0].length; l++)
					{
						int y = i + k - ((mask.length - 1) / 2);
						int x = j + l - ((mask[0].length - 1) / 2);
						if (x >= 0 && y >= 0 && x < input[0].length && y < input.length)
						{
							value = value + (input[y][x] * mask[k][l]);
						}
					}
				}
				result[i][j] = value;
			}
		}
		return result;
	}

	public double intensity(Particle particle, int x, int y, int side)
	{
		x = x - (side / 2);
		y = y - (side / 2);
		int alive = 0;
		int total = 0;
		for (int x1 = x; x1 < side + x; x1++)
		{
			for (int y1 = y; y1 < side + y; y1++)
			{
				if (particle.localInMap(x1, y1))
				{
					if (particle.getLocalValue(x1, y1))
					{
						alive++;
					}
				}
				total++;
			}
		}
		return (alive * 1.0);
	}

	public double[] eigenvalue(double[][] mat)
	{
		/*
		 * Notes mat[i][j] i j 0 1 0 x2 xy 1 xy y2
		 */
		//Using http://www.math.harvard.edu/archive/21b_fall_04/exhibits/2dmatrices/index.html
		double[] value = new double[2];
		if (mat.length < 2)
		{
			return new double[2];
		}
		if (mat[0].length < 2)
		{
			return new double[2];
		}
		double T = mat[0][0] + mat[1][1];
		double D = (mat[0][0] * mat[1][1]) - (mat[0][1] * mat[1][0]);
		double t = T / 2.0;
		double s = Math.sqrt(((T * T) / 4.0) - D);
		value[0] = t - s;
		value[1] = t + s;
		return value;
	}

	public Particle[] findContour(Particle particle)
	{
		Particle contour = new Particle(particle);
		//Shell, alive cells in shell represents dead cells around the particle, this avoids a contour with holes within
		Particle shell = new Particle(particle.x - 1, particle.y - 1, new boolean[particle.map.length + 2][particle.map[0].length + 2]);
		//Represents previous tiles that changed, used to determine which tiles to check for expansion;
		Particle expanded = new Particle(shell);
		//Starting "seed", guaranteed to be false in particle
		for (int i = 0; i < shell.getWidth(); i++)
		{
			shell.setLocalValue(i, 0, true);
			shell.setLocalValue(i, shell.getHeight() - 1, true);
			expanded.setLocalValue(i, 0, true);
			expanded.setLocalValue(i, shell.getHeight() - 1, true);
		}
		for (int i = 0; i < shell.getHeight(); i++)
		{
			shell.setLocalValue(0, i, true);
			shell.setLocalValue(shell.getWidth() - 1, i, true);
			expanded.setLocalValue(0, i, true);
			expanded.setLocalValue(shell.getWidth() - 1, i, true);
		}
		expanded.setLocalValue(0, 0, false);
		expanded.setLocalValue(0, expanded.getHeight() - 1, false);
		expanded.setLocalValue(expanded.getWidth() - 1, 0, false);
		expanded.setLocalValue(expanded.getWidth() - 1, expanded.getHeight() - 1, false);
		boolean change = true;
		while (change)
		{
			//Represents expansion in this cycle
			Particle expanding = new Particle(shell);
			change = false;
			for (int i = 0; i < expanded.getHeight(); i++)
			{
				for (int j = 0; j < expanded.getWidth(); j++)
				{
					if (expanded.getLocalValue(j, i))
					{
						int x = j + expanded.x;
						int y = i + expanded.y;
						//Look at all surrounding tiles for alive squares
						Cell[] around = checkSurroundingGlobal(x, y, particle);
						if (around[0].equals(Cell.FALSE))
						{
							if (!shell.getGlobalValue(x, y + 1))
							{
								change = true;
								expanding.setGlobalValue(x, y + 1, true);
								shell.setGlobalValue(x, y + 1, true);
							}
						}
						if (around[1].equals(Cell.FALSE))
						{
							if (!shell.getGlobalValue(x + 1, y))
							{
								change = true;
								expanding.setGlobalValue(x + 1, y, true);
								shell.setGlobalValue(x + 1, y, true);
							}
						}
						if (around[2].equals(Cell.FALSE))
						{
							if (!shell.getGlobalValue(x, y - 1))
							{
								change = true;
								expanding.setGlobalValue(x, y - 1, true);
								shell.setGlobalValue(x, y - 1, true);
							}
						}
						if (around[3].equals(Cell.FALSE))
						{
							if (!shell.getGlobalValue(x - 1, y))
							{
								change = true;
								expanding.setGlobalValue(x - 1, y, true);
								shell.setGlobalValue(x - 1, y, true);
							}
						}
					}
				}
			}
			/*
			 * "I'm fifty percent sure this is useless and caused a bug, and I'm fifty percent sure that this is needed."
			 * --Best Programmer in the World
			 */

			/*
			 * //Clean up, avoid points already in shell for(int
			 * i=0;i<expanding.map.length;i++) { for(int
			 * j=0;j<expanding.map[0].length;j++) {
			 * if(expanding.getLocalValue(j, i)) { if(shell.getLocalValue(j, i))
			 * { expanding.setLocalValue(j, i, false); } } } }
			 */
			expanded = expanding;
		}
		for (int x = 0; x < shell.getWidth(); x++)
		{
			for (int y = 0; y < shell.getHeight(); y++)
			{
				if (!shell.getLocalValue(x, y))
				{
					Cell[] around = checkSurroundingLocal(x, y, shell);
					boolean valid = false;
					for (Cell value : around)
					{
						if (value.equals(Cell.TRUE))
						{
							valid = true;
							break;
						}
					}
					if (valid)
					{
						contour.setGlobalValue((int) (x + shell.getX()), (int) (y + shell.getY()), true);
					}
				}
			}
		}
		Particle[] toReturn = new Particle[2];
		toReturn[0] = shell;
		toReturn[1] = contour;
		return toReturn;
	}

	public boolean[][] generateCircle(boolean[][] scan, int radius)
	{
		int x = 0;
		int y = radius;
		int dp = 1 - radius;
		do
		{
			if (dp < 0)
			{
				dp = dp + 2 * (x++) + 3;
			}
			else
			{
				dp = dp + 2 * (x++) - 2 * (y--) + 5;
			}

			scan[radius + y][radius + x] = true;
			scan[radius + y][radius - x] = true;
			scan[radius - y][radius + x] = true;
			scan[radius - y][radius - x] = true;
			scan[radius + x][radius + y] = true;
			scan[radius + x][radius - y] = true;
			scan[radius - x][radius + y] = true;
			scan[radius - x][radius - y] = true;
		} while (x < y);
		scan[radius][0] = true;
		scan[radius][scan[0].length - 1] = true;
		scan[0][radius] = true;
		scan[scan[0].length - 1][radius] = true;
		for (int i = 0; i < scan.length; i++)
		{
			int start = -1;
			int end = 0;
			for (int j = 0; j < scan[0].length; j++)
			{
				if (scan[i][j])
				{
					if (start == -1)
					{
						start = j + 0;
					}
					end = j + 0;
				}
			}
			for (int k = start; k < end; k++)
			{
				scan[i][k] = true;
			}
		}
		return scan;
	}

	private static double distance(Point p, Point p2)
	{
		return Math.sqrt(Math.pow(p.x - p2.x, 2) + Math.pow(p.y - p2.y, 2));
	}

	public Cell[] allSurroundLocal(int x, int y, Particle toCheck)
	{
		Cell[] surrounding = new Cell[8];
		//Top, clockwise
		for (byte i = 0; i < allSurround.length; i++)
		{
			int dx = x + allSurround[i].x;
			int dy = y + allSurround[i].y;
			if (toCheck.localInMap(dx, dy))
			{
				if (toCheck.getLocalValue(dx, dy))
				{
					surrounding[i] = Cell.TRUE;
				}
				else
				{
					surrounding[i] = Cell.FALSE;
				}
			}
			else
			{
				surrounding[i] = Cell.NULL;
			}
		}
		return surrounding;
	}

	public Cell[] allSurroundGlobal(int x, int y, Particle toCheck)
	{
		return allSurroundLocal(x - toCheck.x, y - toCheck.y, toCheck);
	}

	public Cell[] checkSurroundingLocal(int x, int y, Particle toCheck)
	{
		Cell[] surrounding = new Cell[4];
		//Top, right, bottom, left
		if (toCheck.localInMap(x, y + 1))
		{
			if (toCheck.getLocalValue(x, y + 1))
			{
				surrounding[0] = Cell.TRUE;
			}
			else
			{
				surrounding[0] = Cell.FALSE;
			}
		}
		else
		{
			surrounding[0] = Cell.NULL;
		}
		if (toCheck.localInMap(x + 1, y))
		{
			if (toCheck.getLocalValue(x + 1, y))
			{
				surrounding[1] = Cell.TRUE;
			}
			else
			{
				surrounding[1] = Cell.FALSE;
			}
		}
		else
		{
			surrounding[1] = Cell.NULL;
		}
		if (toCheck.localInMap(x, y - 1))
		{
			if (toCheck.getLocalValue(x, y - 1))
			{
				surrounding[2] = Cell.TRUE;
			}
			else
			{
				surrounding[2] = Cell.FALSE;
			}
		}
		else
		{
			surrounding[2] = Cell.NULL;
		}
		if (toCheck.localInMap(x - 1, y))
		{
			if (toCheck.getLocalValue(x - 1, y))
			{
				surrounding[3] = Cell.TRUE;
			}
			else
			{
				surrounding[3] = Cell.FALSE;
			}
		}
		else
		{
			surrounding[3] = Cell.NULL;
		}
		return surrounding;
	}

	public Cell[] checkSurroundingGlobal(int x, int y, Particle toCheck)
	{
		return checkSurroundingLocal(x - toCheck.x, y - toCheck.y, toCheck);
	}

	public Point addPoints(Point a, Point b)
	{
		return new Point(a.x + b.x, a.y + b.y);
	}

	public static boolean[][] copyOf(boolean[][] original)
	{
		boolean[][] copy = new boolean[original.length][];
		for (int i = 0; i < original.length; i++)
		{
			copy[i] = Arrays.copyOf(original[i], original[i].length);
		}
		return copy;
	}

	public static int[][] copyOf(int[][] original)
	{
		int[][] copy = new int[original.length][];
		for (int i = 0; i < original.length; i++)
		{
			copy[i] = Arrays.copyOf(original[i], original[i].length);
		}
		return copy;
	}
}
