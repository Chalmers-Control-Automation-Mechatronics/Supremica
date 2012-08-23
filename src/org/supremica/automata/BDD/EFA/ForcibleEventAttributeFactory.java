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
import net.sourceforge.waters.model.module.EventDeclProxy;


public class ForcibleEventAttributeFactory implements AttributeFactory
{

  //#########################################################################
  //# Singleton Pattern
  public static ForcibleEventAttributeFactory getInstance()
  {
    return ForcibleEventAttributeFactory.SingletonHolder.INSTANCE;
  }

  private ForcibleEventAttributeFactory()
  {
  }

  private static class SingletonHolder {
    private static final ForcibleEventAttributeFactory INSTANCE =
      new ForcibleEventAttributeFactory();
  }


  //#########################################################################
   public Collection<String> getApplicableKeys
     (final Class<? extends Proxy> clazz)
   {
     if (clazz.isAssignableFrom(EventDeclProxy.class)) {
       return ATTRIBUTES_FOR_EVENT;
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
  
  public static boolean isForcible(final Map<String, String> attribs)
  {
      final String value = attribs.get(TimeForcible_KEY);
      if(value == null){
          return false;
      }
      else{
          if(value.equals("true"))
                  return true;
      }
      
      return false;
  }

  //#########################################################################
  //# String Constants

   public static final String TimeForcible_KEY = "Time:Forcible";  
  
   private static final Collection<String> ATTRIBUTES_FOR_EVENT =
     Collections.singletonList(TimeForcible_KEY);
    
   private static final Map<String,List<String>> ATTRIBUTE_VALUES =
     new HashMap<String,List<String>>(2);
   static {
    final List<String> types = new ArrayList<String>(2);
    types.add("true");
    types.add("false");
    ATTRIBUTE_VALUES.put(TimeForcible_KEY, types);   
   }   

}
