package net.sourceforge.waters.analysis;

import net.sourceforge.waters.model.des.EventProxy;
import java.util.Collection;
import java.util.Collections;
import net.sourceforge.waters.model.base.NamedProxy;
import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.ProductDESProxyVisitor;
import gnu.trove.THashSet;
import java.util.Set;


public class AnnotatedMemStateProxy implements StateProxy
{
  private final int mName;
  private final boolean mIsInitial;
  private final Collection<EventProxy> mProps;

  public AnnotatedMemStateProxy(final int name,
                                final Collection<EventProxy> props,
                                final boolean isInitial)
  {
    mName = name;
    mProps = props;
    mIsInitial = isInitial;
  }

  public AnnotatedMemStateProxy(final int name, final EventProxy marked,
                                final boolean isInitial)
  {
    this(name, marked == null ? new THashSet<EventProxy>() : Collections
        .singleton(marked), isInitial);
  }

  public AnnotatedMemStateProxy(final int name, final EventProxy marked)
  {
    this(name, Collections.singleton(marked), false);
  }

  public AnnotatedMemStateProxy(final int name)
  {
    this(name, getRightType(), false);
  }

  private static Set<EventProxy> getRightType()
  {
    final Set<EventProxy> empty = Collections.emptySet();
    return empty;
  }

  public Collection<EventProxy> getPropositions()
  {
    return mProps;
  }

  public boolean isInitial()
  {
    return mIsInitial;
  }

  public int getNum()
  {
    return mName;
  }

  public AnnotatedMemStateProxy clone()
  {
    return new AnnotatedMemStateProxy(mName, mProps, mIsInitial);
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
    if (o instanceof AnnotatedMemStateProxy) {
      final AnnotatedMemStateProxy s = (AnnotatedMemStateProxy) o;
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
