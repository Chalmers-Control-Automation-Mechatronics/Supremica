package org.supremica.automata.BDD.EFA;


/**
 *
 * @author Sajed Miremadi, Zhennan Fei
 */

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.model.base.AttributeFactory;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.module.NodeProxy;


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
    final List<String> values = ATTRIBUTE_VALUES.get(attrib);
    if (values == null) {
      return Collections.emptyList();
    } else {
      return values;
    }
  }


  //#########################################################################
  //# String Constants

   private static final Collection<String> ATTRIBUTES_FOR_NODE =
     Collections.singletonList("Time:Invariant");
  
  
   private static final Map<String,List<String>> ATTRIBUTE_VALUES =
     new HashMap<String,List<String>>(2);

}
