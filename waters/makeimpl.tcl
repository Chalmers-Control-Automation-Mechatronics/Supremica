# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
#
# source waters/makeimpl.tcl
# Java_ProcessProxies
#

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
  Java_GenerateVisitor $subpack $prefix $destname $classes 1 \
      implClassMap importMap
  set destname [file join $indir "Abstract${prefix}ProxyVisitor.java"]
  Java_GenerateVisitor $subpack $prefix $destname $classes 0 \
      implClassMap importMap
}

proc Java_ExtractFileInfo {srcname importMapName} {
  upvar $importMapName importMap
  set attribs ""
  set inclass 0
  set incomment 0
  set optional 0
  set ref 0
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
	    } elseif {[regexp {Geometry} $type all]} {
	      set eqstatus "geometry"
	    } else {
	      set eqstatus "required"
	    }
	    if {$ref} {
	      set refstatus "ref"
	      set ref 0
	    } else {
	      set refstatus "owned"
	    }
	    set attrib [Java_AttribCreate $type $name $eqstatus $refstatus \
                            $dftvalue $comment]
	    lappend attribs $attrib
            set dftvalue ""
            set comment ""
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
      [Java_AttribCreate "String" "Name" "required" "owned" "" "name"]
  set attribLocation \
      [Java_AttribCreate "File" "Location" "ignored" "owned" "" \
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
           "NamedProxy" "" [list $attribLocation]]
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
	     "String getName()" "" "return mIdentifier.getName();"]
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
	  "final NodeSetSubject parent = (NodeSetSubject) getParent();" \
	  "try \{" \
	  "  mImmediateChildNodes = new ChildNodeSetSubject(children);" \
	  "  if (parent != null) \{" \
	  "    parent.rearrangeGroupNodes();" \
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
  }
  set classMap($interfacename) [Java_ClassSetGeometryStatus $classinfo 0]
  set attribs [Java_ClassGetAttributes $classinfo]
  foreach attrib $attribs {
    set type [Java_AttribGetDeclaredType $attrib ""]
    set parts [split $type "<>, "]
    foreach part $parts {
      set geo [Java_AddGeometryInformation classMap $part]
      if {$geo == 3} {
	set geo 2
	break
      } elseif {$geo > 0} {
	if {[regexp {^List<} $type all] || [regexp {^Set<} $type all]} {
	  set geo 2
	  break
	} else {
	  set geo 1
	}
      }
    }
    if {$geo == 2} {
      break
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
  set supername [lindex $superinfo 0]
  set superclassname [lindex $superinfo 1]
  regsub {Proxy$} $interfacename $implobjname classname

  ############################################################################
  # Extract Superclass Attributes
  set allattribs $attribs
  set current $superinterfacename
  while {[string length $current] > 0} {
    set currentinfo $classMap($current)
    set currentattribs [Java_ClassGetAttributes $currentinfo]
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
      set eqstatus [Java_AttribGetEqualityStatus $attrib $impl]
      set refstatus [Java_AttribGetRefStatus $attrib $impl]
      if {[string compare $transformer ""] == 0} {
	set membertype [Java_AttribGetMemberType $attrib $impl classMap]
        if {[regexp {2D$} $decltype all]} {
	  Java_WriteLn $stream $umap \
              "    $membername = ($decltype) $paramname.clone();"
	} elseif {[regexp {Subject$} $membertype all]} {
	  Java_WriteLn $stream $umap \
	      "    $membername = ($membertype) $paramname;"
	} else {
	  Java_WriteLn $stream $umap "    $membername = $paramname;"
	}
	set impltype $membertype
      } elseif {[string compare $impl "plain"] == 0} {
	set type [Java_AttribGetDeclaredType $attrib $impl]
	if {[regexp {^CloningGeometryListElement<(.*)>$} \
		 $impltype all elemtype]} {
	  Java_WriteLn $stream $umap \
	      "    $membername = new ${impltype}($paramname);"
	} else {
	  Java_WriteLn $stream $umap "    final $type ${paramname}Modifiable ="
	  Java_WriteLn $stream $umap "      new ${impltype}($paramname);"
	  Java_WriteLn $stream $umap "    $membername ="
	  Java_WriteLn $stream $umap \
	      "      Collections.${transformer}(${paramname}Modifiable);";
	}
      } elseif {[regexp {^.*Subject<(.*Subject)>} $impltype all elemtype]} {
	Java_WriteLn $stream $umap \
	    "    $membername = new ${impltype}"
	Java_WriteLn $stream $umap \
	    "      ($paramname, $elemtype.class);"
      } else {
	Java_WriteLn $stream $umap \
	    "    $membername = new ${impltype}($paramname);"
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

    set emptydefaults ""
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
        } elseif {[Java_IsCollectionType $decltype] &&
                  ![info exists emptynames($decltype)]} {
          set emptynames($decltype) 1
          lappend emptydefaults $attrib
        }
      }
      catch {unset emptynames}
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
        if {[string compare $dftvalue ""] != 0} {
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
    Java_GenerateSeparatorComment $stream $umap "Cloning"
    if {[string compare $impl "plain"] == 0 || [llength $attribs] == 0} {
      Java_WriteLn $stream $umap "  public $classname clone()"
      Java_WriteLn $stream $umap "  \{"
      Java_WriteLn $stream $umap "    return ($classname) super.clone();"
      Java_WriteLn $stream $umap "  \}"
      Java_WriteLn $stream $umap ""
    } else {
      foreach attrib $attribs {
	set refstatus [Java_AttribGetRefStatus $attrib $impl]
	if {[string compare $refstatus "ref"] == 0} {
	  set membertype [Java_AttribGetMemberType $attrib $impl classMap]
	  if {[regexp {^CollectionSubject<(.*)>$} $membertype all elemtype] ||
	      [regexp {^ListSubject<(.*)>$} $membertype all elemtype] ||
	      [regexp {^SetSubject<(.*)>$} $membertype all elemtype]} {
	    set refarray($elemtype) 1
	  } else {
	    set refarray($membertype) 1
	  }
	}
      }
      set reftype [array names refarray]
      set numrefs [llength $reftype]
      if {$numrefs > 1} {
	puts stderr "WARNING: More than once reference type in $classname!"
	set numrefs 1
      }
      catch {unset refarray}

      for {set withref 0} {$withref <= $numrefs} {incr withref} {
	Java_Write $stream $umap "  public $classname clone("
	if {$withref} {
	  Java_Write $stream $umap "final IndexedSet<$reftype> refmap"
	}
	Java_WriteLn $stream $umap ")"
	Java_WriteLn $stream $umap "  \{"
	Java_WriteLn $stream $umap \
	    "    final $classname cloned = ($classname) super.clone();"
	foreach attrib $attribs {
	  set decltype [Java_AttribGetDeclaredType $attrib $impl]
	  set refstatus [Java_AttribGetRefStatus $attrib $impl]
	  set membername [Java_AttribGetMemberName $attrib $impl]
	  set iscoll [Java_IsCollectionType $decltype]
	  if {[string compare $refstatus "owned"] == 0} {
	    if {$iscoll || [regexp {Proxy$} $decltype all]} {
	      set eqstatus [Java_AttribGetEqualityStatus $attrib $impl]
	      if {[string compare $decltype "List<EdgeProxy>"] == 0} {
		set impltype \
		    [Java_AttribGetImplementationType $attrib $impl classMap]
		set membertype \
		    [Java_AttribGetMemberType $attrib $impl classMap]
		regexp {<(.*)>} $membertype all elemtype
		Java_WriteLn $stream $umap \
		    "    cloned.$membername ="
		Java_WriteLn $stream $umap \
		    "      new ${impltype}($membername.size());"
		Java_WriteLn $stream $umap \
		    "    for (final $elemtype edge : $membername) \{"
		Java_Write   $stream $umap \
		    "      final $elemtype clonededge = "
		Java_WriteLn $stream $umap \
		    "edge.clone(cloned.mNodes);"
		Java_WriteLn $stream $umap \
		    "      cloned.$membername.add(clonededge);"
		Java_WriteLn $stream $umap \
		    "    \}"
		Java_WriteLn $stream $umap \
		    "    cloned.$membername.setParent(cloned);"
	      } elseif {[string compare $eqstatus "required"] == 0} {
		Java_WriteLn $stream $umap \
		    "    cloned.$membername = $membername.clone();"
		Java_WriteLn $stream $umap \
		    "    cloned.$membername.setParent(cloned);"
	      } else {
		Java_WriteLn $stream $umap \
		    "    if ($membername != null) \{"
		Java_WriteLn $stream $umap \
		    "      cloned.$membername = $membername.clone();"
		Java_WriteLn $stream $umap \
		    "      cloned.$membername.setParent(cloned);"
		Java_WriteLn $stream $umap \
		    "    \}"
	      }
            } elseif {[regexp {2D$} $decltype all]} {
              Java_WriteLn $stream $umap \
                  "    cloned.$membername = ($decltype) $membername.clone();"
            }
	  } elseif {$iscoll} {
	    set impltype \
		[Java_AttribGetImplementationType $attrib $impl classMap]
	    Java_WriteLn $stream $umap \
		"    cloned.$membername ="
	    if {$withref} {
	      Java_WriteLn $stream $umap \
		  "      new ${impltype}($membername.size());"
	      Java_WriteLn $stream $umap \
		  "    for (final $reftype item : $membername) \{"
	      Java_WriteLn $stream $umap \
		  "      final String name = item.getName();"
	      Java_WriteLn $stream $umap \
		  "      final $reftype cloneditem = refmap.find(name);"
	      Java_WriteLn $stream $umap \
		  "      cloned.$membername.add(cloneditem);"
	      Java_WriteLn $stream $umap \
		  "    \}"
	    } else {
	      Java_WriteLn $stream $umap \
		  "      new ${impltype}($membername);"
	    }
	    Java_WriteLn $stream $umap \
		"    cloned.$membername.setParent(cloned);"
	  } else {
	    if {$withref} {
	      set paramname [Java_AttribGetParameterName $attrib $impl]
	      set namename "${paramname}Name"
	      Java_WriteLn $stream $umap \
		  "    final String $namename = $membername.getName();"
	      Java_WriteLn $stream $umap \
		  "    cloned.$membername = refmap.find($namename);"
	      
	    }
	  }
	}
	Java_WriteLn $stream $umap "    return cloned;"
	Java_WriteLn $stream $umap "  \}"
	Java_WriteLn $stream $umap ""
      }
    }


  ############################################################################
  # Equality
    if {$numattribs > 0} {
      Java_GenerateSeparatorComment $stream $umap "Equality"
      set numgeoattribs 0
      foreach attrib $attribs {
	set eqstatus [Java_AttribGetEqualityStatus $attrib $impl]
	if {[string compare $eqstatus "geometry"] == 0} {
	  incr numgeoattribs
	}
      }
      if {$numattribs > $numgeoattribs} {
	Java_WriteLn $stream $umap \
	    "  public boolean equals(final Object partner)"
	Java_WriteLn $stream $umap "  {"
	Java_WriteLn $stream $umap "    if (super.equals(partner)) {"
	Java_WriteLn $stream $umap \
	    "      final $classname downcast = ($classname) partner;"
	Java_WriteLn $stream $umap "      return"
	set i [expr $numgeoattribs + 1]
	foreach attrib $attribs {
	  set eqstatus [Java_AttribGetEqualityStatus $attrib $impl]
	  if {[string compare $eqstatus "geometry"] == 0 ||
	      [string compare $eqstatus "ignored"] == 0} {
	    continue
	  }
	  set type [Java_AttribGetDeclaredType $attrib $impl]
	  set membername [Java_AttribGetMemberName $attrib $impl]
	  if {[regexp {^[a-z]} $type all]} {
	    Java_Write $stream $umap \
		"        ($membername == downcast.$membername)"
	  } elseif {[regexp {^Collection<} $type all]} {
	    Java_Write $stream $umap \
		"        ArrayCollection.equalSet"
	    Java_Write $stream $umap "($membername, downcast.$membername)"
	  } elseif {[string compare $eqstatus "required"] == 0} {
	    Java_Write $stream $umap \
		"        $membername.equals(downcast.$membername)"
	  } else {
	    Java_WriteLn $stream $umap \
		"        ($membername == null ? downcast.$membername == null :"
	    Java_Write $stream $umap \
		"         $membername.equals(downcast.$membername))"
	  }
	  if {$i < $numattribs} {
	    Java_WriteLn $stream $umap " &&"
	    incr i
	  }
	}
	Java_WriteLn $stream $umap ";"
	Java_WriteLn $stream $umap "    } else {"
	Java_WriteLn $stream $umap "      return false;"
	Java_WriteLn $stream $umap "    }"
	Java_WriteLn $stream $umap "  }"
	Java_WriteLn $stream $umap ""
      }
    }
    if {$geo == 1 || $geo == 2} {
      Java_WriteLn $stream $umap \
	  "  public boolean equalsWithGeometry(final Object partner)"
      Java_WriteLn $stream $umap "  {"
      Java_WriteLn $stream $umap "    if (super.equalsWithGeometry(partner)) {"
      Java_WriteLn $stream $umap \
	  "      final $classname downcast = ($classname) partner;"
      Java_WriteLn $stream $umap "      return"
      set i 1
      foreach attrib $attribs {
	set type [Java_AttribGetDeclaredType $attrib $impl]
	set membername [Java_AttribGetMemberName $attrib $impl]
	set eqstatus [Java_AttribGetEqualityStatus $attrib $impl]
	if {[string compare $eqstatus "ignored"] == 0} {
	  continue
	}
	if {[info exists classMap($type)]} {
	  set typeinfo $classMap($type)
	  set typegeo [Java_ClassGetGeometryStatus $typeinfo]
	}
	if {[regexp {^[a-z]} $type all]} {
	  Java_Write $stream $umap \
	      "        ($membername == downcast.$membername)"
	} elseif {[string compare $eqstatus "geometry"] == 0} {
	  Java_Write $stream $umap \
	      "        Geometry.equalGeometry"
	  Java_Write $stream $umap "($membername, downcast.$membername)"
	} elseif {[regexp {^List<} $type all]} {
	  Java_Write $stream $umap \
	      "        Geometry.equalList($membername, downcast.$membername)"
	} elseif {[regexp {^Set<} $type all]} {
	  Java_Write $stream $umap \
	      "        Geometry.equalSet($membername, downcast.$membername)"
	} elseif {[info exists classMap($type)] && $typegeo > 0} {
	  Java_Write $stream $umap \
	      "        $membername.equalsWithGeometry(downcast.$membername)"
	} elseif {[string compare $eqstatus "required"] == 0} {
	  Java_Write $stream $umap \
	      "        $membername.equals(downcast.$membername)"
	} else {
	  Java_WriteLn $stream $umap \
	      "        ($membername == null ? downcast.$membername == null :"
	  Java_Write $stream $umap \
	      "         $membername.equals(downcast.$membername))"
	}
	if {$i < $numattribs} {
	  Java_WriteLn $stream $umap " &&"
	  incr i
	}
      }
      Java_WriteLn $stream $umap ";"
      Java_WriteLn $stream $umap "    } else {"
      Java_WriteLn $stream $umap "      return false;"
      Java_WriteLn $stream $umap "    }"
      Java_WriteLn $stream $umap "  }"
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
      Java_WriteLn $stream $umap "  {"
      Java_Write   $stream $umap "    final ${prefix}ProxyVisitor downcast = "
      Java_WriteLn $stream $umap "(${prefix}ProxyVisitor) visitor;"
      Java_Write   $stream $umap "    return downcast.visit${interfacename}"
      Java_WriteLn $stream $umap "(this);"
      Java_WriteLn $stream $umap "  }"
      Java_WriteLn $stream $umap ""
    }

  ############################################################################
  # Getters
    if {[llength $attribs] > 0} {
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
	Java_WriteLn $stream $umap "  public $type ${gettername}()"
	Java_WriteLn $stream $umap "  \{"
        if {[regexp {2D$} $type all]} {
	  Java_WriteLn $stream $umap "    return ($type) $membername.clone();"
	} elseif {[string compare $transformer ""] == 0 ||
	    [string compare $impl "plain"] == 0} {
	  Java_WriteLn $stream $umap "    return $membername;"
	} else {
	  Java_WriteLn $stream $umap \
	      "    final $type downcast = Casting.${caster}($membername);"
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
            "    return getName().compareTo(partner.getName());"
	Java_WriteLn $stream $umap "  \}"
	Java_WriteLn $stream $umap ""
      } else {
	puts stderr "WARNING: Unknown interface $otherinterface!"
      }
    }

  ############################################################################
  # Setters
    if {[string compare $impl "subject"] == 0 && [llength $attribs] > 0} {
      Java_GenerateSeparatorComment $stream $umap "Setters"
      foreach attrib $attribs {
	set decltype [Java_AttribGetDeclaredType $attrib $impl]
	set membername [Java_AttribGetMemberName $attrib $impl]
	set comment [Java_AttribGetComment $attrib $impl]
        regexp {^Gets the [^\.]+\.} $comment getcomment
	if {[Java_IsCollectionType $decltype]} {
          if {[info exists getcomment]} {
            regsub {^Gets the } $getcomment {Gets the modifiable } setcomment
	    Java_WriteLn $stream $umap "  /**"
	    Java_WriteLn $stream $umap "   * $setcomment"
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
	  set eqstatus [Java_AttribGetEqualityStatus $attrib $impl]
	  set refstatus [Java_AttribGetRefStatus $attrib $impl]
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
	    Java_WriteLn $stream $umap \
		"    final Subject source = getParent();"
            Java_WriteLn $stream $umap "    final ModelChangeEvent event ="
	    Java_WriteLn $stream $umap \
		"      ModelChangeEvent.createGeometryChanged(source, this);"
	  } elseif {[string compare $eqstatus "geometry"] == 0} {
            Java_WriteLn $stream $umap "    final ModelChangeEvent event ="
	    Java_WriteLn $stream $umap \
            "      ModelChangeEvent.createGeometryChanged(this, $membername);"
	  } else {
            Java_WriteLn $stream $umap "    final ModelChangeEvent event ="
	    Java_WriteLn $stream $umap \
		"      ModelChangeEvent.createStateChanged(this);"
	  }
	  Java_WriteLn $stream $umap "    fireModelChanged(event);"
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
  # Auxiliary Methods
    if {[llength $emptydefaults] > 0} {
      Java_GenerateSeparatorComment $stream $umap "Auxiliary Methods"
      foreach attrib $emptydefaults {
        set header [Java_AttribGetDefaultValue $attrib $impl]
        set call [Java_AttribGetEmptyCollectionsCall $attrib $impl]
        set emptytype [Java_AttribGetEmptyCollectionType $attrib $impl]
	Java_WriteLn $stream $umap "  private static $emptytype $header"
	Java_WriteLn $stream $umap "  \{"
	Java_WriteLn $stream $umap "    return $call;"
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
	} else {
	  Java_WriteLn $stream $umap "  private $type $membername;"
	}
      }
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
      Java_WriteLn $stream $umap "  public static $factoryname getInstance()"
      Java_WriteLn $stream $umap "  \{"
      Java_WriteLn $stream $umap "    return INSTANCE;"
      Java_WriteLn $stream $umap "  \}"
      Java_WriteLn $stream $umap ""
    }

  ############################################################################
  # Write Creator Methods
    if {!$iface} {
      Java_GenerateSeparatorComment $stream $umap \
	  "Interface net.sourceforge.waters.model.$subpack.$interfacename"
    }
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
          Java_WriteConstructorComment $stream $umap $impl \
              "method" $short $allattribs $withdft
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
			   iface classMapName importMapName} {
  upvar $classMapName classMap
  upvar $importMapName importMap
  if {$iface} {
    set keyword "interface"
    set visitorname "${prefix}ProxyVisitor"
  } else {
    set keyword "class"
    set visitorname "Abstract${prefix}ProxyVisitor"
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
  # Write Headers
    Java_WriteLn $stream $umap "public $keyword $visitorname"
    if {$iface} {
      Java_WriteLn $stream $umap "  extends ProxyVisitor"
    } else {
      Java_WriteLn $stream $umap "  extends AbstractProxyVisitor"
      Java_WriteLn $stream $umap "  implements ${prefix}ProxyVisitor"
    }
    Java_WriteLn $stream $umap "\{"
    Java_WriteLn $stream $umap ""

  ############################################################################
  # Write Visitor Methods
    set classnames [lsort $classnames]
    foreach classname $classnames {
      set classinfo $classMap($classname)
      set supername [Java_ClassGetParent $classinfo]
      regsub {^Abstract} $supername "" supername
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
	Java_WriteLn $stream $umap "    return visit${supername}(proxy);"
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
# Collecting Imports
##############################################################################

proc Java_CollectGlobalImports {importMapName} {
  upvar $importMapName importMap

  set importMap(File) "java.io"
  set importMap(Writer) "java.io"
  set importMap(ArrayList) "java.util"
  set importMap(Collection) "java.util"
  set importMap(Collections) "java.util"
  set importMap(HashSet) "java.util"
  set importMap(List) "java.util"
  set importMap(Set) "java.util"

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

proc Java_AttribCreate {type name eqstatus refstatus dftvalue comment} {
  return [list $type $name $eqstatus $refstatus $dftvalue $comment]
}

proc Java_AttribSetType {attrib type} {
  return [lreplace $attrib 0 0 $type]
}

proc Java_AttribSetName {attrib name} {
  return [lreplace $attrib 1 1 $name]
}

proc Java_AttribSetEqualityStatus {attrib eqstatus} {
  return [lreplace $attrib 2 2 $eqstatus]
}

proc Java_AttribSetRefStatus {attrib refstatus} {
  return [lreplace $attrib 3 3 $refstatus]
}

proc Java_AttribSetDefaultValue {attrib dftvalue} {
  return [lreplace $attrib 4 4 $dftvalue]
}

proc Java_AttribSetComment {attrib comment} {
  return [lreplace $attrib 5 5 $comment]
}

proc Java_AttribGetName {attrib impl} {
  set name [lindex $attrib 1]
  return $name
}

proc Java_AttribGetGetterName {attrib impl} {
  set type [lindex $attrib 0]
  set name [Java_AttribGetName $attrib $impl]
  if {[string compare $type "boolean"] == 0} {
    return "is$name"
  } else {
    return "get$name"
  }
}

proc Java_AttribGetMemberName {attrib impl} {
  set type [lindex $attrib 0]
  set name [Java_AttribGetName $attrib $impl]
  if {[string compare $type "boolean"] == 0} {
    return "mIs$name"
  } else {
    return "m$name"
  }
}

proc Java_AttribGetParameterName {attrib impl} {
  set name [Java_AttribGetName $attrib $impl]
  set initial [string index $name 0]
  set initial [string tolower $initial]
  set rest [string range $name 1 end]
  return "$initial$rest"
}

proc Java_AttribGetSetterName {attrib impl} {
  set name [Java_AttribGetName $attrib $impl]
  return "set$name"
}

proc Java_AttribGetDeclaredType {attrib impl} {
  set type [lindex $attrib 0]
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
  set type [lindex $attrib 0]
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
      return "IndexedHashSet<$elemtype>"
    } else {
      set refstatus [Java_AttribGetRefStatus $attrib $impl]
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
    return "HashSet<$elemtype>"
  } else {
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
  } elseif {[regexp {^CloningGeometry(List)Subject<(.*)>$} \
		 $impltype all colltype elemtype] ||
	    [regexp {^NotCloningGeometry(Set)Subject<(.*)>$} \
		 $impltype all colltype elemtype]} {
    return "Simple${colltype}Subject<$elemtype>"
  } elseif {[regexp {^NodeSet} $impltype all]} {
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
      [regexp {^(Set)<} $decltype all collectiontype]} {
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
    return "to$collectiontype"
  } else {
    return ""
  }
}

proc Java_AttribGetEqualityStatus {attrib impl} {
  set eqstatus [lindex $attrib 2]
  return $eqstatus
}

proc Java_AttribGetRefStatus {attrib impl} {
  set refstatus [lindex $attrib 3]
  return $refstatus
}

proc Java_AttribGetDefaultValue {attrib impl} {
  set dftvalue [lindex $attrib 4]
  set emptytype [Java_AttribGetEmptyCollectionType $attrib $impl]
  if {[string compare $dftvalue ""] != 0} {
    return $dftvalue
  } elseif {[regexp {GeometryProxy$} $emptytype all]} {
    return "null"
  } elseif {[Java_IsCollectionType $emptytype] &&
            [regexp {^([A-Z][a-z]+)<([A-Za-z0-9_]+)>$} \
                 $emptytype all collectiontype elemtype]} {
    return "empty${elemtype}${collectiontype}()"
  } else {
    return ""
  }
}

proc Java_AttribGetEmptyCollectionsCall {attrib impl} {
  set emptytype [Java_AttribGetEmptyCollectionType $attrib $impl]
  if {[Java_IsCollectionType $emptytype] &&
      [regexp {^([A-Z][a-z]+)<[A-Za-z0-9_]+>$} \
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

proc Java_AttribGetComment {attrib impl} {
  set comment [lindex $attrib 5]
  return $comment
}

proc Java_AttribGetEnglishDescription {attrib impl} {
  set name [Java_AttribGetName $attrib $impl]
  set comment [Java_AttribGetComment $attrib $impl]
  if {[regexp {^Gets the ([a-z]+( [a-z]+)*) [a-z]+ this} $comment all descr]} {
    return $descr
  } else {
    return [Java_ToEnglish $name]
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
      [regexp {^Set<.*>$} $type all]} {
    return 1
  } else {
    return 0
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

proc Java_IsNamedProxy {type classMapName} {
  if {[string compare $type ""] == 0} {
    return 0
  } elseif {[string compare $type "NamedProxy"] == 0} {
    return 1
  } else {
    upvar $classMapName classMap
    set classinfo $classMap($type)
    set type [Java_ClassGetParent $classinfo]
    return [Java_IsNamedProxy $type classMap]
  }
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
        if {[Java_IsCollectionType $type]} {
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
  set dftvalue ""
  foreach attrib $attribs {
    if {$withdft} {
      set dftvalue [Java_AttribGetDefaultValue $attrib $impl]
    }
    if {[string compare $dftvalue ""] == 0} {
      set name [Java_AttribGetParameterName $attrib $impl]
      set descr [Java_AttribGetEnglishDescription $attrib $impl]
      set eqstatus [Java_AttribGetEqualityStatus $attrib $impl]
      Java_Write $stream $umap \
          "   * @param $name The $descr of the new $short"
      if {[string compare $eqstatus "geometry"] == 0 ||
          [string compare $eqstatus "optional"] == 0} {
        Java_Write $stream $umap ", or <CODE>null</CODE>"
      }
      Java_WriteLn $stream $umap "."
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
    puts $stream $line
  } else {
    upvar $useMapName useMap
    Java_RecordString useMap $line
  }
} 

proc Java_RecordString {useMapName line} {
  upvar $useMapName useMap
  set words [split $line "{}<>();,. "]
  foreach word $words {
    if {[regexp {^[A-Z][A-Za-z0-9]+$} $word all]} {
      set useMap($word) 1
    }
  }
} 

proc Java_GenerateHeaderComment {stream packname classname} {
  global gSep75
  puts $stream "//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-"
  puts $stream "//$gSep75"
  puts $stream "//# PROJECT: Waters"
  puts $stream "//# PACKAGE: $packname"
  puts $stream "//# CLASS:   $classname"
  puts $stream "//$gSep75"
  puts $stream "//# \$Id\$"
  puts $stream "//$gSep75"
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


