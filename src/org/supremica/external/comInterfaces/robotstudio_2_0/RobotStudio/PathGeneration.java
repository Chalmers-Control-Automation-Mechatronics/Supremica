package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass PathGeneration
public class PathGeneration extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IPathGenerationJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPathGeneration {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0x09531111,(short)0xB879,(short)0x11D3,new char[]{0xBF,0x75,0x00,0xC0,0x4F,0x68,0xDF,0x5A});
  public static PathGeneration getPathGenerationFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new PathGeneration(comPtr,bAddRef); }
  public static PathGeneration getPathGenerationFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new PathGeneration(comPtr); }
  public static PathGeneration getPathGenerationFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new PathGeneration(unk); }
  public static PathGeneration convertComPtrToPathGeneration(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new PathGeneration(comPtr,true,releaseComPtr); }
  protected PathGeneration(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected PathGeneration(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected PathGeneration(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected PathGeneration(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
  public PathGeneration(short Context) throws com.inzoom.comjni.ComJniException{ 
    super(com.inzoom.comjni.ComLib.coCreateInstance(ClassID,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPathGeneration.IID,Context),false);
  }
  public PathGeneration() throws com.inzoom.comjni.ComJniException{ 
    super(com.inzoom.comjni.ComLib.coCreateInstance(ClassID,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPathGeneration.IID),false);
  }
}
