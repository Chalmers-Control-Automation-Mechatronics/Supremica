package net.sourceforge.waters.analysis.po;

public enum PartialOrderEventStutteringKind
{

  NONSTUTTERING, STUTTERING;

  public String value()
  {
    return name();
  }

  public static PartialOrderEventStutteringKind fromValue(final String v)
  {
    return valueOf(v);
  }
}
