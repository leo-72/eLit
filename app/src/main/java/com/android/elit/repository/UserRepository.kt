package com.android.elit.repository

import com.android.elit.dao.UserDao
import com.android.elit.dataclass.Users

class UserRepository {
    private val usersDao = UserDao()

    fun addUsers(id: String, user: Users) {
        usersDao.addUsers(id, user)
    }

    fun updateUser(user: String, key: String, value: String) {
        usersDao.updateUser(user, key, value)
    }

    fun deleteUsers(id: String) {
        usersDao.deleteUsers(id)
    }

    fun getUsers() = usersDao.getUsers()

    fun getUserById(id: String) = usersDao.getUserById(id)

    fun checkEmail(email: String) = usersDao.checkEmail(email)

    fun checkUsername(username: String) = usersDao.checkUsername(username)

    fun checkPhone(phone: String) = usersDao.checkPhone(phone)

    fun getFavoriteBooks(id: String) = usersDao.getFavoriteBooks(id)
}