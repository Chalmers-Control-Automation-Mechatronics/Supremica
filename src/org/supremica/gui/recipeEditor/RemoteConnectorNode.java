
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

public class RemoteConnectorNode
	extends BasicNode
{

	/** Call initialize() before using. */
	public RemoteConnectorNode() {}

	public void initialize(Point loc, String labtxt, RemoteConnectorNode other)
	{
		setLabelSpot(JGoObject.Center);
		super.initialize(loc, labtxt);
		getPort().setValidSource(false);
		getPort().setValidDestination(false);

		if (getLabel() != null)
		{
			getLabel().setEditable(true);
		}

		myOther = other;
	}

	public RemoteConnectorNode getOtherConnector()
	{
		return myOther;
	}

	private RemoteConnectorNode myOther = null;
}
