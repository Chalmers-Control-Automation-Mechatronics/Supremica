package org.supremica.util.BDD;

/**
 * This class will, given a set of BDDs, check if their ref-count has CHANGED.
 * This is probably ok in the middle of an algorithm, but if a check() succeeds
 * before calling algorithm A, but failes after A, then A has done something funky
 * to the refrences. (This is not always true, A maybe computes something else that
 *  happens to be equal to one of our BDDs and thus its refcount will get changed)
 *
 */
class BDDRefCheck
{
	private IntArray bdds;
	private IntArray refs;
	private String name;
	private JBDD manager;
	private int id;
	private static int id_current = 0;

	/**
	 *
	 * create a BDDRefCheck.
	 *
	 * @param name the name to identify this series of refcount-checks
	 *
	 */
	public BDDRefCheck(JBDD manager, String name)
	{
		this.manager = manager;
		this.name = name;
		this.bdds = new IntArray();
		this.refs = new IntArray();
		this.id = id_current++;
	}

	/** Add a new BDD, book its current refcount */
	public void add(int bdd)
	{
		bdds.add(bdd);
		refs.add(manager.internal_refcount(bdd));
	}

	/** Check all the refcounts */
	private void check()
	{
		check(null);
	}

	/** Check all the refcounts */
	private void check(String place)
	{
		int size = bdds.getSize();

		for (int i = 0; i < size; i++)
		{
			int bdd = bdds.get(i);
			int ref = refs.get(i);
			int new_ref = manager.internal_refcount(bdd);

			if (new_ref != ref)
			{
				System.err.println("*** RefCheck-" + id + " FAILED " + ((place == null)
																		? ""
																		: "at " + place + " ") + "for " + name + ": BDD #" + (i + 1) + " old_ref=" + ref + ", new_ref=" + new_ref);
			}
		}
	}
}
