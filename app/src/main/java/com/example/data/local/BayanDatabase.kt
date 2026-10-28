package com.example.data.local

/**
 * Typealias preserving full backward compatibility for BayanDatabase references
 * while exposing AppDatabase as the primary RoomDatabase class.
 */
typealias BayanDatabase = AppDatabase
