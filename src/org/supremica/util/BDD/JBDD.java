package org.supremica.util.BDD;

/**
		<pre>
		Java Native Interface for BuDDy BDD package

		Binary Decision Diagrams [BDDs] are used for efficient
		computation of many common problems. This is done by giving
		a compact representation and a set efficient operations on
		boolean functions f: {0,1}^n --> {0,1}.

		It turns out that this representation is good enough to solve a huge
		amount of problems in Artificial Intelligence. For more information
		about application areas of BDDs, check out the lecture notes of
		Alan Mishchenko "BDDs and their application" which is found on the
		net.


		This is a BDD Java interface for the two popular BDD
		packages BuDDy and CUDD. This allows you to combain the
		programmer-friend programming language of Java with the
		efficient BDD packages writen in highly optimized C.


		You can choose between BuDDy and CUDD at runtime.
		for BuDDy use
		<i>     static {  System.loadLibrary("buddy");   }</i>
		for CUDD, use this instead
		<i>     static {  System.loadLibrary("cudd");   }</i>

		Assuming that the dynamic loadable library for that packages
		is in your PATH (windows)  or LD_LIBRARY_PATH (UNIX)



		happy hacking

		   Arash Vahidi, November 2002, Chalmers/Sweden

		</pre>

		@author Arash Vahidi
*/

// package se.chalmers.s2.jbuddy;
class JBDD
{
	static
	{
		String lib = (Options.use_cudd)
					 ? "cudd"
					 : "buddy";

		try
		{
			System.loadLibrary(lib);
		}
		catch (UnsatisfiedLinkError ule)
		{
			System.err.println("DLL " + lib + " not in the path, trying user supplied directory");

			try
			{
				java.io.File f = new java.io.File(Options.extraLibPath, System.mapLibraryName(lib));

				System.load(f.getAbsolutePath());
			}
			catch (UnsatisfiedLinkError ule2)
			{
				System.err.println("Unable to load the DLL: " + ule2);
				System.err.println("If you are using linux, this may be a GCC 3.x vs GCC 2.x issue :(");

				throw ule2;
			}
		}
	}

	/** Default value used in the constructors
			*/
	private static final int DEFAULT_VAR_COUNT = 64,
							 DEFAULT_NODE_COUNT = 3 * 10000;    //

	/** Supported dyanmic reorering method */
	public static final int REORDER_NONE = 0, REORDER_WIN2 = 1,
							REORDER_WIN3 = 2, REORDER_SIFT = 3,
							REORDER_RANDOM = 4;

	/** BDD trees for constant one and constant zero */
	public int one, zero;

	/** Create the BDD object using the default number
		if variables and node-count     */
	public JBDD()
	{
		this(DEFAULT_VAR_COUNT, DEFAULT_NODE_COUNT);
	}

	/** Create the BDD object using the default node count and the given number of variables
		@param vars maximum number of BDD variables to be used */
	public JBDD(int vars)
	{
		this(vars, DEFAULT_NODE_COUNT);
	}

	/** Create the BDD object using the given number of variables and node count
		@param vars maximum number of BDD variables to be used
		@param nodes the initial number of BDD nodes
		The BuDDy manual suggest the follwoing values for inital node count:
		<pre>
		small test examples      1000 nodes
		small examples           10000
		medium sized examples    100000
		large examples           1000000
		</pre>

	*/
	public JBDD(int vars, int nodes)
	{
		init(vars, nodes);

		one = getOne();
		zero = getZero();
	}

	/** get the BDD constant 1
		@return BDD for constant 1*/
	public native int getOne();

	/** get the BDD constant 0
@return BDD for constant 0 */
	public native int getZero();

	/** create a new BDD variable
@return new BDD variable
*/
	public native int createBDD();

	/** get number of BDD variables currently in the system
@return number of existing BDD variables
*/
	public native int getVarCount();

	/** get the BDD at the given index (ranked after call the crateBDD)
						@return BDD_index
		*/
	public native int getBDD(int index);

	/** internal init. should not be called from userspace */
	private native void init(int vars, int million_nodes);

	/** close the BDD package and cleanup.<b>Must</b> be called
	  beforenew JBDD objects can be created (the JBDD is a singleton)
	  */
	public native void kill();

	/** increase the reference count of a BDD
		@param bdd The BDD node to be referenced
		@return The same BDD node, after it has been refrensed
		*/
	public native int ref(int bdd);

	/** decrease the reference count of a BDD.<br>
	Note: this corresponds to the BuDDy call <i>bdd_delref</i>
	and CUDD call <i>Cudd_recursiveDeref</i>.
			@param bdd The BDD node to be de-referenced
	*/
	public native void deref(int bdd);

	/** decrease the reference count of a BDD.
	This is guaranteed to only change the reference count of the top node of the
	BDD and no recursive calls are done.<br>
			Note: this corresponds to the BuDDy call <i>bdd_delref</i>
			and CUDD call <i>Cudd_deref</i>.
			@param bdd The BDD node to be de-referenced
			*/
	public native void localDeref(int bdd);

	/**
	BDD AND operation.<br>Note: the returned result is already referenced.
	@param bdd1 first operand
	@param bdd2 second operand
	@return bdd1 AND bdd2
	*/
	public native int and(int bdd1, int bdd2);

	/**
	BDD OR operation.<br>Note: the returned result is already referenced.
	@param bdd1 first operand
	@param bdd2 second operand
	@return bdd1 OR bdd2
	*/
	public native int or(int bdd1, int bdd2);

	/**
	Cumulative BDD AND operation.<br>
	Note: the returned result is already referenced. Furthermore, bdd is
	<b>de-refrenced</b> once. Compare to bdd &= and;
	@param bdd first operand
	@param and second operand
	@return bdd AND and
	*/
	public native int andTo(int bdd, int and);

	/**
	Cumulative BDD OR operation.<br>
	Note: the returned result is already referenced. Furthermore, bdd is
	<b>de-refrenced</b> once. Compare to bdd |= or;
	@param bdd first operand
	@param or second operand
	@return bdd OR or
	*/
	public native int orTo(int bdd, int or);

	/**
	BDD NOT AND operation.<br>Note: the returned result is already referenced.
	@param bdd1 first operand
	@param bdd2 second operand
	@return NOT (bdd1 AND bdd2)
	*/
	public native int nand(int bdd1, int bdd2);

	/**
	BDD NOT OR operation.<br>Note: the returned result is already referenced.
	@param bdd1 first operand
	@param bdd2 second operand
	@return NOT (bdd1 OR bdd2)
	*/
	public native int nor(int bdd1, int bdd2);

	/**
	BDD EXCLUSIVE OR operation.<br>Note: the returned result is already referenced.
	@param bdd1 first operand
	@param bdd2 second operand
	@return bdd1 XOR bdd2
	*/
	public native int xor(int bdd1, int bdd2);

	/**
	BDD If-Then-Elseoperation.<br>Note: the returned result is already referenced.
	@return efficently computes (bdd_if AND bdd_then) OR (NOT bdd_if and bdd_else)
*/
	public native int ite(int bdd_if, int bdd_then, int bdd_else);

	/**
						BDD IMPLICATION operation.<br>Note: the returned result is already referenced.
						@param bdd1 first operand
						@param bdd2 second operand
						@return bdd1 -> bdd2  (which equals (NOT bdd1) OR bdd2)
	*/
	public native int imp(int bdd1, int bdd2);

	/**
						BDD BI-IMPLICATION operation.<br>Note: the returned result is already referenced.
						@param bdd1 first operand
						@param bdd2 second operand
						@return bdd1 == bdd2  (which equals NOT(bdd1 XOR bdd2))
	*/
	public native int biimp(int bdd1, int bdd2);    //  bdd1 <-> bdd2

	/**
						BDD unary negation operation.<br>Note: the returned result is already referenced.<br>
						This is an O(c) operation in CUDD due to its complemented edges!
						@param bdd BDD to negate
						@return NOT bdd
	*/
	public native int not(int bdd);

	/**
			BDD existential quantification. You first need to create a <i>cube</i>
			of variables to be quantified which is basically the all-true minterm. for example
			cube of {v1,v2,v3} is v1 & v2 & v3.
			Note: the returned result is already referenced.<br>
			@param quant_cube a cube of the variables to be quantified.
			@param bdd BDD to be quantified
			@return E quant_cube. bdd(quant_cube)
*/
	public native int exists(int bdd, int quant_cube);

	/**
	BDD universal quantification. You first need to create a <i>cube</i>
	of variables to be quantified which is basically the all-true minterm. for example
	cube of {v1,v2,v3} is v1 & v2 & v3.
	Note: the returned result is already referenced.<br>
	@param quant_cube a cube of the variables to be quantified.
	@param bdd BDD to be quantified
	@return A quant_cube. bdd(quant_cube)
	*/
	public native int forall(int bdd, int quant_cube);

	/**
	BDD relation-product: quantification and product computation in one pass.
	This is a very efficient way of doing a common operation.
	See Clarke papper "10^20and beyond..." for more info.
	Note: the returned result is already referenced.<br>
	@param bdd_left a BDD
	@param bdd_right a BDD
	@param quant_cube the variables to be quantified.
	@return E quant_cube. bdd_left AND bdd_right
	*/
	public native int relProd(int bdd_left, int bdd_right, int quant_cube);    // see Clarke

	/**
	restrict a set of variables to constant values.
	The var is a single minterm of a set of variables with either negative
	or positive polarity (e.g. v1 & v2 & not v3). For each BDD variable <i>v</i> in
	if the polarity of positive, then r is restricted to <i>v</i> otherwise to
	</i>NOT v</i>. See Coudert and Madres paper "A Unified Framework for..." for more info.<br>
	Note: the returned result is already referenced.<br>
	@param r BDD to be restricted
	@param var the minterm BDD
	*/
	public native int restrict(int r, int var);

	/**
	compute the <i>generalized cofactor</i> of <i>f</i> with respect to <i>c</i>.<br>
	See Coudert and Madres paper "A Unified Framework for..." for more info.<br>
	Note: the returned result is already referenced.<br>
	@param r BDD to be restricted
	@param c the minterm BDD
	@param quant_cube the variables to be quantified.
	*/
	public native int constrain(int f, int c);

	//public native int simplify(int f, int d);  // simplify f restricting it to domain d

	/** create a positive cube of the variables in vars.
			@param vars list of considered BDD variables
			@param size the size of the list
			@param offset where to start in the list
			@return vars[offset] & vars[offset+1] & ... & vars[offset+size]
			*/
	public native int makeSet(int[] vars, int size, int offset);    // conjuction (cube)

	/** create a positive cube of the variables in vars.
			@param vars list of considered BDD variables
			@param size the size of the list
			@return vars[o] & vars[1] & ... & vars[offset+size]
			*/
	public native int makeSet(int[] vars, int size);    // conjuction (cube)

	/** creates a pair for permutation given to DISJOINT list of variables.<br>
	creates a function
	<pre>f: BDD variable -> BDD variable</pre>
	such that
	<pre>f(vars_old[i]) = vars_new[i].</pre>
	@param vars_old list of variables to be replaced
	@param vars_new list of variables to be replaced with
	@return the pair, which is the function f discoursed above.
	*/
	public native int createPair(int[] vars_old, int[] vars_new);

	/** remove and free the memory occupied  by this permutation pair
	@param pair a permuation pair created with createPair
	*/
	public native void deletePair(int pair);

	/** replace/permute the variables in a BDD a
	according to the given pair. The results is, as always, already referenced
	@param bdd the BDD to be changed
	@param pair the variable-pair created with createPair.
	@return bdd [x/f(x)] where f(x) is the permutation function given by pair
	*/
	public native int replace(int bdd, int pair);

	/** prints a <i>pair</i> created with createPair */
	public native void showPair(int pair);

	/** BDD support set as a cube (BDD conjuction) */
	public native int support(int bdd);

	/** number of nodes in this bdd tree */
	public native int nodeCount(int bdd);

	/** get one minterm [satisfying variable assignment]  unless bdd equals 0*/
	public native int satOne(int bdd);

	/** get the number of minterms.<br>Note: this function is still under development*/
	public native double satCount(int bdd);

	/**
			get the number of minterms.<br>Note: this function is still under development
			@return Math.pow(2,num_vars) * satCount(bdd) */
	public native double satCount(int bdd, int num_vars);

	/** invoke garbage collection */
	public native void gc();

	/** print the BDD as a Graphviz DOT model to stdout */
	public native void printDot(int bdd);

	/** print the BDD as a Graphviz DOT model to the given file */
	public native void printDot(int bdd, String file);

	/** print the BDD minterms to stdout */
	public native void printSet(int bdd);

	/** print the BDD in the native package representation to stdout */
	public native void print(int bdd);

	/** INTERNAL: print  package statistics to stdout */
	public native void printStats();

	/** INTERNAL: returns false if something is wrong */
	public native boolean checkPackage();

	/** INTERNAL: debug the BDD package */
	public native void debugPackage();

	/** INTERNAL: debug the bdd in the BDD package */
	public native boolean debugBDD(int bdd);

	/** INTERNAL: get the <i>index</i> of a bdd [variable] */
	public native int internal_index(int bdd);

	/** INTERNAL: get the number of <i>refrences</i> to a bdd variable */
	public native int internal_refcount(int bdd);

	/** INTERNAL: returns true if the BDD is either 0 or 1 */
	public native boolean internal_isconst(int bdd);

	/** INTERNAL: if BDD is either 0 or 1, it returns that value */
	public native boolean internal_constvalue(int bdd);

	/** INTERNAL: returns true if the BDD is complemented */
	public native boolean internal_iscomplemented(int bdd);

	/** INTERNAL: get the THEN-node of a BDD*/
	public native int internal_then(int bdd);

	/** INTERNAL: get the ELSE-node of a BDD*/
	public native int internal_else(int bdd);

	/** set dynamic reordering heuristic
			* @param method the heuristic, see the REORDER_* constants */
	public native void reorder_setMethod(int method);

	/** start dyanamic reordering */
	public native void reorder_now();

	/** enable/disable automatic dyanamic reordering */
	public native void reorder_enableDyanamic(boolean enable);

	/** create a variable block, between the first and last variable (USE INDEX)
	 * @param fix_group decides whether to allow reordering inside group or fix to curren ordering */
	public native void reorder_createVariableGroup(int first, int last, boolean fix_group);

	// TEST BED
	public static void main(String[] args)
	{
		JBDD jbdd = new JBDD(10, 1);
		int v1 = jbdd.createBDD();
		int v2 = jbdd.createBDD();
		int v3 = jbdd.createBDD();
		int v4 = jbdd.createBDD();

		Options.out.println("v1.refs = " + jbdd.internal_refcount(v1));
		jbdd.ref(v1);
		Options.out.println("v1.refs = " + jbdd.internal_refcount(v1));
		jbdd.deref(v1);
		Options.out.println("v1.refs = " + jbdd.internal_refcount(v1));
		Options.out.print("v1 = ");
		jbdd.printSet(v1);
		Options.out.print("v2 = ");
		jbdd.printSet(v2);
		Options.out.print("v3 = ");
		jbdd.printSet(v3);
		Options.out.print("v4 = ");
		jbdd.printSet(v4);
		jbdd.gc();
		jbdd.checkPackage();

		int v1andv2 = jbdd.and(v1, v2);
		int v1orv2 = jbdd.or(v1, v2);

		Options.out.print("v1 & v2 = ");
		jbdd.printSet(v1andv2);
		Options.out.print("v1 | v2 = ");
		jbdd.printSet(v1orv2);
		jbdd.gc();
		jbdd.checkPackage();

		int[] set = new int[2];

		set[0] = v1;
		set[1] = 1;

		int x = jbdd.makeSet(set, 2);

		Options.out.print("X = ");
		jbdd.printSet(x);
		jbdd.debugBDD(x);
		jbdd.debugBDD(0);
		jbdd.debugBDD(1);

		// jbdd.ref(x);
		jbdd.gc();
		jbdd.checkPackage();
		jbdd.kill();
	}
}
