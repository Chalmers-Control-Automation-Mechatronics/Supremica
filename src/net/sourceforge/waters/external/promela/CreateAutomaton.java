package net.sourceforge.waters.external.promela;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.IndexedIdentifierProxy;
import net.sourceforge.waters.model.module.IntConstantProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.plain.module.ModuleElementFactory;
import net.sourceforge.waters.xsd.base.EventKind;
import net.sourceforge.waters.xsd.module.ScopeKind;

public class CreateAutomaton
{
  final ModuleProxyFactory factory = ModuleElementFactory.getInstance();
  final CompilerOperatorTable optable = CompilerOperatorTable.getInstance();

  public void createEvent(final String n, final List<String> list){


    final IntConstantProxy c33 = factory.createIntConstantProxy(Integer.parseInt(list.get(0)));
    final IntConstantProxy c124 = factory.createIntConstantProxy(Integer.parseInt(list.get(1)));

    final Collection<SimpleExpressionProxy> index = new ArrayList<SimpleExpressionProxy>(2);
    index.add(c33);
    index.add(c124);

    final IndexedIdentifierProxy ename = factory.createIndexedIdentifierProxy(n, index);

    final IntConstantProxy zero = factory.createIntConstantProxy(0);
    final IntConstantProxy c255 = factory.createIntConstantProxy(255);

    final BinaryOperator op = optable.getRangeOperator();
    final BinaryExpressionProxy range = factory.createBinaryExpressionProxy(op,zero,c255);

    final Collection<SimpleExpressionProxy> ranges = new ArrayList<SimpleExpressionProxy>(2);
    ranges.add(range);
    ranges.add(range);

    @SuppressWarnings("unused")
    final EventDeclProxy event = factory.createEventDeclProxy(ename,EventKind.CONTROLLABLE,true,ScopeKind.LOCAL,ranges,null,null);
  }

  public void createComponent(){

  }
}
