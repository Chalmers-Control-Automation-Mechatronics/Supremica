package org.jgrafchart;

public interface Writable extends Referencable {

  public boolean isBoolean();
  public boolean isInteger();
  public boolean isString();

  public void setStoredBoolAction(boolean b);
  public void setStoredIntAction(int i);
  public void setStoredStringAction(String s);
}


