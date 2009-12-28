package net.sourceforge.waters.analysis;

import net.sourceforge.waters.model.des.EventProxy;
import java.util.Collection;
import java.util.Collections;
import net.sourceforge.waters.model.base.NamedProxy;
import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.ProductDESProxyVisitor;

public class MemStateProxy
  implements StateProxy
{
  private final int mName;
  private final EventProxy mEvent;

  public MemStateProxy(final int name, final EventProxy event)
  {
    mName = name;
    mEvent = event;
  }

  public MemStateProxy(final int name)
  {
    this(name, null);
  }

  public Collection<EventProxy> getPropositions()
  {
    if (mEvent == null) {
      return Collections.emptySet();
    } else {
      return Collections.singleton(mEvent);
    }
  }

  public boolean isInitial()
  {
    return mName == 0;
  }

  public int getNum()
  {
    return mName;
  }

  public MemStateProxy clone()
  {
    return new MemStateProxy(mName, mEvent);
  }

  public String getName()
  {
    return Integer.toString(mName);
  }

  public boolean refequals(final Object o)
  {
    if (o instanceof NamedProxy) {
      return refequals((NamedProxy) o);
    }
    return false;
  }

  public boolean refequals(final NamedProxy o)
  {
    if (o instanceof MemStateProxy) {
      final MemStateProxy s = (MemStateProxy) o;
      return s.mName == mName;
    } else {
      return false;
    }
  }

  public int refHashCode()
  {
    return mName;
  }

  public Object acceptVisitor(final ProxyVisitor visitor)
    throws VisitorException
  {
    final ProductDESProxyVisitor desvisitor = (ProductDESProxyVisitor) visitor;
    return desvisitor.visitStateProxy(this);
  }

  public Class<StateProxy> getProxyInterface()
  {
    return StateProxy.class;
  }

  public int compareTo(final NamedProxy n)
  {
    return n.getName().compareTo(getName());
  }

  public String toString()
  {
    return "S:" + mName;
  }
}
