package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Student
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Query("SELECT * FROM students ORDER BY stdClass ASC, id ASC")
    fun getAllStudents(): Flow<List<Student>>

    @Query("SELECT * FROM students ORDER BY id ASC")
    suspend fun getAllStudentsOnce(): List<Student>

    @Query("SELECT * FROM students WHERE stdClass = :stdClass ORDER BY id ASC")
    fun getStudentsByClass(stdClass: Int): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE id = :id")
    suspend fun getStudentById(id: Long): Student?

    @Query("SELECT * FROM students WHERE admissionNo = :admissionNo LIMIT 1")
    suspend fun getStudentByAdmissionNo(admissionNo: String): Student?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudents(students: List<Student>): List<Long>

    @Update
    suspend fun updateStudent(student: Student)

    @Delete
    suspend fun deleteStudent(student: Student)

    @Query("DELETE FROM students WHERE id = :id")
    suspend fun deleteStudentById(id: Long)

    @Query("DELETE FROM students")
    suspend fun deleteAllStudents()

    @Query("SELECT COUNT(*) FROM students")
    fun getStudentCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM students WHERE stdClass = :stdClass")
    fun getStudentCountByClass(stdClass: Int): Flow<Int>

    @Query("UPDATE students SET stdClass = :newClass, academicYear = :newYear WHERE id = :id")
    suspend fun updateStudentClassAndYear(id: Long, newClass: Int, newYear: String)

    @Query("UPDATE students SET stdClass = :toClass, academicYear = :newYear WHERE stdClass = :fromClass")
    suspend fun promoteStudentsInClass(fromClass: Int, toClass: Int, newYear: String)

    @Query("UPDATE students SET stdClass = stdClass + 1, academicYear = :newYear WHERE stdClass >= 1 AND stdClass <= 7")
    suspend fun promoteAllClasses1To7(newYear: String)

    @Query("UPDATE students SET stdClass = 9, academicYear = :newYear WHERE stdClass = 8")
    suspend fun graduateClass8(newYear: String)

    @Query("DELETE FROM students WHERE stdClass = 8")
    suspend fun removeClass8Students()
}
