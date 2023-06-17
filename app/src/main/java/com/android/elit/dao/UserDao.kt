package com.android.elit.dao

import com.android.elit.dataclass.Users
import com.google.firebase.firestore.FirebaseFirestore

class UserDao {
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

    fun addUsers(id: String, user: Users) {
        usersCollection.document(id).set(user)
    }

    fun updateUser(id: String, key: String, value: String) {
        getUserById(id).update(key, value)
    }

    fun deleteUsers(id: String) {
        usersCollection.document(id).delete()
    }

    fun getUsers() = usersCollection

    fun getUserById(id: String) = usersCollection.document(id)

    fun checkEmail(email: String) = usersCollection.whereEqualTo("email", email)

    fun checkUsername(username: String) = usersCollection.whereEqualTo("username", username)

    fun checkPhone(phone: String) = usersCollection.whereEqualTo("phone", phone)

    fun getFavoriteBooks(id: String) = usersCollection.document(id).collection("favorite")

}