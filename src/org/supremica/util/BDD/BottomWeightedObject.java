
package org.supremica.util.BDD;

// Bottom object, what more can i say?
// yeah, this is a Timbuktu-style "the botten is nådd"-object ...


public class BottomWeightedObject implements WeightedObject {
	public Object object() { return this; }
	public double weight() { return Double.NEGATIVE_INFINITY; }
}