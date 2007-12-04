package net.sourceforge.waters.gui;

import java.util.List;
import net.sourceforge.waters.subject.module.IdentifierSubject;
import net.sourceforge.waters.xsd.base.EventKind;

public class IdentifierWithKind
{
    private List<IdentifierSubject> ip_;
    private EventType e_;
    
    public IdentifierWithKind(List<IdentifierSubject> ip, EventType e)
    {
      ip_ = ip;
      e_ = e;
    }
    
    public List<IdentifierSubject> getIdentifiers()
    {
      return ip_;
    }
    
    public EventType getKind()
    {
      return e_;
    }
}
