package org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE;

// interface _Windows_old Declaration
public interface _Windows_old extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x0002E16A,(short)0x0000,(short)0x0000,new char[]{0xC0,0x00,0x00,0x00,0x00,0x00,0x00,0x46});
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.VBE getVBE() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Application getParent() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Window item(com.inzoom.comjni.Variant index) throws com.inzoom.comjni.ComJniException;
  public int getCount() throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.IUnknown _NewEnum() throws com.inzoom.comjni.ComJniException;
}
