package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass Assemblies
public class Assemblies extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IAssembliesJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IAssemblies {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0x39413689,(short)0x7D48,(short)0x11D3,new char[]{0xAC,0xD5,0x00,0xC0,0x4F,0x68,0xB9,0x87});
  public static Assemblies getAssembliesFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Assemblies(comPtr,bAddRef); }
  public static Assemblies getAssembliesFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Assemblies(comPtr); }
  public static Assemblies getAssembliesFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new Assemblies(unk); }
  public static Assemblies convertComPtrToAssemblies(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Assemblies(comPtr,true,releaseComPtr); }
  protected Assemblies(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected Assemblies(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected Assemblies(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected Assemblies(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
}
