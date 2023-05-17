package com.android.elit.repository

import com.android.elit.dao.UsersDao
import com.android.elit.dataclass.Users

class UsersRepository {
    private val usersDao = UsersDao()

    fun addUsers(id: String, user: HashMap<String, String>) {
        usersDao.addUsers(id,user)
    }

    fun updateUsers(user: Users) {
        usersDao.updateUsers(user)
    }

    fun deleteUsers(user: Users) {
        usersDao.deleteUsers(user)
    }

    fun getUsers() = usersDao.getUsers()

    fun getUserRef(id: String) = usersDao.getUserRef(id)

    fun getUsersById(id: String) = usersDao.getUsersById(id)

    fun checkEmail(email: String) = usersDao.checkEmail(email)

    fun checkUsername(username: String) = usersDao.checkUsername(username)

    fun checkPhone(phone: String) = usersDao.checkPhone(phone)

    fun getFavoriteBooks(id: String) = usersDao.getFavoriteBooks(id)
}