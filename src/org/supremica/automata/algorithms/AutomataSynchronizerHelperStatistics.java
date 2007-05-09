package org.supremica.automata.algorithms;

public class AutomataSynchronizerHelperStatistics
{
    public long nbrOfAddedStates = 0;
    public long nbrOfCheckedStates = 0;
    public long nbrOfForbiddenStates = 0;
    public long nbrOfDeadlockedStates = 0;
    
    public AutomataSynchronizerHelperStatistics()
    {}
    
    public long getNumberOfExaminedTransitions()
    {
        return nbrOfCheckedStates-1; // The first checked state was the initial state, no transition!
    }
    
    public long getNumberOfReachableStates()
    {
        return nbrOfAddedStates;
    }
    
    public long getNumberOfCheckedStates()
    {
        return nbrOfCheckedStates;
    }
    
    public long getNumberOfForbiddenStates()
    {
        return nbrOfForbiddenStates;
    }
    
    public long getNumberOfDeadlockedStates()
    {
        return nbrOfDeadlockedStates;
    }
    
    public void setNumberOfReachableStates(long n)
    {
        nbrOfAddedStates = n;
    }
    
    public void setNumberOfCheckedStates(long n)
    {
        nbrOfCheckedStates = n;
    }
    
    public void setNumberOfForbiddenStates(long n)
    {
        nbrOfForbiddenStates = n;
    }
    
    public void setNumberOfDeadlockedStates(long n)
    {
        nbrOfDeadlockedStates = n;
    }

    public String toString()
    {
        return ("Operation statistics:\n\t" + (getNumberOfExaminedTransitions()) + " transitions were examined.\n\t" + getNumberOfReachableStates() + " reachable states were found.\n\t" + getNumberOfForbiddenStates() + " forbidden states were found.\n\t" + getNumberOfDeadlockedStates() + " deadlocked states were found.");
    }
    
    public String getStatisticsLineLaTeX()
    {
        return getNumberOfReachableStates() + " & " + getNumberOfExaminedTransitions();        
    }
}