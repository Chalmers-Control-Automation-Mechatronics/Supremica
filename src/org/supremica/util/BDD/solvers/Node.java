
package org.supremica.util.BDD.solvers;

import org.supremica.util.BDD.*;

import java.util.*;


/** Internal Node class */
public class Node  {
	PCGNode org;
	int index, index_local;
	int id, size, extra1, extra2; // extra vars used as intermediate vars in algos
	int [] weight, wlocal;
}
