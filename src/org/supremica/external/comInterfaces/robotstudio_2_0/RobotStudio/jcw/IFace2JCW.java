package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw;

// interface IFace2 Implementation
public class IFace2JCW extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IFaceJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IFace2 {
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IFace2 getIFace2FromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IFace2JCW(comPtr,bAddRef); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IFace2 getIFace2FromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IFace2JCW(comPtr); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IFace2 getIFace2FromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new IFace2JCW(unk); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IFace2 convertComPtrToIFace2(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IFace2JCW(comPtr,true,releaseComPtr); }
  protected IFace2JCW(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected IFace2JCW(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IFace2.IID); }
  protected IFace2JCW(int comPtr,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(comPtr,guid); }
  protected IFace2JCW(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IFace2.IID); }
  protected IFace2JCW(com.inzoom.comjni.IUnknown unk,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(unk,guid); }
  protected IFace2JCW(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IFace2.IID,releaseComPtr);}
  protected IFace2JCW(int comPtr,com.inzoom.util.Guid iid,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,iid,releaseComPtr); }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPosition evalClosestPoint(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPosition pPos) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)pPos,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(108,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IFace2.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPosition rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IPositionJCW.getIPositionFromComPtr(_v[1].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public com.inzoom.comjni.Variant evalEdgeIntersections(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEdge2 Edge) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)Edge,false),
      new com.inzoom.comjni.Variant(new com.inzoom.comjni.Variant(),true)
    };
    vtblCall(112,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IFace2.IID);
    com.inzoom.comjni.Variant rv = _v[1].getVariant();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public boolean applyTexture(String FileName,int XTile,int YTile,boolean SwapU,boolean SwapV) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(FileName,false),
      new com.inzoom.comjni.Variant(XTile,com.inzoom.comjni.enum.VarType.INT,false),
      new com.inzoom.comjni.Variant(YTile,com.inzoom.comjni.enum.VarType.INT,false),
      new com.inzoom.comjni.Variant(SwapU,false),
      new com.inzoom.comjni.Variant(SwapV,false),
      new com.inzoom.comjni.Variant((boolean)false,true)
    };
    vtblCall(116,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IFace2.IID);
    boolean rv = _v[5].getBoolean();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public boolean applyTexture(String FileName,int XTile,int YTile,boolean SwapU) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(FileName,false),
      new com.inzoom.comjni.Variant(XTile,com.inzoom.comjni.enum.VarType.INT,false),
      new com.inzoom.comjni.Variant(YTile,com.inzoom.comjni.enum.VarType.INT,false),
      new com.inzoom.comjni.Variant(SwapU,false),
      new com.inzoom.comjni.Variant((boolean)false,false),
      new com.inzoom.comjni.Variant((boolean)false,true)
    };
    vtblCall(116,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IFace2.IID);
    boolean rv = _v[5].getBoolean();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public boolean applyTexture(String FileName,int XTile,int YTile) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(FileName,false),
      new com.inzoom.comjni.Variant(XTile,com.inzoom.comjni.enum.VarType.INT,false),
      new com.inzoom.comjni.Variant(YTile,com.inzoom.comjni.enum.VarType.INT,false),
      new com.inzoom.comjni.Variant((boolean)false,false),
      new com.inzoom.comjni.Variant((boolean)false,false),
      new com.inzoom.comjni.Variant((boolean)false,true)
    };
    vtblCall(116,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IFace2.IID);
    boolean rv = _v[5].getBoolean();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public boolean applyTexture(String FileName,int XTile) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(FileName,false),
      new com.inzoom.comjni.Variant(XTile,com.inzoom.comjni.enum.VarType.INT,false),
      new com.inzoom.comjni.Variant((int)1,com.inzoom.comjni.enum.VarType.INT,false),
      new com.inzoom.comjni.Variant((boolean)false,false),
      new com.inzoom.comjni.Variant((boolean)false,false),
      new com.inzoom.comjni.Variant((boolean)false,true)
    };
    vtblCall(116,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IFace2.IID);
    boolean rv = _v[5].getBoolean();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public boolean applyTexture(String FileName) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(FileName,false),
      new com.inzoom.comjni.Variant((int)1,com.inzoom.comjni.enum.VarType.INT,false),
      new com.inzoom.comjni.Variant((int)1,com.inzoom.comjni.enum.VarType.INT,false),
      new com.inzoom.comjni.Variant((boolean)false,false),
      new com.inzoom.comjni.Variant((boolean)false,false),
      new com.inzoom.comjni.Variant((boolean)false,true)
    };
    vtblCall(116,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IFace2.IID);
    boolean rv = _v[5].getBoolean();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public boolean removeTexture() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((boolean)false,true)
    };
    vtblCall(120,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IFace2.IID);
    boolean rv = _v[0].getBoolean();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
}
