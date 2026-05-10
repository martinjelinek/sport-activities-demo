package io.github.martinjelinek.sportactivitiesdemo.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import io.github.martinjelinek.sportactivitiesdemo.domain.model.SportActivity
import javax.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
) : RemoteDataSource {

    private val collection get() = firestore.collection(COLLECTION)

    override fun observe(): Flow<List<SportActivity>> = callbackFlow {
        val registration = collection
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
        collection.document(sportActivity.id).set(sportActivity.toDto()).await()
    }

    companion object {
        private const val COLLECTION = "sport_activities"
        private const val FIELD_CREATED_AT = "createdAt"
    }
}
