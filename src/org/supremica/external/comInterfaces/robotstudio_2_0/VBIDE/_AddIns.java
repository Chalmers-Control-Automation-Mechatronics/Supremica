package org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE;

// interface _AddIns Declaration
public interface _AddIns extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0xDA936B62,(short)0xAC8B,(short)0x11D1,new char[]{0xB6,0xE5,0x00,0xA0,0xC9,0x0F,0x27,0x44});
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.AddIn item(com.inzoom.comjni.Variant index) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.VBE getVBE() throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.IDispatch getParent() throws com.inzoom.comjni.ComJniException;
  public int getCount() throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.IUnknown _NewEnum() throws com.inzoom.comjni.ComJniException;
  public void update() throws com.inzoom.comjni.ComJniException;
}
