package net.sourceforge.waters.external.promela;

import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;

public abstract class PromelaType
{
  private final String mName;

  PromelaType(final String name)
  {
    mName = name;
  }

  public abstract SimpleExpressionProxy getRangeExpression(ModuleProxyFactory factory);

  public abstract SimpleExpressionProxy getInitialValue(ModuleProxyFactory factory);

  public String getName()
  {
    return mName;
  }
}
