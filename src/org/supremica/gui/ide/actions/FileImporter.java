//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: org.supremica.gui.ide.actions
//# CLASS:   OpenAction
//###########################################################################
//# $Id: FileImporter.java,v 1.2 2005-02-24 09:04:13 robi Exp $
//###########################################################################


package org.supremica.gui.ide.actions;

import javax.swing.JFileChooser;
import org.supremica.gui.ide.IDE;
import java.io.File;

abstract class FileImporter
{
	FileImporter(JFileChooser fileOpener, IDE ide)
	{
		if (fileOpener.showOpenDialog(ide.getFrame()) == JFileChooser.APPROVE_OPTION)
		{
			File[] currFiles = fileOpener.getSelectedFiles();

			if (currFiles != null)
			{
				for (int i = 0; i < currFiles.length; i++)
				{
					if (currFiles[i].isFile())
					{
						openFile(currFiles[i]);
					}
				}
			}

			ide.repaint();
		}
	}

	abstract void openFile(File file);
}
