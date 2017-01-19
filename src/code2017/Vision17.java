package code2017;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.PrintStream;
import java.util.ArrayList;

import visionCore.Vision;

public class Vision17 
{
	private BufferedImage image=null;
	static ScoreType[] SCORE_PATTERN = 
		{
				ScoreType.EQUIV_RECT,
				ScoreType.COVERAGE,
				ScoreType.PROFILE,
				ScoreType.GREENESS,
				ScoreType.CENTERNESS
		};
	public static double VIEW_ANGLE= Math.toRadians(67.9446);
	public PrintStream log = System.out;
	
	public boolean[][] map=null;
	public int[][][] rgb=null;
	public Property property=Property.getIdealGear();
	//Called when vision is initialized, preferably before autonomous begins
	public void init()
	{
		
	}
	//Called when queued for processing
	public Target exec()
	{
		Target target= new Target();
		map=createMap(image);
		ArrayList<Particle> particles;
		particles = Vision17.findParticles(map);
		
		if(particles.size()==0)
		{
			return target;
		}
		findProperties(particles);
		
		evaluateScoreBoiler(particles);
		//Really simple code that needs to be updated
		filterFarParticles(particles, 240.0);
		Particle particle = findBestParticle(particles);
		target= findTargetFromParticle(particle);
		
		return target;
	}
	public Score diagRect(Particle particle)
	{
		/*
		 * Corner order for reference
		 * 		0	1
		 * 
		 * 		2	3
		 */
		double width;
		double height;
		
		//Two options, measure diagonally or just all at once sum of x y. Second option here.
		int[] smallestSums = {9999, 9999, 9999, 9999};
		Point[] local_fixed= {new Point(0,0), new Point(particle.getWidth()-1, 0), new Point(0, particle.getHeight()-1), new Point(particle.getWidth()-1, particle.getHeight()-1)};
		for(int x=0;x<particle.getWidth();x++)
		{
			for(int y=0;y<particle.getHeight();y++)
			{
				if(particle.getLocalValue(x, y))
				{
					for(int i=0;i<smallestSums.length;i++)
					{
						int sum=Math.abs(local_fixed[i].x-x) + Math.abs(local_fixed[i].y-y);
						if(sum < smallestSums[i])
						{
							smallestSums[i]=sum;
							particle.corners[i]=new Point(x,y);
						}
					}
				}
			}
		}
		//Translate corners from local values to global values
		for(int i=0;i<particle.corners.length;i++)
		{
			particle.corners[i].translate((int) (particle.getX()), (int) (particle.getY()));
		}
		
		width=(distance(particle.corners[0],particle.corners[1])+distance(particle.corners[2],particle.corners[3]))/2.0;
		height=(distance(particle.corners[0],particle.corners[2])+distance(particle.corners[1],particle.corners[3]))/2.0;
		particle.tLocation=new Point((particle.corners[0].x+particle.corners[1].x)/2,(particle.corners[0].y+particle.corners[1].y)/2);
		particle.setTWidth((int) width);
		particle.setTHeight((int) height);
		//particle.setAngle(Math.atan((particle.corners[2].y-particle.corners[3].y)/(1.0*particle.corners[2].x-particle.corners[3].x)));
		particle.setAngle(Math.atan((particle.corners[2].y-particle.corners[3].y)/(1.0*(particle.corners[3].x-particle.corners[2].x))));
		//particle.setAngle(Math.atan2(particle.corners[3].x-particle.corners[2].x,particle.corners[2].y-particle.corners[3].y));
		double ratio = (height * 1.0) / (width * 1.0) * 1.0;
		return new Score(ratio, ScoreType.EQUIV_RECT);
	}
	public Score coverage(Particle particle)
	{
		double ratio=particle.count/(particle.getTWidth()*particle.getTHeight()*1.0);
		return new Score(ratio, ScoreType.COVERAGE);
	}
	@SuppressWarnings("unused")
	public Score moment(Particle particle)
	{
		Point centroid=new Point(0,0);
		long m20=0;
		long m02=0;
		long m11=0;
		long m10=0;
		long m01=0;
		int m00=0;
		for(int x=0;x<particle.getWidth();x++)
		{
			for(int y=0;y<particle.getHeight();y++)
			{
				if(particle.getLocalValue(x, y))
				{
					m20=m20+(x*x);
					m02=m02+(y*y);
					m11=m11+(x*y);
					m10=m10+(x);
					m01=m01+(y);
					centroid.x=centroid.x+x;
					centroid.y=centroid.y+y;
					m00++;
				}
			}
		}
		centroid.x=centroid.x/particle.count;
		centroid.y=centroid.y/particle.count;
		
		long u00=particle.count;
		long u11=m11-(centroid.x*m01);
		long u20=m20-(centroid.x*m10);
		long u02=m02-(centroid.y*m01);
		
		double momentOfInertia=Moment.moi(particle, centroid);
		System.out.printf("Moment of inertia: [%f]\n",momentOfInertia);
		//System.out.printf("Mx: [%f]\tMy: [%f]\t Md: [%f]\t Mz: [%f]\n",mx/(particle.count*1.0),my/(particle.count*1.0),md/(particle.count*1.0), mz/(particle.count*1.0));
		return new Score(momentOfInertia,ScoreType.MOMENT);
		//return null;
	}
	public Score profile(Particle particle)
	{
		Point[] corners=new Point[4];
		for(int i=0;i<corners.length;i++)
		{
			corners[i]=new Point(particle.corners[i].x, particle.corners[i].y);
			corners[i].translate(particle.x*-1, particle.y*-1);
		}
		double x1=((particle.corners[2].x-particle.corners[0].x)*1.0)/100.0;
		double y1=((corners[2].y-corners[0].y)*1.0)/100.0;
		double x2=((corners[3].x-corners[1].x)*1.0)/100.0;
		double y2=((corners[3].y-corners[1].y)*1.0)/100.0;
		double[] xprofile=new double[100];
		double[] yprofile=new double[100];
		//First y profile
		for(int i=0;i<100;i++)
		{
			Point p1=new Point((int)(corners[0].x+(i*x1)),(int)(corners[0].y+(i*y1)));
			Point p2=new Point((int)(corners[1].x+(i*x2)),(int)(corners[1].y+(i*y2)));
			double slope=(p1.y-p2.y)/(p1.x-p2.x*1.0);
			int count=0;
			int alive=0;
			for(int x=p1.x;x<p2.x;x++)
			{
				int y=(int) (slope*x)+p1.y;
				if(particle.localInMap(x, y))
				{
					if(particle.getLocalValue(x, y))
					{
						alive++;
					}
					count++;
				}
			}
			if(count==0)
			{
				yprofile[i]=alive/(1e-10);
			}
			else
			{
				yprofile[i]=alive/(count*1.0);
			}
		}
		x1=((corners[1].x-corners[0].x)*1.0)/100;
		y1=((corners[1].y-corners[0].y)*1.0)/100;
		x2=((corners[3].x-corners[2].x)*1.0)/100;
		y2=((corners[3].y-corners[2].y)*1.0)/100;
		//Next x profile
		for(int i=0;i<100;i++)
		{
			Point p1=new Point((int)(corners[0].x+(i*x1)),(int)(corners[0].y+(i*y1)));
			Point p2=new Point((int)(corners[2].x+(i*x2)),(int)(corners[2].y+(i*y2)));
			double slope=(p1.x-p2.x)/(p1.y-p2.y*1.0);
			int count=0;
			int alive=0;
			for(int y=p1.y;y<p2.y;y++)
			{
				int x=(int) (slope*y)+p1.x;
				if(particle.localInMap(x, y))
				{
					if(particle.getLocalValue(x, y))
					{
						alive++;
					}
					count++;
				}
			}
			if(count==0)
			{
				xprofile[i]=alive/(1e-10);
			}
			else
			{
				xprofile[i]=alive/(count*1.0);
			}
		}
		return new Score(xprofile, yprofile);
	}
	public Score green(Particle particle)
	{
		long greeness=0;
		for(int x = (int) particle.getX(); x<particle.getWidth()+particle.getX();x++)
		{
			for(int y = (int) particle.getY(); y<particle.getHeight()+particle.getY();y++)
			{
				if(particle.getGlobalValue(x, y))
				{
					int[] rgb=this.rgb[y][x];
					greeness=greeness+rgb[1]-(rgb[0]);
				}
			}
		}
		double ratio=((greeness*1.0)/particle.count)*255.0;
		return new Score(ratio, ScoreType.GREENESS);
	}
	public Score center(Particle particle)
	{
		return new Score(0.0, ScoreType.CENTERNESS);
	}
	
	private void findProperties(ArrayList<Particle> particles)
	{
		for (int i=0; i<particles.size();i++)
		{
			particles.get(i).scores=new Score[SCORE_PATTERN.length];
			
			for(int j=0;j<SCORE_PATTERN.length;j++)
			{
				Score s = null;
				switch(SCORE_PATTERN[j])
				{
					case EQUIV_RECT:
						s=diagRect(particles.get(i));
						break;
					case COVERAGE:
						s=coverage(particles.get(i));
						break;
					case MOMENT:
						s=moment(particles.get(i));
						break;
					case PROFILE:
						s=profile(particles.get(i));
						break;
					case GREENESS:
						s=green(particles.get(i));
						break;
					case CENTERNESS:
						s=center(particles.get(i));
						break;
				}
				particles.get(i).scores[j]=s;
			}
		}
	}
	private void evaluateScoreBoiler(ArrayList<Particle> particles)
	{
		//Compare properties to top line
		for(int i=0; i<particles.size();i++)
		{
			int totalScore = 0;
			for(int j=0; j<SCORE_PATTERN.length;j++)
			{
				int s=0;
				s=particles.get(i).scores[j].getScore(property);
				totalScore= totalScore + s;
			}
			particles.get(i).score=totalScore;
		}
	}
	
	private void filterFarParticles(ArrayList<Particle> particles, double maxDistance)
	{
		ArrayList<Particle> disqualified= new ArrayList<Particle>();
		for(int i=0;i<particles.size();i++)
		{
			findParticleDistance(particles.get(i), 15.0, image.getWidth());
			if(particles.get(i).distance>maxDistance)
			{
				disqualified.add(particles.get(i));
			}
		}
		for(int i=0;i<disqualified.size();i++)
		{
			particles.remove(disqualified.get(i));
		}
	}
	private Particle findBestParticle(ArrayList<Particle> particles)
	{
		Particle p=null;
		int bestScore=999999;
		for(int i=0;i<particles.size();i++)
		{
			if(particles.get(i).score<bestScore)
			{
				p=particles.get(i);
				bestScore=particles.get(i).score;
			}
		}
		return p;
	}
	private Target findTargetFromParticle(Particle particle)
	{
		Target target;
		if(particle==null)
		{
			return Target.getNullTarget();
		}
		double x = ((particle.tLocation.getX()) - (image.getWidth() / 2.0)) / (image.getWidth() / 2.0);
		double y = -1.0 * ((particle.tLocation.getY()) - (image.getHeight() / 2.0)) / (image.getHeight() / 2.0);
		target = new Target(x, y, particle.getAngle(), particle.distance);
		return target;
	}
	
	
	public static ArrayList<Particle> findParticles(boolean[][] map)// Generates rectangles for every point
	{
		final int minimumAlive = 100;
		
		boolean[][] mapCopy = Array2DCopier.copyOf(map);
		long total=0;
		ArrayList<Particle> toReturn = new ArrayList<Particle>();
		int iStart = 0, jStart = 0, iMax = 0, jMax = 0;
		iMax = mapCopy[0].length;
		jMax = mapCopy.length;
		for (int i = iStart; i < iMax; i++)
		{
			for (int j = jStart; j < jMax; j++)
			{
				if (mapCopy[j][i])
				{
					mapCopy[j][i]=false;
					Particle particle = new Particle(i, j, new boolean[1][1]);
					particle.map[0][0] = true;
					boolean change = true;
					Particle expansion = new Particle(
							(int) (particle.getX()),
							(int) (particle.getY()), new boolean[1][1]);
					if (particle.getX() > 0)
					{
						expansion.expandLeft();
						expansion.setGlobalValue(
								(int) (particle.getX() - 1),
								(int) (particle.getY()), true);
					}
					if (particle.getY() > 0)
					{
						expansion.expandUp();
						expansion.setGlobalValue((int) (particle.getX()),
								(int) (particle.getY() - 1), true);
					}
					if (particle.getX() < mapCopy[0].length - 1)
					{
						expansion.expandRight();
						expansion.setGlobalValue(
								(int) (particle.getX() + 1),
								(int) (particle.getY()), true);
					}
					if (particle.getY() < mapCopy.length - 1)
					{
						expansion.expandDown();
						expansion.setGlobalValue((int) (particle.getX()),
								(int) (particle.getY() + 1), true);
					}
					int x;
					int y;
					while (change)
					{
						Particle next = new Particle((int) (expansion.getX()),(int) (expansion.getY()),new boolean[expansion.map.length][expansion.map[0].length]);
						change = false;
						for (int k = 0; k < expansion.getWidth(); k++)
						{
							for (int l = 0; l < expansion.getHeight(); l++)
							{
								// Compare to picture map values to
								// determine expansion
								if (expansion.getLocalValue(k, l))
								{
									x = (int) (k + expansion.getX());
									y = (int) (l + expansion.getY());
									if (mapCopy[y][x])
									{
										mapCopy[y][x]=false;
										change = true;
										// Expand the particle into that square
										// Determines if size increase of particle required
										if (x - particle.getX() < 0)
										{
											particle.expandLeft();
										}
										if (x - particle.getX() >= particle.getWidth())
										{
											particle.expandRight();
										}
										if (y - particle.getY() < 0)
										{
											particle.expandUp();
										}
										if (y - particle.getY() >= particle.getHeight())
										{
											particle.expandDown();
										}
										// Sets particle value
										particle.setGlobalValue(x, y, true);
										// Prepares expansion for next cycle
										// Make surrounding position of new
										// particle true
										// Top Side
										// Check if space in global map
										if (y > 0)
										{
											// Check if expansion neccessary
											while (y - 1 - next.getY() < 0)
											{
												next.expandUp();
											}
											next.setGlobalValue(x, y - 1,
													true);
										}
										// Left side
										if (x > 0)
										{
											// Check if expansion neccessary
											while (x - 1 - next.getX() < 0)
											{
												next.expandLeft();
											}
											next.setGlobalValue(x - 1, y,
													true);
										}
										// Bottom side
										if (y + 1 < mapCopy.length)
										{
											// Check if expansion neccessary
											while (y + 1 - next.getY() >= next
													.getHeight())
											{
												next.expandDown();
											}
											next.setGlobalValue(x, y + 1,
													true);
										}
										// Right Side
										if (x + 1 < mapCopy[0].length)
										{
											// Check if expansion neccessary
											while (x + 1 - next.getX() >= next.getWidth())
											{
												next.expandRight();
											}
											next.setGlobalValue(x + 1, y,true);
										}
									}
								}
							}
						}
						if (change)
						{
							next.shorten();
							expansion = next;
						}
					}
					if (particle.count >= minimumAlive)
					{
						toReturn.add(particle);
					}
				}
			}
		}
		System.out.println("Total \t"+total);
		return toReturn;
	}
	
	public static double findParticleDistance(Particle particle, double idealWidth, int imageWidth)
	{
		double distance = idealWidth * imageWidth / (2 * particle.getTWidth()* Math.tan(VIEW_ANGLE));
		particle.distance=distance;
		return distance;
	}
	public static double distance(Point p, Point p2)
	{
		return Math.sqrt(Math.pow(p.x - p2.x, 2) + Math.pow(p.y - p2.y, 2));
	}
	public void setImage(BufferedImage image)
	{
		this.image=image;
	}
	public boolean[][] createMap(BufferedImage picture)// Because x & y are irrelevant here, do not bother changing i & j places in map array
	{
		rgb = getArray(picture);
		boolean[][] map = new boolean[rgb.length][rgb[0].length];
		//map = useHsl(map, image, hmin, hmax, smin, lmin, lmax);
		map=useHsv(map, rgb);
		//map=advancedHSV(map, image);
		// LightHSL is experimental idea, more lenient towards pixels surrounded by alive cells
		// map=lightHsl(map,image, hmin, hmax, smin, lmin, lmax);
		return map;
	}
	public static int[][][] getArray(BufferedImage image)
	{

		final byte[] pixels = ((DataBufferByte) image.getRaster()
				.getDataBuffer()).getData();
		final int width = image.getWidth();
		final int height = image.getHeight();
		final boolean hasAlphaChannel = image.getAlphaRaster() != null;

		int[][][] result = new int[height][width][4];
		if (hasAlphaChannel)
		{
			final int pixelLength = 4;
			for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel += pixelLength)
			{
				/*
				 * int argb = 0; argb += (((int) pixels[pixel] & 0xff) << 24);
				 * // alpha argb += ((int) pixels[pixel + 1] & 0xff); // blue
				 * argb += (((int) pixels[pixel + 2] & 0xff) << 8); // green
				 * argb += (((int) pixels[pixel + 3] & 0xff) << 16); // red
				 */// Code from where I copy pas- *ahem* made myself
					// argb = (int) pixels[pixel + color];

				// Order goes in red, green, blue, alpha
				result[row][col][0] = (int) pixels[pixel + 3] & 0xff;// red
				result[row][col][1] = (int) pixels[pixel + 2] & 0xff;// green
				result[row][col][2] = (int) pixels[pixel + 1] & 0xff;// blue
				result[row][col][3] = (int) (pixels[pixel]) & 0xff;// alpha
				col++;
				if (col == width)
				{
					col = 0;
					row++;
				}
			}
		} else
		{
			final int pixelLength = 3;
			for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel += pixelLength)
			{
				// int argb = 0;
				/*
				 * argb += -16777216; // 255 alpha argb += ((int) pixels[pixel]
				 * & 0xff); // blue argb += (((int) pixels[pixel + 1] & 0xff) <<
				 * 8); // green argb += (((int) pixels[pixel + 2] & 0xff) <<
				 * 16); // red
				 */
				// argb=(int) pixels[pixel+(color-1)];//code for specific color,
				// replace with rgb

				// Order goes in red, green, blue, alpha
				result[row][col][0] = (int) pixels[pixel + 2] & 0xff;// red
				result[row][col][1] = (int) pixels[pixel + 1] & 0xff;// green
				result[row][col][2] = (int) pixels[pixel] & 0xff;// blue
				result[row][col][3] = 255;// alpha
				col++;
				if (col == width)
				{
					col = 0;
					row++;
				}
			}
		}

		return result;
	}
	public static boolean[][] useHsv(boolean[][] map, int[][][] image)
	{
		//Modified code from useHsl
		for (int i = 0; i < image.length; i++)
		{
			for (int j = 0; j < image[0].length; j++)
			{
				boolean valid = true;
				int red = image[i][j][0];
				int green = image[i][j][1];
				int blue = image[i][j][2];
				
				int foo=green+blue-((red*2)+Math.abs(green-blue));
				if(foo<100)
				{
					valid=false;
				}
				map[i][j] = valid;
			}
		}
		return map;
	}
}
