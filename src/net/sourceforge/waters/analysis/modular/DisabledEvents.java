package net.sourceforge.waters.analysis.modular;

import net.sourceforge.waters.plain.base.NamedElement;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.xsd.base.EventKind;
import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.des.ProductDESProxyVisitor;
import java.util.Set;
import net.sourceforge.waters.model.base.VisitorException;

public class DisabledEvents
  extends NamedElement
  implements EventProxy
{
  private final Set<EventProxy> mDisabled;
  
  public DisabledEvents(Set<EventProxy> disabled)
  {
    super("disabled");
    mDisabled = disabled;
  }
  
  public Set<EventProxy> getDisabled()
  {
    return mDisabled;
  }
  
  public EventKind getKind()
  {
    return EventKind.UNCONTROLLABLE;
  }
  
  public boolean isObservable()
  {
    return false;
  }
  
  public Object acceptVisitor(final ProxyVisitor visitor)
    throws VisitorException
  {
    final ProductDESProxyVisitor desvisitor = (ProductDESProxyVisitor) visitor;
    return desvisitor.visitEventProxy(this);
  }
  
  public Class<EventProxy> getProxyInterface()
  {
    return EventProxy.class;
  }
  
  public String toString()
  {
    return "Disabled: " + mDisabled.toString();
  }
}
