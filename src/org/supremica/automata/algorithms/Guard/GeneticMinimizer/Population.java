/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.supremica.automata.algorithms.Guard.GeneticMinimizer;

import java.util.TreeSet;

/**
 *
 * @author sajed
 */
public class Population
{
    private TreeSet<Chromosome> chromosomes;

    public Population(TreeSet<Chromosome> chromosomes)
    {
        this.chromosomes = chromosomes;
    }

    public TreeSet<Chromosome> getChromosomes()
    {
        return chromosomes;
    }
}
