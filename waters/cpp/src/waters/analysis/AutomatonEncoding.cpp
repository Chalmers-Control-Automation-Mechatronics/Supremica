//# -*- indent-tabs-mode: nil -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.analysis
//# CLASS:   AutomatonEncoding
//###########################################################################
//# $Id: AutomatonEncoding.cpp,v 1.10 2007-06-05 15:09:36 robi Exp $
//###########################################################################

#ifdef __GNUG__
#pragma implementation
#endif

#include <new>

#ifdef DEBUG
#include <iostream>
#endif /* DEBUG */

#include <jni.h>
#include <stdlib.h>

#include "jni/cache/ClassCache.h"
#include "jni/cache/ClassGlue.h"
#include "jni/cache/JavaString.h"
#include "jni/glue/IteratorGlue.h"
#include "jni/glue/KindTranslatorGlue.h"
#include "jni/glue/ProductDESGlue.h"
#include "jni/glue/SetGlue.h"
#include "jni/glue/StateGlue.h"

#include "waters/analysis/AutomatonEncoding.h"


namespace waters {

//############################################################################
//# class AutomatonRecordHashAccessor
//############################################################################

//############################################################################
//# AutomatonRecordHashAccessor: Hash Methods

uint32 AutomatonRecordHashAccessor::
hash(const void* key)
  const
{
  const jni::AutomatonGlue* aut = (const jni::AutomatonGlue*) key;
  return (uint32) aut->hashCode();
}


bool AutomatonRecordHashAccessor::
equals(const void* key1, const void* key2)
  const
{
  const jni::AutomatonGlue* aut1 = (const jni::AutomatonGlue*) key1;
  const jni::AutomatonGlue* aut2 = (const jni::AutomatonGlue*) key2;
  return aut1->equals(aut2);
}


const void* AutomatonRecordHashAccessor::
getKey(const void* value)
  const
{
  const AutomatonRecord* record = (const AutomatonRecord*) value;
  return &record->getJavaAutomaton();
}



//############################################################################
//# class AutomatonRecord
//############################################################################

//############################################################################
//# AutomatonRecord: Class Variables

const AutomatonRecordHashAccessor AutomatonRecord::theHashAccessor;


//############################################################################
//# AutomatonRecord: Constructors & Destructors

AutomatonRecord::
AutomatonRecord(const jni::AutomatonGlue aut,
                bool plant,
                jni::ClassCache* cache)
  : mJavaAutomaton(aut),
    mIsPlant(plant),
    mWordIndex(0),
    mShift(0),
    mBitMask(0)
{
  const jni::SetGlue states = aut.getStatesGlue(cache);
  const jni::IteratorGlue iter = states.iteratorGlue(cache);
  mNumStates = states.size();
  mNumBits = log2(mNumStates);
  mJavaStates = (jni::StateGlue*) malloc(mNumStates * sizeof(jni::StateGlue));
  uint32 code = 0;
  while (iter.hasNext()) {
    jobject javaobject = iter.next();
    new (&mJavaStates[code++]) jni::StateGlue(javaobject, cache);
  }
}

AutomatonRecord::
~AutomatonRecord()
{
  for (int i = 0; i < mNumStates; i++) {
    mJavaStates[i].jni::StateGlue::~StateGlue();
  }
  free(mJavaStates);
}


//############################################################################
//# AutomatonRecord: Simple Access

jni::JavaString AutomatonRecord::
getName()
  const
{
  const jni::ClassGlue* cls = mJavaAutomaton.getClass();
  JNIEnv* env = cls->getEnvironment();
  jstring jname = mJavaAutomaton.getName();
  return jni::JavaString(env, jname);
}

jni::JavaString AutomatonRecord::
getStateName(uint32 code)
  const
{
  const jni::StateGlue& state = mJavaStates[code];
  const jni::ClassGlue* cls = state.getClass();
  JNIEnv* env = cls->getEnvironment();
  jstring jname = state.getName();
  return jni::JavaString(env, jname);
}


//############################################################################
//# AutomatonRecord: Comparing

int AutomatonRecord::
compareTo(const AutomatonRecord* partner)
  const
{
  int result = partner->mNumStates - mNumStates;
  if (result != 0) {
    return result;
  } else {
    return mJavaAutomaton.compareTo(&partner->mJavaAutomaton);
  }
}

int AutomatonRecord::
compare(const void* elem1, const void* elem2)
{
  const AutomatonRecord* val1 = *((const AutomatonRecord**) elem1);
  const AutomatonRecord* val2 = *((const AutomatonRecord**) elem2);
  return val1->compareTo(val2);
}


//############################################################################
//# AutomatonRecord: Setting up

void AutomatonRecord::
allocate(int wordindex, int shift)
{
  mWordIndex = wordindex;
  mShift = shift;
  mBitMask = ((1 << mNumBits) - 1) << shift;
}



//############################################################################
//# class AutomatonEncoding
//############################################################################

//############################################################################
//# AutomatonEncoding: Constructors & Destructors

AutomatonEncoding::
AutomatonEncoding(const jni::ProductDESGlue des,
                  const jni::KindTranslatorGlue translator,
                  jni::ClassCache* cache)
{
  int totalbits = 0;
  int a, w;

  // create records ...
  const jni::SetGlue automata = des.getAutomataGlue(cache);
  const jni::IteratorGlue iter = automata.iteratorGlue(cache);
  const int numautomata = automata.size();
  AutomatonRecord** records = new AutomatonRecord*[numautomata];
  a = 0;
  while(iter.hasNext()) {
    jobject javaobject = iter.next();
    jni::AutomatonGlue aut(javaobject, cache);
    bool plant;
    switch (translator.getComponentKindGlue(&aut, cache)) {
    case jni::ComponentKind_PLANT:
      plant = true;
      break;
    case jni::ComponentKind_SPEC:
      plant = false;
      break;
    default:
      continue;
    }
    AutomatonRecord* record = new AutomatonRecord(aut, plant, cache);
    totalbits += record->getNumberOfBits();
    records[a++] = record;
  }
  mNumRecords = a;

  // sort records ...
  qsort(records, mNumRecords, sizeof(AutomatonRecord*),
        AutomatonRecord::compare);

  // allocate bits ...
  int maxwords = totalbits / 16 + 1;
  int* used = new int[maxwords];
  for (w = 0; w < maxwords; w++) {
    used[w] = 0;
  }
  mNumWords = 0;
  for (a = 0; a < mNumRecords; a++) {
    AutomatonRecord* record = records[a];
    int numbits = record->getNumberOfBits();
    for (w = 0; used[w] + numbits > 32; w++);
    int shift = used[w];
    record->allocate(w, shift);
    used[w] += numbits;
    if (w == mNumWords) {
      mNumWords++;
    }
  }

  // rearrange and store records ...
  mWordStop = new int[mNumWords];
  for (w = 0; w < mNumWords; w++) {
    mWordStop[w] = 0;
  }
  for (a = 0; a < mNumRecords; a++) {
    w = records[a]->getWordIndex();
    mWordStop[w]++;
  }
  int prev = 0;
  for (w = 0; w < mNumWords; w++) {
    used[w] = prev;
    prev = (mWordStop[w] += prev);
  }
  mAutomatonRecords = new AutomatonRecord*[mNumRecords];
  for (a = 0; a < mNumRecords; a++) {
    AutomatonRecord* record = records[a];
    w = record->getWordIndex();
    const int index = used[w]++;
    mAutomatonRecords[index] = record;
    record->setAutomatonIndex(index);
  }

  // clean up ...
  delete[] records;
  delete[] used;
}

AutomatonEncoding::
~AutomatonEncoding()
{
  for (int a = 0; a < mNumRecords; a++) {
    delete mAutomatonRecords[a];
  }
  delete [] mAutomatonRecords;
  delete [] mWordStop;
}


//############################################################################
//# AutomatonEncoding: Simple Access

bool AutomatonEncoding::
hasSpecs()
  const
{
  for (int a = 0; a < mNumRecords; a++) {
    const AutomatonRecord* record = mAutomatonRecords[a];
    if (!record->isPlant()) {
      return true;
    }
  }
  return false;
}


//############################################################################
//# AutomatonEncoding: Encoding and Decoding

void AutomatonEncoding::
encode(const uint32* decoded, uint32* encoded)
  const
{
  int a = 0;
  for (int w = 0; w < mNumWords; w++) {
    const int end = mWordStop[w];
    uint32 word = 0;
    for (; a < end; a++) {
      const AutomatonRecord* record = mAutomatonRecords[a];
      const int shift = record->getShift();
      const uint32 code = decoded[a];
      word |= (code << shift);
    }
    encoded[w] = word;
  }
}

void AutomatonEncoding::
decode(const uint32* encoded, uint32* decoded)
  const
{
  int a = 0;
  for (int w = 0; w < mNumWords; w++) {
    const int end = mWordStop[w];
    const uint32 word = encoded[w];
    for (; a < end; a++) {
      const AutomatonRecord* record = mAutomatonRecords[a];
      const int shift = record->getShift();
      const uint32 mask = record->getBitMask();
      decoded[a] = (word & mask) >> shift;
    }
  }
}

uint32 AutomatonEncoding::
get(const uint32* encoded, int index)
  const
{
  const AutomatonRecord* record = mAutomatonRecords[index];
  const int w = record->getWordIndex();
  const uint32 word = encoded[w];
  const int shift = record->getShift();
  const uint32 mask = record->getBitMask();
  return (word & mask) >> shift;
}

void AutomatonEncoding::
set(uint32* encoded, int index, uint32 code)
  const
{
  const AutomatonRecord* record = mAutomatonRecords[index];
  const int w = record->getWordIndex();
  const int shift = record->getShift();
  const uint32 mask = record->getBitMask();
  encoded[w] = (encoded[w] & ~mask) | (code << shift);
}

void AutomatonEncoding::
shift(uint32* decoded)
  const
{
  for (int a = 0; a < mNumRecords; a++) {
    const AutomatonRecord* record = mAutomatonRecords[a];
    const int shift = record->getShift();
    decoded[a] <<= shift;
  }
}


//############################################################################
//# AutomatonEncoding: Masking

void AutomatonEncoding::
initMask(uint32* mask)
  const
{
  for (int w = 0; w < mNumWords; w++) {
    mask[w] = 0;
  }
}

void AutomatonEncoding::
addToMask(uint32* mask, int index)
  const
{
  const AutomatonRecord* record = mAutomatonRecords[index];
  const int w = record->getWordIndex();
  const uint32 imask = record->getBitMask();
  mask[w] |= imask;
}

bool AutomatonEncoding::
equals(const uint32* encoded1, const uint32* encoded2, const uint32* nmask)
  const
{
  for (int w = 0; w < mNumWords; w++) {
    if ((encoded1[w] ^ encoded2[w]) & ~nmask[w]) {
      return false;
    }
  }
  return true;
}


//############################################################################
//# AutomatonEncoding: Debug Output

#ifdef DEBUG

void AutomatonEncoding::
dump()
  const
{
  std::cerr << "ENCODING DUMP:" << std::endl;
  std::cerr << "  Number of automata: " << mNumRecords << std::endl;
  std::cerr << "  Number of words: " << mNumWords << std::endl;
  for (int a = 0; a < mNumRecords; a++) {
    const AutomatonRecord* record = mAutomatonRecords[a];
    const jni::JavaString name = record->getName();
    const int numstates = record->getNumberOfStates();
    const int numbits = record->getNumberOfBits();
    const int shift = record->getShift();
    const int w = record->getWordIndex();
    std::cerr << "  #" << a << ": " << (const char*) name << " (" << numstates
              << " states) : " << w << ":" << shift << ":" << numbits
              << std::endl;
  }
  std::cerr << "END OF ENCODING DUMP" << std::endl;
}

void AutomatonEncoding::
dumpEncodedState(const uint32* encoded)
  const
{
  std::cerr << '(';
  for (int w = 0; w < mNumWords; w++) {
    if (w > 0) {
      std::cerr << ',';
    }
    std::cerr << encoded[w];
  }
  std::cerr << ") = ";
  uint32* decoded = new uint32[mNumRecords];
  decode(encoded, decoded);
  dumpDecodedState(decoded);
  delete [] decoded;
}

void AutomatonEncoding::
dumpDecodedState(const uint32* decoded)
  const
{
  std::cerr << '(';
  for (int a = 0; a < mNumRecords; a++) {
    const AutomatonRecord* record = mAutomatonRecords[a];
    const uint32 code = decoded[a];
    const jni::JavaString name = record->getStateName(code);
    if (a > 0) {
      std::cerr << ',';
    }
    std::cerr << (const char*) name;
  }
  std::cerr << ')' << std::endl;
}

#endif /* DEBUG */

}  /* namespace waters */
