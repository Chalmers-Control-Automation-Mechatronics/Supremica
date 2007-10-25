/*
 * BooleanCombinationTreeSet.java
 *
 * Created on den 23 oktober 2007, 11:19
 * @author Avenir Kobetski
 */

package org.supremica.automata.algorithms.scheduling.milp;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;

/**
 * This class extends a TreeSet to order int[]-objects that represent boolean
 * combinations of internal precedence variables. Only the combination of 
 * variables used in the current precedence specification are added to this 
 * BooleanCombinationTreeSet. The int[]-objects that serve as input to this tree 
 * consist of values in {-1, 0, 1}, while the int[]-objects stored in the tree 
 * correspond to possible combinations of boolean internal precedence variables, 
 * that is they can only contain 0's or 1's. The value of -1 means that the 
 * corresponding internal precedence variable may be of any value. Thus, each 
 * -1-value is converted into two possibilities (0 and 1) and the number of int[]-
 * objects added to the tree is multiplied by two. 
 */
public class BooleanCombinationTreeSet
        extends IntArrayTreeSet
{
    /**
     * Creates a IntArrayTreeSet using {@link IntArrayComparator} to order 
     * int[]-objects that represent boolean permutations. 
     */
    BooleanCombinationTreeSet()
    {
        super();
    }
    
    /**
     * Adds the supplied combination of variables to an array and calls 
     * {@link #addArray(ArrayList) addArray} 
     * to add all combinations that do not negate this int[]-object to the 
     * BooleanCombinationTreeSet.
     *
     * @param   boolCombination the int[]-object representing a combination of
     *                          internal precedence variable values.
     * @return  true    if an object was added;
     *          false   otherwise.
     */
    public boolean add(int[] boolCombination)
    {
        ArrayList<int[]> combinationsToBeAdded = new ArrayList<int[]>();
        combinationsToBeAdded.add(boolCombination);
        
        return addArray(combinationsToBeAdded);
    }

    /**
     * The value of -1 in any int[]-object supplied to this method means that the 
     * corresponding internal precedence variable may be of any value. Thus, this
     * method finds all -1-values in the current combination of boolean variables 
     * replaces them with 0's and 1's, by copying the corresponding int[]-object,
     * add the new objects to the array and calls itself. When no -1-values are
     * found, all int[]-objects in the array (that is all variable combinations
     * that do not negate the original one) are added to the BooleanCombinationTreeSet.
     *
     * @param  combinationsToBeAdded    an array list containing int[]-objects that 
     *                                  represent variable combinations that should 
     *                                  be added to the tree.
     * @return  true    if an object was added;
     *          false   otherwise.                
     */
	private boolean addArray(ArrayList<int[]> combinationsToBeAdded)
	{
		ArrayList<int[]> newCombinationsToBeAdded = new ArrayList<int[]>();
		for (int i = 0; i < combinationsToBeAdded.get(0).length; i++)
		{
			if (combinationsToBeAdded.get(0)[i] == -1)
			{
				for (Iterator<int[]> it = combinationsToBeAdded.iterator(); it.hasNext(); )
				{
					int[] currCombination = it.next();

					currCombination[i] = 0;
					newCombinationsToBeAdded.add(currCombination);

					int[] newCombination = new int[currCombination.length];
					for (int j = 0; j < currCombination.length; j++)
					{
						newCombination[j] = currCombination[j];
					}
					newCombination[i] = 1;
					newCombinationsToBeAdded.add(newCombination);
				}

				return addArray(newCombinationsToBeAdded);
			}
		}

		boolean elementAdded = false;
		for (Iterator<int[]> it = combinationsToBeAdded.iterator(); it.hasNext(); )
		{
			int[] currCombination = it.next();
			if (super.add(currCombination))
			{
				elementAdded = true;
			}
		}

		return elementAdded;
	}
}