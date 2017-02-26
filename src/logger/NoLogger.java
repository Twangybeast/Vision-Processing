package logger;

import java.text.SimpleDateFormat;
import java.util.Date;

public class NoLogger extends Logger
{
	public NoLogger()
	{
		super();
	}
	public void println(String s)
	{
		
	}
	public void print(String s)
	{
		
	}
	public void printf(String s, Object ...args)
	{
		
	}
	public void close()
	{
		
	}
	protected String getTimeString()
	{
		SimpleDateFormat sdfDate = new SimpleDateFormat("MM-dd HH:mm:ss.SSS");
	    Date now = new Date();
	    String strDate = sdfDate.format(now);
	    return strDate+"\t";
	}
}
