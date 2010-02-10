package net.sourceforge.waters.gui.actions;

import java.util.Map;

import net.sourceforge.waters.model.analysis.IdenticalKindTranslator;
import net.sourceforge.waters.model.base.NamedProxy;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.compiler.context.SourceInfo;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;

public class SinglePropertyKindTranslator extends IdenticalKindTranslator
{
  public SinglePropertyKindTranslator(final NamedProxy aut, final Map<Proxy, SourceInfo> sourceInfoMap)
  {
    namedProxy = aut;
    mSourceInfo = sourceInfoMap;
  }

  public ComponentKind getComponentKind(final AutomatonProxy aut)
  {
    if (namedProxy instanceof AutomatonProxy)
    {
    if (aut == namedProxy)
      return ComponentKind.SPEC;
    else
      return super.getComponentKind(aut);
    }
    else
    {
      if (mSourceInfo.get(aut).getSourceObject() == namedProxy)
        return ComponentKind.SPEC;
      else
        return super.getComponentKind(aut);
    }
  }

  // Data Members
  private final NamedProxy namedProxy;
  private final Map<Proxy,SourceInfo> mSourceInfo;

  // Class Constants
  private static final long serialVersionUID = 1572309754913945037L;
}
