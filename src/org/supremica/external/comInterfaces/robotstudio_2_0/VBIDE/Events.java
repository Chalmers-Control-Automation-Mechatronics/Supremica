package org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE;

// interface Events Declaration
public interface Events extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x0002E167,(short)0x0000,(short)0x0000,new char[]{0xC0,0x00,0x00,0x00,0x00,0x00,0x00,0x46});
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.ReferencesEvents getReferencesEvents(org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.VBProject VBProject) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.CommandBarEvents getCommandBarEvents(com.inzoom.comjni.IDispatch CommandBarControl) throws com.inzoom.comjni.ComJniException;
}
