
package org.supremica.util.BDD;

import java.util.*;

public class Workset
{

	private int [] workset;
	private int [][] dependent;
	private boolean [] remaining;
	private int size;
	private int workset_count;// sum of workset_i

	public Workset(int size, int [][]dependent)
	{
		this.size = size;
		this.dependent = dependent;
		this.workset = new int[size];



		// all automata are enabled from start
		for(int i = 0; i < size; i++) workset[i] = 1;
		this.workset_count = size;



		// this is for the exclusive stuff
		remaining = new boolean[size];
		for(int i = 0; i < size; i++) remaining[i] = true;
	}



	/** choose the next automaton (to be re-written) */
	public int pickOne() {
			/*
			// first best
			for(int i = 0; i < list.length; i++) {
				if(list[i] > 0) return i;
			}
			return -1;
			*/

			/*
			// the largest one (most affected so far)
			int max = 0, maxdex = -1;
			for(int i = 0; i < list.length; i++) {
				if(max < list[i]) {
					maxdex = i;
					max = list[i];
				}
			}
			return maxdex;
			*/

			// the smallest one (least affected so far)
			int min = 0xFFFF, mindex = -1;
			for(int i = 0; i < size; i++) {
				if(min >  workset[i] && workset[i] > 0) {
					mindex = i;
					min = workset[i];
				}
			}
			return mindex;
	}


	/** this does the same thing as pickOne, but chosses each automaton/cluster only once
	 * this is used in monotonic algorithms where each automaton is added only once
	 */
	public int pickOneExcelsuive()
	{

		int min = 0xFFFF, mindex = -1;
		for(int i = 0; i < size; i++) {
		// for(int j = 0; j < size; j++) { int i = size - j -1;
			if(remaining[i] && min >  workset[i] && workset[i] > 0) {
				mindex = i;
				min = workset[i];
			}
		}

		// mark the choosen one as taken
		if(mindex >= 0 && mindex < size) remaining[mindex] = false;

		return mindex;
	}

	/**
	 * we are done with this automaton.
	 * if changed is new, then something has changed and we should consider the
	 * affect of this by adding automata that are directly connected with our automaton
	 */
	public void advance(int automaton, boolean changed)
	{
		workset_count -= workset[automaton];
		workset[automaton] = 0;

		if(changed) {
			int count = dependent[automaton][0];
			for(int i = 0 ; i < count; i++) workset[  dependent[automaton][i + 1] ] ++;
			workset_count += count;
		}

	}

	public boolean empty()
	{
		return workset_count <= 0;
	}
}