package net.sourceforge.waters.gui.simulator;

public class NonDeterministicException extends Exception
{
  NonDeterministicException(final String e)
  {
    super(e);
  }
  NonDeterministicException()
  {
    super();
  }

  private static final long serialVersionUID = 6026398816181263491L;
}
