package code2017;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.PrintStream;
import java.util.ArrayList;

import edgedetector.Conv;
import edgedetector.EdgeDetector;
import edgedetector.EdgeFiller;
import logger.FileLogger;
import logger.Logger;

import java.awt.Rectangle;

import visionCore.RGB;
import visionCore.Vision;
//NOTE: When using on RoboRIO, comment out all classes using BufferedImage
public class Vision17 
{
	//Time Variables. Delete all when done.
	private long[] t=new long[3];
	private BufferedImage image1=null;
	private BufferedImage image2=null;
	private Dimension image=null;
	static ScoreType[] SCORE_PATTERN = 
		{
				ScoreType.EQUIV_RECT,
				ScoreType.COVERAGE,
				ScoreType.PROFILE,
				ScoreType.GREENESS,
				ScoreType.CENTERNESS
		};
	public static double VIEW_ANGLE= Math.toRadians(67.9446)/2;
	public final static double MIN_SIZE_RATIO = 500.0/(640.0*480.0);
	
	public boolean[][] map=null;
	public int[][][] rgb=null;
	public ArrayList<Particle> edges=null;
	public ArrayList<Particle> particles=null;
	public Particle particle=null;
	public Particle pair=null;
	
	public Property property=Property.getIdealGear();
	Logger fl = null;
	//Called when vision is initialized, preferably before autonomous begins
	public void init()
	{
		
	}
	//Called when queued for processing
	public Target exec()
	{
		int roots;
		if((roots=FileLogger.decideLoggerType())>=2)
		{
			fl = new FileLogger("D:\\log\\log."+System.currentTimeMillis()+".txt");
			fl.printf("INFO: Multiple roots found. [%d] Roots. Printing to file."+System.lineSeparator(), roots);
		}
		else
		{
			fl=new Logger();
			fl.printf("INFO: Not enough roots found. [%d] root. Printing to console."+System.lineSeparator(), roots);
		}
		t[0]=System.currentTimeMillis();
		t[1]=System.currentTimeMillis();
		//rgb=getDualImage(image1, image2);
		rgb=getSingleImage(image2);
		fl.printf("INFO: Dual Image Time: [%d] ms"+System.lineSeparator(), System.currentTimeMillis()-t[0]);
		t[0]=System.currentTimeMillis();
		t[2]=System.currentTimeMillis();
		image = new Dimension(image2.getWidth(), image2.getHeight());
		ArrayList<Particle> particles;
		EdgeDetector ed=new EdgeDetector(4);
		fl.printf("INFO: Misc. Variable setting time: [%d]"+System.lineSeparator(), System.currentTimeMillis()-t[2]);
		t[2]=System.currentTimeMillis();
		ed.init(Conv.generateFloatMap(rgb));
		fl.printf("INFO: EdgeDetector.init time: [%d]"+System.lineSeparator(), System.currentTimeMillis()-t[2]);
		t[2]=System.currentTimeMillis();
		ed.execSingle();
		fl.printf("INFO: Edge Detector exec time: [%d]"+System.lineSeparator(), System.currentTimeMillis()-t[2]);
		fl.printf("INFO: Edge find time: [%d] ms"+System.lineSeparator(), System.currentTimeMillis()-t[0]);
		t[0]=System.currentTimeMillis();
		ArrayList<Particle> edges=ed.getEdges();
		this.edges=edges;
		findCorners(edges);
		filterShortParticles(edges, 7);
		particles=EdgeFiller.fillEdgeTest(edges);
		filterInvalidParticles(particles, image);
		if(particles.size()==0)
		{
			fl.println("WARNING: No edges detected. Returning NULL target.");
			fl.close();
			return Target.getNullTarget();
		}
		particles=matchParticles(particles);
		filterSmallParticles(particles, (int)(MIN_SIZE_RATIO * image.getWidth()*image.getHeight()));
		this.particles=particles;
		for (int i=0; i<particles.size();i++)
		{
			particles.get(i).scores=new Score[6];
			particles.get(i).scores[0]=diagRect(particles.get(i));
			particles.get(i).scores[1]=coverage(particles.get(i));
			//particles.get(i).scores[2]=moment(particles.get(i));
			particles.get(i).scores[3]=profile(particles.get(i));
			particles.get(i).scores[4]=green(particles.get(i));
			particles.get(i).scores[5]=center(particles.get(i));
		}
		
		evaluateScoreBoiler(particles);
		//Really simple code that needs to be updated
		filterFarParticles(particles, 240.0);
		if(particles.size()==0)
		{
			fl.println("WARNING: All particles filtered. Returning NULL particle.");
			fl.close();
			return Target.getNullTarget();
		}
		Particle particle = findBestParticle(particles);
		this.particle=particle;
		Particle pair= pairParticles(particle, particles, particles.indexOf(particle));
		this.pair=pair;
		if(pair==null)
		{
			fl.println("INFO: Pair not found. Returning partial target.");
			fl.close();
			Target target=new Target();
			target=setSingleRectanglePosition(target, particle, image);
			printTarget(target);
			return target;
		}
		Particle[] pairs= new Particle[2];
		pairs[0]=particle;
		pairs[1]=pair;
		Target target=setGearPairPosition(new Target(), pairs, this.image);
		if(target==null)
		{
			fl.println("WARNING: Found null target. Need investigation. Returning NULL target.");
			fl.close();
			return Target.getNullTarget();
		}
		printTarget(target);
		return target;
	}
	public static void findCorners(ArrayList<Particle> particles)
	{
		for(int i=0;i<particles.size();i++)
		{
			diagRect(particles.get(i));
		}
	}
	public static Score diagRect(Particle particle)
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
		double angleRatio1=((particle.corners[2].y-particle.corners[3].y)/(1.0*(particle.corners[3].x-particle.corners[2].x)));
		double angleRatio2=((particle.corners[0].y-particle.corners[1].y)/(1.0*(particle.corners[1].x-particle.corners[0].x)));
		particle.setAngle(Math.atan((angleRatio1+angleRatio2)/2));
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
		fl.printf("Moment of inertia: [%f]"+System.lineSeparator(),momentOfInertia);
		//fl.printf("Mx: [%f]\tMy: [%f]\t Md: [%f]\t Mz: [%f]"+System.lineSeparator(),mx/(particle.count*1.0),my/(particle.count*1.0),md/(particle.count*1.0), mz/(particle.count*1.0));
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
		double ratio=((greeness*1.0)/(particle.count*127.5));
		return new Score(ratio, ScoreType.GREENESS);
	}
	public Score center(Particle particle)
	{
		Point p = getParticleCenter(particle);
		int dx = (int) Math.abs(p.x-(image.getWidth()/2));
		int dy = (int) Math.abs(p.y-(image.getHeight()/2));
		double ratio = Math.sqrt(Math.pow( (dx*0.5/image.getWidth()), 2) + Math.pow((dy*0.5/image.getHeight()), 2));
		return new Score(ratio, ScoreType.CENTERNESS);
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
			for(int j=0; j<particles.get(i).scores.length;j++)
			{
				int s=0;
				Score score =particles.get(i).scores[j];
				if(score!=null)
				{
					s=score.getScore(property);
				}
				totalScore= totalScore + s;
			}
			particles.get(i).score=totalScore;
		}
	}
	public static void filterInvalidParticles(ArrayList<Particle> particles, Dimension d)
	{
		while(particles.remove(null)){};
		for(int i=0;i<particles.size();i++)
		{
			Particle p=particles.get(i);
			if(p.x < 0 || p.y < 0 || p.x+p.getWidth() > d.width || p.y+p.getHeight() > d.height)
			{
				for(int x=0;x<p.getWidth();x++)
				{
					for(int y=0;y<p.getHeight();y++)
					{
						if(x + p.x< 0 || y + p.y< 0 || x+p.x+p.getWidth() > d.width || y+p.y+p.getHeight() > d.height)
						{
							p.setLocalValue(x, y, false);
						}
					}
				}
				p.recount();
				if(p.count==0)
				{
					particles.set(i, null);
				}
				else
				{
					p.shorten();
				}
			}
		}
		while(particles.remove(null)){};
	}
	
	private void filterFarParticles(ArrayList<Particle> particles, double maxDistance)
	{
		ArrayList<Particle> disqualified= new ArrayList<Particle>();
		for(int i=0;i<particles.size();i++)
		{
			findParticleDistance(particles.get(i), 5.0, (int)(image.getHeight()));
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
	private ArrayList<Particle> filterSmallParticles(ArrayList<Particle> particles, int minSize)
	{
		ArrayList<Particle> toRemove = new ArrayList<Particle>();
		for(Particle particle: particles)
		{
			if(particle.count<minSize)
			{
				toRemove.add(particle);
			}
		}
		particles.removeAll(toRemove);
		return particles;
	}
	private ArrayList<Particle> filterShortParticles(ArrayList<Particle> particles, int minHeight)
	{
		ArrayList<Particle> toRemove = new ArrayList<Particle>();
		for(Particle particle: particles)
		{
			if(Math.min(particle.getWidth(), particle.getHeight())<minHeight)
			{
				toRemove.add(particle);
			}
		}
		particles.removeAll(toRemove);
		return particles;
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
	public Particle pairParticles(Particle particle, ArrayList<Particle> particles, int ignoreIndex)
	{
		Particle pair=null;
		//Note: L/R depicts the position of the CURRENT particle, not the pair
		Point centerR=getParticleCenter(particle);
		Point centerL=getParticleCenter(particle);
		int centerSeparationL=(int) (8.25*particle.getTHeight()/5.0);
		int centerSeparationR=(int) -centerSeparationL;
		int dx=(int)(Math.cos(particle.getAngle())*centerSeparationL);
		int dy=(int)(Math.sin(particle.getAngle())*centerSeparationL);
		centerL.translate(dx, dy);
		dx = (int)(Math.cos(particle.getAngle())*centerSeparationR);
		dy = (int)(Math.sin(particle.getAngle())*centerSeparationR);
		centerR.translate(dx, dy);
		double lowestScore=Double.POSITIVE_INFINITY;
		for(int i=0;i<particles.size();i++)
		{
			if(i!=ignoreIndex)
			{
				double distance= Math.min(distance(centerL, getParticleCenter(particles.get(i))), distance(centerR, getParticleCenter(particles.get(i))));
				int score=particles.get(i).score;
				if(distance+score<lowestScore && distance < particle.getTHeight()*2.5)
				{
					if((particles.get(i).count*1.0) / particle.count > 0.1 )
					{
						lowestScore = distance+score;
						pair= particles.get(i);
					}
				}
			}
		}
		if(pair==null)
		{
			fl.println("INFO: Pairless particle detected.");
		}
		return pair;
	}
	public static Point getParticleCenter(Particle particle)
	{
		int x=0;
		int y=0;
		for(int i=0;i<particle.corners.length;i++)
		{
			x=x+particle.corners[i].x;
			y=y+particle.corners[i].y;
		}
		return new Point((int)((x*1.0)/particle.corners.length), (int)((y*1.0)/particle.corners.length));
	}
	public static ArrayList<Particle> matchParticles(ArrayList<Particle> particles)
	{
		ArrayList<Particle> newParticles=new ArrayList<Particle>();
		for(int i=0;i<particles.size();i++)
		{
			if(particles.get(i)!=null)
			{
				boolean addedParticle=false;
				for(int j=i+1;j<particles.size();j++)
				{
					if(particles.get(j)!=null)
					{
						int dx=Math.abs(particles.get(i).x-particles.get(j).x);
						int dy=Math.min(Math.abs(particles.get(i).y-(particles.get(j).y)+particles.get(j).getHeight()), Math.abs(particles.get(j).y-(particles.get(i).y)+particles.get(i).getHeight()));
						// ha ha! Good luck reading these lines.
						if(dx<=Math.max(Math.max(particles.get(i).getTWidth(), 10), particles.get(j).getTWidth())*1.0 
								&& dy<=Math.max(Math.max(particles.get(i).getHeight()*1.0, 20), particles.get(j).getHeight()*1.0) 
								&& (Math.abs(particles.get(i).getTWidth()-particles.get(j).getTWidth())*1.0)/Math.max(Math.max(particles.get(i).getTWidth(), 20),particles.get(j).getTWidth())<0.1
								&& !encaptulated(particles.get(i), particles.get(j)))
						{
							newParticles.add(mergeParticles(particles.get(i), particles.get(j)));
							addedParticle=true;
							particles.set(j, null);
							break;
						}
					}
				}
				if(!addedParticle)
				{
					newParticles.add(particles.get(i));
				}
			}
		}
		return newParticles;
	}
	public static boolean encaptulated(Particle p1, Particle p2)
	{
		Particle ptop, pbot;
		if(p1.y<p2.y)
		{
			ptop=p1;
			pbot=p2;
		}
		else
		{
			ptop=p2;
			pbot=p1;
		}
		if(ptop.y+ptop.getHeight()>pbot.y+pbot.getHeight())
		{
			return true;
		}
		if(ptop.y+ptop.getHeight()>pbot.y+5)
		{
			return true;
		}
		return false;
	}
	public static Particle mergeParticles(Particle p1, Particle p2)
	{
		Particle ptop;
		Particle pbottom;
		if(p1.y<p2.y)
		{
			ptop=p1;
			pbottom=p2;
		}
		else
		{
			ptop=p2;
			pbottom=p1;
		}
		Rectangle r1=new Rectangle(ptop.x, ptop.y, ptop.getWidth(), ptop.getHeight());
		Rectangle r2= (new Rectangle(pbottom.x, pbottom.y, pbottom.getWidth(), pbottom.getHeight()));
		Rectangle r;
		int rx=Math.min(ptop.x, pbottom.x);
		int rw=Math.max(pbottom.x+pbottom.getWidth(), ptop.x+ptop.getWidth())-rx;
		r = new Rectangle(rx, ptop.y, rw, pbottom.getHeight()+pbottom.y-ptop.y);
		Particle particle = new Particle(r.x, r.y, new boolean[(int)(r.getHeight())][(int)(r.getWidth())]);
		try
		{
		for(int x=ptop.x;x<ptop.getWidth()+ptop.x;x++)
		{
			for(int y=ptop.y;y<ptop.getHeight()+ptop.y;y++)
			{
				particle.setGlobalValue(x, y, ptop.getGlobalValue(x, y));
			}
		}
		}
		catch(ArrayIndexOutOfBoundsException e)
		{
			e.printStackTrace();
		}
		for(int x=pbottom.x;x<pbottom.getWidth()+pbottom.x;x++)
		{
			for(int y=pbottom.y;y<pbottom.getHeight()+pbottom.y;y++)
			{
				particle.setGlobalValue(x, y, pbottom.getGlobalValue(x, y));
			}
		}
		particle = EdgeFiller.drawLine(particle, ptop.corners[2], pbottom.corners[0]);
		particle = EdgeFiller.drawLine(particle, ptop.corners[3], pbottom.corners[1]);
		particle= EdgeFiller.fillEdge(particle, false);
		return particle;
	}
	public static Particle drawLine(Particle particle, Point p1, Point p2)
	{
		int y1=p1.y;
		int x1=p1.x;
		int y2=p2.y;
		int x2=p2.x;
		int dy= y2-y1;
		int dx= x2-x1;
		for(int y=y1;y<=y2;y++)
		{
			int x= x1 + dx * (y - y1)/dy;
			particle.setGlobalValue(x, y, true);
		}
		return particle;
	}
	private Target setGearTargetPosition(Target target, Particle particle, boolean[][] map)
	{
		boolean onLeft=onLeft(map, particle);
		Point center=new Point(particle.x+(particle.getWidth()/2), particle.y+(particle.getHeight()/2));
		int centerSeparation=(int) (4.125*particle.getTHeight()/5.0);
		if(!onLeft)
		{
			centerSeparation=-centerSeparation;

		}
		int dx=(int)(Math.cos(particle.getAngle())*centerSeparation);
		int dy=(int)(Math.sin(particle.getAngle())*centerSeparation);
		center.translate(dx, dy);
		particle.tLocation=new Point(center.x, center.y);
		target=findTargetFromParticle(particle);
		return target;
	}
	public Target setSingleRectanglePosition(Target target, Particle particle, Dimension image)
	{
		Point location = new Point(getParticleCenter(particle));
		double x = ((location.getX()) - (image.getWidth() / 2.0)) / (image.getWidth() / 2.0);
		double y = -1.0 * ((location.getY()) - (image.getHeight() / 2.0)) / (image.getHeight() / 2.0);
		double angle = (Math.atan(((particle.corners[2].y-particle.corners[3].y)/(1.0*(particle.corners[3].x-particle.corners[2].x))))+Math.atan(((particle.corners[0].y-particle.corners[1].y)/(1.0*(particle.corners[1].x-particle.corners[0].x)))))/2.0;
		double distance = findParticleDistance(particle, 5.0, image.height);
		target=new Target(x, y, angle, distance);
		target.singleTarget=true;
		return target;
	}
	public Target setGearPairPosition(Target target, Particle[] pair, Dimension image)
	{
		Point p1, p2;
		if(pair==null)
		{
			return null;
		}
		if(pair[0]==null||pair[1]==null)
		{
			fl.println("BAD STUFF HAPPENED! EXITING!");
			return null;
		}
		if(pair[0].x<pair[1].x)
		{
			p1= getParticleCenter(pair[0]);
			p2= getParticleCenter(pair[1]);
		}
		else
		{
			p1= getParticleCenter(pair[1]);
			p2= getParticleCenter(pair[0]);
		}
		Point location = new Point((p1.x+p2.x)/2, (p1.y+p2.y)/2);
		double x = ((location.getX()) - (image.getWidth() / 2.0)) / (image.getWidth() / 2.0);
		double y = -1.0 * ((location.getY()) - (image.getHeight() / 2.0)) / (image.getHeight() / 2.0);
		//Radians counterclockwise
		double angle= Math.atan(((p1.y-p2.y)/(1.0*(p2.x-p1.x))));	
		target = new Target(x, y, angle, findParticleDistance(distance(p1, p2), 8.25, (int)(image.getWidth())));
		return target;
	}
	private boolean onLeft(boolean[][] map, Particle particle)
	{
		int range=(int) (particle.getTHeight()*2.5);
		int leftScore=0;
		int heightUnit = particle.getHeight()/4;
		for(int i=0;i<3;i++)
		{
			Point p=new Point(particle.x, particle.y+(heightUnit*(i+1)));
			leftScore=leftScore+cellsInLineRange(map, p, -range);
		}
		int rightScore=0;
		for(int i=0;i<3;i++)
		{
			Point p=new Point(particle.x+particle.getWidth()-1, particle.y+(heightUnit*(i+1)));
			rightScore=rightScore+cellsInLineRange(map, p, range);
		}
		return rightScore>leftScore;
	}
	public static int cellsInLineRange(boolean[][] map, Point p, int range)
	{
		int total=0;
		int r;
		if(range<0)
		{
			r=-range;
			for(int i=0;i<r;i++)
			{
				int x = p.x-i;
				int y = p.y;
				if(x>=0&&y>=0&&x<map[0].length&&y<map.length)
				{
					if(map[y][x])
					{
						total++;
					}
				}
			}
		}
		else
		{
			r=range;
			for(int i=0;i<r;i++)
			{
				int x = p.x+i;
				int y = p.y;
				if(x>=0&&y>=0&&x<map[0].length&&y<map.length)
				{
					if(map[y][x])
					{
						total++;
					}
				}
			}
		}
		return total;
	}
	
	public static ArrayList<Particle> findParticles(boolean[][] map)// Generates rectangles for every point
	{
		final int minimumAlive = 700;
		
		boolean[][] mapCopy = Array2DCopier.copyOf(map);
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
		return toReturn;
	}
	
	public static double findParticleDistance(Particle particle, double idealHeight, int imageHeight)
	{
		double distance = idealHeight* imageHeight / (2 * particle.getTHeight()* Math.tan(VIEW_ANGLE*(3.0/4.0)));
		particle.distance=distance;
		return distance;
	}
	public static double findParticleDistance(double width, double idealwidth, int imagewidth)
	{
		double distance = idealwidth* imagewidth/ (2 * width* Math.tan(VIEW_ANGLE));
		return distance;
	}
	public static double distance(Point p, Point p2)
	{
		return Math.sqrt(Math.pow(p.x - p2.x, 2) + Math.pow(p.y - p2.y, 2));
	}
	public void printTarget(Target target)
	{
		fl.printf("INFO: Target: (%f, %f)"+System.lineSeparator(), target.x, target.y);
		fl.printf("INFO: Angle: [%f] degrees"+System.lineSeparator(), Math.toDegrees(target.angle));
		fl.printf("INFO: Distance: [%f] in."+System.lineSeparator(), target.distance);
		fl.printf("INFO: Miscellaneous time: [%d] ms"+System.lineSeparator(), System.currentTimeMillis()-t[0]);
		fl.printf("INFO: Total time: [%d] ms"+System.lineSeparator(), System.currentTimeMillis()-t[1]);
		fl.close();
	}
	public void setImage(BufferedImage image1, BufferedImage image2)
	{
		this.image1=image1;
		this.image2=image2;
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
		System.out.println("Alpha Channel: "+hasAlphaChannel);
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
	//Support if first is null
	public static int[][][] getDualImage(BufferedImage image1, BufferedImage image2)
	{
		int[][][] rgb1, rgb2;
		long t1=System.currentTimeMillis();
		rgb2=getArray(image2);
		System.out.printf("getArray(image2) time: [%d]ms"+System.lineSeparator(), System.currentTimeMillis()-t1);
		t1=System.currentTimeMillis();
		if(image1==null)
		{
			rgb1=new int[rgb2.length][rgb2[0].length][3];
		}
		else
		{
			System.out.println("Getting array for first image...");
			rgb1=getArray(image1);
		}
		System.out.printf("Dual Image null generation time: [%d] ms\n", System.currentTimeMillis()-t1);
		return getDualImage(rgb1, rgb2);
	}
	public static int[][][] getSingleImage(BufferedImage image)
	{
		final byte[] pixels = ((DataBufferByte) image.getRaster()
				.getDataBuffer()).getData();
		final int width = image.getWidth();
		final int height = image.getHeight();
		final boolean hasAlphaChannel = image.getAlphaRaster() != null;
		int[] red = new int[height*width];
		int[] green = new int[height*width];
		int[] blue= new int[height*width];
		int pixelLength=3;
		if (hasAlphaChannel)
		{
			pixelLength=3;
		}
		pixelLength = 4;
		int gTotal=0;
		int gCount=0;
		int max=Integer.MIN_VALUE;
		int min=Integer.MAX_VALUE;
		int r, g, b;
		int maxl, minl;
		for (int pixel = 0, i = 0; pixel < pixels.length; pixel += pixelLength)
		{
			r = (int) pixels[pixel + 3] & 0xff;// red
			g = (int) pixels[pixel + 2] & 0xff;// green
			b = (int) pixels[pixel + 1] & 0xff;// blue
			if(r > g)
			{
				maxl=r;
				minl=g;
			}
			else
			{
				maxl=g;
				minl=r;
			}
			if(b > maxl)
			{
				maxl=b;
			}
			else if(b <minl)
			{
				minl=b;
			}
			if(g>100)
			{
				gTotal=gTotal+g;
				gCount++;
			}
			max = Math.max(maxl, max);
			min = Math.min(maxl, min);
			
			red[i]=r;
			green[i]=g;
			blue[i]=b;
			
			i++;
		}
		int gMax;
		if(gCount==0)
		{
			gMax=140;
		}
		else
		{
			gMax=(int) (gTotal/gCount);
		}
		if(max-min==0)
		{
			max++;
		}
		float factor = 255.0f/(max-min);
		int beta=Math.min(140-gMax, 0);
		System.out.printf("Beta generated to be [%d]\ngMax found to be [%d]\n", beta, gMax);
		float alpha=1.0f;
		if(gCount<MIN_SIZE_RATIO*2*pixels.length)
		{
			alpha=1.5f;
		}
		int[][][] rgb=new int[height][width][3];
		for(int i=0, row = 0, col = 0;i<red.length;i++)
		{
			rgb[row][col][0]=limit((int)(((red[i]-min)*factor)*alpha)+beta);
			rgb[row][col][1]=limit((int)(((green[i]-min)*factor)*alpha)+beta);
			rgb[row][col][2]=limit((int)(((blue[i]-min)*factor)*alpha)+beta);
			
			col++;
			if (col == width)
			{
				col = 0;
				row++;
			}
		}
		return rgb;
	}
	public static int[][][] getSingleImage(int[][][] rgb)
	{
		float alpha=1.0f;
		int beta=0;
		int gMax=0;
		long gTotal=0;
		int gCount=0;
		int min=Integer.MAX_VALUE;
		int max=Integer.MIN_VALUE;
		for(int i=0;i<rgb.length;i++)
		{
			for(int j=0;j<rgb[0].length;j++)
			{
				for(int k=0;k<3;k++)
				{
					int d=rgb[i][j][k];
					min = Math.min(d, min);
					max = Math.max(d, max);
					if(k==1)
					{
						if(d>100)
						{
							gTotal=gTotal+d;
							gCount++;
						}
					}
				}
			}
		}
		if(gCount==0)
		{
			gMax=140;
		}
		else
		{
			gMax=(int) (gTotal/gCount);
		}
		if(max-min==0)
		{
			max++;
		}
		float factor = 255.0f/(max-min);
		beta=Math.min(140-gMax, 0);
		if(gCount<MIN_SIZE_RATIO*2*rgb.length*rgb[0].length)
		{
			alpha=1.5f;
		}
		System.out.printf("Beta generated to be [%d]\ngMax found to be [%d]\n", beta, gMax);
		for(int i=0;i<rgb.length;i++)
		{
			for(int j=0;j<rgb[0].length;j++)
			{
				for(int k=0;k<3;k++)
				{
					rgb[i][j][k]=limit((int)(((rgb[i][j][k]-min)*factor)*alpha)+beta);
					//rgbD[i][j][k]=limit((int)((rgbD[i][j][k]-min)*factor));
				}
			}
		}
		return rgb;
	}
	public static int max(int a, int b, int c)
	{
		return Math.max(Math.max(a, b), c);
	}
	public static int min(int a, int b, int c)
	{
		return Math.min(Math.min(a, b), c);
	}
	public static int[][][] getDualImage(int[][][] rgb1, int[][][] rgb2)
	{
		float alpha=1.0f;
		int beta=0;
		int gMax=0;
		long gTotal=0;
		int gCount=0;
		if(rgb1==null)
		{
			rgb1=new int[rgb2.length][rgb2[0].length][3];
		}
		int[][][] rgbD=new int[rgb1.length][rgb1[0].length][rgb1[0][0].length];
		int min=Integer.MAX_VALUE;
		int max=Integer.MIN_VALUE;
		for(int i=0;i<rgb1.length;i++)
		{
			for(int j=0;j<rgb1[0].length;j++)
			{
				for(int k=0;k<3;k++)
				{
					int d=rgb2[i][j][k]-rgb1[i][j][k];
					min = Math.min(d, min);
					max = Math.max(d, max);
					rgbD[i][j][k]=d;
					if(k==1)
					{
						if(d>100)
						{
							gTotal=gTotal+d;
							gCount++;
						}
					}
				}
			}
		}
		gMax=(int) (gTotal/gCount);
		float factor = 255.0f/(max-min);
		beta=Math.min(140-gMax, 0);
		if(gCount<300)
		{
			alpha=1.5f;
		}
		System.out.printf("Beta generated to be [%d]\ngMax found to be [%d]\n", beta, gMax);
		for(int i=0;i<rgb1.length;i++)
		{
			for(int j=0;j<rgb1[0].length;j++)
			{
				for(int k=0;k<3;k++)
				{
					rgbD[i][j][k]=limit((int)(((rgbD[i][j][k]-min)*factor)*alpha)+beta);
					//rgbD[i][j][k]=limit((int)((rgbD[i][j][k]-min)*factor));
				}
			}
		}
		return rgbD;
	}
	public static int limit(int number)
	{
		return limit(number, 0, 255);
	}
	public static int limit(int number, int min, int max)
	{
		return Math.min(Math.max(min, number), max);
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
				int[] hsv = getHSV(red, green, blue);
				int allowance=(int) ((-7.0/10.0)*hsv[1]+100);
				//allowance=Math.max(allowance, (hsv[2]-50)*10);
				allowance=Math.max(allowance, 15);
				//While loop stupid way to exit checker when done
				while(true)
				{
					if(Math.abs(hsv[0]-165)>allowance)
					{
						valid=false;
						break;
					}
					if(hsv[2]<30)
					{
						valid=false;
						break;
					}
					break;
				}
				map[i][j] = valid;
			}
		}
		return map;
	}
	public static int[] getHSV(int red, int green, int blue)
	{
		//Calculations based of this website http://www.rapidtables.com/convert/color/rgb-to-hsv.htm
		int[] hsv=new int[3];
		RGB maxType=RGB.RED;
		double r=(red*1.0)/255.0;
		double g=(green*1.0)/255.0;
		double b=(blue*1.0)/255.0;
		double max=r;
		if(g>max)
		{
			max=g;
			maxType=RGB.GREEN;
		}
		if(b>max)
		{
			max=b;
			maxType=RGB.BLUE;
		}
		double min=Math.min(Math.min(r,g), b);
		double delta=max-min;
		if(delta==0)
		{
			hsv[0]=0;
		}
		else
		{
			switch(maxType)
			{
				case RED:
					hsv[0]=(int) (60.0*(((g-b)/delta)%6));
					break;
				case GREEN:
					hsv[0]=(int) (60.0*(((b-r)/delta)+2));
					break;
				case BLUE:
					hsv[0]=(int) (60.0*(((r-g)/delta)+4));
					break;
				default:
					assert false;//OOH! Fancy Keywords! But realistically, if it gets here, the program is messed up. A lot.
			}
		}
		if(max==0)
		{
			hsv[1]=0;
		}
		else
		{
			hsv[1]=(int) Math.round(100.0*delta/max);
		}
		hsv[2]=(int) Math.round(100.0*max);
		return hsv;
	}
}
