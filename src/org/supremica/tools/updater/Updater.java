import java.io.*;


public class SupremicaUpdater
{
	private String host = null;
	private Properties localProperties = null;
	private Properties remoteProperties = null;

	private static final SupremicaFilename = "Supremica.jar";
	private static final SupremicaVersionFilename = "SupremicaVersion.cfg";

	public SupremicaUpdater(String host)
	{
		this.host = host;
	}

	public void loadProperties()
		throws Exception
	{
		InputStream localStream = getLocalFile(SupremicaVersionFilename);
		InputStream remoteStream = getRemoteFile(SupremicaVersionFilename);
		localProperties = new Properties();
		localProperties.load(localStream);
		remoteProperties = new Properties();
		remoteProperties.load(remoteStream);
		localStream.close();
		remoteStream.close();
	}

	public void updateNeeded()
		throws Exception
	{
		loadProperties();
		if (localProperties == null || remoteProperties == null)
		{
			return true;
		}
		String localVersion = localProperties.getProperty("SupremicaVersion");
		String remoteVersion = remoteProperties.getProperty("SupremicaVersion");
		if (localVersion == null || remoteVersion == null)
		{
			return true;
		}
		return !localVersion.equalsIgnoreCase(remoteVersion);
	}

	public void writeLocalFile(InputStream iStream, File outFile)
		throws Exception
	{
		FileOutputStream outStream = new FileOutputStream(outFile);
		BufferedInputStream bIStream = new BufferedInputStream(iStream);
		byte[] buffer = new byte[1024];
		int offset = 0;
		do
		{
			int nbrOfReadBytes = bIStream.read(buffer, offset, buffer.length);
			if (nbrOfReadBytes >= 0)
			{
				outStream.write(buffer, offset, nbrOfReadBytes);
				offset = offset + nbrOfReadBytes;
			}
		}
		while (nbrOfReadBytes >= 0)
		outStream.flush();
		outStream.close();
	}

	public InputStream getRemoteFile(String fileName)
		throws Exception
	{
		URL url = new URL(host + fileName);
		HttpURLConnection httpConnection = (HttpURLConnection)url.openConnection();
		httpConnection.connect();
		InputStream iStream = httpConnection.getInputStream();
		return iStream;
	}

	public InputStream getLocalFile(String fileName)
		throws Exception
	{
		File theFile = new File(fileName);
		FileInputStream iStream = new FileInputStream(theFile);
		BufferedInputStream bIStream = new BufferedInputStream(iStream);
		return bIStream;
	}

	public void update()
		throws Exception
	{
		if (!updateNeeded())
		{
			return;
		}
		File newSupremicaVersionFile = new File("tmp_ " + SupremicaVersionFilename);
		File newSupremicaFile = new File("tmp_" + SupremicaFilename)
		writeLocalFile(getRemoteFile(SupremicaVersionFilename), newSupremicaVersionFile);
		writeLocalFile(getRemoteFile(SupremicaFilename), newSupremicaFile);
		File orgSupremicaVersionFile = new File(SupremicaVersionFilename);
		File orgSupremicaFile = new File(SupremicaFilename);
		orgSupremicaVersionFile.delete();
		orgSupremicaFile.delete();
		newSupremicaVersionFile.renameTo(orgSupremicaVersionFile);
		newSupremicaFile.renameTo(orgSupremicaFile);
	}


	public static void main(String[] args)
	{

	}
}