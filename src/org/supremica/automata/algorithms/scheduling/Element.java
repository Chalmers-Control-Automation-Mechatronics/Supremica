
/********************** Element.java *********************/

// Interface for what populates the open and closed lists
package org.supremica.automata.algorithms.scheduling;

import com.objectspace.jgl.OrderedSetIterator;

public interface Element
	extends Comparable
{
	int getBound();

	void setBound(int b);

	int getCost();

	void setCost(int i);

	int getDepth();

	void setDepth(int i);

	int[] getStateArray();

	int[] getTimeArray();

	void setTime(int index, int time);

	String toString();

	String timeArrayToString();

	int compareState(final Element ef);

	int compareRemainingTime(final Element ef);

	int compareCost(final Element ef);

	int compareBound(final Element ef);

	Element getParent();

	void setParent(Element ef);

	OrderedSetIterator getStateIterator();

	OrderedSetIterator getBoundIterator();

	void setStateIterator(OrderedSetIterator it);

	void setBoundIterator(OrderedSetIterator it);
}
