/*************************** MultiSet.java **********************/
// Implements a multiset over the given Set by storing
// arrays of elements
// Note: Java sets differ from C++sets in that Java sets only
// allow fast existence check bot linear access, whereas C++
// sets allow fast access (and this also acts as existence check,
// if the value does not exist, end() is returned)
//
// Note, we cheat here by adding MultiSet into the jgl package
// We need access to default accessible stuff for implementing insert
package com.objectspace.jgl;

import java.util.*;

import com.objectspace.jgl.OrderedSet;
import com.objectspace.jgl.OrderedSetIterator;
// Note a difference with OrderedSet/Map and java std Set/Map
// Ordered* have no first(), contains() and so on, but rely on iterators
// THis makes contains() a more costly operation
// Thus, OrderedSet allows you to first find, then access
import com.objectspace.jgl.BinaryPredicate;
// Note another difference. OrderedSet by default sorts based on the hashcode
// java.util.TreeSet orders on the "natural ordering", that is, compareTo
import com.objectspace.jgl.Range;
import com.objectspace.jgl.Tree;

public class MultiSet
	extends OrderedSet
{
	static class ComparatorWrapper
		implements BinaryPredicate
	{
		Comparator comparator;
		
		ComparatorWrapper(Comparator comparator)
		{
			this.comparator = comparator;
		}
		
		public boolean execute(Object obj1, Object obj2)
		{
			return comparator.compare(obj1, obj2) < 0;
		}
	}
	
	public MultiSet()
	{
		super(true);
	}
	
	public MultiSet(Comparator comp)
	{
		super(new ComparatorWrapper(comp), true);
	}
	
	public MultiSet(BinaryPredicate binpred)
	{
		super(binpred, true);
	}
	
	public boolean contains(Object obj)
	{
		OrderedSetIterator it = find(obj);
		return !it.atEnd();
	}
	
	public Object first()
	{
		OrderedSetIterator it = begin(); 
		return it.get();
	}
	
	public OrderedSetIterator insert(Object object)
	{
		// copied from OrderedSet::add
	    if ( object == null )
			throw new NullPointerException();

		Tree.InsertResult result = myTree.insert( object );
		OrderedSetIterator it = new OrderedSetIterator(myTree, result.node, this);
		return it;		
	}
	
	// For testing only
	public static void main(String args[])
	{
		class Tuple
		{
			private int x;	// sort on x
			private int y;	// vary y
		
			Tuple(int x, int y)
			{
				this.x = x;
				this.y = y;
			}
			
			public int hashCode()
			{
				return x;
			}
			
			public String toString()
			{
				return "<" + Integer.toString(x) + ", " + Integer.toString(y) + ">";
			}
		}
		
		MultiSet ms = new MultiSet();
		Tuple t11 = new Tuple(1, 1);	ms.add(t11);
		Tuple t12 = new Tuple(1, 2);	ms.add(t12);
		Tuple t22 = new Tuple(2, 2);	ms.add(t22);
		Tuple t13 = new Tuple(1, 3);	ms.add(t13);
		

		System.out.println(ms.toString());
		
		Tuple t = (Tuple)ms.first();
		System.out.println("Tuple t = " + t.toString());
		
		// insert t31 and t32 and see that they are found correctly
		Tuple t31 = new Tuple(3, 1);
		OrderedSetIterator it31 = ms.insert(t31);
		Tuple t32 = new Tuple(3, 2);
		OrderedSetIterator it32 = ms.insert(t32);
		System.out.println("it31.get(): " + it31.get().toString());
		System.out.println("it32.get(): " + it32.get().toString());
	
		Range range = ms.equalRange(t11);
		for(OrderedSetIterator it = (OrderedSetIterator)range.begin; !it.equals(range.end); it.advance())
		{
			System.out.println(((Tuple)it.get()).toString());
		}
		
		OrderedSetIterator osi = ms.find(t12);
		System.out.println("found: " + ((Tuple)osi.get()).toString());
		
		osi.advance();
		System.out.println("advanced to: " + ((Tuple)osi.get()).toString());
		
		ms.remove(osi);	// Remove this specific element <1,2>
		System.out.println(ms.toString());
		
		ms.remove((Tuple)osi.get());	// Removes all elements that "Look like this one"
		System.out.println(ms.toString());
		
	}

}