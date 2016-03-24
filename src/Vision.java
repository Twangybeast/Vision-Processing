import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;

public class Vision{
	//Because some code was copy pas- *ahem* written by me, arrays that represent images are weird as they work as map[y][x]
	//Detail:the reason for this is because the objects were created as [row][column], this is found in getArray where you find [height][width]
	final double viewAngle=1.515;
	/*LIST OF VIEW ANGLES add when found
	 * MICROSOFT Lifecam HD-3000= {1.515 }
	 *
	 *
	 *
	 *
	 *
	 *
	 */
	final int colorThreshold=130;
	final int colorDifference=30;
	//red    =0
	//green    =1
	//blue    =2
	//
	//Lots of variables to adjust HSL threshold, naming scheme initial of hsl type and min/max threshold, inclusive; Default GRIP tutorial
	final int hmin=63;
	final int hmax=96;
	final int smin=20;
	final int smax=255;//This value not used as it is maximum. If changed go to createMap method to add into code
	final int lmin=40;
	final int lmax=161;
	//End of lots of variables
	final double totalPercent=0.0006510416666666666;
	int minimumAlive=200;//Minimum alive cells in particle to be considered particle.
	double[] profileMin=new double[100];
	double[] profileMax=new double[100];
	int[] tWidth;//Width based on equivalent rectangle
	int[] tHeight;//Height based on equivalent rectangle
	double[] angles;
	Point[] targetLocations;
	Point[] COMasses;
	final int furthestDistance=400;
	public double[] process(boolean[][] map)
	{
		minimumAlive=(int) (totalPercent*(map.length*map[0].length));
		double[] toReturn=new double[4];//Returns center of mass, x returns 2 when none detected
		//toReturn[0]   x position of target, in coordinate system of -1.0 to 1.0 left to right
		//toReturn[1]   y position of target, in coordinate system of -1.0 to 1.0 bottom to top
		//toReturn[2] distance to target, units should be inches
		//toReturn[3] angle of the target, in radians
		ArrayList<Particle> particles=null;
		long start=System.currentTimeMillis();
		particles=findParticles(map);
		System.out.println("Particle Finding Time: "+(System.currentTimeMillis()-start));
		System.out.println("Particles Found: "+particles.size());
		if(particles.size()==0)//No targets detected
		{
			toReturn[0]=2.0;
			toReturn[1]=2.0;
			toReturn[2]=0.0;
			toReturn[3]=100.0;
			return toReturn;
		}
		ArrayList<int[]> score=new ArrayList<int[]>();
		generateProfile();
		tWidth=new int[particles.size()];
		tHeight=new int[particles.size()];
		targetLocations=new Point[particles.size()];
		COMasses=new Point[particles.size()];
		angles=new double[particles.size()];
		for(int i=0;i<particles.size();i++)
		{
			centerMass(particles.get(i),i);
			int[] Score=new int[5];
			//less is better
			//Score[0]=CoverageArea(particles.get(i));
			Score[1]=equivalentRectangle(particles.get(i),i); //I couldn't quite figure out how to get this to work
			//Score[2]=moment(particles.get(i),i);
			//Score[3]=xyprofile(particles.get(i));
			//Score[4]=Score[0]+Score[1]+Score[2]+Score[3];
			Score[4]=0-particles.get(i).count;//Very stupid method that should work better
			score.add(Score);
		}
		boolean impossibleTarget=true;
		while(impossibleTarget)
		{
			int recordIndex=0;
			int record=99999;//Absurd number that is easy to beat
			for(int i=0;i<score.size();i++)
			{
				if(score.get(i)[4]<record)
				{
					recordIndex=i;
					record=score.get(i)[4];
				}
			}
			//Distance calculation
			toReturn[2]=(20.0*map[0].length)/(2.0*tWidth[recordIndex]*Math.tan(viewAngle/2.0));
			if(toReturn[2]<furthestDistance)
			{
				impossibleTarget=false;
				Particle particle=particles.get(recordIndex);
				//This option finds center of mass. With y from top of target according to the equivalent rectangle
				/*toReturn[0]=COMasses[recordIndex].getX()+particle.getX();
				toReturn[1]=tHeight[recordIndex];
				if(toReturn[1]==0)
				{
					toReturn[1]=particle.getHeight();
				}
				toReturn[1]=(Math.abs(particle.getHeight()-toReturn[1])/2.0)+particle.getY();*/
				toReturn[0]=targetLocations[recordIndex].getX()+particle.getX();
				toReturn[1]=targetLocations[recordIndex].getY()+particle.getY();
				//Visual Demo
				/*Demo demo=new Demo(map[0].length,map.length,map);
				demo.particleTest(demo.getGraphics(),particles,recordIndex);
				demo.drawPoint(demo.getGraphics(), (int)(toReturn[0]), (int)(toReturn[1]));
				*///demo.drawPoint(demo.getGraphics(), (int)(COMasses[recordIndex].getX()+particle.getX()), (int)(COMasses[recordIndex].getY()+particle.getY()),Color.GREEN);
				//Converts coordinate system to domain & range of -1 to 1, centered on center of image
				toReturn[0]=((toReturn[0])-(map[0].length/2.0))/(map[0].length/2.0);
				toReturn[1]=-1.0*((toReturn[1])-(map.length/2.0))/(map.length/2.0);
				toReturn[3]=angles[recordIndex];
			}
			else
			{
				int[] badScore={200,200,200,200,800};
				score.set(recordIndex, badScore);
			}
		}
		return toReturn;
	}
	private int equivalentRectangle(Particle particle, int index)
	{
		int score=0;
		Point upperLeft=new Point();
		Point lowerLeft=new Point();
		Point upperRight=new Point();
		Point lowerRight=new Point();
		double ulrecord=0;//Absurd numbers ez to beat
		double llrecord=0;
		double urrecord=0;
		double lrrecord=0;
		Point center=COMasses[index];
		double distance;
		//Scan Quadrant I
		for(int i=(int) (particle.getWidth()/2.0);i<particle.getWidth();i++)
		{
			for(int j=0;j<particle.getHeight()/2.0;j++)
			{
				if(particle.getLocalValue(i, j))
				{
					distance=distance(new Point(i,j),center);
					if(distance>urrecord)
					{
						urrecord=distance;
						upperRight=new Point(i,j);
					}
				}
			}
		}
		//Scan Quadrant II
		for(int x=0;x<particle.getWidth()/2.0;x++)
		{
			for(int y=0;y<particle.getHeight()/2.0;y++)
			{
				if(particle.getLocalValue(x,y))
				{
					distance=distance(new Point(x,y), center);
					if(distance>ulrecord)
					{
						ulrecord=distance;
						upperLeft=new Point(x,y);
					}
				}
			}
		}
		//Scan Quadrant III
		for(int x=0; x<particle.getWidth()/2.0;x++)
		{
			for(int y=(int) (particle.getHeight()/2.0);y<particle.getHeight();y++)
			{
				if(particle.getLocalValue(x,y))
				{
					distance= distance(new Point(x,y),center);
					if(distance>llrecord)
					{
						llrecord=distance;
						lowerLeft=new Point(x,y);
					}
				}
			}
		}
		//Scan Quadrant IV
		for(int x=particle.getWidth()/2;x<particle.getWidth();x++)
		{
			for(int y=particle.getHeight()/2;y<particle.getHeight();y++)
			{
				if(particle.getLocalValue(x, y))
				{
					distance=distance(new Point(x,y),center);
					if(distance>lrrecord)
					{
						lrrecord=distance;
						lowerRight=new Point(x,y);
					}
				}
			}
		}
		//Finds the most Invalid point
		short angleDiff=0;
		double furthestDistance=distance(new Point(particle.getWidth()-1,0),upperRight);;
		//Finds furthest point from corner
		distance=distance(new Point(0,0),upperLeft);
		if(distance>furthestDistance)
		{
			angleDiff=1;
			furthestDistance=distance;
		}
		distance=distance(new Point(0,particle.getHeight()-1),lowerLeft);
		if(distance>furthestDistance)
		{
			angleDiff=2;
			furthestDistance=distance;
		}
		distance=distance(new Point(particle.getWidth()-1,particle.getHeight()-1),lowerRight);
		if(distance>furthestDistance)
		{
			angleDiff=3;
		}
		double width=0;
		double height=0;
		Point location;
		Point bottom;
		double adjacent;
		switch(angleDiff)
		{
			case 0:
				width=distance(lowerLeft,lowerRight);
				height=distance(lowerLeft,upperLeft);
				bottom=new Point((int)((lowerLeft.getX()+lowerRight.getX())/2.0),(int)((lowerLeft.getY()+lowerRight.getY())/2.0));
				location=new Point((int)((upperLeft.getX()-lowerLeft.getX())+bottom.getX()),(int)(bottom.getY()+upperLeft.getY()-lowerLeft.getY()));
				targetLocations[index]=location;
				/*location=COMasses[index];
				trueCenter=new Point((int)(Math.abs(upperLeft.getX()+lowerRight.getX())/2.0),(int)(Math.abs(upperLeft.getY()+lowerRight.getY())/2.0));
				angles[index]=Math.atan(((location.getX()-trueCenter.getX())/(trueCenter.getY()-location.getY())));
				*/
				adjacent=lowerRight.getX()-lowerLeft.getX();
				if(adjacent==0)
				{
					adjacent=0.000000001;
				}
				angles[index]=Math.atan((lowerRight.getY()-lowerLeft.getY())/(adjacent));
				break;
			case 1:
				width=distance(lowerLeft,lowerRight);
				height=distance(lowerRight,upperRight);
				bottom=new Point((int)((lowerLeft.getX()+lowerRight.getX())/2.0),(int)((lowerLeft.getY()+lowerRight.getY())/2.0));
				location=new Point((int)((upperRight.getX()-lowerRight.getX())+bottom.getX()),(int)(bottom.getY()+upperRight.getY()-lowerRight.getY()));
				targetLocations[index]=location;
				/*location=COMasses[index];
				trueCenter=new Point((int)(Math.abs(upperRight.getX()+lowerLeft.getX())/2.0),(int)(Math.abs(upperRight.getY()+lowerLeft.getY())/2.0));
				angles[index]=Math.atan(((location.getX()-trueCenter.getX())/(trueCenter.getY()-location.getY())));
				*/
				adjacent=lowerRight.getX()-lowerLeft.getX();
				if(adjacent==0)
				{
					adjacent=0.000000001;
				}
				angles[index]=Math.atan((lowerRight.getY()-lowerLeft.getY())/(adjacent));
				break;
			case 2:
				width=distance(upperLeft,upperRight);
				height=distance(upperRight,lowerRight);
				location=new Point((int)((upperLeft.getX()+upperRight.getX())/2.0),(int)((upperLeft.getY()+upperRight.getY())/2.0));
				targetLocations[index]=location;
				/*location=COMasses[index];
				trueCenter=new Point((int)(Math.abs(upperLeft.getX()+lowerRight.getX())/2.0),(int)(Math.abs(upperLeft.getY()+lowerRight.getY())/2.0));
				angles[index]=Math.atan(((location.getX()-trueCenter.getX())/(trueCenter.getY()-location.getY())));
				*/
				adjacent=upperRight.getX()-upperLeft.getX();
				if(adjacent==0)
				{
					adjacent=0.000000001;
				}
				angles[index]=Math.atan((upperRight.getY()-upperLeft.getY())/(adjacent));
				break;
			case 3:
				width=distance(upperLeft,upperRight);
				height=distance(lowerLeft,upperLeft);
				location=new Point((int)((upperLeft.getX()+upperRight.getX())/2.0),(int)((upperLeft.getY()+upperRight.getY())/2.0));
				targetLocations[index]=location;
				/*location=COMasses[index];
				trueCenter=new Point((int)(Math.abs(upperRight.getX()+lowerLeft.getX())/2.0),(int)(Math.abs(upperRight.getY()+lowerLeft.getY())/2.0));
				angles[index]=Math.atan(((location.getX()-trueCenter.getX())/(trueCenter.getY()-location.getY())));
				*/
				adjacent=upperRight.getX()-upperLeft.getX();
				if(adjacent==0)
				{
					adjacent=0.000000001;
				}
				angles[index]=Math.atan((upperRight.getY()-upperLeft.getY())/(adjacent));
				break;
		}
		tWidth[index]=(int) width;
		tHeight[index]=(int) height;
		double ratio=(width*1.0)/(height*1.0)*1.0;
		double ideal=1.6;
		score=(int)(100.0*(Math.abs(ratio-ideal)/(0.9)));
		if(score>100)
		{
			score=100;
		}
		return score;
	}
	private double distance(Point p, Point p2)
	{
		return Math.sqrt(Math.pow(p.x-p2.x,2)+Math.pow(p.y-p2.y,2));
	}
	/*private int xyprofile(Particle particle)
	{
		int score=100;
		for(int i=1;i<=100;i++)
		{
			int range=0;
			int columnCount=0;
			for(int x=(int) Math.floor(particle.getWidth()*((i-1)/100.0));x<(int) Math.ceil(particle.getWidth()*((i)/100.0));x++)
			{
				for(int y=0;y<particle.getHeight();y++)
				{
					range++;
					if(particle.getLocalValue(x, y))
					{
						columnCount++;
					}
				}
			}
			double average=columnCount/(range*1.0);
			if(average>(profileMin[i-1])&&average<(profileMax[i-1]))
			{
				score--;
			}
		}
		return score;
	}*/
	private void generateProfile()
	{
		for(int i=0;i<5;i++)
		{
			profileMin[i]=0.4+(i*0.04);
			profileMin[99-i]=0.4+(i*0.04);
			profileMin[i+5]=0.6+(i*-0.1);
			profileMin[94-i]=0.6+(i*-0.1);
		}
		for(int i=0;i<80;i++)
		{
			profileMin[i+10]=0.2;
		}
		for(int i=0;i<10;i++)
		{
			profileMax[i]=1.0;
			profileMax[99-i]=1.0;
			profileMax[i+10]=1.0+(i/-15.0);
			profileMax[89-i]=1.0+(i/-15.0);
		}
		for(int i=0;i<60;i++)
		{
			profileMax[i+20]=0.4;
		}
	}
	/*
	private int moment(Particle particle, int location)
	{
		int score=0;
		Point centerMass=COMasses[location];
		double moment=0;
		for(int y=0;y<particle.getHeight();y++)
		{
			for(int x=0;x<particle.getWidth();x++)
			{
				if(particle.getLocalValue(x,y))
				{
					moment=moment+(distance(new Point(x,y), centerMass));
				}
			}
		}
		double mass=80.0/(particle.count*1.0);
		moment=moment*mass;
		double ratio=moment/(particle.count*1.0);
		double ideal=2.092030503536954;
		score=(int)(100.0*(Math.abs(ratio-ideal)/(ideal)));
		if(score>100)
		{
			score=100;
		}
		return score;
	}*/
	private Point centerMass(Particle particle, int location)
	{
		int xTotal=0;
		int yTotal=0;
		for(int y=0;y<particle.getHeight();y++)
		{
			for(int x=0;x<particle.getWidth();x++)
			{
				if(particle.getLocalValue(x, y))
				{
					xTotal=xTotal+x;
					yTotal=yTotal+y;
				}
			}
		}
		Point toReturn=new Point((int) (xTotal/(particle.count*1.0)), (int) (yTotal/(particle.count*1.0)));
		COMasses[location]=toReturn;
		return toReturn;
	}
	/*
	private int CoverageArea(Particle particle)
	{
		int toReturn=0;
		double ratio=(particle.count*1.0)/(particle.getWidth()*particle.getHeight()*1.0)*1.0;//Random 1.0's to ensure quotient is a double
		double ideal=1.0/3.0;
		toReturn=(int)(100.0*(Math.abs(ratio-ideal)/(ideal)));
		if(toReturn>100)
		{
			toReturn=100;
		}
		return toReturn;
	}*/
	private ArrayList<Particle> findParticles(boolean[][] map)//Generates rectangles for every point
	{
		ArrayList<Particle> toReturn=new ArrayList<Particle>();
		ArrayList<Particle> smallParticles=new ArrayList<Particle>();
		int iStart = 0, jStart=0, iMax=0, jMax=0;
		iMax=map[0].length;
		jMax=map.length;
		for (int i=iStart;i<iMax;i++)
		{
			for(int j=jStart;j<jMax;j++)
			{
				if(map[j][i])
				{
					boolean Continue=true;//Messy code to check if point was in rectangle
					for(Particle particle:toReturn)
					{
						if(particle.globalInMap(i,j))
						{
							if(particle.getGlobalValue(i, j))//Don't create a new rectangle if it already is in one
							{ 
								Continue=false;
								break;
							}
						}
					}
					for(Particle particle:smallParticles)
					{
						if(particle.globalInMap(i,j))
						{
							if(particle.getGlobalValue(i, j))//Don't create a new rectangle if it already is in one
							{ 
								Continue=false;
								break;
							}
						}
					}
					if(Continue)//Generates new rectangle
        			{
        				Particle particle=new Particle(i,j,new boolean[1][1]);
        				particle.map[0][0]=true;
        				boolean change=true;
        				Particle expansion=new Particle((int)(particle.getX()),(int)(particle.getY()),new boolean[1][1]);
        				if(particle.getX()>0)
        				{
        					expansion.expandLeft();
        					expansion.setGlobalValue((int)(particle.getX()-1),(int)(particle.getY()),true);
        				}
        				if(particle.getY()>0)
        				{
        					expansion.expandUp();
        					expansion.setGlobalValue((int)(particle.getX()),(int)(particle.getY()-1),true);
        				}
        				if(particle.getX()<map[0].length-1)
        				{
        					expansion.expandRight();
        					expansion.setGlobalValue((int)(particle.getX()+1),(int)(particle.getY()),true);
        				}
        				if(particle.getY()<map.length-1)
        				{
        					expansion.expandDown();
        					expansion.setGlobalValue((int)(particle.getX()),(int)(particle.getY()+1),true);
        				}
        				int x;
        				int y;
        				while(change)
        				{
        					Particle next=new Particle((int)(expansion.getX()),(int)(expansion.getY()),new boolean[expansion.map.length][expansion.map[0].length]);
        					change=false;
        					for(int k=0;k<expansion.getWidth();k++)
        					{
        						for(int l=0;l<expansion.getHeight();l++)
        						{
        							//Compare to picture map values to determine expansion
        							if(expansion.getLocalValue(k,l))
        							{	
        								x=(int) (k+expansion.getX());
        								y=(int) (l+expansion.getY());
        								if(map[y][x])
        								{
        									change=true;
        									//Expand the particle into that square
        									//Determines if size increase of particle required
        									if(x-particle.getX()<0)
        									{
        										particle.expandLeft();
        									}
        									if(x-particle.getX()>=particle.getWidth())
        									{
        										particle.expandRight();
        									}
        									if(y-particle.getY()<0)
        									{
        										particle.expandUp();
        									}
        									if(y-particle.getY()>=particle.getHeight())
        									{
        										particle.expandDown();
        									}
        									//Sets particle value
        									particle.count++;
        									particle.setGlobalValue(x, y, true);
        									//Prepares expansion for next cycle
        									//Make surrounding position of new particle true
        									//Top Side
        									//Check if space in global map
        									if(y>0)
        									{
        										//Check if expansion neccessary
        										while(y-1-next.getY()<0)
        										{
        											next.expandUp();
        										}
        										next.setGlobalValue(x, y-1, true);
        									}
        									//Left side
        									if(x>0)
        									{
        										//Check if expansion neccessary
        										while(x-1-next.getX()<0)
        										{
        											next.expandLeft();
        										}
        										next.setGlobalValue(x-1, y, true);
        									}
        									//Bottom side
        									if(y+1<map.length)
        									{
        										//Check if expansion neccessary
        										while(y+1-next.getY()>=next.getHeight())
        										{
        											next.expandDown();
        										}
        										next.setGlobalValue(x, y+1, true);
        									}
        									//Right Side
        									if(x+1<map[0].length)
        									{
        										//Check if expansion neccessary
        										while(x+1-next.getX()>=next.getWidth())
        										{
        											next.expandRight();
        										}
        										next.setGlobalValue(x+1, y, true);
        									}
        								}
        							}
        						}
        					}
        					for(int k=0;k<particle.getWidth();k++)
        					{
        						for(int l=0;l<particle.getHeight();l++)
        						{
        							if(particle.getLocalValue(k, l))
        							{
        								x=(int) (k+particle.getX());
        								y=(int) (l+particle.getY());
        								if(x-next.getX()>=0&&x-next.getX()<next.getWidth())
        								{
        									if(y-next.getY()>=0&&y-next.getY()<next.getHeight())
        									{
        										next.setLocalValue((int)(x-next.getX()), (int)(y-next.getY()), false);
        									}
        								}
        							}
        						}
        					}
        					if(change)
        					{
        						next.shorten();
        						expansion=next;
        					}
        				}
        				if(particle.count>=minimumAlive)
        				{
        					toReturn.add(particle);
        				}
        			}
				}
			}
		}
		System.out.println("Small Particles Size: "+smallParticles.size());
		return toReturn;
	}
	private int[][][] getArray(BufferedImage image) {

		final byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		final int width = image.getWidth();
		final int height = image.getHeight();
		final boolean hasAlphaChannel = image.getAlphaRaster() != null;

		int[][][] result = new int[height][width][4];
		if (hasAlphaChannel) {
			final int pixelLength = 4;
			for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel += pixelLength) {
				/*int argb = 0;
               argb += (((int) pixels[pixel] & 0xff) << 24); // alpha
               argb += ((int) pixels[pixel + 1] & 0xff); // blue
               argb += (((int) pixels[pixel + 2] & 0xff) << 8); // green
               argb += (((int) pixels[pixel + 3] & 0xff) << 16); // red*/ //Code from where I copy pas- *ahem* made myself
				//argb = (int) pixels[pixel + color];

				//Order goes in red, green, blue, alpha
				result[row][col][0] = (int)pixels[pixel+3]& 0xff;//red
				result[row][col][1] = (int)pixels[pixel+2]& 0xff;//green
				result[row][col][2] = (int)pixels[pixel+1]& 0xff;//blue
				result[row][col][3] = (int)(pixels[pixel])& 0xff;//alpha
				col++;
				if (col == width) {
					col = 0;
					row++;
				}
			}
		} else {
			final int pixelLength = 3;
			for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel += pixelLength) {
				//int argb = 0;
				/*argb += -16777216; // 255 alpha
               argb += ((int) pixels[pixel] & 0xff); // blue
               argb += (((int) pixels[pixel + 1] & 0xff) << 8); // green
               argb += (((int) pixels[pixel + 2] & 0xff) << 16); // red*/
				//argb=(int) pixels[pixel+(color-1)];//code for specific color, replace with rgb

				//Order goes in red, green, blue, alpha
				result[row][col][0] = (int)pixels[pixel+2]& 0xff;//red
				result[row][col][1] = (int)pixels[pixel+1]& 0xff;//green
				result[row][col][2] = (int)pixels[pixel]& 0xff;//blue
				result[row][col][3] = 255;//alpha
				col++;
				if (col == width) {
					col = 0;
					row++;
				}
			}
		}

		return result;
	}
	public boolean inMap(boolean[][] map, int x, int y)
	{
		if(x<0||y<0)
		{
			return false;
		}
		if(y<map.length&&x<map[0].length)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	public boolean[][] createMap(BufferedImage picture)//Because x & y are irrelevant here, do not bother changing i & j places in map array
	{
		int totalAlive=0;
		long start=System.currentTimeMillis();
		int[][][] image=getArray(picture);
		boolean[][] map=new boolean[image.length][image[0].length];
		for(int i=0;i<image.length;i++)//Converts array into map of reflected light, based on color threshold
		{
			for(int j=0;j<image[0].length;j++)
			{
				boolean valid=false;
				//Place analysis per pixel here
				int red=image[i][j][0];
				int green=image[i][j][1];
				int blue = image[i][j][2];
				//Option 1: Basic Color Scan, compares value of determined color to predetermined threshold
				/*if(image[i][j][1]>=colorThreshold&&image[i][j][2]>=colorThreshold&&image[i][j][0]<=colorThreshold&&(image[i][j][2]+image[i][j][1])/2.0-image[i][j][0]>colorDifference)
				{
					valid=true;
				}
				*/ //Option 2: Replicates method from grip tutorial.
                int[] hsl=getHSL(red, green, blue);
                //Using values from GRIP tutorial, adjust values as necessary at top of class
                if(hsl[0]>=hmin&&hsl[0]<=hmax)//Hue. Using nested if statements for clarity of reading. Don't change it.
                {
                    if(hsl[1]>=smin)//Saturation. Notice that there is no max value detection as we are only reading for min. Change as needed.
                    {
                        if(hsl[2]>=lmin&&hsl[2]<=lmax)//Luminance. All the other ones have long comments so I'm typing here too
                        {
                        	totalAlive++;
                            valid=true;
                        }
                    }
                }
				//Not actually an option. Just code that everyone uses.
				map[i][j]=valid;
			}
		}
		System.out.println("Create Map: "+(System.currentTimeMillis()-start));
		System.out.println("Percentage True: "+(100*(1.0*totalAlive)/(map[0].length*map.length)));
		return map;
	}
	public int[] getHSL(int red, int green, int blue)
	{
		int[] hsl=new int[3];//Self explanatory, array with elements in order of hue, saturation, and luminance
		//ranges noted below of hsl, inclusively
		double r=(red*1.0)/255.0;
		double g=(green*1.0)/255.0;
		double b=(blue*1.0)/255.0;
		double min=Math.min(r, g);
		min=Math.min(min, b);//Because math only compares 1 number at a time, repeated operation for blue as well
		double max=Math.max(r, g);
		max=Math.max(max, b);
		hsl[2]=(int) (((1.0*(min+max))/2.0)*255);//Luminance Calculation 0-255
		if(min==max)//Certain circumstance for saturation
		{
			hsl[1]=0;
			hsl[0]=0;//Hue as well to avoid dividing by zero;
		}
		else
		{
			//Magical saturation equation, don't ask questions unless I'm wrong, which i'm not, cause
			//http://www.niwa.nu/2013/05/math-behind-colorspace-conversions-rgb-hsl/ says so
			if(hsl[2]<128)
			{
				hsl[1]=(int) ((((max-min)*1.0)/((max+min)*1.0))*255.0);
			}
			else
			{
				hsl[1]=(int) (((max-min)/(2.0-max-min))*255.0);
			}
			//Finally, the hue calculation
			if(r==max)
			{
				hsl[0]=(int) (((g-b)/(max-min))*30.0);
			}
			else
			{
				if(g==max)
				{
					hsl[0]=(int) ((2.0+((b-r)/(max-min)))*30.0);
				}
				else
				{
					//b is max
					hsl[0]=(int) ((4.0+((r-g)/(max-min)))*30.0);
				}
			}
		}
		return hsl;
	}
}