package org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE;

// interface _CodePanes Declaration
public interface _CodePanes extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x0002E172,(short)0x0000,(short)0x0000,new char[]{0xC0,0x00,0x00,0x00,0x00,0x00,0x00,0x46});
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.VBE getParent() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.VBE getVBE() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.CodePane item(com.inzoom.comjni.Variant index) throws com.inzoom.comjni.ComJniException;
  public int getCount() throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.IUnknown _NewEnum() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.CodePane getCurrent() throws com.inzoom.comjni.ComJniException;
  public void setCurrent(org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.CodePane CodePane) throws com.inzoom.comjni.ComJniException;
}
