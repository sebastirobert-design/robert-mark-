package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val NavyPrimary = Color(0xFF1A237E)
val AcademicBlue = Color(0xFF1976D2)
val LightBlueBg = Color(0xFFF0F4F8)
val AmberGold = Color(0xFFD97706)
val AmberGoldLight = Color(0xFFFEF3C7)
val EmeraldPass = Color(0xFF059669)
val EmeraldLight = Color(0xFFD1FAE5)
val CrimsonFail = Color(0xFFDC2626)
val CrimsonLight = Color(0xFFFEE2E2)

val Slate900 = Color(0xFF0F172A)
val Slate800 = Color(0xFF1E293B)
val Slate700 = Color(0xFF334155)
val Slate500 = Color(0xFF64748B)
val Slate100 = Color(0xFFF1F5F9)
val Slate50 = Color(0xFFF8FAFC)

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF1A237E)
val PurpleGrey40 = Color(0xFF374151)
val Pink40 = Color(0xFFB45309)

/**
 * Adaptive text and accent color that ensures high contrast in both Light and Dark themes.
 * In Light theme: deep dark navy (#0D1658)
 * In Dark theme: crisp bright slate (#F1F5F9)
 */
val NavyDark: Color
    @Composable
    get() = if (isSystemInDarkTheme()) Color(0xFFF1F5F9) else Color(0xFF0D1658)

/**
 * Adaptive NavyPrimary color for text and icons:
 * In Light theme: deep blue (#1A237E)
 * In Dark theme: clear accessible sky blue (#93C5FD)
 */
val TextNavy: Color
    @Composable
    get() = if (isSystemInDarkTheme()) Color(0xFF93C5FD) else Color(0xFF1A237E)

/**
 * Adaptive secondary text color:
 * In Light theme: Slate 500 (#64748B)
 * In Dark theme: Slate 400 (#94A3B8)
 */
val TextMuted: Color
    @Composable
    get() = if (isSystemInDarkTheme()) Color(0xFF94A3B8) else Color(0xFF64748B)

/**
 * Adaptive primary body text color:
 * In Light theme: Slate 900 (#0F172A)
 * In Dark theme: Slate 50 (#F8FAFC)
 */
val TextDark: Color
    @Composable
    get() = if (isSystemInDarkTheme()) Color(0xFFF8FAFC) else Color(0xFF0F172A)

/**
 * Adaptive neutral gray color for secondary details:
 * In Light theme: Slate 600 (#475569)
 * In Dark theme: Slate 300 (#CBD5E1)
 */
val TextGray: Color
    @Composable
    get() = if (isSystemInDarkTheme()) Color(0xFFCBD5E1) else Color(0xFF475569)

/**
 * Adaptive card background:
 * In Light theme: pure white (#FFFFFF)
 * In Dark theme: elegant dark slate (#1E293B)
 */
val CardBg: Color
    @Composable
    get() = if (isSystemInDarkTheme()) Color(0xFF1E293B) else Color.White

/**
 * Adaptive subtle surface background (e.g. inner sections):
 * In Light theme: Slate 50 (#F8FAFC)
 * In Dark theme: Slate 850 (#243144)
 */
val CardBgSubtle: Color
    @Composable
    get() = if (isSystemInDarkTheme()) Color(0xFF243144) else Color(0xFFF8FAFC)

/**
 * Adaptive subtle border:
 * In Light theme: Slate 200 (#E2E8F0)
 * In Dark theme: Slate 700 (#334155)
 */
val BorderSubtle: Color
    @Composable
    get() = if (isSystemInDarkTheme()) Color(0xFF334155) else Color(0xFFE2E8F0)

/**
 * Adaptive rose/red card background for Naney 1 & Naney 2
 */
val RoseCardBg: Color
    @Composable
    get() = if (isSystemInDarkTheme()) Color(0xFF2A151C) else Color(0xFFFFF1F2)

val RoseCardBorder: Color
    @Composable
    get() = if (isSystemInDarkTheme()) Color(0xFF4C1D24) else Color(0xFFFECDD3)

val RoseCardText: Color
    @Composable
    get() = if (isSystemInDarkTheme()) Color(0xFFFCA5A5) else Color(0xFFB91C1C)

/**
 * Adaptive yellow/amber card background for Thiranari
 */
val AmberCardBg: Color
    @Composable
    get() = if (isSystemInDarkTheme()) Color(0xFF2B2310) else Color(0xFFFEFCE8)

val AmberCardBorder: Color
    @Composable
    get() = if (isSystemInDarkTheme()) Color(0xFF533F17) else Color(0xFFFEF08A)

val AmberCardText: Color
    @Composable
    get() = if (isSystemInDarkTheme()) Color(0xFFFDE047) else Color(0xFF854D0E)

/**
 * Adaptive PE card background
 */
val PeCardBg: Color
    @Composable
    get() = if (isSystemInDarkTheme()) Color(0xFF261E14) else Color(0xFFFFFBEB)

val PeCardBorder: Color
    @Composable
    get() = if (isSystemInDarkTheme()) Color(0xFF4B3A22) else Color(0xFFFDE68A)

val PeCardText: Color
    @Composable
    get() = if (isSystemInDarkTheme()) Color(0xFFFCD34D) else Color(0xFF92400E)


