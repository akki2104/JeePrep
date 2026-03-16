package com.jeeprep.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey val id: Int,
    val name: String,     // Physics, Chemistry, Mathematics
    val iconName: String  // material icon name
)
