package org.supremica.util.BDD.solvers;

import org.supremica.util.BDD.*;

/** Internal Node class */
public class Node
{
	PCGNode org;
	int index, index_local;
	int id, size;
	int extra1, extra2, extra3;    // extra vars used as intermediate vars in algos
	int[] weight, wlocal;
}
