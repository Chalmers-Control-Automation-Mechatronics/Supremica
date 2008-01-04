/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.supremica.automata.SAT.expr;

/**
 *
 * @author voronov
 */
public class Domain {
        private int size;
        private static Domain binaryDomain;
        public Domain(int i) {
            size = i;
        }
        public int size(){
            return size;
        }
        public int significantBits(){            
            if(size==0)
                return 0;// throw exception
            return (int)java.lang.Math.ceil(
                    java.lang.Math.log(size)/java.lang.Math.log(2));
        }
        public static Domain BINARY()
        {
            if(binaryDomain == null)
                binaryDomain = new Domain(2);
            return binaryDomain;
        }

}
