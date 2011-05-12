package net.sourceforge.waters.external.promela;

public class PromelaEdge
{
  private PromelaLabel mLabel;
  private PromelaNode mStart;
  private final PromelaNode mEnd;
  public PromelaEdge(final PromelaNode start, final PromelaNode end, final PromelaLabel label){
    setStart(start);
    mEnd = end;
    setLabel(label);
  }
  public void setLabel(final PromelaLabel label)
  {
    mLabel = label;
  }
  public PromelaLabel getLabel()
  {
    return mLabel;
  }
  public void setStart(final PromelaNode start)
  {
    mStart = start;
  }
  public PromelaNode getStart()
  {
    return mStart;
  }
  public PromelaNode getEnd()
  {
    return mEnd;
  }
}
