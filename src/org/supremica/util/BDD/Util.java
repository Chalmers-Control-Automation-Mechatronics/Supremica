package org.supremica.util.BDD;

import java.util.*;

public class Util
{
	private static int free_memory;    // free memory in MB
	private static final long MIN_NODES = 10000, MAX_NODES = 8000000;

	static
	{
		// I have no idea why i wrote this part....
		Runtime rt = Runtime.getRuntime();
		free_memory = (int) (rt.freeMemory() / (1024 * 1024));
	}


	// -------------------------------------------------

	/** duplicates a vector of booleans */
	public static boolean [] duplicate(boolean [] x) {
		int len = x.length;
		boolean [] ret = new boolean[len];
		System.arraycopy(x, 0,  ret, 0, len);
		return ret;
	}


	/** duplicates a vector of integers.
	 * MY KINGDOM FOR JAVA-TEMPLATES.... James Gosling, please :)
	 */
	public static int [] duplicate(int [] x) {
		int len = x.length;
		int [] ret = new int[len];
		System.arraycopy(x, 0,  ret, 0, len);
		return ret;
	}
	/**
	 * count the number of occurrence of 'v' in x.
	 *
	 */
	public static int countEQ(int [] x, int v) {
		int ret = 0, len = x.length;
		for(int i = 0; i < len; i++) if(x[i] == v) ret++;
		return ret;
	}

	/** shuffle/permutate a list of integers */
	public static void permutate(int [] list, int size) {
		for(int i = 0; i < size; i++) {
			int next = (int)(Math.random() * size);
			int tmp = list[i];
			list[i] = list[next];
			list[next] = tmp;
		}
	}

	/** shuffle/permutate a list of integers */
	public static int [] permutate(int size) {
		int [] x = new int[size];
		for(int i = 0; i < size; i++) x[i] = i;
		permutate(x, size);
		return x;
	}

	// -------------------------------------------------

	public static int suggest_nodecount(Automata a)
	{

		// I dont know why and I dont know how... :)
		int size = a.getVariableCount();
		// double d = free_memory * 100000 * Math.log(size + 2) / Math.log(2);
		double d = 10000 * (size + 1);
		int nodes = (int) Math.max(Math.min(d, MAX_NODES), MIN_NODES);

		if(Options.size_watch) {
			Options.out.println("suggsted " + nodes + " nodes for " + size + " variables.");
		}

		return nodes;
	}

	// -------------------------------------------------
	/** this is an extremely sophisticated math algorithm, do not try to understand it, you cant */
	public static int log2ceil(int num)
	{
		if (num <= 1)
		{
			return 1;    // this is the minimum!
		}

		for (int i = 0; i < 32; i++)
		{
			if ((1 << i) >= num)
			{
				return i;
			}
		}

		System.err.println("Cannot log2 " + num);
		System.exit(20);

		return 0;    // damn compiler :)
	}

	/**
	 * create a BDD for a number, using the default encoding.
	 *
	 */
	public static int getNumber(BDDAutomata manager, int[] vars, int number)
	{
		int ret = manager.getOne();

		manager.ref(ret);

		for (int i = 0; i < vars.length; i++)
		{
			ret = manager.andTo(ret, ((number & (1 << i)) != 0)
									 ? vars[i]
									 : manager.not(vars[i]));

			/*
			 * Options.out.println(i + " --> " + manager.internal_refcount(ret) +
			 *                  " / " + manager.internal_refcount(vars[i]));
			 *
			 * if( manager.internal_refcount(ret) == 0) {
			 *   Options.out.println("(number & (1 << i)) == " +
			 *                      (number & (1 << i)));
			 *
			 *   int not = manager.not(vars[i]);
			 *
			 *   manager.print(vars[i]);
			 *   manager.print(not);
			 *   Options.out.println("ID: " + vars[i] + " / " + not + " -- " +
			 *                      "REFS: " +
			 *                      manager.internal_refcount(vars[i]) + " / " +
			 *                      manager.internal_refcount(not));
			 *
			 *   System.exit(20);
			 * }
			 */
		}

		return ret;
	}





	// --[BDD tree quicksort] ------------------------------------------------------------------

	/** helper function to sort_variable_list (quicksort partition) */
	private static int partition_bdd(JBDD manager, int [] list, int p, int r) {
		int x = manager.internal_index(list[r]);
		int i = p -1, tmp;

		for(int j = p; j < r; j++) {
			if(manager.internal_index(list[j])<= x) {
				i++;
				// SWAP I <-> J
				// Options.out.println("SWAP "+i+" <-> "+j);
				tmp = list[i];
				list[i] = list[j];
				list[j] = tmp;
			}
		}

		// SWAP I+1 <-> r
		i++;
		// Options.out.println("SWAP2 "+i+" <-> "+r);
		tmp = list[i];
		list[i] = list[r];
		list[r] = tmp;
		return i;
	}

	/** helper function to sort_variable_list (quick sort function) */
	private static void quicksort_bdd(JBDD manager, int [] list, int p, int r) {
		if(p < r) {
				int q = partition_bdd(manager, list, p, r);
				quicksort_bdd(manager,list, p, q-1);
				quicksort_bdd(manager,list, q+1,r);

		}
	}

	/**
	 * sort a list of BDD _variables_ (not trees) from top to bottom)
	 * if reverse_ is not set, the highest BDD will be put first
	 * if revesre_ is set, the lowest BDD (nearest to terminal 0/1) are put first in the list
	 */
	public static void sort_variable_list(JBDD manager, int [] variables, int size, boolean reverse_) {
		quicksort_bdd(manager, variables, 0, size-1);
		if(reverse_) reverse(variables,size);
	}


	// ------------------------------------------------------------------------------
	/** reverse some list */
	public static void reverse(Object [] variables, int size) {
		for(int j = 0; j < size / 2; j++) {
			int i = size - j -1;
			Object tmp = variables[i];
			variables[i] = variables[j];
			variables[j] =  tmp;
		}
	}

	/** reverse some int-array */
	public static void reverse(int [] variables, int size) {
		for(int j = 0; j < size / 2; j++) {
			int i = size - j -1;
			int tmp = variables[i];
			variables[i] = variables[j];
			variables[j] =  tmp;
		}
	}


	/** reverse some doube-array */
	public static void reverse(double [] variables, int size) {
		for(int j = 0; j < size / 2; j++) {
			int i = size - j -1;
			double tmp = variables[i];
			variables[i] = variables[j];
			variables[j] =  tmp;
		}
	}


	// ------------------------------------------------------------------------------

	/**
     * write to stderr and wait until user presses ENTER.
     * good to make user see why we are going to die before we die...
     */
	public static void notify(String msg) {
		System.err.println(msg);
		System.err.flush();
		try {
			while( System.in.read() != '\n') ;
		} catch(Exception exxx) { }
	}


	// ------------------------------------------------------------------------------

    /**
     * append_to := append_to + append_from<br>
     * duplicates are NOT ignored :(
     *
     */

	public static void append(Vector append_to, Vector append_from) {
		for (Enumeration e = append_from.elements(); e.hasMoreElements(); )
			append_to.addElement( e.nextElement() );
	}

	// ------------------------------------------------------------------------------

    /**
     * Actually SHOW the BDD as an EPS file<br>
     * requires: AT&T dot, ghostview.<br>
     * works only on UNIX :)
     */
    public static void showBDD(JBDD manager, int bdd, String name) {
	String file = "/tmp/BDD_" + name ;

	manager.printDot(bdd, file + ".dot");
	try {

	    Runtime.getRuntime().exec("dot -Tps " + file + ".dot -o " + file + ".eps");
	    Runtime.getRuntime().exec("ghostview " + file + ".eps &");

	    Thread.sleep(1000); // calm down [for user]
	} catch(Exception exx) {
	    exx.printStackTrace();
	}
    }

    public static String showHugeNumber(double n) {
		int mul = 0;

		if(n < 1000) return "" + ((int)n);

		while( n > 1000) {
			n /= 1000;
			mul ++;
		}

		// three decimals would do
		n = ((double)((int)(n * 1000))) / 1000.0;
		return "" + n + " E+" + (mul * 3);
	}
}
