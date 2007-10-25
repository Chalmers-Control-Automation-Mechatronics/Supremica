/*
 * IntArrayComparator.java
 *
 * Created on den 23 oktober 2007, 11:21
 * @author Avenir Kobetski
 */

package org.supremica.automata.algorithms.scheduling.milp;

import java.util.Comparator;

/** 
 * This class enables comparison between two int[]-objects, being equal if and 
 * only if all elements of the objects are identical. Otherwise, the object with 
 * the smallest leftmost element is labeled as smaller. If the leftmost elements
 * are identical, the next-leftmost elements are considered, etc. The comparator
 * implemented in this class is used to order the elements of the BooleanCombinationTreeSet.
 */
public class IntArrayComparator
	implements Comparator<int[]>
{
    /**
     * Returns the value of the first element that differ between the 
     * supplied int[]-objects (firstNode[i] - secondNode[i]). If all elements 
     * are identical, zero is returned.
     *
     * @param   firstNode   the first int[]-object
     * @param   secondNode  the second int[]-object
     * @return  the difference between the first element that has not the same 
     *          value in the supplied int[]-objects, i.e. firstNode[i] - secondNode[i], 
     *          where firstNode[j] = secondNode[j] forall j < i.
     */
    public int compare(int[] firstNode, int[] secondNode)
    {
        for (int i = 0; i < firstNode.length; i++)
        {
            int currDiff = firstNode[i] - secondNode[i];
            if (currDiff != 0)
            {
                return currDiff;
            }
        }

        return 0;
    }
}
