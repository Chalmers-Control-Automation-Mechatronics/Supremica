package org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE;

// interface SelectedComponents Declaration
public interface SelectedComponents extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0xBE39F3D4,(short)0x1B13,(short)0x11D0,new char[]{0x88,0x7F,0x00,0xA0,0xC9,0x0F,0x27,0x44});
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Component item(int index) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Application getApplication() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.VBProject getParent() throws com.inzoom.comjni.ComJniException;
  public int getCount() throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.IUnknown _NewEnum() throws com.inzoom.comjni.ComJniException;
}
