package code2017;

public class Dimension 
{
	public int width;
	public int height;
	public Dimension(int width, int height)
	{
		this.width=width;
		this.height=height;
	}
	public Dimension()
	{
		this.width=0;
		this.height=0;
	}
	public Dimension(Dimension dimension)
	{
		this.width=dimension.width;
		this.height=dimension.height;
	}
	public int getWidth()
	{
		return width;
	}
	public int getHeight()
	{
		return height;
	}
}
