package org.supremica.external.comInterfaces.robotstudio_2_0.Office;

// interface MsoDebugOptions Declaration
public interface MsoDebugOptions extends org.supremica.external.comInterfaces.robotstudio_2_0.Office._IMsoDispObj {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x000C035A,(short)0x0000,(short)0x0000,new char[]{0xC0,0x00,0x00,0x00,0x00,0x00,0x00,0x46});
  public int getFeatureReports() throws com.inzoom.comjni.ComJniException;
  public void setFeatureReports(int puintFeatureReports) throws com.inzoom.comjni.ComJniException;
}
