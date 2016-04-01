
public class DelicateTest 
{
	static boolean[][] map;
	static String[] mapString=
	{
		//	    01234567890
/*			0*/"00000000000000000000",
/*			1*/"00000000000000000000",
/*			2*/"00000000000000000000",
/*			3*/"00000010000100000000",
/*			4*/"00000010000100000000",
/*			5*/"00000010000100000000",
/*			6*/"00000011111100000000",
/*			7*/"00000000000000000000",
/*			8*/"00000000000000000000",
/*			9*/"00000000000000000000"
	};
	public static void main(String[] args)
	{
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
		Vision vision=new Vision();
		double[] target=vision.process(map);
		
		System.out.println("("+target[0]+", "+target[1]+") Distance: "+target[2]);
	}
}
