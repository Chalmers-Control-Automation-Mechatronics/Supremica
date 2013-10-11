package net.sourceforge.waters.analysis.po;

public class PartialOrderStateTuplePairing
{
  private PartialOrderStateTuple mPrev;
  private PartialOrderStateTuple mState;
  private PartialOrderParingRequest mReq;

  public PartialOrderStateTuplePairing(final PartialOrderStateTuple state,
                                       final PartialOrderStateTuple prev,
                                       final PartialOrderParingRequest req){
    mPrev = prev;
    mState = state;
    mReq = req;
  }

  public PartialOrderStateTuple getPrev(){
    return mPrev;
  }

  public void setPrev(final PartialOrderStateTuple prev){
    mPrev = prev;
  }

  public PartialOrderStateTuple getState(){
    return mState;
  }

  public void setState(final PartialOrderStateTuple state){
    mState = state;
  }

  public PartialOrderParingRequest getReq(){
    return mReq;
  }

  public void setReq(final PartialOrderParingRequest req){
    mReq = req;
  }
}
