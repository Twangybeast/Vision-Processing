package edgedetector;

import java.util.ArrayList;

import code2017.Particle;
import code2017.Point;
import code2017.Vision17;

public class EdgeFiller
{
	public static Particle fillEdgeTest2(Particle edge)
	{
		edge.shorten();
		Particle particle = new Particle(edge);
		particle.corners=edge.corners;
		particle.setTWidth(edge.getTWidth());
		particle.setTHeight(edge.getTHeight());
		particle.setTarget(edge.getTarget());
		ArrayList<Point> ends=new ArrayList<Point>();
		for(int x=0;x<edge.getWidth();x++)
		{
			for(int y=0;y<edge.getHeight();y++)
			{
				if(edge.getLocalValue(x, y))
				{
					//Evaluate neighbors. Eight connection
					int count=0;
					boolean[][] local=EdgeAlgorithm.getLocalArray(edge, x, y);
					for(int i=0;i<local.length;i++)
					{
						for(int j=0;j<local[0].length;j++)
						{
							if(local[i][j])
							{
								count++;
							}
						}
					}
					if(count<=2)
					{
						ends.add(new Point(x, y));
					}
				}
			}
		}
		if(ends.size()>1 && ends.size() < 8)//Too lazy to do more than that
		{
			//Pair the points
			ArrayList<Point> pairs=new ArrayList<Point>();
			for(int i=0;i<ends.size();i++)
			{
				double lowest=Integer.MAX_VALUE;
				Point closest= null;
				for(int j=0;j<ends.size();j++)
				{
					if(j!=i)
					{
						double distance = Vision17.distance(ends.get(i), ends.get(j));
						if(distance < lowest)
						{
							lowest=distance;
							closest=ends.get(j);
						}
					}
				}
				if(closest!=null)//idk why it would
				{
					pairs.add(closest);
				}
				else
				{
					pairs.add(ends.get(i));
				}
			}
			for(int i=0;i<pairs.size();i++)
			{
				if(pairs.get(i)!=null)
				{
					Point pair=pairs.get(i);
					int pairIndex=ends.indexOf(pair);
					if(pairIndex!=-1)
					{
						if(pairs.get(pairIndex).equals(ends.get(i)))
						{
							int q1=edge.getQuadrantLocal(ends.get(i).x, ends.get(i).y);
							int q2=edge.getQuadrantLocal(pair.x, pair.y);
							//Global points
							Point p1=new Point(ends.get(i).x, ends.get(i).y);
							p1.translate(edge.x, edge.y);
							Point p2=new Point(pair.x, pair.y);
							p2.translate(edge.x, edge.y);
							if(q1==q2)
							{
								//Assume close proximity, draw line to connect
								drawLine(edge, p1, p2);
							}
							else
							{
								int quadrant1, quadrant2;
								if(q1<q2)
								{
									quadrant1=q1;
									quadrant2=q2;
								}
								else
								{
									quadrant1=q2;
									quadrant2=q1;
								}
								Point last;
								switch(quadrant1)//Note: if invalid combination ,does nothing. Intentional.
								{
									case 0:
										switch(quadrant2)
										{
											case 1:
												if(p1.y>p2.y)
												{
													connectLast(edge, getLastCorner(edge.corners, q1), q1);
												}
												else
												{
													connectLast(edge, getLastCorner(edge.corners, q2), q2);
												}
												break;
											case 2:
												if(p1.x>p2.x)
												{
													connectLast(edge, getLastCorner(edge.corners, q1), q1);
												}
												else
												{
													connectLast(edge, getLastCorner(edge.corners, q2), q2);
												}
												break;
											case 3:
												if(edge.getQuadrantGlobal(edge.corners[1].x, edge.corners[1].y)==1)
												{
													connectLast(edge, getLastCorner(edge.corners, 2), 2);
												}
												else
												{
													connectLast(edge, getLastCorner(edge.corners, 1), 1);
												}
												break;
										}
										break;
									case 1:
										switch(quadrant2)
										{
											case 2:
												if(edge.getQuadrantGlobal(edge.corners[0].x, edge.corners[0].y)==0)
												{
													connectLast(edge, getLastCorner(edge.corners, 3), 3);
												}
												else
												{
													connectLast(edge, getLastCorner(edge.corners, 0), 0);
												}
												break;
											case 3:
												if(p1.x>p2.x)
												{
													connectLast(edge, getLastCorner(edge.corners, q2), q2);
												}
												else
												{
													connectLast(edge, getLastCorner(edge.corners, q1), q1);
												}
												break;
										}
										break;
									case 2:
										if(quadrant2==3)//Supposed to
										{
											if(p1.y>p2.y)
											{
												connectLast(edge, getLastCorner(edge.corners, q2), q2);
											}
											else
											{
												connectLast(edge, getLastCorner(edge.corners, q1), q1);
											}
										}
										break;
								}
							}
						}
						pairs.set(i, null);
						ends.set(i, null);
					}
				}	
			}
		}
		return fillEdgeNormal(edge);
	}
	public static void connectLast(Particle particle, Point last, int quadrant)
	{
		particle.globalExpand(last.x, last.y);
		switch(quadrant)
		{
			case 0:
				drawLine(particle, last, particle.corners[1]);
				drawLine(particle, last, particle.corners[2]);
				break;
			case 1:
				drawLine(particle, last, particle.corners[0]);
				drawLine(particle, last, particle.corners[3]);
				break;
			case 2:
				drawLine(particle, last, particle.corners[0]);
				drawLine(particle, last, particle.corners[3]);
				break;
			case 3:
				drawLine(particle, last, particle.corners[1]);
				drawLine(particle, last, particle.corners[2]);
				break;
		}
	}
	public static Point getLastCorner(Point[] corners, int badCorner)
	{
		int dx;
		int dy;
		Point start;
		switch (badCorner)
		{
			case 0:
				dx=corners[2].x-corners[3].x;
				dy=corners[2].y-corners[3].y;
				start=new Point(corners[1].x, corners[1].y);
				break;
			case 1:
				dx=corners[3].x-corners[2].x;
				dy=corners[3].y-corners[2].y;
				start=new Point(corners[0].x, corners[0].y);
				break;
			case 2:
				dx=corners[0].x-corners[1].x;
				dy=corners[0].y-corners[1].y;
				start=new Point(corners[3].x, corners[3].y);
				break;
			case 3:
				dx=corners[1].x-corners[0].x;
				dy=corners[1].y-corners[0].y;
				start=new Point(corners[2].x, corners[2].y);
				break;
			default:
				return null;
		}
		start.translate(dx, dy);
		return start;
	}
	public static Particle fillEdgeTest(Particle edge)
	{
		edge.shorten();
		Particle particle = new Particle(edge);
		particle.corners=edge.corners;
		particle.setTWidth(edge.getTWidth());
		particle.setTHeight(edge.getTHeight());
		particle.setTarget(edge.getTarget());
		//N, S, W, E, Profile gathering
		int[][] profile=new int[4][];
		//N & S
		profile[0]=new int[edge.getWidth()];
		profile[1]=new int[edge.getWidth()];
		for(int x=0;x<edge.getWidth();x++)
		{
			for(int y=0;y<edge.getHeight();y++)
			{
				if(edge.getLocalValue(x, y))
				{
					profile[0][x] = y;
					break;
				}
			}
			for(int y=edge.getHeight()-1;y>=0;y--)
			{
				if(edge.getLocalValue(x, y))
				{
					profile[1][x] = edge.getHeight()-1-y;
					break;
				}
			}
		}
		//W & E
		profile[2]=new int[edge.getHeight()];
		profile[3]=new int[edge.getHeight()];
		for(int y=0;y<edge.getHeight();y++)
		{
			for(int x=0;x<edge.getWidth();x++)
			{
				if(edge.getLocalValue(x, y))
				{
					profile[2][y] = x;
					break;
				}
			}
			for(int x=edge.getWidth()-1;x>=0;x--)
			{
				if(edge.getLocalValue(x, y))
				{
					profile[3][y] = edge.getWidth()-1-x;
					break;
				}
			}
		}
		//N, S, W, E
		//N & S
		for(int x=0;x<edge.getWidth();x++)
		{
			int start=profile[0][x];
			int end=edge.getHeight()-profile[1][x]-1;
			//Evaluate if hits end
			if(start==end)
			{
				end=start;//Don't draw line
			}
			for(int y=start;y<end;y++)
			{
				particle.setLocalValue(x, y, true);
			}
		}
		//W & E
		for(int y=0;y<edge.getHeight();y++)
		{
			int start=profile[2][y];
			int end=edge.getWidth()-profile[3][y]-1;
			//Evaluate if hits end
			if(start==end)
			{
				end=start;//Don't draw line
			}
			for(int x=start;x<end;x++)
			{
				particle.setLocalValue(x, y, true);
			}
		}
		particle.recount();
		return particle;
	}
	public static Particle fillEdgeDeriv2(Particle edge)
	{
		fillEdgeTest(edge);//                                       DELETE THIS!@!!!!!
		edge.shorten();
		Particle particle = new Particle(edge);
		particle.corners=edge.corners;
		particle.setTWidth(edge.getTWidth());
		particle.setTHeight(edge.getTHeight());
		particle.setTarget(edge.getTarget());

		//Initial fill
		for(int x=0;x<particle.getWidth();x++)
		{
			for(int y=0;y<particle.getHeight();y++)
			{
				particle.setLocalValue(x, y, true);
			}
		}
		
		//N, S, W, E
		int[][] profile=new int[4][];
		//N & S
		profile[0]=new int[edge.getWidth()];
		profile[1]=new int[edge.getWidth()];
		for(int x=0;x<edge.getWidth();x++)
		{
			for(int y=0;y<edge.getHeight();y++)
			{
				if(edge.getLocalValue(x, y))
				{
					profile[0][x] = y;
					break;
				}
			}
			for(int y=edge.getHeight()-1;y>=0;y--)
			{
				if(edge.getLocalValue(x, y))
				{
					profile[1][x] = edge.getHeight()-1-y;
					break;
				}
			}
		}
		//W & E
		profile[2]=new int[edge.getHeight()];
		profile[3]=new int[edge.getHeight()];
		for(int y=0;y<edge.getHeight();y++)
		{
			for(int x=0;x<edge.getWidth();x++)
			{
				if(edge.getLocalValue(x, y))
				{
					profile[2][y] = x;
					break;
				}
			}
			for(int x=edge.getWidth()-1;x>=0;x--)
			{
				if(edge.getLocalValue(x, y))
				{
					profile[3][y] = edge.getWidth()-1-x;
					break;
				}
			}
		}
		int[][] profileDeriv=new int[4][];
		for(int i=0;i<profile.length;i++)//Should be 4 times
		{
			profileDeriv[i]=deriv(profile[i]);
		}
		//Evaluation
		//Track the sides
		boolean[] broken=new boolean[4];
		int threshold=2;
		int side;
		int iStart=0;
		int iEnd=0;
		int[] index;
		//N
		side=0;
		index=findIndex(profileDeriv[side], iStart, iEnd, threshold);
		iStart=index[0];
		iEnd=index[1];
		broken[side]=evaluateGap(profile[side], iStart, iEnd, edge.getHeight()/2);
		//S
		side=1;
		index=findIndex(profileDeriv[side], iStart, iEnd, threshold);
		iStart=index[0];
		iEnd=index[1];
		broken[side]=evaluateGap(profile[side], iStart, iEnd, edge.getHeight()/2);
		//W
		side=2;
		index=findIndex(profileDeriv[side], iStart, iEnd, threshold);
		iStart=index[0];
		iEnd=index[1];
		broken[side]=evaluateGap(profile[side], iStart, iEnd, edge.getWidth()/2);
		//E
		side=3;
		index=findIndex(profileDeriv[side], iStart, iEnd, threshold);
		iStart=index[0];
		iEnd=index[1];
		broken[side]=evaluateGap(profile[side], iStart, iEnd, edge.getWidth()/2);
		if(broken[0] || broken[1] || broken[2] || broken[3])
		{
			fixParticle(edge, broken);
			return fillEdgeNormal(edge);
		}
		else
		{
			//Now mold the particle
			//N
			for(int x=0;x<particle.getWidth();x++)
			{
				for(int y=0;y<profile[0][x];y++)
				{
					particle.setLocalValue(x, y, false);
				}
			}
			//S
			for(int x=0;x<particle.getWidth();x++)
			{
				for(int y=0;y<profile[1][x];y++)
				{
					particle.setLocalValue(x, particle.getHeight()-y-1, false);
				}
			}
			//W
			for(int y=0;y<particle.getHeight();y++)
			{
				for(int x=0;x<profile[2][y];x++)
				{
					particle.setLocalValue(x, y, false);
				}
			}
			//E
			for(int y=0;y<particle.getHeight();y++)
			{
				for(int x=0;x<profile[3][y];x++)
				{
					particle.setLocalValue(particle.getWidth()-x-1, y, false);
				}
			}
			return particle;
		}
	} 

	public static Particle fillEdgeNormal(Particle edge)
	{
		Particle particle = new Particle(edge);
		particle.corners=edge.corners;
		particle.setTWidth(edge.getTWidth());
		particle.setTHeight(edge.getTHeight());
		particle.setTarget(edge.getTarget());
		//Initial fill
		for(int x=0;x<particle.getWidth();x++)
		{
			for(int y=0;y<particle.getHeight();y++)
			{
				particle.setLocalValue(x, y, true);
			}
		}
		//Approach from N, S, W, E directions in straight line patterns. Match the outer "mold" of the edge, but solidifiy it
		//N
		for(int x=0;x<edge.getWidth();x++)
		{
			for(int y=0;y<edge.getHeight();y++)
			{
				if(edge.getLocalValue(x, y))
				{
					break;
				}
				else
				{
					particle.setLocalValue(x, y, false);
				}
			}
		}
		//S
		for(int x=0;x<edge.getWidth();x++)
		{
			for(int y=edge.getHeight()-1;y>=0;y--)
			{
				if(edge.getLocalValue(x, y))
				{
					break;
				}
				else
				{
					particle.setLocalValue(x, y, false);
				}
			}
		}
		//W
		for(int y=0;y<edge.getHeight();y++)
		{
			for(int x=0;x<edge.getWidth();x++)
			{
				if(edge.getLocalValue(x, y))
				{
					break;
				}
				else
				{
					particle.setLocalValue(x, y, false);
				}
			}
		}
		//E
		for(int y=0;y<edge.getHeight();y++)
		{
			for(int x=edge.getWidth()-1;x>=0;x--)
			{
				if(edge.getLocalValue(x, y))
				{
					break;
				}
				else
				{
					particle.setLocalValue(x, y, false);
				}
			}
		}
		particle.recount();
		return particle;
	}
	public static boolean evaluateGap(int[] profile, int start, int end, int halfFull)
	{
		for(int i=start;i<=end;i++)
		{
			if(profile[i]>halfFull)
			{
				return true;
			}
		}
		return false;
	}
	public static int[] findIndex(int[] deriv, int start, int end, int threshold)
	{
		for(int i=0;i<deriv.length;i++)
		{
			if(deriv[i]>-threshold)
			{
				start=i;
				break;
			}
		}
		for(int i=deriv.length-1;i>=0;i--)
		{
			if(deriv[i]<threshold)
			{
				end=i;
				break;
			}
		}
		int[] index=new int[2];
		index[0]=start;
		index[1]=end;
		return index;
	}
	public static Particle fillEdgeDeriv(Particle edge)
	{
		edge.shorten();
		Particle particle = new Particle(edge);
		particle.corners=edge.corners;
		particle.setTWidth(edge.getTWidth());
		particle.setTHeight(edge.getTHeight());
		particle.setTarget(edge.getTarget());

		//Initial fill
		for(int x=0;x<particle.getWidth();x++)
		{
			for(int y=0;y<particle.getHeight();y++)
			{
				particle.setLocalValue(x, y, true);
			}
		}
		
		//N, S, W, E
		int[][] profile=new int[4][];
		//N & S
		profile[0]=new int[edge.getWidth()];
		profile[1]=new int[edge.getWidth()];
		for(int x=0;x<edge.getWidth();x++)
		{
			for(int y=0;y<edge.getHeight();y++)
			{
				if(edge.getLocalValue(x, y))
				{
					profile[0][x] = y;
					break;
				}
			}
			for(int y=edge.getHeight()-1;y>=0;y--)
			{
				if(edge.getLocalValue(x, y))
				{
					profile[1][x] = edge.getHeight()-1-y;
					break;
				}
			}
		}
		//W & E
		profile[2]=new int[edge.getHeight()];
		profile[3]=new int[edge.getHeight()];
		for(int y=0;y<edge.getHeight();y++)
		{
			for(int x=0;x<edge.getWidth();x++)
			{
				if(edge.getLocalValue(x, y))
				{
					profile[2][y] = x;
					break;
				}
			}
			for(int x=edge.getWidth()-1;x>=0;x--)
			{
				if(edge.getLocalValue(x, y))
				{
					profile[3][y] = edge.getWidth()-1-x;
					break;
				}
			}
		}
		int[][] profileDeriv=new int[4][];
		for(int i=0;i<profile.length;i++)//Should be 4 times
		{
			profileDeriv[i]=deriv(profile[i]);
		}
		
		//Evaluation
		int thresholdUp;
		float thresholdFactorUp=0.4f;
		int thresholdDown;
		float thresholdFactorDown=0.25f;
		boolean wentUp;
		int index;
		thresholdUp=(int) (edge.getHeight()*thresholdFactorUp);
		thresholdDown=(int) (edge.getHeight()*thresholdFactorDown);
		boolean[] change=new boolean[profileDeriv.length];
		//N
		wentUp=false;
		index=0;
		for(int i=0;i<profileDeriv[0].length;i++)
		{
			if(profileDeriv[0][i]>thresholdUp)
			{
				wentUp=true;
				index=i;
			}
			if(-profileDeriv[0][i]>thresholdDown)
			{
				if(wentUp)
				{
					if(index==0)
					{
						profileDeriv[0][index]=0;
						index=1;//To be lowered to 0 later
					}
					if(i==profileDeriv[0].length-1)
					{
						profileDeriv[0][i]=0;
						i=profileDeriv[0].length-2;
					}
					drawLine(profileDeriv[0], index-1, i+1);
					wentUp=false;
					change[0]=true;
				}
			}
		}
		//S
		wentUp=false;
		index=0;
		for(int i=0;i<profileDeriv[1].length;i++)
		{
			if(profileDeriv[1][i]>thresholdUp)
			{
				wentUp=true;
				index=i;
			}
			if(-profileDeriv[1][i]>thresholdDown)
			{
				if(wentUp)
				{
					if(index==0)
					{
						profileDeriv[1][index]=0;
						index=1;//To be lowered to 0 later
					}
					if(i==profileDeriv[1].length-1)
					{
						profileDeriv[1][i]=0;
						i=profileDeriv[1].length-2;
					}
					drawLine(profileDeriv[1], index-1, i+1);
					wentUp=false;
					change[1]=true;
				}
			}
		}
		thresholdUp=(int) (edge.getWidth()*thresholdFactorUp);
		thresholdDown=(int) (edge.getWidth()*thresholdFactorDown);
		//W
		wentUp=false;
		index=0;
		for(int i=0;i<profileDeriv[2].length;i++)
		{
			if(profileDeriv[2][i]>thresholdUp)
			{
				wentUp=true;
				index=i;
			}
			if(-profileDeriv[2][i]>thresholdDown)
			{
				if(wentUp)
				{
					if(index==0)
					{
						profileDeriv[2][index]=0;
						index=1;//To be lowered to 0 later
					}
					if(i==profileDeriv[2].length-1)
					{
						profileDeriv[2][i]=0;
						i=profileDeriv[2].length-2;
					}
					drawLine(profileDeriv[2], index-1, i+1);
					wentUp=false;
					change[2]=true;
				}
			}
		}
		//E
		wentUp=false;
		index=0;
		for(int i=0;i<profileDeriv[3].length;i++)
		{
			if(profileDeriv[3][i]>thresholdUp)
			{
				wentUp=true;
				index=i;
			}
			if(-profileDeriv[3][i]>thresholdDown)
			{
				if(wentUp)
				{
					if(index==0)
					{
						profileDeriv[3][index]=0;
						index=1;//To be lowered to 0 later
					}
					if(i==profileDeriv[3].length-1)
					{
						profileDeriv[3][i]=0;
						i=profileDeriv[3].length-2;
					}
					drawLine(profileDeriv[3], index-1, i+1);
					wentUp=false;
					change[3]=true;
				}
			}
		}
		profile[0]=integral(profileDeriv[0], profile[0][0], edge.getHeight()-1);
		profile[1]=integral(profileDeriv[1], profile[1][0], edge.getHeight()-1);
		profile[2]=integral(profileDeriv[2], profile[2][0], edge.getWidth()-1);
		profile[3]=integral(profileDeriv[3], profile[3][0], edge.getWidth()-1);
		//Now mold the particle
		//N
		for(int x=0;x<particle.getWidth();x++)
		{
			for(int y=0;y<profile[0][x];y++)
			{
				particle.setLocalValue(x, y, false);
			}
		}
		//S
		for(int x=0;x<particle.getWidth();x++)
		{
			for(int y=0;y<profile[1][x];y++)
			{
				particle.setLocalValue(x, particle.getHeight()-y-1, false);
			}
		}
		//W
		for(int y=0;y<particle.getHeight();y++)
		{
			for(int x=0;x<profile[2][y];x++)
			{
				particle.setLocalValue(x, y, false);
			}
		}
		//E
		for(int y=0;y<particle.getHeight();y++)
		{
			for(int x=0;x<profile[3][y];x++)
			{
				particle.setLocalValue(particle.getWidth()-x-1, y, false);
			}
		}
		return particle;
	}
	public static void drawLine(int[] profile, int iStart, int iEnd)
	{
		int x1 = iStart;
		int y1 = profile[iStart];
		int x2 = iEnd;
		int y2 = profile[iEnd];
		int dx= x2-x1;
		int dy= y2-y1;
		for(int x=x1+1;x<x2;x++)
		{
			int y = y1 + dy * (x - x1) / dx;
			profile[x]=y;
		}
	}
	public static int[] integral(int[] deriv, int start, int max)
	{
		int[] profile=new int[deriv.length+1];
		profile[0]=start;
		for(int i=0;i<deriv.length;i++)
		{
			profile[i+1]=Math.min(profile[i]+deriv[i], max);
		}
		return profile;
	}
	public static int[] deriv(int[] profile)
	{
		int[] deriv=new int[profile.length-1];
		for(int i=0;i<profile.length-1;i++)
		{
			deriv[i]=profile[i+1]-profile[i];
		}
		return deriv;
	}

	public static Particle fillEdge(Particle edge)//Replace with derivatives
	{
		edge.shorten();
		Particle particle = new Particle(edge);
		particle.corners=edge.corners;
		particle.setTWidth(edge.getTWidth());
		particle.setTHeight(edge.getTHeight());
		particle.setTarget(edge.getTarget());
		//Initial fill
		for(int x=0;x<particle.getWidth();x++)
		{
			for(int y=0;y<particle.getHeight();y++)
			{
				particle.setLocalValue(x, y, true);
			}
		}
		//Approach from N, S, W, E directions in straight line patterns. Match the outer "mold" of the edge, but solidifiy it
		int lastGoodIndex=0;
		int lastGoodValue1=0;
		int lastGoodValue2=0;
		boolean broken=false;
		boolean direction=false;//First is true, second is false
		//N and S
		for(int x=0;x<edge.getWidth();x++)
		{
			int NIndex=0;
			int SIndex=0;
			for(int y=0;y<edge.getHeight();y++)
			{
				if(edge.getLocalValue(x, y))
				{
					NIndex=y;
					break;
				}
				else
				{
					particle.setLocalValue(x, y, false);
				}
			}
			for(int y=edge.getHeight()-1;y>=0;y--)
			{
				if(edge.getLocalValue(x, y))
				{
					SIndex=y;
					break;
				}
				else
				{
					particle.setLocalValue(x, y, false);
				}
			}
			if(NIndex == SIndex && x > 0 && x < edge.getWidth()-1)
			{
				broken=true;
				direction = NIndex > edge.getHeight()/2;
			}
			else
			{
				if(broken)
				{
					if(direction)
					{
						Point p1=new Point(lastGoodIndex, lastGoodValue1);
						Point p2=new Point(x, NIndex);
						int dx= p2.x-p1.x;
						int dy= p2.y-p1.y;
						for(int x1=p1.x+1;x1<p2.x;x1++)
						{
							int y = p1.y + dy * (x1 - p1.x) / dx;
							for(int y1=y;y1<edge.getHeight();y1++)
							{
								if(edge.getLocalValue(x1, y1))
								{
									break;
								}
								else
								{
									particle.setLocalValue(x1, y1, true);
								}
							}
						}
					}
					else
					{
						Point p1=new Point(lastGoodIndex, lastGoodValue2);
						Point p2=new Point(x, SIndex);
						int dx= p2.x-p1.x;
						int dy= p2.y-p1.y;
						for(int x1=p1.x+1;x1<p2.x;x1++)
						{
							int y = p1.y + dy * (x1 - p1.x) / dx;
							for(int y1=y;y1>=0;y1--)
							{
								if(edge.getLocalValue(x1, y1))
								{
									break;
								}
								else
								{
									particle.setLocalValue(x1, y1, true);
								}
							}
						}
					}
					broken=false;
				}
				lastGoodIndex=x;
				lastGoodValue1=NIndex;
				lastGoodValue2=SIndex;
			}
		}
		return particle;
	}
	public static Particle fillEdge(Particle edge, boolean lineEnabled)
	{
		Particle particle = new Particle(edge);
		particle.corners=edge.corners;
		particle.setTWidth(edge.getTWidth());
		particle.setTHeight(edge.getTHeight());
		particle.setTarget(edge.getTarget());
		//Initial fill
		for(int x=0;x<particle.getWidth();x++)
		{
			for(int y=0;y<particle.getHeight();y++)
			{
				particle.setLocalValue(x, y, true);
			}
		}
		//Approach from N, S, W, E directions in straight line patterns. Match the outer "mold" of the edge, but solidifiy it
		boolean[] reachEnd=new boolean[4];
		boolean needsFix=false;
		int count;
		final double failPercent=0.3;
		final double reachPercent=0.6;
		//N
		count=0;
		for(int x=0;x<edge.getWidth();x++)
		{
			for(int y=0;y<edge.getHeight();y++)
			{
				if(edge.getLocalValue(x, y))
				{
					if(y>edge.getHeight()*reachPercent )
					{
						count++;
					}
					break;
				}
				else
				{
					particle.setLocalValue(x, y, false);
				}
			}
		}
		//if(count*1.0/edge.getWidth()>failPercent)
		if(count > 2)
		{
			needsFix=true;
			reachEnd[0]=true;
		}
		else
		{
			reachEnd[0]=false;
		}
		//S
		count=0;
		for(int x=0;x<edge.getWidth();x++)
		{
			for(int y=edge.getHeight()-1;y>=0;y--)
			{
				if(edge.getLocalValue(x, y))
				{
					if(y<edge.getHeight()*(1.0-reachPercent))
					{
						count++;
					}
					break;
				}
				else
				{
					particle.setLocalValue(x, y, false);
				}
			}
		}
		//if(count*1.0/edge.getWidth()>failPercent)
		if(count > 2)
		{
			needsFix=true;
			reachEnd[1]=true;
		}
		else
		{
			reachEnd[1]=false;
		}
		//W
		count=0;
		for(int y=0;y<edge.getHeight();y++)
		{
			for(int x=0;x<edge.getWidth();x++)
			{
				if(edge.getLocalValue(x, y))
				{
					if(x>edge.getWidth()*reachPercent)
					{
						count++;
					}
					break;
				}
				else
				{
					particle.setLocalValue(x, y, false);
				}
			}
		}
		//if(count*1.0/edge.getHeight()>failPercent)
		if(count > 2)
		{
			needsFix=true;
			reachEnd[2]=true;
		}
		else
		{
			reachEnd[2]=false;
		}
		//E
		count=0;
		for(int y=0;y<edge.getHeight();y++)
		{
			for(int x=edge.getWidth()-1;x>=0;x--)
			{
				if(edge.getLocalValue(x, y))
				{
					if(x<edge.getWidth()*(1.0-reachPercent))
					{
						count++;
					}
					break;
				}
				else
				{
					particle.setLocalValue(x, y, false);
				}
			}
		}
		//if(count*1.0/edge.getHeight()>failPercent)
		if(count >2)
		{
			needsFix=true;
			reachEnd[3]=true;
		}
		else
		{
			reachEnd[3]=false;
		}
		if(particle.count*1.0/(edge.getTWidth()*edge.getTHeight())<0.5 || needsFix)
		{
			if(lineEnabled)
			{
				particle=fixParticle(edge, reachEnd);
				if(particle.x<0 || particle.y<0)
				{
					return null;
				}
			}
		}
		particle.recount();
		return particle;
	}
	public static Particle fixParticle(Particle edge, boolean[] reachEnd)
	{
		int failCount=0;
		for(int i=0;i<reachEnd.length;i++)
		{
			if(reachEnd[i])
			{
				failCount++;
			}
		}
		switch(failCount)
		{
			case 1:
				int side=0;
				for(;side<reachEnd.length;side++)
				{
					if(reachEnd[side])
					{
						break;
					}
				}
				Point p1=null;
				Point p2=null;
				switch(side)
				{
					case 0:
						p1=edge.corners[0];
						p2=edge.corners[1];
						break;
					case 1:
						p1=edge.corners[2];
						p2=edge.corners[3];
						break;
					case 2:
						p1=edge.corners[0];
						p2=edge.corners[2];
						break;
					case 3:
						p1=edge.corners[1];
						p2=edge.corners[3];
						break;
					default:
						System.out.printf("Uh....\n");
						break;
				}
				edge=drawLine(edge, p1, p2);
				break;
			case 2:
				int corner=0;
				if(reachEnd[1])
				{
					//Corner can now be 2 or 3
					if(reachEnd[2])
					{
						corner=2;
					}
					else
					{
						corner=3;
					}
				}
				else
				{
					if(reachEnd[2])
					{
						corner=0;//Redundant
					}
					else
					{
						corner=1;
					}
				}
				Point c=null;
				Point c1=null;
				Point c2=null;
				switch(corner)
				{
					case 0:
						c=edge.corners[3];
						c1=edge.corners[1];
						c2=edge.corners[2];
						break;
					case 1:
						c=edge.corners[2];
						c1=edge.corners[0];
						c2=edge.corners[3];
						break;
					case 2:
						c=edge.corners[1];
						c1=edge.corners[0];
						c2=edge.corners[3];
						break;
					case 3:
						c=edge.corners[0];
						c1=edge.corners[1];
						c2=edge.corners[2];
						break;
				}
				c=findLastVertex(c, c1, c2);
				edge.globalExpand(c.x, c.y);
				edge=drawLine(edge, c, c1);
				edge=drawLine(edge, c, c2);
				edge.shorten();
				break;
			default:
			case 4:
			case 3:
		}
		return edge;
	}
	public static Point findLastVertex(Point c, Point c1, Point c2)
	{
		int dx=c1.x-c.x;
		int dy=c1.y-c.y;
		Point c3=new Point(c2.x, c2.y);
		c3.translate(dx, dy);
		return c3;
	}
	public static Point reflectPoint(Point c, Point c1, Point c2)
	{
		//First find the line generated by c2 and c1. (y = mx +b)
		double m=(c2.y-c1.y)*1.0 / (c2.x-c1.x);
		double b=(c1.y-(m*c1.x));
		double d=((c.getX() + (c.getY() - b)*m)/(1+ (m*m)));
		double x1=(2*d)-c.getX();
		double y1=(2*d*m)-c.getY() + (2*b);
		return new Point((int)(x1), (int)(y1));
	}
	public static Particle drawLine(Particle particle, Point p1, Point p2)
	{
		int dy=p2.y-p1.y;
		int dx=p2.x-p1.x;
		if(dx==0 && dy==0)
		{
			return particle;
		}
		if(dx==0)
		{
			return drawVerticalLine(particle, p1, p2);
		}
		if(dy==0)
		{
			return drawHorizontalLine(particle, p1, p2);
		}
		if(Math.abs((dy*1.0)/dx)>1.0)
		{
			return drawVerticalLine(particle, p1, p2);
		}
		else
		{
			return drawHorizontalLine(particle, p1, p2);
		}
	}
	public static Particle drawHorizontalLine(Particle particle, Point p1, Point p2)
	{
		int x1;
		int y1;
		int x2;
		int y2;
		if(p1.x<p2.x)
		{
			x1=p1.x;
			y1=p1.y;
			x2=p2.x;
			y2=p2.y;
		}
		else
		{
			x1=p2.x;
			y1=p2.y;
			x2=p1.x;
			y2=p1.y;
		}
		int dx= x2-x1;
		int dy= y2-y1;
		for(int x=x1;x<=x2;x++)
		{
			int y = y1 + dy * (x - x1) / dx;
			if(particle.globalInMap(x, y))
			{
				particle.setGlobalValue(x, y, true);
			}
		}
		return particle;
	}
	public static Particle drawVerticalLine(Particle particle, Point p1, Point p2)
	{
		int y1;
		int x1;
		int y2;
		int x2;
		if(p1.y<p2.y)
		{
			y1=p1.y;
			x1=p1.x;
			y2=p2.y;
			x2=p2.x;
		}
		else
		{
			y1=p2.y;
			x1=p2.x;
			y2=p1.y;
			x2=p1.x;
		}
		int dy= y2-y1;
		int dx= x2-x1;
		for(int y=y1;y<=y2;y++)
		{
			int x= x1 + dx * (y - y1)/dy;
			if(particle.globalInMap(x, y))
			{
				particle.setGlobalValue(x, y, true);
			}
		}
		return particle;
	}
	public static Particle getParticleFromLine(Particle edge)
	{
		int dx = edge.x + (edge.getWidth()/2);
		int dy = edge.y + (edge.getHeight()/2);
		Particle particle = new Particle(edge);
		particle.corners=edge.corners;
		for(int x=0;x<edge.getWidth();x++)
		{
			for(int y=0;y<edge.getHeight();y++)
			{
				particle.setLocalValue(x, y, edge.getLocalValue(x, y));
			}
		}
		
		for(int x=edge.x;x<edge.x+edge.getWidth();x++)
		{
			for(int y=edge.y;y<edge.y+edge.getHeight();y++)
			{
				if(edge.getGlobalValue(x, y))
				{
					Point p=new Point(x, y);
					p.translate(-dx, -dy);
					p = new Point(-p.x, -p.y);
					p.translate(dx, dy);
					particle.globalExpand(p.x, p.y);
					particle.setGlobalValue(p.x, p.y, true);
				}
			}
		}
		particle=fillEdge(particle, false);
		return particle;
	}
	public static ArrayList<Particle> fillEdge(ArrayList<Particle> edge)
	{
		ArrayList<Particle> particles=new ArrayList<Particle>();
		for(int i=0;i<edge.size();i++)
		{
			particles.add(fillEdge(edge.get(i), true));
		}
		while(particles.remove(null))
		{
			//Removes all null particles. No code inside loop intentional.
		}
		return particles;
	}
	public static ArrayList<Particle> fillEdgeDeriv(ArrayList<Particle> edge)
	{
		ArrayList<Particle> particles=new ArrayList<Particle>();
		for(int i=0;i<edge.size();i++)
		{
			particles.add(fillEdgeDeriv2(edge.get(i)));
		}
		while(particles.remove(null))
		{
			//Removes all null particles. No code inside loop intentional.
		}
		return particles;
	}
	public static ArrayList<Particle> fillEdgeTest(ArrayList<Particle> edge)
	{
		ArrayList<Particle> particles=new ArrayList<Particle>();
		for(int i=0;i<edge.size();i++)
		{
			particles.add(fillEdgeTest2(edge.get(i)));
		}
		while(particles.remove(null))
		{
			//Removes all null particles. No code inside loop intentional.
		}
		return particles;
	}
}
