package org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE;

// interface _VBComponent_Old Declaration
public interface _VBComponent_Old extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x0002E164,(short)0x0000,(short)0x0000,new char[]{0xC0,0x00,0x00,0x00,0x00,0x00,0x00,0x46});
  public boolean getSaved() throws com.inzoom.comjni.ComJniException;
  public String getName() throws com.inzoom.comjni.ComJniException;
  public void setName(String pbstrReturn) throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.IDispatch getDesigner() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.CodeModule getCodeModule() throws com.inzoom.comjni.ComJniException;
  public int getType() throws com.inzoom.comjni.ComJniException;
  public void export(String FileName) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.VBE getVBE() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.VBComponents getCollection() throws com.inzoom.comjni.ComJniException;
  public boolean getHasOpenDesigner() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Properties getProperties() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Window designerWindow() throws com.inzoom.comjni.ComJniException;
  public void activate() throws com.inzoom.comjni.ComJniException;
}
