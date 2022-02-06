

package org.supremica.automata.algorithms.Guard.GeneticMinimizer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author Sajed
 * @author Alexey
 */
public class Genetics
{
    FitnessEvaluation fitnessEval;
    Set<String> genes;
    GeneticOptions options;
    Comparator<Chromosome> comparator;
    Random random;

    public Genetics(final FitnessEvaluation fitnessEval,
                    final Set<String> genes,
                    final GeneticOptions options)
    {
        this.fitnessEval = fitnessEval;
        this.genes = genes;
        this.options = options;
        random = new Random();

        comparator = new Comparator<Chromosome>() {

            @Override
            public int compare(final Chromosome o1, final Chromosome o2) {
                final double eval1 = fitnessEval.eval(o1);
                final double eval2 = fitnessEval.eval(o2);

                if(o1.equals(o2))
                    return 0;
                else
                {
                    if(eval1 > eval2)
                        return 1;
                    else if(eval1 < eval2)
                        return -1;
                    else
                        return (o1.hashCode() > o2.hashCode()) ? 1 : -1; // Just to make the set work as a list
                }
            }
        };
    }

    Population initializePopulation(final int randomMethod)
    {
        final TreeSet<Chromosome> chromosomes = new TreeSet<Chromosome>(comparator);

        System.err.println("Start initializing population...");
        for(int i = 0; i < options.sizeOfPopulation; i++)
        {
            switch(randomMethod)
            {
                case 0:
                    chromosomes.add(getRandomChromosome());
                    break;
                case 1:
                    final Chromosome c = new Chromosome(new ArrayList<String>(genes));
                    swapChromosome(c);
                    chromosomes.add(c);
            }
            System.err.println("Chromosome "+i+" created.");
        }
        System.err.println("Initialization of population done.");

        return new Population(chromosomes);
    }

    void swapChromosome(final Chromosome chromosome)
    {
        for(int i = 0; i < chromosome.getGenes().size() / 2 ; i++)
        {
            mutationByReference(chromosome);
        }
    }

    Chromosome getRandomChromosome()
    {
        final List<String> localGenes = new ArrayList<String>(this.genes);
        final Set<String>  randomChromosome = new HashSet<String>();
        while(!localGenes.isEmpty())
        {
            final int randomIndex = random.nextInt(localGenes.size());
            randomChromosome.add(localGenes.get(randomIndex));
            localGenes.remove(randomIndex);
        }

        return new Chromosome(new ArrayList<String>(randomChromosome));
    }

    TreeSet<Chromosome> selectParents(final Population population)
    {
        final TreeSet<Chromosome> parents = new TreeSet<Chromosome>(comparator);
        int soFar = 0;

        final int top = (int)(population.getChromosomes().size()*
                                    options.percentageOfTopChromosomes);

        final Iterator<Chromosome> iterator = population.getChromosomes().iterator();
        while(iterator.hasNext() && soFar < top)
        {
            parents.add(iterator.next());
            soFar++;
        }

        return parents;
    }

    Set<Chromosome> mating(final TreeSet<Chromosome> parents)
    {
        final Set<Chromosome> children = new HashSet<Chromosome>();

        if(parents.size() % 2 != 0)
            parents.remove(parents.last());

        while(!parents.isEmpty())
            children.addAll(crossOver(parents.pollFirst(), parents.pollLast()));

        return children;
    }

    void reinsertion(final Population population, final Set<Chromosome> newChromosomes)
    {
        int soFar = 0;

        while(soFar < newChromosomes.size())
        {
            population.getChromosomes().remove(
                                        population.getChromosomes().last());
            soFar++;
        }

        population.getChromosomes().addAll(newChromosomes);

    }

    Set<Chromosome> crossOver(final Chromosome parent1, final Chromosome parent2)
    {
        final int randomIndex = random.nextInt(parent1.getGenes().size());
        final List<String>  child1 = new ArrayList<String>();
        final List<String>  child2 = new ArrayList<String>();


        for(int i = 0; i < randomIndex; i++)
        {
            child1.add(parent1.getGenes().get(i));
            child2.add(parent2.getGenes().get(i));
        }

        final List<String> remainingGenes1 = new ArrayList<String>(parent2.getGenes());
        remainingGenes1.removeAll(child1);
        child1.addAll(remainingGenes1);

        final List<String> remainingGenes2 = new ArrayList<String>(parent1.getGenes());
        remainingGenes2.removeAll(child2);
        child2.addAll(remainingGenes2);

        final Set<Chromosome> children = new HashSet<Chromosome>();
        children.add(new Chromosome(child1));
        children.add(new Chromosome(child2));

        return children;
    }

    void mutationByReference(final Chromosome chromosome)
    {
        final int firstRandomIndex = random.nextInt(chromosome.getGenes().size());
        final int secondRandomIndex = random.nextInt(chromosome.getGenes().size());
        final String firstRandomGene = chromosome.getGenes().get(firstRandomIndex);
        chromosome.setGene(firstRandomIndex,
                            chromosome.getGenes().get(secondRandomIndex));
        chromosome.setGene(secondRandomIndex, firstRandomGene);
    }

    Chromosome mutation(final Chromosome chromosome)
    {
        final Chromosome mutatedChromosome = new Chromosome(chromosome.getGenes());
        mutationByReference(mutatedChromosome);
        return mutatedChromosome;
    }

    void nextPopulation(final Population population)
    {
        final int numberOfMutations = (int) (population.getChromosomes().size() *
                                        options.percentageOfMutatedChromosomes);
        final List<Chromosome> populationList =
                        new ArrayList<Chromosome>(population.getChromosomes());
        for(int i = 0 ; i < numberOfMutations; i++)
        {
            final int randomIndex = random.nextInt(populationList.size());
            final Chromosome goingToGetMutated = populationList.get(randomIndex);
            population.getChromosomes().remove(goingToGetMutated);
            population.getChromosomes().add(mutation(goingToGetMutated));
        }

        reinsertion(population, mating(selectParents(population)));
    }

    public Chromosome runGenetics()
    {
        final Population population = initializePopulation(1);

        for(int i = 0 ; i < options.maxIterations ; i++)
        {
            if(fitnessEval.eval(population.getChromosomes().first()) == 1)
                break;

            System.err.println("Index of current population: "+i);
            System.err.println("Best chromosome is "+
                                population.getChromosomes().first().getGenes()+
                                " with fitness value "+
                    fitnessEval.eval(population.getChromosomes().first()));
            System.err.println("Individs: ");
            for(final Chromosome chromosome: population.getChromosomes())
            {
                System.err.print(fitnessEval.eval(chromosome)+ " ");
            }
            System.err.println();

            nextPopulation(population);
        }

        return population.getChromosomes().first();
    }

    public static class GeneticOptions
    {
        public int maxIterations;
        public int sizeOfPopulation;
        public double percentageOfTopChromosomes;
        public double percentageOfMutatedChromosomes;

        public GeneticOptions()
        {
            maxIterations = 10;
            sizeOfPopulation = 30;
            percentageOfTopChromosomes = 0.2;
            percentageOfMutatedChromosomes = 0.2;
        }
    }
}
