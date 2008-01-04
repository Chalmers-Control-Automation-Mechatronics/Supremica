/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.supremica.automata.SAT.expr;

/**
 *
 * @author voronov
 */
public class Variable {
        public String Name;
        public Domain domain;
        public int id;
        public Variable(String iName, Domain iDomain, int iId){
            if(iDomain==null)
                throw new IllegalArgumentException("domain can't be null");
            Name   = iName;
            domain = iDomain;
            id     = iId;            
        }
        public int significantBits()
        {
            return domain.significantBits();
        }
}
