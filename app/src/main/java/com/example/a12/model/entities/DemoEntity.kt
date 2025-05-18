package com.example.a12.model.entities

import androidx.room.*

@Entity(tableName = "demo")
data class DemoEntity(
    @PrimaryKey @ColumnInfo("ID")  val id: Int,
    @ColumnInfo("Name")            val name: String,
    @ColumnInfo("Hint")            val hint: String?
)