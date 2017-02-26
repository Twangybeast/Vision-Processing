package pathFinding;

import java.awt.Rectangle;
import java.util.ArrayList;

import code2017.Particle;
import code2017.Point;

public class PathFinder
{

	public static void findPath(Point p1, Point p2, Particle particle, float[][] mag)
	{
		int x1, y1, x2, y2;
		if(p1.x<p2.x)
		{
			x1=p1.x;
			x2=p2.x;
		}
		else
		{
			x1=p2.x;
			x2=p1.x;
		}
		if(p1.y<p2.y)
		{
			y1=p1.y;
			y2=p2.y;
		}
		else
		{
			y1=p2.y;
			y2=p1.y;
		}
		float max=0;
		for(int x=x1;x<=x2;x++)
		{
			for(int y=y1;y<=y2;y++)
			{
				max = Math.max(max, mag[y][x]);
			}
		}
		//Now values from mag will be used as max-mag
		ArrayList<Node> list=new ArrayList<Node>();
		list.add(new Node(p1));
		ArrayList<Node> nodes = exec(list, mag, max, p2, new Rectangle(x1, y1, 1+x2-x1, 1+y2-y1));
		for(Node n: nodes)
		{
			particle.setLocalValue(n.getX(), n.getY(), true);
		}
	}
	public static ArrayList<Node> exec(ArrayList<Node> list, float[][] mag, float max, Point end, Rectangle region)
	{
		if(list.size()==0)
		{
			return new ArrayList<Node>();
		}
		Node current=list.get(0);
		ArrayList<Point> points=new ArrayList<Point>();
		int x=current.getX();
		int y=current.getY();
		int x1, y1;
		//NW, NE, SW, SE, N, S, W, E
		x1=x-1;
		y1=y-1;
		if(region.contains(x, y))
		{
			points.add(new Point(x1, y1));
		}
		x1=x+1;
		y1=y-1;
		if(region.contains(x, y))
		{
			points.add(new Point(x1, y1));
		}
		x1=x-1;
		y1=y+1;
		if(region.contains(x, y))
		{
			points.add(new Point(x1, y1));
		}
		x1=x+1;
		y1=y+1;
		if(region.contains(x, y))
		{
			points.add(new Point(x1, y1));
		}
		x1=x;
		y1=y-1;
		if(region.contains(x, y))
		{
			points.add(new Point(x1, y1));
		}
		x1=x;
		y1=y+1;
		if(region.contains(x, y))
		{
			points.add(new Point(x1, y1));
		}
		x1=x-1;
		y1=y;
		if(region.contains(x, y))
		{
			points.add(new Point(x1, y1));
		}
		x1=x+1;
		y1=y;
		if(region.contains(x, y))
		{
			points.add(new Point(x1, y1));
		}
		for(int i=0;i<points.size();i++)
		{
			Point p=points.get(i);
			float distance=max-mag[p.y][p.x];
			if(mag[p.y][p.x]>10.0f)
			{
				mag[p.y][p.x]=0.0f;
				Node n=new Node(current, distance, p);
				if(p.equals(end))
				{
					return n.getNodes();
				}
				placeSortedInList(n, list);
			}
		}
		list.remove(current);
		return exec(list, mag, max, end, region);
	}
	public static void placeSortedInList(Node node, ArrayList<Node> list)
	{
		for(int i=0;i<list.size();i++)
		{
			if(node.totalDistance<list.get(i).totalDistance)
			{
				list.add(i,node);
				return;
			}
		}
		list.add(node);
	}
	private static boolean inMap(int x, int y, float[][] map)
	{
		if(x < 0 || y < 0)
		{
			return false;
		}
		if(x >= map[0].length || y >= map.length)
		{
			return false;
		}
		return true;
	}
}
