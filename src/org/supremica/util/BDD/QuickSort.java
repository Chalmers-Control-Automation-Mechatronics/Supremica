package org.supremica.util.BDD;

import java.util.*;

// NOTE:
//
// !!! THIS CLASS HAS NOT BEEN TESTED YET!!!
//




public class QuickSort
{

	// ----[ quicksort for WeightedObject ]-----------------------------------------------

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




	// ----[ quicksort for array types ]---------------------------------------------

	private static int [] array_;
	private static double [] cost_;

	/**
	 * quicksor of arrays + costs
	 * @param reverse reverse the sort
	 */

	public static void sort(int [] array, double [] cost, int size, boolean reverse) {
		array_ = array;
		cost_  = cost;
		quicksort_(0, size-1);
		if(reverse) {
			Util.reverse(array,size);
			Util.reverse(cost,size);
		}
	}


	/** more useless junk...  */
	private static void swap_(int a, int b) {
		double tmp1;
		int tmp2;

		tmp1 = cost_[a]; cost_[a] = cost_[b]; cost_[b] = tmp1;
		tmp2 = array_[a]; array_[a] = array_[b]; array_[b] = tmp2;
	}

	/** helper function to quicksort (quicksort partition) */
	private static int partition_(int p, int r) {
		double x = cost_[r];
		int i = p - 1;

		for(int j = p; j < r; j++) {
			if(cost_[j] <= x) {
				i++;
				swap_(i, j);
			}
		}

		i++;
		swap_(i, r);
		return i;
	}

	/** worker function for sort (quick sort function) */
	private static void quicksort_(int p, int r) {
		if(p < r) {
			int q = partition_(p, r);
			quicksort_(p, q-1);
			quicksort_(q+1,r);

		}
	}


}
