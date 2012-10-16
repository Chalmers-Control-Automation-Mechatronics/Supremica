package net.sourceforge.waters.analysis.po;

public class PartialOrderEventDependencyTuple
{
   public PartialOrderEventDependencyTuple(final int dependencyCoupling, final PartialOrderEventDependencyKind dependencyKind){
     mDependencyCoupling = dependencyCoupling;
     mDependencyKind = dependencyKind;
   }

   @SuppressWarnings("unused")
  private final int mDependencyCoupling;
   @SuppressWarnings("unused")
  private final PartialOrderEventDependencyKind mDependencyKind;
}
