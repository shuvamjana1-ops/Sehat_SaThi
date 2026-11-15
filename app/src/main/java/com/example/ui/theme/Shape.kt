package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val SehatShapes =
  Shapes(
    small = RoundedCornerShape(12.dp), // Buttons & inputs
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp), // Cards & bento tiles
    extraLarge = RoundedCornerShape(28.dp), // Bottom sheets & dialogs
  )

val PillShape = RoundedCornerShape(999.dp)
val CardShape = RoundedCornerShape(20.dp)
val ButtonShape = RoundedCornerShape(12.dp)
