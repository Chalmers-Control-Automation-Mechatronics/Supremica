package org.supremica.external.fbd2smv.isagrafReader;

public class ARC
{
    private int sourceIndex;
    private int sourceOutputNumber;
    private int targetIndex;
    private int targetInputNumber;
    private boolean invert;

    public ARC(int sourceIndex, int sourceOutputNumber, int targetIndex, int targetInputNumber)
	{
		this.sourceIndex = sourceIndex;
		this.sourceOutputNumber = sourceOutputNumber;
		this.targetIndex = targetIndex;
		this.targetInputNumber = targetInputNumber;
		this.invert = false;
	}

    public ARC(int sourceIndex, int sourceOutputNumber, int targetIndex, int targetInputNumber, boolean invert)
	{
		this.sourceIndex = sourceIndex;
		this.sourceOutputNumber = sourceOutputNumber;
		this.targetIndex = targetIndex;
		this.targetInputNumber = targetInputNumber;
		this.invert = invert;
	}

    public int getSourceIndex()
    {
	return sourceIndex;
    }

    public int getSourceOutputNumber()
    {
	return sourceOutputNumber;
    }

    public int getTargetIndex()
    {
	return targetIndex;
    }

    public int getTargetInputNumber()
    {
	return targetInputNumber;
    }

    public boolean getInvert()
    {
	return invert;
    }

    public String toString()
    {
	return "Source: "; 
    }
}
