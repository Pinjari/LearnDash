package com.intellipaat.learndash.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.intellipaat.learndash.data.local.entity.LessonEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LessonDao {

    @Query("SELECT * FROM lessons WHERE courseId = :courseId ORDER BY lessonIndex ASC")
    fun observeForCourse(courseId: Int): Flow<List<LessonEntity>>

    @Query("SELECT COUNT(*) FROM lessons WHERE courseId = :courseId")
    suspend fun countForCourse(courseId: Int): Int

    @Query(
        "UPDATE lessons SET completed = :completed " +
            "WHERE courseId = :courseId AND lessonIndex = :index"
    )
    suspend fun setCompleted(courseId: Int, index: Int, completed: Boolean)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(lessons: List<LessonEntity>)
}
