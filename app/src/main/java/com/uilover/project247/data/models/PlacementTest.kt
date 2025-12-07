package com.uilover.project247.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PlacementTest(
    val id: String = "",
    val title: String = "",
    val titleEn: String = "",
    val description: String = "",
    val descriptionEn: String = "",
    val duration: Int = 0, // seconds
    val totalQuestions: Int = 0,
    val passingScores: PassingScores = PassingScores(),
    val instructions: List<String> = emptyList(),
    val questions: List<PlacementQuestion> = emptyList()
) : Parcelable

@Parcelize
data class PassingScores(
    val beginner: Int = 0,
    val elementary: Int = 0,
    val intermediate: Int = 0,
    val advanced: Int = 0
) : Parcelable

@Parcelize
data class PlacementQuestion(
    val id: String = "",
    val order: Int = 0,
    val level: String = "",
    val type: String = "",
    val question: String = "",
    val questionVi: String = "",
    val options: List<String> = emptyList(),
    val correctAnswer: Int = 0
) : Parcelable

data class PlacementTestResult(
    val totalQuestions: Int,
    val correctAnswers: Int,
    val wrongAnswers: Int,
    val score: Int, // percentage
    val recommendedLevel: String,
    val recommendedLevelVi: String,
    val completedDate: Long
)
