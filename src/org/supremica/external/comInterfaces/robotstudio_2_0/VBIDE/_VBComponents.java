package org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE;

// interface _VBComponents Declaration
public interface _VBComponents extends org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._VBComponents_Old {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0xEEE0091C,(short)0xE393,(short)0x11D1,new char[]{0xBB,0x03,0x00,0xC0,0x4F,0xB6,0xC4,0xA6});
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.VBComponent addCustom(String ProgId) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.VBComponent addMTDesigner(int index) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.VBComponent addMTDesigner() throws com.inzoom.comjni.ComJniException;
}
