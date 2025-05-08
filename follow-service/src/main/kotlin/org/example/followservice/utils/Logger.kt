package org.example.followservice.utils

class Logger {
    companion object {
        fun log(message: String) {
            println("✅ $message")
        }

        fun logError(message: String) {
            System.err.println("🛑 ERROR: $message")
        }
    }
}