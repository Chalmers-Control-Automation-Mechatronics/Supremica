//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: org.supremica.gui.ide.actions
//# CLASS:   OpenAction
//###########################################################################
//# $Id: FileImporter.java,v 1.3 2005-03-03 23:40:20 knut Exp $
//###########################################################################


package org.supremica.gui.ide.actions;

import javax.swing.JFileChooser;
import org.supremica.gui.ide.IDE;
import java.io.File;

abstract class FileImporter
{
	FileImporter(JFileChooser fileOpener, IDEActionInterface ide)
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
