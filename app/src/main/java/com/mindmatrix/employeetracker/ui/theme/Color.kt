package com.mindmatrix.employeetracker.ui.theme

import androidx.compose.ui.graphics.Color

// Primary palette - Indigo (#3949AB per PRD spec)
val Primary = Color(0xFF3949AB)
val PrimaryLight = Color(0xFF6F74DD)
val PrimaryDark = Color(0xFF00227B)
val PrimaryContainer = Color(0xFFE8EAF6)
val OnPrimaryContainer = Color(0xFF00227B)

// Accent palette - Green (#43A047 per PRD spec)
val Accent = Color(0xFF43A047)
val AccentLight = Color(0xFF76D275)
val AccentDark = Color(0xFF00701A)
val AccentContainer = Color(0xFFE8F5E9)
val OnAccentContainer = Color(0xFF00701A)

// Secondary palette - Amber (for highlights)
val Secondary = Color(0xFFFFC107)
val SecondaryLight = Color(0xFFFFD54F)
val SecondaryDark = Color(0xFFFFA000)
val SecondaryContainer = Color(0xFFFFF8E1)
val OnSecondaryContainer = Color(0xFF2E1C00)

// Tertiary palette - Teal (for balance)
val Tertiary = Color(0xFF009688)
val TertiaryContainer = Color(0xFFE0F2F1)

// Error / Alert palette (#F44336 per PRD spec)
val Error = Color(0xFFF44336)
val ErrorContainer = Color(0xFFFFEBEE)
val OnError = Color.White

// Success palette
val Success = Color(0xFF43A047)      // reuses Accent green for consistency
val SuccessContainer = Color(0xFFE8F5E9)

// Neutral palette (#FAFAFA background per PRD spec)
val Background = Color(0xFFFAFAFA)
val Surface = Color(0xFFFFFFFF)
val SurfaceVariant = Color(0xFFF1F3F4)
val SurfaceVariantDark = Color(0xFFEEEEEE)
val OnBackground = Color(0xFF202124)
val OnSurface = Color(0xFF202124)
val OnSurfaceVariant = Color(0xFF5F6368)
val Outline = Color(0xFFDADCE0)

// Podium colors
val Gold = Color(0xFFFFD700)
val Silver = Color(0xFFBDBDBD)
val Bronze = Color(0xFFCD7F32)

// Warning / Highlight
val Warning = Color(0xFFEF6C00)
val WarningContainer = Color(0xFFFFF3E0)

// Dark theme colors - High Contrast
val DarkBackground = Color(0xFF000000)
val DarkSurface = Color(0xFF121212)
val DarkSurfaceVariant = Color(0xFF1E1E1E)
val DarkOnBackground = Color(0xFFFFFFFF)
val DarkOnSurface = Color(0xFFFFFFFF)
val DarkOnSurfaceVariant = Color(0xFFE0E0E0)
val DarkOutline = Color(0xFFBBBBBB)

// Status colors
val StatusPresent = Color(0xFF43A047)   // uses Accent green
val StatusAbsent = Color(0xFFF44336)    // uses Error red
val StatusLate = Color(0xFFFFAB00)
val StatusOnLeave = Color(0xFFF50057)

// Priority colors
val PriorityLow = Color(0xFF66BB6A)
val PriorityMedium = Color(0xFFFFC400)
val PriorityHigh = Color(0xFFFF9100)
val PriorityCritical = Color(0xFFF44336)  // uses Error red
