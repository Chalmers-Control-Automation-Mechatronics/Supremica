package net.sourceforge.waters.analysis;

import net.sourceforge.waters.plain.base.NamedElement;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.xsd.base.EventKind;
import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.des.ProductDESProxyVisitor;
import java.util.Set;
import net.sourceforge.waters.model.base.VisitorException;

public class AnnotationEvent
  extends NamedElement
  implements EventProxy
{
  private static final long serialVersionUID = 1L;

  private final Set<Set<EventProxy>> mAnnotations;
  
  public AnnotationEvent(Set<Set<EventProxy>> annotations, String name)
  {
    super(name);
    mAnnotations = annotations;
  }
  
  public Set<Set<EventProxy>> getAnnotations()
  {
    return mAnnotations;
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
    return "Annotation: " + mAnnotations.toString();
  }
}
