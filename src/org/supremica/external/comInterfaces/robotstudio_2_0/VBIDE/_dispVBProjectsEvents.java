package org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE;

// Dispinterface _dispVBProjectsEvents Declaration
public interface _dispVBProjectsEvents extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid DIID = new com.inzoom.util.Guid(0x0002E103,(short)0x0000,(short)0x0000,new char[]{0xC0,0x00,0x00,0x00,0x00,0x00,0x00,0x46});
  public void itemAdded(org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.VBProject VBProject) throws com.inzoom.comjni.ComJniException ;
  public void itemRemoved(org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.VBProject VBProject) throws com.inzoom.comjni.ComJniException ;
  public void itemRenamed(org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.VBProject VBProject,String OldName) throws com.inzoom.comjni.ComJniException ;
  public void itemActivated(org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.VBProject VBProject) throws com.inzoom.comjni.ComJniException ;
}
