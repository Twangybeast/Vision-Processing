

public class Particle 
{
	public int x;
	public int y;
	public boolean[][] map;
	public int count=1;
	public Particle(int x, int y, boolean[][] map)
	{
		this.x=x;
		this.y=y;
		this.map=map;
	}
	public double getX()
	{
		return x;
	}
	public double getY()
	{
		return y;
	}
	public boolean[][] getMap()
	{
		return map;
	}
	public boolean getGlobalValue(int x, int y)
	{
		return map[y-this.y][x-this.x];
	}
	public boolean setGlobalValue(int x, int y, boolean value)
	{
		try
		{
			map[y-this.y][x-this.x]=value;
		}
		catch(ArrayIndexOutOfBoundsException e)
		{
			System.out.println("Error");
			e.printStackTrace();
			return false;
		}
		return true;
	}
	public boolean globalInMap(int x, int y)
	{
		x=x-this.x;
		y=y-this.y;
		if(x<0||y<0)
		{
			return false;
		}
		if(x>=getWidth()||y>=getHeight())
		{
			return false;
		}
		return true;
	}
	public boolean getLocalValue(int x, int y)
	{
		return map[y][x];
	}
	public void setLocalValue(int x, int y, boolean value)
	{
		map[y][x]=value;
	}
	public boolean getValue(int x, int y)
	{
		return map[y][x];
	}
	public int getWidth()
	{
		return map[0].length;
	}
	public int getHeight()
	{
		return map.length;
	}
	public void expandRight()
	{
		boolean[] row;
		int rowLength=map[0].length;
		for(int i=0;i<map.length;i++)
		{
			row=new boolean[rowLength+1];
			System.arraycopy(map[i], 0, row, 0, map[i].length);
			map[i]=row;
		}
	}
	public void expandDown()
	{
		boolean[][] copy=new boolean[map.length+1][map[0].length];
		System.arraycopy(map, 0, copy, 0, map.length);
		copy[map.length]=new boolean[map[0].length];
		map=copy;
	}
	public void expandLeft()
	{
		boolean[] row;
		int rowLength=map[0].length;
		for(int i=0;i<map.length;i++)
		{
			row=new boolean[rowLength+1];
			System.arraycopy(map[i], 0, row, 1, map[i].length);
			map[i]=row;
		}
		x--;
	}
	public void expandUp()
	{
		boolean[][] copy=new boolean[map.length+1][map[0].length];
		System.arraycopy(map, 0, copy, 1, map.length);
		copy[0]=new boolean[map[0].length];
		map=copy;
		y--;
	}
	public void shorten()
	{
		if(map.length<1)
		{
			return;
		}
		boolean change=true;
		while(change)
		{
			change=false;
			boolean shortenFirst=true;
			boolean shortenLast=true;
			if(map.length<2)
			{
				shortenLast=false;
			}
			for(int i=0;i<map[0].length;i++)
			{
				if(map[0][i])
				{
					shortenFirst=false;
				}
				if(shortenLast)
				{
					if(map[map.length-1][i])
					{
						shortenLast=false;
					}
				}
			}
			if(shortenFirst)
			{
				change=true;
				y++;
				boolean[][] copy=new boolean[map.length-1][map[0].length];
				System.arraycopy(map, 1, copy, 0, copy.length);
				map=copy;
			}
			if(shortenLast)
			{
				change=true;
				boolean[][] copy=new boolean[map.length-1][map[0].length];
				System.arraycopy(map, 0, copy, 0, copy.length);
				map=copy;
			}
			shortenFirst=true;
			shortenLast=true;
			if(map[0].length<2)
			{
				shortenLast=false;
			}
			for(int i=0;i<map.length;i++)
			{
				if(map[i][0])
				{
					shortenFirst=false;
					if(!shortenLast)
					{
						break;
					}
				}
				if(shortenLast)
				{
					if(map[i][map[0].length-1])
					{
						shortenLast=false;
						if(!shortenFirst)
						{
							break;
						}
					}
				}
			}
			boolean[] row;
			if(shortenFirst)
			{
				int rowSize;
				x++;
				if(shortenLast)
				{
					rowSize=map[0].length-2;
					for(int i=0;i<map.length;i++)
					{
						row=new boolean[rowSize];
						System.arraycopy(map[i],1,row,0,row.length);
						map[i]=row;
					}
				}
				else
				{
					rowSize=map[0].length-1;
					for(int i=0;i<map.length;i++)
					{
						row=new boolean[rowSize];
						System.arraycopy(map[i],1,row,0,row.length);
						map[i]=row;
					}
				}
			}
			else
			{
				if(shortenLast)
				{
					int rowSize=map[0].length-1;
					for(int i=0;i<map.length;i++)
					{
						row=new boolean[rowSize];
						System.arraycopy(map[i],0,row,0,row.length);
						map[i]=row;
					}
				}
			}
		}
	}
}
