package org.jgrafchart;

public interface Readable extends Referencable {

  public boolean getBoolVal();
  public boolean getOldBoolVal();

  public int getIntVal();
  public int getOldIntVal();

  public String getStringVal();
  public String getOldStringVal();
}
