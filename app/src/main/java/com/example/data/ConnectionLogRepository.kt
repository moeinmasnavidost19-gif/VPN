package com.example.data

import kotlinx.coroutines.flow.Flow

class ConnectionLogRepository(private val connectionLogDao: ConnectionLogDao) {
    val allLogs: Flow<List<ConnectionLog>> = connectionLogDao.getAllLogs()

    suspend fun insertLog(log: ConnectionLog) {
        connectionLogDao.insertLog(log)
    }

    suspend fun clearLogs() {
        connectionLogDao.clearAllLogs()
    }
}
