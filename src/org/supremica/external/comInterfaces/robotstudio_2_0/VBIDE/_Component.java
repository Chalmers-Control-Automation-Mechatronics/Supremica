package org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE;

// interface _Component Declaration
public interface _Component extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x0002E163,(short)0x0000,(short)0x0000,new char[]{0xC0,0x00,0x00,0x00,0x00,0x00,0x00,0x46});
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Application getApplication() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Components getParent() throws com.inzoom.comjni.ComJniException;
  public boolean getIsDirty() throws com.inzoom.comjni.ComJniException;
  public void setIsDirty(boolean lpfReturn) throws com.inzoom.comjni.ComJniException;
  public String getName() throws com.inzoom.comjni.ComJniException;
  public void setName(String pbstrReturn) throws com.inzoom.comjni.ComJniException;
}
