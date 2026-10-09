package com.intellipaat.learndash.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.intellipaat.learndash.data.local.dao.CourseDao
import com.intellipaat.learndash.data.local.dao.LessonDao
import com.intellipaat.learndash.data.local.entity.CourseEntity
import com.intellipaat.learndash.data.local.entity.LessonEntity

@Database(entities = [CourseEntity::class, LessonEntity::class], version = 1, exportSchema = false)
abstract class LearnDatabase : RoomDatabase() {
    abstract fun courses(): CourseDao
    abstract fun lessons(): LessonDao
}
