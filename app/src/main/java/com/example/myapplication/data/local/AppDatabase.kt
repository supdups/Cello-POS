package com.example.myapplication.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.myapplication.util.PasswordHasher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Order::class, User::class, Product::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun orderDao(): OrderDao
    abstract fun userDao(): UserDao
    abstract fun productDao(): ProductDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cello_pos.db"
                )
                    // TODO: replace with a real Migration before this ships anywhere real —
                    // destructive migration just wipes local data on schema changes, which is
                    // fine while we're actively developing but not once there's real order history.
                    .fallbackToDestructiveMigration(true)
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            seedDatabase(context)
                        }

                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            seedDatabase(context)
                        }
                    })
                    .build()
                    .also { INSTANCE = it }
            }
        }

        private fun seedDatabase(context: Context) {
            CoroutineScope(Dispatchers.IO).launch {
                val instance = getInstance(context)
                val userDao = instance.userDao()
                if (userDao.countUsers() == 0) {
                    val salt = PasswordHasher.generateSalt()
                    val hash = PasswordHasher.hash("admin1234", salt)
                    userDao.insert(
                        User(
                            username = "admin",
                            passwordHash = hash,
                            salt = salt,
                            role = "admin"
                        )
                    )
                }

                val productDao = instance.productDao()
                if (productDao.countProducts() == 0) {
                    listOf(
                        Product(name = "Espresso", price = 3.00),
                        Product(name = "Latte", price = 4.50),
                        Product(name = "Cappuccino", price = 4.50),
                        Product(name = "Americano", price = 3.50),
                        Product(name = "Croissant", price = 3.25),
                        Product(name = "Muffin", price = 3.00),
                    ).forEach { productDao.insert(it) }
                }
            }
        }
    }
}
