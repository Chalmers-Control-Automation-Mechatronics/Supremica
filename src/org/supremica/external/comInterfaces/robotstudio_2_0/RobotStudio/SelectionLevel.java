package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass SelectionLevel
public class SelectionLevel extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.ISelectionLevelJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ISelectionLevel {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0xC02A25D9,(short)0xE9C2,(short)0x11D3,new char[]{0xAD,0x4F,0x00,0xC0,0x4F,0x68,0xB9,0x87});
  public static SelectionLevel getSelectionLevelFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new SelectionLevel(comPtr,bAddRef); }
  public static SelectionLevel getSelectionLevelFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new SelectionLevel(comPtr); }
  public static SelectionLevel getSelectionLevelFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new SelectionLevel(unk); }
  public static SelectionLevel convertComPtrToSelectionLevel(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new SelectionLevel(comPtr,true,releaseComPtr); }
  protected SelectionLevel(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected SelectionLevel(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected SelectionLevel(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected SelectionLevel(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
  public SelectionLevel(short Context) throws com.inzoom.comjni.ComJniException{ 
    super(com.inzoom.comjni.ComLib.coCreateInstance(ClassID,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ISelectionLevel.IID,Context),false);
  }
  public SelectionLevel() throws com.inzoom.comjni.ComJniException{ 
    super(com.inzoom.comjni.ComLib.coCreateInstance(ClassID,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ISelectionLevel.IID),false);
  }
}
