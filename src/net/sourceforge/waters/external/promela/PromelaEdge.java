package net.sourceforge.waters.external.promela;

import java.util.List;

import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
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
   * @author Ethan Duff
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

  /**
   * A method to check if this object is equal to another object
   * @param o The object to compare against
   * @return True if the other object is an equal promela edge. <br>
   * Uses value typed equality, and treats all labels as being equal
   * @author Ethan Duff
   */
  @Override
  public boolean equals(final Object o)
  {
    if(!(o instanceof PromelaEdge))
    {
      //Is not a promela edge, so is not equal
      return false;
    }
    final PromelaEdge edge = (PromelaEdge) o;
    if(!(edge.getSource() == this.getSource() && edge.getTarget() == this.getTarget()))
    {
      //Has different starting or ending node, so is not equal
      return false;
    }

    if(this.equalGuards(edge) && this.equalActions(edge))
    {
      //The guards and actions are equal, so is considered an equal edge
      return true;
    }
    else
    {
      //The guards or actions are not equal, so is not equal
      return false;
    }
  }

  /**
   * A method to check if the guards of this edge are equal to the guards of another edge
   * @param other The edge to compare against
   * @return True if the guards are equal, false otherwise
   * @author Ethan Duff
   */
  private boolean equalGuards(final PromelaEdge other)
  {
    final ModuleEqualityVisitor comparitor = new ModuleEqualityVisitor(false);

    if(other.getGuards() == null && this.getGuards() == null)
    {
      //The guard edges are both null
      return true;
    }
    else if(other.getGuards() != null && this.getGuards() != null)
    {
      if(comparitor.isEqualCollection(other.getGuards(), this.getGuards()))
      {
        //The guard edges are equal
        return true;
      }
      else
      {
        //The guard edges are not equal
        return false;
      }
    }
    else
    {
      //The guard blocks are different, so are not equal
      return false;
    }
  }

  /**
   * A method to check if the actions of this edge are equal to the actions of another edge
   * @param other The edge to compare against
   * @return True if the actions are equal, false otherwise
   * @author Ethan Duff
   */
  private boolean equalActions(final PromelaEdge other)
  {
    final ModuleEqualityVisitor comparitor = new ModuleEqualityVisitor(false);

    if(other.getActions() == null && this.getActions() == null)
    {
      //The action edges are both null, so are considered equal
      return true;
    }
    else if(other.getActions() != null && this.getActions() != null)
    {
      if(comparitor.isEqualCollection(other.getActions(), this.getActions()))
      {
        //The action blocks are the same, so are considered equal
        return true;
      }
      else
      {
        //The action blocks are different, so are not equal
        return false;
      }
    }
    else
    {
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
