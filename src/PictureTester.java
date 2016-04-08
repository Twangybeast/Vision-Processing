import java.awt.Color;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.image.*;

import javax.swing.*;

public class PictureTester extends JFrame implements Runnable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6265417222272758163L;
	//Size of Target Marker
	final int radius=4;
	//Controls
	final int right=KeyEvent.VK_RIGHT;
	final int left=KeyEvent.VK_LEFT;
	final int a=KeyEvent.VK_A;
	final int d=KeyEvent.VK_D;
	//End Controls
	private BufferedImage image;
	private Insets inset;
	private KeyboardInput keyboard=new KeyboardInput();
	private Thread t;
	private int imageQ=0;
	private boolean processImage=true;
	private Vision v;
	private boolean[][] map;
	private Point target=new Point(-1,-1);
	private Particle particle=null;
	public PictureTester(BufferedImage image)
	{
		super();
		t=new Thread(this);
		this.image=image;
		setSize(image.getWidth()*2+8,image.getHeight()+31);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		addKeyListener(keyboard);
		setVisible(true);
		t.start();
		this.inset=getInsets();
	}
	private void mainCycle()
	{
		long time;
		while(true)
		{
			time=System.currentTimeMillis();
			if(processImage)
			{
				processImage=false;
				v=new Vision();
				map=v.createMap(image);
				double[] target=v.process(map);
				target[0]=(target[0]+1)*(image.getWidth()/2.0);
				target[1]=-1.0*(target[1]-1)*(image.getHeight()/2.0);
				this.target=new Point((int)target[0],(int)target[1]);
				if(v.bestParticle!=null)
				{
					particle=v.bestParticle;
				}
			}
			keyboard.updateKeys();
			if(keyboard.keyOnce(right)||keyboard.keyOnce(d))
			{
				imageQ++;
			}
			if(keyboard.keyOnce(left)||keyboard.keyOnce(a))
			{
				imageQ--;
			}
			repaint();
			time=50-(System.currentTimeMillis()-time);
			if(time<0)
			{
				time=0;
			}
			try
			{
				Thread.sleep(time);
			} catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public void paint(Graphics frameG)
	{
		BufferedImage picture=new BufferedImage((int)(image.getWidth())*2,(int)(image.getHeight()), BufferedImage.TYPE_INT_RGB);
		Graphics g=picture.getGraphics();
		g.setColor(Color.YELLOW);
		if(map!=null)
		{
			for(int i=0;i<image.getHeight();i++)
			{
				for(int j=0;j<image.getWidth();j++)
				{
					if(map[i][j])
					{
						g.fillRect(j, i, 1, 1);
					}
				}
			}
		}
		if(particle!=null)
		{
			g.setColor(Color.RED);
			for(Point corner: particle.corners)
			{
				//g.fillRect(corner.x+particle.x, corner.y+particle.y, 2, 2);
				g.drawLine(target.x, target.y,corner.x+particle.x, corner.y+particle.y);
			}
		}
		g.setColor(Color.CYAN);
		g.fillRect(target.x, target.y, radius, radius);
		g.drawImage(image, image.getWidth(), 0, null);
		frameG.drawImage(picture, inset.left, inset.top, null);
	}
	public void setImage(BufferedImage image)
	{
		this.image=image;
		processImage=true;
	}
	public int getImageQueue()
	{
		int toReturn=imageQ;
		imageQ=0;
		return toReturn;
	}
	@Override
	public void run()
	{
		mainCycle();
		
	}
}
