package org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw;

// interface MsoDebugOptions Implementation
public class MsoDebugOptionsJCW extends org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw._IMsoDispObjJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.Office.MsoDebugOptions {
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.MsoDebugOptions getMsoDebugOptionsFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new MsoDebugOptionsJCW(comPtr,bAddRef); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.MsoDebugOptions getMsoDebugOptionsFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new MsoDebugOptionsJCW(comPtr); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.MsoDebugOptions getMsoDebugOptionsFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new MsoDebugOptionsJCW(unk); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.MsoDebugOptions convertComPtrToMsoDebugOptions(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new MsoDebugOptionsJCW(comPtr,true,releaseComPtr); }
  protected MsoDebugOptionsJCW(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected MsoDebugOptionsJCW(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.Office.MsoDebugOptions.IID); }
  protected MsoDebugOptionsJCW(int comPtr,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(comPtr,guid); }
  protected MsoDebugOptionsJCW(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk,org.supremica.external.comInterfaces.robotstudio_2_0.Office.MsoDebugOptions.IID); }
  protected MsoDebugOptionsJCW(com.inzoom.comjni.IUnknown unk,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(unk,guid); }
  protected MsoDebugOptionsJCW(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.Office.MsoDebugOptions.IID,releaseComPtr);}
  protected MsoDebugOptionsJCW(int comPtr,com.inzoom.util.Guid iid,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,iid,releaseComPtr); }
  public int getFeatureReports() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,com.inzoom.comjni.enum.VarType.INT,true)
    };
    vtblCall(36,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.MsoDebugOptions.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setFeatureReports(int puintFeatureReports) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(puintFeatureReports,com.inzoom.comjni.enum.VarType.INT,false)
    };
    vtblCall(40,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.MsoDebugOptions.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
}
