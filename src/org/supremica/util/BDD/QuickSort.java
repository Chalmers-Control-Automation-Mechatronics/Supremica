package org.supremica.util.BDD;

import java.util.*;

// NOTE:
//
// !!! THIS CLASS HAS NOT BEEN TESTED YET!!!
//




public class QuickSort
{

	/** helper function to quicksort (quicksort partition) */
	private static int partition(WeightedObject [] list, int p, int r) {
		double x = list[r].weight();
		int i = p - 1;
		WeightedObject tmp;

		for(int j = p; j < r; j++) {
			if(list[j].weight() <= x) {
				i++;
				// SWAP I <-> J
				tmp = list[i];
				list[i] = list[j];
				list[j] = tmp;
			}
		}

		// SWAP I+1 <-> r
		i++;
		tmp = list[i];
		list[i] = list[r];
		list[r] = tmp;
		return i;
	}

	/** worker function for sort (quick sort function) */
	private static void quicksort(WeightedObject [] list, int p, int r) {
		if(p < r) {
				int q = partition(list, p, r);
				quicksort(list, p, q-1);
				quicksort(list, q+1,r);

		}
	}

	/**
	 * quicksort.
	 * @param reverse reverse the sort
	 */
	public static void sort(WeightedObject [] v, boolean reverse) {
		int size = v.length;
		quicksort(v, 0, size-1);
		if(reverse) Util.reverse(v,size);
	}
}
