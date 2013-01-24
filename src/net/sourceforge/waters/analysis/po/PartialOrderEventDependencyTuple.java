package net.sourceforge.waters.analysis.po;

public class PartialOrderEventDependencyTuple
{
   public PartialOrderEventDependencyTuple(final int dependencyCoupling, final PartialOrderEventDependencyKind dependencyKind){
     mDependencyCoupling = dependencyCoupling;
     mDependencyKind = dependencyKind;
   }

   public PartialOrderEventDependencyKind getKind(){
     return mDependencyKind;
   }

   public int getCoupling(){
     return mDependencyCoupling;
   }
   public String toString(){
     return String.valueOf(mDependencyCoupling);
   }

  private final int mDependencyCoupling;
  private final PartialOrderEventDependencyKind mDependencyKind;
}
