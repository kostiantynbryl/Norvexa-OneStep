package com.norvexa.onestep.data

import com.norvexa.onestep.model.AppData
import com.norvexa.onestep.model.AppSettings
import com.norvexa.onestep.model.Goal
import com.norvexa.onestep.model.GoalCategory
import com.norvexa.onestep.model.GoalStatus
import com.norvexa.onestep.model.Stage
import com.norvexa.onestep.model.Step
import com.norvexa.onestep.model.StepStatus
import com.norvexa.onestep.model.ThemeMode
import org.json.JSONArray
import org.json.JSONObject

object AppDataCodec {
    fun encode(data: AppData): String = JSONObject().apply {
        put("schemaVersion", 1)
        put("onboardingComplete", data.onboardingComplete)
        put("goals", JSONArray().apply { data.goals.forEach { put(goalToJson(it)) } })
        put("settings", settingsToJson(data.settings))
        put("activityDates", JSONArray().apply { data.activityDates.sorted().forEach(::put) })
    }.toString()

    fun decode(raw: String?): AppData {
        if (raw.isNullOrBlank()) return AppData()
        return runCatching {
            val root = JSONObject(raw)
            AppData(
                onboardingComplete = root.optBoolean("onboardingComplete", false),
                goals = root.optJSONArray("goals").toList(::goalFromJson),
                settings = root.optJSONObject("settings")?.let(::settingsFromJson) ?: AppSettings(),
                activityDates = root.optJSONArray("activityDates").toLongSet(),
            )
        }.getOrDefault(AppData())
    }

    private fun goalToJson(goal: Goal) = JSONObject().apply {
        put("id", goal.id)
        put("title", goal.title)
        put("description", goal.description)
        put("category", goal.category.name)
        put("status", goal.status.name)
        put("createdAt", goal.createdAt)
        putNullable("deadline", goal.deadline)
        putNullable("completedAt", goal.completedAt)
        put("availableMinutesPerDay", goal.availableMinutesPerDay)
        put("stages", JSONArray().apply { goal.stages.forEach { put(stageToJson(it)) } })
    }

    private fun goalFromJson(json: JSONObject) = Goal(
        id = json.optString("id"),
        title = json.optString("title"),
        description = json.optString("description"),
        category = enumValue(json.optString("category"), GoalCategory.OTHER),
        status = enumValue(json.optString("status"), GoalStatus.DRAFT),
        createdAt = json.optLong("createdAt", System.currentTimeMillis()),
        deadline = json.optNullableLong("deadline"),
        completedAt = json.optNullableLong("completedAt"),
        availableMinutesPerDay = json.optInt("availableMinutesPerDay", 25),
        stages = json.optJSONArray("stages").toList(::stageFromJson),
    )

    private fun stageToJson(stage: Stage) = JSONObject().apply {
        put("id", stage.id)
        put("title", stage.title)
        put("steps", JSONArray().apply { stage.steps.forEach { put(stepToJson(it)) } })
    }

    private fun stageFromJson(json: JSONObject) = Stage(
        id = json.optString("id"),
        title = json.optString("title"),
        steps = json.optJSONArray("steps").toList(::stepFromJson),
    )

    private fun stepToJson(step: Step) = JSONObject().apply {
        put("id", step.id)
        put("title", step.title)
        put("description", step.description)
        put("status", step.status.name)
        put("estimatedMinutes", step.estimatedMinutes)
        put("actualMinutes", step.actualMinutes)
        putNullable("scheduledAt", step.scheduledAt)
        putNullable("completedAt", step.completedAt)
        put("postponeCount", step.postponeCount)
        putNullable("parentStepId", step.parentStepId)
    }

    private fun stepFromJson(json: JSONObject) = Step(
        id = json.optString("id"),
        title = json.optString("title"),
        description = json.optString("description"),
        status = enumValue(json.optString("status"), StepStatus.TODO),
        estimatedMinutes = json.optInt("estimatedMinutes", 15),
        actualMinutes = json.optInt("actualMinutes", 0),
        scheduledAt = json.optNullableLong("scheduledAt"),
        completedAt = json.optNullableLong("completedAt"),
        postponeCount = json.optInt("postponeCount", 0),
        parentStepId = json.optNullableString("parentStepId"),
    )

    private fun settingsToJson(settings: AppSettings) = JSONObject().apply {
        put("themeMode", settings.themeMode.name)
        put("languageTag", settings.languageTag)
        put("remindersEnabled", settings.remindersEnabled)
        put("reminderHour", settings.reminderHour)
        put("reminderMinute", settings.reminderMinute)
    }

    private fun settingsFromJson(json: JSONObject) = AppSettings(
        themeMode = enumValue(json.optString("themeMode"), ThemeMode.SYSTEM),
        languageTag = json.optString("languageTag"),
        remindersEnabled = json.optBoolean("remindersEnabled", false),
        reminderHour = json.optInt("reminderHour", 9),
        reminderMinute = json.optInt("reminderMinute", 0),
    )

    private inline fun <reified T : Enum<T>> enumValue(raw: String, fallback: T): T =
        enumValues<T>().firstOrNull { it.name == raw } ?: fallback

    private fun JSONObject.putNullable(key: String, value: Any?) {
        put(key, value ?: JSONObject.NULL)
    }

    private fun JSONObject.optNullableLong(key: String): Long? =
        if (!has(key) || isNull(key)) null else optLong(key)

    private fun JSONObject.optNullableString(key: String): String? =
        if (!has(key) || isNull(key)) null else optString(key)

    private fun <T> JSONArray?.toList(transform: (JSONObject) -> T): List<T> {
        if (this == null) return emptyList()
        return buildList {
            for (index in 0 until length()) {
                optJSONObject(index)?.let { add(transform(it)) }
            }
        }
    }

    private fun JSONArray?.toLongSet(): Set<Long> {
        if (this == null) return emptySet()
        return buildSet {
            for (index in 0 until length()) add(optLong(index))
        }
    }
}
