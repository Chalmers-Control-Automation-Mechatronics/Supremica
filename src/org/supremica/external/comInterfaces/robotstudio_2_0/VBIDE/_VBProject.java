package org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE;

// interface _VBProject Declaration
public interface _VBProject extends org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._VBProject_Old {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0xEEE00915,(short)0xE393,(short)0x11D1,new char[]{0xBB,0x03,0x00,0xC0,0x4F,0xB6,0xC4,0xA6});
  public void saveAs(String FileName) throws com.inzoom.comjni.ComJniException;
  public void makeCompiledFile() throws com.inzoom.comjni.ComJniException;
  public int getType() throws com.inzoom.comjni.ComJniException;
  public String getFileName() throws com.inzoom.comjni.ComJniException;
  public String getBuildFileName() throws com.inzoom.comjni.ComJniException;
  public void setBuildFileName(String lpbstrBldFName) throws com.inzoom.comjni.ComJniException;
}
