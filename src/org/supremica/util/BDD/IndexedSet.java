

package org.supremica.util.BDD;

/**
 * contains helper functions for indexed sets (membership functions)
 *
 */

public class IndexedSet {

	/** get the cardinality of a set */
	public static int cardinality(boolean [] x) {
		int len = x.length;
		int c = 0;
		for(int i = 0; i < len; i++) if(x[i]) c++;
		return c;
	}


	/** compute set intersection, return cardinality */
	public static int intersection(boolean [] left, boolean [] right, boolean [] result) {
			int len = left.length;
			int c = 0;
			for(int i = 0; i < len; i++)
				if( (result[i] = left[i] & right[i]))
					c++;
			return c;
	}

	/** compute set union, return cardinality */
	public static int union(boolean [] left, boolean [] right, boolean [] result) {
			int len = left.length;
			int c = 0;
			for(int i = 0; i < len; i++)
				if( (result[i] = left[i] | right[i]))
					c++;
			return c;
	}

	/** compute set diff, result = left - right, return cardinality */
	public static int  diff(boolean [] left, boolean [] right, boolean [] result) {
		int len = left.length;
		int c = 0;
		for(int i = 0; i < len; i++)
			if( (result[i] = left[i] & !right[i]))
				c++;
		return c;
	}


	/** x := emptyset */
	public static void empty(boolean [] x)  {
		int len = x.length;
		for(int i = 0; i < len; i++) x[i] = false;
	}

	/** x := universe set */
	public static void full(boolean [] x)  {
		int len = x.length;
		for(int i = 0; i < len; i++) x[i] = true;
	}

	/** set inverse, x - universe */
	public static void negate(boolean [] x)  {
		int len = x.length;
		for(int i = 0; i < len; i++) x[i] = ! x[i];
	}

	/** dst := src */
	public static void copy(boolean [] dst, boolean[] src)  {
		int len = src.length;
		for(int i = 0; i < len; i++) dst[i] = src[i];
	}

	public static boolean[] clone(boolean [] src)  {
		boolean [] ret = new boolean[src.length];
		copy(ret, src);
		return ret;
	}


	/** left += right */
	public static void add(boolean [] left, boolean [] right) {
		int len = left.length;
		for(int i = 0; i < len; i++)
			left[i] |= right[i];
	}

}