package org.supremica.external.comInterfaces.robotstudio_2_0.Office;

// interface PropertyTest Declaration
public interface PropertyTest extends org.supremica.external.comInterfaces.robotstudio_2_0.Office._IMsoDispObj {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x000C0333,(short)0x0000,(short)0x0000,new char[]{0xC0,0x00,0x00,0x00,0x00,0x00,0x00,0x46});
  public String getName() throws com.inzoom.comjni.ComJniException;
  public int getCondition() throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.Variant getValue() throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.Variant getSecondValue() throws com.inzoom.comjni.ComJniException;
  public int getConnector() throws com.inzoom.comjni.ComJniException;
}
