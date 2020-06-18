# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
###########################################################################
# Copyright (C) 2004-2020 Robi Malik
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
# source waters/makelic.tcl
# Java_DistributeLicense
###########################################################################
# File that need manual patching of license:
# src/waters/net/sourceforge/waters/config/*.txt
# waters/*.tcl
# waters/cpp/src/jni/templates/*
# waters/xml/xsd/*.xsd
# waters/xml/xsl/*.xsl
###########################################################################

set gHeaderDir [file join "src" "net" "sourceforge" "waters" "config"]
set gWatersHeader [file join $gHeaderDir "header_waters.txt"]
set gSupremicaHeader [file join $gHeaderDir "header_supremica.txt"]

proc Java_DistributeLicense {} {
  set roots [list "src/net" "src/org/supremica" "tests/src/net" "waters"]
  foreach root $roots {
    Java_ProcessDirectory $root
  }
  return ""
}

proc Java_ProcessDirectory {dir} {
  if {[regexp {org/supremica/.+} $dir all]} {
    if {![regexp {org/supremica/gui} $dir all] &&
        ![regexp {org/supremica/log} $dir all]} {
      return
    }
  } elseif {[regexp {^waters/cpp/include/?$} $dir all]} {
    return
  }
  set joker [file join $dir "*"]
  set matches [glob -nocomplain $joker]
  foreach match $matches {
    if {[file isdirectory $match]} {
      Java_ProcessDirectory $match
    } elseif {[regexp {\.(java|cpp|h)$} $match all]} {
      Java_ProcessFile $match
    }
  }
}

proc Java_ProcessFile {fileName} {
  set java [regexp {\.java$} $fileName]
  set inStream [open $fileName r]
  while {![eof $inStream]} {
    set line [gets $inStream]
    if {[regexp "DO NOT EDIT" $line] ||
        [regexp "This file was generated" $line]} {
      close $inStream
      return
    } elseif {$java} {
      if {[regexp {^ *package +([^ ]+) *; *$} $line all pack]} {
        set line "package $pack;"
        break
      }
    } elseif {![regexp {^ *$} $line all] && ![regexp {^ *//} $line all]} {
      break
    }
    set line ""
  }
  set tmpName "$fileName.tmp"
  set outStream [open $tmpName w]
  if {$java} {
    puts $outStream "//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-"
  } elseif {[regexp {\.cpp$} $fileName all]} {
    puts $outStream "//# -*- indent-tabs-mode: nil -*-"
  } elseif {[regexp {\.h$} $fileName all]} {
    puts $outStream \
        "//# This may look like C code, but it really is -*- C++ -*-"
  }
  global gWatersHeader gSupremicaHeader
  if {[regexp {/org/supremica/} $fileName all]} {
    set headStream [open $gSupremicaHeader r]
  } else {
    set headStream [open $gWatersHeader r]
  }
  while {![eof $headStream]} {
    set head [gets $headStream]
    if {![regexp {^ *$} $head all]} {
      puts $outStream $head
    }
  }
  close $headStream
  if {[string compare $line ""] != 0} {
    puts $outStream ""
    puts $outStream $line
    set blankLines 0
    while {![eof $inStream]} {
      set line [gets $inStream]
      if {[regexp {^ *$} $line all]} {
        incr blankLines
      } else {
        for {set i 0} {$i < $blankLines} {incr i} {
          puts $outStream ""
        }
        puts $outStream $line
        set blankLines 0
      }
    }
  }
  close $inStream
  close $outStream
  file rename -force $tmpName $fileName
}
