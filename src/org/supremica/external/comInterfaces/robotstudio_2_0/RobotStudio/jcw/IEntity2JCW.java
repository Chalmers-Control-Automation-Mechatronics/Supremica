package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw;

// interface IEntity2 Implementation
public class IEntity2JCW extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IEntityJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity2 {
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity2 getIEntity2FromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IEntity2JCW(comPtr,bAddRef); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity2 getIEntity2FromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IEntity2JCW(comPtr); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity2 getIEntity2FromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new IEntity2JCW(unk); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity2 convertComPtrToIEntity2(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IEntity2JCW(comPtr,true,releaseComPtr); }
  protected IEntity2JCW(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected IEntity2JCW(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity2.IID); }
  protected IEntity2JCW(int comPtr,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(comPtr,guid); }
  protected IEntity2JCW(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity2.IID); }
  protected IEntity2JCW(com.inzoom.comjni.IUnknown unk,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(unk,guid); }
  protected IEntity2JCW(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity2.IID,releaseComPtr);}
  protected IEntity2JCW(int comPtr,com.inzoom.util.Guid iid,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,iid,releaseComPtr); }
  public boolean isPointInside(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPosition pPoint) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)pPoint,false),
      new com.inzoom.comjni.Variant((boolean)false,true)
    };
    vtblCall(164,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity2.IID);
    boolean rv = _v[1].getBoolean();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
}
