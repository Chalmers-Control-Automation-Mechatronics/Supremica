package net.sourceforge.waters.gui;

import java.util.Collection;
import net.sourceforge.waters.subject.module.IdentifierSubject;
import net.sourceforge.waters.xsd.base.EventKind;

public class IdentifierWithKind
{
    private Collection<IdentifierSubject> ip_;
    private EventType e_;
    
    public IdentifierWithKind(Collection<IdentifierSubject> ip, EventType e)
    {
      ip_ = ip;
      e_ = e;
    }
    
    public Collection<IdentifierSubject> getIdentifiers()
    {
      return ip_;
    }
    
    public EventType getKind()
    {
      return e_;
    }
}
