package org.supremica.util.BDD;

// NOTE:
//
// !!! THIS CLASS HAS NOT BEEN TESTED YET!!!
//
public class QuickSort
{

	// ----[ bubble-sort, when quick-sort is an overkill ]---------------------------------------------

	/** bubble sort for list of Objects */
	public static void bubble_sort(final double [] weights, final Object [] data, final int len) {
		for (int i = len; --i >= 0; ) {
		boolean flipped = false;
			for (int j = 0; j<i; j++) {
				if( weights[j+1] < weights[j]) {
					// SWAP
					final Object tmp1 = data[j]; data[j] = data[j+1]; data[j+1] = tmp1;
					final double tmp2 = weights[j]; weights[j] = weights[j+1]; weights[j+1] = tmp2;
					flipped = true;
				}
			}
			if (!flipped)
				return;
		}
	}


	// ----[ quicksort for array types ]---------------------------------------------
	private static int[] array_;
	private static double[] cost_;

	/**
	 * quicksor of arrays + costs
	 * @param reverse reverse the sort
	 */
	public static void sort(final int[] array, final double[] cost, final int size, final boolean reverse)
	{
		array_ = array;
		cost_ = cost;

		quicksort_(0, size - 1);

		if (reverse)
		{
			Util.reverse(array, size);
			Util.reverse(cost, size);
		}
	}

	/** more useless junk...  */
	private static void swap_(final int a, final int b)
	{
		double tmp1;
		int tmp2;

		tmp1 = cost_[a];
		cost_[a] = cost_[b];
		cost_[b] = tmp1;
		tmp2 = array_[a];
		array_[a] = array_[b];
		array_[b] = tmp2;
	}

	/** helper function to quicksort (quicksort partition) */
	private static int partition_(final int p, final int r)
	{
		final double x = cost_[r];
		int i = p - 1;

		for (int j = p; j < r; j++)
		{
			if (cost_[j] <= x)
			{
				i++;

				swap_(i, j);
			}
		}

		i++;

		swap_(i, r);

		return i;
	}

	/** worker function for sort (quick sort function) */
	private static void quicksort_(final int p, final int r)
	{
		if (p < r)
		{
			final int q = partition_(p, r);

			quicksort_(p, q - 1);
			quicksort_(q + 1, r);
		}
	}

	// ----[ quicksort for Object-array types ]---------------------------------------------
	private static Object[] oarray_;

	/**
	 * quicksor of arrays of Objects + costs
	 * @param reverse reverse the sort
	 */
	public static void sort(final Object[] array, final double[] cost, final int size, final boolean reverse)
	{


		oarray_ = array;
		cost_ = cost;

		if(size >= Options.MIN_QUICKSORT_THRESHOLD)
		{
			oquicksort_(0, size - 1);
		}
		else
		{
			bubble_sort(cost, array, size);
		}


		if (reverse)
		{
			Util.reverse(oarray_, size);
			Util.reverse(cost_, size);
		}
	}

	private static void oswap_(final int a, final int b)
	{
		double tmp1;
		Object tmp2;

		tmp1 = cost_[a];
		cost_[a] = cost_[b];
		cost_[b] = tmp1;
		tmp2 = oarray_[a];
		oarray_[a] = oarray_[b];
		oarray_[b] = tmp2;
	}

	/** helper function to quicksort (quicksort partition) */
	private static int opartition_(final int p, final int r)
	{
		final double x = cost_[r];
		int i = p - 1;

		for (int j = p; j < r; j++)
		{
			if (cost_[j] <= x)
			{
				i++;

				oswap_(i, j);
			}
		}

		i++;

		oswap_(i, r);

		return i;
	}

	/** worker function for sort (quick sort function) */
	private static void oquicksort_(final int p, final int r)
	{
		if (p < r)
		{
			final int q = opartition_(p, r);

			oquicksort_(p, q - 1);
			oquicksort_(q + 1, r);
		}
	}
}
