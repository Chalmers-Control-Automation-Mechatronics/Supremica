# -*- Makefile -*-
#############################################################################
#
# Makefile for libwaters.so / waters.dll
#
#############################################################################

#############################################################################
# OS dependent

ifeq "$(OSTYPE)" "linux-gnu"
  export JAVAOS        = linux
  export EXTRA_CFLAGS  = -fPIC
  export LIB_PREFIX    = lib
  export LIB_SUFFIX    = so
  RMDIR	               = rm -rf $(strip $(1))
  RMFILE	           = rm -f $(strip $(1))
else
  export JAVAOS        = win32
  export EXTRA_CFLAGS  = -D_JNI_IMPLEMENTATION_
  export EXTRA_LDFLAGS = -Wl,--kill-at
  export LIB_SUFFIX    = dll
  RMDIR                = $(if $(wildcard $(1)),rd /s /q $(subst /,\,$(1)))
  RMFILE               = $(if $(wildcard $(1)),del $(subst /,\,$(1)))
endif


#############################################################################
# List of sources

export PROJECT  = waters
export SRCDIR   = src
export JNIDIR   = $(SRCDIR)/jni
export TESDIR   = $(SRCDIR)/test
export CPPDIR   = $(SRCDIR)/waters
export OBJDIR   = objects
export LIBDIR   = lib
export BINDIR   = bin

export CPP	= $(wildcard $(CPPDIR)/analysis/*.cpp) \
		  $(wildcard $(CPPDIR)/base/*.cpp) \
		  $(wildcard $(CPPDIR)/des/*.cpp) \
		  $(wildcard $(JNIDIR)/cache/*.cpp) \
		  $(wildcard $(JNIDIR)/glue/*.cpp)
export HPP	= $(wildcard $(CPPDIR)/base/*.h) \
		  $(wildcard $(CPPDIR)/des/*.h) \
		  $(wildcard $(CPPDIR)/javah/*.h) \
		  $(wildcard $(JNIDIR)/cache/*.h) \
		  $(wildcard $(JNIDIR)/glue/*.h)
export CPP_TEST = $(TESDIR)/HashTest.cpp

export OBJ      = $(CPP:$(SRCDIR)%.cpp=$(OBJDIR)%.o)
export TESTOBJ  = $(CPP_TEST:$(SRCDIR)%.cpp=$(OBJDIR)%.o)

export LIB      = $(LIBDIR)/$(LIB_PREFIX)$(PROJECT).$(LIB_SUFFIX)
export TESTAPP  = $(BINDIR)/testapp
export DEP      = dependencies

# commands

export CXX      = g++

# flags

ifdef JAVA_HOME
  JNI_INC       = -I"$(JAVA_HOME)/include" -I"$(JAVA_HOME)/include/$(JAVAOS)"
endif

export CFLAGS   = -O2 -g -Wall -DDEBUG $(EXTRA_CFLAGS)
export INCDIRS	= -I$(SRCDIR) $(JNI_INC)
export LDFLAGS	= $(EXTRA_LDFLAGS)


#############################################################################
# Main Rules

all::
			@$(MAKE) -f Makefile.dep

test::
			@$(MAKE) -f Makefile.dep test

clean::
			-$(call RMFILE,$(DEP))
			-$(call RMDIR,$(OBJDIR))
			-$(call RMDIR,$(BINDIR))
			-$(call RMDIR,$(LIBDIR))

