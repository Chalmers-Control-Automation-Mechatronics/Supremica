package org.supremica.external.processAlgebraPetriNet.ppnedit.util;

/*
*	class BaseCellArray holds commomly used
*	functions for manipulation baseCell array
*
*	David Millares 2007-02-16 
*/

/*
 * To Do:
 *
 */
 
import java.util.Arrays;
import java.util.Comparator;

import org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.celltype.base.*;
import org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.celltype.operation.*;


public class BaseCellArray{
    
	/**
    *	Indata: array of BaseCell and one BaseCell
    *	Returns: array of BaseCell 
	*
	*	add the cell last in array and return the 
	*	new array of BaseCell
    */
    public static BaseCell[] add(BaseCell item, BaseCell[] array){
        if(item == null){
			return array;
		}
		
        if(array == null){
            array = new BaseCell[] {item};
        } else {
		
			for(int i = 0; i < array.length; i++){
				if(item.equals(array[i])){
					return array;	//don't add cell again
				}
			}
			
			//add new cell
            BaseCell[] tmp = array;
            array = new BaseCell[tmp.length+1];
            for(int i = 0; i < tmp.length; i++) {
                array[i] = tmp[i];
            }
            array[tmp.length] = item;
        }
        return array;
    }
    
	/**
    *	Indata: array of BaseCell and one BaseCell
    *	Returns: array of BaseCell 
	*
	*	Tries to remove cell from array and return
	*	and array whitout the cell 
    */
    public static BaseCell[] remove(BaseCell item, BaseCell[] array){
        
        BaseCell[] tmp = null;
        
        if(array != null && item != null){
       
            int index = -1;
			
            //serch for cell
            for(int i = 0; i < array.length; i++) {
                if(array[i].equals(item)){
                    index = i;
                    break;
                }
            }
            
            //if we have found one
            if(index != -1){
                tmp = array;
                array = new BaseCell[tmp.length-1];
                
                int j = 0;
                for(int i = 0; i < tmp.length; i++) {
                    if(index != i){
                        array[j] = tmp[i];
                        j = j + 1;
                    }
                }
            }
        }
		
		if(array != null && array.length == 0){
			return null;
		}
		
        return array;
    }
	
	/**
    *	Indata: array of BaseCell and two BaseCell
    *	Returns: array of BaseCell 
	*
	*	Tries to replace one BaseCell whit another BaseCell
	*	and return array
	*	 
    */
	public static BaseCell[] replace(BaseCell oldItem,
	                                 BaseCell newItem,
									 BaseCell[] array){
		if(oldItem == null ||
		   newItem == null ||
		   array == null){
				return array;
		}
		
		BaseCell[] tmp = new BaseCell[array.length];
		for(int i = 0; i < tmp.length; i++) {
			if(array[i].equals(oldItem)){
            	tmp[i] = newItem;
			}else{
				tmp[i] = array[i];
			}
		}
		return tmp;
	}
    
	/**
	*	Indata: array of BaseCell
    *	Returns: array of array of BaseCell 
	*
    *	Returns all posible permutaitions of a array contaioning
    *	BaseCells.
    */
    public static BaseCell[][] permutations(BaseCell[] data){
           
        Executor executor = new MyExecutor(data);
		  
        Permutations.permute(data, executor);
         
        return MyExecutor.getAllPermutations();
    }
    
    /**
    *	Indata: array of BaseCell
    *	Returns: array of BaseCell 
	*
    *	Sort BaseCell depending of their Id number
    *	used in permutations
    */
    public static BaseCell[] sortId(BaseCell[] array){
        
        Comparator comparator = new Comparator() {
                public int compare(Object cell1, 
                                   Object cell2){
                /*
                 * if cell1 == cell2 return 0
                 * if cell1 < cell2 return negative integer
                 * if cell1 > cell2 return positive integer
				 *
                 */
				 
                 return ((BaseCell)cell1).getId() - 
                         ((BaseCell)cell2).getId();  
                }
        };
        
        Arrays.sort(array,comparator);
        
        return array;
    }
	
	/**
    *	
	*
	*
    */
    public static boolean equal(BaseCell[] array1, BaseCell[] array2){
        
		/* test indata */
		if(array1 == null || array2 == null){
			return true;
		}
		
		/* same length */
		if(array1.length != array2.length){
			return false;
		}
		
		/* loop over all cells */
		for(int i=0; i < array1.length; i++){
			if(!array1[i].equals(array2[i])){
				return false;
			}
		}
		
		/* all cell same */
		return true;
    }
}
