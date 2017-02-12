package logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class FileLogger extends Logger 
{
	private String path;
	private PrintWriter writer=null;
	public FileLogger(String filePath) 
	{
		super();
		path=filePath;
	}
	public void init()
	{
		initialized=true;
		File file = new File(path);
		if(!file.exists())
		{
			file.mkdirs();
		}
		try
		{
			writer = new PrintWriter(file);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	public void println(String s)
	{
		checkInit();
		writer.println(getTimeString() + s);
	}
	public void print(String s)
	{
		checkInit();
		writer.print(getTimeString() + s);
	}
	public void printf(String s, Object ...args)
	{
		checkInit();
		writer.printf(getTimeString()+s, args);
	}
	public void close()
	{
		writer.close();
	}
}
