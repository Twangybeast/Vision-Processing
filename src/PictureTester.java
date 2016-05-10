import java.awt.Color;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.image.*;

import javax.swing.*;
import javax.swing.event.*;

import visionCore.Particle;
import visionCore.Vision;
import keyboard.KeyboardInput;

public class PictureTester extends JFrame implements Runnable, ChangeListener
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
	private JSlider hueMin=new JSlider(JSlider.HORIZONTAL,0,180,Vision.HMIN);
	private JSlider hueMax=new JSlider(JSlider.HORIZONTAL,0,180,Vision.HMAX);
	private JSlider satMin=new JSlider(JSlider.HORIZONTAL,0,255,Vision.SMIN);
	private JSlider satMax=new JSlider(JSlider.HORIZONTAL,0,255,Vision.SMAX);
	private JSlider valMin=new JSlider(JSlider.HORIZONTAL,0,255,Vision.VMIN);
	private JSlider valMax=new JSlider(JSlider.HORIZONTAL,0,255,Vision.VMAX);
	private JLabel[] labels=new JLabel[6];
	private int[] hsv={-1,-1,-1,-1,-1,-1};
	public PictureTester(BufferedImage image)
	{
		super();
		setLayout(null);
		t=new Thread(this);
		this.image=image;
		setSize(image.getWidth()*2+8,image.getHeight()+280+31);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//Sliders
		hueMin.setMajorTickSpacing(45);
		hueMin.setMinorTickSpacing(5);
		hueMin.setPaintTicks(true);
		hueMin.setPaintLabels(true);
		hueMax.setMajorTickSpacing(45);
		hueMax.setMinorTickSpacing(5);
		hueMax.setPaintTicks(true);
		hueMax.setPaintLabels(true);
		
		satMin.setMajorTickSpacing(255);
		satMin.setMinorTickSpacing(5);
		satMin.setPaintTicks(true);
		satMin.setPaintLabels(true);
		satMax.setMajorTickSpacing(255);
		satMax.setMinorTickSpacing(5);
		satMax.setPaintTicks(true);
		satMax.setPaintLabels(true);
		valMin.setMajorTickSpacing(255);
		valMin.setMinorTickSpacing(5);
		valMin.setPaintTicks(true);
		valMin.setPaintLabels(true);
		valMax.setMajorTickSpacing(255);
		valMax.setMinorTickSpacing(5);
		valMax.setPaintTicks(true);
		valMax.setPaintLabels(true);
		
		hueMin.setName("hmin");
		hueMax.setName("hmax");
		satMin.setName("smin");
		satMax.setName("smax");
		valMin.setName("vmin");
		valMax.setName("vmax");
		
		hueMin.addChangeListener(this);
		hueMax.addChangeListener(this);
		satMin.addChangeListener(this);
		satMax.addChangeListener(this);
		valMin.addChangeListener(this);
		valMax.addChangeListener(this);
		
		hueMin.addKeyListener(keyboard);
		hueMax.addKeyListener(keyboard);
		satMin.addKeyListener(keyboard);
		satMax.addKeyListener(keyboard);
		valMin.addKeyListener(keyboard);
		valMax.addKeyListener(keyboard);
		
		add(hueMin);
		add(hueMax);
		add(satMin);
		add(satMax);
		add(valMin);
		add(valMax);
		for(int i=0;i<6;i++)
		{
			labels[i]=new JLabel("050");
			add(labels[i]);
		}
		
		hueMin.setBounds(new Rectangle(new Point(8+20, 	-90+31+image.getHeight()+100),hueMin.getPreferredSize()));
		hueMax.setBounds(new Rectangle(new Point(8+220, -90+31+image.getHeight()+100),hueMax.getPreferredSize()));
		satMin.setBounds(new Rectangle(new Point(8+20, 	-90+31+image.getHeight()+180),satMin.getPreferredSize()));
		satMax.setBounds(new Rectangle(new Point(8+220, -90+31+image.getHeight()+180),satMax.getPreferredSize()));
		valMin.setBounds(new Rectangle(new Point(8+20, 	-90+31+image.getHeight()+260),valMin.getPreferredSize()));
		valMax.setBounds(new Rectangle(new Point(8+220, -90+31+image.getHeight()+260),valMax.getPreferredSize()));
		
		labels[0].setBounds(new Rectangle(new Point(8+120,-90+31+image.getHeight()+70),labels[0].getPreferredSize()));
		labels[1].setBounds(new Rectangle(new Point(8+310,-90+31+image.getHeight()+70),labels[1].getPreferredSize()));
		labels[2].setBounds(new Rectangle(new Point(8+120,-90+31+image.getHeight()+150),labels[2].getPreferredSize()));
		labels[3].setBounds(new Rectangle(new Point(8+310,-90+31+image.getHeight()+150),labels[3].getPreferredSize()));
		labels[4].setBounds(new Rectangle(new Point(8+120,-90+31+image.getHeight()+230),labels[4].getPreferredSize()));
		labels[5].setBounds(new Rectangle(new Point(8+310,-90+31+image.getHeight()+230),labels[5].getPreferredSize()));
		
		hueMin.repaint();
		hueMax.repaint();
		satMin.repaint();
		satMax.repaint();
		valMin.repaint();
		valMax.repaint();
		
		stateChanged(new ChangeEvent(hueMin));
		stateChanged(new ChangeEvent(hueMax));
		stateChanged(new ChangeEvent(satMin));
		stateChanged(new ChangeEvent(satMax));
		stateChanged(new ChangeEvent(valMin));
		stateChanged(new ChangeEvent(valMax));
		//End Sliders
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
				v=new Vision(hsv);
				map=v.createMap(image);
				double[] target=v.process(image);
				target[0]=(target[0]+1)*(image.getWidth()/2.0);
				target[1]=-1.0*(target[1]-1)*(image.getHeight()/2.0);
				this.target=new Point((int)target[0],(int)target[1]);
				if(v.bestParticle!=null)
				{
					particle=v.bestParticle;
				}
				System.out.println("Processed");
			}
			keyboard.updateKeys();
			if(keyboard.keyOnce(d)||keyboard.keyOnce(d))
			{
				imageQ++;
			}
			if(keyboard.keyOnce(a)||keyboard.keyOnce(a))
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
		g.setColor(Color.WHITE);
		g.fillRect(0, image.getHeight()+1, image.getWidth()*2, image.getHeight());
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
		g.fillRect(target.x-(radius/2), target.y-(radius/2), radius, radius);
		g.drawImage(image, image.getWidth(), 0, null);
		g.fillRect(target.x+image.getWidth()-(radius/2), target.y-(radius/2), radius, radius);
		frameG.drawImage(picture, inset.left, inset.top, null);
		hueMin.repaint();
		hueMax.repaint();
		satMin.repaint();
		satMax.repaint();
		valMin.repaint();
		valMax.repaint();
		for(int i=0;i<6;i++)
		{
			labels[i].repaint();
		}
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
	@Override
	public void stateChanged(ChangeEvent e)
	{
		JSlider source=(JSlider)e.getSource();
		if(!source.getValueIsAdjusting())
		{
			switch(source.getName())
			{
				case "hmin":
					hsv[0]=source.getValue();
					labels[0].setText(""+hsv[0]);
					break;
				case "hmax":
					hsv[1]=source.getValue();
					labels[1].setText(""+hsv[1]);
					break;
				case "smin":
					hsv[2]=source.getValue();
					labels[2].setText(""+hsv[2]);
					break;
				case "smax":
					hsv[3]=source.getValue();
					labels[3].setText(""+hsv[3]);
					break;
				case "vmin":
					hsv[4]=source.getValue();
					labels[4].setText(""+hsv[4]);
					break;
				case "vmax":
					hsv[5]=source.getValue();
					labels[5].setText(""+hsv[5]);
					break;
				default:
			}
			processImage=true;
		}
	}
}
