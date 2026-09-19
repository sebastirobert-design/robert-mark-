package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.AttendanceDao
import com.example.data.dao.MarksDao
import com.example.data.dao.SchoolProfileDao
import com.example.data.dao.StudentDao
import com.example.data.model.SchoolProfile
import com.example.data.model.Student
import com.example.data.model.StudentMarks
import com.example.data.model.TermAttendance

@Database(
    entities = [
        SchoolProfile::class,
        Student::class,
        StudentMarks::class,
        TermAttendance::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun schoolProfileDao(): SchoolProfileDao
    abstract fun studentDao(): StudentDao
    abstract fun marksDao(): MarksDao
    abstract fun attendanceDao(): AttendanceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "school_marks_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
