package org.jgrafchart.Transitions;

import com.nwoods.jgo.*;
import org.jgrafchart.*;

public class TREvdvar extends TRVar {


  TREvdvar(int id){
    super(id);
  }

  public void setName(String n) {
    super.setName(n.substring(1));
  }

  public String toString() {
    return " Down Event: " + name;
  }

  public boolean evaluate() {
    if (dotX) { return (!((GrafcetObject)in).x && ((GrafcetObject)in).oldx);}
    else {
      if (dotT) { return false;}
      else {
        return (!in.getBoolVal() && in.getOldBoolVal()) ;
      }
    }
  }

  public int intEvaluate() {
    return 0;
  }

}
