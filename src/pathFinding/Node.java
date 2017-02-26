package pathFinding;

import java.util.ArrayList;

import code2017.Point;

public class Node
{
	public Node PreviousNode=null;
	public float totalDistance=0;
	public Point position;
	public Node(Point p)
	{
		position=p;
	}
	public Node(Node previousNode, float addDistance, Point p)
	{
		this.PreviousNode=previousNode;
		this.totalDistance=this.totalDistance+addDistance;
		this.position=p;
	}
	public ArrayList<Node> getNodes()
	{
		ArrayList<Node> nodes=new ArrayList<Node>();
		if(PreviousNode==null)
		{
			nodes.add(this);
			return nodes;
		}
		else
		{
			nodes.addAll(PreviousNode.getNodes());
			nodes.add(this);
			return nodes;
		}
	}
	public int getX()
	{
		return position.x;
	}
	public int getY()
	{
		return position.y;
	}
}
