package org.supremica.automata.BDD.EFA;


/**
 *
 * @author Sajed Miremadi
 */

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.model.base.AttributeFactory;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.Operator;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.subject.module.SimpleExpressionSubject;


public class TimeInvariantAttributeFactory implements AttributeFactory
{

  //#########################################################################
  //# Singleton Pattern
  public static TimeInvariantAttributeFactory getInstance()
  {
    return TimeInvariantAttributeFactory.SingletonHolder.INSTANCE;
  }

  private TimeInvariantAttributeFactory()
  {
  }

  private static class SingletonHolder {
    private static final TimeInvariantAttributeFactory INSTANCE =
      new TimeInvariantAttributeFactory();
  }


  //#########################################################################
   public Collection<String> getApplicableKeys
     (final Class<? extends Proxy> clazz)
   {
     if (clazz.isAssignableFrom(NodeProxy.class)) {
       return ATTRIBUTES_FOR_NODE;
     } else {
       return Collections.emptyList();
     }
   }


  public List<String> getApplicableValues(final String attrib)
  {
      return Collections.emptyList();
  }

  
  public static SimpleExpressionProxy getInvariant(final Map<String,
                                                   String> attribs)
  {

    final String value = attribs.get(TimeInvariant_KEY);
    ExpressionParser parser = new ExpressionParser(
                    ModuleSubjectFactory.getInstance(), 
                    CompilerOperatorTable.getInstance());    
    if (value != null) {                        
            try {
                return (SimpleExpressionSubject) (parser.parse(value, 
                                                    Operator.TYPE_BOOLEAN));
            } catch (final ParseException pe) {
                System.err.println(pe);
            }
    }
    
    return null;
  }  

  //#########################################################################
  //# String Constants
   public static final String TimeInvariant_KEY = "Time:Invariant";  
  
   private static final Collection<String> ATTRIBUTES_FOR_NODE =
     Collections.singletonList(TimeInvariant_KEY);
}
