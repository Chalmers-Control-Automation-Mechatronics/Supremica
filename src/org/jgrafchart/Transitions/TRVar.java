package org.jgrafchart.Transitions;

import com.nwoods.jgo.*;
import org.jgrafchart.*;
import java.util.*;

public class TRVar extends SimpleNode {
  protected String name;
  protected Readable in;
  protected boolean dotX = false;
  protected boolean dotT = false;

  TRVar(int id){
    super(id);
  }

  public void setName(String n) {
    name = n;
    if (name.endsWith(".x") || name.endsWith(".X")) {
      dotX = true;
      int length = name.length();
      name = name.substring(0,length-2);
    } else {
      if (name.endsWith(".t") || name.endsWith(".T")) {
	dotT = true;
        int length = name.length();
        name = name.substring(0,length-2); 
      }
    }
  }


  public String toString() {
    return "Variable: " + name;
  }

  public boolean evaluate() {
    if (dotX) { return ((GrafcetObject)in).x;}
    else {
      if (dotT) {return false;}
      else {return  in.getBoolVal();
      }
    }
  }

  public int intEvaluate() {
    if (dotX) { return 0;}
    else {
      if (dotT) {return ((GrafcetObject)in).timer;}
      else {return  in.getIntVal();}
    }
  }



  public boolean compile(ArrayList doc) {
    boolean found;
    boolean result = true;
    Object obj;
    ArrayList table = doc;
    String reference = name;
    int dotIndex = reference.indexOf('.');
    while (dotIndex != -1) {
      String firstName = reference.substring(0,dotIndex);
      reference = reference.substring(dotIndex + 1);
      found = false;
      for (Iterator i = table.iterator(); !found && i.hasNext();) {
        obj = (Object)i.next();
        if (obj instanceof Referencable) {
          Referencable ref = (Referencable)obj;
          if (ref.getName().compareTo(firstName) == 0) {
            found = true;
	    if (ref instanceof GCDocument) {
	      table = ((GCDocument)ref).getSymbolTable();
	    }
	  }
	}
      }
      result = result && found;
      dotIndex = reference.indexOf('.');
    }
    found = false;
    for (Iterator i = table.iterator(); !found && i.hasNext();) {
        obj = (Object)i.next();
        if (obj instanceof Readable) {
          Readable ref = (Readable)obj;
          if (ref.getName().compareTo(reference) == 0) {
            found = true;
	    in = ref;
	  }
	}
    }
    result = result && found;
    return result;
  }




}
