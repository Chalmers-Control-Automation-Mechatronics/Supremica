

package org.supremica.util.BDD;

/**
 * information needed to decide characteristic of the dependency sets
 * in a disjunctive partitioning.
 *
 * if not disjunctive, this data should be set to monolithic characteristic
 */

public class DependencyData {
	public double min;
	public double max;
	public double avg;
	public int size;

	/** its not disjunctive, no dependency groups exist! */
	public void setNeutral() {
		min = max = avg = 1.0;
		size = 1;
	}

	/** compute from a set of clusters */
	public void fromClusters(Cluster [] clusters, int size) {

		this.size = size;

		if(size == 0) {
			min = max = avg = 0.0;
			return;
		}


		min = Integer.MAX_VALUE;
		max = Integer.MIN_VALUE;
		avg = 0; // use it as sum


		for(int i = 0; i < size; i++) {
			int count = clusters[i].getDependencySize();
			min = Math.min(min, count);
			max = Math.max(max, count);
			avg += count;
		}

		avg /= ( size * size);
		min /= size;
		max /= size;
	}

	// -----------------------------------------------------
		/** compute from a set of PerEventTransition:s	*/
		public void fromPerEventTransitions(PerEventTransition [] pet, int size, boolean is_forward) {

			this.size = size;

			if(size == 0) {
				min = max = avg = 0.0;
				return;
			}


			min = Integer.MAX_VALUE;
			max = Integer.MIN_VALUE;
			avg = 0; // use it as sum


			for(int i = 0; i < size; i++) {
				int count = is_forward ? pet[i].getNumberOfNextEvents() : pet[i].getNumberOfPrevEvents();
				min = Math.min(min, count);
				max = Math.max(max, count);
				avg += count;
			}

			avg /= ( size * size);
			min /= size;
			max /= size;
		}

	// -----------------------------------------------------
	public String toString() {
		return " " +
			Math.round(min * 100) / 100.0 + "/" +
			Math.round(max * 100) / 100.0 + "/" +
			Math.round(avg * 100) / 100.0;
	}
}