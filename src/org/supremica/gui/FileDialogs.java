
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
package org.supremica.gui;

import javax.swing.filechooser.*;
import javax.swing.*;

public class FileDialogs
{
	private JFileChooser fileImporter = null;
	private JFileChooser fileExporter = null;
	private JFileChooser fileSaveAs = null;
	private FileFilter xmlFilter = null;
	private FileFilter vprjFilter = null;
	private FileFilter vmodFilter = null;
	private FileFilter dgrfFilter = null;
	private FileFilter dsxFilter = null;
	private FileFilter dotFilter = null;
	private FileFilter epsFilter = null;
	private FileFilter gifFilter = null;
	private FileFilter mifFilter = null;
	private FileFilter sFilter = null;
	private static FileDialogs fd = new FileDialogs();

	private FileDialogs() {}

	public static JFileChooser getXMLFileSaveAs()
	{
		JFileChooser fileSaveAs = fd.getFileSaveAs();

		fileSaveAs.resetChoosableFileFilters();
		fileSaveAs.setFileFilter(fd.getXMLFilter());

		return fileSaveAs;
	}

	public static JFileChooser getXMLFileImporter()
	{
		JFileChooser fileImporter = fd.getFileImporter();

		fileImporter.resetChoosableFileFilters();
		fileImporter.setFileFilter(fd.getXMLFilter());

		return fileImporter;
	}

	public static JFileChooser getVALIDFileImporter()
	{
		JFileChooser fileImporter = fd.getFileImporter();

		fileImporter.resetChoosableFileFilters();
		fileImporter.addChoosableFileFilter(fd.getDGRFFilter());
		fileImporter.addChoosableFileFilter(fd.getVMODFilter());
		fileImporter.setFileFilter(fd.getVPRJFilter());

		return fileImporter;
	}

	public static JFileChooser getXMLFileExporter()
	{
		JFileChooser fileExporter = fd.getFileExporter();

		fileExporter.resetChoosableFileFilters();
		fileExporter.setFileFilter(fd.getXMLFilter());

		return fileExporter;
	}

	public static JFileChooser getDSXFileExporter()
	{
		JFileChooser fileExporter = fd.getFileExporter();

		fileExporter.resetChoosableFileFilters();
		fileExporter.setFileFilter(fd.getDSXFilter());

		return fileExporter;
	}

	public static JFileChooser getDOTFileExporter()
	{
		JFileChooser fileExporter = fd.getFileExporter();

		fileExporter.resetChoosableFileFilters();
		fileExporter.setFileFilter(fd.getDOTFilter());

		return fileExporter;
	}

	public static JFileChooser getEPSFileExporter()
	{
		JFileChooser fileExporter = fd.getFileExporter();

		fileExporter.resetChoosableFileFilters();
		fileExporter.setFileFilter(fd.getEPSFilter());

		return fileExporter;
	}

	public static JFileChooser getMIFFileExporter()
	{
		JFileChooser fileExporter = fd.getFileExporter();

		fileExporter.resetChoosableFileFilters();
		fileExporter.setFileFilter(fd.getMIFFilter());

		return fileExporter;
	}

	public static JFileChooser getGIFFileExporter()
	{
		JFileChooser fileExporter = fd.getFileExporter();

		fileExporter.resetChoosableFileFilters();
		fileExporter.setFileFilter(fd.getGIFFilter());

		return fileExporter;
	}

	public static JFileChooser getSFileExporter()
	{
		JFileChooser fileExporter = fd.getFileExporter();

		fileExporter.resetChoosableFileFilters();
		fileExporter.setFileFilter(fd.getSFilter());

		return fileExporter;
	}

	private JFileChooser getFileImporter()
	{
		if (fileImporter == null)
		{
			fileImporter = new JFileChooser();

			fileImporter.setDialogType(JFileChooser.SAVE_DIALOG);
			fileImporter.setCurrentDirectory(new java.io.File(WorkbenchProperties.getFileOpenPath()));
			fileImporter.setMultiSelectionEnabled(true);
		}

		return fileImporter;
	}

	private JFileChooser getFileExporter()
	{
		if (fileExporter == null)
		{
			fileExporter = new JFileChooser();

			fileExporter.setDialogType(JFileChooser.OPEN_DIALOG);
			fileExporter.setCurrentDirectory(new java.io.File(WorkbenchProperties.getFileSavePath()));
			fileExporter.setMultiSelectionEnabled(false);
		}

		return fileExporter;
	}

	private JFileChooser getFileSaveAs()
	{
		if (fileSaveAs == null)
		{
			fileSaveAs = new JFileChooser();

			fileSaveAs.setDialogType(JFileChooser.OPEN_DIALOG);
			fileSaveAs.setCurrentDirectory(new java.io.File(WorkbenchProperties.getFileSavePath()));
			fileSaveAs.setMultiSelectionEnabled(false);
		}

		return fileSaveAs;
	}

	private FileFilter getXMLFilter()
	{
		if (xmlFilter == null)
		{
			xmlFilter = new FileFilter()
			{    // Anonymous class
				public boolean accept(java.io.File f)
				{
					return f.getName().toLowerCase().endsWith(".xml") || f.isDirectory();
				}

				public String getDescription()
				{
					return "XML files (*.xml)";
				}
			};
		}

		return xmlFilter;
	}

	private FileFilter getVPRJFilter()
	{
		if (vprjFilter == null)
		{
			vprjFilter = new FileFilter()
			{    // Anonymous class
				public boolean accept(java.io.File f)
				{
					return f.getName().toLowerCase().endsWith(".vprj") || f.isDirectory();
				}

				public String getDescription()
				{
					return "VPRJ files (*.vprj)";
				}
			};
		}

		return vprjFilter;
	}

	private FileFilter getVMODFilter()
	{
		if (vmodFilter == null)
		{
			vmodFilter = new FileFilter()
			{    // Anonymous class
				public boolean accept(java.io.File f)
				{
					return f.getName().toLowerCase().endsWith(".vmod") || f.isDirectory();
				}

				public String getDescription()
				{
					return "VMOD files (*.vmod)";
				}
			};
		}

		return vmodFilter;
	}

	private FileFilter getDGRFFilter()
	{
		if (dgrfFilter == null)
		{
			dgrfFilter = new FileFilter()
			{    // Anonymous class
				public boolean accept(java.io.File f)
				{
					return f.getName().toLowerCase().endsWith(".dgrf") || f.isDirectory();
				}

				public String getDescription()
				{
					return "DGRF files (*.dgrf)";
				}
			};
		}

		return dgrfFilter;
	}

	private FileFilter getDSXFilter()
	{
		if (dsxFilter == null)
		{
			dsxFilter = new FileFilter()
			{    // Anonymous class
				public boolean accept(java.io.File f)
				{
					return f.getName().toLowerCase().endsWith(".dsx") || f.isDirectory();
				}

				public String getDescription()
				{
					return "DSX files (*.dsx)";
				}
			};
		}

		return dsxFilter;
	}

	private FileFilter getDOTFilter()
	{
		if (dotFilter == null)
		{
			dotFilter = new FileFilter()
			{    // Anonymous class
				public boolean accept(java.io.File f)
				{
					return f.getName().toLowerCase().endsWith(".dot") || f.isDirectory();
				}

				public String getDescription()
				{
					return "DOT files (*.dot)";
				}
			};
		}

		return dotFilter;
	}

	private FileFilter getEPSFilter()
	{
		if (epsFilter == null)
		{
			epsFilter = new FileFilter()
			{    // Anonymous class
				public boolean accept(java.io.File f)
				{
					return f.getName().toLowerCase().endsWith(".eps") || f.isDirectory();
				}

				public String getDescription()
				{
					return "EPS files (*.eps)";
				}
			};
		}

		return epsFilter;
	}

	private FileFilter getGIFFilter()
	{
		if (gifFilter == null)
		{
			gifFilter = new FileFilter()
			{    // Anonymous class
				public boolean accept(java.io.File f)
				{
					return f.getName().toLowerCase().endsWith(".gif") || f.isDirectory();
				}

				public String getDescription()
				{
					return "GIF files (*.gif)";
				}
			};
		}

		return gifFilter;
	}

	private FileFilter getMIFFilter()
	{
		if (mifFilter == null)
		{
			mifFilter = new FileFilter()
			{    // Anonymous class
				public boolean accept(java.io.File f)
				{
					return f.getName().toLowerCase().endsWith(".mif") || f.isDirectory();
				}

				public String getDescription()
				{
					return "MIF files (*.mif)";
				}
			};
		}

		return mifFilter;
	}

	private FileFilter getSFilter()
	{
		if (sFilter == null)
		{
			sFilter = new FileFilter()
			{    // Anonymous class
				public boolean accept(java.io.File f)
				{
					return f.getName().toLowerCase().endsWith(".s") || f.isDirectory();
				}

				public String getDescription()
				{
					return "SattLine files (*.s)";
				}
			};
		}

		return sFilter;
	}
}
