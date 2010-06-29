# -*- Makefile -*-
#############################################################################
#
# Makefile for libwaters.so
#
#############################################################################

#############################################################################
# OS dependent

ifeq "$(OSTYPE)" "linux"
  RMDIR         = rm -rf
  JAVAOS        = linux
  EXTRA_CFLAGS  = -fPIC
  LIB_PREFIX    = lib
  LIB_SUFFIX    = so
else
  RMDIR         = rd /s /q
  JAVAOS        = win32
  EXTRA_CFLAGS  = -D_JNI_IMPLEMENTATION_
  EXTRA_LDFLAGS = -Wl,--kill-at
  LIB_SUFFIX    = dll
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
			-$(RMDIR) $(DEP) $(OBJDIR) $(BINDIR) $(LIBDIR)

