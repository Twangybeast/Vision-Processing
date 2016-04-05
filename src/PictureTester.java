import java.awt.Color;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.*;

import javax.swing.*;

public class PictureTester extends JFrame implements Runnable
{
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
		System.out.println("Left: "+inset.left);
		System.out.println("Top: "+inset.top);
	}
	private void mainCycle()
	{
		while(true)
		{
			if(processImage)
			{
				processImage=false;
				v=new Vision();
				map=v.createMap(image);
				double[] target=v.process(map);
				target[0]=(target[0]+1)*(image.getWidth()/2.0);
				target[1]=-1.0*(target[1]-1)*(image.getHeight()/2.0);
				this.target=new Point((int)target[0],(int)target[1]);
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
