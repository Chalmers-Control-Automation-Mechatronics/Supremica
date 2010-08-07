package net.sourceforge.waters.analysis.modular;

import java.io.PrintWriter;

import net.sourceforge.waters.model.analysis.VerificationResult;

public class LoopResult extends VerificationResult
{

  LoopResult()
  {
    super();
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

  @Override
  public void print(final PrintWriter writer)
  {
    super.print(writer);
    if (mPeakNumberOfAutomata >= 0) {
      writer.println("Peak number of automata: " + mPeakNumberOfAutomata);
    }
    if (mNumberOfCompositions >= 0) {
      writer.println("Number of Compositions: " + mNumberOfCompositions);
    }
  }

  @Override
  public void printCSVHorizontal(final PrintWriter writer)
  {
    super.printCSVHorizontal(writer);
    writer.print("," + mPeakNumberOfAutomata);
    writer.print("," + mNumberOfCompositions);
  }

  @Override
  public void printCSVHorizontalHeadings(final PrintWriter writer)
  {
    super.printCSVHorizontalHeadings(writer);
    writer.print(",Peak aut");
    writer.print(",Compositions");
  }


  private int mPeakNumberOfAutomata;
  private int mNumberOfCompositions;

}
