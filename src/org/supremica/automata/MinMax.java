package org.supremica.automata;

/**
 *
 * @author sajed
 */
public class MinMax
{
    private int min;
    private int max;
    
    public MinMax()
    {

    }
    public MinMax(int min, int max)
    {
        this.min = min;
        this.max = max;
    }

    public void setMin(int min)
    {
        this.min = min;
    }

    public void setMax(int max)
    {
        this.max = max;
    }

    public int getMin()
    {
        return min;
    }

    public int getMax()
    {
        return max;
    }

}