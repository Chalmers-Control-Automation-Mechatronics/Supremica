
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

import org.supremica.properties.SupremicaProperties;
import org.supremica.automata.IO.FileFormats;
import javax.swing.filechooser.*;
import javax.swing.*;

public class FileDialogs
{
	private JFileChooser fileImporter = null;
	private JFileChooser fileExporter = null;
	private JFileChooser fileSaveAs = null;
	private FileFilter rcpFilter = null;
	private FileFilter xmlFilter = null;
	private FileFilter spFilter = null;
	private FileFilter vprjFilter = null;
	private FileFilter vmodFilter = null;
	private FileFilter dgrfFilter = null;
	private FileFilter dsxFilter = null;
	private FileFilter dotFilter = null;
	private FileFilter epsFilter = null;
	private FileFilter pngFilter = null;
	private FileFilter svgFilter = null;
	private FileFilter gifFilter = null;
	private FileFilter mifFilter = null;
	private FileFilter sFilter = null;
	private FileFilter prjFilter = null;
	private FileFilter autFilter = null;
	private FileFilter stFilter = null;
	private FileFilter ilFilter = null;
	private FileFilter nqcFilter = null;
	private FileFilter stnFilter = null;
	private static FileDialogs fd = new FileDialogs();

	private FileDialogs() {}

	public static JFileChooser getSaveAsFileChooser(FileFormats fileType)
	{
		JFileChooser fileSaveAs = fd.getFileSaveAs();

		fileSaveAs.resetChoosableFileFilters();
		fileSaveAs.setFileFilter(fd.getFilter(fileType));

		return fileSaveAs;
	}

	public static JFileChooser getExportFileChooser(FileFormats fileType)
	{
		JFileChooser fileExport = fd.getFileExporter();

		fileExport.resetChoosableFileFilters();
		fileExport.setFileFilter(fd.getFilter(fileType));

		return fileExport;
	}

	public static JFileChooser getImportFileChooser(FileFormats fileType)
	{
		JFileChooser fileImport = fd.getFileImporter();

		fileImport.resetChoosableFileFilters();
		fileImport.setFileFilter(fd.getFilter(fileType));

		return fileImport;
	}

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

	public static JFileChooser getSPFileSaveAs()
	{
		JFileChooser fileSaveAs = fd.getFileSaveAs();

		fileSaveAs.resetChoosableFileFilters();
		fileSaveAs.setFileFilter(fd.getSPFilter());

		return fileSaveAs;
	}

	public static JFileChooser getSPFileImporter()
	{
		JFileChooser fileImporter = fd.getFileImporter();

		fileImporter.resetChoosableFileFilters();
		fileImporter.setFileFilter(fd.getSPFilter());

		return fileImporter;
	}

	public static JFileChooser getSPFileExporter()
	{
		JFileChooser fileExporter = fd.getFileExporter();

		fileExporter.resetChoosableFileFilters();
		fileExporter.setFileFilter(fd.getSPFilter());

		return fileExporter;
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

	public static JFileChooser getRCPFileExporter()
	{
		JFileChooser fileExporter = fd.getFileExporter();

		fileExporter.resetChoosableFileFilters();
		fileExporter.setFileFilter(fd.getRCPFilter());

		return fileExporter;
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

	public static JFileChooser getPNGFileExporter()
	{
		JFileChooser fileExporter = fd.getFileExporter();

		fileExporter.resetChoosableFileFilters();
		fileExporter.setFileFilter(fd.getPNGFilter());

		return fileExporter;
	}

	public static JFileChooser getSVGFileExporter()
	{
		JFileChooser fileExporter = fd.getFileExporter();

		fileExporter.resetChoosableFileFilters();
		fileExporter.setFileFilter(fd.getSVGFilter());

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

	public static JFileChooser getPRJFileExporter()
	{
		JFileChooser fileExporter = fd.getFileExporter();

		fileExporter.resetChoosableFileFilters();
		fileExporter.setFileFilter(fd.getPRJFilter());

		return fileExporter;
	}

	public static JFileChooser getSTFileExporter()
	{
		JFileChooser fileExporter = fd.getFileExporter();

		fileExporter.resetChoosableFileFilters();
		fileExporter.setFileFilter(fd.getSTFilter());

		return fileExporter;
	}

	public static JFileChooser getILFileExporter()
	{
		JFileChooser fileExporter = fd.getFileExporter();

		fileExporter.resetChoosableFileFilters();
		fileExporter.setFileFilter(fd.getILFilter());

		return fileExporter;
	}

	public static JFileChooser getNQCFileExporter()
	{
		JFileChooser fileExporter = fd.getFileExporter();

		fileExporter.resetChoosableFileFilters();
		fileExporter.setFileFilter(fd.getNQCFilter());

		return fileExporter;
	}

	public static JFileChooser getAutFileImporter()
	{
		JFileChooser autFileImporter = fd.getFileImporter();

		autFileImporter.resetChoosableFileFilters();
		autFileImporter.setFileFilter(fd.getAutFilter());

		return autFileImporter;
	}

	public static JFileChooser getRobotStudioStationFileImporter()
	{
		JFileChooser stationFileImporter = fd.getFileImporter();

		stationFileImporter.resetChoosableFileFilters();
		stationFileImporter.setFileFilter(fd.getSTNFilter());

		return stationFileImporter;
	}

	public static JFileChooser getRobotCellFileImporter()
	{
		JFileChooser cellFileImporter = fd.getFileImporter();

		cellFileImporter.resetChoosableFileFilters();
		cellFileImporter.setFileFilter(fd.getSTNFilter());    // RobotStudio

		return cellFileImporter;
	}

	private JFileChooser getFileImporter()
	{
		if (fileImporter == null)
		{
			fileImporter = new JFileChooser();

			fileImporter.setDialogType(JFileChooser.OPEN_DIALOG);
			fileImporter.setCurrentDirectory(new java.io.File(SupremicaProperties.getFileOpenPath()));
			fileImporter.setMultiSelectionEnabled(true);
		}

		return fileImporter;
	}

	private JFileChooser getFileExporter()
	{
		if (fileExporter == null)
		{
			fileExporter = new StandardExtensionFileChooser();

			fileExporter.setDialogType(JFileChooser.SAVE_DIALOG);
			fileExporter.setCurrentDirectory(new java.io.File(SupremicaProperties.getFileSavePath()));
			fileExporter.setMultiSelectionEnabled(false);
		}

		return fileExporter;
	}

	private JFileChooser getFileSaveAs()
	{
		if (fileSaveAs == null)
		{
			fileSaveAs = new StandardExtensionFileChooser();

			fileSaveAs.setDialogType(JFileChooser.SAVE_DIALOG);
			fileSaveAs.setCurrentDirectory(new java.io.File(SupremicaProperties.getFileSavePath()));
			fileSaveAs.setMultiSelectionEnabled(false);
		}

		return fileSaveAs;
	}

	// all of the below look exactly the same - this FileFilter derivative generalizes it all
	// Note, could not have variable referred to in anonymous class
	private FileFilter makeFileFilter(final String ext, final String description)
	{
		return new StandardExtensionFileFilter(ext, description);
	}

	private FileFilter getFilter(FileFormats fileType)
	{
		return makeFileFilter(fileType.getExtension(), fileType.getDescription());
	}

	private FileFilter getRCPFilter()
	{
		if (rcpFilter == null)
		{
			rcpFilter = makeFileFilter(".rcp", "RCP files (*.rcp)");
		}

		return rcpFilter;
	}

	private FileFilter getXMLFilter()
	{
		if (xmlFilter == null)
		{
			xmlFilter = makeFileFilter(".xml", "XML files (*.xml)");
		}

		return xmlFilter;
	}

	private FileFilter getSPFilter()
	{
		if (spFilter == null)
		{
			spFilter = makeFileFilter(".sp", "Supremica Project files (*.sp)");
		}

		return spFilter;
	}

	private FileFilter getVPRJFilter()
	{
		if (vprjFilter == null)
		{
			vprjFilter = makeFileFilter(".vprj", "Valid Project files (*.vprj)");
		}

		return vprjFilter;
	}

	private FileFilter getVMODFilter()
	{
		if (vmodFilter == null)
		{
			vmodFilter = makeFileFilter(".vmod", "Valid Module files (*.vmod)");
		}

		return vmodFilter;
	}

	private FileFilter getDGRFFilter()
	{
		if (dgrfFilter == null)
		{
			dgrfFilter = makeFileFilter(".dgrf", "Valid Graph files (*.dgrf)");
		}

		return dgrfFilter;
	}

	private FileFilter getDSXFilter()
	{
		if (dsxFilter == null)
		{
			dsxFilter = makeFileFilter(".dsx", "Desco files (*.dsx)");
		}

		return dsxFilter;
	}

	private FileFilter getDOTFilter()
	{
		if (dotFilter == null)
		{
			dotFilter = makeFileFilter(".dot", "Graphviz files (*.dot)");
		}

		return dotFilter;
	}

	private FileFilter getEPSFilter()
	{
		if (epsFilter == null)
		{
			epsFilter = makeFileFilter(".eps", "Encapsulated Postscript (*.eps)");
		}

		return epsFilter;
	}

	private FileFilter getGIFFilter()
	{
		if (gifFilter == null)
		{
			gifFilter = makeFileFilter(".gif", "GIF files (*.gif)");
		}

		return gifFilter;
	}

	private FileFilter getMIFFilter()
	{
		if (mifFilter == null)
		{
			mifFilter = makeFileFilter(".mif", "MIF files (*.mif)");
		}

		return mifFilter;
	}

	private FileFilter getPNGFilter()
	{
		if (pngFilter == null)
		{
			pngFilter = makeFileFilter(".png", "PNG files (*.png)");
		}

		return pngFilter;
	}

	private FileFilter getSVGFilter()
	{
		if (svgFilter == null)
		{
			svgFilter = makeFileFilter(".svg", "SVG files (*.svg)");
		}

		return svgFilter;
	}

	private FileFilter getAutFilter()
	{
		if (autFilter == null)
		{
			autFilter = makeFileFilter(".aut", "Aldebaran files (*.aut)");
		}

		return autFilter;
	}

	private FileFilter getSFilter()
	{
		if (sFilter == null)
		{
			sFilter = makeFileFilter(".s", "SattLine files (*.s)");
		}

		return sFilter;
	}

	private FileFilter getPRJFilter()
	{
		if (prjFilter == null)
		{
			prjFilter = makeFileFilter(".prj", "Control Builder Project files (*.prj)");
		}

		return prjFilter;
	}

	private FileFilter getSTFilter()
	{
		if (stFilter == null)
		{
			stFilter = makeFileFilter(".st", "IEC-1131 Structured Text files (*.st)");
		}

		return stFilter;
	}

	private FileFilter getILFilter()
	{
		if (ilFilter == null)
		{
			ilFilter = makeFileFilter(".il", "IEC-1131 Instruction List files (*.il)");
		}

		return ilFilter;
	}

	private FileFilter getNQCFilter()
	{
		if (nqcFilter == null)
		{
			nqcFilter = makeFileFilter(".nqc", "Mindstorm NQC files (*.nqc)");
		}

		return nqcFilter;
	}

	private FileFilter getSTNFilter()
	{
		if (stnFilter == null)
		{
			stnFilter = makeFileFilter(".stn", "RobotStudio Station file (*.stn)");
		}

		return stnFilter;
	}
}
