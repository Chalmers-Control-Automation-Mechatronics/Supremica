
/*
 * Supremica Software License Agreement
 *
 * The Supremica software is not in the public domain
 * However, it is freely available without fee for education,
 * research, and non-profit purposes.  By obtaining copies of
 * this and other files that comprise the Supremica software,
 * you, the Licensee, agree to abide by the following
 * conditions and understandings with respect to the
 * copyrighted software:
 *
 * The software is copyrighted in the name of Supremica,
 * and ownership of the software remains with Supremica.
 *
 * Permission to use, copy, and modify this software and its
 * documentation for education, research, and non-profit
 * purposes is hereby granted to Licensee, provided that the
 * copyright notice, the original author's names and unit
 * identification, and this permission notice appear on all
 * such copies, and that no charge be made for such copies.
 * Any entity desiring permission to incorporate this software
 * into commercial products or to use it for commercial
 * purposes should contact:
 *
 * Knut Akesson (KA), knut@supremica.org
 * Supremica,
 * Haradsgatan 26A
 * 431 42 Molndal
 * SWEDEN
 *
 * to discuss license terms. No cost evaluation licenses are
 * available.
 *
 * Licensee may not use the name, logo, or any other symbol
 * of Supremica nor the names of any of its employees nor
 * any adaptation thereof in advertising or publicity
 * pertaining to the software without specific prior written
 * approval of the Supremica.
 *
 * SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 * SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 * IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 * Supremica or KA shall not be liable for any damages
 * suffered by Licensee from the use of this software.
 *
 * Supremica is owned and represented by KA.
 */
package org.supremica.tools.updater;

import java.io.*;
import java.util.*;
import java.net.*;
import javax.swing.*;

public class Updater
{
	private String host = null;
	private Properties localProperties = null;
	private Properties remoteProperties = null;

	private static final String SupremicaFilename = "Supremica.jar";
	private static final String SupremicaVersionFilename = "SupremicaVersion.cfg";

	public Updater(String host)
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

	public boolean updateNeeded()
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
		int nbrOfReadBytes = 0;
		do
		{
			nbrOfReadBytes = bIStream.read(buffer, offset, buffer.length);
			if (nbrOfReadBytes >= 0)
			{
				outStream.write(buffer, offset, nbrOfReadBytes);
				offset = offset + nbrOfReadBytes;
			}
		}
		while (nbrOfReadBytes >= 0);
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
		File newSupremicaFile = new File("tmp_" + SupremicaFilename);
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

		Updater updater = new Updater("http://www.s2.chalmers.se/~ka/supremica/");
		try
		{
			updater.update();
		}
		catch (Exception e)
		{
			JOptionPane.showMessageDialog(null, e.getMessage(), "Error during update", JOptionPane.ERROR_MESSAGE);
		}


	}
}