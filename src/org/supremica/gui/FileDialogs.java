//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2019 Knut Akesson, Martin Fabian, Robi Malik
//###########################################################################
//# This file is part of Waters/Supremica IDE.
//# Waters/Supremica IDE is free software: you can redistribute it and/or
//# modify it under the terms of the GNU General Public License as published
//# by the Free Software Foundation, either version 2 of the License, or
//# (at your option) any later version.
//# Waters/Supremica IDE is distributed in the hope that it will be useful,
//# but WITHOUT ANY WARRANTY; without even the implied warranty of
//# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
//# Public License for more details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters/Supremica IDE statically or dynamically with other modules
//# is making a combined work based on Waters/Supremica IDE. Thus, the terms
//# and conditions of the GNU General Public License cover the whole
//# combination.
//# In addition, as a special exception, the copyright holders of
//# Waters/Supremica IDE give you permission to combine Waters/Supremica IDE
//# with code included in the standard release of Supremica under the
//# Supremica Software License Agreement (or modified versions of such code,
//# with unchanged license). You may copy and distribute such a system
//# following the terms of the GNU GPL for Waters/Supremica IDE and the
//# licenses of the other code concerned.
//# Note that people who make modified versions of Waters/Supremica IDE are
//# not obligated to grant this special exception for their modified versions;
//# it is their choice whether to do so. The GNU General Public License gives
//# permission to release a modified version without this exception; this
//# exception also makes it possible to release a modified version which
//# carries forward this exception.
//###########################################################################

package org.supremica.gui;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import net.sourceforge.waters.model.marshaller.StandardExtensionFileFilter;

import org.supremica.automata.IO.FileFormats;
import org.supremica.properties.Config;


public class FileDialogs
{

    //#######################################################################
    //# Class Constants (for File Extensions)
    public static final String MAINVMOD_EXT = "_main.vmod";
    public static final String VPRJ_EXT = ".vprj";
    public static final String VMOD_EXT = ".vmod";
    public static final String WMOD_EXT = ".wmod";



    //#######################################################################
    //# Data Members
    private JFileChooser fileImporter = null;
    private JFileChooser fileExporter = null;
    private JFileChooser fileSaveAs = null;

    private FileFilter rcpFilter = null;
    private FileFilter xmlFilter = null;
    private FileFilter stsFilter = null;
    private FileFilter spFilter = null;
    private FileFilter vprjFilter = null;
    private FileFilter vmodFilter = null;
    private FileFilter mainvmodFilter = null;
    private FileFilter wmodFilter = null;
    private FileFilter dgrfFilter = null;
    private FileFilter hybFilter = null;
    private FileFilter hiscFilter = null;
    private FileFilter dsxFilter = null;
    @SuppressWarnings("unused")
    private final FileFilter smcFilter = null;
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


    private static final FileDialogs fd = new FileDialogs();

    private FileDialogs()
    {}



    public static JFileChooser getSaveAsFileChooser(final FileFormats fileType)
    {
        final JFileChooser fileSaveAs = fd.getFileSaveAs();

        fileSaveAs.resetChoosableFileFilters();
        fileSaveAs.setFileFilter(fd.getFilter(fileType));

        return fileSaveAs;
    }

    public static JFileChooser getExportFileChooser(final FileFormats fileType)
    {
        final JFileChooser fileExport = fd.getFileExporter();

        fileExport.resetChoosableFileFilters();
        fileExport.setFileFilter(fd.getFilter(fileType));

        return fileExport;
    }

    public static JFileChooser getWatersImportFileChooser()
    {
        final JFileChooser fileImport = fd.getFileImporter();
        final FileFilter last = fileImport.getFileFilter();
        final FileFilter wmod = fd.getWMODFilter();
        final FileFilter vprj = fd.getVPRJFilter();
        final FileFilter vmod = fd.getMainVMODFilter();
        final FileFilter[] filters = {wmod, vprj, vmod};

        // If the dialog has been used before with the same filters,
        // try to set the filter to what is was last.
        boolean found = false;
        fileImport.resetChoosableFileFilters();
        for (int i = 0; i < filters.length; i++)
        {
            fileImport.addChoosableFileFilter(filters[i]);
            if (last == filters[i])
            {
                found = true;
            }
        }
        if (found)
        {
            fileImport.setFileFilter(last);
        }
        else
        {
            fileImport.setFileFilter(wmod);
        }
        return fileImport;
    }

    public static JFileChooser getImportFileChooser(final FileFormats fileType)
    {
        final JFileChooser fileImport = fd.getFileImporter();

        fileImport.resetChoosableFileFilters();
        fileImport.setFileFilter(fd.getFilter(fileType));

        return fileImport;
    }

    public static JFileChooser getXMLFileSaveAs()
    {
        final JFileChooser fileSaveAs = fd.getFileSaveAs();

        fileSaveAs.resetChoosableFileFilters();
        fileSaveAs.setFileFilter(fd.getXMLFilter());

        return fileSaveAs;
    }

    public static JFileChooser getXMLFileImporter()
    {
        final JFileChooser fileImporter = fd.getFileImporter();

        fileImporter.resetChoosableFileFilters();
        fileImporter.setFileFilter(fd.getXMLFilter());

        return fileImporter;
    }

    public static JFileChooser getSPFileSaveAs()
    {
        final JFileChooser fileSaveAs = fd.getFileSaveAs();

        fileSaveAs.resetChoosableFileFilters();
        fileSaveAs.setFileFilter(fd.getSPFilter());

        return fileSaveAs;
    }

    public static JFileChooser getSPFileImporter()
    {
        final JFileChooser fileImporter = fd.getFileImporter();

        fileImporter.resetChoosableFileFilters();
        fileImporter.setFileFilter(fd.getSPFilter());

        return fileImporter;
    }

    public static JFileChooser getSPFileExporter()
    {
        final JFileChooser fileExporter = fd.getFileExporter();

        fileExporter.resetChoosableFileFilters();
        fileExporter.setFileFilter(fd.getSPFilter());

        return fileExporter;
    }

    public static JFileChooser getVALIDFileImporter()
    {
        final JFileChooser fileImporter = fd.getFileImporter();

        fileImporter.resetChoosableFileFilters();
        fileImporter.addChoosableFileFilter(fd.getDGRFFilter());
        fileImporter.addChoosableFileFilter(fd.getVMODFilter());
        fileImporter.setFileFilter(fd.getVPRJFilter());

        return fileImporter;
    }

    public static JFileChooser getWatersFileImporter()
    {
        final JFileChooser fileImporter = fd.getFileImporter();

        fileImporter.resetChoosableFileFilters();
        fileImporter.setFileFilter(fd.getWMODFilter());

        return fileImporter;
    }

    public static JFileChooser getHYBFileImporter()
    {
        final JFileChooser fileImporter = fd.getFileImporter();

        fileImporter.resetChoosableFileFilters();
        fileImporter.addChoosableFileFilter(fd.getHYBFilter());
        fileImporter.setFileFilter(fd.getHYBFilter());

        return fileImporter;
    }

    public static JFileChooser getHISCFileImporter()
    {
        final JFileChooser fileImporter = fd.getFileImporter();

        fileImporter.resetChoosableFileFilters();
        fileImporter.addChoosableFileFilter(fd.getHISCFilter());
        fileImporter.setFileFilter(fd.getHISCFilter());

        return fileImporter;
    }

    public static JFileChooser getRCPFileExporter()
    {
        final JFileChooser fileExporter = fd.getFileExporter();

        fileExporter.resetChoosableFileFilters();
        fileExporter.setFileFilter(fd.getRCPFilter());

        return fileExporter;
    }

    public static JFileChooser getXMLFileExporter()
    {
        final JFileChooser fileExporter = fd.getFileExporter();

        fileExporter.resetChoosableFileFilters();
        fileExporter.setFileFilter(fd.getXMLFilter());

        return fileExporter;
    }

    public static JFileChooser getSTSFileExporter()
    {
        final JFileChooser fileExporter = fd.getFileExporter();

        fileExporter.resetChoosableFileFilters();
        fileExporter.setFileFilter(fd.getSTSFilter());

        return fileExporter;
    }

    public static JFileChooser getDSXFileExporter()
    {
        final JFileChooser fileExporter = fd.getFileExporter();

        fileExporter.resetChoosableFileFilters();
        fileExporter.setFileFilter(fd.getDSXFilter());

        return fileExporter;
    }

    public static JFileChooser getDOTFileExporter()
    {
        final JFileChooser fileExporter = fd.getFileExporter();

        fileExporter.resetChoosableFileFilters();
        fileExporter.setFileFilter(fd.getDOTFilter());

        return fileExporter;
    }

    public static JFileChooser getEPSFileExporter()
    {
        final JFileChooser fileExporter = fd.getFileExporter();

        fileExporter.resetChoosableFileFilters();
        fileExporter.setFileFilter(fd.getEPSFilter());

        return fileExporter;
    }

    public static JFileChooser getMIFFileExporter()
    {
        final JFileChooser fileExporter = fd.getFileExporter();

        fileExporter.resetChoosableFileFilters();
        fileExporter.setFileFilter(fd.getMIFFilter());

        return fileExporter;
    }

    public static JFileChooser getPNGFileExporter()
    {
        final JFileChooser fileExporter = fd.getFileExporter();

        fileExporter.resetChoosableFileFilters();
        fileExporter.setFileFilter(fd.getPNGFilter());

        return fileExporter;
    }

    public static JFileChooser getSVGFileExporter()
    {
        final JFileChooser fileExporter = fd.getFileExporter();

        fileExporter.resetChoosableFileFilters();
        fileExporter.setFileFilter(fd.getSVGFilter());

        return fileExporter;
    }

    public static JFileChooser getGIFFileExporter()
    {
        final JFileChooser fileExporter = fd.getFileExporter();

        fileExporter.resetChoosableFileFilters();
        fileExporter.setFileFilter(fd.getGIFFilter());

        return fileExporter;
    }

    public static JFileChooser getSFileExporter()
    {
        final JFileChooser fileExporter = fd.getFileExporter();

        fileExporter.resetChoosableFileFilters();
        fileExporter.setFileFilter(fd.getSFilter());

        return fileExporter;
    }

    public static JFileChooser getPRJFileExporter()
    {
        final JFileChooser fileExporter = fd.getFileExporter();

        fileExporter.resetChoosableFileFilters();
        fileExporter.setFileFilter(fd.getPRJFilter());

        return fileExporter;
    }

    public static JFileChooser getSTFileExporter()
    {
        final JFileChooser fileExporter = fd.getFileExporter();

        fileExporter.resetChoosableFileFilters();
        fileExporter.setFileFilter(fd.getSTFilter());

        return fileExporter;
    }

    public static JFileChooser getILFileExporter()
    {
        final JFileChooser fileExporter = fd.getFileExporter();

        fileExporter.resetChoosableFileFilters();
        fileExporter.setFileFilter(fd.getILFilter());

        return fileExporter;
    }

    public static JFileChooser getNQCFileExporter()
    {
        final JFileChooser fileExporter = fd.getFileExporter();

        fileExporter.resetChoosableFileFilters();
        fileExporter.setFileFilter(fd.getNQCFilter());

        return fileExporter;
    }

    public static JFileChooser getAutFileImporter()
    {
        final JFileChooser autFileImporter = fd.getFileImporter();

        autFileImporter.resetChoosableFileFilters();
        autFileImporter.setFileFilter(fd.getAutFilter());

        return autFileImporter;
    }

    public static JFileChooser getRobotStudioStationFileImporter()
    {
        final JFileChooser stationFileImporter = fd.getFileImporter();

        stationFileImporter.resetChoosableFileFilters();
        stationFileImporter.setFileFilter(fd.getSTNFilter());

        return stationFileImporter;
    }

    public static JFileChooser getRobotCellFileImporter()
    {
        final JFileChooser cellFileImporter = fd.getFileImporter();

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
            fileImporter.setCurrentDirectory(new java.io.File(Config.FILE_OPEN_PATH.getAsString()));
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
            fileExporter.setCurrentDirectory(new java.io.File(Config.FILE_SAVE_PATH.getAsString()));
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
            fileSaveAs.setCurrentDirectory(new java.io.File(Config.FILE_SAVE_PATH.getAsString()));
            fileSaveAs.setMultiSelectionEnabled(false);
        }

        return fileSaveAs;
    }

    // all of the below look exactly the same - this FileFilter derivative generalizes it all
    // Note, could not have variable referred to in anonymous class
    private FileFilter makeFileFilter(final String ext, final String description)
    {
        return new StandardExtensionFileFilter(description, ext);
    }

    private FileFilter getFilter(final FileFormats fileType)
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

    private FileFilter getSTSFilter()
    {
        if (stsFilter == null)
        {
            stsFilter = makeFileFilter("", "STS files (*)");
        }

        return stsFilter;
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
            vprjFilter =
                makeFileFilter(VPRJ_EXT, "VALID Project files (*.vprj)");
        }

        return vprjFilter;
    }

    private FileFilter getVMODFilter()
    {
        if (vmodFilter == null)
        {
            vmodFilter =
                makeFileFilter(VMOD_EXT, "VALID Module files (*.vmod)");
        }

        return vmodFilter;
    }

    private FileFilter getMainVMODFilter()
    {
        if (mainvmodFilter == null)
        {
            mainvmodFilter = makeFileFilter
                (MAINVMOD_EXT, "VALID Main Module files (*_main.vmod)");
        }

        return mainvmodFilter;
    }

    private FileFilter getDGRFFilter()
    {
        if (dgrfFilter == null)
        {
            dgrfFilter = makeFileFilter(".dgrf", "VALID Graph files (*.dgrf)");
        }

        return dgrfFilter;
    }

    private FileFilter getWMODFilter()
    {
        if (wmodFilter == null)
        {
            wmodFilter =
                makeFileFilter(WMOD_EXT, "Waters Module files (*.wmod)");
        }

        return wmodFilter;
    }

    private FileFilter getHYBFilter()
    {
        if (hybFilter == null)
        {
            hybFilter = makeFileFilter(".hyb", "Balemi HYB file (*.hyb)");
        }

        return hybFilter;
    }

    private FileFilter getHISCFilter()
    {
        if (hiscFilter == null)
        {
            hiscFilter = makeFileFilter(".prj", "HISC project file (*.prj)");
        }

        return hiscFilter;
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
