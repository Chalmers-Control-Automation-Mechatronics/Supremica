package net.sourceforge.waters.gui.springembedder;

public interface EmbedderSubject
{
  public void addObserver(EmbedderObserver observer);
  
  public void removeObserver(EmbedderObserver observer);
}
