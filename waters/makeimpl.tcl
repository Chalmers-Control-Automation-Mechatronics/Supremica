# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
###########################################################################
# Copyright (C) 2004-2018 Robi Malik
###########################################################################
# This file is part of Waters.
# Waters is free software: you can redistribute it and/or modify it under
# the terms of the GNU General Public License as published by the Free 
# Software Foundation, either version 2 of the License, or(at your option)
# any later version.
# Waters is distributed in the hope that it will be useful, but WITHOUT ANY 
# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS 
# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
# details.
# You should have received a copy of the GNU General Public License along
# with Waters. If not, see <http://www.gnu.org/licenses/>.
#
# Linking Waters statically or dynamically with other modules is making a
# combined work based on Waters. Thus, the terms and conditions of the GNU
# General Public License cover the whole combination.
# In addition, as a special exception, the copyright holders of Waters give
# you permission to combine Waters with code included in the standard
# release of Supremica under the Supremica Software License Agreement (or
# modified versions of such code, with unchanged license). You may copy and
# distribute such a system following the terms of the GNU GPL for Waters and
# the licenses of the other code concerned.
# Note that people who make modified versions of Waters are not obligated to
# grant this special exception for their modified versions; it is their
# choice whether to do so. The GNU General Public License gives permission
# to release a modified version without this exception; this exception also
# makes it possible to release a modified version which carries forward this
# exception.
###########################################################################
# source waters/makeimpl.tcl
# Java_ProcessProxies
###########################################################################

proc Java_ProcessProxies {} {
  set impls [list "plain" "subject"]
  Java_ProcessPackage $impls "module" "Module"
  #Java_ProcessPackage "des" "ProductDES"
  return ""
}

proc Java_ProcessPackage {impls subpack prefix} {
  Java_CollectGlobalImports importMap

  puts "Generating $subpack classes ..."
  set root [file join "src" "net" "sourceforge" "waters"]
  set indir [file join $root "model" $subpack]
  set joker [file join $indir "*Proxy.java"]
  set matches [glob -nocomplain $joker]
  foreach srcname $matches {
    set tail [file tail $srcname]
    set classinfo [Java_ExtractFileInfo $srcname importMap]
    set classname [Java_ClassGetName $classinfo]
    if {[string compare $classname ""] == 0} {
      continue
    } elseif {[string compare $tail "$classname.java"] != 0} {
      puts stderr "WARNING: Class name '$classname' does not match file name!"
      continue
    }
    set classMap($classname) $classinfo
  }
  set classes [array names classMap]

  foreach impl $impls {
    Java_ProcessClassHierarchy $impl $subpack classMap implClassMap
    set implobjname [Java_GetImplObjectName $impl]
    set outdir [file join $root $impl $subpack]
    catch {file mkdir $outdir}
    foreach classname $classes {
      regsub {Proxy$} $classname $implobjname outname
      set destname [file join $outdir "$outname.java"]
      set classinfo $implClassMap($classname)
      Java_GenerateClass $impl $subpack \
          $prefix $destname $classinfo implClassMap importMap
    }
    set destname [file join $outdir "${prefix}${implobjname}Factory.java"]
    Java_GenerateFactory $impl $subpack $prefix $destname $classes 0 \
        implClassMap importMap
  }

  set destname [file join $indir "${prefix}ProxyFactory.java"]
  Java_GenerateFactory "model" $subpack $prefix $destname $classes 1 \
      implClassMap importMap
  set destname [file join $indir "${prefix}ProxyVisitor.java"]
  Java_GenerateVisitor $subpack $prefix $destname $classes "Iface" \
      implClassMap importMap
  set destname [file join $indir "Default${prefix}ProxyVisitor.java"]
  Java_GenerateVisitor $subpack $prefix $destname $classes "Default" \
      implClassMap importMap
  set destname [file join $indir "Descending${prefix}ProxyVisitor.java"]
  Java_GenerateVisitor $subpack $prefix $destname $classes "Descending" \
      implClassMap importMap
  set destname [file join $indir "${prefix}ProxyCloner.java"]
  Java_GenerateCloningVisitor $subpack $prefix $destname $classes \
      implClassMap importMap
  set destname [file join $indir "${prefix}HashCodeVisitor.java"]
  Java_GenerateHashCodeVisitor $subpack $prefix $destname $classes \
      implClassMap importMap
  set destname [file join $indir "${prefix}EqualityVisitor.java"]
  Java_GenerateEqualityVisitor $subpack $prefix $destname $classes \
      implClassMap importMap
}

proc Java_ExtractFileInfo {srcname importMapName} {
  upvar $importMapName importMap
  set attribs ""
  set inclass 0
  set incomment 0
  set optional 0
  set ref 0
  set forcedgeo 0
  set short ""
  set dftvalue ""
  set instream [open $srcname r]
  while {![eof $instream]} {
    set line [gets $instream]
    set line [string trim $line]
    while {[string length $line] > 0} {
      if {$incomment} {
        set eoc [string first "*/" $line]
        if {$eoc < 0} {
          set thiscomment $line
          set line ""
        } else {
          set last [expr $eoc - 1]
          set thiscomment [string range $line 0 $last]
          set thiscomment [string trim $thiscomment]
          set last [expr $eoc + 2]
          set line [string range $line $last end]
          set incomment 0
        }
        regsub {^\* *} $thiscomment "" thiscomment
        set comment [concat $comment $thiscomment]
      } elseif {[regexp {^/\*\*} $line all]} {
        set len [string length $all]
        set line [string range $line $len end]
        set line [string trim $line]
        set incomment 1
        set comment ""
      } elseif {!$inclass} {
        if {[regexp {^// +@short +(.*) *$} $line all short]} {
          set line ""
        } elseif {[regexp {^import +([a-z\.]+)\.([A-Z][A-Za-z0-9]+);} \
                 $line all pack class]} {
          set importMap($class) $pack
          set len [string length $all]
          set line [string range $line $len end]
          set line [string trim $line]
        } elseif {[regexp {^public +interface} $line all]} {
          set headerinfo [Java_ExtractHeader $instream $line "\{"]
          set header [lindex $headerinfo 0]
          if {[regexp \
                {^public +interface +([A-Z][A-Za-z]+) +extends +([A-Z].*) *$} \
                $header all classname words]} {
            set match 1
            set words [split $words ","]
            set parents ""
            foreach word $words {
              if {[regexp {^ *([A-Z][A-Za-z0-9_<>]*) *$} $word all word]} {
                lappend parents $word
              } else {
                set match 0
                break
              }
            }
          } else {
            set match 0
          }
          if {!$match} {
            puts stderr "WARNING: Failed to parse class header:"
            puts stderr $header
            break
          }
          set line [lindex $headerinfo 1]
          set line [string trim $line]
          set parent [lindex $parents 0]
          set interfaces [lrange $parents 1 end]
          set comment ""
          set nextid 0
          set inclass 1
        } else {
          set line ""
        }
      } else {
        if {[regexp {^// +@optional} $line all]} {
          set optional 1
          set dftvalue "null"
          set line ""
        } elseif {[regexp {^// +@ref} $line all]} {
          set ref 1
          set line ""
        } elseif {[regexp {^// +@geometry} $line all]} {
          set forcedgeo 1
          set line ""
        } elseif {[regexp {^// +@default ([a-zA-Z0-9_\.]+)} \
                       $line all dftvalue]} {
          set line ""
        } elseif {[regexp {^public +[A-Za-z]} $line all]} {
          set headerinfo [Java_ExtractHeader $instream $line ";"]
          set header [lindex $headerinfo 0]
          if {[regexp \
                   {^public +([a-zA-Z0-9<>,]+) +get([A-Z][A-Za-z0-9]+) *\(\)} \
                   $header all type name] ||
              [regexp \
                   {^public +(boolean) +is([A-Z][A-Za-z0-9]+) *\(\)} \
                   $header all type name]} {
            if {$optional} {
              set eqstatus "optional"
              set optional 0
            } elseif {$forcedgeo || [regexp {Geometry} $type all]} {
              set eqstatus "geometry"
              set forcedgeo 0
            } else {
              set eqstatus "required"
            }
            if {$ref} {
              set refstatus "ref"
              set ref 0
            } else {
              set refstatus "owned"
            }
            incr nextid
            set attrib [Java_AttribCreate $type $name $nextid $eqstatus \
                            $refstatus $dftvalue $comment]
            lappend attribs $attrib
            set dftvalue ""
            set comment ""
          } elseif {[regexp \
                    {^public +static +final +[A-Za-z0-9_]+ +[A-Za-z0-9_]+ *=} \
                    $header all] ||
                    [regexp \
                    {^public +[A-Za-z0-9_]+ +clone *\(\)} $header all]} {
            # ignore ...
          } else {
            puts stderr "WARNING: Failed to parse method header:"
            puts stderr $header
          }
          set line [lindex $headerinfo 1]
          set line [string trim $line]
        } else {
          set line ""
        }
      }
    }
  }
  close $instream
  if {[info exists classname]} {
    return [Java_ClassCreate $classname $short $parent $interfaces $attribs]
  } else {
    return ""
  }
}


proc Java_ExtractHeader {instream line sep} {
  set header ""
  while {1} {
    set index [string first $sep $line]
    if {$index < 0} {
      set used $line
    } else {
      set prev [expr $index - 1]
      set next [expr $index + 1]
      set used [string range $line 0 $prev]
      set used [string trim $used]
      set line [string range $line $next end]
      set line [string trim $line]
    }
    if {[string length $header] > 0} {
      set header "$header $used"
    } else {
      set header $used
    }
    if {$index >= 0} {
      break
    } elseif {[eof $instream]} {
      set line ""
      break
    }
    set line [gets $instream]
    set line [string trim $line]
  }
  return [list $header $line]
}


proc Java_InitClassMap {classMapName} {
  upvar $classMapName classMap
  set attribName \
      [Java_AttribCreate "String" "Name" 1 "required" "owned" "" "name"]
  set attribComment \
      [Java_AttribCreate "String" "Comment" 1 "optional" "owned" "null" \
           "comment"]
  set attribLocation \
      [Java_AttribCreate "URI" "Location" 2 "ignored" "owned" "" \
           "file system location"]
  set classMap(Proxy) \
      [Java_ClassCreateAbstract "Proxy" "proxy" "" "" ""]
  set classMap(GeometryProxy) \
      [Java_ClassCreateAbstract "GeometryProxy" "geometry information" \
           "Proxy" "" ""]
  set classMap(AbstractNamedProxy) \
      [Java_ClassCreateAbstract "Proxy" "abstract named proxy" "" "" ""]
  set classMap(NamedProxy) \
      [Java_ClassCreateAbstract "NamedProxy" "named proxy" \
           "Proxy" "" [list $attribName]]
  set classMap(DocumentProxy) \
      [Java_ClassCreateAbstract "DocumentProxy" "document" \
           "NamedProxy" "" [list $attribComment $attribLocation]]
}


proc Java_ProcessClassHierarchy {impl subpack inMapName outMapName} {
  upvar $inMapName inClassMap
  upvar $outMapName outClassMap
  set names [array names inClassMap]
  Java_InitClassMap outClassMap
  foreach name $names {
    set outClassMap($name) $inClassMap($name)
  }

  # final (0) or abstract (1)
  set classinfo $outClassMap(Proxy)
  foreach interfacename $names {
    set classinfo $outClassMap($interfacename)
    set superinterfacename [Java_ClassGetParent $classinfo]
    if {[info exists outClassMap($superinterfacename)]} {
      set superclassinfo $outClassMap($superinterfacename)
      set superabstract [Java_ClassIsAbstract $superclassinfo]
      if {[string compare $superabstract ""] == 0} {
        set classinfo $outClassMap($superinterfacename)
        set classinfo [Java_ClassSetAbstract $classinfo 1]
        set outClassMap($superinterfacename) $classinfo
      }
    }
  }
  foreach interfacename $names {
    set classinfo $outClassMap($interfacename)
    set abstract [Java_ClassIsAbstract $classinfo]
    if {[string compare $abstract ""] == 0} {
      set classinfo [Java_ClassSetAbstract $classinfo 0]
      set outClassMap($interfacename) $classinfo
    }
  }

  # no geometry (0), has geometry (1), has geometry immediately (2),
  # or is geometry (3)
  set classinfo $outClassMap(GeometryProxy)
  set classinfo [Java_ClassSetGeometryStatus $classinfo 3]
  set outClassMap(GeometryProxy) $classinfo
  foreach name $names {
    Java_AddGeometryInformation outClassMap $name
  }

  # SPECIALS
  set Impl [Java_GetImplObjectName $impl]
  if {[string compare $subpack "module"] == 0} {
    # IdentifiedProxy
    set classinfo $outClassMap(IdentifiedProxy)
    set classinfo [Java_ClassSetParent $classinfo "AbstractNamedProxy"]
    set special \
        [list "method" \
             "Interface net.sourceforge.waters.model.base.NamedProxy" \
             "String getName()" "" "return mIdentifier.toString();"]
    set classinfo [Java_ClassSetSpecial $classinfo $special]
    set outClassMap(IdentifiedProxy) $classinfo

    # NodeProxy & Co.
    set classinfo $outClassMap(NodeProxy)
    set attribs [Java_ClassGetAttributes $classinfo]
    set index [lsearch -regexp $attribs {ImmediateChildNode}]
    set attrib [lindex $attribs $index]
    set attribs [lreplace $attribs $index $index]
    set classinfo [Java_ClassSetAttributes $classinfo $attribs]
    set outClassMap(NodeProxy) $classinfo

    set classinfo $outClassMap(GroupNodeProxy)
    set attrib [Java_AttribSetRefStatus $attrib "ref"]
    set attribs [Java_ClassGetAttributes $classinfo]
    set attribs [concat [list $attrib] $attribs]
    set classinfo [Java_ClassSetAttributes $classinfo $attribs]
    if {[string compare $impl "subject"] == 0} {
      set special \
        [list "method" \
          "Additional Setters" \
          "void setImmediateChildNodes\n\
           \   (final Collection<? extends NodeSubject> children)" \
          "" \
          "final SetSubject<NodeSubject> oldchildren = mImmediateChildNodes;" \
          "try \{" \
          "  mImmediateChildNodes = new ChildNodeSetSubject(children);" \
          "  final Subject parent = getParent();" \
          "  if (parent != null && parent instanceof NodeSetSubject) \{" \
          "    final NodeSetSubject nodeset = (NodeSetSubject) parent;" \
          "    nodeset.rearrangeGroupNodes();" \
          "  \}" \
          "\} catch (final CyclicGroupNodeException exception) \{" \
          "  mImmediateChildNodes = oldchildren;" \
          "  exception.putOperation(\"Changing children of '\" + getName() + \"'\");" \
          "  throw exception;" \
          "\}"]
      set classinfo [Java_ClassSetSpecial $classinfo $special]
    }
    set outClassMap(GroupNodeProxy) $classinfo

    set classinfo $outClassMap(SimpleNodeProxy)
    set special \
        [list "method" \
             "Interface net.sourceforge.waters.model.module.NodeProxy" \
             "Set<NodeProxy> getImmediateChildNodes()" "" \
             "return Collections.emptySet();"]
    set classinfo [Java_ClassSetSpecial $classinfo $special]
    set outClassMap(SimpleNodeProxy) $classinfo

    # GraphProxy
    set classinfo $outClassMap(GraphProxy)
    set attribs [Java_ClassGetAttributes $classinfo]
    set index [lsearch -regexp $attribs {Nodes}]
    set attrib [lindex $attribs $index]
    set attrib [Java_AttribSetType $attrib "NodeSet$Impl"]
    set attribs [lreplace $attribs $index $index $attrib]
    set classinfo [Java_ClassSetAttributes $classinfo $attribs]
    set outClassMap(GraphProxy) $classinfo
  } elseif {[string compare $subpack "des"] == 0} {
    # TransitionProxy
    set classinfo $outClassMap(TransitionProxy)
    set special \
        [list "method" \
           "Interface java.lang.Comparable" \
           "int compareTo(final TransitionProxy trans)" "" \
           "final int compsource = getSource().compareTo(trans.getSource());" \
           "if (compsource != 0) \{" \
           "  return compsource;" \
           "\}" \
           "final int compevent = getEvent().compareTo(trans.getEvent());" \
           "if (compevent != 0) \{" \
           "  return compevent;" \
           "\}" \
           "return getTarget().compareTo(trans.getTarget());"]
    set classinfo [Java_ClassSetSpecial $classinfo $special]
    set outClassMap(TransitionProxy) $classinfo
  }
}


proc Java_AddGeometryInformation {classMapName interfacename} {
  upvar $classMapName classMap
  if {![info exists classMap($interfacename)]} {
    return 0
  }
  set classinfo $classMap($interfacename)
  set geo [Java_ClassGetGeometryStatus $classinfo]
  if {[string compare $geo ""] != 0} {
    return $geo
  }
  set superinterfacename [Java_ClassGetParent $classinfo]
  set geo [Java_AddGeometryInformation classMap $superinterfacename]
  if {$geo == 3} {
    set classinfo [Java_ClassSetGeometryStatus $classinfo $geo]
    set classMap($interfacename) $classinfo
    return $geo
  } elseif {$geo == 2} {
    set geo 1
  }
  set classMap($interfacename) [Java_ClassSetGeometryStatus $classinfo 0]
  set attribs [Java_ClassGetAttributes $classinfo]
  foreach attrib $attribs {
    set type [Java_AttribGetDeclaredType $attrib ""]
    set eqstatus [Java_AttribGetEqualityStatus $attrib]
    if {[string compare $eqstatus "geometry"] == 0} {
      set geo 2
    } else {
      set parts [split $type "<>, "]
      foreach part $parts {
        set subgeo [Java_AddGeometryInformation classMap $part]
        if {$subgeo == 3} {
          set geo 2
          break
        } elseif {$subgeo > 0} {
          set geo 1
        }
      }
      if {$geo == 2} {
        break
      }
    }
  }
  set classinfo [Java_ClassSetGeometryStatus $classinfo $geo]
  set classMap($interfacename) $classinfo
  return $geo
}


##############################################################################
# Generate Element Classes
##############################################################################

proc Java_GenerateClass {impl subpack prefix destname classinfo
                         classMapName importMapName} {
  upvar $classMapName classMap
  upvar $importMapName importMap
  global gSpaces
  set implobjname [Java_GetImplObjectName $impl]
  set packname "net.sourceforge.waters.$impl.$subpack"
  set interfacename [Java_ClassGetName $classinfo]
  set short [Java_ClassGetShortName $classinfo]
  set superinterfacename [Java_ClassGetParent $classinfo]
  set otherinterfaces [Java_ClassGetInterfaces $classinfo]
  set attribs [Java_ClassGetAttributes $classinfo]
  set abstract [Java_ClassIsAbstract $classinfo]
  set geo [Java_ClassGetGeometryStatus $classinfo]
  set specials [Java_ClassGetSpecials $classinfo]
  set superinfo \
      [Java_GetSuperClassInfo $superinterfacename $implobjname classMap]
  set supername [Java_ClassGetName $superinfo]
  set superclassname [Java_ClassGetShortName $superinfo]
  set superinfo $classMap($supername)
  set hash [Aux_Hash "$superclassname:" 0xabababab]
  regsub {Proxy$} $interfacename $implobjname classname

  ############################################################################
  # Extract Superclass Attributes
  set allattribs $attribs
  set current $superinterfacename
  set numsuperattribus 0
  while {[string length $current] > 0} {
    set currentinfo $classMap($current)
    set currentattribs [Java_ClassGetAttributes $currentinfo]
    incr numsuperattribus [llength $currentattribs]
    set allattribs [concat $currentattribs $allattribs]
    set current [Java_ClassGetParent $currentinfo]
  }

  ############################################################################
  # Prepare and Write Output
  foreach write {0 1} {
    if {$write} {
      set tmpname "$destname.tmp"
      set stream [open $tmpname w]
      set umap ""
      Java_GenerateHeaderComment $stream $packname $classname
      Java_WritePackageAndImports $stream $packname useMap importMap
    } else {
      set stream ""
      set umap useMap
    }

  ############################################################################
  # Write Headers
    if {$write} {
      puts $stream "/**"
      if {[string compare $impl "subject"] == 0} {
        set label "The subject"
      } else {
        set label "An immutable"
      }
      puts $stream \
          " * $label implementation of the {@link $interfacename} interface."
      puts $stream " *"
      puts $stream " * @author Robi Malik"
      puts $stream " */"
      puts $stream ""
    }
    if {$abstract} {
      set mod "abstract"
    } else {
      set mod "final"
    }
    Java_WriteLn $stream $umap "public $mod class $classname"
    Java_WriteLn $stream $umap "  extends $superclassname"
    Java_WriteLn $stream $umap "  implements $interfacename"
    Java_WriteLn $stream $umap "{"

  ############################################################################
  # Constructor
    Java_GenerateSeparatorComment $stream $umap "Constructors"
    Java_WriteConstructorComment $stream $umap $impl \
        "constructor" $short $allattribs 0
    if {$abstract} {
      set access "protected"
    } else {
      set access "public"
    }
    set ctortext "  $access ${classname}("
    Java_Write $stream $umap $ctortext
    set indent [string length $ctortext]
    set indent [string range $gSpaces 1 $indent]
    set i 1
    set numallattribs [llength $allattribs]
    set numdefaults 0
    set exceptions ""
    foreach attrib $allattribs {
      set ctortype [Java_AttribGetConstructorArgumentType $attrib $impl]
      set exception [Java_AttribGetConstructorExceptions \
                         $attrib $impl classMap]
      set paramname [Java_AttribGetParameterName $attrib $impl]
      set dftvalue [Java_AttribGetDefaultValue $attrib $impl]
      Java_Write $stream $umap "final $ctortype $paramname"
      if {[string compare $dftvalue ""] != 0} {
        incr numdefaults
      }
      if {$i < $numallattribs} {
        Java_WriteLn $stream $umap ","
        Java_Write $stream $umap $indent
        incr i
      }
      set exceptions [concat $exceptions $exception]
    }
    Java_WriteLn $stream $umap ")"
    Java_WriteExceptionDeclaration $stream $umap $exceptions
    set numattribs [llength $attribs]
    set numsuperattribs [expr $numallattribs - $numattribs]
    set lastsuperattrib [expr $numsuperattribs - 1]
    set superattribs [lrange $allattribs 0 $lastsuperattrib]
    Java_WriteLn $stream $umap "  \{"
    if {$numsuperattribs > 0} {
      Java_Write $stream $umap "    super("
      set i 1
      foreach attrib $superattribs {
        set paramname [Java_AttribGetParameterName $attrib $impl]
        Java_Write $stream $umap $paramname
        if {$i < $numsuperattribs} {
          Java_Write $stream $umap ", "
          incr i
        }
      }
      Java_WriteLn $stream $umap ");"
    }
    foreach attrib $attribs {
      set membername [Java_AttribGetMemberName $attrib $impl]
      set paramname [Java_AttribGetParameterName $attrib $impl]
      set decltype [Java_AttribGetDeclaredType $attrib $impl]
      set impltype [Java_AttribGetImplementationType $attrib $impl classMap]
      set transformer [Java_AttribGetCollectionTransformerName $attrib $impl]
      set eqstatus [Java_AttribGetEqualityStatus $attrib]
      set refstatus [Java_AttribGetRefStatus $attrib]
      if {[string compare $transformer ""] == 0} {
        set membertype [Java_AttribGetMemberType $attrib $impl classMap]
        set dftvalue [Java_AttribGetDefaultValue $attrib $impl]
        if {[regexp {Subject$} $membertype all]} {
          set typecast "($membertype) "
        } else {
          set typecast ""
        }
        if {[regexp {2D$} $decltype all]} {
          Java_WriteLn $stream $umap \
              "    $membername = ($decltype) $paramname.clone();"
        } elseif {[string compare $dftvalue "empty"] == 0 &&
                  [regexp {Proxy$} $decltype all]} {
          Java_WriteLn $stream $umap "    if ($paramname == null) \{"
          Java_WriteLn $stream $umap "      $membername = new ${impltype}();"
          Java_WriteLn $stream $umap "    \} else \{"
          Java_WriteLn $stream $umap "      $membername = $typecast$paramname;"
          Java_WriteLn $stream $umap "    \}"
        } else {
          Java_WriteLn $stream $umap "    $membername = $typecast$paramname;"
        }
        set impltype $membertype
      } elseif {[string compare $impl "plain"] == 0} {
        set type [Java_AttribGetDeclaredType $attrib $impl]
        if {[regexp {^CloningGeometryListElement<(.*)>$} \
                 $impltype all elemtype]} {
          Java_WriteLn $stream $umap \
              "    $membername = new ${impltype}($paramname);"
        } else {
          if {[string compare $eqstatus "optional"] == 0} {
            set empty "null"
          } else {
            set empty [Java_AttribGetEmptyCollectionsCall $attrib $impl]
          }
          Java_WriteLn $stream $umap "    if ($paramname == null) \{"
          Java_WriteLn $stream $umap "      $membername = $empty;"
          Java_WriteLn $stream $umap "    \} else \{"
          if {[regexp {^Immutable} $impltype all]} {
            Java_WriteLn $stream $umap "      $membername ="
            Java_WriteLn $stream $umap "        new ${impltype}($paramname);"
          } else {
            Java_WriteLn $stream $umap \
                "      final $type ${paramname}Modifiable ="
            Java_WriteLn $stream $umap "        new ${impltype}($paramname);"
            Java_WriteLn $stream $umap "      $membername ="
            Java_WriteLn $stream $umap \
                "        Collections.${transformer}(${paramname}Modifiable);";
          }
          Java_WriteLn $stream $umap "    \}"
        }
      } elseif {[regexp {^.*Subject<(.*Subject)>} $impltype all elemtype]} {
        Java_WriteLn $stream $umap "    if ($paramname == null) \{"
        Java_WriteLn $stream $umap "      $membername = new ${impltype}();"
        Java_WriteLn $stream $umap "    \} else \{"
        Java_WriteLn $stream $umap "      $membername = new ${impltype}"
        Java_WriteLn $stream $umap "        ($paramname, $elemtype.class);"
        Java_WriteLn $stream $umap "    \}"
      } else {
        if {[string compare $eqstatus "optional"] == 0} {
          set empty "null"
        } else {
          set empty "new ${impltype}()"
        }
        Java_WriteLn $stream $umap "    if ($paramname == null) \{"
        Java_WriteLn $stream $umap "      $membername = $empty;"
        Java_WriteLn $stream $umap "    \} else \{"
        Java_WriteLn $stream $umap \
            "      $membername = new ${impltype}($paramname);"
        Java_WriteLn $stream $umap "    \}"
      }
      if {[string compare $impl "plain"] == 0} {
        # nothing
      } elseif {[string compare $refstatus "owned"] == 0} {
        if {[regexp {Subject$} $impltype all] ||
            [Java_IsCollectionType $decltype] &&
            [regexp {Subject} $impltype all]} {
          Java_WriteSetParent $stream $umap $membername $eqstatus "this"
        }
      } else {
        if {[string compare $impl "subject"] == 0 &&
            [Java_IsCollectionType $decltype]} {
          Java_WriteSetParent $stream $umap $membername $eqstatus "this"
        }
      }
    }
    Java_WriteLn $stream $umap "  \}"
    Java_WriteLn $stream $umap ""

    if {$numdefaults > 0} {
      Java_WriteConstructorComment $stream $umap $impl \
          "constructor" $short $allattribs 1
      Java_Write $stream $umap $ctortext
      set numnondefaults [expr $numallattribs - $numdefaults]
      set i 1
      foreach attrib $allattribs {
        set dftvalue [Java_AttribGetDefaultValue $attrib $impl]
        set decltype [Java_AttribGetDeclaredType $attrib $impl]
        set paramname [Java_AttribGetParameterName $attrib $impl]
        if {[string compare $dftvalue ""] == 0} {
          set ctortype [Java_AttribGetConstructorArgumentType $attrib $impl]
          Java_Write $stream $umap "final $ctortype $paramname"
          if {$i < $numnondefaults} {
            Java_WriteLn $stream $umap ","
            Java_Write $stream $umap $indent
            incr i
          }
        }
      }
      Java_WriteLn $stream $umap ")"
      Java_WriteExceptionDeclaration $stream $umap $exceptions
      Java_WriteLn $stream $umap "  \{"
      set thistext "    this("
      set thisindent [string length $thistext]
      set thisindent [string range $gSpaces 1 $thisindent]
      Java_Write $stream $umap $thistext
      set i 1
      foreach attrib $allattribs {
        set dftvalue [Java_AttribGetDefaultValue $attrib $impl]
        if {[string compare $dftvalue "empty"] == 0} {
          Java_Write $stream $umap "null"
        } elseif {[string compare $dftvalue ""] != 0} {
          Java_Write $stream $umap $dftvalue
        } else {
          set paramname [Java_AttribGetParameterName $attrib $impl]
          Java_Write $stream $umap $paramname
        }
        if {$i < $numallattribs} {
          Java_WriteLn $stream $umap ","
          Java_Write $stream $umap $thisindent
          incr i
        }
      }
      Java_WriteLn $stream $umap ");"
      Java_WriteLn $stream $umap "  \}"
      Java_WriteLn $stream $umap ""
    }


  ############################################################################
  # Cloning
    set needassign [expr [string compare $impl "subject"] == 0 && \
                         $numattribs > 0]
    if {$needassign} {
      Java_GenerateSeparatorComment $stream $umap "Cloning and Assigning"
    } else {
      Java_GenerateSeparatorComment $stream $umap "Cloning"
    }
    Java_WriteLn $stream $umap "  @Override"
    Java_WriteLn $stream $umap "  public $classname clone()"
    Java_WriteLn $stream $umap "  \{"
    if {[string compare $impl "plain"] == 0 || $numattribs == 0} {
      Java_WriteLn $stream $umap "    return ($classname) super.clone();"
    } else {
      set implobjname [Java_GetImplObjectName $impl]
      set getinst "${prefix}${implobjname}Factory.getCloningInstance()"
      Java_WriteLn $stream $umap \
          "    final ${prefix}ProxyCloner cloner = ${getinst};"
      Java_WriteLn $stream $umap \
          "    return ($classname) cloner.getClone(this);"
    }
    Java_WriteLn $stream $umap "  \}"
    Java_WriteLn $stream $umap ""


  ############################################################################
  # Assigning
    if {$needassign} {
      set literal 0
      foreach attrib $attribs {
        set type [Java_AttribGetDeclaredType $attrib $impl]
        if {![Java_IsCollectionType $type]} {
          set literal 1
          break
        }
      }
      if {$literal} {
        Java_WriteLn $stream $umap "  @Override"
        Java_WriteLn $stream $umap \
            "  public ModelChangeEvent assignMember(final int index,"
        Java_WriteLn $stream $umap \
            "                                       final Object oldValue,"
        Java_WriteLn $stream $umap \
            "                                       final Object newValue)"
        Java_WriteLn $stream $umap "  \{"
        Java_WriteLn $stream $umap "    if (index <= $numsuperattribs) \{"
        Java_WriteLn $stream $umap \
            "      return super.assignMember(index, oldValue, newValue);"
        Java_WriteLn $stream $umap "    \} else \{"
        Java_WriteLn $stream $umap "      switch (index) \{"
        foreach attrib $attribs {
          set decltype [Java_AttribGetDeclaredType $attrib $impl]
          if {[Java_IsCollectionType $decltype]} {
            continue
          }
          set id [Java_AttribGetId $attrib]
          incr id $numsuperattribs
          set type [Java_AttribGetMemberType $attrib $impl classMap]
          set casttype [Java_GetObjectAssignableType $type]
          set membername [Java_AttribGetMemberName $attrib $impl]
          set eqstatus [Java_AttribGetEqualityStatus $attrib]
          set refstatus [Java_AttribGetRefStatus $attrib]
          set doparent [expr \
                            [regexp {Proxy$} $decltype all] && \
                            [string compare $refstatus "owned"] == 0]
          Java_WriteLn $stream $umap "      case $id:"
          if {$doparent} {
            if {[string compare $eqstatus "required"] == 0} {
              Java_WriteLn $stream $umap "        $membername.setParent(null);"
            } else {
              Java_WriteLn $stream $umap "        if ($membername != null) \{"
              Java_WriteLn $stream $umap \
                  "          $membername.setParent(null);"
              Java_WriteLn $stream $umap "        \}"
            }
          }
          Java_WriteLn $stream $umap \
              "        $membername = ($casttype) newValue;"
          if {$doparent} {
            if {[string compare $eqstatus "required"] == 0} {
              Java_WriteLn $stream $umap "        $membername.setParent(this);"
            } else {
              Java_WriteLn $stream $umap "        if ($membername != null) \{"
              Java_WriteLn $stream $umap \
                  "          $membername.setParent(this);"
              Java_WriteLn $stream $umap "        \}"
            }
          }
          if {[string compare $eqstatus "geometry"] == 0 ||
              [regexp {Geometry} $classname all]} {
            Java_WriteLn $stream $umap \
                "        return ModelChangeEvent.createGeometryChanged(this, newValue);"
          } else {
            Java_WriteLn $stream $umap \
                "        return ModelChangeEvent.createStateChanged(this);"
          }
        }
        Java_WriteLn $stream $umap "      default:"
        Java_WriteLn $stream $umap "        return null;"
        Java_WriteLn $stream $umap "      \}"
        Java_WriteLn $stream $umap "    \}"
        Java_WriteLn $stream $umap "  \}"
        Java_WriteLn $stream $umap ""
      }

      Java_WriteLn $stream $umap "  @Override"
      Java_WriteLn $stream $umap \
          "  protected void collectUndoInfo(final ProxySubject newState,"
      Java_WriteLn $stream $umap \
          "                                 final RecursiveUndoInfo info,"
      Java_WriteLn $stream $umap \
          "                                 final Set<? extends Subject> boundary)"
      Java_WriteLn $stream $umap "  \{"
      Java_WriteLn $stream $umap "    super.collectUndoInfo(newState, info, boundary);"
      Java_WriteLn $stream $umap \
          "    final $classname downcast = ($classname) newState;"
      foreach attrib $attribs {
        set id [Java_AttribGetId $attrib]
        incr id $numsuperattribs
        set decltype [Java_AttribGetDeclaredType $attrib $impl]
        set type [Java_AttribGetMemberType $attrib $impl classMap]
        set membername [Java_AttribGetMemberName $attrib $impl]
        set eqstatus [Java_AttribGetEqualityStatus $attrib]
        if {[regexp {^[a-z]} $decltype all]} {
          Java_WriteLn $stream $umap \
              "    if ($membername != downcast.$membername) \{"
          Java_WriteLn $stream $umap \
              "      final UndoInfo step$id = new ReplacementUndoInfo($id, $membername, downcast.$membername);"
          Java_WriteLn $stream $umap "      info.add(step$id);"
          Java_WriteLn $stream $umap "    \}"
        } elseif {[Java_IsCollectionType $decltype]} {
          if {[string compare $eqstatus "required"] == 0} {
            Java_WriteLn $stream $umap "    final UndoInfo step$id = $membername.createUndoInfo(downcast.$membername, boundary);"
            Java_WriteLn $stream $umap "    if (step$id != null) \{"
            Java_WriteLn $stream $umap "      info.add(step$id);"
            Java_WriteLn $stream $umap "    \}"
          } else {
            puts stderr "ERROR: optional collection $membername in $classname!"
            exit 1
          }
        } elseif {[regexp {Proxy$} $decltype all]} {
          set refstatus [Java_AttribGetRefStatus $attrib]
          if {[string compare $refstatus "ref"] == 0} {
            Java_WriteLn $stream $umap \
                "    if ($membername != downcast.$membername) \{"
            Java_WriteLn $stream $umap \
                "      final UndoInfo step$id = new ReplacementUndoInfo($id, $membername, downcast.$membername);"
            Java_WriteLn $stream $umap "      info.add(step$id);"
            Java_WriteLn $stream $umap "    \}"
          } elseif {[Java_ClassIsAbstract $classMap($decltype)]} {
            if {[string compare $eqstatus "required"] == 0} {
              Java_WriteLn $stream $umap \
                  "    if ($membername.getClass() == downcast.$membername.getClass()) \{"
              Java_WriteLn $stream $umap \
                  "      final UndoInfo step$id = $membername.createUndoInfo(downcast.$membername, boundary);"
              Java_WriteLn $stream $umap "      if (step$id != null) \{"
              Java_WriteLn $stream $umap "        info.add(step$id);"
              Java_WriteLn $stream $umap "      \}"
              Java_WriteLn $stream $umap "    \} else \{"
              Java_WriteLn $stream $umap \
                  "      final $type clone$id = downcast.$membername.clone();"
              Java_WriteLn $stream $umap \
                  "      final UndoInfo step$id = new ReplacementUndoInfo($id, $membername, clone$id);"
              Java_WriteLn $stream $umap "      info.add(step$id);"
              Java_WriteLn $stream $umap "    \}"
            } else {
              Java_WriteLn $stream $umap \
                  "    final boolean null${id}a = $membername == null;"
              Java_WriteLn $stream $umap \
                  "    final boolean null${id}b = downcast.$membername == null;"
              Java_WriteLn $stream $umap \
                  "    if (null${id}a != null${id}b ||"
              Java_WriteLn $stream $umap \
                  "        !null${id}a && $membername.getClass() != downcast.$membername.getClass()) \{"
              Java_WriteLn $stream $umap \
                  "      if (boundary ==  null || !boundary.contains($membername)) \{"
              Java_WriteLn $stream $umap \
                  "        final $type clone$id = ProxyTools.clone(downcast.$membername);"
              Java_WriteLn $stream $umap \
                  "        final UndoInfo step$id = new ReplacementUndoInfo($id, $membername, clone$id);"
              Java_WriteLn $stream $umap "        info.add(step$id);"
              Java_WriteLn $stream $umap "      \}"
              Java_WriteLn $stream $umap "    \} else if (!null${id}a) \{"
              Java_WriteLn $stream $umap \
                  "      final UndoInfo step$id = $membername.createUndoInfo(downcast.$membername, boundary);"
              Java_WriteLn $stream $umap "      if (step$id != null) \{"
              Java_WriteLn $stream $umap "        info.add(step$id);"
              Java_WriteLn $stream $umap "      \}"
              Java_WriteLn $stream $umap "    \}"
            }
          } else {
            if {[string compare $eqstatus "required"] == 0} {
              Java_WriteLn $stream $umap \
                  "    final UndoInfo step$id = $membername.createUndoInfo(downcast.$membername, boundary);"
              Java_WriteLn $stream $umap "    if (step$id != null) \{"
              Java_WriteLn $stream $umap "      info.add(step$id);"
              Java_WriteLn $stream $umap "    \}"
            } else {
              Java_WriteLn $stream $umap \
                  "    final boolean null${id}a = $membername == null;"
              Java_WriteLn $stream $umap \
                  "    final boolean null${id}b = downcast.$membername == null;"
              Java_WriteLn $stream $umap \
                  "    if (null${id}a != null${id}b) \{"
              Java_WriteLn $stream $umap \
                  "      if (boundary ==  null || !boundary.contains($membername)) \{"
              Java_WriteLn $stream $umap \
                  "        final $type clone$id = ProxyTools.clone(downcast.$membername);"
              Java_WriteLn $stream $umap \
                  "        final UndoInfo step$id = new ReplacementUndoInfo($id, $membername, clone$id);"
              Java_WriteLn $stream $umap "        info.add(step$id);"
              Java_WriteLn $stream $umap "      \}"
              Java_WriteLn $stream $umap "    \} else if (!null${id}a) \{"
              Java_WriteLn $stream $umap \
                  "      final UndoInfo step$id = $membername.createUndoInfo(downcast.$membername, boundary);"
              Java_WriteLn $stream $umap "      if (step$id != null) \{"
              Java_WriteLn $stream $umap "        info.add(step$id);"
              Java_WriteLn $stream $umap "      \}"
              Java_WriteLn $stream $umap "    \}"
            }
          }   
        } else {
          if {[string compare $eqstatus "required"] == 0} {
            Java_WriteLn $stream $umap \
                "    if (!$membername.equals(downcast.$membername)) \{"
          } else {
            Java_WriteLn $stream $umap \
                "    if (!ProxyTools.equals($membername, downcast.$membername)) \{"
          }
          Java_WriteLn $stream $umap \
              "      final UndoInfo step$id = new ReplacementUndoInfo($id, $membername, downcast.$membername);"
          Java_WriteLn $stream $umap "      info.add(step$id);"
          Java_WriteLn $stream $umap "    \}"
        }
      }
      Java_WriteLn $stream $umap "  \}"
      Java_WriteLn $stream $umap ""
    }

  ############################################################################
  # Comparing
    if {!$abstract} {
      Java_GenerateSeparatorComment $stream $umap "Comparing"
      Java_WriteLn $stream $umap \
          "  public Class<$interfacename> getProxyInterface()"
      Java_WriteLn $stream $umap "  \{"
      Java_WriteLn $stream $umap "    return $interfacename.class;"
      Java_WriteLn $stream $umap "  \}"
      Java_WriteLn $stream $umap ""
    }

  ############################################################################
  # Visitors
    if {!$abstract} {
      Java_GenerateSeparatorComment $stream $umap \
          "Interface net.sourceforge.waters.model.base.Proxy"
      Java_Write   $stream $umap "  public Object acceptVisitor"
      Java_WriteLn $stream $umap "(final ProxyVisitor visitor)"
      Java_WriteLn $stream $umap "    throws VisitorException"
      Java_WriteLn $stream $umap "  \{"
      Java_Write   $stream $umap "    final ${prefix}ProxyVisitor downcast = "
      Java_WriteLn $stream $umap "(${prefix}ProxyVisitor) visitor;"
      Java_Write   $stream $umap "    return downcast.visit${interfacename}"
      Java_WriteLn $stream $umap "(this);"
      Java_WriteLn $stream $umap "  \}"
      Java_WriteLn $stream $umap ""
    }

  ############################################################################
  # Getters
    if {$numattribs > 0} {
      set interfacepack $importMap($interfacename)
      Java_GenerateSeparatorComment $stream $umap \
          "Interface $interfacepack.$interfacename"
      foreach attrib $attribs {
        set type [Java_AttribGetDeclaredType $attrib $impl]
        if {[string compare $impl "subject"] == 0} {
          set type [Java_GetImplClassName $type $impl]
        }
        set gettername [Java_AttribGetGetterName $attrib $impl]
        set membername [Java_AttribGetMemberName $attrib $impl]
        set caster [Java_AttribGetCastTransformerName $attrib $impl]
        set transformer [Java_AttribGetCollectionTransformerName $attrib $impl]
        set eqstatus [Java_AttribGetEqualityStatus $attrib]
        Java_WriteLn $stream $umap "  public $type ${gettername}()"
        Java_WriteLn $stream $umap "  \{"
        if {[regexp {2D$} $type all]} {
          Java_WriteLn $stream $umap "    return ($type) $membername.clone();"
        } elseif {[string compare $transformer ""] == 0 ||
                  [string compare $impl "plain"] == 0} {
          Java_WriteLn $stream $umap "    return $membername;"
        } elseif {[string compare $eqstatus "optional"] == 0} {
          Java_WriteLn $stream $umap "    if ($membername == null) \{"
          Java_WriteLn $stream $umap "      return null;"
          Java_WriteLn $stream $umap "    \} else \{"
          Java_WriteLn $stream $umap \
              "      final $caster precast = $membername;"
          Java_WriteLn $stream $umap \
              "      @SuppressWarnings(\"unchecked\")"
          Java_WriteLn $stream $umap \
              "      final $type downcast = ($type) precast;"
          Java_WriteLn $stream $umap \
              "      return Collections.${transformer}(downcast);"
          Java_WriteLn $stream $umap "    \}"
        } else {
          Java_WriteLn $stream $umap \
              "    final $caster precast = $membername;"
          Java_WriteLn $stream $umap \
              "    @SuppressWarnings(\"unchecked\")"
          Java_WriteLn $stream $umap \
              "    final $type downcast = ($type) precast;"
          Java_WriteLn $stream $umap \
              "    return Collections.${transformer}(downcast);"
        }
        Java_WriteLn $stream $umap "  \}"
        Java_WriteLn $stream $umap ""
      }
    }

  ############################################################################
  # Other Interfaces
    foreach otherinterface $otherinterfaces {
      if {[regexp {^Comparable<([A-Z][A-Za-z0-9_]*)>$} \
               $otherinterface all partnertype]} {
        Java_GenerateSeparatorComment $stream $umap \
            "Interface java.lang.$otherinterface"
        Java_WriteLn $stream $umap \
            "  public int compareTo(final $partnertype partner)"
        Java_WriteLn $stream $umap "  \{"
        Java_WriteLn $stream $umap \
            "    return toString().compareTo(partner.toString());"
        Java_WriteLn $stream $umap "  \}"
        Java_WriteLn $stream $umap ""
      } else {
        puts stderr "WARNING: Unknown interface $otherinterface!"
      }
    }

  ############################################################################
  # Setters
    if {[string compare $impl "subject"] == 0 && $numattribs > 0} {
      Java_GenerateSeparatorComment $stream $umap "Setters"
      foreach attrib $attribs {
        set decltype [Java_AttribGetDeclaredType $attrib $impl]
        set membername [Java_AttribGetMemberName $attrib $impl]
        set comment [Java_AttribGetComment $attrib]
        regexp {^Gets the [^\.]+\.} $comment getcomment
        if {[Java_IsCollectionType $decltype]} {
          if {[info exists getcomment]} {
            regsub {^Gets the } $getcomment {Gets the modifiable } setcomment
            Java_WriteLn $stream $umap "  /**"
            Java_WriteLn $stream $umap "   * $setcomment"
            set eachcomment [Java_AttribGetEachComment $attrib $impl]
            if {[string compare $eachcomment ""] != 0} {
              Java_WriteLn $stream $umap "   * $eachcomment"
            }
            Java_WriteLn $stream $umap "   */"
          }
          set gettername [Java_AttribGetGetterName $attrib $impl]
          set type [Java_AttribGetMemberType $attrib $impl classMap]
          Java_WriteLn $stream $umap "  public $type ${gettername}Modifiable()"
          Java_WriteLn $stream $umap "  \{"
          Java_WriteLn $stream $umap "    return $membername;"
        } else {
          set settername [Java_AttribGetSetterName $attrib $impl]
          set paramname [Java_AttribGetParameterName $attrib $impl]
          set paramtype [Java_GetImplClassName $decltype $impl]
          set eqstatus [Java_AttribGetEqualityStatus $attrib]
          set refstatus [Java_AttribGetRefStatus $attrib]
          set notref [string compare $refstatus "ref"]
          set subject [regexp {Subject$} $paramtype all]
          set setparent [expr $notref && $subject]
          if {[info exists getcomment]} {
            regsub {^Gets } $getcomment {Sets } setcomment
            Java_WriteLn $stream $umap "  /**"
            Java_WriteLn $stream $umap "   * $setcomment"
            Java_WriteLn $stream $umap "   */"
          }
          Java_WriteLn $stream $umap \
              "  public void ${settername}(final $paramtype ${paramname})"
          Java_WriteLn $stream $umap "  \{"
          if {[regexp {^[a-z]} $paramtype all] ||
              [regexp {Subject$} $paramtype all]} {
            Java_WriteLn $stream $umap \
                "    if ($membername == $paramname) \{"
          } else {
            Java_WriteLn $stream $umap \
                "    if ($membername.equals($paramname)) \{"
          }
          Java_WriteLn $stream $umap "      return;"
          Java_WriteLn $stream $umap "    \}"
          if {$setparent} {
            Java_WriteSetParent $stream $umap $paramname $eqstatus "this"
            Java_WriteSetParent $stream $umap $membername $eqstatus "null"
          }
          if {[regexp {2D$} $decltype all]} {
            Java_WriteLn $stream $umap \
                "    $membername = ($decltype) $paramname.clone();"
          } else {
            Java_WriteLn $stream $umap "    $membername = $paramname;"
          }
          if {$geo == 3} {
            Java_WriteLn $stream $umap "    fireGeometryChanged();"
          } elseif {[string compare $eqstatus "geometry"] == 0} {
            Java_WriteLn $stream $umap "    fireGeometryChanged($membername);"
          } else {
            Java_WriteLn $stream $umap "    fireStateChanged();"
          }
        }
        Java_WriteLn $stream $umap "  \}"
        Java_WriteLn $stream $umap ""
        catch {unset getcomment}
      }
    }

  ############################################################################
  # Special Methods
    foreach special $specials {
      set kind [lindex $special 0]
      if {[string compare $kind "method"] == 0} {
        set comment [lindex $special 1]
        set method [lindex $special 2]
        set body [lrange $special 4 end]
        if {[string length $comment] > 0} {
          Java_GenerateSeparatorComment $stream $umap $comment
        }
        Java_WriteLn $stream $umap "  public $method"
        Java_WriteLn $stream $umap "  \{"
        foreach line $body {
          Java_WriteLn $stream $umap "    $line"
        }
        Java_WriteLn $stream $umap "  \}"
        Java_WriteLn $stream $umap ""
      }
    }

  ############################################################################
  # Inner Classes
    foreach special $specials {
      set kind [lindex $special 0]
      if {[string compare $kind "class"] == 0} {
        set name [lindex $special 1]
        Java_GenerateSeparatorComment $stream $umap "Inner Class $name"
        Java_CopyInnerClass $stream $umap $name
      }
    }

  ############################################################################
  # Data Members
    if {[string length $attribs] > 0} {
      Java_GenerateSeparatorComment $stream $umap "Data Members"
      foreach attrib $attribs {
        set type [Java_AttribGetMemberType $attrib $impl classMap]
        set membername [Java_AttribGetMemberName $attrib $impl]
        if {[string compare $impl "plain"] == 0 ||
            [Java_IsCollectionType $type]} {
          Java_WriteLn $stream $umap "  private final $type $membername;"
          set hash [Aux_Hash "$type:$membername:" $hash]
        } else {
          Java_WriteLn $stream $umap "  private $type $membername;"
        }
      }
      Java_WriteLn $stream $umap ""
    }

  ############################################################################
  # Serial Version UID
    if {[string compare $impl "plain"] == 0} {
      Java_GenerateSeparatorComment $stream $umap "Class Constants"
      Java_WriteLn $stream $umap \
          "  private static final long serialVersionUID = ${hash}L;"
      Java_WriteLn $stream $umap ""
    }

    Java_WriteLn $stream $umap "}"
    if {$write} {
      close $stream
      Java_ReplaceFile $tmpname $destname
    }
  }

}


##############################################################################
# Generate Factory
##############################################################################

proc Java_GenerateFactory {impl subpack prefix destname classnames
                           iface classMapName importMapName} {
  upvar $classMapName classMap
  upvar $importMapName importMap
  global gSpaces
  if {$iface} {
    set keyword "interface"
    set factoryname "${prefix}ProxyFactory"
  } else {
    set implobjname [Java_GetImplObjectName $impl]
    set keyword "class"
    set factoryname "${prefix}${implobjname}Factory"
    set interfacename "${prefix}ProxyFactory"
  }
  set packname "net.sourceforge.waters.$impl.$subpack"

  ############################################################################
  # Prepare and Write Output
  foreach write {0 1} {
    if {$write} {
      set tmpname "$destname.tmp"
      set stream [open $tmpname w]
      set umap ""
      Java_GenerateHeaderComment $stream $packname $factoryname
      Java_WritePackageAndImports $stream $packname useMap importMap
    } else {
      set stream ""
      set umap useMap
    }

  ############################################################################
  # Write Headers
    Java_WriteLn $stream $umap "public $keyword $factoryname"
    if {!$iface} {
      Java_WriteLn $stream $umap "  implements $interfacename"
    }
    Java_WriteLn $stream $umap "\{"

  ############################################################################
  # Write getInstance() Method
    if {!$iface} {
      Java_GenerateSeparatorComment $stream $umap "Static Class Methods"
      Java_WriteLn $stream $umap "  /**"
      Java_WriteLn $stream $umap "   * Gets the single instance of this class."
      Java_WriteLn $stream $umap "   */"
      Java_WriteLn $stream $umap "  public static $factoryname getInstance()"
      Java_WriteLn $stream $umap "  \{"
      Java_WriteLn $stream $umap "    return INSTANCE;"
      Java_WriteLn $stream $umap "  \}"
      Java_WriteLn $stream $umap ""
      Java_WriteLn $stream $umap "  /**"
      Java_WriteLn $stream $umap \
          "   * Gets the cloning visitor that can convert $prefix objects from"
      Java_WriteLn $stream $umap \
          "   * arbitrary implementations into this implementation."
      Java_WriteLn $stream $umap "   */"
      Java_WriteLn $stream $umap "  public static ${prefix}ProxyCloner getCloningInstance()"
      Java_WriteLn $stream $umap "  \{"
      Java_WriteLn $stream $umap "    if (CLONING_INSTANCE == null) \{"
      Java_WriteLn $stream $umap "      CLONING_INSTANCE = new ${prefix}ProxyCloner(INSTANCE);"
      Java_WriteLn $stream $umap "    \}"
      Java_WriteLn $stream $umap "    return CLONING_INSTANCE;"
      Java_WriteLn $stream $umap "  \}"
      Java_WriteLn $stream $umap ""
    }

  ############################################################################
  # Write Cloner Methods
    if {!$iface} {
      Java_GenerateSeparatorComment $stream $umap \
          "Interface net.sourceforge.waters.model.$subpack.$interfacename"
    }
    if {$iface} {
      Java_WriteLn $stream $umap "  /**"
      Java_WriteLn $stream $umap \
          "   * Gets the cloning visitor that can convert $prefix objects from"
      Java_WriteLn $stream $umap \
          "   * arbitrary implementations into this implementation."
      Java_WriteLn $stream $umap "   */"
      Java_WriteLn $stream $umap "  public ${prefix}ProxyCloner getCloner();"
    } else {
      Java_WriteLn $stream $umap "  public ${prefix}ProxyCloner getCloner()"
      Java_WriteLn $stream $umap "  \{"
      Java_WriteLn $stream $umap "    return getCloningInstance();"
      Java_WriteLn $stream $umap "  \}"
    }
    Java_WriteLn $stream $umap ""

  ############################################################################
  # Write Creator Methods
    set classnames [lsort $classnames]
    foreach classname $classnames {
      set classinfo $classMap($classname)
      set abstract [Java_ClassIsAbstract $classinfo]
      if {!$abstract} {
        set short [Java_ClassGetShortName $classinfo]
        set allattribs ""
        set exceptions ""
        set currentname $classname
        while {[string length $currentname] > 0} {
          set currentinfo $classMap($currentname)
          set currentattribs [Java_ClassGetAttributes $currentinfo]
          set allattribs [concat $currentattribs $allattribs]
          set currentname [Java_ClassGetParent $currentinfo]
        }
        set numallattribs [llength $allattribs]
        set numdefaults 0
        foreach attrib $allattribs {
          set dftvalue [Java_AttribGetDefaultValue $attrib $impl]
          if {[string compare $dftvalue ""] != 0} {
            incr numdefaults
          }
        }
        set hasdft [expr $numdefaults > 0]
        if {$iface} {
          set returntype $classname
        } else {
          regsub {Proxy$} $classname $implobjname returntype
        }
        for {set withdft 0} {$withdft <= $hasdft} {incr withdft} {
          set numattribs $numallattribs
          if {$withdft} {
            incr numattribs -$numdefaults
          }
          if {$iface} {
            Java_WriteConstructorComment $stream $umap $impl \
                "method" $short $allattribs $withdft
          }
          Java_Write $stream $umap "  public $returntype create$classname"
          if {$numattribs > 0} {
            Java_WriteLn $stream $umap ""
            Java_Write $stream $umap "      "
          }
          Java_Write $stream $umap "("
          set i 1
          foreach attrib $allattribs {
            if {$withdft} {
              set dftvalue [Java_AttribGetDefaultValue $attrib $impl]
              if {[string compare $dftvalue ""] != 0} {
                continue
              }
            }
            set ctortype [Java_AttribGetConstructorArgumentType $attrib $impl]
            set exception [Java_AttribGetConstructorExceptions \
                               $attrib $impl classMap]
            set paramname [Java_AttribGetParameterName $attrib $impl]
            if {!$iface} {
              Java_Write $stream $umap "final "
            }
            Java_Write $stream $umap "$ctortype $paramname"
            if {$i < $numattribs} {
              Java_WriteLn $stream $umap ","
              Java_Write $stream $umap "       "
              incr i
            }
            if {!$iface} {
              set exceptions [concat $exceptions $exception]
            }
          }
          Java_Write $stream $umap ")"
          if {$iface} {
            Java_WriteLn $stream $umap ";"
          } else {
            Java_WriteLn $stream $umap ""
            Java_WriteExceptionDeclaration $stream $umap $exceptions
            Java_WriteLn $stream $umap "  \{"
            set text "    return new ${returntype}("
            set indent [string length $text]
            set indent [string range $gSpaces 1 $indent]
            Java_Write $stream $umap $text
            set i 1
            foreach attrib $allattribs {
              if {$withdft} {
                set dftvalue [Java_AttribGetDefaultValue $attrib $impl]
                if {[string compare $dftvalue ""] != 0} {
                  continue
                }
              }
              set paramname [Java_AttribGetParameterName $attrib $impl]
              Java_Write $stream $umap $paramname
              if {$i < $numattribs} {
                Java_WriteLn $stream $umap ","
                Java_Write $stream $umap $indent
                incr i
              }
            }
            Java_WriteLn $stream $umap ");"
            Java_WriteLn $stream $umap "  \}"
          }
          Java_WriteLn $stream $umap ""
        }
      }
    }

  ############################################################################
  # Write Data Members
    if {!$iface} {
      Java_GenerateSeparatorComment $stream $umap "Data Members"
      Java_WriteLn $stream $umap \
          "  private static final $factoryname INSTANCE ="
      Java_WriteLn $stream $umap "    new ${factoryname}();"
      Java_WriteLn $stream $umap \
          "  private static ${prefix}ProxyCloner CLONING_INSTANCE;"
      Java_WriteLn $stream $umap ""
    }

    if {$write} {
      Java_WriteLn $stream $umap "\}"
      close $stream
      Java_ReplaceFile $tmpname $destname
    }
  }
}


##############################################################################
# Generate Visitor
##############################################################################

proc Java_GenerateVisitor {subpack prefix destname classnames
                           mode classMapName importMapName} {
  upvar $classMapName classMap
  upvar $importMapName importMap
  if {[string compare $mode "Iface"] == 0} {
    set iface 1
    set keyword "interface"
    set visitorname "${prefix}ProxyVisitor"
  } elseif {[string compare $mode "Default"] == 0} {
    set iface 0
    set descending 0
    set keyword "class"
    set visitorname "Default${prefix}ProxyVisitor"
  } elseif {[string compare $mode "Descending"] == 0} {
    set iface 0
    set descending 1
    set keyword "class"
    set visitorname "Descending${prefix}ProxyVisitor"
  } else {
    puts stderr "ERROR: Unknown visitor generation mode $mode!"
    exit 1
  }
  set packname "net.sourceforge.waters.model.$subpack"

  ############################################################################
  # Prepare and Write Output
  foreach write {0 1} {
    if {$write} {
      set tmpname "$destname.tmp"
      set stream [open $tmpname w]
      set umap ""
      Java_GenerateHeaderComment $stream $packname $visitorname
      Java_WritePackageAndImports $stream $packname useMap importMap
    } else {
      set stream ""
      set umap useMap
    }

  ############################################################################
  # Write Javadoc
    Java_WriteLn $stream $umap "/**"
    if {$iface} {
      Java_WriteLn $stream $umap \
          " * The visitor interface for the module class hierarchy."
    } else {
      Java_WriteLn $stream $umap " * <P>An empty implementation of the {@link ${prefix}ProxyVisitor} interface.</P>"
      Java_WriteLn $stream $umap " *"
      Java_WriteLn $stream $umap " * <P>This is an adapter class to make it more convenient to implement"
      Java_WriteLn $stream $umap " * visitors that do not explicitly implement all the visit methods."
      if {$descending} {
        Java_WriteLn $stream $umap \
            " * All the visit methods in this adapter class call the visit"
        Java_WriteLn $stream $umap \
            " * method for the immediate superclass and"
        Java_WriteLn $stream $umap \
            " * afterwards visit all children of their argument."
        Java_WriteLn $stream $umap \
            " * In all cases, <CODE>null</CODE> is returned.</P>"
      } else {
        Java_WriteLn $stream $umap " * All the visit methods in this adapter class do nothing or call the visit"
        Java_WriteLn $stream $umap " * method for the immediate superclass of their argument.</P>"
      }
    }
    Java_WriteLn $stream $umap " *"
    Java_WriteLn $stream $umap " * @author Robi Malik"
    Java_WriteLn $stream $umap " */"
    Java_WriteLn $stream $umap ""

  ############################################################################
  # Write Headers
    Java_WriteLn $stream $umap "public $keyword $visitorname"
    if {$iface} {
      Java_WriteLn $stream $umap "  extends ProxyVisitor"
    } else {
      set ifacename "${prefix}ProxyVisitor"
      Java_WriteLn $stream $umap "  extends ${mode}ProxyVisitor"
      Java_WriteLn $stream $umap "  implements $ifacename"
    }
    Java_WriteLn $stream $umap "\{"

  ############################################################################
  # Write Visitor Methods
    if {$iface} {
      Java_GenerateSeparatorComment $stream $umap "Visitor Methods"
    } else {
      set pack $importMap($ifacename)
      Java_GenerateSeparatorComment $stream $umap "Interface $pack.$ifacename"
    }
    set classnames [lsort $classnames]
    foreach classname $classnames {
      set classinfo $classMap($classname)
      set supername [Java_ClassGetParent $classinfo]
      regsub {^Abstract} $supername "" supername
      if {!$iface} {
        Java_WriteLn $stream $umap "  @Override"
      }
      Java_Write $stream $umap "  public Object visit${classname}("
      if {!$iface} {
        Java_Write $stream $umap "final "
      }
      Java_WriteLn $stream $umap "$classname proxy)"
      Java_Write $stream $umap "    throws VisitorException"
      if {$iface} {
        Java_WriteLn $stream $umap ";"
      } else {
        Java_WriteLn $stream $umap ""
        Java_WriteLn $stream $umap "  \{"
        set attribs [Java_ClassGetAttributes $classinfo]
        set numattribs [llength $attribs]
        if {$descending && $numattribs > 0} {
          Java_WriteLn $stream $umap "    visit${supername}(proxy);"
          foreach attrib $attribs {
            set decltype [Java_AttribGetDeclaredType $attrib ""]
            set paramname [Java_AttribGetParameterName $attrib ""]
            set gettername [Java_AttribGetGetterName $attrib ""]
            if {[regexp {Proxy$} $decltype all]} {
              set eqstatus [Java_AttribGetEqualityStatus $attrib]
              Java_WriteLn $stream $umap \
                  "    final $decltype $paramname = proxy.${gettername}();"
              if {[string compare $eqstatus "optional"] == 0} {
                Java_WriteLn $stream $umap "    if ($paramname != null) \{"
                set ind "  "
              } else {
                set ind ""
              }
              set memberclassinfo $classMap($decltype)
              if {[Java_ClassIsAbstract $memberclassinfo]} {
                Java_WriteLn $stream $umap \
                    "$ind    $paramname.acceptVisitor(this);"
              } else {
                Java_WriteLn $stream $umap \
                    "$ind    visit${decltype}($paramname);"
              }
              if {[string compare $eqstatus "optional"] == 0} {
                Java_WriteLn $stream $umap "    \}"
              }
            } elseif {[Java_IsCollectionType $decltype] &&
                      [regexp {<(\? extends )?([a-zA-Z]*Proxy)>} \
                           $decltype all ext itemtype]} {
              set itemclassinfo $classMap($itemtype)
              set abstract [Java_ClassIsAbstract $itemclassinfo]
              Java_WriteLn $stream $umap \
                  "    final $decltype $paramname = proxy.${gettername}();"
              Java_WriteLn $stream $umap \
                  "    visitCollection($paramname);"
            }
          }
          Java_WriteLn $stream $umap "    return null;"
        } else {
          Java_WriteLn $stream $umap "    return visit${supername}(proxy);"
        }
        Java_WriteLn $stream $umap "  \}"
      }
      Java_WriteLn $stream $umap ""
    }

    if {$write} {
      Java_WriteLn $stream $umap "\}"
      close $stream
      Java_ReplaceFile $tmpname $destname
    }
  }
}



##############################################################################
# Generate Cloning Visitor
##############################################################################

proc Java_GenerateCloningVisitor {subpack prefix destname classnames
                                  classMapName importMapName} {
  global gSpaces
  upvar $classMapName classMap
  upvar $importMapName importMap
  set keyword "class"
  set visitorname "${prefix}ProxyCloner"
  set packname "net.sourceforge.waters.model.$subpack"
  set impl "plain"
  lappend classnames "Proxy"

  ############################################################################
  # Prepare and Write Output
  foreach write {0 1} {
    if {$write} {
      set tmpname "$destname.tmp"
      set stream [open $tmpname w]
      set umap ""
      Java_GenerateHeaderComment $stream $packname $visitorname
      Java_WritePackageAndImports $stream $packname useMap importMap
    } else {
      set stream ""
      set umap useMap
    }

  ############################################################################
  # Write Headers
    Java_WriteLn $stream $umap "/**"
    Java_WriteLn $stream $umap \
        " * A tool to create deep copies of ${prefix} objects."
    Java_WriteLn $stream $umap \
        " * Parameterised by a factory, this visitor can accept objects from"
    Java_WriteLn $stream $umap \
        " * one {@link Proxy} implementation and translate them to another."
    Java_WriteLn $stream $umap " *"
    Java_WriteLn $stream $umap " * @author Robi Malik"
    Java_WriteLn $stream $umap " */"
    Java_WriteLn $stream $umap ""
    Java_WriteLn $stream $umap "public $keyword $visitorname"
    Java_WriteLn $stream $umap "  extends Default${prefix}ProxyVisitor"
    Java_WriteLn $stream $umap "  implements ProxyCloner"
    Java_WriteLn $stream $umap "\{"

  ############################################################################
  # Write Constructor and Invocation
    Java_GenerateSeparatorComment $stream $umap "Constructor"
    Java_WriteLn $stream $umap \
        "  public ${visitorname}(final ${prefix}ProxyFactory factory)"
    Java_WriteLn $stream $umap "  \{"
    Java_WriteLn $stream $umap "    mFactory = factory;"
    Java_WriteLn $stream $umap "    mNodeMap = null;"
    Java_WriteLn $stream $umap "  \}"
    Java_WriteLn $stream $umap ""

    Java_GenerateSeparatorComment $stream $umap "Invocation"
    Java_WriteLn $stream $umap "  public Proxy getClone(final Proxy proxy)"
    Java_WriteLn $stream $umap "  \{"
    Java_WriteLn $stream $umap "    if (proxy == null) \{"
    Java_WriteLn $stream $umap "      return null;"
    Java_WriteLn $stream $umap "    \} else \{"
    Java_WriteLn $stream $umap "      try \{"
    Java_WriteLn $stream $umap "        return cloneProxy(proxy);"
    Java_WriteLn $stream $umap \
        "      \} catch (final VisitorException exception) \{"
    Java_WriteLn $stream $umap \
        "        throw exception.getRuntimeException();"
    Java_WriteLn $stream $umap "      \}"
    Java_WriteLn $stream $umap "    \}"
    Java_WriteLn $stream $umap "  \}"
    Java_WriteLn $stream $umap ""


  ############################################################################
  # Special Cloners
    Java_WriteLn $stream $umap "  public <P extends Proxy>"
    Java_WriteLn $stream $umap \
        "  List<P> getClonedList(final Collection<? extends P> collection)"
    Java_WriteLn $stream $umap "  \{"
    Java_WriteLn $stream $umap "    final int size = collection.size();"
    Java_WriteLn $stream $umap \
        "    final List<Proxy> result = new ArrayList<>(size);"
    Java_WriteLn $stream $umap "    for (final P proxy : collection) \{"
    Java_WriteLn $stream $umap "      final Proxy cloned = getClone(proxy);"
    Java_WriteLn $stream $umap "      result.add(cloned);"
    Java_WriteLn $stream $umap "    \}"
    Java_WriteLn $stream $umap "    final List<?> precast = result;"
    Java_WriteLn $stream $umap "    @SuppressWarnings(\"unchecked\")"
    Java_WriteLn $stream $umap "    final List<P> cast = (List<P>) precast;"
    Java_WriteLn $stream $umap "    return cast;"
    Java_WriteLn $stream $umap "  \}"
    Java_WriteLn $stream $umap ""
    Java_WriteLn $stream $umap "  public <P extends Proxy>"
    Java_WriteLn $stream $umap \
        "  Set<P> getClonedSet(final Collection<? extends P> collection)"
    Java_WriteLn $stream $umap "  \{"
    Java_WriteLn $stream $umap "    final int size = collection.size();"
    Java_WriteLn $stream $umap \
        "    final Set<Proxy> result = new THashSet<>(size);"
    Java_WriteLn $stream $umap "    for (final P proxy : collection) \{"
    Java_WriteLn $stream $umap "      final Proxy cloned = getClone(proxy);"
    Java_WriteLn $stream $umap "      result.add(cloned);"
    Java_WriteLn $stream $umap "    \}"
    Java_WriteLn $stream $umap "    final Set<?> precast = result;"
    Java_WriteLn $stream $umap "    @SuppressWarnings(\"unchecked\")"
    Java_WriteLn $stream $umap "    final Set<P> cast = (Set<P>) precast;"
    Java_WriteLn $stream $umap "    return cast;"
    Java_WriteLn $stream $umap "  \}"
    Java_WriteLn $stream $umap ""

    Java_WriteLn $stream $umap "  /**"
    Java_WriteLn $stream $umap \
        "   * Creates a clone of the given graph using two factories."
    Java_WriteLn $stream $umap \
        "   * This methods creates a clone of a graph's nodes and edges using"
    Java_WriteLn $stream $umap \
        "   * the standard factory of this cloner, then creates a new graph "
    Java_WriteLn $stream $umap \
        "   * using another factory given as an argument."
    Java_WriteLn $stream $umap \
        "   * @param  proxy    The graph to be duplicated."
    Java_WriteLn $stream $umap \
        "   * @param  factory  The factory used to create the new graph."
    Java_WriteLn $stream $umap "   * @return The cloned graph."
    Java_WriteLn $stream $umap "   */"
    Java_WriteLn $stream $umap \
        "  public GraphProxy getClonedGraph(final GraphProxy proxy,"
    Java_WriteLn $stream $umap \
        "                                   final ModuleProxyFactory factory)"
    Java_WriteLn $stream $umap "  \{"
    set classinfo $classMap(GraphProxy)
    Java_GenerateCloningMethodBody $stream classMap $umap $classinfo factory
    Java_WriteLn $stream $umap "  \}"
    Java_WriteLn $stream $umap ""

  ############################################################################
  # Write Visitor Methods
    Java_GenerateSeparatorComment $stream $umap \
        "Interface $packname.${prefix}ProxyVisitor"
    set classnames [lsort $classnames]
    foreach classname $classnames {
      set classinfo $classMap($classname)
      if {![Java_ClassIsAbstract $classinfo]} {
        Java_WriteLn $stream $umap "  @Override"
        Java_WriteLn $stream $umap "  public $classname visit${classname}"
        Java_WriteLn $stream $umap "    (final $classname proxy)"
        Java_WriteLn $stream $umap "    throws VisitorException"
        Java_WriteLn $stream $umap "  \{"
        Java_GenerateCloningMethodBody $stream classMap $umap \
            $classinfo mFactory
        Java_WriteLn $stream $umap "  \}"
        Java_WriteLn $stream $umap ""
      }
    }

  ############################################################################
  # Write Auxiliary Methods
    Java_GenerateSeparatorComment $stream $umap "Hooks"
    Java_WriteLn $stream $umap "  protected Proxy cloneProxy(final Proxy orig)"
    Java_WriteLn $stream $umap "    throws VisitorException"
    Java_WriteLn $stream $umap "  \{"
    Java_WriteLn $stream $umap "    if (orig == null) \{"
    Java_WriteLn $stream $umap "      return orig;"
    Java_WriteLn $stream $umap "    \} else \{"
    Java_WriteLn $stream $umap "      return (Proxy) orig.acceptVisitor(this);"
    Java_WriteLn $stream $umap "    \}"
    Java_WriteLn $stream $umap "  \}"
    Java_WriteLn $stream $umap ""
    Java_GenerateSeparatorComment $stream $umap "Auxiliary Methods"
    Java_WriteLn $stream $umap \
        "  private NodeProxy lookupNodeProxy(final NodeProxy orig)"
    Java_WriteLn $stream $umap "    throws VisitorException"
    Java_WriteLn $stream $umap "  \{"
    Java_WriteLn $stream $umap "    if (mNodeMap == null) \{"
    Java_WriteLn $stream $umap "      return orig;"
    Java_WriteLn $stream $umap "    \} else \{"
    Java_WriteLn $stream $umap "      final String name = orig.getName();"
    Java_WriteLn $stream $umap "      NodeProxy node = mNodeMap.get(name);"
    Java_WriteLn $stream $umap "      if (node == null) \{"
    Java_WriteLn $stream $umap "        node = (NodeProxy) cloneProxy(orig);"
    Java_WriteLn $stream $umap "        mNodeMap.put(name, node);"
    Java_WriteLn $stream $umap "      \}"
    Java_WriteLn $stream $umap "      return node;"
    Java_WriteLn $stream $umap "    \}"
    Java_WriteLn $stream $umap "  \}"
    Java_WriteLn $stream $umap ""
    Java_WriteLn $stream $umap \
        "  private Collection<NodeProxy> lookupNodeProxyCollection"
    Java_WriteLn $stream $umap \
        "    (final Collection<? extends NodeProxy> orig)"
    Java_WriteLn $stream $umap "    throws VisitorException"
    Java_WriteLn $stream $umap "  \{"
    Java_WriteLn $stream $umap \
        "    final Collection<NodeProxy> result = new LinkedList<>();"
    Java_WriteLn $stream $umap "    for (final NodeProxy orignode : orig) \{"
    Java_WriteLn $stream $umap \
        "      final NodeProxy resnode = lookupNodeProxy(orignode);"
    Java_WriteLn $stream $umap "      result.add(resnode);"
    Java_WriteLn $stream $umap "    \}"
    Java_WriteLn $stream $umap "    return result;"
    Java_WriteLn $stream $umap "  \}"
    Java_WriteLn $stream $umap ""
    Java_WriteLn $stream $umap "  private <P extends Proxy>"
    Java_WriteLn $stream $umap \
        "  Collection<P> cloneProxyCollection(final Collection<P> orig)"
    Java_WriteLn $stream $umap "    throws VisitorException"
    Java_WriteLn $stream $umap "  \{"
    Java_WriteLn $stream $umap \
        "    final Collection<Proxy> result = new LinkedList<>();"
    Java_WriteLn $stream $umap "    for (final Proxy origelem : orig) \{"
    Java_WriteLn $stream $umap \
        "      final Proxy reselem = cloneProxy(origelem);"
    Java_WriteLn $stream $umap "      result.add(reselem);"
    Java_WriteLn $stream $umap "    \}"
    Java_WriteLn $stream $umap "    final Collection<?> precast = result;"
    Java_WriteLn $stream $umap "    @SuppressWarnings(\"unchecked\")"
    Java_WriteLn $stream $umap \
        "    Collection<P> cast = (Collection<P>) precast;"
    Java_WriteLn $stream $umap "    return cast;"
    Java_WriteLn $stream $umap "  \}"
    Java_WriteLn $stream $umap ""

  ############################################################################
  # Write Data Members
    Java_GenerateSeparatorComment $stream $umap "Data Members"
    Java_WriteLn $stream $umap \
        "  private final ${prefix}ProxyFactory mFactory;"
    Java_WriteLn $stream $umap \
        "  private Map<String,NodeProxy> mNodeMap;"
    Java_WriteLn $stream $umap ""

    if {$write} {
      Java_WriteLn $stream $umap "\}"
      close $stream
      Java_ReplaceFile $tmpname $destname
    }
  }

}


proc Java_GenerateCloningMethodBody {stream classMapName useMapName
                                     classinfo factory} {
  global gSpaces
  upvar $classMapName classMap
  if {[string compare $useMapName ""] == 0} {
    set umap ""
  } else {
    upvar $useMapName useMap
    set umap useMap
  }
  set impl "plain"
  set classname [Java_ClassGetName $classinfo]
  set isroot [expr [string compare $classname "GraphProxy"] == 0]
  if {$isroot} {
    Java_WriteLn $stream $umap \
        "    final int size = proxy.getNodes().size();"
    Java_WriteLn $stream $umap \
        "    mNodeMap = new HashMap<>(size);"
    Java_WriteLn $stream $umap \
        "    try \{"
    set ind "  "
  } else {
    set ind ""
  }
  set allattribs [Java_ClassGetAllAttributes $classinfo classMap]
  set args ""
  foreach attrib $allattribs {
    set decltype [Java_AttribGetDeclaredType $attrib $impl]
    if {[string compare $decltype "URI"] == 0} {
      lappend args "null"
      continue
    }
    set refstatus [Java_AttribGetRefStatus $attrib]
    set paramname [Java_AttribGetParameterName $attrib $impl]
    set gettername [Java_AttribGetGetterName $attrib $impl]
    set iscoll [Java_IsCollectionType $decltype]
    if {[regexp {Proxy$} $decltype all]} {
      if {[string compare $refstatus "owned"] == 0} {
        set method "($decltype) cloneProxy"
      } else {
        set method "lookup$decltype"
      }
    } elseif {$iscoll} {
      regexp {<(.*)>} $decltype all elemtype
      if {![regexp {^Map<} $decltype all]} {
        regsub {^[A-Z].*<} $decltype "Collection<" decltype
      }
      if {![regexp {Proxy$} $elemtype all]} {
        set method "use"
      } elseif {[string compare $elemtype "NodeProxy"] == 0} {
        set method "lookup${elemtype}Collection"
      } else {
        set method "cloneProxyCollection"
      }
    } else {
      set method "use"
    }
    if {[string compare $method "use"] == 0} {
      Java_WriteLn $stream $umap \
          "$ind    final $decltype $paramname = proxy.${gettername}();"
    } else {
      set eqstatus [Java_AttribGetEqualityStatus $attrib]
      set paramname0 "${paramname}0"
      Java_WriteLn $stream $umap \
          "$ind    final $decltype $paramname0 = proxy.${gettername}();"
      Java_WriteLn $stream $umap \
          "$ind    final $decltype $paramname = ${method}($paramname0);"
    }
    lappend args $paramname
  }
  set text "$ind    return $factory.create${classname}("
  set indent [string length $text]
  set indent [string range $gSpaces 1 $indent]
  Java_Write $stream $umap $text
  set first 1
  foreach arg $args {
    if {$first} {
      set first 0
    } else {
      Java_WriteLn $stream $umap ","
      Java_Write $stream $umap $indent
    }
    Java_Write $stream $umap $arg
  }
  Java_WriteLn $stream $umap ");"
  if {$isroot} {
    if {[string compare $factory "mFactory"] != 0} {
      Java_WriteLn $stream $umap \
          "    \} catch (final VisitorException exception) \{"
      Java_WriteLn $stream $umap \
          "      throw exception.getRuntimeException();"
    }
    Java_WriteLn $stream $umap "    \} finally \{"
    Java_WriteLn $stream $umap "      mNodeMap = null;"
    Java_WriteLn $stream $umap "    \}"
  }
}


##############################################################################
# Generate HashCode Visitor
##############################################################################

proc Java_GenerateHashCodeVisitor {subpack prefix destname classnames
                                   classMapName importMapName} {
  global gSpaces
  upvar $classMapName classMap
  upvar $importMapName importMap
  set keyword "class"
  set suffix "HashCodeVisitor"
  set visitorname "${prefix}${suffix}"
  set packname "net.sourceforge.waters.model.$subpack"
  set impl ""

  ############################################################################
  # Prepare and Write Output
  foreach write {0 1} {
    if {$write} {
      set tmpname "$destname.tmp"
      set stream [open $tmpname w]
      set umap ""
      Java_GenerateHeaderComment $stream $packname $visitorname
      Java_WritePackageAndImports $stream $packname useMap importMap
    } else {
      set stream ""
      set umap useMap
    }

  ############################################################################
  # Write Headers
    Java_WriteLn $stream $umap "/**"
    Java_WriteLn $stream $umap \
        " * A visitor to compute hash code for ${prefix} objects based on"
    Java_WriteLn $stream $umap \
        " * their contents. The $visitorname can be parameterised to respect"
    Java_WriteLn $stream $umap \
        " * or not to respect geometry information."
    Java_WriteLn $stream $umap " *"
    Java_WriteLn $stream $umap " * @author Robi Malik"
    Java_WriteLn $stream $umap " */"
    Java_WriteLn $stream $umap ""
    Java_WriteLn $stream $umap "public $keyword $visitorname"
    Java_WriteLn $stream $umap "  extends Abstract${suffix}"
    Java_WriteLn $stream $umap "  implements ${prefix}ProxyVisitor"
    Java_WriteLn $stream $umap "\{"

  ############################################################################
  # Write Singleton Pattern
    Java_GenerateSeparatorComment $stream $umap "Singleton Pattern"
    Java_WriteLn $stream $umap \
        "  public static $visitorname getInstance(final boolean geo)"
    Java_WriteLn $stream $umap "  \{"
    Java_WriteLn $stream $umap "    if (geo) \{"
    Java_WriteLn $stream $umap \
        "      return SingletonHolderWithGeometry.INSTANCE;";
    Java_WriteLn $stream $umap "    \} else \{"
    Java_WriteLn $stream $umap \
        "      return SingletonHolderWithoutGeometry.INSTANCE;"
    Java_WriteLn $stream $umap "    \}"
    Java_WriteLn $stream $umap "  \}"
    Java_WriteLn $stream $umap ""
    Java_WriteLn $stream $umap "  private ${visitorname}(final boolean geo)"
    Java_WriteLn $stream $umap "  \{"
    Java_WriteLn $stream $umap "    super (geo);"
    Java_WriteLn $stream $umap "  \}"
    Java_WriteLn $stream $umap ""
    for {set geo 0} {$geo <= 1} {incr geo} {
      if {$geo} {
        set gbool "true"
        set gname "With"
      } else {
        set gbool "false"
        set gname "Without"
      }
      Java_WriteLn $stream $umap \
          "  private static class SingletonHolder${gname}Geometry \{"
      Java_WriteLn $stream $umap \
          "    private static final $visitorname INSTANCE ="
      Java_WriteLn $stream $umap "      new ${visitorname}(${gbool});"
      Java_WriteLn $stream $umap "  \}"
      Java_WriteLn $stream $umap ""
    }

  ############################################################################
  # Write Visitor Methods
    Java_GenerateSeparatorComment $stream $umap \
        "Interface $packname.${prefix}ProxyVisitor"
    set classnames [lsort $classnames]
    foreach classname $classnames {
      set classinfo $classMap($classname)
      Java_WriteLn $stream $umap "  public Integer visit${classname}"
      Java_WriteLn $stream $umap "    (final $classname proxy)"
      Java_WriteLn $stream $umap "    throws VisitorException"
      Java_WriteLn $stream $umap "  \{"
      if {[string compare $classname "IdentifiedProxy"] == 0} {
        set supername "Proxy"
      } else {
        set supername [Java_ClassGetParent $classinfo]
      }
      set attribs [Java_ClassGetAttributes $classinfo]
      set geo [Java_ClassGetGeometryStatus $classinfo]
      set numattribs 0
      set nongeoattribs ""
      set geoattribs ""
      foreach attrib $attribs {
        set eqstatus [Java_AttribGetEqualityStatus $attrib]
        if {[string compare $eqstatus "ignored"] == 0} {
          continue
        } elseif {$geo != 3 &&
                  [string compare $eqstatus "geometry"] == 0} {
          incr numattribs
          lappend geoattribs $attrib
        } else {
          incr numattribs
          lappend nongeoattribs $attrib
        }
      }
      if {$numattribs == 0} {
        Java_WriteLn $stream $umap "    return visit${supername}(proxy);"
      } else {
        Java_WriteLn $stream $umap "    int result = visit${supername}(proxy);"
        for {set geo 0} {$geo <= 1} {incr geo} {
          if {$geo} {
            if {[llength $geoattribs] == 0} {
              break
            }
            set attribs $geoattribs
            Java_WriteLn $stream $umap "    if (isRespectingGeometry()) \{"
            set indent 6
          } else {
            set attribs $nongeoattribs
            set indent 4
          }
          set indent [string range $gSpaces 1 $indent]
          foreach attrib $attribs {
            set varname [Java_AttribGetParameterName $attrib $impl]
            set type [Java_AttribGetDeclaredType $attrib $impl]
            set getter [Java_AttribGetGetterName $attrib $impl]
            set refstatus [Java_AttribGetRefStatus $attrib]
            Java_WriteLn $stream $umap \
                "${indent}final $type $varname = proxy.${getter}();"
            Java_WriteLn $stream $umap "${indent}result *= 5;"
            if {[regexp {^boolean$} $type all]} {
              Java_WriteLn $stream $umap "${indent}if ($varname) \{"
              Java_WriteLn $stream $umap "${indent}  result++;"
              Java_WriteLn $stream $umap "${indent}\}"
            } elseif {[regexp {^[a-z]} $type all]} {
              Java_WriteLn $stream $umap "${indent}result += $varname;"
            } elseif {[regexp {^Set<[a-zA-Z]*Proxy>} $type all] ||
                      [regexp {^Collection<[a-zA-Z]*Proxy>$} $type all]} {
              # TODO: collection or set?
              if {[string compare $refstatus "ref"] == 0} {
                set method "computeRefCollectionHashCode"
                Java_WriteLn $stream $umap \
                    "${indent}result += ${method}($varname);"
              } else {
                Java_WriteLn $stream $umap \
                    "${indent}result += computeCollectionHashCode($varname);"
              }
            } elseif {[regexp {^List<[a-zA-Z]*Proxy>} $type all]} {
              # TODO: ref or not ref ?
              Java_WriteLn $stream $umap \
                  "${indent}result += computeListHashCode($varname);"
            } elseif {[string compare $refstatus "ref"] == 0} {
              Java_WriteLn $stream $umap \
                  "${indent}result += computeRefHashCode($varname);"
            } elseif {[info exists classMap($type)]} {
              Java_WriteLn $stream $umap \
                  "${indent}result += computeProxyHashCode($varname);"
            } else {
              Java_WriteLn $stream $umap \
                  "${indent}result += computeOptionalHashCode($varname);"
            }
          }
          if {$geo} {
            Java_WriteLn $stream $umap "    \}"
          }
        }
        Java_WriteLn $stream $umap "    return result;"
      }
      Java_WriteLn $stream $umap "  \}"
      Java_WriteLn $stream $umap ""
    }

    if {$write} {
      Java_WriteLn $stream $umap "\}"
      close $stream
      Java_ReplaceFile $tmpname $destname
    }
  }
}



##############################################################################
# Generate Equality Visitor
##############################################################################

proc Java_GenerateEqualityVisitor {subpack prefix destname classnames
                                   classMapName importMapName} {
  global gSpaces
  upvar $classMapName classMap
  upvar $importMapName importMap
  set keyword "class"
  set suffix "EqualityVisitor"
  set visitorname "${prefix}${suffix}"
  set nonreporter "mNonReporting${suffix}"
  set packname "net.sourceforge.waters.model.$subpack"
  set impl ""

  ############################################################################
  # Prepare and Write Output
  foreach write {0 1} {
    if {$write} {
      set tmpname "$destname.tmp"
      set stream [open $tmpname w]
      set umap ""
      Java_GenerateHeaderComment $stream $packname $visitorname
      Java_WritePackageAndImports $stream $packname useMap importMap
    } else {
      set stream ""
      set umap useMap
    }

  ############################################################################
  # Write Headers
    Java_WriteLn $stream $umap "/**"
    Java_WriteLn $stream $umap \
        " * A visitor to compare module objects based on their contents."
    Java_WriteLn $stream $umap \
        " * The $visitorname can be configured to respect or not to"
    Java_WriteLn $stream $umap \
        " * respect geometry information found in some \{@link Proxy\}"
    Java_WriteLn $stream $umap \
        " * objects, and to produce detailed diagnostic information when two"
    Java_WriteLn $stream $umap \
        " * items are found to be not equal."
    Java_WriteLn $stream $umap " *"
    Java_WriteLn $stream $umap " * @see Abstract${suffix}"
    Java_WriteLn $stream $umap " * @author Robi Malik"
    Java_WriteLn $stream $umap " */"
    Java_WriteLn $stream $umap ""
    Java_WriteLn $stream $umap "public $keyword $visitorname"
    Java_WriteLn $stream $umap "  extends Abstract${suffix}"
    Java_WriteLn $stream $umap "  implements ${prefix}ProxyVisitor"
    Java_WriteLn $stream $umap "\{"

  ############################################################################
  # Write Constructor
    Java_GenerateSeparatorComment $stream $umap "Constructor"
    Java_WriteLn $stream $umap "  /**"
    Java_WriteLn $stream $umap \
        "   * Creates a new equality checker without diagnostic information."
    Java_WriteLn $stream $umap \
        "   * @param  geo  A flag, indicating whether the equality checker"
    Java_WriteLn $stream $umap \
        "   *              should consider geometry information."
    Java_WriteLn $stream $umap \
        "   *              If <CODE>true</CODE>, objects will be considered"
    Java_WriteLn $stream $umap \
        "   *              equal if their contents and geometry are equal,"
    Java_WriteLn $stream $umap \
        "   *              otherwise any geometry information will be ignored"
    Java_WriteLn $stream $umap \
        "   *              when checking for equality."
    Java_WriteLn $stream $umap "   */"
    Java_WriteLn $stream $umap \
        "  public ${visitorname}(final boolean geo)"
    Java_WriteLn $stream $umap "  \{"
    Java_WriteLn $stream $umap "    this(false, geo);"
    Java_WriteLn $stream $umap "  \}"
    Java_WriteLn $stream $umap ""

    Java_WriteLn $stream $umap "  /**"
    Java_WriteLn $stream $umap \
        "   * Creates a new equality checker."
    Java_WriteLn $stream $umap \
        "   * @param  diag A flag, indicating whether the equality checker"
    Java_WriteLn $stream $umap \
        "   *              should provide diagnostic information if items"
    Java_WriteLn $stream $umap \
        "   *              compared are found not to be equal. Diagnostic"
    Java_WriteLn $stream $umap \
        "   *              information can be retrieved using \{@link"
    Java_WriteLn $stream $umap \
        "   *              Abstract${suffix}#getDiagnostics()\}."
    Java_WriteLn $stream $umap \
        "   *              This is useful for testing and debugging, but it"
    Java_WriteLn $stream $umap \
        "   *              does have an impact on performance."
    Java_WriteLn $stream $umap \
        "   * @param  geo  A flag, indicating whether the equality checker"
    Java_WriteLn $stream $umap \
        "   *              should consider geometry information."
    Java_WriteLn $stream $umap \
        "   *              If <CODE>true</CODE>, objects will be considered"
    Java_WriteLn $stream $umap \
        "   *              equal if their contents and geometry are equal,"
    Java_WriteLn $stream $umap \
        "   *              otherwise any geometry information will be ignored"
    Java_WriteLn $stream $umap \
        "   *              when checking for equality."
    Java_WriteLn $stream $umap "   */"
    Java_WriteLn $stream $umap \
        "  public ${visitorname}(final boolean diag, final boolean geo)"
    Java_WriteLn $stream $umap "  \{"
    Java_WriteLn $stream $umap "    super (diag, geo);"
    Java_WriteLn $stream $umap \
        "    mHashCodeVisitor = ${prefix}HashCodeVisitor.getInstance(geo);"
    Java_WriteLn $stream $umap \
        "    $nonreporter = diag ? new ModuleEqualityVisitor(geo) : this;"
    Java_WriteLn $stream $umap "  \}"
    Java_WriteLn $stream $umap ""

  ############################################################################
  # Write Overrides
    Java_GenerateSeparatorComment $stream $umap \
        "Overrides for net.sourceforge.waters.model.base.Abstract${suffix}"
    Java_WriteLn $stream $umap \
        "  public ${prefix}HashCodeVisitor getHashCodeVisitor()"
    Java_WriteLn $stream $umap "  \{"
    Java_WriteLn $stream $umap "    return mHashCodeVisitor;"
    Java_WriteLn $stream $umap "  \}"
    Java_WriteLn $stream $umap \
        "  public $visitorname getNonReporting${suffix}()"
    Java_WriteLn $stream $umap "  \{"
    Java_WriteLn $stream $umap "    return $nonreporter;"
    Java_WriteLn $stream $umap "  \}"
    Java_WriteLn $stream $umap ""

  ############################################################################
  # Write Visitor Methods
    Java_GenerateSeparatorComment $stream $umap \
        "Interface $packname.${prefix}ProxyVisitor"
    set classnames [lsort $classnames]
    foreach classname $classnames {
      set classinfo $classMap($classname)
      Java_WriteLn $stream $umap "  public Boolean visit${classname}"
      Java_WriteLn $stream $umap "    (final $classname proxy)"
      Java_WriteLn $stream $umap "    throws VisitorException"
      Java_WriteLn $stream $umap "  \{"
      if {[string compare $classname "IdentifiedProxy"] == 0} {
        set supername "Proxy"
      } else {
        set supername [Java_ClassGetParent $classinfo]
      }
      set attribs [Java_ClassGetAttributes $classinfo]
      set geo [Java_ClassGetGeometryStatus $classinfo]
      set numattribs 0
      set nongeoattribs ""
      set geoattribs ""
      foreach attrib $attribs {
        set eqstatus [Java_AttribGetEqualityStatus $attrib]
        if {[string compare $eqstatus "ignored"] == 0} {
          continue
        } elseif {$geo != 3 &&
                  [string compare $eqstatus "geometry"] == 0} {
          incr numattribs
          lappend geoattribs $attrib
        } else {
          incr numattribs
          lappend nongeoattribs $attrib
        }
      }
      if {$numattribs == 0} {
        Java_WriteLn $stream $umap "    return visit${supername}(proxy);"
      } else {
        Java_WriteLn $stream $umap "    if (visit${supername}(proxy)) \{"
        Java_WriteLn $stream $umap \
            "      final $classname expected = ($classname) getSecondProxy();"
        set needreset 0
        for {set geo 0} {$geo <= 1} {incr geo} {
          if {$geo} {
            if {[llength $geoattribs] == 0} {
              break
            }
            set attribs $geoattribs
            Java_WriteLn $stream $umap "      if (isRespectingGeometry()) \{"
            set indent 8
          } else {
            set attribs $nongeoattribs
            set indent 6
          }
          set indent [string range $gSpaces 1 $indent]
          foreach attrib $attribs {
            set varname [Java_AttribGetParameterName $attrib $impl]
            set name [Java_AttribGetEnglishDescription $attrib $impl]
            set type [Java_AttribGetDeclaredType $attrib $impl]
            set getter [Java_AttribGetGetterName $attrib $impl]
            set eqstatus [Java_AttribGetEqualityStatus $attrib]
            set refstatus [Java_AttribGetRefStatus $attrib]
            Java_WriteLn $stream $umap \
                "${indent}final $type ${varname}1 = proxy.${getter}();"
            Java_WriteLn $stream $umap \
                "${indent}final $type ${varname}2 = expected.${getter}();"
            if {[regexp {^[a-z]} $type all]} {
              Java_WriteLn $stream $umap \
                  "${indent}if (${varname}1 != ${varname}2) \{"
              Java_Write $stream $umap \
                  "${indent}  return reportAttributeMismatch"
              Java_WriteLn $stream $umap \
                  "(\"$name\", ${varname}1, ${varname}2);"
              Java_WriteLn $stream $umap "${indent}\}"
            } elseif {[regexp {^Set<[a-zA-Z]*Proxy>} $type all] ||
                      [regexp {^Collection<[a-zA-Z]*Proxy>$} $type all]} {
              # TODO: collection or set ?
              if {[string compare $refstatus "ref"] == 0} {
                set method "compareRefCollections"
              } else {
                set method "compareCollections"
                set needreset 1
              }
              Java_WriteLn $stream $umap \
                  "${indent}if (!${method}(${varname}1, ${varname}2)) \{"
              Java_WriteLn $stream $umap "${indent}  return false;"
              Java_WriteLn $stream $umap "${indent}\}"
            } elseif {[regexp {^List<([a-zA-Z]*Proxy)>} $type all elemtype]} {
              # TODO: ref or not ref ?
              if {[Java_IsSimpleExpressionProxy $elemtype classMap] ||
                  [string compare $varname "eventList"] == 0} {
                set method "compareExpressionLists"
              } elseif {[string compare $elemtype "EventDeclProxy"] == 0} {
                set method "compareNamedSets"
              } else {
                set method "compareLists"
              }
              set needreset 1
              Java_WriteLn $stream $umap \
                  "${indent}if (!${method}(${varname}1, ${varname}2)) \{"
              Java_WriteLn $stream $umap "${indent}  return false;"
              Java_WriteLn $stream $umap "${indent}\}"
            } elseif {[regexp {^Map} $type all]} {
              # TODO: other maps ?
              set method "compareAttributeMaps"
              Java_WriteLn $stream $umap \
                  "${indent}if (!${method}(${varname}1, ${varname}2)) \{"
              Java_WriteLn $stream $umap "${indent}  return false;"
              Java_WriteLn $stream $umap "${indent}\}"
            } elseif {![info exists classMap($type)]} {
              Java_WriteLn $stream $umap \
                  "${indent}if (!compareObjects(${varname}1, ${varname}2)) \{"
              Java_Write $stream $umap \
                  "${indent}  return reportAttributeMismatch"
              Java_WriteLn $stream $umap \
                  "(\"$name\", ${varname}1, ${varname}2);"
              Java_WriteLn $stream $umap "${indent}\}"
            } elseif {[string compare $refstatus "ref"] == 0} {
              set method "compareReferences"
              Java_WriteLn $stream $umap \
                  "${indent}if (!${method}(${varname}1, ${varname}2)) \{"
              Java_WriteLn $stream $umap \
                  "${indent}  return reportAttributeMismatch"
              Java_Write $stream $umap \
                  "${indent}      (\"$name\", "
              Java_WriteLn $stream $umap \
                  "${varname}1.getName(), ${varname}2.getName());"
              Java_WriteLn $stream $umap "${indent}\}"
            } elseif {[Java_IsSimpleExpressionProxy $type classMap] ||
                      [string compare $eqstatus "geometry"] == 0} {
              set method "$nonreporter.equals"
              Java_WriteLn $stream $umap \
                  "${indent}if (!${method}(${varname}1, ${varname}2)) \{"
              Java_Write $stream $umap \
                  "${indent}  return reportAttributeMismatch"
              Java_WriteLn $stream $umap \
                  "(\"$name\", ${varname}1, ${varname}2);"
              Java_WriteLn $stream $umap "${indent}\}"
              set needreset 1
            } else {
              Java_WriteLn $stream $umap \
                  "${indent}if (!compareProxies(${varname}1, ${varname}2)) \{"
              if {[string compare $eqstatus "optional"] == 0} {
                Java_WriteLn $stream $umap \
                    "${indent}  return reportAttributeMismatch"
                Java_WriteLn $stream $umap \
                    "${indent}      (\"$name\", ${varname}1, ${varname}2);"
              } else {
                Java_WriteLn $stream $umap "${indent}  return false;"
              }
              Java_WriteLn $stream $umap "${indent}\}"
              set needreset 1
            }
          }
          if {$geo} {
            Java_WriteLn $stream $umap "      \}"
          }
        }
        if {$needreset && [Java_ClassIsAbstract $classinfo]} {
          Java_WriteLn $stream $umap "      setSecondProxy(expected);"
        }
        Java_WriteLn $stream $umap "      return true;"
        Java_WriteLn $stream $umap "    \} else \{"
        Java_WriteLn $stream $umap "      return false;"
        Java_WriteLn $stream $umap "    \}"
      }
      Java_WriteLn $stream $umap "  \}"
      Java_WriteLn $stream $umap ""
    }

  ############################################################################
  # Write Auxiliary Methods
    Java_GenerateSeparatorComment $stream $umap "Auxiliary Methods"
    set etype "Proxy"
    Java_WriteLn $stream $umap "  private boolean compareExpressionLists"
    Java_WriteLn $stream $umap "    (final List<? extends $etype> list,"
    Java_WriteLn $stream $umap "     final List<? extends $etype> expected)"
    Java_WriteLn $stream $umap "  \{"
    Java_WriteLn $stream $umap \
        "    final Iterator<? extends $etype> iter1 = list.iterator();"
    Java_WriteLn $stream $umap \
        "    final Iterator<? extends $etype> iter2 = expected.iterator();"
    Java_WriteLn $stream $umap \
        "    while (iter1.hasNext() && iter2.hasNext()) \{"
    Java_WriteLn $stream $umap "      final $etype expr1 = iter1.next();"
    Java_WriteLn $stream $umap "      final $etype expr2 = iter2.next();"
    Java_WriteLn $stream $umap \
        "      if (!$nonreporter.equals(expr1, expr2)) \{"
    Java_WriteLn $stream $umap \
        "        return reportItemMismatch(expr1, expr2);"
    Java_WriteLn $stream $umap "      \}"
    Java_WriteLn $stream $umap "    \}"
    Java_WriteLn $stream $umap "    if (iter1.hasNext()) \{"
    Java_WriteLn $stream $umap \
        "      return reportSuperfluousItem(iter1.next());"
    Java_WriteLn $stream $umap "    \} else if (iter2.hasNext()) \{"
    Java_WriteLn $stream $umap \
        "      return reportMissingItem(iter2.next());"
    Java_WriteLn $stream $umap "    \} else \{"
    Java_WriteLn $stream $umap "      return true;"
    Java_WriteLn $stream $umap "    \}"
    Java_WriteLn $stream $umap "  \}"
    Java_WriteLn $stream $umap ""

  ############################################################################
  # Write Data Members
    Java_GenerateSeparatorComment $stream $umap "Data Members"
    Java_WriteLn $stream $umap \
        "  private final ${prefix}HashCodeVisitor mHashCodeVisitor;"
    Java_WriteLn $stream $umap \
        "  private final $visitorname $nonreporter;"

    if {$write} {
      Java_WriteLn $stream $umap ""
      Java_WriteLn $stream $umap "\}"
      close $stream
      Java_ReplaceFile $tmpname $destname
    }
  }
}



##############################################################################
# Collecting Imports
##############################################################################

proc Java_CollectGlobalImports {importMapName} {
  upvar $importMapName importMap

  set importMap(File) "java.io"
  set importMap(Writer) "java.io"
  set importMap(URI) "java.net"
  set importMap(URL) "java.net"
  set importMap(ArrayList) "java.util"
  set importMap(Collection) "java.util"
  set importMap(Collections) "java.util"
  set importMap(HashMap) "java.util"
  set importMap(HashSet) "java.util"
  set importMap(Iterator) "java.util"
  set importMap(LinkedList) "java.util"
  set importMap(List) "java.util"
  set importMap(Map) "java.util"
  set importMap(Set) "java.util"
  set importMap(THashSet) "gnu.trove.set.hash"
  set importMap(TreeMap) "java.util"

  set packprefix "net.sourceforge.waters"
  set packsuffixes [list "model.base" "model.module" "model.des" \
                         "model.unchecked" "plain.base" "subject.base"]
  foreach suffix $packsuffixes {
    set pack "$packprefix.$suffix"
    set parts [split $pack "."]
    set dir [eval file join "src" $parts]
    set joker [file join $dir "*.java"]
    set matches [glob -nocomplain $joker]
    foreach match $matches {
      set tail [file tail $match]
      set classname [file root $tail]
      set importMap($classname) $pack
    }
  }
}


proc Java_WritePackageAndImports {stream packname
                                  useMapName globalImportMapName} {
  upvar $useMapName useMap
  upvar $globalImportMapName globalImportMap
  Java_CollectUsedImports $packname useMap globalImportMap importMap
  set allimports [Java_SplitImports importMap]
  Java_WritePackageAndImportList $stream $allimports $packname
}

proc Java_CollectUsedImports {currentpack useMapName
                              globalImportMapName importMapName} {
  upvar $useMapName useMap
  upvar $globalImportMapName globalImportMap
  upvar $importMapName importMap
  set names [array names useMap]
  foreach name $names {
    if {[info exists globalImportMap($name)]} {
      set pack $globalImportMap($name)
      if {[string compare $pack $currentpack] != 0} {
        set importMap($name) $pack
      }
    }
  }
}

proc Java_SplitImports {importMapName} {
  upvar $importMapName importMap
  set imports1 ""
  set imports2 ""
  set imports3 ""
  foreach class [array names importMap] {
    set pack $importMap($class)
    set import "$pack.$class"
    if {[regexp {^net\.sourceforge\.waters\.xsd\.} $pack all]} {
      lappend imports3 $import
    } elseif {[regexp {^net\.sourceforge\.waters\.} $pack all]} {
      lappend imports2 $import
    } else {
      lappend imports1 $import
    }
  }
  return [list $imports1 $imports2 $imports3]
}

proc Java_WritePackageAndImportList {stream allimports packname} {
  puts $stream "package $packname;"
  puts $stream ""
  foreach imports $allimports {
    if {[llength $imports] > 0} {
      set imports [lsort $imports]
      foreach import $imports {
        puts $stream "import $import;"
      }
      puts $stream ""
    }
  }
  puts $stream ""
}

proc Java_WriteSetParent {stream umap varname eqstatus parent} {
  if {[string compare $umap ""] != 0} {
    upvar $umap useMap
    set umap useMap
  }
  if {[string compare $eqstatus "required"] == 0} {
    Java_WriteLn $stream $umap "    $varname.setParent($parent);"
  } else {
    Java_WriteLn $stream $umap "    if ($varname != null) \{"
    Java_WriteLn $stream $umap "      $varname.setParent($parent);"
    Java_WriteLn $stream $umap "    \}"
  }
}

proc Java_CopyInnerClass {stream umap classname} {
  if {[string compare $umap ""] != 0} {
    upvar $umap useMap
    set umap useMap
  }
  set filename [file join "waters" "tcl" "$classname.java"]
  set instream [open $filename r]
  while {![eof $instream]} {
    set line [gets $instream]
    if {[regexp {private +[a-z ]*class} $line all]} {
      Java_WriteLn $stream $umap "  $line"
      break
    }
  }
  while {![eof $instream]} {
    set line [gets $instream]
    if {[regexp {^ *$} $line all]} {
      Java_WriteLn $stream $umap ""
    } else {
      Java_WriteLn $stream $umap "  $line"
    }
  }
  close $instream
  Java_WriteLn $stream $umap ""
}


##############################################################################
# Class Structure
##############################################################################

proc Java_ClassCreate {name short parent interfaces attribs} {
  if {[string compare $short ""] == 0} {
    regsub {Proxy$} $name "" short
    set short [Java_ToEnglish $short]
  }
  return [list $name $short $parent $interfaces $attribs "" "" ""]
}

proc Java_ClassCreateAbstract {name short parent interfaces attribs} {
  set classinfo [Java_ClassCreate $name $short $parent $interfaces $attribs]
  set classinfo [Java_ClassSetAbstract $classinfo 1]
  return $classinfo
}

proc Java_ClassSetName {classinfo name} {
  return [lreplace $classinfo 0 0 $name]
}

proc Java_ClassSetShortName {classinfo short} {
  return [lreplace $classinfo 1 1 $short]
}

proc Java_ClassSetParent {classinfo parent} {
  return [lreplace $classinfo 2 2 $parent]
}

proc Java_ClassSetInterfaces {classinfo interfaces} {
  return [lreplace $classinfo 3 3 [list $interfaces]]
}

proc Java_ClassSetAttributes {classinfo attribs} {
  return [lreplace $classinfo 4 4 $attribs]
}

proc Java_ClassSetAbstract {classinfo abstract} {
  return [lreplace $classinfo 5 5 $abstract]
}

proc Java_ClassSetGeometryStatus {classinfo geo} {
  return [lreplace $classinfo 6 6 $geo]
}

proc Java_ClassSetSpecials {classinfo specials} {
  return [lreplace $classinfo 7 7 $specials]
}

proc Java_ClassSetSpecial {classinfo special} {
  return [Java_ClassSetSpecials $classinfo [list $special]]
}

proc Java_ClassGetName {classinfo} {
  return [lindex $classinfo 0]
}

proc Java_ClassGetShortName {classinfo} {
  return [lindex $classinfo 1]
}

proc Java_ClassGetParent {classinfo} {
  return [lindex $classinfo 2]
}

proc Java_ClassGetInterfaces {classinfo} {
  return [lindex $classinfo 3]
}

proc Java_ClassGetAttributes {classinfo} {
  return [lindex $classinfo 4]
}

proc Java_ClassGetAllAttributes {classinfo classMapName} {
  upvar $classMapName classMap
  set allattribs [Java_ClassGetAttributes $classinfo]
  set current [Java_ClassGetParent $classinfo]
  while {[string length $current] > 0} {
    set classinfo $classMap($current)
    set attribs [Java_ClassGetAttributes $classinfo]
    set allattribs [concat $attribs $allattribs]
    set current [Java_ClassGetParent $classinfo]
  }
  return $allattribs
}

proc Java_ClassIsAbstract {classinfo} {
  return [lindex $classinfo 5]
}

proc Java_ClassGetGeometryStatus {classinfo} {
  return [lindex $classinfo 6]
}

proc Java_ClassGetSpecials {classinfo} {
  return [lindex $classinfo 7]
}


##############################################################################
# Attributes Structure
##############################################################################

proc Java_AttribCreate {type name id eqstatus refstatus dftvalue comment} {
  return [list $type $name $id $eqstatus $refstatus $dftvalue $comment]
}

proc Java_AttribSetType {attrib type} {
  return [lreplace $attrib 0 0 $type]
}

proc Java_AttribSetName {attrib name} {
  return [lreplace $attrib 1 1 $name]
}

proc Java_AttribSetId {attrib id} {
  return [lreplace $attrib 2 2 $id]
}

proc Java_AttribSetEqualityStatus {attrib eqstatus} {
  return [lreplace $attrib 3 3 $eqstatus]
}

proc Java_AttribSetRefStatus {attrib refstatus} {
  return [lreplace $attrib 4 4 $refstatus]
}

proc Java_AttribSetDefaultValue {attrib dftvalue} {
  return [lreplace $attrib 5 5 $dftvalue]
}

proc Java_AttribSetComment {attrib comment} {
  return [lreplace $attrib 6 6 $comment]
}

proc Java_AttribGetType {attrib} {
  set type [lindex $attrib 0]
  return $type
}

proc Java_AttribGetName {attrib} {
  set name [lindex $attrib 1]
  return $name
}

proc Java_AttribGetId {attrib} {
  set id [lindex $attrib 2]
  return $id
}

proc Java_AttribGetEqualityStatus {attrib} {
  set eqstatus [lindex $attrib 3]
  return $eqstatus
}

proc Java_AttribGetRefStatus {attrib} {
  set refstatus [lindex $attrib 4]
  return $refstatus
}

proc Java_AttribGetDefaultValue {attrib impl} {
  set dftvalue [lindex $attrib 5]
  set emptytype [Java_AttribGetEmptyCollectionType $attrib $impl]
  set eqstatus [Java_AttribGetEqualityStatus $attrib]
  if {[string compare $dftvalue "none"] == 0} {
    return ""
  } elseif {[string compare $dftvalue ""] != 0} {
    return $dftvalue
  } elseif {[string compare $eqstatus "geometry"] == 0} {
    return "null"
  } elseif {[Java_IsCollectionType $emptytype]} {
    return "null"
  } else {
    return ""
  }
}

proc Java_AttribGetComment {attrib} {
  set comment [lindex $attrib 6]
  return $comment
}


proc Java_AttribGetGetterName {attrib impl} {
  set type [Java_AttribGetType $attrib]
  set name [Java_AttribGetName $attrib]
  if {[string compare $type "boolean"] == 0} {
    return "is$name"
  } else {
    return "get$name"
  }
}

proc Java_AttribGetMemberName {attrib impl} {
  set type [Java_AttribGetType $attrib]
  set name [Java_AttribGetName $attrib]
  if {[string compare $type "boolean"] == 0} {
    return "mIs$name"
  } else {
    return "m$name"
  }
}

proc Java_AttribGetParameterName {attrib impl} {
  set name [Java_AttribGetName $attrib]
  set initial [string index $name 0]
  set initial [string tolower $initial]
  set rest [string range $name 1 end]
  return "$initial$rest"
}

proc Java_AttribGetSetterName {attrib impl} {
  set name [Java_AttribGetName $attrib]
  return "set$name"
}

proc Java_AttribGetDeclaredType {attrib impl} {
  set type [Java_AttribGetType $attrib]
  if {[regexp {^NodeSet} $type all]} {
    return "Set<NodeProxy>"
  } else {
    return $type
  }
}

proc Java_AttribGetCovariantReturnType {attrib impl} {
  set decltype [Java_AttribGetDeclaredType $attrib $impl]
  return $decltype
}

proc Java_AttribGetConstructorArgumentType {attrib impl} {
  set decltype [Java_AttribGetDeclaredType $attrib $impl]
  if {[regexp {^Collection<(.*)>$} $decltype all elemtype] ||
      [regexp {^List<(.*)>$} $decltype all elemtype] ||
      [regexp {^Set<(.*)>$} $decltype all elemtype]} {
    return "Collection<? extends $elemtype>"
  } else {
    return $decltype
  }
}

proc Java_AttribGetImplementationType {attrib impl classMapName} {
  set type [Java_AttribGetType $attrib]
  if {[regexp {^Collection<(.*Proxy)>$} $type all elemtype] ||
      [regexp {^List<(.*Proxy)>$} $type all elemtype]} {
    if {[string compare $impl "plain"] == 0} {
      return "ArrayList<$elemtype>"
    } else {
      upvar $classMapName classMap
      set subjecttype [Java_GetImplClassName $elemtype $impl]
      if {[Java_IsNamedProxy $elemtype classMap]} {
        return "IndexedArrayListSubject<$subjecttype>"
      } else {
        return "ArrayListSubject<$subjecttype>"
      }
    }
  } elseif {[regexp {^Set<(.*Proxy)>$} $type all elemtype]} {
    if {[string compare $impl "plain"] == 0} {
      return "ImmutableOrderedSet<$elemtype>"
    } else {
      set refstatus [Java_AttribGetRefStatus $attrib]
      set suffix [Java_GetImplObjectName $impl]
      regsub {Proxy$} $elemtype $suffix elemtype
      if {[string compare $refstatus "ref"] == 0 &&
          [string compare $elemtype "NodeProxy"]} {
        return "ChildNodeSetSubject"
      } elseif {[string compare $refstatus "ref"] != 0} {
        return "IndexedHashSetSubject<$elemtype>"
      } else {
        puts stderr "WARNING: Unknown implementation type for $type!"
        return $type
      }
    }
  } elseif {[regexp {^List<(.*2D)>$} $type all elemtype]} {
    set suffix [Java_GetImplObjectName $impl]
    return "CloningGeometryList$suffix<$elemtype>"
  } elseif {[regexp {^Set<(Color)>$} $type all elemtype] &&
            [string compare $impl "subject"] == 0} {
    return "NotCloningGeometrySetSubject<$elemtype>"
  } elseif {[regexp {^Collection<(.*)>$} $type all elemtype] ||
            [regexp {^List<(.*)>$} $type all elemtype]} {
    return "ArrayList<$elemtype>"
  } elseif {[regexp {^Set<(.*)>$} $type all elemtype]} {
    return "THashSet<$elemtype>"
  } elseif {[regexp {^Map<String,String>$} $type all]} {
    if {[string compare $impl "plain"] == 0} {
      return "TreeMap<String,String>"
    } else {
      return "AttributeMapSubject"
    }
  } else {
    set suffix [Java_GetImplObjectName $impl]
    regsub {Proxy$} $type $suffix type
    return $type
  }
}

proc Java_AttribGetMemberType {attrib impl classMapName} {
  upvar $classMapName classMap
  set decltype [Java_AttribGetDeclaredType $attrib $impl]
  set impltype [Java_AttribGetImplementationType $attrib $impl classMap]
  if {[string compare $impl "plain"] == 0} {
    return $decltype
  } elseif {[regexp {^ArrayListSubject<(.*)>$} $impltype all elemtype]} {
    return "ListSubject<$elemtype>"
  } elseif {[regexp {^IndexedArrayListSubject<(.*)>$} $impltype all \
                 elemtype]} {
    return "IndexedListSubject<$elemtype>"
  } elseif {[regexp {^IndexedHashSetSubject<(.*)>$} $impltype all elemtype]} {
    return "IndexedSetSubject<$elemtype>"
  } elseif {[regexp {^ChildNodeSet} $impltype all]} {
    return "SetSubject<NodeSubject>"
  } elseif {[regexp {^NodeSet} $impltype all]} {
    return "IndexedSetSubject<NodeSubject>"
  } elseif {[regexp {^CloningGeometry(List)Subject<(.*)>$} \
                 $impltype all colltype elemtype] ||
            [regexp {^NotCloningGeometry(Set)Subject<(.*)>$} \
                 $impltype all colltype elemtype]} {
    return "Simple${colltype}Subject<$elemtype>"
  } elseif {[regexp {^NodeSet} $impltype all]} {
    return $impltype
  } elseif {[regexp {^AttributeMap} $impltype all]} {
    return $impltype
  } else {
    return [Java_GetImplClassName $decltype $impl]
  }
}

proc Java_AttribGetConstructorExceptions {attrib impl classMapName} {
  return ""
}

proc Java_AttribGetCollectionTransformerName {attrib impl} {
  set decltype [Java_AttribGetDeclaredType $attrib $impl]
  if {[regexp {^(Collection)<} $decltype all collectiontype] ||
      [regexp {^(List)<} $decltype all collectiontype] ||
      [regexp {^(Set)<} $decltype all collectiontype] ||
      [regexp {^(Map)<} $decltype all collectiontype]} {
    return "unmodifiable$collectiontype"
  } else {
    return ""
  }
}

proc Java_AttribGetCastTransformerName {attrib impl} {
  set decltype [Java_AttribGetDeclaredType $attrib $impl]
  if {[regexp {^(Collection)<} $decltype all collectiontype] ||
      [regexp {^(List)<} $decltype all collectiontype] ||
      [regexp {^(Set)<} $decltype all collectiontype]} {
    return "$collectiontype<?>"
  } elseif {[regexp {^(Map)<} $decltype all collectiontype]} {
    return "$collectiontype<?,?>"
  } else {
    return ""
  }
}

proc Java_AttribGetEmptyCollectionsCall {attrib impl} {
  set emptytype [Java_AttribGetEmptyCollectionType $attrib $impl]
  if {[Java_IsCollectionType $emptytype] &&
      [regexp {^([A-Z][a-z]+)<[A-Za-z0-9_,]+>$} \
           $emptytype all collectiontype]} {
    return "Collections.empty${collectiontype}()"
  } else {
    return ""
  }
}

proc Java_AttribGetEmptyCollectionType {attrib impl} {
  set decltype [Java_AttribGetDeclaredType $attrib $impl]
  regsub {^Collection<} $decltype {List<} decltype
  return $decltype
}

proc Java_AttribGetEnglishDescription {attrib impl} {
  set name [Java_AttribGetName $attrib]
  set comment [Java_AttribGetComment $attrib]
  if {[regexp {^Gets the ([a-z]+( [a-z]+)*) [a-z]+ this} $comment all descr]} {
    return $descr
  } else {
    return [Java_ToEnglish $name]
  }
}

proc Java_AttribGetEachComment {attrib impl} {
  set comment [Java_AttribGetComment $attrib]
  set needle "Each element is"
  set start [string first $needle $comment]
  if {$start >= 0} {
    set end [expr $start + [string length $needle]]
    set len [string length $comment]
    set braced 0
    while {$end < $len} {
      set ch [string index $comment $end]
      if {$braced == 0 && [string compare $ch "."] == 0} {
        break
      } elseif {[string compare $ch "\{"] == 0} {
        incr braced
      } elseif {[string compare $ch "\}"] == 0} {
        incr braced -1
      }
      incr end
    }
    set eachcomment [string range $comment $start $end]
    return $eachcomment
  } else {
    return ""
  }
}



##############################################################################
# Utilities
##############################################################################

proc Java_GetImplObjectName {impl} {
  if {[string compare $impl "plain"] == 0} {
    return "Element"
  } else {
    return [string totitle $impl]
  }
}

proc Java_GetImplClassName {type impl} {
  set suffix [Java_GetImplObjectName $impl]
  if {[regsub {Proxy$} $type $suffix type]} {
    if {[string compare $type "Subject"] == 0} {
      set type "AbstractSubject"
    }
  }
  return $type
}

proc Java_IsCollectionType {type} {
  if {[regexp {^Collection<.*>$} $type all] ||
      [regexp {^List<.*>$} $type all] ||
      [regexp {^Set<.*>$} $type all] ||
      [regexp {^Map<.*>$} $type all]} {
    return 1
  } else {
    return 0
  }
}

proc Java_GetObjectAssignableType {type} {
  if {[regexp {^[a-z]} $type all]} {
    if {[string compare $type "int"] == 0} {
      return "Integer"
    } elseif {[string compare $type "boolean"] == 0} {
      return "Boolean"
    } elseif {[string compare $type "double"] == 0} {
      return "Double"
    } else {
      puts stderr "ERROR: Unknown primitive type $type!"
      exit 1
    }
  } else {
    return $type
  }
}

proc Java_GetSuperClassInfo {supername implobjname classMapName} {
  upvar $classMapName classMap
  set mutable 1
  while {1} {
    if {[string compare $supername "SimpleExpressionProxy"] == 0} {
      set mutable 0
    }
    set superinfo $classMap($supername)
    set superabstract [Java_ClassIsAbstract $superinfo]
    if {$superabstract == 1} {
      break
    }
    set supername [Java_ClassGetParent $superinfo]
  }
  regsub {Proxy$} $supername $implobjname superclassname
  if {[string compare $superclassname "Subject"] == 0} {
    if {$mutable} {
      set superclassname "MutableSubject"
    } else {
      set superclassname "ImmutableSubject"
    }
  }
  return [list $supername $superclassname]
}

proc Java_IsProxySubtypeOf {type iface classMapName} {
  if {[string compare $type ""] == 0} {
    return 0
  } elseif {[string compare $type $iface] == 0} {
    return 1
  } else {
    upvar $classMapName classMap
    set classinfo $classMap($type)
    set type [Java_ClassGetParent $classinfo]
    return [Java_IsProxySubtypeOf $type $iface classMap]
  }
}

proc Java_IsNamedProxy {type classMapName} {
  upvar $classMapName classMap
  return [Java_IsProxySubtypeOf $type "NamedProxy" classMap]
}

proc Java_IsSimpleExpressionProxy {type classMapName} {
  upvar $classMapName classMap
  return [Java_IsProxySubtypeOf $type "SimpleExpressionProxy" classMap]
}

proc Java_ToEnglish {javaname} {
  set result ""
  while {![string is lower -failindex failindex $javaname]} {
    set end [expr $failindex - 1]
    set lower [string range $javaname 0 $end]
    set ch [string index $javaname $failindex]
    set ch [string tolower $ch]
    if {[string compare $result ""] != 0} {
      set result "$result$lower $ch"
    } else {
      set result $ch
    }
    incr failindex
    set javaname [string range $javaname $failindex end]
  }
  set result "$result$javaname"
}

proc Java_WriteConstructorComment {stream umap impl methodkind
                                   short attribs withdft} {
  Java_WriteLn $stream $umap "  /**"
  Java_Write $stream $umap "   * Creates a new $short"
  if {$withdft} {
    set numdefaults 0
    foreach attrib $attribs {
      set dftvalue [Java_AttribGetDefaultValue $attrib $impl]
      if {[string compare $dftvalue ""] != 0} {
        incr numdefaults
      }
    }
    Java_WriteLn $stream $umap " using default values."
    if {[regexp {^[aeiouAEIOU]} $short all]} {
      set art "an"
    } else {
      set art "a"
    }
    Java_WriteLn $stream $umap "   * This $methodkind creates $art $short with"
    set i 1
    foreach attrib $attribs {
      set dftvalue [Java_AttribGetDefaultValue $attrib $impl]
      if {[string compare $dftvalue ""] != 0} {
        set descr [Java_AttribGetEnglishDescription $attrib $impl]
        set type [Java_AttribGetDeclaredType $attrib $impl]
        if {[Java_IsCollectionType $type] ||
            [string compare $dftvalue "empty"] == 0} {
          Java_Write $stream $umap "   * an empty $descr"
        } else {
          Java_Write $stream $umap \
              "   * the $descr set to <CODE>$dftvalue</CODE>"
        }
        if {$i < $numdefaults - 1} {
          Java_WriteLn $stream $umap ","
        } elseif {$i == $numdefaults - 1} {
          if {$numdefaults > 2} {
            Java_Write $stream $umap ","
          }
          Java_WriteLn $stream $umap " and"
        }
        incr i
      }
    }
  }
  Java_WriteLn $stream $umap "."
  foreach attrib $attribs {
    set dftvalue [Java_AttribGetDefaultValue $attrib $impl]
    if {!$withdft || [string compare $dftvalue ""] == 0} {
      set name [Java_AttribGetParameterName $attrib $impl]
      set descr [Java_AttribGetEnglishDescription $attrib $impl]
      set eqstatus [Java_AttribGetEqualityStatus $attrib]
      set transformer [Java_AttribGetCollectionTransformerName $attrib $impl]
      Java_Write $stream $umap \
          "   * @param $name The $descr of the new $short"
      if {[string compare $transformer ""] != 0 ||
          [string compare $dftvalue "empty"] == 0} {
        Java_Write $stream $umap ", or <CODE>null</CODE> if empty"
      } elseif {[string compare $eqstatus "geometry"] == 0 ||
                [string compare $eqstatus "optional"] == 0} {
        Java_Write $stream $umap ", or <CODE>null</CODE>"
      }
      Java_WriteLn $stream $umap "."
      set decltype [Java_AttribGetDeclaredType $attrib $impl]
      if {[Java_IsCollectionType $decltype]} {
        set eachcomment [Java_AttribGetEachComment $attrib $impl]
        if {[string compare $eachcomment ""] != 0} {
          Java_WriteLn $stream $umap "   *        $eachcomment"
        }
      }
    }
  }
  Java_WriteLn $stream $umap "   */"
}

proc Java_WriteExceptionDeclaration {stream umap exceptions} {
  if {[llength $exceptions] > 0} {
    if {[string compare $umap ""] != 0} {
      upvar $umap useMap
      set umap useMap
    }
    foreach exception $exceptions {
      set exceptionArray($exception) 1
    }
    set exceptions [array names exceptionArray]
    set exceptions [lsort $exceptions]
    set exception1 [lindex $exceptions 0]
    set exceptions [lrange $exceptions 1 end]
    Java_Write $stream $umap "    throws $exception1"
    foreach exception $exceptions {
      Java_WriteLn $stream $umap ","
      Java_Write $stream $umap "           $exception"
    }
    Java_WriteLn $stream $umap ""
  }
}

proc Java_Write {stream useMapName line} {
  if {[string compare $useMapName ""] == 0} {
    puts -nonewline $stream $line
  } else {
    upvar $useMapName useMap
    Java_RecordString useMap $line
  }
}

proc Java_WriteLn {stream useMapName line} {
  if {[string compare $useMapName ""] == 0} {
    if {[string length $line] <= 78} {
      puts $stream $line
    } elseif {[regexp {^( *)([a-zA-Z][^;]*) += +([^;]*);$} \
                   $line all indent lhs rhs]} {
      puts $stream "$indent$lhs ="
      puts $stream "$indent  $rhs;"
    } else {
      puts $stream $line
    }
  } else {
    upvar $useMapName useMap
    Java_RecordString useMap $line
  }
}

proc Java_RecordString {useMapName line} {
  upvar $useMapName useMap
  regsub -all "\{@link \[^\}\.\]*\.\[^\}\]*\}" $line "" line
  set words [split $line "{}<>();,.! "]
  foreach word $words {
    if {[regexp {^[A-Z][A-Za-z0-9]+$} $word all]} {
      set useMap($word) 1
    }
  }
}

proc Java_GenerateHeaderComment {stream packname classname} {
  global gSep75 gCopyrightFile
  puts $stream "//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-"
  puts $stream "//$gSep75"
  puts $stream "//# THIS FILE HAS BEEN AUTOMATICALLY GENERATED BY A SCRIPT."
  puts $stream "//# DO NOT EDIT."
  set copyright [open $gCopyrightFile r]
  while {![eof $copyright]} {
    set line [gets $copyright]
    if {[string compare $line ""] != 0} {
      puts $stream $line
    }
  }
  close $copyright  
  puts $stream ""
}

proc Java_GenerateSeparatorComment {stream umap comment} {
  global gSep73
  if {[string compare $umap ""] == 0} {
    puts $stream ""
    puts $stream "  //$gSep73"
    puts $stream "  //# $comment"
  }
}


set gCopyrightFile \
    [file join "src" "net" "sourceforge" "waters" "config" "header_waters.txt"]

set gSpaces " "
set gSepLine "#"
for {set i 0} {$i < 7} {incr i} {
  set gSepLine "$gSepLine$gSepLine"
  set gSpaces "$gSpaces$gSpaces"
}
set gSep73 [string range $gSepLine 1 73]
set gSep75 [string range $gSepLine 1 75]
unset gSepLine


proc Java_ReplaceFile {tmpname destname} {
  if {[file exists $destname] &&
      [catch {exec diff -I {\$Id} $tmpname $destname}] == 0} {
    catch {file delete $tmpname}
  } else {
    catch {file rename -force $tmpname $destname}
    puts "  [file tail $destname]"
  }
}


##############################################################################
# Auxiliary Methods
##############################################################################

proc Aux_Hash {text hash0} {
  set result $hash0
  set len [string length $text]
  for {set i 0} {$i < $len} {incr i} {
    set ch [string index $text $i]
    scan $ch "%c" value
    set result [expr 5 * $result + $value]
  }
  catch {
    set delta -0x10000000000000000
    set result [expr $result & 0xffffffffffffffff]
    if {$result >= 0x8000000000000000} {
      incr result $delta
    }
  }
  return $result
}
