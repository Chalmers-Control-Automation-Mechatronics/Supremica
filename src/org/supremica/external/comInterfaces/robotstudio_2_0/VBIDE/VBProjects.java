package org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE;

// coclass VBProjects
public class VBProjects extends org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.jcw._VBProjectsJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._VBProjects {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0x0002E101,(short)0x0000,(short)0x0000,new char[]{0xC0,0x00,0x00,0x00,0x00,0x00,0x00,0x46});
  public static VBProjects getVBProjectsFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new VBProjects(comPtr,bAddRef); }
  public static VBProjects getVBProjectsFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new VBProjects(comPtr); }
  public static VBProjects getVBProjectsFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new VBProjects(unk); }
  public static VBProjects convertComPtrToVBProjects(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new VBProjects(comPtr,true,releaseComPtr); }
  protected VBProjects(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected VBProjects(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected VBProjects(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected VBProjects(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
  public VBProjects(short Context) throws com.inzoom.comjni.ComJniException{ 
    super(com.inzoom.comjni.ComLib.coCreateInstance(ClassID,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._VBProjects.IID,Context),false);
  }
  public VBProjects() throws com.inzoom.comjni.ComJniException{ 
    super(com.inzoom.comjni.ComLib.coCreateInstance(ClassID,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._VBProjects.IID),false);
  }
}
