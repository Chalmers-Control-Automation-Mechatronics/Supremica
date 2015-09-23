//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2015 Knut Akesson, Martin Fabian, Robi Malik
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

import javax.swing.filechooser.*;
import javax.swing.*;
import java.io.File;

import net.sourceforge.waters.model.marshaller.StandardExtensionFileFilter;
import org.supremica.log.*;

public class StandardExtensionFileChooser
	extends JFileChooser
{
	private static final long serialVersionUID = 1L;
	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.createLogger(StandardExtensionFileChooser.class);

	public StandardExtensionFileChooser() {}

	/**
	 * Adds the standard extension if nothing else is specificed.
	 */
	public File getSelectedFile()
	{

		//System.err.println("getSelectedFile");
		File orgFile = super.getSelectedFile();

		if (getDialogType() != JFileChooser.SAVE_DIALOG)
		{
			return orgFile;
		}

		if (orgFile == null)
		{
			return orgFile;
		}

		FileFilter theFilter = getFileFilter();

		if (theFilter == null)
		{
			return orgFile;
		}

		if (!(theFilter instanceof StandardExtensionFileFilter))
		{
			return orgFile;
		}

		//System.err.println("all files pre");
		StandardExtensionFileFilter standardFileFilter = (StandardExtensionFileFilter) theFilter;

		if ("All Files".equals(theFilter.getDescription()))
		{    // For some reason this does not work

			//System.err.println("all files");
			String fileName = getName(orgFile);

			if (fileName == null)
			{
				return orgFile;
			}

			if (fileName.startsWith("\"") && fileName.endsWith("\""))
			{

				//System.err.println("all files with fnuttar");
				File newFile = new File(orgFile.getParentFile().getAbsolutePath() + File.separator + fileName.substring(1, fileName.length() - 1));

				return newFile;
			}

			return orgFile;
		}

		String fileName = getName(orgFile);

		if (fileName == null)
		{
			return orgFile;
		}

		if (fileName.startsWith("\"") && fileName.endsWith("\""))
		{
			File newFile = new File(orgFile.getParentFile().getAbsolutePath() + File.separator + fileName.substring(1, fileName.length() - 1));

			return newFile;
		}

		return standardFileFilter.ensureDefaultExtension(orgFile);
	}
}
