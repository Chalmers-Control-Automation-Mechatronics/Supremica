package org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw;

// interface CommandBar Implementation
public class CommandBarJCW extends org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw._IMsoOleAccDispObjJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar {
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar getCommandBarFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new CommandBarJCW(comPtr,bAddRef); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar getCommandBarFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new CommandBarJCW(comPtr); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar getCommandBarFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new CommandBarJCW(unk); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar convertComPtrToCommandBar(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new CommandBarJCW(comPtr,true,releaseComPtr); }
  protected CommandBarJCW(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected CommandBarJCW(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar.IID); }
  protected CommandBarJCW(int comPtr,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(comPtr,guid); }
  protected CommandBarJCW(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar.IID); }
  protected CommandBarJCW(com.inzoom.comjni.IUnknown unk,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(unk,guid); }
  protected CommandBarJCW(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar.IID,releaseComPtr);}
  protected CommandBarJCW(int comPtr,com.inzoom.util.Guid iid,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,iid,releaseComPtr); }
  public boolean getBuiltIn() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((boolean)false,true)
    };
    vtblCall(120,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar.IID);
    boolean rv = _v[0].getBoolean();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public String getContext() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((String)null,true)
    };
    vtblCall(124,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar.IID);
    String rv = _v[0].getString();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setContext(String pbstrContext) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pbstrContext,false)
    };
    vtblCall(128,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControls getControls() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(132,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControls rv = org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw.CommandBarControlsJCW.getCommandBarControlsFromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void delete() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
    };
    vtblCall(136,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public boolean getEnabled() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((boolean)false,true)
    };
    vtblCall(140,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar.IID);
    boolean rv = _v[0].getBoolean();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setEnabled(boolean pvarfEnabled) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pvarfEnabled,false)
    };
    vtblCall(144,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl findControl(com.inzoom.comjni.Variant Type,com.inzoom.comjni.Variant Id,com.inzoom.comjni.Variant Tag,com.inzoom.comjni.Variant Visible,com.inzoom.comjni.Variant Recursive) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      createVTblVArg(Type),
      createVTblVArg(Id),
      createVTblVArg(Tag),
      createVTblVArg(Visible),
      createVTblVArg(Recursive),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(148,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl rv = org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw.CommandBarControlJCW.getCommandBarControlFromComPtr(_v[5].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl findControl(com.inzoom.comjni.Variant Type,com.inzoom.comjni.Variant Id,com.inzoom.comjni.Variant Tag,com.inzoom.comjni.Variant Visible) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant noParam = com.inzoom.comjni.Variant.createNoParam();
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      createVTblVArg(Type),
      createVTblVArg(Id),
      createVTblVArg(Tag),
      createVTblVArg(Visible),
      createVTblVArg(noParam),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(148,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl rv = org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw.CommandBarControlJCW.getCommandBarControlFromComPtr(_v[5].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl findControl(com.inzoom.comjni.Variant Type,com.inzoom.comjni.Variant Id,com.inzoom.comjni.Variant Tag) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant noParam = com.inzoom.comjni.Variant.createNoParam();
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      createVTblVArg(Type),
      createVTblVArg(Id),
      createVTblVArg(Tag),
      createVTblVArg(noParam),
      createVTblVArg(noParam),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(148,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl rv = org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw.CommandBarControlJCW.getCommandBarControlFromComPtr(_v[5].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl findControl(com.inzoom.comjni.Variant Type,com.inzoom.comjni.Variant Id) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant noParam = com.inzoom.comjni.Variant.createNoParam();
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      createVTblVArg(Type),
      createVTblVArg(Id),
      createVTblVArg(noParam),
      createVTblVArg(noParam),
      createVTblVArg(noParam),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(148,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl rv = org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw.CommandBarControlJCW.getCommandBarControlFromComPtr(_v[5].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl findControl(com.inzoom.comjni.Variant Type) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant noParam = com.inzoom.comjni.Variant.createNoParam();
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      createVTblVArg(Type),
      createVTblVArg(noParam),
      createVTblVArg(noParam),
      createVTblVArg(noParam),
      createVTblVArg(noParam),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(148,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl rv = org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw.CommandBarControlJCW.getCommandBarControlFromComPtr(_v[5].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl findControl() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant noParam = com.inzoom.comjni.Variant.createNoParam();
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      createVTblVArg(noParam),
      createVTblVArg(noParam),
      createVTblVArg(noParam),
      createVTblVArg(noParam),
      createVTblVArg(noParam),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(148,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl rv = org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw.CommandBarControlJCW.getCommandBarControlFromComPtr(_v[5].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public int getHeight() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,com.inzoom.comjni.enum.VarType.INT,true)
    };
    vtblCall(152,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setHeight(int pdy) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pdy,com.inzoom.comjni.enum.VarType.INT,false)
    };
    vtblCall(156,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getIndex() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,com.inzoom.comjni.enum.VarType.INT,true)
    };
    vtblCall(160,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public int getInstanceId() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(164,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public int getLeft() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,com.inzoom.comjni.enum.VarType.INT,true)
    };
    vtblCall(168,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setLeft(int pxpLeft) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pxpLeft,com.inzoom.comjni.enum.VarType.INT,false)
    };
    vtblCall(172,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public String getName() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((String)null,true)
    };
    vtblCall(176,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar.IID);
    String rv = _v[0].getString();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setName(String pbstrName) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pbstrName,false)
    };
    vtblCall(180,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public String getNameLocal() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((String)null,true)
    };
    vtblCall(184,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar.IID);
    String rv = _v[0].getString();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setNameLocal(String pbstrNameLocal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pbstrNameLocal,false)
    };
    vtblCall(188,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public com.inzoom.comjni.IDispatch getParent() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(192,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar.IID);
    com.inzoom.comjni.IDispatch rv = com.inzoom.comjni.jcw.IDispatchJCW.getIDispatchFromPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public int getPosition() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(196,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setPosition(int ppos) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(ppos,false)
    };
    vtblCall(200,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getRowIndex() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,com.inzoom.comjni.enum.VarType.INT,true)
    };
    vtblCall(204,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setRowIndex(int piRow) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(piRow,com.inzoom.comjni.enum.VarType.INT,false)
    };
    vtblCall(208,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getProtection() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(212,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setProtection(int pprot) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pprot,false)
    };
    vtblCall(216,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void reset() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
    };
    vtblCall(220,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void showPopup(com.inzoom.comjni.Variant x,com.inzoom.comjni.Variant y) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      createVTblVArg(x),
      createVTblVArg(y)
    };
    vtblCall(224,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void showPopup(com.inzoom.comjni.Variant x) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant noParam = com.inzoom.comjni.Variant.createNoParam();
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      createVTblVArg(x),
      createVTblVArg(noParam)
    };
    vtblCall(224,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void showPopup() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant noParam = com.inzoom.comjni.Variant.createNoParam();
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      createVTblVArg(noParam),
      createVTblVArg(noParam)
    };
    vtblCall(224,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getTop() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,com.inzoom.comjni.enum.VarType.INT,true)
    };
    vtblCall(228,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setTop(int pypTop) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pypTop,com.inzoom.comjni.enum.VarType.INT,false)
    };
    vtblCall(232,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getType() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(236,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public boolean getVisible() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((boolean)false,true)
    };
    vtblCall(240,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar.IID);
    boolean rv = _v[0].getBoolean();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setVisible(boolean pvarfVisible) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pvarfVisible,false)
    };
    vtblCall(244,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getWidth() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,com.inzoom.comjni.enum.VarType.INT,true)
    };
    vtblCall(248,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setWidth(int pdx) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pdx,com.inzoom.comjni.enum.VarType.INT,false)
    };
    vtblCall(252,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public boolean getAdaptiveMenu() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((boolean)false,true)
    };
    vtblCall(256,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar.IID);
    boolean rv = _v[0].getBoolean();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setAdaptiveMenu(boolean pvarfAdaptiveMenu) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pvarfAdaptiveMenu,false)
    };
    vtblCall(260,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
}
