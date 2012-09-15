/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.supremica.automata.algorithms.Guard.GeneticMinimizer;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Sajed & Alexey
 */
public class Chromosome
{
    private List<String> genes;

    public Chromosome(List<String> genes)
    {
        this.genes = new ArrayList<String>(genes);
    }

    public List<String> getGenes()
    {
        return genes;
    }

    public void setGene(int index, String gene)
    {
        genes.set(index, gene);
    }

}
