package net.sourceforge.waters.analysis.modular;

import java.io.PrintStream;

import net.sourceforge.waters.model.analysis.VerificationResult;

public class LoopResult extends VerificationResult
{

  public LoopResult()
  {
    super();
    mPeakNumberOfAutomata = -1;
    mNumberOfCompositions = -1;
  }

  public LoopResult(final VerificationResult old)
  {
    super();
    this.setCounterExample(old.getCounterExample());
    this.setException(old.getException());
    this.setTotalNumberOfAutomata(old.getTotalNumberOfAutomata());
    this.setTotalNumberOfStates(old.getTotalNumberOfStates());
    this.setTotalNumberOfTransitions(old.getTotalNumberOfTransitions());
    this.setPeakNumberOfStates(old.getPeakNumberOfStates());
    this.setPeakNumberOfTransitions(old.getPeakNumberOfTransitions());
    this.setPeakNumberOfNodes(old.getPeakNumberOfNodes());
    mPeakNumberOfAutomata = -1;
    mNumberOfCompositions = -1;
  }

  /**
   * Gets the maximum number of automata which are composed by the analysis.
   * The peak number of automata should identify the largest number of
   * automata in an automata group. For monolithic algorithms, it will
   * be equal to the total number of automata, but for compositional algorithms
   * it may be different
   *
   * @return The peak number of states, or <CODE>-1</CODE> if unknown
   */

  public int getPeakNumberOfAutomata()
  {
    return mPeakNumberOfAutomata;
  }

  /**
   * Gets the number of Compositions used in the entire model. For monolithic
   * algorithms, this will be 0. For compositional algorithms, it will always
   * be less than the number of automata in the model, and it will always be
   * equal to or greater than one less than the peak number of automata
   *
   * @return The number of compositions, or <CODE>-1</CODE> if unknown
   */

  public int getNumberOfCompositions()
  {
    return mNumberOfCompositions;
  }

  /**
   * Specifies a value for both the peak number of automata and the total
   * number of automata constructed by the analysis.
   */
  public void setNumberOfAutomata(final int numaut)
  {
    setTotalNumberOfAutomata(numaut);
    setPeakNumberOfAutomata(numaut);
  }

  /**
   * Specifies a value for the total number of automata constructed by the
   * analysis.
   */
  public void setTotalNumberOfAutomata(final int numaut)
  {
    super.setNumberOfAutomata(numaut);
  }

  /**
   * Specifies a value for the peak number of automata constructed by the
   * analysis.
   */
  public void setPeakNumberOfAutomata(final int numaut)
  {
    mPeakNumberOfAutomata = numaut;
  }

  /**
   * Specifies a value for the peak number of automata composed by the
   * analysis.
   */
  public void setNumberOfCompositions(final int numcomp)
  {
    mNumberOfCompositions = numcomp;
  }

  public void print(final PrintStream stream)
  {
    super.print(stream);
    if (mPeakNumberOfAutomata >= 0) {
      stream.println("Peak number of automata: " + mPeakNumberOfAutomata);
    }
    if (mNumberOfCompositions >= 0) {
      stream.println("Number of Compositions: " + mNumberOfCompositions);
    }
  }

  public void printCSVHorizontal(final PrintStream stream)
  {
    super.printCSVHorizontal(stream);
    stream.print(mPeakNumberOfAutomata + ",");
    stream.print(mNumberOfCompositions + ",");
  }

  public void printCSVHorizontalHeadings(final PrintStream stream)
  {
    super.printCSVHorizontalHeadings(stream);
    stream.print("Peak aut,");
    stream.print("Compositions,");
  }

  public int mPeakNumberOfAutomata;
  public int mNumberOfCompositions;

}
