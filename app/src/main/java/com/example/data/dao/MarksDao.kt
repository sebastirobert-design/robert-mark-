package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.StudentMarks
import kotlinx.coroutines.flow.Flow

@Dao
interface MarksDao {
    @Query("SELECT * FROM student_marks WHERE studentId = :studentId")
    fun getMarksForStudent(studentId: Long): Flow<List<StudentMarks>>

    @Query("SELECT * FROM student_marks WHERE studentId = :studentId AND term = :term")
    fun getMarksForStudentAndTerm(studentId: Long, term: Int): Flow<List<StudentMarks>>

    @Query("SELECT * FROM student_marks WHERE studentId IN (:studentIds)")
    fun getMarksForStudents(studentIds: List<Long>): Flow<List<StudentMarks>>

    @Query("SELECT * FROM student_marks WHERE studentId IN (:studentIds) AND term = :term")
    fun getMarksForStudentsAndTerm(studentIds: List<Long>, term: Int): Flow<List<StudentMarks>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateMark(marks: StudentMarks): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateMarks(marksList: List<StudentMarks>)

    @Query("DELETE FROM student_marks WHERE studentId = :studentId")
    suspend fun deleteMarksForStudent(studentId: Long)

    @Query("DELETE FROM student_marks")
    suspend fun deleteAllMarks()
}
