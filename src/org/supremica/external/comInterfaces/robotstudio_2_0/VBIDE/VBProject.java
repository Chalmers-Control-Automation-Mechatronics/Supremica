package org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE;

// coclass VBProject
public class VBProject extends org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.jcw._VBProjectJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._VBProject {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0x0002E169,(short)0x0000,(short)0x0000,new char[]{0xC0,0x00,0x00,0x00,0x00,0x00,0x00,0x46});
  public static VBProject getVBProjectFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new VBProject(comPtr,bAddRef); }
  public static VBProject getVBProjectFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new VBProject(comPtr); }
  public static VBProject getVBProjectFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new VBProject(unk); }
  public static VBProject convertComPtrToVBProject(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new VBProject(comPtr,true,releaseComPtr); }
  protected VBProject(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected VBProject(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected VBProject(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected VBProject(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
  public VBProject(short Context) throws com.inzoom.comjni.ComJniException{ 
    super(com.inzoom.comjni.ComLib.coCreateInstance(ClassID,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._VBProject.IID,Context),false);
  }
  public VBProject() throws com.inzoom.comjni.ComJniException{ 
    super(com.inzoom.comjni.ComLib.coCreateInstance(ClassID,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._VBProject.IID),false);
  }
}
