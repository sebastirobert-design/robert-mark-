package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.TermAttendance
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM term_attendance WHERE studentId = :studentId")
    fun getAttendanceForStudent(studentId: Long): Flow<List<TermAttendance>>

    @Query("SELECT * FROM term_attendance")
    suspend fun getAllAttendanceOnce(): List<TermAttendance>

    @Query("SELECT * FROM term_attendance WHERE studentId = :studentId AND term = :term LIMIT 1")
    fun getAttendanceForStudentAndTerm(studentId: Long, term: Int): Flow<TermAttendance?>

    @Query("SELECT * FROM term_attendance WHERE studentId IN (:studentIds)")
    fun getAttendanceForStudents(studentIds: List<Long>): Flow<List<TermAttendance>>

    @Query("SELECT * FROM term_attendance WHERE studentId IN (:studentIds) AND term = :term")
    fun getAttendanceForStudentsAndTerm(studentIds: List<Long>, term: Int): Flow<List<TermAttendance>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAttendance(attendance: TermAttendance): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAttendanceList(list: List<TermAttendance>)

    @Query("DELETE FROM term_attendance WHERE studentId = :studentId")
    suspend fun deleteAttendanceForStudent(studentId: Long)

    @Query("DELETE FROM term_attendance")
    suspend fun deleteAllAttendance()
}
