package org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE;

// interface _VBProjects Declaration
public interface _VBProjects extends org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._VBProjects_Old {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0xEEE00919,(short)0xE393,(short)0x11D1,new char[]{0xBB,0x03,0x00,0xC0,0x4F,0xB6,0xC4,0xA6});
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.VBProject add(int Type) throws com.inzoom.comjni.ComJniException;
  public void remove(org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.VBProject lpc) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.VBProject open(String bstrPath) throws com.inzoom.comjni.ComJniException;
}
