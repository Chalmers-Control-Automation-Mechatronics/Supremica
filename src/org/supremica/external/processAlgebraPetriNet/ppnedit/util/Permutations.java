package org.supremica.external.processAlgebraPetriNet.ppnedit.util;

/*
 *      Special thanks to Tim Tyler
 *        Permutations - Tim Tyler 2000.
 * 
 *         A generator of permutations.
 *
 *
 */

/*
 * To Do:
 *
 */

   import java.lang.Object;

import org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.celltype.base.BaseCell;
import org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.celltype.operation.PetriProCell;
import org.supremica.external.processAlgebraPetriNet.ppnedit.petrinet.ppn.PetriPro;
import org.supremica.external.processAlgebraPetriNet.ppnedit.util.Executor;
   
   

   public class Permutations extends Object {
      public static int size;
      public static int sizemo;
      public static int sizeo2;
      public static int max;
      public static int min;
      public static int debug = 0;
      public static int i, j;
      public static int ret_val;
      
      public static BaseCell temp;
      public static BaseCell[] data;
      
      public static int[] checklist;
      public static int position;
      public static int total_number;
      public static int iterations;
      public static int count;
   
      public static Executor executor;

	  /**
      *	Indata: array of BaseCell and an executor
      */
      public static void permute(BaseCell[] n, Executor executor) {
          
         /*
         The Alghorithm use lexical sort so the array of BaseCell
         must be orderd from lower to higer value. I use the id number of
         each BaseCell to do that
         */
		 
         size = n.length;
		 
		 data = new PetriProCell[n.length];
         for(int i = 0; i < n.length; i++){
		 
		 	if(n[i] instanceof PetriProCell){
				data[i] = ((PetriProCell)n[i]).copy();
			}else{
				PetriPro pp = new PetriPro();
				pp.setExp(n[i].getExp());
				data[i] = new PetriProCell(pp);
			}
         }
         
		 //sort the array from lower to higer number
         data = BaseCellArray.sortId(data);
      
         executor.execute();
      
         iterations = factorial(size);
         for (count = 0; count < iterations - 1; count++) {
            getNext();
            executor.execute();
         }
      }
   
      public static void getNext() {
         int i = size - 1;
      
         while (data[i-1].getId() >= data[i].getId()) 
            i = i-1;
      
         int j = size;
      
         while (data[j-1].getId() <= data[i-1].getId()) 
            j = j-1;
      
         swap(i-1, j-1);
      
         i++; 
         j = size;
      
         while (i < j) {
            swap(i-1, j-1);
            i++;
            j--;
         }
      }
   
      public static void swap(int a, int b) {
         temp = data[a];
         data[a] = data[b];
         data[b] = temp;
      }
   
      public static int factorial(int a) {
         int temp = 1;
         if (a > 1) {
            for (i = 1; i <= a; i++) {
               temp *= i;
            }
         }
      
         return temp;
      }
   }
