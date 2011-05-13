package net.sourceforge.waters.external.promela;

public class PromelaEdge
{
  private PromelaLabel mLabel;
  private PromelaNode mStart;
  private final PromelaNode mEnd;
  public PromelaEdge(final PromelaNode start, final PromelaNode end, final PromelaLabel label){
    mStart = start;
    mEnd = end;
    mLabel = label;
  }
  public void setLabel(final PromelaLabel label)
  {
    mLabel = label;
  }
  public PromelaLabel getLabelBlock()
  {
    return mLabel;
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
