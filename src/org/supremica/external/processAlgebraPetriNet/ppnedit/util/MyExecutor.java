package org.supremica.external.processAlgebraPetriNet.ppnedit.util;

/*
 *        Executor - Tim Tyler 2000.
 * 
 *         An Executor class
 *
 * This code has been placed in the public domain.
 * This means that you can do what you like with it.
 * Please note that this code comes with no warranty.
 *
 */

/*
 * To Do:
 *
 */

   import java.lang.Object;

import org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.celltype.base.BaseCell;
import org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.celltype.base.EditableCell;
import org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.celltype.operation.PetriProCell;
   
   public class MyExecutor implements Executor{
       
      private static BaseCell[][] tmp;
      
      private int lengthOfPermutation;
      private int lengthOfArray;
      
      private int counter;
      
      public MyExecutor(BaseCell[] data){
          lengthOfPermutation = Permutations.factorial(data.length);
          lengthOfArray = data.length;
          
          tmp = new PetriProCell[lengthOfPermutation][lengthOfArray];
          
          counter = 0;
      }
      
      public void execute(){
          if(counter < lengthOfPermutation){
              for(int i = 0; i < lengthOfArray; i++){
                  BaseCell cell = Permutations.data[i];
                  
				  if(cell instanceof PetriProCell){
				  	tmp[counter][i] = ((PetriProCell)cell).copy();
				  }
              }
              counter = counter + 1;
          }
      }
      
      public static BaseCell[][] getAllPermutations(){
           return tmp;
      }
   }
