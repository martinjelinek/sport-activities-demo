package io.github.martinjelinek.sportactivitiesdemo.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import io.github.martinjelinek.sportactivitiesdemo.domain.model.SportActivity
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await

@Singleton
internal class FirestoreRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
) : RemoteDataSource {

    // Serializes anonymous sign-in so two concurrent callers can't both mint a user.
    private val authMutex = Mutex()

    override fun observe(): Flow<List<SportActivity>> = callbackFlow {
        val registration = userCollection(ensureSignedIn())
            .orderBy(FIELD_CREATED_AT, Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error); return@addSnapshotListener
                }
                val items = snapshot?.documents
                    ?.mapNotNull { it.toObject(SportActivityDto::class.java) }
                    ?.map { it.toDomain() }
                    ?: emptyList()
                trySend(items)
            }
        awaitClose { registration.remove() }
    }

    override suspend fun save(sportActivity: SportActivity) {
        userCollection(ensureSignedIn())
            .document(sportActivity.id)
            .set(sportActivity.toDto())
            .await()
    }

    /**
     * Ensure that there is a signed-in user.
     * Uses a mutex so two concurrent callers can't both create a user.
     */
    private suspend fun ensureSignedIn(): String =
        auth.currentUser?.uid ?: authMutex.withLock {
            auth.currentUser?.uid
                ?: auth.signInAnonymously().await().user?.uid
                ?: error("Anonymous sign-in returned no user")
        }

    private fun userCollection(uid: String): CollectionReference =
        firestore.collection(USERS).document(uid).collection(COLLECTION)

    companion object {
        private const val USERS = "users"
        private const val COLLECTION = "sport_activities"
        private const val FIELD_CREATED_AT = "createdAt"
    }
}
