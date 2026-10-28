package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.remote.CloudBookmark
import com.example.data.remote.CloudNote
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

class CloudSyncRepositoryRuleTest : FirestoreEmulatorTestBase() {

  private lateinit var repository: CloudSyncRepository

  @Before
  fun setUpRepository() {
    repository = CloudSyncRepository(firestore)
  }

  @Test
  fun saveAndReadBookmark_whenAuthenticatedAsOwner_succeeds() = runTest {
    val uid = signInTestUser()
    val bookmark = CloudBookmark(
      itemType = "AYAH",
      referenceKey = "2:255",
      title = "Al-Baqarah 2:255",
      arabicSnippet = "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ",
      translationSnippet = "Allah - there is no deity except Him"
    )

    repository.saveBookmark(uid, "ayah_2_255", bookmark)
    val fetched = repository.getBookmark(uid, "ayah_2_255")

    assertNotNull(fetched)
    assertEquals("ayah_2_255", fetched?.id)
    assertEquals("Al-Baqarah 2:255", fetched?.title)
    assertEquals("2:255", fetched?.referenceKey)
  }

  @Test
  fun saveAndReadNote_whenAuthenticatedAsOwner_succeeds() = runTest {
    val uid = signInTestUser()
    val note = CloudNote(
      itemType = "HADITH",
      referenceKey = "nawawi_1",
      title = "Sincerity of Intention",
      content = "Every action is judged by its intention."
    )

    repository.saveNote(uid, "note_nawawi_1", note)
    val fetched = repository.getNote(uid, "note_nawawi_1")

    assertNotNull(fetched)
    assertEquals("note_nawawi_1", fetched?.id)
    assertEquals("Sincerity of Intention", fetched?.title)
  }

  @Test
  fun saveBookmark_whenUnauthenticated_throwsPermissionDenied() = runTest {
    signOutTestUser()
    val bookmark = CloudBookmark(
      itemType = "AYAH",
      referenceKey = "1:1",
      title = "Al-Fatihah 1:1",
      arabicSnippet = "بِسْمِ اللَّهِ",
      translationSnippet = "In the name of Allah"
    )

    try {
      repository.saveBookmark("some_user_id", "ayah_1_1", bookmark)
      fail("Expected PERMISSION_DENIED when unauthenticated")
    } catch (e: FirebaseFirestoreException) {
      assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, e.code)
    }
  }

  @Test
  fun readBookmark_whenAuthenticatedAsDifferentUser_throwsPermissionDenied() = runTest {
    val ownerUid = signInTestUser()
    val bookmark = CloudBookmark(
      itemType = "AYAH",
      referenceKey = "36:1",
      title = "Ya-Sin 36:1",
      arabicSnippet = "يس",
      translationSnippet = "Ya, Seen."
    )
    repository.saveBookmark(ownerUid, "ayah_36_1", bookmark)

    // Switch to another user
    signOutTestUser()
    val otherUid = signInTestUser()
    org.junit.Assert.assertNotEquals(ownerUid, otherUid)

    try {
      repository.getBookmark(ownerUid, "ayah_36_1")
      fail("Expected PERMISSION_DENIED when reading another user's bookmark")
    } catch (e: FirebaseFirestoreException) {
      assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, e.code)
    }
  }
}
