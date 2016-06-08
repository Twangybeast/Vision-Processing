package algorithm;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;

import visionCore.Particle;
import visionCore.RGB;

public class ParticleFinder
{
	public static ArrayList<Particle> findParticles(BufferedImage image)
	{
		short[][][] hsvArray=getHSVArray(image);
		if(hsvArray.length<1)
		{
			System.err.println("Image empty.");
			return new ArrayList<Particle>();
		}
		/*
		 * Use contrast to find particles.
		 * First, check if pixel falls in general range of HSV.
		 * Using map of only general range pixels, find particles based on contrast compared to surrounding pixels.
		 * Then, smoothen each particle: 
		 * -Reduce blur on boundaries.
		 * -Solidify particle more?
		 */
		Particle general=new Particle(0,0,new boolean[hsvArray.length][hsvArray[0].length]);
		for(int i=0;i<hsvArray.length;i++)
		{
			for(int j=0;j<hsvArray.length;j++)
			{
				short[] hsv=hsvArray[i][j];
				boolean valid=true;
				int allowance=(int) ((-7.0/10.0)*hsv[1]+100);
				allowance=Math.max(allowance, 15);
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
				general.map[i][j] = valid;
			}
		}
		boolean[][][] contrast=new boolean[hsvArray.length][hsvArray[0].length][4];
		/*
		 * Contrast:
		 * x y index
		 * - 0 0
		 * + 0 1
		 * 0 - 2
		 * 0 + 3
		 * TRUE for low contrast acceptable for threshold
		 * FALSE for high contrast unacceptable for threshold
		 */
		/*
		 * |			     /\			|---\	  /---		|----			|\     |	  /----\	-------		|----
		 * |			    /  \		|    |	 /    \		|				| \    |	 /      \	   |		|
		 * |			   /    \		|---/	|			|				|  \   |	|   _   |	   |		|
		 * |			  /------\		|\		|    __		|--				|   \  |	|  |_|  |	   |		|--
		 * |			 /        \		| \		 \     |	|				|    \ |	 \      /	   |		|
		 * |________	/          \	|  \	  \___/		|----			|     \|	  \----/	   |		|----
		 * Check for valid in general before marking contrast?
		 */
		final int CONTRAST_THRESHOLD=50;
		for(int x=0;x<general.getWidth();x++)
		{
			for(int y=0;y<general.getHeight();y++)
			{
				if(general.getLocalValue(x, y))
				{
					byte index=0;
					for(int i=0;i<4;i++)
					{
						int xd;
						int yd;
						if(index>1)
						{
							yd=2*(index-2)-1;
							xd=0;
						}
						else
						{
							xd=(2*index)-1;
							yd=0;
						}
						short[] neighbor=null;
						int x1=x+xd;
						int y1=y+yd;
						if(x1>=0&&x1<hsvArray[0].length&&y1>=0&&y1<hsvArray.length)
						{
							neighbor=hsvArray[y1][x1];
							if(getContrast(hsvArray[y][x], neighbor)<=CONTRAST_THRESHOLD)
							{
								contrast[y][x][index]=true;
							}
						}
						else
						{
							contrast[y][x][index]=false;
						}
						index++;
					}
				}
			}
		}
		ArrayList<Particle> particles=new ArrayList<>();
		
		return particles;
	}
	public static int getContrast(short[] hsvArray, short[] neighbor)
	{
		return 0;
	}
	public static short[] getHSV(short[] rgb)
	{
		return getHSV(rgb[0], rgb[1], rgb[2]);
	}
	public static short[] getHSV(int red, int green, int blue)
	{
		//Calculations based of this website http://www.rapidtables.com/convert/color/rgb-to-hsv.htm
		short[] hsv=new short[3];
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
					hsv[0]=(short) (60.0*(((g-b)/delta)%6));
					break;
				case GREEN:
					hsv[0]=(short) (60.0*(((b-r)/delta)+2));
					break;
				case BLUE:
					hsv[0]=(short) (60.0*(((r-g)/delta)+4));
					break;
				default:
					assert false;//OOH! Fancy Keywords! But realisticly, if it gets here, the program is messed up. A lot.
			}
		}
		if(max==0)
		{
			hsv[1]=0;
		}
		else
		{
			hsv[1]=(short) Math.round(100.0*delta/max);
		}
		hsv[2]=(short) Math.round(100.0*max);
		return hsv;
	}
	public static short[][][] getArray(BufferedImage image)
	{

		final byte[] pixels = ((DataBufferByte) image.getRaster()
				.getDataBuffer()).getData();
		final int width = image.getWidth();
		final int height = image.getHeight();
		final boolean hasAlphaChannel = image.getAlphaRaster() != null;

		short[][][] result = new short[height][width][4];
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
				result[row][col][0] = (short) (pixels[pixel + 3] & 0xff);// red
				result[row][col][1] = (short) (pixels[pixel + 2] & 0xff);// green
				result[row][col][2] = (short) (pixels[pixel + 1] & 0xff);// blue
				result[row][col][3] = (short) (pixels[pixel] & 0xff);// alpha
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
				result[row][col][0] = (short) (pixels[pixel + 2] & 0xff);// red
				result[row][col][1] = (short) (pixels[pixel + 1] & 0xff);// green
				result[row][col][2] = (short) (pixels[pixel] & 0xff);// blue
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
	public static short[][][] getHSVArray(short[][][] image)
	{
		short[][][] map=new short[image.length][image[0].length][3];
		for(int i=0;i<image.length;i++)
		{
			for(int j=0;j<image[0].length;j++)
			{
				map[i][j]=getHSV(image[i][j]);
			}
		}
		return map;
	}
	public static short[][][] getHSVArray(BufferedImage image)
	{
		return ParticleFinder.getHSVArray(getArray(image));
	}

}
