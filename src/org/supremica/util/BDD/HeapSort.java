

package org.supremica.util.BDD;


public class HeapSort {

	public static void sort( WeightedObject [] a, int n) {
		WeightedObject tmp;

		for(int i = ( n-1) / 2; i>= 0; i--)
			percolate_down(a, i, n);

		for(int j = n -1; j>= 1; j--) {
			// sawp 0 <-> j
			tmp = a[0];
			a[0] = a[j];
			a[j] = tmp;
			percolate_down(a, 0, j-1 );
		}
	}

	private static void percolate_down(WeightedObject [] array, int hole, int curr) {
		int child;
		WeightedObject tmp = array[hole];

		curr--;
		for( tmp = array[hole];  2 * hole  < curr; hole = child) {
			child = 2 * hole + 1;
			if(child != curr && array[child].less ( array[child + 1] ) )
				child ++;
			if(tmp.less( array[child]) )
				array[hole] = array[child];
			else
				break;
		}
		array[hole] = tmp;
	}


// --[ testbench ]-----------------------------------------------

	public static void main(String [] args) {
		WeightedObject [] x = new WeightedObject[10];

		for(int i = 0; i < x.length; i++) {
			double r = 100 * Math.random();
			x[i] = new WeightedObject( "" + i, r);
			System.out.println( "" + x[i].object()  + " --> " + r);
		}

		HeapSort.sort(x, x.length);

		for(int i = 0; i < x.length; i++) {
			String s= (String) x[i].object();
			System.out.println(s);
		}
	}

}