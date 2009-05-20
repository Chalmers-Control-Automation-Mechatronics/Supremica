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
#include "jni/glue/EventGlue.h"
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
AutomatonRecord(const jni::AutomatonGlue& aut,
                bool plant,
                const jni::EventGlue& omega,
                jni::ClassCache* cache)
  : mJavaAutomaton(aut),
    mIsPlant(plant),
    mWordIndex(0),
    mShift(0),
    mBitMask(0)
{
  const jni::SetGlue states = aut.getStatesGlue(cache);
  mNumStates = states.size();
  mNumBits = log2(mNumStates);
  mJavaStates = (jni::StateGlue*) malloc(mNumStates * sizeof(jni::StateGlue));
  if (omega.isNull()) {
    initNonMarking(cache);
  } else {
    initMarking(omega, cache);
  }
}

AutomatonRecord::
~AutomatonRecord()
{
  for (uint32 code = 0; code < mNumStates; code++) {
    mJavaStates[code].jni::StateGlue::~StateGlue();
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

const jni::StateGlue& AutomatonRecord::
getJavaState(uint32 code)
  const
{
  return mJavaStates[code];
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

int AutomatonRecord::
compareToByMarking(const AutomatonRecord* partner)
  const
{
  float prob1 = (float) getNumberOfMarkedStates() / (float) mNumStates;
  float prob2 = (float) partner->getNumberOfMarkedStates() /
                (float) partner->mNumStates;
  if (prob1 < prob2) {
    return -1;
  } else if (prob1 > prob2) {
    return 1;
  } else {
    return compareTo(partner);
  }
}

int AutomatonRecord::
compareByMarking(const void* elem1, const void* elem2)
{
  const AutomatonRecord* val1 = *((const AutomatonRecord**) elem1);
  const AutomatonRecord* val2 = *((const AutomatonRecord**) elem2);
  return val1->compareToByMarking(val2);
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

HashTable<const jni::StateGlue*,uint32>* AutomatonRecord::
createStateMap()
{
  HashTable<const jni::StateGlue*,uint32>* statemap =
    new HashTable<const jni::StateGlue*,uint32>(this, mNumStates);
  for (uint32 code = 0; code < mNumStates; code++) {
    statemap->add(code);
  }
  return statemap;
}

void AutomatonRecord::
deleteStateMap(HashTable<const jni::StateGlue*,uint32>* statemap)
{
  delete statemap;
}


//############################################################################
//# AutomatonRecord: Auxiliary Methods

void AutomatonRecord::
initNonMarking(jni::ClassCache* cache)
{
  const jni::SetGlue states = mJavaAutomaton.getStatesGlue(cache);
  const jni::IteratorGlue iter = states.iteratorGlue(cache);
  uint32 nextinit = 0;
  uint32 nextnoninit = mNumStates - 1;
  while (iter.hasNext()) {
    jobject javaobject = iter.next();
    jni::StateGlue state(javaobject, cache);
    uint32 code;
    if (state.isInitial()) {
      code = nextinit++;
    } else {
      code = nextnoninit--;
    }
    new (&mJavaStates[code]) jni::StateGlue(state);
  }
  mFirstInitialState = 0;
  mEndInitialStates = nextinit;
  mFirstMarkedState = mNumStates;
}

void AutomatonRecord::
initMarking(const jni::EventGlue& omega, jni::ClassCache* cache)
{
  int cat;
  uint32 catindex[CAT_COUNT];
  for (cat = 0; cat < CAT_COUNT; cat++) {
    catindex[cat] = 0;
  }
  const jni::SetGlue states = mJavaAutomaton.getStatesGlue(cache);
  const jni::IteratorGlue iter1 = states.iteratorGlue(cache);
  while (iter1.hasNext()) {
    jobject javaobject = iter1.next();
    jni::StateGlue state(javaobject, cache);
    cat = getCategory(state, omega, cache);
    catindex[cat]++;
  }
  uint32 start = 0;
  for (cat = 0; cat < CAT_COUNT; cat++) {
    uint32 next = start + catindex[cat];
    catindex[cat] = start;
    start = next;
  }
  const jni::IteratorGlue iter2 = states.iteratorGlue(cache);
  while (iter2.hasNext()) {
    jobject javaobject = iter2.next();
    jni::StateGlue state(javaobject, cache);
    cat = getCategory(state, omega, cache);
    const uint32 code = catindex[cat]++;
    new (&mJavaStates[code]) jni::StateGlue(state);
  }
  mFirstInitialState = catindex[0];
  mEndInitialStates = catindex[2];
  mFirstMarkedState = catindex[1];
}

int AutomatonRecord::
getCategory(const jni::StateGlue& state,
            const jni::EventGlue& omega,
            jni::ClassCache* cache)
{
  int init = state.isInitial() ? 1 : 0;
  jni::CollectionGlue props = state.getPropositionsGlue(cache);
  int marked = props.contains(&omega) ? 3 : 0;
  return init ^ marked;
}


//############################################################################
//# AutomatonRecord: Hash Methods (for states!!!)

uint32 AutomatonRecord::
hash(const void* key)
  const
{
  const jni::StateGlue* state = (const jni::StateGlue*) key;
  const int javahash = state->hashCode();
  return waters::hashInt(javahash);
}

bool AutomatonRecord::
equals(const void* key1, const void* key2)
  const
{
  const jni::StateGlue* state1 = (const jni::StateGlue*) key1;
  const jni::StateGlue* state2 = (const jni::StateGlue*) key2;
  return state1->equals(state2);
}

const void* AutomatonRecord::
getKey(const void* value)
  const
{
  const uint32 code = (uint32) value;
  return &mJavaStates[code];
}


//############################################################################
//# class AutomatonEncoding
//############################################################################

//############################################################################
//# AutomatonEncoding: Constructors & Destructors

AutomatonEncoding::
AutomatonEncoding(const jni::ProductDESGlue& des,
                  const jni::KindTranslatorGlue& translator,
                  const jni::EventGlue& omega,
                  jni::ClassCache* cache,
                  int numtags)
  : mNumTags(numtags),
    mMarkingTestRecords(0),
    mNumMarkingTestRecords(0)
{
  int totalbits = numtags;
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
    AutomatonRecord* record = new AutomatonRecord(aut, plant, omega, cache);
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
  used[0] = numtags;
  for (w = 1; w < maxwords; w++) {
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
  delete [] mMarkingTestRecords;
}


//############################################################################
//# AutomatonEncoding: Simple Access

uint32 AutomatonEncoding::
getInverseTagMask()
  const
{
  return ~((1 << mNumTags) - 1);
}

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

int AutomatonEncoding::
getNumberOfNondeterministicInitialAutomata()
  const
{
  int ndcount = 0;
  for (int a = 0; a < mNumRecords; a++) {
    const AutomatonRecord* record = mAutomatonRecords[a];
    if (record->getNumberOfInitialStates() > 1) {
      ndcount++;
    }
  }
  return ndcount;
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
//# AutomatonEncoding: Marking

void AutomatonEncoding::
setupMarkingTest()
{
  if (mMarkingTestRecords == 0) {
    int a;
    for (a = 0; a < mNumRecords; a++) {
      AutomatonRecord* record = mAutomatonRecords[a];
      if (!record->isAllMarked()) {
        mNumMarkingTestRecords++;
      }
    }
    mMarkingTestRecords =
      new const AutomatonRecord*[mNumMarkingTestRecords];
    mNumMarkingTestRecords = 0;
    for (a = 0; a < mNumRecords; a++) {
      const AutomatonRecord* record = mAutomatonRecords[a];
      if (!record->isAllMarked()) {
        mMarkingTestRecords[mNumMarkingTestRecords++] = record;
      }
    }
    qsort(mMarkingTestRecords, mNumMarkingTestRecords,
          sizeof(AutomatonRecord*), AutomatonRecord::compareByMarking);
  }
}

bool AutomatonEncoding::
isMarkedStateTuplePacked(const uint32* encoded)
  const
{
  for (int a = 0; a < mNumMarkingTestRecords; a++) {
    const AutomatonRecord* record = mMarkingTestRecords[a];
    const uint32 index = record->getAutomatonIndex();
    const uint32 state = get(encoded, index);
    if (!record->isMarkedState(state)) {
      return false;
    }
  }
  return true;
}

bool AutomatonEncoding::
isMarkedStateTuple(const uint32* decoded)
  const
{
  for (int a = 0; a < mNumMarkingTestRecords; a++) {
    const AutomatonRecord* record = mMarkingTestRecords[a];
    const uint32 index = record->getAutomatonIndex();
    if (!record->isMarkedState(decoded[index])) {
      return false;
    }
  }
  return true;
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


