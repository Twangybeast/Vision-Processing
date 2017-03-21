package exampleModules;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import com.github.sarxos.webcam.Webcam;

import code2017.Target;

public class ReadStream extends JFrame {
	private static final long serialVersionUID = -3531252242476033437L;
	final static float wMult = 4.0f;
	final static float hMult = 4.0f;
	public float fps = 0.0f;
	int imageWidth;
	int imageHeight;
	Socket socket = null;
	DataOutputStream out = null;
	DataInputStream in = null;
	final static byte[] INTRODUCTION = { 0x36, 0x6a, 0x59, 0x5c };
	final static byte IMAGE_TITLE = 0x2e;
	final static String hostName = "169.254.139.222";
	// "10.29.76.68";
	final static int portNumber = 5801;
	private BufferedImage image = null;
	private int failConsecutive = 0;
	private long count=0;
	private Webcam webcam = null;

	public static void main(String[] args) {
		ReadStream rs = new ReadStream(320, 240);
		rs.initWebcam();
		float measurement = 0.0f;
		final float smoothing = 0.9f;
		long t1 = System.currentTimeMillis();
		while (true) 
		{
			rs.exec();
			int msDiff = (int) (System.currentTimeMillis() - t1);
			try {
				Thread.sleep(Math.max(0, 20 - msDiff));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			measurement = (measurement * smoothing) + ((System.currentTimeMillis() - t1) * (1.0f - smoothing));
			rs.fps = round(1000.0f / measurement, 2);
			t1 = System.currentTimeMillis();
		}
	}

	public static float round(float value, int places) {
		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return (float) bd.doubleValue();
	}

	public ReadStream(int w, int h) {
		super();
		pack();
		imageWidth = w;
		imageHeight = h;
		setSize((int) (w * wMult) + getInsets().left + getInsets().right,
				(int) (h * hMult) + getInsets().bottom + getInsets().top);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}

	public void initialize() {
		try {
			socket = new Socket(hostName, portNumber);
			out = new DataOutputStream(socket.getOutputStream());
			in = new DataInputStream(socket.getInputStream());

			out.write(INTRODUCTION);
			byte[] reply = new byte[INTRODUCTION.length];
			in.read(reply, 0, INTRODUCTION.length);

			boolean replyMatch = true;
			for (int i = 0; i < INTRODUCTION.length; i++) {
				if (INTRODUCTION[i] != reply[i]) {
					replyMatch = false;
					System.out.printf("Reply did not match. At index [%d], expected [%d] got [%d\n", i, INTRODUCTION[i],
							reply[i]);
					break;
				}
			}

			if (replyMatch) {
				System.out.println("Connection established.");
			} else {
				System.out.println("Received bytes: ");
				for (int i = 0; i < reply.length; i++) {
					System.out.printf(" [%d]", reply[i]);
				}
				System.out.println("");
				System.out.println("Connection failed.");
			}

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void initWebcam() {
		// Include webcam initialization
		System.out.println("Getting webcams");
		List<Webcam> webcams = Webcam.getWebcams();
		System.out.println("Got webcams");
		if (webcams.size() == 0) {
			System.out.println("No webcams found. Exiting...");
			System.exit(1);
		}
		for (Webcam cam : webcams) {
			System.out.println("Webcam found: " + cam.getName());
		}
		webcam = webcams.get(0);
		System.out.println("Webcam gotten: " + webcam.getName());
		/*
		 * Dimension[] sizes=webcam.getViewSizes(); for(Dimension d: sizes) {
		 * System.out.printf("Possible size (w x h): [%d] x [%d]\n", d.width,
		 * d.height); }
		 */
		webcam.setViewSize(new Dimension(320, 240));
		webcam.open();
		System.out.println("Webcam opened.");
	}

	public void exec() {
		image = webcam.getImage();
		repaint();
	}

	public boolean execPI() {
		byte[] req = new byte[1];
		try {
			if (in.read(req, 0, req.length) > 0) {
				if (req[0] == IMAGE_TITLE) {
					byte[] bytes = new byte[4];
					in.read(bytes, 0, bytes.length);
					ByteBuffer bb = ByteBuffer.wrap(bytes);
					int imageSize = bb.getInt();
					bytes = new byte[imageSize];
					in.readFully(bytes);
					InputStream is = new ByteArrayInputStream(bytes);
					BufferedImage image = ImageIO.read(is);
					this.image = image;
					count++;
					System.out.printf("Image Number: [%d]\n", count);
					repaint();
				}
			} else {
				System.out.println("Got [-1] when reading from bytes, breaking from loop and closing webcam.");
				return false;
			}
			failConsecutive = 0;
		} catch (IOException e) {
			failConsecutive++;
			e.printStackTrace();
			if (failConsecutive > 3) {
				System.out.println("Too many errors. Exiting loop...");
				return false;
			}
		}
		return true;
	}

	public void paint(Graphics g) {
		if (image == null) {
			g.setColor(Color.GRAY);
			g.fillRect(getInsets().left, getInsets().top, (int) (imageWidth * wMult), (int) (imageHeight * hMult));
		} else {
			int w = image.getWidth();
			int h = image.getHeight();
			BufferedImage after = new BufferedImage((int) (w * wMult), (int) (h * hMult), BufferedImage.TYPE_INT_ARGB);
			AffineTransform at = new AffineTransform();
			at.scale(wMult, hMult);
			AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
			//scaleOp.filter(image, after);
			g.drawImage(image, getInsets().left, getInsets().top, null);
		}
		setTitle("FPS: " + fps);
	}
}
