/*
 * MinMaxDC.java
 *
 * Created on July 4, 2008, 3:20 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.supremica.automata.algorithms.Guard;

import java.util.TreeSet;
import java.util.Set;

/**
 *
 * @author Administrator
 */
public class MinMaxDC {
    
    private TreeSet<Integer> minTerm;
    private TreeSet<Integer> maxTerm;
    private TreeSet<Integer> DCs;
    /** Creates a new instance of MinMaxDC */
    public MinMaxDC(TreeSet<Integer> minTerm, TreeSet<Integer> maxTerm, TreeSet<Integer> DCs) {
        this.minTerm = minTerm;
        this.maxTerm = maxTerm;
        this.DCs = DCs;
    }
    
    public MinMaxDC() {
        this.minTerm = null;
        this.maxTerm = null;
        this.DCs = null;
    }
    
    public TreeSet<Integer> getMinTerm()
    {
        return minTerm;
    }
    
    public TreeSet<Integer> getMaxTerm()
    {
        return maxTerm;
    }
        
    public TreeSet<Integer> getDCs()
    {
        return DCs;
    }
    
    public void setMinTerm(TreeSet<Integer> minTerm)
    {
        this.minTerm = minTerm;
    }
    
    public void setMaxTerm(TreeSet<Integer> maxTerm)
    {
        this.maxTerm = maxTerm;
    }
    
    public void setDCs(TreeSet<Integer> DCs)
    {
        this.DCs = DCs;
    }
    
    
}
