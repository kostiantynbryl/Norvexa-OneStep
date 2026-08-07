package com.norvexa.onestep.model

object GoalTemplates {
    fun stagesFor(category: GoalCategory, title: String, languageTag: String): List<Stage> {
        val language = languageTag.substringBefore('-').lowercase()
        val copy = copyFor(language)
        val planning = copy.planning[category] ?: copy.planning.getValue(GoalCategory.OTHER)
        val execution = copy.execution[category] ?: copy.execution.getValue(GoalCategory.OTHER)
        return listOf(
            Stage(
                title = "${copy.preparation}: ${title.trim()}",
                steps = planning.mapIndexed { index, text ->
                    Step(title = text, estimatedMinutes = if (index == 0) 10 else 15)
                },
            ),
            Stage(
                title = copy.executionTitle,
                steps = execution.map { Step(title = it, estimatedMinutes = 25) },
            ),
            Stage(
                title = copy.completion,
                steps = copy.completionSteps.mapIndexed { index, text ->
                    Step(title = text, estimatedMinutes = if (index == 0) 15 else 10)
                },
            ),
        )
    }

    fun splitTitles(sourceTitle: String, languageTag: String): List<String> = when (languageTag.substringBefore('-').lowercase()) {
        "uk" -> listOf(
            "Підготувати все необхідне для: $sourceTitle",
            "Виконати першу частину: $sourceTitle",
            "Перевірити й завершити: $sourceTitle",
        )
        "pl" -> listOf(
            "Przygotuj wszystko do: $sourceTitle",
            "Wykonaj pierwszą część: $sourceTitle",
            "Sprawdź i zakończ: $sourceTitle",
        )
        "en" -> listOf(
            "Prepare everything needed for: $sourceTitle",
            "Complete the first part of: $sourceTitle",
            "Review and finish: $sourceTitle",
        )
        else -> listOf(
            "Подготовить всё необходимое для: $sourceTitle",
            "Выполнить первую часть: $sourceTitle",
            "Проверить и завершить: $sourceTitle",
        )
    }

    private data class TemplateCopy(
        val preparation: String,
        val executionTitle: String,
        val completion: String,
        val planning: Map<GoalCategory, List<String>>,
        val execution: Map<GoalCategory, List<String>>,
        val completionSteps: List<String>,
    )

    private fun copyFor(language: String): TemplateCopy = when (language) {
        "uk" -> ukrainian()
        "pl" -> polish()
        "en" -> english()
        else -> russian()
    }

    private fun russian() = TemplateCopy(
        preparation = "Подготовка",
        executionTitle = "Выполнение",
        completion = "Завершение",
        planning = mapOf(
            GoalCategory.DEVELOPMENT to listOf("Сформулировать проблему и результат", "Описать основные функции", "Подготовить структуру проекта"),
            GoalCategory.LEARNING to listOf("Определить текущий уровень", "Выбрать учебные материалы", "Составить расписание занятий"),
            GoalCategory.HEALTH to listOf("Зафиксировать исходную точку", "Выбрать безопасное минимальное действие", "Подготовить расписание и напоминания"),
            GoalCategory.BUSINESS to listOf("Описать клиента и его проблему", "Проверить спрос минимальным тестом", "Сформировать первое предложение"),
            GoalCategory.FINANCE to listOf("Собрать исходные цифры", "Определить целевую сумму и срок", "Назначить регулярное действие"),
            GoalCategory.HOME to listOf("Определить одну рабочую зону", "Подготовить необходимые материалы", "Выполнить первый участок"),
            GoalCategory.CREATIVE to listOf("Собрать референсы", "Сделать быстрый черновик", "Выбрать направление для доработки"),
            GoalCategory.OTHER to listOf("Уточнить ожидаемый результат", "Определить ограничения и ресурсы", "Выбрать первый небольшой шаг"),
        ),
        execution = mapOf(
            GoalCategory.DEVELOPMENT to listOf("Создать минимальную рабочую версию", "Проверить основной сценарий", "Исправить критические ошибки"),
            GoalCategory.LEARNING to listOf("Пройти первое короткое занятие", "Закрепить материал практикой", "Проверить результат мини-тестом"),
            GoalCategory.OTHER to listOf("Выполнить первое действие по цели", "Проверить полученный результат", "Скорректировать следующий шаг"),
        ),
        completionSteps = listOf("Оценить итог относительно цели", "Зафиксировать результат и следующий уровень"),
    )

    private fun ukrainian() = TemplateCopy(
        preparation = "Підготовка",
        executionTitle = "Виконання",
        completion = "Завершення",
        planning = mapOf(
            GoalCategory.DEVELOPMENT to listOf("Сформулювати проблему й результат", "Описати основні функції", "Підготувати структуру проєкту"),
            GoalCategory.LEARNING to listOf("Визначити поточний рівень", "Обрати навчальні матеріали", "Скласти розклад занять"),
            GoalCategory.HEALTH to listOf("Зафіксувати початкову точку", "Обрати безпечну мінімальну дію", "Підготувати розклад і нагадування"),
            GoalCategory.BUSINESS to listOf("Описати клієнта та його проблему", "Перевірити попит мінімальним тестом", "Сформувати першу пропозицію"),
            GoalCategory.FINANCE to listOf("Зібрати вихідні цифри", "Визначити цільову суму й термін", "Призначити регулярну дію"),
            GoalCategory.HOME to listOf("Визначити одну робочу зону", "Підготувати необхідні матеріали", "Виконати першу ділянку"),
            GoalCategory.CREATIVE to listOf("Зібрати референси", "Зробити швидку чернетку", "Обрати напрям для доопрацювання"),
            GoalCategory.OTHER to listOf("Уточнити очікуваний результат", "Визначити обмеження й ресурси", "Обрати перший невеликий крок"),
        ),
        execution = mapOf(
            GoalCategory.DEVELOPMENT to listOf("Створити мінімальну робочу версію", "Перевірити основний сценарій", "Виправити критичні помилки"),
            GoalCategory.LEARNING to listOf("Пройти перше коротке заняття", "Закріпити матеріал практикою", "Перевірити результат мінітестом"),
            GoalCategory.OTHER to listOf("Виконати першу дію за ціллю", "Перевірити отриманий результат", "Скоригувати наступний крок"),
        ),
        completionSteps = listOf("Оцінити підсумок відносно цілі", "Зафіксувати результат і наступний рівень"),
    )

    private fun english() = TemplateCopy(
        preparation = "Preparation",
        executionTitle = "Execution",
        completion = "Completion",
        planning = mapOf(
            GoalCategory.DEVELOPMENT to listOf("Define the problem and desired outcome", "Describe the core features", "Prepare the project structure"),
            GoalCategory.LEARNING to listOf("Assess the current level", "Choose learning materials", "Create a study schedule"),
            GoalCategory.HEALTH to listOf("Record the starting point", "Choose a safe minimum action", "Set up a schedule and reminders"),
            GoalCategory.BUSINESS to listOf("Describe the customer and problem", "Validate demand with a small test", "Create the first offer"),
            GoalCategory.FINANCE to listOf("Collect the starting numbers", "Set the target amount and deadline", "Schedule a recurring action"),
            GoalCategory.HOME to listOf("Choose one work area", "Prepare the required materials", "Complete the first section"),
            GoalCategory.CREATIVE to listOf("Collect references", "Make a quick draft", "Choose a direction to refine"),
            GoalCategory.OTHER to listOf("Clarify the expected outcome", "Identify constraints and resources", "Choose the first small step"),
        ),
        execution = mapOf(
            GoalCategory.DEVELOPMENT to listOf("Create a minimum working version", "Test the primary flow", "Fix critical issues"),
            GoalCategory.LEARNING to listOf("Complete the first short lesson", "Reinforce it with practice", "Check progress with a mini-test"),
            GoalCategory.OTHER to listOf("Complete the first action", "Review the result", "Adjust the next step"),
        ),
        completionSteps = listOf("Evaluate the result against the goal", "Record the outcome and next level"),
    )

    private fun polish() = TemplateCopy(
        preparation = "Przygotowanie",
        executionTitle = "Realizacja",
        completion = "Zakończenie",
        planning = mapOf(
            GoalCategory.DEVELOPMENT to listOf("Zdefiniuj problem i rezultat", "Opisz główne funkcje", "Przygotuj strukturę projektu"),
            GoalCategory.LEARNING to listOf("Określ obecny poziom", "Wybierz materiały do nauki", "Ułóż harmonogram nauki"),
            GoalCategory.HEALTH to listOf("Zapisz punkt początkowy", "Wybierz bezpieczne minimalne działanie", "Ustaw harmonogram i przypomnienia"),
            GoalCategory.BUSINESS to listOf("Opisz klienta i jego problem", "Sprawdź popyt małym testem", "Przygotuj pierwszą ofertę"),
            GoalCategory.FINANCE to listOf("Zbierz dane początkowe", "Określ kwotę docelową i termin", "Zaplanuj regularne działanie"),
            GoalCategory.HOME to listOf("Wybierz jeden obszar pracy", "Przygotuj potrzebne materiały", "Wykonaj pierwszy fragment"),
            GoalCategory.CREATIVE to listOf("Zbierz inspiracje", "Przygotuj szybki szkic", "Wybierz kierunek do rozwinięcia"),
            GoalCategory.OTHER to listOf("Doprecyzuj oczekiwany rezultat", "Określ ograniczenia i zasoby", "Wybierz pierwszy mały krok"),
        ),
        execution = mapOf(
            GoalCategory.DEVELOPMENT to listOf("Utwórz minimalną działającą wersję", "Sprawdź główny scenariusz", "Napraw krytyczne błędy"),
            GoalCategory.LEARNING to listOf("Ukończ pierwszą krótką lekcję", "Utrwal materiał w praktyce", "Sprawdź wynik krótkim testem"),
            GoalCategory.OTHER to listOf("Wykonaj pierwsze działanie", "Sprawdź rezultat", "Dostosuj następny krok"),
        ),
        completionSteps = listOf("Oceń rezultat względem celu", "Zapisz wynik i kolejny poziom"),
    )
}
