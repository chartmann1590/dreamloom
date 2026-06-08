package com.charles.app.dreamloom.data.feedback

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private val Context.feedbackDataStore: DataStore<Preferences> by preferencesDataStore(name = "feedback_bug_reports")

@Singleton
class BugReportRepo @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val ds = context.feedbackDataStore

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    val bugReports: Flow<List<BugReport>> = ds.data.map { prefs ->
        val rawJson = prefs[KEY_BUG_REPORTS_LIST] ?: "[]"
        try {
            json.decodeFromString<List<BugReport>>(rawJson)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveBugReport(report: BugReport) {
        val current = getBugReportsList()
        val index = current.indexOfFirst { it.number == report.number }
        val updated = current.toMutableList()
        if (index != -1) {
            updated[index] = report
        } else {
            updated.add(report)
        }
        // Sort newest first where practical (issue number represents order of creation)
        updated.sortByDescending { it.number }
        updateBugReports(updated)
    }

    suspend fun updateBugReports(reports: List<BugReport>) {
        val rawJson = json.encodeToString(reports)
        ds.edit { prefs ->
            prefs[KEY_BUG_REPORTS_LIST] = rawJson
        }
    }

    suspend fun getBugReportsList(): List<BugReport> {
        return try {
            bugReports.first()
        } catch (e: Exception) {
            emptyList()
        }
    }

    companion object {
        private val KEY_BUG_REPORTS_LIST = stringPreferencesKey("bug_reports_list")
    }
}
