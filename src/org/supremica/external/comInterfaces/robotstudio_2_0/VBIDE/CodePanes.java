package org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE;

// coclass CodePanes
public class CodePanes extends org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.jcw._CodePanesJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._CodePanes {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0x0002E174,(short)0x0000,(short)0x0000,new char[]{0xC0,0x00,0x00,0x00,0x00,0x00,0x00,0x46});
  public static CodePanes getCodePanesFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new CodePanes(comPtr,bAddRef); }
  public static CodePanes getCodePanesFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new CodePanes(comPtr); }
  public static CodePanes getCodePanesFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new CodePanes(unk); }
  public static CodePanes convertComPtrToCodePanes(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new CodePanes(comPtr,true,releaseComPtr); }
  protected CodePanes(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected CodePanes(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected CodePanes(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected CodePanes(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
  public CodePanes(short Context) throws com.inzoom.comjni.ComJniException{ 
    super(com.inzoom.comjni.ComLib.coCreateInstance(ClassID,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._CodePanes.IID,Context),false);
  }
  public CodePanes() throws com.inzoom.comjni.ComJniException{ 
    super(com.inzoom.comjni.ComLib.coCreateInstance(ClassID,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._CodePanes.IID),false);
  }
}
