package net.sourceforge.waters.analysis.po;

public enum PartialOrderParingRequest
{
  VISIT,CLOSE;
  public String value()
  {
    return name();
  }

  public static PartialOrderParingRequest fromValue(final String v)
  {
    return valueOf(v);
  }
}
