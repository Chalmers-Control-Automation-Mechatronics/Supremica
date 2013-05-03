package net.sourceforge.waters.external.promela;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;

public class PromelaMType extends PromelaType
{
  private final List<String> mMTypes;
  String undefined = ":undefined";

  public PromelaMType(final String name)
  {
    super(name);
    mMTypes = new ArrayList<String>();
  }

  public SimpleExpressionProxy getRangeExpression(final ModuleProxyFactory factory)
  {
    //Create all of the identifiers for the mtype
    final List<SimpleIdentifierProxy> identifiers = new ArrayList<SimpleIdentifierProxy>(mMTypes.size());
    for(final String s : mMTypes)
    {
      identifiers.add(factory.createSimpleIdentifierProxy(s));
    }

    //Add in an identifier for undefined
    identifiers.add(factory.createSimpleIdentifierProxy(undefined));

    return factory.createEnumSetExpressionProxy(identifiers);
  }

  public List<String> getMTypes()
  {
    return mMTypes;
  }

  public void addMType(final String mtype)
  {
    mMTypes.add(mtype);
  }

  public SimpleExpressionProxy getInitialValue(final ModuleProxyFactory factory)
  {
    return factory.createSimpleIdentifierProxy(undefined);
  }
}
