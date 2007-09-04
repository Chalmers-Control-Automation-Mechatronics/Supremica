package org.supremica.util;

import java.io.IOException;
import org.supremica.log.*;
import java.lang.reflect.Method;
import javax.swing.JOptionPane;

/**
* A simple, static class to display a URL in the system browser.
*
* Under Unix, the system browser is hard-coded to be 'netscape'.
* Netscape must be in your PATH for this to work.  This has been
* tested with the following platforms: AIX, HP-UX and Solaris.
*
* Under Windows, this will bring up the default browser under windows,
* usually either Netscape or Microsoft IE.  The default browser is
* determined by the OS.  This has been tested under Windows 95/98/NT.
*
* Examples:
* BrowserControl.displayURL("http://www.javaworld.com")
* BrowserControl.displayURL("file://c:\\docs\\index.html")
* BrowserContorl.displayURL("file:///user/joe/index.html");
*
* Note - you must include the url type -- either "http://" or
* "file://".
*/
public class BrowserControl
{
	private static Logger logger = LoggerFactory.createLogger(BrowserControl.class);
  	private static final String errMsg = "Error attempting to launch web browser";
	/**
	 * Display a file in the system browser.  If you want to display a
	 * file, you must include the absolute path name.
	 *
	 * @param url the file's url (the url must start with either "http://" or
	 * "file://").
	 */
	public static void displayURL(String url) 
	{
		String osName = System.getProperty("os.name");
		try
		{
			if (osName.startsWith("Mac OS")) 
			{
				Class fileMgr = Class.forName("com.apple.eio.FileManager");
				Method openURL = fileMgr.getDeclaredMethod("openURL", new Class[] {String.class});
				openURL.invoke(null, new Object[] {url});
			}
			else if (osName.startsWith("Windows"))
			{
				Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
			}
			else 
			{ //assume Unix or Linux
				String[] browsers = {
					"firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape" };
				String browser = null;
				for (int count = 0; count < browsers.length && browser == null; count++)
					if (Runtime.getRuntime().exec(new String[] {"which", browsers[count]}).waitFor() == 0)
						browser = browsers[count];
				if (browser == null)
				{
					throw new Exception("Could not find web browser");	
				}
				else
				{
					Runtime.getRuntime().exec(new String[] {browser, url});
				}
			}
		}
		catch (Exception e) 
		{
			JOptionPane.showMessageDialog(null, errMsg + ":\n" + e.getLocalizedMessage());
		}
	}


	/**
	 * Try to determine whether this application is running under Windows
	 * or some other platform by examing the "os.name" property.
	 *
	 * @return true if this application is running under a Windows OS
	 */
/*
	public static boolean isWindowsPlatform()
	{
		String os = System.getProperty("os.name");

		if ((os != null) && os.startsWith(WIN_ID))
		{
			return true;
		}
		else
		{
			return false;
		}
	}
*/
	/**
	 * Simple example.
	 */
	public static void main(String[] args)
	{
		displayURL("http://www.supremica.org");
	}

}
