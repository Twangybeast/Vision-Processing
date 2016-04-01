import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;

public class Vision
{
	// Because some code was copy pas- *ahem* written by me, arrays that
	// represent images are weird as they work as map[y][x]
	// Detail:the reason for this is because the objects were created as
	// [row][column], this is found in getArray where you find [height][width]
	final double viewAngle = 1.515;
	/*
	 * LIST OF VIEW ANGLES add when found 
	 * MICROSOFT Lifecam HD-3000= {1.515 }
	 */
	//----------------------UNUSED-----------------------
	final int colorThreshold = 130;
	final int colorDifference = 30;
	// red=0
	// green=1
	// blue=2
	//
	//-------------------HSL------------------------------------------------------------------
	// Lots of variables to adjust HSL threshold, naming scheme initial of hsl
	// type and min/max threshold, inclusive; Default GRIP tutorial
	final int hmin = 63;
	final int hmax = 96;
	final int smin = 20;
	final int smax = 255;// This value not used as it is maximum. If changed go to createMap method to add into code
	final int lmin = 40;
	final int lmax = 161;
	// End of lots of variables
	//--------------------FIND PARTICLES------------------------------------------------------
	final double totalPercent = 0.0006510416666666666;
	int minimumAlive = 200;// Minimum alive cells in particle to be considered particle. This is default value. Real value based on totalPercent.		
	final double maxRatio=3.5;//Highest simple ratio number can have to be valid
	final int largeMinAlive=500;//Higher minimum alive cell count assuming there is one that satisfies this one
	final int furthestDistance = 400;
	//----------------------XY PROFILE---------------------------------------------------
	//DON'T LOOK HERE
	//Data found experimentally, good luck have fun
	final double[] profileMaxX = { 0.5325419745487893, 0.7722804963523211,
			0.9437657370298176, 1.0232068477333296, 1.049626613273606,
			1.0609269644417032, 1.0574181511285068, 1.0492152986857035,
			1.0446743664223905, 1.0317483659740134, 1.0374145675127089,
			1.07219296306833, 1.018050547507774, 0.9194192618397579,
			0.7913106378571826, 0.6801335686045272, 0.5732009307630057,
			0.47435216452105144, 0.4068677557293565, 0.34421431057124097,
			0.28032910523377613, 0.243026130621746, 0.21550982911462735,
			0.20494623310757853, 0.20047674349750916, 0.19767388390252877,
			0.197720369136684, 0.19749555365904653, 0.1966911318998693,
			0.19643333701086751, 0.1971169425571753, 0.1957908146219804,
			0.19552300924524643, 0.19565255654155542, 0.19468088461890623,
			0.19419006810099093, 0.19489452308002497, 0.19474206817185669,
			0.1951117240580812, 0.19651591818643768, 0.19740953043465662,
			0.1968813830788542, 0.19692696396270137, 0.1958027307632876,
			0.1951828225174134, 0.19556780708088445, 0.19562764044293757,
			0.19612008158771135, 0.1966472097232204, 0.19660949803398145,
			0.19660949803398145, 0.1966472097232204, 0.19612008158771135,
			0.19562764044293757, 0.19556780708088445, 0.1951828225174134,
			0.1958027307632876, 0.19692696396270137, 0.1968813830788542,
			0.19740953043465662, 0.19651591818643768, 0.1951117240580812,
			0.19474206817185669, 0.19489452308002497, 0.19419006810099093,
			0.19468088461890623, 0.19565255654155542, 0.19552300924524643,
			0.1957908146219804, 0.1971169425571753, 0.19643333701086751,
			0.1966911318998693, 0.19749555365904653, 0.197720369136684,
			0.19767388390252877, 0.20047674349750916, 0.20494623310757853,
			0.21550982911462735, 0.243026130621746, 0.28032910523377613,
			0.34421431057124097, 0.4068677557293565, 0.47435216452105144,
			0.5732009307630057, 0.6801335686045272, 0.7913106378571826,
			0.9194192618397579, 1.018050547507774, 1.07219296306833,
			1.0374145675127089, 1.0317483659740134, 1.0446743664223905,
			1.0492152986857035, 1.0574181511285068, 1.0609269644417032,
			1.049626613273606, 1.0232068477333296, 0.9437657370298176,
			0.7722804963523211, 0.5325419745487893 };
	final double[] profileMaxY = { 0.12193135366010589, 0.12703006495864894,
			0.14518035051864792, 0.15289980335246955, 0.16174827134078565,
			0.17361103656735818, 0.17916152501223062, 0.18152339915912497,
			0.18781491825201935, 0.19273497505438686, 0.1996116766378952,
			0.20758578846450312, 0.21580813387964115, 0.222364956612006,
			0.2253929879098854, 0.23277642949042723, 0.23650360058740905,
			0.23982370264623296, 0.24209486136890102, 0.2422386484300219,
			0.24457883471411607, 0.24792766839021502, 0.25082427642010013,
			0.2520831073381642, 0.2529583200908265, 0.2532229263735606,
			0.25369254030784666, 0.2551786343559369, 0.25486683345537536,
			0.2525791552938892, 0.2526794102577287, 0.2530030142328574,
			0.252292218075372, 0.2520628791421964, 0.2523001305676126,
			0.2548276355654818, 0.2531652368972359, 0.2531183648700706,
			0.25484213099276626, 0.25494936893662684, 0.2545397214570442,
			0.2532696181239743, 0.2515291100367782, 0.2515613960056998,
			0.2514380394152972, 0.25122612387684334, 0.25534380704115006,
			0.25652237968771086, 0.2553330136713359, 0.2562685861018664,
			0.2586217691922319, 0.2585496495336308, 0.2620202311945956,
			0.263324574711494, 0.2663114929559388, 0.2688802888415248,
			0.27223785303711895, 0.2809687957650937, 0.28902465906035923,
			0.30210088876915125, 0.31591475714768313, 0.3352879136018885,
			0.350741832321321, 0.36733913958144676, 0.38927812394426503,
			0.4157309628901814, 0.4462789475300839, 0.47686729996562693,
			0.5083834209803791, 0.5431188871172126, 0.589945845522837,
			0.624281547371798, 0.6659741637601201, 0.7119242437166428,
			0.7572716549843661, 0.8035198700909241, 0.8428210155729507,
			0.8920977389931223, 0.9342417327231008, 0.9804075152273352,
			1.0198875791840776, 1.063989824598999, 1.1199284578219508,
			1.1613501698335593, 1.193577096878319, 1.1924309567342442,
			1.1786159071065367, 1.1645674311832162, 1.1492111890146004,
			1.1287185905189232, 1.1008794149153454, 1.0768840675563864,
			1.048323520569128, 1.0169364788029793, 0.9771959047325056,
			0.9039267362474883, 0.8418678094813794, 0.7622854628835617,
			0.5810810230321509, 0.3283870447301824 };
	final double[] profileMinX = { -0.16852752284136432, -0.14501327330537173,
			-0.03824013373685892, 0.07104773558856164, 0.18039099727022045,
			0.2702628956982512, 0.34329225977533007, 0.4104041340446292,
			0.4581510961559417, 0.49889122336539526, 0.47510537753581716,
			0.2922930492826088, 0.07495234208992685, -0.038680811134493176,
			-0.06273215302582552, -0.06750379344755009, -0.05553750870531898,
			-0.026669486907982737, -0.004669852246615314, 0.02408560939810639,
			0.05625239383864561, 0.07835501900487814, 0.09590516402449704,
			0.10228523504460013, 0.10431934454790219, 0.10653733739032245,
			0.10707328025120466, 0.1074202037393408, 0.10791207537395577,
			0.1070282735411121, 0.1064788502537177, 0.10795560442444758,
			0.10813697836076838, 0.10780963121374712, 0.1089478180702951,
			0.10986758407572328, 0.10895107463212376, 0.10921422229838447,
			0.10937648141073658, 0.10881459098704979, 0.10798257230528832,
			0.10828235509977305, 0.10820969496577469, 0.10841483526896531,
			0.10947996331789481, 0.10949419754490058, 0.10944902709653959,
			0.10898885367406111, 0.10860678386326775, 0.1094939535645382,
			0.1094939535645382, 0.10860678386326775, 0.10898885367406111,
			0.10944902709653959, 0.10949419754490058, 0.10947996331789481,
			0.10841483526896531, 0.10820969496577469, 0.10828235509977305,
			0.10798257230528832, 0.10881459098704979, 0.10937648141073658,
			0.10921422229838447, 0.10895107463212376, 0.10986758407572328,
			0.1089478180702951, 0.10780963121374712, 0.10813697836076838,
			0.10795560442444758, 0.1064788502537177, 0.1070282735411121,
			0.10791207537395577, 0.1074202037393408, 0.10707328025120466,
			0.10653733739032245, 0.10431934454790219, 0.10228523504460013,
			0.09590516402449704, 0.07835501900487814, 0.05625239383864561,
			0.02408560939810639, -0.004669852246615314, -0.026669486907982737,
			-0.05553750870531898, -0.06750379344755009, -0.06273215302582552,
			-0.038680811134493176, 0.07495234208992685, 0.2922930492826088,
			0.47510537753581716, 0.49889122336539526, 0.4581510961559417,
			0.4104041340446292, 0.34329225977533007, 0.2702628956982512,
			0.18039099727022045, 0.07104773558856164, -0.03824013373685892,
			-0.14501327330537173, -0.16852752284136432 };
	final double[] profileMinY = { -0.012565949576067006,
			-0.009505449356118134, 0.040042737082312506, 0.053574229835870776,
			0.0542074692574096, 0.05264863496662609, 0.05102331911027591,
			0.05388853633109911, 0.05465971072216762, 0.053718135911625775,
			0.05389623812767125, 0.054646174054458735, 0.055967748823494576,
			0.05544488040523769, 0.05879040384017223, 0.059581659150788974,
			0.06194470961782578, 0.06391308679539512, 0.06543748880750362,
			0.07101905633439808, 0.07488016556487281, 0.07610800534762961,
			0.08620654845770641, 0.09167249947201436, 0.09792411613919891,
			0.10761240362218832, 0.11187471602720933, 0.11716560324031126,
			0.12137344772724389, 0.1281050801806022, 0.13167008467282143,
			0.1335741786994158, 0.13700387321261082, 0.13891416767823872,
			0.1404715960437331, 0.13959610297631905, 0.14369847117701448,
			0.14692910006039545, 0.14710717998896605, 0.1494973729403744,
			0.1526191603887237, 0.15511028545957675, 0.15937841588820234,
			0.1613804828158908, 0.162052520748342, 0.1635320543448129,
			0.1616017995727234, 0.1622581774472371, 0.16383796769663628,
			0.16487189171530026, 0.1634177607804433, 0.16328720304095765,
			0.1615773380762056, 0.16151628151190953, 0.1597930549312745,
			0.15945548824728328, 0.15836543897071823, 0.1537185530715592,
			0.1505394471523281, 0.1445049921707754, 0.1385556486959037,
			0.12654047704795984, 0.11965450099573215, 0.11419107667626799,
			0.10830976108987722, 0.09732249107900756, 0.08565244503029967,
			0.07952628824565308, 0.07298130231865338, 0.06832083446382253,
			0.06803832476537874, 0.07287837308132977, 0.0826283581326489,
			0.08918628491891367, 0.10134667056738045, 0.11817729804259369,
			0.12519913619107376, 0.14265816817519672, 0.1689190951347953,
			0.20452183681338465, 0.27769407037825794, 0.33093005191003494,
			0.3827442488408085, 0.3825908369739944, 0.36494939014814254,
			0.326053975136237, 0.29160548595406216, 0.24754608329366057,
			0.19208736368781304, 0.14738047915029606, 0.0963964429094113,
			0.04678820175465448, 0.0028948404016799767, -0.04839095305519103,
			-0.09823268067940166, -0.13611624362816493, -0.17919975542559263,
			-0.2155597905937513, -0.20134609730818862, -0.158912037398586 };
	int[] tWidth;// Width based on equivalent rectangle
	//THOSE ARRAYS TAKE UP A LOT OF SPACE
	
	//-----------------------COVERAGE AREA----------------------------------------
	final double dev=0.03715117993112262*5.0;
	final double ideal=0.27858733495702565;
	
	//---------------------GLOBAL VARIABLE 
	int[] tHeight;// Height based on equivalent rectangle
	double[] angles;
	Point[] targetLocations;
	Point[] COMasses;
	int largestCount=0;
	public double[] process(boolean[][] map)
	{
		minimumAlive = (int) (totalPercent * (map.length * map[0].length));
		double[] toReturn = new double[4];
		// Returns center of mass, x returns 2 when none detected
		// toReturn[0] x position of target, in coordinate system of -1.0 to 1.0 left to right
		// toReturn[1] y position of target, in coordinate system of -1.0 to 1.0 bottom to top
		// toReturn[2] distance to target, units should be inches
		// toReturn[3] angle of the target, in radians
		ArrayList<Particle> particles = null;
		long start = System.currentTimeMillis();
		particles = findParticles(map);
		System.out.println("Particle Finding Time: "
				+ (System.currentTimeMillis() - start));
		System.out.println("Particles Found: " + particles.size());
		if (particles.size() == 0)// No targets detected
		{
			toReturn[0] = 2.0;
			toReturn[1] = 2.0;
			toReturn[2] = 0.0;
			toReturn[3] = 100.0;
			return toReturn;
		}
		ArrayList<int[]> score = new ArrayList<int[]>();
		tWidth = new int[particles.size()];
		tHeight = new int[particles.size()];
		targetLocations = new Point[particles.size()];
		COMasses = new Point[particles.size()];
		angles = new double[particles.size()];
		for (int i = 0; i < particles.size(); i++)
		{
			centerMass(particles.get(i), i);
			int[] Score = new int[5];
			// less is better
			Score[0]=coverageArea(particles.get(i));
			Score[1] = equivalentRectangle(particles.get(i), i); // Moderately works
			// Score[2]=moment(particles.get(i),i);
			Score[3]=xyprofile(particles.get(i));
			Score[4]=Score[0]+Score[1]+Score[2]+Score[3];
			//Score[4] = 0 - particles.get(i).count;// Very stupid method that should work better
			score.add(Score);
		}
		boolean impossibleTarget = true;
		while (impossibleTarget)
		{
			int recordIndex = 0;
			int record = 99999;// Absurd number that is easy to beat
			for (int i = 0; i < score.size(); i++)
			{
				if (score.get(i)[4] < record)
				{
					recordIndex = i;
					record = score.get(i)[4];
				}
			}
			// Distance calculation
			toReturn[2] = (20.0 * map[0].length)
					/ (2.0 * tWidth[recordIndex] * Math.tan(viewAngle / 2.0));
			if (toReturn[2] < furthestDistance)
			{
				impossibleTarget = false;
				Particle particle = particles.get(recordIndex);
				toReturn[0] = targetLocations[recordIndex].getX()
						+ particle.getX();
				toReturn[1] = targetLocations[recordIndex].getY()
						+ particle.getY();
				// Visual Demo
				/*
				 * Demo demo=new Demo(map[0].length,map.length,map);
				 * demo.particleTest(demo.getGraphics(),particles,recordIndex);
				 * demo.drawPoint(demo.getGraphics(), (int)(toReturn[0]),
				 * (int)(toReturn[1]));
				 */// demo.drawPoint(demo.getGraphics(),
					// (int)(COMasses[recordIndex].getX()+particle.getX()),
					// (int)(COMasses[recordIndex].getY()+particle.getY()),Color.GREEN);
					// Converts coordinate system to domain & range of -1 to 1,
					// centered on center of image
				toReturn[0] = ((toReturn[0]) - (map[0].length / 2.0))
						/ (map[0].length / 2.0);
				toReturn[1] = -1.0 * ((toReturn[1]) - (map.length / 2.0))
						/ (map.length / 2.0);
				toReturn[3] = angles[recordIndex];
			} 
			else
			{
				int[] badScore = { 200, 200, 200, 200, 800 };
				score.set(recordIndex, badScore);
			}
		}
		return toReturn;
	}

	private int equivalentRectangle(Particle particle, int index)
	{
		int score = 0;
		Point upperLeft = new Point();
		Point lowerLeft = new Point();
		Point upperRight = new Point();
		Point lowerRight = new Point();
		double ulrecord = 0;// Absurd numbers ez to beat
		double llrecord = 0;
		double urrecord = 0;
		double lrrecord = 0;
		Point center = COMasses[index];
		double distance;
		// Scan Quadrant I
		for (int i = (int) (particle.getWidth() / 2.0); i < particle.getWidth(); i++)
		{
			for (int j = 0; j < particle.getHeight() / 2.0; j++)
			{
				if (particle.getLocalValue(i, j))
				{
					distance = distance(new Point(i, j), center);
					if (distance > urrecord)
					{
						urrecord = distance;
						upperRight = new Point(i, j);
					}
				}
			}
		}
		// Scan Quadrant II
		for (int x = 0; x < particle.getWidth() / 2.0; x++)
		{
			for (int y = 0; y < particle.getHeight() / 2.0; y++)
			{
				if (particle.getLocalValue(x, y))
				{
					distance = distance(new Point(x, y), center);
					if (distance > ulrecord)
					{
						ulrecord = distance;
						upperLeft = new Point(x, y);
					}
				}
			}
		}
		// Scan Quadrant III
		for (int x = 0; x < particle.getWidth() / 2.0; x++)
		{
			for (int y = (int) (particle.getHeight() / 2.0); y < particle
					.getHeight(); y++)
			{
				if (particle.getLocalValue(x, y))
				{
					distance = distance(new Point(x, y), center);
					if (distance > llrecord)
					{
						llrecord = distance;
						lowerLeft = new Point(x, y);
					}
				}
			}
		}
		// Scan Quadrant IV
		for (int x = particle.getWidth() / 2; x < particle.getWidth(); x++)
		{
			for (int y = particle.getHeight() / 2; y < particle.getHeight(); y++)
			{
				if (particle.getLocalValue(x, y))
				{
					distance = distance(new Point(x, y), center);
					if (distance > lrrecord)
					{
						lrrecord = distance;
						lowerRight = new Point(x, y);
					}
				}
			}
		}
		// Finds the most Invalid point
		short angleDiff = 0;
		double furthestDistance = distance(
				new Point(particle.getWidth() - 1, 0), upperRight);
		;
		// Finds furthest point from corner
		distance = distance(new Point(0, 0), upperLeft);
		if (distance > furthestDistance)
		{
			angleDiff = 1;
			furthestDistance = distance;
		}
		distance = distance(new Point(0, particle.getHeight() - 1), lowerLeft);
		if (distance > furthestDistance)
		{
			angleDiff = 2;
			furthestDistance = distance;
		}
		distance = distance(
				new Point(particle.getWidth() - 1, particle.getHeight() - 1),
				lowerRight);
		if (distance > furthestDistance)
		{
			angleDiff = 3;
		}
		double width = 0;
		double height = 0;
		Point location;
		Point bottom;
		double adjacent;
		switch (angleDiff) {
			case 0:
				width = distance(lowerLeft, lowerRight);
				height = distance(lowerLeft, upperLeft);
				bottom = new Point(
						(int) ((lowerLeft.getX() + lowerRight.getX()) / 2.0),
						(int) ((lowerLeft.getY() + lowerRight.getY()) / 2.0));
				location = new Point(
						(int) ((upperLeft.getX() - lowerLeft.getX()) + bottom
								.getX()),
						(int) (bottom.getY() + upperLeft.getY() - lowerLeft
								.getY()));
				targetLocations[index] = location;
				/*
				 * location=COMasses[index]; trueCenter=new
				 * Point((int)(Math.abs(upperLeft
				 * .getX()+lowerRight.getX())/2.0),(int
				 * )(Math.abs(upperLeft.getY()+lowerRight.getY())/2.0));
				 * angles[index
				 * ]=Math.atan(((location.getX()-trueCenter.getX())/(trueCenter
				 * .getY()-location.getY())));
				 */
				adjacent = lowerRight.getX() - lowerLeft.getX();
				if (adjacent == 0)
				{
					adjacent = 0.000000001;
				}
				angles[index] = Math
						.atan((lowerRight.getY() - lowerLeft.getY())
								/ (adjacent));
				break;
			case 1:
				width = distance(lowerLeft, lowerRight);
				height = distance(lowerRight, upperRight);
				bottom = new Point(
						(int) ((lowerLeft.getX() + lowerRight.getX()) / 2.0),
						(int) ((lowerLeft.getY() + lowerRight.getY()) / 2.0));
				location = new Point(
						(int) ((upperRight.getX() - lowerRight.getX()) + bottom
								.getX()),
						(int) (bottom.getY() + upperRight.getY() - lowerRight
								.getY()));
				targetLocations[index] = location;
				/*
				 * location=COMasses[index]; trueCenter=new
				 * Point((int)(Math.abs(upperRight
				 * .getX()+lowerLeft.getX())/2.0),(int
				 * )(Math.abs(upperRight.getY()+lowerLeft.getY())/2.0));
				 * angles[index
				 * ]=Math.atan(((location.getX()-trueCenter.getX())/(trueCenter
				 * .getY()-location.getY())));
				 */
				adjacent = lowerRight.getX() - lowerLeft.getX();
				if (adjacent == 0)
				{
					adjacent = 0.000000001;
				}
				angles[index] = Math
						.atan((lowerRight.getY() - lowerLeft.getY())
								/ (adjacent));
				break;
			case 2:
				width = distance(upperLeft, upperRight);
				height = distance(upperRight, lowerRight);
				location = new Point(
						(int) ((upperLeft.getX() + upperRight.getX()) / 2.0),
						(int) ((upperLeft.getY() + upperRight.getY()) / 2.0));
				targetLocations[index] = location;
				/*
				 * location=COMasses[index]; trueCenter=new
				 * Point((int)(Math.abs(upperLeft
				 * .getX()+lowerRight.getX())/2.0),(int
				 * )(Math.abs(upperLeft.getY()+lowerRight.getY())/2.0));
				 * angles[index
				 * ]=Math.atan(((location.getX()-trueCenter.getX())/(trueCenter
				 * .getY()-location.getY())));
				 */
				adjacent = upperRight.getX() - upperLeft.getX();
				if (adjacent == 0)
				{
					adjacent = 0.000000001;
				}
				angles[index] = Math
						.atan((upperRight.getY() - upperLeft.getY())
								/ (adjacent));
				break;
			case 3:
				width = distance(upperLeft, upperRight);
				height = distance(lowerLeft, upperLeft);
				location = new Point(
						(int) ((upperLeft.getX() + upperRight.getX()) / 2.0),
						(int) ((upperLeft.getY() + upperRight.getY()) / 2.0));
				targetLocations[index] = location;
				/*
				 * location=COMasses[index]; trueCenter=new
				 * Point((int)(Math.abs(upperRight
				 * .getX()+lowerLeft.getX())/2.0),(int
				 * )(Math.abs(upperRight.getY()+lowerLeft.getY())/2.0));
				 * angles[index
				 * ]=Math.atan(((location.getX()-trueCenter.getX())/(trueCenter
				 * .getY()-location.getY())));
				 */
				adjacent = upperRight.getX() - upperLeft.getX();
				if (adjacent == 0)
				{
					adjacent = 0.000000001;
				}
				angles[index] = Math
						.atan((upperRight.getY() - upperLeft.getY())
								/ (adjacent));
				break;
		}
		tWidth[index] = (int) width;
		tHeight[index] = (int) height;
		double ratio = (width * 1.0) / (height * 1.0) * 1.0;
		double ideal = 1.6;
		score = (int) (100.0 * (Math.abs(ratio - ideal) / (0.9)));
		if (score > 100)
		{
			score = 100;
		}
		return score;
	}

	private double distance(Point p, Point p2)
	{
		return Math.sqrt(Math.pow(p.x - p2.x, 2) + Math.pow(p.y - p2.y, 2));
	}

	private int xyprofile(Particle particle)
	{
		int score = 200;
		double xHun = particle.getWidth() / 100.0;
		double yHun = particle.getHeight() / 100.0;
		double[] xProfile = new double[100];
		double[] yProfile = new double[100];
		int cellCount = 0;
		for (int x = 0; x < 100; x++)
		{
			for (int i = 0; i < Math.ceil(xHun); i++)
			{
				for (int j = 0; j < particle.getHeight(); j++)
				{
					if (particle.getLocalValue((int) ((x * xHun) + i), j))
					{
						xProfile[x]++;
					}
					cellCount++;
				}
			}
			xProfile[x] = xProfile[x] / (cellCount * 1.0);
			cellCount = 0;
		}
		for (int y = 0; y < 100; y++)
		{
			for (int j = 0; j < Math.ceil(yHun); j++)
			{
				for (int i = 0; i < particle.getWidth(); i++)
				{
					if (particle.getLocalValue(i, (int) ((y * yHun) + j)))
					{
						yProfile[y]++;
					}
					cellCount++;
				}
			}
			yProfile[y] = yProfile[y] / (cellCount * 1.0);
			cellCount = 0;
		}
		for (int i = 0; i < 100; i++)
		{
			if (xProfile[i] < profileMaxX[i] && xProfile[i] > profileMinX[i])
			{
				score--;
			}
			if (yProfile[i] < profileMaxY[i] && yProfile[i] > profileMinY[i])
			{
				score--;
			}
		}
		return score;
	}

	/*
	 * private int moment(Particle particle, int location) { int score=0; Point
	 * centerMass=COMasses[location]; double moment=0; for(int
	 * y=0;y<particle.getHeight();y++) { for(int x=0;x<particle.getWidth();x++)
	 * { if(particle.getLocalValue(x,y)) { moment=moment+(distance(new
	 * Point(x,y), centerMass)); } } } double mass=80.0/(particle.count*1.0);
	 * moment=moment*mass; double ratio=moment/(particle.count*1.0); double
	 * ideal=2.092030503536954;
	 * score=(int)(100.0*(Math.abs(ratio-ideal)/(ideal))); if(score>100) {
	 * score=100; } return score; }
	 */
	private Point centerMass(Particle particle, int location)
	{
		int xTotal = 0;
		int yTotal = 0;
		for (int y = 0; y < particle.getHeight(); y++)
		{
			for (int x = 0; x < particle.getWidth(); x++)
			{
				if (particle.getLocalValue(x, y))
				{
					xTotal = xTotal + x;
					yTotal = yTotal + y;
				}
			}
		}
		Point toReturn = new Point((int) (xTotal / (particle.count * 1.0)),
				(int) (yTotal / (particle.count * 1.0)));
		COMasses[location] = toReturn;
		return toReturn;
	}

	private int coverageArea(Particle particle)
	{
		int toReturn=0;
		double ratio=(particle.count*1.0)/((particle.getWidth()*particle.getHeight())*1.0);//Random 1.0's to ensure quotient is a double
		toReturn=(int)(100.0*(Math.abs(ratio-ideal)/(dev)));
		if(toReturn>100)
		{
			toReturn=100;
		}
		return toReturn;
	}

	private ArrayList<Particle> findParticles(boolean[][] map)// Generates rectangles for every point
	{
		ArrayList<Particle> toReturn = new ArrayList<Particle>();
		ArrayList<Particle> smallParticles = new ArrayList<Particle>();
		int iStart = 0, jStart = 0, iMax = 0, jMax = 0;
		iMax = map[0].length;
		jMax = map.length;
		for (int i = iStart; i < iMax; i++)
		{
			for (int j = jStart; j < jMax; j++)
			{
				if (map[j][i])
				{
					boolean Continue = true;// Messy code to check if point was
											// in rectangle
					for (Particle particle : toReturn)
					{
						if (particle.globalInMap(i, j))
						{
							if (particle.getGlobalValue(i, j))// Don't create a
																// new rectangle
																// if it already
																// is in one
							{
								Continue = false;
								break;
							}
						}
					}
					for (Particle particle : smallParticles)
					{
						if (particle.globalInMap(i, j))
						{
							if (particle.getGlobalValue(i, j))// Don't create a
																// new rectangle
																// if it already
																// is in one
							{
								Continue = false;
								break;
							}
						}
					}
					if (Continue)// Generates new rectangle
					{
						Particle particle = new Particle(i, j,
								new boolean[1][1]);
						particle.map[0][0] = true;
						boolean change = true;
						Particle expansion = new Particle(
								(int) (particle.getX()),
								(int) (particle.getY()), new boolean[1][1]);
						if (particle.getX() > 0)
						{
							expansion.expandLeft();
							expansion.setGlobalValue(
									(int) (particle.getX() - 1),
									(int) (particle.getY()), true);
						}
						if (particle.getY() > 0)
						{
							expansion.expandUp();
							expansion.setGlobalValue((int) (particle.getX()),
									(int) (particle.getY() - 1), true);
						}
						if (particle.getX() < map[0].length - 1)
						{
							expansion.expandRight();
							expansion.setGlobalValue(
									(int) (particle.getX() + 1),
									(int) (particle.getY()), true);
						}
						if (particle.getY() < map.length - 1)
						{
							expansion.expandDown();
							expansion.setGlobalValue((int) (particle.getX()),
									(int) (particle.getY() + 1), true);
						}
						int x;
						int y;
						while (change)
						{
							Particle next = new Particle(
									(int) (expansion.getX()),
									(int) (expansion.getY()),
									new boolean[expansion.map.length][expansion.map[0].length]);
							change = false;
							for (int k = 0; k < expansion.getWidth(); k++)
							{
								for (int l = 0; l < expansion.getHeight(); l++)
								{
									// Compare to picture map values to
									// determine expansion
									if (expansion.getLocalValue(k, l))
									{
										x = (int) (k + expansion.getX());
										y = (int) (l + expansion.getY());
										if (map[y][x])
										{
											change = true;
											// Expand the particle into that
											// square
											// Determines if size increase of
											// particle required
											if (x - particle.getX() < 0)
											{
												particle.expandLeft();
											}
											if (x - particle.getX() >= particle
													.getWidth())
											{
												particle.expandRight();
											}
											if (y - particle.getY() < 0)
											{
												particle.expandUp();
											}
											if (y - particle.getY() >= particle
													.getHeight())
											{
												particle.expandDown();
											}
											// Sets particle value
											particle.count++;
											particle.setGlobalValue(x, y, true);
											// Prepares expansion for next cycle
											// Make surrounding position of new
											// particle true
											// Top Side
											// Check if space in global map
											if (y > 0)
											{
												// Check if expansion neccessary
												while (y - 1 - next.getY() < 0)
												{
													next.expandUp();
												}
												next.setGlobalValue(x, y - 1,
														true);
											}
											// Left side
											if (x > 0)
											{
												// Check if expansion neccessary
												while (x - 1 - next.getX() < 0)
												{
													next.expandLeft();
												}
												next.setGlobalValue(x - 1, y,
														true);
											}
											// Bottom side
											if (y + 1 < map.length)
											{
												// Check if expansion neccessary
												while (y + 1 - next.getY() >= next
														.getHeight())
												{
													next.expandDown();
												}
												next.setGlobalValue(x, y + 1,
														true);
											}
											// Right Side
											if (x + 1 < map[0].length)
											{
												// Check if expansion neccessary
												while (x + 1 - next.getX() >= next
														.getWidth())
												{
													next.expandRight();
												}
												next.setGlobalValue(x + 1, y,
														true);
											}
										}
									}
								}
							}
							for (int k = 0; k < particle.getWidth(); k++)
							{
								for (int l = 0; l < particle.getHeight(); l++)
								{
									if (particle.getLocalValue(k, l))
									{
										x = (int) (k + particle.getX());
										y = (int) (l + particle.getY());
										if (x - next.getX() >= 0
												&& x - next.getX() < next
														.getWidth())
										{
											if (y - next.getY() >= 0
													&& y - next.getY() < next
															.getHeight())
											{
												next.setLocalValue(
														(int) (x - next.getX()),
														(int) (y - next.getY()),
														false);
											}
										}
									}
								}
							}
							if (change)
							{
								next.shorten();
								expansion = next;
							}
						}
						if (particle.count >= minimumAlive)
						{
							//Special ratio check
							if(particle.getHeight()/particle.getWidth()<maxRatio)
							{
								if(particle.getWidth()/particle.getHeight()<maxRatio)
								{
									if(particle.count>largestCount)
									{
										largestCount=particle.count;
									}
									toReturn.add(particle);
								}
							}
						}
					}
				}
			}
		}
		System.out.println("Small Particles Size: " + smallParticles.size());
		if(largestCount>largeMinAlive)
		{
			ArrayList<Particle> toRemove=new ArrayList<Particle>();
			for(Particle particle: toReturn)
			{
				if(particle.count<largeMinAlive)
				{
					toRemove.add(particle);
				}
			}
			for(Particle particle: toRemove)
			{
				toReturn.remove(particle);
			}
		}
		return toReturn;
	}

	private int[][][] getArray(BufferedImage image)
	{

		final byte[] pixels = ((DataBufferByte) image.getRaster()
				.getDataBuffer()).getData();
		final int width = image.getWidth();
		final int height = image.getHeight();
		final boolean hasAlphaChannel = image.getAlphaRaster() != null;

		int[][][] result = new int[height][width][4];
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
				result[row][col][0] = (int) pixels[pixel + 3] & 0xff;// red
				result[row][col][1] = (int) pixels[pixel + 2] & 0xff;// green
				result[row][col][2] = (int) pixels[pixel + 1] & 0xff;// blue
				result[row][col][3] = (int) (pixels[pixel]) & 0xff;// alpha
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
				result[row][col][0] = (int) pixels[pixel + 2] & 0xff;// red
				result[row][col][1] = (int) pixels[pixel + 1] & 0xff;// green
				result[row][col][2] = (int) pixels[pixel] & 0xff;// blue
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

	public boolean inMap(boolean[][] map, int x, int y)
	{
		if (x < 0 || y < 0)
		{
			return false;
		}
		if (y < map.length && x < map[0].length)
		{
			return true;
		} else
		{
			return false;
		}
	}

	public boolean[][] createMap(BufferedImage picture)// Because x & y are
														// irrelevant here, do
														// not bother changing i
														// & j places in map
														// array
	{
		int[][][] image = getArray(picture);
		boolean[][] map = new boolean[image.length][image[0].length];
		map = useHsl(map, image, hmin, hmax, smin, lmin, lmax);
		// LightHSL is experimental idea, more lenient towards pixels surrounded
		// by alive cells
		// map=lightHsl(map,image, hmin, hmax, smin, lmin, lmax);
		return map;
	}

	private boolean[][] useHsl(boolean[][] map, int[][][] image, int hmin,
			int hmax, int smin, int lmin, int lmax)
	{
		for (int i = 0; i < image.length; i++)// Converts array into map of
												// reflected light, based on
												// color threshold
		{
			for (int j = 0; j < image[0].length; j++)
			{
				boolean valid = false;
				// Place analysis per pixel here
				int red = image[i][j][0];
				int green = image[i][j][1];
				int blue = image[i][j][2];
				// Option 1: Basic Color Scan, compares value of determined
				// color to predetermined threshold
				/*
				 * if(image[i][j][1]>=colorThreshold&&image[i][j][2]>=colorThreshold
				 * &&
				 * image[i][j][0]<=colorThreshold&&(image[i][j][2]+image[i][j][
				 * 1])/2.0-image[i][j][0]>colorDifference) { valid=true; }
				 */// Option 2: Replicates method from grip tutorial.
				int[] hsl = getHSL(red, green, blue);
				// Using values from GRIP tutorial, adjust values as necessary
				// at top of class
				if (hsl[0] >= hmin && hsl[0] <= hmax)// Hue. Using nested if
														// statements for
														// clarity of reading.
														// Don't change it.
				{
					if (hsl[1] >= smin)// Saturation. Notice that there is no
										// max value detection as we are only
										// reading for min. Change as needed.
					{
						if (hsl[2] >= lmin && hsl[2] <= lmax)// Luminance. All
																// the other
																// ones have
																// long comments
																// so I'm typing
																// here too
						{
							valid = true;
						}
					}
				}
				// Not actually an option. Just code that everyone uses.
				map[i][j] = valid;
			}
		}
		return map;
	}
	//private boolean[][] lightHsl(boolean[][] map, int[][][] image, int hmin,int hmax,int smin,int lmin,int lmax){for(int i=0;i<image.length;i++){for(int j=0;j<image[0].length;j++){boolean adjacent=false;if(j>0){if(map[i][j-1]){adjacent=true;}}if(i>0){if(map[i-1][j]){adjacent=true;}}if(j+1<image[0].length){if(map[i][j+1]){adjacent=true;}}if(i+1<image.length){if(map[i+1][j]){adjacent=true;}}if(adjacent){boolean valid=false;int red=image[i][j][0];int green=image[i][j][1];int blue = image[i][j][2];int[] hsl=getHSL(red, green, blue);if(hsl[0]>=hmin&&hsl[0]<=hmax){if(hsl[1]>=smin){if(hsl[2]>=lmin&&hsl[2]<=lmax){valid=true;}}}map[i][j]=valid;}}}return map;}
	public int[] getHSL(int red, int green, int blue)
	{
		int[] hsl=new int[3];//Self explanatory, array with elements in order of hue, saturation, and luminance
		//ranges noted below of hsl, inclusively
		double r=(red*1.0)/255.0;
		double g=(green*1.0)/255.0;
		double b=(blue*1.0)/255.0;
		double min=Math.min(r, g);
		min=Math.min(min, b);//Because math only compares 1 number at a time, repeated operation for blue as well
		double max=Math.max(r, g);
		max=Math.max(max, b);
		hsl[2]=(int) (((1.0*(min+max))/2.0)*255);//Luminance Calculation 0-255
		if(min==max)//Certain circumstance for saturation
		{
			hsl[1]=0;
			hsl[0]=0;//Hue as well to avoid dividing by zero;
		}
		else
		{
			//Magical saturation equation, don't ask questions unless I'm wrong, which i'm not, cause
			//http://www.niwa.nu/2013/05/math-behind-colorspace-conversions-rgb-hsl/ says so
			if(hsl[2]<128)
			{
				hsl[1]=(int) ((((max-min)*1.0)/((max+min)*1.0))*255.0);
			}
			else
			{
				hsl[1]=(int) (((max-min)/(2.0-max-min))*255.0);
			}
			//Finally, the hue calculation
			if(r==max)
			{
				hsl[0]=(int) (((g-b)/(max-min))*30.0);
			}
			else
			{
				if(g==max)
				{
					hsl[0]=(int) ((2.0+((b-r)/(max-min)))*30.0);
				}
				else
				{
					//b is max
					hsl[0]=(int) ((4.0+((r-g)/(max-min)))*30.0);
				}
			}
		}
		return hsl;
	}
}