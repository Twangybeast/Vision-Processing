package code2017;

public class Point 
{
	public int x;
	public int y;
	public Point(int x, int y)
	{
		this.x=x;
		this.y=y;
	}
	public Point()
	{
		x=0;
		y=0;
	}
	public Point(Point point)
	{
		this.x=point.x;
		this.y=point.y;
	}
	public int getX()
	{
		return x;
	}
	public int getY()
	{
		return y;
	}
	public void translate(int x, int y) 
	{
		this.x=this.x+x;
		this.y=this.y+y;
	}
}
