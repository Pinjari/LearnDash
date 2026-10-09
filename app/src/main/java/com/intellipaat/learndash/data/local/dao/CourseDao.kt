package com.intellipaat.learndash.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.intellipaat.learndash.data.local.entity.CourseEntity
import kotlinx.coroutines.flow.Flow

/** Joins lessons at read time so progress is always consistent. */
@Dao
interface CourseDao {

    data class CourseWithCounts(
        val id: Int,
        val title: String,
        val instructor: String,
        val totalLessons: Int,
        val completedLessons: Int
    )

    @Query(
        """SELECT c.id AS id, c.title AS title, c.instructor AS instructor,
                  c.totalLessons AS totalLessons,
                  (SELECT COUNT(*) FROM lessons l
                    WHERE l.courseId = c.id AND l.completed = 1) AS completedLessons
           FROM courses c ORDER BY c.id ASC"""
    )
    fun observeCourses(): Flow<List<CourseWithCounts>>

    @Query(
        """SELECT c.id AS id, c.title AS title, c.instructor AS instructor,
                  c.totalLessons AS totalLessons,
                  (SELECT COUNT(*) FROM lessons l
                    WHERE l.courseId = c.id AND l.completed = 1) AS completedLessons
           FROM courses c WHERE c.id = :id"""
    )
    suspend fun courseById(id: Int): CourseWithCounts?

    @Query("SELECT COUNT(*) FROM courses")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(courses: List<CourseEntity>)
}
