package net.sourceforge.waters.gui.springembedder;

public class EmbedderEvent
{
  private EmbedderEventType mType;
  
  public EmbedderEvent(EmbedderEventType type)
  {
    mType = type;
  }
  
  public EmbedderEventType getType()
  {
    return mType;
  }
}
