package code2017;

public class Property 
{
	double heightWidthRatio;
	double moment;
	double coverage;
	double[] xprofile;
	double[] yprofile;
	
	public Property(double hwr, double m, double c, double[] xp, double[] yp)
	{
		this.heightWidthRatio=hwr;
		this.moment=m;
		this.coverage=c;
		this.xprofile=xp;
		this.yprofile=yp;
	}
	public static Property getIdealBoilerTop()
	{
		return new Property(4.0/15.0, 0.0, 1.0, null, null);
	}
	public static Property getIdealBoilerDown()
	{
		return new Property(2.0/15.0, 0.0, 1.0, null, null);
	}
	public static Property getIdealGear()
	{
		return new Property(5.0/2.0, 0.0, 1.0, null, null);
	}
}
