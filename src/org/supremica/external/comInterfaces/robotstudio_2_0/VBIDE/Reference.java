package org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE;

// interface Reference Declaration
public interface Reference extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x0002E17E,(short)0x0000,(short)0x0000,new char[]{0xC0,0x00,0x00,0x00,0x00,0x00,0x00,0x46});
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.References getCollection() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.VBE getVBE() throws com.inzoom.comjni.ComJniException;
  public String getName() throws com.inzoom.comjni.ComJniException;
  public String getGuid() throws com.inzoom.comjni.ComJniException;
  public int getMajor() throws com.inzoom.comjni.ComJniException;
  public int getMinor() throws com.inzoom.comjni.ComJniException;
  public String getFullPath() throws com.inzoom.comjni.ComJniException;
  public boolean getBuiltIn() throws com.inzoom.comjni.ComJniException;
  public boolean getIsBroken() throws com.inzoom.comjni.ComJniException;
  public int getType() throws com.inzoom.comjni.ComJniException;
  public String getDescription() throws com.inzoom.comjni.ComJniException;
}
