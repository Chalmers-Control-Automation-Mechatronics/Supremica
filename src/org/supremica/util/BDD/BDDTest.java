package org.supremica.util.BDD;

// ---------------------- NOTE : this code hasn't been rewriten to fit the new JBDD yet!!
// This class tests the BDD package for some small wired stuffs
// mostly to see if the refrence-counting is the same for both BDD packages
public class BDDTest
	extends JBDD
{
	private void save_refs(int[] bdds, int[] save)
	{
		for (int i = 0; i < bdds.length; i++)
		{
			save[i] = internal_refcount(bdds[i]);
		}

		show_refs(bdds);
	}

	private int changed(int[] bdds, int[] save)
	{
		int count = 0;

		for (int i = 0; i < bdds.length; i++)
		{
			if (save[i] != internal_refcount(bdds[i]))
			{
				count++;
			}
		}

		show_refs(bdds);

		return count;
	}

	private void show_refs(int[] bdds)
	{
		Options.out.print("BDD refs:\t");

		for (int i = 0; i < bdds.length; i++)
		{
			Options.out.print("   v" + (i + 1) + ": " + internal_refcount(bdds[i]));
		}

		Options.out.println();
	}

	public BDDTest()
		throws BDDException
	{
		int zero = getZero();
		int one = getOne();

		// check some dumb stuffs ....
		BDDAssert.bddAssert(not(zero) == one, "NOT zero != one");
		BDDAssert.bddAssert(not(one) == zero, "NOT one != zero");

		// check ref/deref for T and F
		int tmp = internal_refcount(zero);

		BDDAssert.bddAssert(tmp > 0, "default refcount for False not greater than zero");
		ref(zero);
		BDDAssert.bddAssert(tmp == internal_refcount(zero), "Ref not disabled for zero");
		localDeref(zero);
		BDDAssert.bddAssert(tmp == internal_refcount(zero), "Deef not disabled for zero");
		deref(zero);
		BDDAssert.bddAssert(tmp == internal_refcount(zero), "RecursiveDeref not disabled for zero");
		internal_refcount(one);
		BDDAssert.bddAssert(tmp > 0, "default refcount for False not greater than one");
		ref(one);
		BDDAssert.bddAssert(tmp == internal_refcount(one), "Ref not disabled for one");
		deref(one);
		BDDAssert.bddAssert(tmp == internal_refcount(one), "Deef not disabled for one");
		deref(one);
		BDDAssert.bddAssert(tmp == internal_refcount(one), "RecursiveDeref not disabled for one");

		int v1 = createBDD();
		int v2 = createBDD();

		// check default refcount for a new var
		BDDAssert.bddAssert(internal_refcount(v1) == 1, "BAD ref for v1");
		BDDAssert.bddAssert(internal_refcount(v2) == 1, "BAD ref for v2");

		// check ref/deref for operations
		int v1andv2 = and(v1, v2);

		BDDAssert.bddAssert(internal_refcount(v1andv2) == 0, "default refcount for AND not zero");
		ref(v1andv2);
		BDDAssert.bddAssert(internal_refcount(v1andv2) == 1, "refcount of refed AND not one");
		deref(v1andv2);
		BDDAssert.bddAssert(internal_refcount(v1andv2) == 0, "refcount of refed/derefed AND not zero");
		BDDAssert.bddAssert(internal_refcount(v1) == 1, "v1 refcount changed after and/ref/recursiveDeref");
		BDDAssert.bddAssert(internal_refcount(v2) == 1, "v2 refcount changed after and/ref/recursiveDeref");

		int v3 = createBDD();
		int v4 = createBDD();
		int[] list = new int[2];

		list[0] = v1;
		list[1] = v2;

		int set = makeSet(list, 2);

		// sets have default ref of 1:
		BDDAssert.bddAssert(internal_refcount(set) == 1, "set has not default refcount of one");

		// check permutation
		int[] from = new int[2];
		int[] to = new int[2];

		from[0] = v1;
		from[1] = v2;
		to[0] = v3;
		to[1] = v4;

		int pair = createPair(from, to);
		int p = replace(v1andv2, pair);

		BDDAssert.bddAssert(internal_refcount(p) == 0, "Permutation should have default refcount of zero");
		ref(p);

		int answer = and(v3, v4);

		ref(answer);
		BDDAssert.bddAssert(answer == p, "replace() returns wrong answer!");
		deref(answer);
		deref(p);
		deletePair(pair);

		int[] bdds = new int[4];
		int[] save = new int[4];

		bdds[0] = v1;
		bdds[1] = v2;
		bdds[2] = v3;
		bdds[3] = v4;
		tmp = or(v3, v1andv2);

		ref(tmp);

		int big = and(tmp, v4);

		ref(big);
		deref(tmp);
		save_refs(bdds, save);

		// testing exists:
		int e1 = exists(big, v2);

		ref(e1);

		int ch = changed(bdds, save);

		if (ch != 0)
		{
			Options.out.println("" + ch + " refs changed after Exist");
		}

		deref(e1);
		save_refs(bdds, save);

		int not1 = not(big);

		ref(not1);

		ch = changed(bdds, save);

		if (ch != 0)
		{
			Options.out.println("" + ch + " refs changed after not");
		}

		deref(not1);
		save_refs(bdds, save);
		deref(big);
		gc();
		checkPackage();
		gc();
		checkPackage();
	}

	public static void main(String[] args)
	{
		try
		{
			new BDDTest();
		}
		catch (BDDException ex)
		{
			ex.printStackTrace();
		}
	}
}
