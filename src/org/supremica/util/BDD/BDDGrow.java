package org.supremica.util.BDD;

import org.supremica.util.BDD.graphs.*;

/**
 * This is a sort of 'factory' class for the BDD grow graphs.
 * It will return the corret type of the graph, or null if none
 * needed.
 *
 * the two proxy classes are defined below in this file.
 *
 * TODO:
 * SAT-count is given as a 'double', but then catsed and converted to an 'int'.
 * we might need to use log-scale, otherwise that 'int' will overflow very easy.
 */
public class BDDGrow
{
	static GrowFrame getGrowFrame(BDDAutomata manager, String title)
	{
		switch (Options.show_grow)
		{

		case Options.SHOW_GROW_NODES :
			return new BDDNodeGrow(manager, title);

		case Options.SHOW_GROW_NODES_LOG :
			return new BDDNodeLogGrow(manager, title);

		case Options.SHOW_GROW_NODES_DIFF :
			return new BDDNodeDiffGrow(manager, title);

		case Options.SHOW_GROW_SATCOUNT :
			return new BDDSATGrow(manager, title);

		case Options.SHOW_GROW_SATCOUNT_LOG :
			return new BDDSATLogGrow(manager, title);

		case Options.SHOW_GROW_SATCOUNT_DIFF :
			return new BDDSATDiffGrow(manager, title);

		// and the big bad harley
		case Options.SHOW_GROW_NODES_AND_SATCOUNT_LOG :
			return new BDDNodeANDSATLogGrow(manager, title);

		case Options.SHOW_GROW_NODES_AND_SATCOUNT_DIFF :
			return new BDDNodeANDSATDiffGrow(manager, title);

		default :
			return null;
		}
	}
}
