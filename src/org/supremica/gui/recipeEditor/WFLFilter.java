
/*
 *  Copyright © Northwoods Software Corporation, 2000-2002. All Rights
 *  Reserved.
 *
 *  Restricted Rights: Use, duplication, or disclosure by the U.S.
 *  Government is subject to restrictions as set forth in subparagraph
 *  (c) (1) (ii) of DFARS 252.227-7013, or in FAR 52.227-19, or in FAR
 *  52.227-14 Alt. III, as applicable.
 *
 */
package org.supremica.gui.recipeEditor;

import java.io.File;
import javax.swing.*;
import javax.swing.filechooser.*;

public class WFLFilter
	extends FileFilter
{

	// Accept all directories and all gif, jpg, or tiff files.
	public boolean accept(File f)
	{
		if (f.isDirectory())
		{
			return true;
		}

		// String extension = java.Utils.getExtension(f);
		String sPath = f.getPath();

		sPath.toLowerCase();

		if (sPath.indexOf(".") != -1)
		{
			if (sPath.endsWith(".wfl"))
			{
				return true;
			}
			else
			{
				return false;
			}
		}

		return false;
	}

	// The description of this filter
	public String getDescription()
	{
		return ".wfl";
	}
}
