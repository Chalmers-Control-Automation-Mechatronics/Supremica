
/*
 *  Copyright © Northwoods Software Corporation, 1999-2002. All Rights
 *  Reserved.
 *
 *  Restricted Rights: Use, duplication, or disclosure by the U.S.
 *  Government is subject to restrictions as set forth in subparagraph
 *  (c) (1) (ii) of DFARS 252.227-7013, or in FAR 52.227-19, or in FAR
 *  52.227-14 Alt. III, as applicable.
 *
 */
package org.supremica.gui.recipeEditor;

import com.nwoods.jgo.*;

//import com.nwoods.jgo.examples.*;
import java.awt.*;

public class ExternalConnectorNode
	extends BasicNode
{

	/** Call initialize() before using. */
	public ExternalConnectorNode() {}

	public void initialize(Point loc, String labtxt, boolean out)
	{
		super.initialize(loc, labtxt);

		if (out)
		{
			getPort().setValidSource(false);
			getPort().setToSpot(JGoObject.CenterLeft);
		}
		else
		{
			getPort().setValidDestination(false);
			getPort().setFromSpot(JGoObject.CenterRight);
		}

		if (getLabel() != null)
		{
			getLabel().setEditable(true);
		}
	}
}
