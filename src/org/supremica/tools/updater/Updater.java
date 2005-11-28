
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
 * Knarrhogsgatan 10
 * SE-431 60 MOLNDAL
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

public class Updater
{
	public InputStream getRemoteFile(String url)
		throws Exception
	{
		URL theUrl = new URL(url);
		HttpURLConnection httpConnection;

		httpConnection = (HttpURLConnection) theUrl.openConnection();

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

	// E.g. http://www.s2.chalmers.se/%7Eka/Supremica/updates/noarch/SupremicaVersion.txt
	public String getLatestVersion(String url)
	{
		InputStream remoteSupremicaVersionStream;

		try
		{
			remoteSupremicaVersionStream = getRemoteFile(url);
		}
		catch (Exception e)
		{
			System.err.println("Error downloading: " + url + ". Update check aborted.");

			return null;
		}

		Properties remoteProperties = new Properties();

		try
		{
			remoteProperties.load(remoteSupremicaVersionStream);
		}
		catch (IOException e)
		{
			System.err.println("Error finding new version information.");

			return null;
		}

		return remoteProperties.getProperty("SupremicaVersion");
	}

	public void writeLocalFile(InputStream iStream, File outFile)
		throws Exception
	{
		try
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
					System.out.print(".");
					outStream.write(buffer, offset, nbrOfReadBytes);
				}
			}
			while (nbrOfReadBytes >= 0);

			outStream.flush();
			outStream.close();
		}
		catch (Exception e)
		{
			System.err.println(e);
			e.printStackTrace();
		}
	}

/*
		private String host = null;
		private Properties localProperties = null;
		private Properties remoteProperties = null;

		private static final String SupremicaFilename = "Supremica.jar";
		private static final String SupremicaVersionFilename = "SupremicaVersion.cfg";

	public final static int ONE_SECOND = 1000;

	private ProgressMonitor progressMonitor;
	private Timer timer;
	private JButton startButton;
	private LongTask task;
	private JTextArea taskOutput;
	private String newline = "\n";

	public Updater(String host) {
		super("Supremica Updater");
		this.host = host;
		task = new LongTask();

		//Create the demo's UI.
		startButton = new JButton("Update");
		startButton.setActionCommand("start");
		startButton.addActionListener(new ButtonListener());

		taskOutput = new JTextArea(5, 20);
		taskOutput.setMargin(new Insets(5,5,5,5));
		taskOutput.setEditable(false);

		JPanel contentPane = new JPanel();
		contentPane.setLayout(new BorderLayout());
		contentPane.add(startButton, BorderLayout.NORTH);
		contentPane.add(new JScrollPane(taskOutput), BorderLayout.CENTER);
		contentPane.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		setContentPane(contentPane);

		//Create a timer.
		timer = new Timer(ONE_SECOND, new TimerListener());
	}

	//
	// The actionPerformed method in this class
	// is called each time the Timer "goes off".
	//
	class TimerListener implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			if (progressMonitor.isCanceled() || task.done()) {
				progressMonitor.close();
				task.stop();
				Toolkit.getDefaultToolkit().beep();
				timer.stop();
				if (task.done()) {
					taskOutput.append("Task completed." + newline);
				}
				startButton.setEnabled(true);
			} else {
				progressMonitor.setNote(task.getMessage());
				progressMonitor.setProgress(task.getCurrent());
				taskOutput.append(task.getMessage() + newline);
				taskOutput.setCaretPosition(
					taskOutput.getDocument().getLength());
			}
		}
	}

	//
	// The actionPerformed method in this class
	// is called when the user presses the start button.
	//
	class ButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			progressMonitor = new ProgressMonitor(ProgressMonitorDemo.this,
									  "Downloading",
									  "", 0, task.getLengthOfTask());
			progressMonitor.setProgress(0);
			progressMonitor.setMillisToDecideToPopup(2 * ONE_SECOND);

			startButton.setEnabled(false);
			task.go();
			timer.start();
		}
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

		public boolean doUpdate()
				throws Exception
		{
				loadProperties();
				if (localProperties == null)
				{
						return true;
				}
				if (remoteProperties == null)
				{
						JOptionPane.showMessageDialog(this, "Could not find information about new Supremica versions.\nUpdate aborted.", "Information", JOptionPane.ERROR_MESSAGE);
						return false;
				}
				String localVersion = localProperties.getProperty("SupremicaVersion");
				String remoteVersion = remoteProperties.getProperty("SupremicaVersion");
				if (localVersion == null || remoteVersion == null)
				{
						return true;
				}
				if (localVersion.equalsIgnoreCase(remoteVersion))
				{
						JOptionPane.showMessageDialog(this, "Supremica is up to date.\nCurrent version is: " + localVersion, "Information", JOptionPane.INFORMATION_MESSAGE);
						return false;
				}

				int response = JOptionPane.showConfirmDialog(this, "Installed version: " + localVersion + "\nNew version: " + remoteVersion + "\n\nUpdate Supremica", "Update Supremica", JOptionPane.YES_NO_OPTION);
				if (response == JOptionPane.YES_OPTION)
				{
						return true;
				}
				return false;
		}

		public void writeLocalFile(InputStream iStream, File outFile)
				throws Exception
		{
				try
				{
				FileOutputStream outStream = new FileOutputStream(outFile);
				ProgressMonitorInputStream progressMonitorStream =
						new ProgressMonitorInputStream(this, "Reading", iStream);
				BufferedInputStream bIStream = new BufferedInputStream(progressMonitorStream);
				ProgressMonitor monitor = progressMonitorStream.getProgressMonitor();

				byte[] buffer = new byte[1024];
				int offset = 0;
				int nbrOfReadBytes = 0;
				do
				{
						nbrOfReadBytes = bIStream.read(buffer, offset, buffer.length);
						if (nbrOfReadBytes >= 0)
						{
								outStream.write(buffer, offset, nbrOfReadBytes);
						}
				}
				while (nbrOfReadBytes >= 0);
				outStream.flush();
				outStream.close();
				}
				catch (Exception e)
				{
						System.err.println(e);
						e.printStackTrace();
				}
		}

		public InputStream getRemoteFile(String fileName)
				throws Exception
		{
				URL url = new URL(host + fileName);
				HttpURLConnection httpConnection;
				httpConnection = (HttpURLConnection)url.openConnection();
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
				if (!doUpdate())
				{
						return;
				}
				File newSupremicaVersionFile = new File("tmp_" + SupremicaVersionFilename);
				File newSupremicaFile = new File("tmp_" + SupremicaFilename);
				InputStream remoteSupremicaVersionStream;
				InputStream remoteSupremicaStream;
				try
				{
						remoteSupremicaVersionStream = getRemoteFile(SupremicaVersionFilename);
				}
				catch (Exception e)
				{
						JOptionPane.showMessageDialog(this,"Error downloading: " + SupremicaVersionFilename + ".\nUpdate aborted.", "Download error", JOptionPane.ERROR_MESSAGE);
						return;
				}
				try
				{
						remoteSupremicaStream = getRemoteFile(SupremicaFilename);
				}
				catch (Exception e)
				{
						JOptionPane.showMessageDialog(this,"Error downloading: " + SupremicaFilename + ".\nUpdate aborted.", "Download error", JOptionPane.ERROR_MESSAGE);
						return;
				}
				try
				{
						writeLocalFile(remoteSupremicaVersionStream, newSupremicaVersionFile);
				}
				catch (Exception e)
				{
						JOptionPane.showMessageDialog(this,"Error writing file: " + newSupremicaVersionFile + ".\nUpdate aborted.", "File access error", JOptionPane.ERROR_MESSAGE);
						return;
				}
				try
				{
						writeLocalFile(remoteSupremicaStream, newSupremicaFile);
				}
				catch (Exception e)
				{
						JOptionPane.showMessageDialog(this,"Error writing file: " + newSupremicaFile + ".\nUpdate aborted.", "File access error", JOptionPane.ERROR_MESSAGE);
						return;
				}
				File orgSupremicaVersionFile = new File(SupremicaVersionFilename);
				File orgSupremicaFile = new File(SupremicaFilename);
				try
				{
						orgSupremicaVersionFile.delete();
				}
				catch (Exception e)
				{
						JOptionPane.showMessageDialog(this,"Error deleting file: " + SupremicaVersionFilename + ".\nUpdate aborted.", "File access error", JOptionPane.ERROR_MESSAGE);
						return;
				}
				try
				{
						orgSupremicaFile.delete();
				}
				catch (Exception e)
				{
						JOptionPane.showMessageDialog(this,"Error deleting file: " + SupremicaFilename + ".\nUpdate aborted.", "File access error", JOptionPane.ERROR_MESSAGE);
						return;
				}
				try
				{
						newSupremicaVersionFile.renameTo(orgSupremicaVersionFile);
				}
				catch (Exception e)
				{
						JOptionPane.showMessageDialog(this,"Error renaming file: " + "tmp_" + SupremicaVersionFilename + " to " + SupremicaVersionFilename + ".\nUpdate aborted.", "File access error", JOptionPane.ERROR_MESSAGE);
						return;
				}
				try
				{
						newSupremicaFile.renameTo(orgSupremicaFile);
				}
				catch (Exception e)
				{
						JOptionPane.showMessageDialog(this,"Error renaming file: " + "tmp_" + SupremicaFilename + " to " + SupremicaFilename + ".\nUpdate aborted.", "File access error", JOptionPane.ERROR_MESSAGE);
						return;
				}
		}


		public static void main(String[] args)
		{

				Updater updater = new Updater("http://www.s2.chalmers.se/~ka/supremica/");
		updater.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		updater.pack();
		updater.setVisible(true);
				try
				{
						updater.update();
				}
				catch (Exception e)
				{
						JOptionPane.showMessageDialog(null, e.getMessage(), "Error during update", JOptionPane.ERROR_MESSAGE);
				}


		}
*/
}
