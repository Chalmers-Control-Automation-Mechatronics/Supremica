package org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE;

// coclass ProjectTemplate
public class ProjectTemplate extends org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.jcw._ProjectTemplateJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._ProjectTemplate {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0x32CDF9E0,(short)0x1602,(short)0x11CE,new char[]{0xBF,0xDC,0x08,0x00,0x2B,0x2B,0x8C,0xDA});
  public static ProjectTemplate getProjectTemplateFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ProjectTemplate(comPtr,bAddRef); }
  public static ProjectTemplate getProjectTemplateFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ProjectTemplate(comPtr); }
  public static ProjectTemplate getProjectTemplateFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new ProjectTemplate(unk); }
  public static ProjectTemplate convertComPtrToProjectTemplate(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ProjectTemplate(comPtr,true,releaseComPtr); }
  protected ProjectTemplate(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected ProjectTemplate(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected ProjectTemplate(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected ProjectTemplate(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
  public ProjectTemplate(short Context) throws com.inzoom.comjni.ComJniException{ 
    super(com.inzoom.comjni.ComLib.coCreateInstance(ClassID,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._ProjectTemplate.IID,Context),false);
  }
  public ProjectTemplate() throws com.inzoom.comjni.ComJniException{ 
    super(com.inzoom.comjni.ComLib.coCreateInstance(ClassID,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._ProjectTemplate.IID),false);
  }
}
