package com.android.elit.dao

import com.android.elit.dataclass.Users
import com.google.firebase.firestore.FirebaseFirestore

class UsersDao {
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

    fun addUsers(id: String, user: HashMap<String, String>) {
        usersCollection.document(id).set(user)
    }

    fun updateUsers(user: Users) {
        usersCollection.document(user.id.toString()).set(user)
    }

    fun deleteUsers(user: Users) {
        usersCollection.document(user.id.toString()).delete()
    }

    fun getUsers() = usersCollection

    fun getUserById(id: String) = usersCollection.document(id)

    fun checkEmail(email: String) = usersCollection.whereEqualTo("email", email)

    fun checkUsername(username: String) = usersCollection.whereEqualTo("username", username)

    fun checkPhone(phone: String) = usersCollection.whereEqualTo("phone", phone)

    fun getFavoriteBooks(id: String) = usersCollection.document(id).collection("favorite")

}