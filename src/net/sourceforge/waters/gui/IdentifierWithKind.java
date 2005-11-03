package net.sourceforge.waters.gui;

import net.sourceforge.waters.subject.module.IdentifierSubject;
import net.sourceforge.waters.xsd.base.EventKind;

public class IdentifierWithKind
{
    private IdentifierSubject ip_;
    private EventKind e_;
    
    public IdentifierWithKind(IdentifierSubject ip, EventKind e)
    {
	ip_ = ip;
	e_ = e;
    }
    
    public IdentifierSubject getIdentifier()
    {
	return ip_;
    }
    
    public EventKind getKind()
    {
	return e_;
    }
}
