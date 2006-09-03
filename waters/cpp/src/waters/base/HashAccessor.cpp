//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.base
//# CLASS:   HashAccessor
//###########################################################################
//# $Id: HashAccessor.cpp,v 1.3 2006-09-03 06:38:42 robi Exp $
//###########################################################################

#ifdef __GNUG__
#pragma implementation
#endif

#include <string.h>

#include "waters/base/HashAccessor.h"


namespace waters {

//############################################################################
//# Some Hash Functions
//############################################################################

const uint32 gold = 0x9e3779b9;

#define mix(a,b,c) \
{ \
  a -= b; a -= c; a ^= (c >> 13); \
  b -= c; b -= a; b ^= (a << 8); \
  c -= a; c -= b; c ^= (b >> 13); \
  a -= b; a -= c; a ^= (c >> 12); \
  b -= c; b -= a; b ^= (a << 16); \
  c -= a; c -= b; c ^= (b >> 5); \
  a -= b; a -= c; a ^= (c >> 3); \
  b -= c; b -= a; b ^= (a << 10); \
  c -= a; c -= b; c ^= (b >> 15); \
}

uint32 hashInt(uint32 key)
{
  register uint32 a = key - gold;
  register uint32 b = 0;
  register uint32 c = -a;

  a -= gold; a ^= (gold >> 13);
  b -= a; b ^= (a << 8);
  c -= a; c -= b; c ^= (b >> 13);
  a -= b; a -= c; a ^= (c >> 12);
  b -= c; b -= a; b ^= (a << 16);
  c -= a; c -= b; c ^= (b >> 5);
  a -= b; a -= c; a ^= (c >> 3);
  b -= c; b -= a; b ^= (a << 10);
  c -= a; c -= b; c ^= (b >> 15);

  return c;
}


uint32 hashInt(int key)
{
  return hashInt((uint32) key);
}


uint32 hashInt(const void* key)
{
  return hashInt((uint32) key);
}


uint32 hashIntArray(const uint32* key, const int len)
{
  register uint32 a = gold;
  register uint32 b = gold;
  register uint32 c = gold;
  register int rest = len;

  // Handle most of the key ...
  while (rest >= 3) {
    a += key[0];
    b += key[1];
    c += key[2];
    mix(a,b,c);
    key += 3;
    rest -= 3;
  }

  // Handle the last 1 or 2 words ...
  switch(rest) {
    // all the case statements fall through ...
  case 2: b += key[1];
  case 1: a += key[0];
    // case 0: nothing left to add
  }
  mix(a,b,c);
 
  return c;
}


uint32 hashString(const char* key)
{
  int len = strlen(key);
  register uint32 a = gold;
  register uint32 b = gold;
  register uint32 c = gold;
  register int rest = len;

  // Handle most of the key ...
  while (rest >= 12) {
    a += (key[0] + (key[1] << 8) + (key[2] << 16) + (key[3] << 24));
    b += (key[4] + (key[5] << 8) + (key[6] << 16) + (key[7] << 24));
    c += (key[8] + (key[9] << 8) + (key[10] << 16)+ (key[11] << 24));
    mix(a,b,c);
    key += 12;
    rest -= 12;
  }

  // Handle the last 11 bytes ...
  c += len;
  switch(rest) {
    // all the case statements fall through ...
  case 11: c += (key[10] << 24);
  case 10: c += (key[9] << 16);
  case 9 : c += (key[8] << 8);
    // the first byte of c is reserved for the length
  case 8 : b += (key[7] << 24);
  case 7 : b += (key[6] << 16);
  case 6 : b += (key[5] << 8);
  case 5 : b += key[4];
  case 4 : a += (key[3] << 24);
  case 3 : a += (key[2] << 16);
  case 2 : a += (key[1] << 8);
  case 1 : a += key[0];
    // case 0: nothing left to add
  }
  mix(a,b,c);
 
  return c;
}

}  /* namespace waters */
