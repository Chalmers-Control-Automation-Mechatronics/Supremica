package org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE;

// interface _Components Declaration
public interface _Components extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x0002E161,(short)0x0000,(short)0x0000,new char[]{0xC0,0x00,0x00,0x00,0x00,0x00,0x00,0x46});
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Component item(com.inzoom.comjni.Variant index) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Application getApplication() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.VBProject getParent() throws com.inzoom.comjni.ComJniException;
  public int getCount() throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.IUnknown _NewEnum() throws com.inzoom.comjni.ComJniException;
  public void remove(org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Component Component) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Component add(int ComponentType) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Component import_(String FileName) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.VBE getVBE() throws com.inzoom.comjni.ComJniException;
}
