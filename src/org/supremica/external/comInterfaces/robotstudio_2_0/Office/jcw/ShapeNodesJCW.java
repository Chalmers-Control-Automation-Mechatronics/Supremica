package org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw;

// interface ShapeNodes Implementation
public class ShapeNodesJCW extends org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw._IMsoDispObjJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.Office.ShapeNodes {
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.ShapeNodes getShapeNodesFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ShapeNodesJCW(comPtr,bAddRef); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.ShapeNodes getShapeNodesFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ShapeNodesJCW(comPtr); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.ShapeNodes getShapeNodesFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new ShapeNodesJCW(unk); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.ShapeNodes convertComPtrToShapeNodes(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ShapeNodesJCW(comPtr,true,releaseComPtr); }
  protected ShapeNodesJCW(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected ShapeNodesJCW(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ShapeNodes.IID); }
  protected ShapeNodesJCW(int comPtr,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(comPtr,guid); }
  protected ShapeNodesJCW(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ShapeNodes.IID); }
  protected ShapeNodesJCW(com.inzoom.comjni.IUnknown unk,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(unk,guid); }
  protected ShapeNodesJCW(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ShapeNodes.IID,releaseComPtr);}
  protected ShapeNodesJCW(int comPtr,com.inzoom.util.Guid iid,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,iid,releaseComPtr); }
  public com.inzoom.comjni.IDispatch getParent() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(36,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ShapeNodes.IID);
    com.inzoom.comjni.IDispatch rv = com.inzoom.comjni.jcw.IDispatchJCW.getIDispatchFromPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public int getCount() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,com.inzoom.comjni.enum.VarType.INT,true)
    };
    vtblCall(40,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ShapeNodes.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.ShapeNode item(com.inzoom.comjni.Variant Index) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      createVTblVArg(Index),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(44,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ShapeNodes.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.Office.ShapeNode rv = org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw.ShapeNodeJCW.getShapeNodeFromComPtr(_v[1].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public com.inzoom.comjni.IUnknown get_NewEnum() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IUnknown)null,true)
    };
    vtblCall(48,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ShapeNodes.IID);
    com.inzoom.comjni.IUnknown rv = com.inzoom.comjni.jcw.IUnknownJCW.getIUnknownFromPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void delete(int Index) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(Index,com.inzoom.comjni.enum.VarType.INT,false)
    };
    vtblCall(52,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ShapeNodes.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void insert(int Index,int SegmentType,int EditingType,float X1,float Y1,float X2,float Y2,float X3,float Y3) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(Index,com.inzoom.comjni.enum.VarType.INT,false),
      new com.inzoom.comjni.Variant(SegmentType,false),
      new com.inzoom.comjni.Variant(EditingType,false),
      new com.inzoom.comjni.Variant(X1,false),
      new com.inzoom.comjni.Variant(Y1,false),
      new com.inzoom.comjni.Variant(X2,false),
      new com.inzoom.comjni.Variant(Y2,false),
      new com.inzoom.comjni.Variant(X3,false),
      new com.inzoom.comjni.Variant(Y3,false)
    };
    vtblCall(56,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ShapeNodes.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void insert(int Index,int SegmentType,int EditingType,float X1,float Y1,float X2,float Y2,float X3) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(Index,com.inzoom.comjni.enum.VarType.INT,false),
      new com.inzoom.comjni.Variant(SegmentType,false),
      new com.inzoom.comjni.Variant(EditingType,false),
      new com.inzoom.comjni.Variant(X1,false),
      new com.inzoom.comjni.Variant(Y1,false),
      new com.inzoom.comjni.Variant(X2,false),
      new com.inzoom.comjni.Variant(Y2,false),
      new com.inzoom.comjni.Variant(X3,false),
      new com.inzoom.comjni.Variant((float)0.0,false)
    };
    vtblCall(56,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ShapeNodes.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void insert(int Index,int SegmentType,int EditingType,float X1,float Y1,float X2,float Y2) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(Index,com.inzoom.comjni.enum.VarType.INT,false),
      new com.inzoom.comjni.Variant(SegmentType,false),
      new com.inzoom.comjni.Variant(EditingType,false),
      new com.inzoom.comjni.Variant(X1,false),
      new com.inzoom.comjni.Variant(Y1,false),
      new com.inzoom.comjni.Variant(X2,false),
      new com.inzoom.comjni.Variant(Y2,false),
      new com.inzoom.comjni.Variant((float)0.0,false),
      new com.inzoom.comjni.Variant((float)0.0,false)
    };
    vtblCall(56,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ShapeNodes.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void insert(int Index,int SegmentType,int EditingType,float X1,float Y1,float X2) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(Index,com.inzoom.comjni.enum.VarType.INT,false),
      new com.inzoom.comjni.Variant(SegmentType,false),
      new com.inzoom.comjni.Variant(EditingType,false),
      new com.inzoom.comjni.Variant(X1,false),
      new com.inzoom.comjni.Variant(Y1,false),
      new com.inzoom.comjni.Variant(X2,false),
      new com.inzoom.comjni.Variant((float)0.0,false),
      new com.inzoom.comjni.Variant((float)0.0,false),
      new com.inzoom.comjni.Variant((float)0.0,false)
    };
    vtblCall(56,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ShapeNodes.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void insert(int Index,int SegmentType,int EditingType,float X1,float Y1) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(Index,com.inzoom.comjni.enum.VarType.INT,false),
      new com.inzoom.comjni.Variant(SegmentType,false),
      new com.inzoom.comjni.Variant(EditingType,false),
      new com.inzoom.comjni.Variant(X1,false),
      new com.inzoom.comjni.Variant(Y1,false),
      new com.inzoom.comjni.Variant((float)0.0,false),
      new com.inzoom.comjni.Variant((float)0.0,false),
      new com.inzoom.comjni.Variant((float)0.0,false),
      new com.inzoom.comjni.Variant((float)0.0,false)
    };
    vtblCall(56,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ShapeNodes.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void setEditingType(int Index,int EditingType) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(Index,com.inzoom.comjni.enum.VarType.INT,false),
      new com.inzoom.comjni.Variant(EditingType,false)
    };
    vtblCall(60,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ShapeNodes.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void setPosition(int Index,float X1,float Y1) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(Index,com.inzoom.comjni.enum.VarType.INT,false),
      new com.inzoom.comjni.Variant(X1,false),
      new com.inzoom.comjni.Variant(Y1,false)
    };
    vtblCall(64,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ShapeNodes.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void setSegmentType(int Index,int SegmentType) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(Index,com.inzoom.comjni.enum.VarType.INT,false),
      new com.inzoom.comjni.Variant(SegmentType,false)
    };
    vtblCall(68,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ShapeNodes.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
}
