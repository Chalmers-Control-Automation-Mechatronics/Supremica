package org.supremica.util;

import java.util.*;
import java.lang.reflect.Array;
/**
 * This code is beleived to be in the public domain,
 * and was found at:
 * http://www.codeproject.com/useritems/ArrayToString.asp
 */
public class ArrayHelper
{
    private ArrayHelper()
    {
    }
    
     @SuppressWarnings("unchecked")
	public static String arrayToString(Object array)
     {
         if (array == null)
         {
             return "[NULL]";
         }
         else
         {
             Object obj = null;
             if (array instanceof Hashtable)
             {
                 array = ((Hashtable)array).entrySet().toArray();
             }
             else if (array instanceof HashSet)
             {
                 array = ((HashSet)array).toArray();
             }
             else if (array instanceof Collection)
             {
                 array = ((Collection)array).toArray();
             }
             int length = Array.getLength(array);
             int lastItem = length - 1;
             StringBuffer sb = new StringBuffer("[");
             for (int i = 0; i < length; i++)
             {
                 obj = Array.get(array, i);
                 if (obj != null)
                 {
                     sb.append(obj);
                 }
                 else
                 {
                     sb.append("[NULL]");
                 }
                 if (i < lastItem)
                 {
                     sb.append(", ");
                 }
             }
             sb.append("]");
             return sb.toString();
         }
     }
}