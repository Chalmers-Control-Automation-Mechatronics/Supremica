package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass Workspace
public class Workspace extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IWorkspaceJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IWorkspace {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0x282D0CC5,(short)0x0771,(short)0x11D3,new char[]{0xAC,0x7A,0x00,0xC0,0x4F,0x68,0xB9,0x87});
  public static Workspace getWorkspaceFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Workspace(comPtr,bAddRef); }
  public static Workspace getWorkspaceFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Workspace(comPtr); }
  public static Workspace getWorkspaceFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new Workspace(unk); }
  public static Workspace convertComPtrToWorkspace(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Workspace(comPtr,true,releaseComPtr); }
  protected Workspace(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected Workspace(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected Workspace(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected Workspace(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
}
