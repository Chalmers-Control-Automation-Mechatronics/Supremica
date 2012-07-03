package net.sourceforge.waters.external.promela;

import java.util.List;

import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;

public class PromelaEdge
{
  private PromelaLabel mLabel;
  private List<SimpleExpressionProxy> mGuards;
  private List<BinaryExpressionProxy> mActions;
  private PromelaNode mStart;
  private final PromelaNode mEnd;

  public PromelaEdge(final PromelaNode start, final PromelaNode end, final PromelaLabel label)
  {
    mStart = start;
    mEnd = end;
    mLabel = label;
  }

  /**
   * A constructor for the Promela Edge class. Takes a label block, and optional guard block, and an optional action block
   * @param start The start node the edge is connected to
   * @param end The end node the edge is connected to
   * @param label The label block for this edge
   * @param guards The guard block for this edge, or null
   * @param actions The action block for this edge, or null
   */
  public PromelaEdge(final PromelaNode start, final PromelaNode end, final PromelaLabel label, final List<SimpleExpressionProxy> guards, final List<BinaryExpressionProxy> actions)
  {
    this(start, end, label);
    mGuards = guards;
    mActions = actions;
  }

  public void setLabel(final PromelaLabel label)
  {
    mLabel = label;
  }
  @Override
  public boolean equals(final Object e){
    if(this.getSource()==((PromelaEdge) e).getSource() && this.getTarget()==((PromelaEdge) e).getTarget()){
      return true;
    }else{
      return false;
    }
  }

  public PromelaLabel getLabelBlock()
  {
    return mLabel;
  }

  public List<SimpleExpressionProxy> getGuards()
  {
    return mGuards;
  }

  public List<BinaryExpressionProxy> getActions()
  {
    return mActions;
  }

  public void setStart(final PromelaNode start)
  {
    mStart = start;
  }
  public PromelaNode getSource()
  {
    return mStart;
  }
  public PromelaNode getTarget()
  {
    return mEnd;
  }
}
