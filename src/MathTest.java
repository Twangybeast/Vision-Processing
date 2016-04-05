import java.awt.Point;

/*
 * PURPOSE OF CLASS
 * 
 * This was meant to test the findParticles method
 * By using a precise input, we can examine the errors in the method when it was made
 */

public class MathTest 
{
	static String[] mapString=
		{
			//	    01234567890
	/*			0*/"11                11",
	/*			1*/"11                11",
	/*			2*/"11                11",
	/*			3*/"11                11",
	/*			4*/"11                11",
	/*			5*/"11                11",
	/*			6*/"11                11",
	/*			7*/"11                11",
	/*			8*/"11                11",
	/*			9*/"11                11",
	/*			0*/"11111111111111111111",
				   "11111111111111111111"
		};
	public static void main(String[] args)
	{
		boolean[][] map;
		map=new boolean[mapString.length][mapString[0].length()];
		for(int i=0;i<mapString.length;i++)
		{
			for(int j=0;j<mapString[0].length();j++)
			{
				if((mapString[i].charAt(j)+"").equals("1"))
				{
					map[i][j]=true;
				}
			}
		}
		Point point=centerMass(map);
		System.out.println("("+point.getX()+", "+point.getY()+")");
	}
	private static Point centerMass(boolean[][] particle)
	{
		int xTotal=0;
		int yTotal=0;
		int count=0;
		for(int y=0;y<particle.length;y++)
		{
			for(int x=0;x<particle[0].length;x++)
			{
				if(particle[y][x])
				{
					xTotal=xTotal+x;
					yTotal=yTotal+y;
					count++;
				}
			}
		}
		Point toReturn=new Point((int) (xTotal/(count*1.0)), (int) (yTotal/(count*1.0)));
		return toReturn;
	}
}
