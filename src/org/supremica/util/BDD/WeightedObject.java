package org.supremica.util.BDD;

/*
public class WeightedObject {
		private Object o;
		private double w;
		public WeightedObject(Object object, double weight) {
				this.o = object;
				this.w = weight;
		}

		public Object object() { return o; }
		public boolean less(WeightedObject x) { return w < x.w; }
}
*/
public interface WeightedObject
{
	public Object object();

	public double weight();
}
