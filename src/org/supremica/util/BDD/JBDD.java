package org.supremica.util.BDD;

// Java Native Interface for BuDDy BDD package
// Arash Vahidi, Chalmers/Sweden - 2001-2002
// package se.chalmers.s2.jbuddy;
class JBDD
{
	static
	{
		if (Options.use_cudd)
		{
			System.loadLibrary("cudd");
		}
		else
		{
			System.loadLibrary("buddy");
		}
	}

	private static final int DEFAULT_VAR_COUNT = 64, DEFAULT_NODE_COUNT = 2;    // in million nodes
	public int one, zero;

	public JBDD()
	{
		this(DEFAULT_VAR_COUNT, DEFAULT_NODE_COUNT);
	}

	public JBDD(int vars)
	{
		this(vars, DEFAULT_NODE_COUNT);
	}

	public JBDD(int vars, int nodes)
	{
		init(vars, nodes);

		one = getOne();
		zero = getZero();
	}

	// basic BDD creation
	public native int getOne();

	public native int getZero();

	public native int createBDD();

	public native int getVarCount();

	public native int getBDD(int index);

	// init/kill (init called by the constructer)
	private native void init(int vars, int million_nodes);

	public native void kill();

	// ref counting
	public native void ref(int bdd);

	public native void deref(int bdd);

	public native void recursiveDeref(int bdd);

	// BDD operations:
	public native int and(int bdd1, int bdd2);

	public native int or(int bdd1, int bdd2);

	// bdd &= and;   plus referensing (old bdd -1, new bdd +1)
	public native int andTo(int bdd, int and);

	public native int orTo(int bdd, int or);

	public native int nand(int bdd1, int bdd2);

	public native int nor(int bdd1, int bdd2);

	public native int xor(int bdd1, int bdd2);

	// (if & then) or (not if & else)
	public native int ite(int bdd_if, int bdd_then, int bdd_else);

	public native int imp(int bdd1, int bdd2);    // bdd1 -> bdd2

	public native int biimp(int bdd1, int bdd2);    // bdd1 <-> bdd2

	public native int not(int bdd);

	// other BDD operations
	public native int exists(int bdd, int quant_cube);

	public native int forall(int bdd, int quant_cube);

	public native int relProd(int bdd_left, int bdd_right, int quant_cube);    // see Clarke

	public native int restrict(int r, int var);    // the restrict operation

	public native int constrain(int f, int c);    // general cofactorr of f with respect to c

	// public native int simplify(int f, int d);  // simplify f restricting it to domain d
	// note: in opposite to BuDDy, we use REAL variables here (in BuDDy, vars
	// contains index of the variables!)
	public native int makeSet(int[] vars, int size, int offset);    // conjuction (cube)

	public native int makeSet(int[] vars, int size);    // conjuction (cube)

	// replace operations
	public native int createPair(int[] vars_old, int[] vars_new);

	public native void deletePair(int pair);

	public native int replace(int bdd, int pair);

	public native void showPair(int pair);

	public native int support(int bdd);    // BDD support set as a cube (BDD conjuction)

	public native int nodeCount(int bdd);    // # of nodes in this bdd tree

	public native int satOne(int bdd);    // get 1 SAT unless bdd == bdd_false

	public native double satCount(int bdd);    // SAT count :)

	public native double satCount(int bdd, int num_vars);    // return Math.pow(2,num_vars) * satCount(bdd);

	// garbage collections
	public native void gc();

	// print and debugging
	public native void printDot(int bdd);

	public native void printDot(int bdd, String file);

	public native void printSet(int bdd);    // truth assigments

	public native void print(int bdd);

	// Debugging BDD package itself
	public native void printStats();    // package stats

	public native boolean checkPackage();    // returns false if something is wrong

	public native void debugPackage();    // debug the BDD Packages

	public native boolean debugBDD(int bdd);    // debug cudd

	// low-level and internal stuffs
	public native int internal_index(int bdd);

	public native int internal_refcount(int bdd);

	public native boolean internal_isconst(int bdd);

	public native boolean internal_constvalue(int bdd);

	public native boolean internal_iscomplemented(int bdd);

	public native int internal_then(int bdd);

	public native int internal_else(int bdd);

	// TEST BED
	public static void main(String[] args)
	{
		JBDD jbdd = new JBDD(10, 1);
		int v1 = jbdd.createBDD();
		int v2 = jbdd.createBDD();
		int v3 = jbdd.createBDD();
		int v4 = jbdd.createBDD();

		System.out.print("v1 = ");
		jbdd.printSet(v1);
		System.out.print("v2 = ");
		jbdd.printSet(v2);
		System.out.print("v3 = ");
		jbdd.printSet(v3);
		System.out.print("v4 = ");
		jbdd.printSet(v4);

		int v1andv2 = jbdd.and(v1, v2);
		int v1orv2 = jbdd.or(v1, v2);

		System.out.print("v1 & v2 = ");
		jbdd.printSet(v1andv2);
		System.out.print("v1 | v2 = ");
		jbdd.printSet(v1orv2);

		int f = jbdd.exists(v1andv2, v1);
		int g = jbdd.forall(v1andv2, v2);

		System.out.print("E v1. v1 & v2 = ");
		jbdd.printSet(f);
		System.out.print("A v2. v1 & v2 = ");
		jbdd.printSet(g);
		jbdd.debugBDD(v1);
		jbdd.debugBDD(v2);
		jbdd.debugBDD(f);

		// test replace
		int[] s = new int[2];
		int[] sp = new int[2];

		s[0] = v1;
		s[1] = v2;
		sp[0] = v3;
		sp[1] = v4;

		int s2sp = jbdd.createPair(s, sp);
		int rep = jbdd.replace(v1andv2, s2sp);

		System.out.print("S -> S' : v1 & v2 = ");
		jbdd.printSet(rep);
		jbdd.deletePair(s2sp);
		jbdd.kill();
	}
}
