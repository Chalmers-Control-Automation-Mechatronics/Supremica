
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
						openFile(ide, currFiles[i]);
					}
				}
			}

			ide.repaint();
		}
	}

	abstract void openFile(IDE ide, File file);
}
