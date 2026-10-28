const { test, describe, before, after, beforeEach } = require('node:test');
const assert = require('node:assert');
const { initializeTestEnvironment, assertFails, assertSucceeds } = require('@firebase/rules-unit-testing');
const fs = require('node:fs');

let testEnv;

before(async () => {
  testEnv = await initializeTestEnvironment({
    projectId: 'demo-bayanalhuda-test',
    firestore: {
      rules: fs.readFileSync('firestore.rules', 'utf8'),
      host: '127.0.0.1',
      port: 8085
    }
  });
});

after(async () => {
  await testEnv.cleanup();
});

beforeEach(async () => {
  await testEnv.clearFirestore();
});

describe('Bayan al-Huda Firestore Security Rules', () => {
  const validBookmark = () => ({
    itemType: 'AYAH',
    referenceKey: '2:255',
    title: 'Al-Baqarah 2:255 (Ayat al-Kursi)',
    arabicSnippet: 'اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ',
    translationSnippet: 'Allah - there is no deity except Him, the Ever-Living',
    createdAt: new Date(Date.now() - 1000)
  });

  const validNote = () => ({
    itemType: 'HADITH',
    referenceKey: 'nawawi_1',
    title: '40 Hadith Nawawi #1',
    content: 'Actions are judged by intentions, so every person will be rewarded according to what they intended.',
    createdAt: new Date(Date.now() - 1000),
    updatedAt: new Date(Date.now() - 1000)
  });

  test('denies unauthenticated users from reading or writing bookmarks and notes', async () => {
    const unauthedDb = testEnv.unauthenticatedContext().firestore();
    await assertFails(unauthedDb.doc('users/alice/bookmarks/bm_1').get());
    await assertFails(unauthedDb.doc('users/alice/bookmarks/bm_1').set(validBookmark()));
    await assertFails(unauthedDb.doc('users/alice/notes/note_1').get());
    await assertFails(unauthedDb.doc('users/alice/notes/note_1').set(validNote()));
  });

  test('allows authenticated owner to create, read, update, and delete valid bookmarks', async () => {
    const aliceDb = testEnv.authenticatedContext('alice').firestore();
    const bmRef = aliceDb.doc('users/alice/bookmarks/bm_2_255');

    await assertSucceeds(bmRef.set(validBookmark()));
    await assertSucceeds(bmRef.get());
    await assertSucceeds(bmRef.delete());
  });

  test('denies cross-user access to bookmarks and notes', async () => {
    const aliceDb = testEnv.authenticatedContext('alice').firestore();
    const bobDb = testEnv.authenticatedContext('bob').firestore();

    await assertSucceeds(aliceDb.doc('users/alice/bookmarks/bm_1').set(validBookmark()));
    await assertSucceeds(aliceDb.doc('users/alice/notes/note_1').set(validNote()));

    await assertFails(bobDb.doc('users/alice/bookmarks/bm_1').get());
    await assertFails(bobDb.doc('users/alice/bookmarks/bm_1').delete());
    await assertFails(bobDb.doc('users/alice/notes/note_1').get());
    await assertFails(bobDb.doc('users/alice/notes/note_1').update({ content: 'Tampered' }));
  });

  test('denies bookmarks and notes with invalid itemType or extra fields', async () => {
    const aliceDb = testEnv.authenticatedContext('alice').firestore();

    await assertFails(aliceDb.doc('users/alice/bookmarks/bm_bad').set({
      ...validBookmark(),
      itemType: 'INVALID_TYPE'
    }));

    await assertFails(aliceDb.doc('users/alice/notes/note_bad').set({
      ...validNote(),
      extraField: 'unauthorized'
    }));
  });
});
