package net.sourceforge.waters.external.promela;

import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;

public class PromelaIntRange extends PromelaType
{
  public final static PromelaIntRange BIT = new PromelaIntRange("bit", 0, 1);
  public final static PromelaIntRange BYTE = new PromelaIntRange("byte", 0, 255);
  public final static PromelaIntRange SHORT = new PromelaIntRange("short", Short.MIN_VALUE , Short.MAX_VALUE);
  public final static PromelaIntRange INT = new PromelaIntRange("short", Integer.MIN_VALUE , Integer.MAX_VALUE);

  private final int mLower;
  private final int mUpper;

  public PromelaIntRange(final String name, final int lower, final int upper)
  {
    super(name);
    mLower = lower;
    mUpper = upper;
  }

  public SimpleExpressionProxy getRangeExpression(final ModuleProxyFactory factory)
  {
    final CompilerOperatorTable opTable = CompilerOperatorTable.getInstance();
    final BinaryOperator rangeOperator = opTable.getRangeOperator();
    final SimpleExpressionProxy lower = factory.createIntConstantProxy(mLower);
    final SimpleExpressionProxy upper = factory.createIntConstantProxy(mUpper);
    return factory.createBinaryExpressionProxy(rangeOperator, lower, upper);
  }

  public SimpleExpressionProxy getInitialValue(final ModuleProxyFactory factory)
  {
    return factory.createIntConstantProxy(0);
  }

  public int getLower()
  {
    return mLower;
  }

  public int getUpper()
  {
    return mUpper;
  }
}
