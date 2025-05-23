package com.example.a12.model.DAO

import androidx.room.*
import com.example.a12.model.entities.*

@Dao
interface TestDao {

    @Query("SELECT * FROM tests WHERE test_id = :testId")
    suspend fun getTestById(testId: Long): TestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTest(test: TestEntity): Long

    @Query("DELETE FROM tests WHERE test_id = :testId")
    suspend fun deleteTest(testId: Long)

    @Query("SELECT * FROM questions WHERE test_id = :testId ORDER BY order_number ASC")
    suspend fun getQuestions(testId: Long): List<QuestionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: QuestionEntity): Long

    @Query("DELETE FROM questions WHERE test_id = :testId")
    suspend fun deleteQuestionsForTest(testId: Long)

    @Query("SELECT test_name FROM tests WHERE test_id = :testId")
    suspend fun getTestName(testId: Long): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnswer(answer: AnswerEntity): Long

    @Query("DELETE FROM answers WHERE question_id = :questionId")
    suspend fun deleteAnswersForQuestion(questionId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTestResult(result: TestResultEntity): Long

    @Update
    suspend fun updateTestResult(result: TestResultEntity)

    @Query("DELETE FROM test_results WHERE test_id = :testId")
    suspend fun deleteTestResults(testId: Long)

    @Query("SELECT * FROM user_answers WHERE result_id = :resultId AND question_id = :questionId")
    suspend fun getUserAnswer(resultId: Long, questionId: Long): UserAnswerEntity?

    @Query("SELECT test_id FROM test_results WHERE result_id = :resultId")
    suspend fun getTestIdByResult(resultId: Long): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserAnswer(userAnswer: UserAnswerEntity): Long

    @Query("UPDATE test_results SET remaining_seconds = :remainingSeconds WHERE result_id = :resultId")
    suspend fun updateRemainingTime(resultId: Long, remainingSeconds: Int)

    @Update
    suspend fun updateUserAnswer(userAnswer: UserAnswerEntity)

    @Query("DELETE FROM user_answers WHERE result_id = :resultId")
    suspend fun deleteUserAnswers(resultId: Long)

    @Transaction
    @Query("""
    SELECT
      t.*,
      COUNT(q.question_id)               AS total_questions,
      tr.status                          AS last_status,
      tr.result_id                       AS last_result_id,
      COALESCE(tr.remaining_seconds, t.duration_minutes * 60) AS remaining_seconds,
      COALESCE(a.answered_cnt, 0)        AS answered_count,
      tr.finished_at                    AS finished_at
    FROM tests t
    LEFT JOIN questions q
      ON q.test_id = t.test_id
    LEFT JOIN (
      SELECT test_id, MAX(result_id) AS last_result_id
      FROM test_results
      GROUP BY test_id
    ) latest
      ON latest.test_id = t.test_id
    LEFT JOIN test_results tr
      ON tr.result_id = latest.last_result_id
    LEFT JOIN (
      SELECT result_id, COUNT(DISTINCT question_id) AS answered_cnt
      FROM user_answers
      GROUP BY result_id
    ) a
      ON a.result_id = tr.result_id
    GROUP BY t.test_id
  """)
    suspend fun getAllTestItems(): List<TestWithStats>

    data class TestWithStats(
        @Embedded val test: TestEntity,
        @ColumnInfo(name = "total_questions")   val totalQuestions: Int,
        @ColumnInfo(name = "last_status")       val lastStatus: String?,
        @ColumnInfo(name = "last_result_id")    val lastResultId: Long?,
        @ColumnInfo(name = "remaining_seconds") val remainingSeconds: Int,
        @ColumnInfo(name = "answered_count")    val answeredCount: Int,
        @ColumnInfo(name = "finished_at")      val finishedAt: Long?
    )

    @Query(
        """
        SELECT 
            SUM(CASE WHEN ua.is_correct = 1 THEN 1 ELSE 0 END) AS correct,
            COUNT(*) AS total 
        FROM user_answers ua
        WHERE ua.result_id = :resultId
    """
    )
    suspend fun getResultStats(resultId: Long): ResultStats

    data class ResultStats(
        @ColumnInfo(name = "correct") val correctAnswers: Int,
        @ColumnInfo(name = "total") val totalAnswers: Int
    )

    @Transaction
    suspend fun startTestSession(testId: Long): Long {
        val result = TestResultEntity(
            testId = testId,
            status = "in_progress",
            currentQuestionOrder = 1,
            remainingSeconds = null,
            finishedAt = null
        )
        return insertTestResult(result)
    }

    @Transaction
    suspend fun finishTestSession(resultId: Long, remainingSeconds: Int?) {
        val result = getTestResult(resultId) ?: return
        val updatedResult = result.copy(
            status = "completed",
            finishedAt = System.currentTimeMillis(),
            remainingSeconds = remainingSeconds
        )
        updateTestResult(updatedResult)
    }

    @Transaction
    suspend fun saveUserAnswer(
        resultId: Long,
        questionId: Long,
        answerId: Long?,
        freeText: String?,
        isCorrect: Boolean
    ) {
        val existing = getUserAnswer(resultId, questionId)
        val userAnswer = existing?.copy(
            answerId = answerId,
            isCorrect = isCorrect
        ) ?: UserAnswerEntity(
            userAnswerId = 0,
            resultId = resultId,
            questionId = questionId,
            answerId = answerId,
            isCorrect = isCorrect
        )

        if (existing != null) {
            updateUserAnswer(userAnswer)
        } else {
            insertUserAnswer(userAnswer)
        }
    }

    @Query("SELECT * FROM test_results WHERE result_id = :resultId")
    suspend fun getTestResult(resultId: Long): TestResultEntity?

    @Query("SELECT * FROM answers WHERE question_id = :questionId")
    suspend fun getAnswers(questionId: Long): List<AnswerEntity>

    @Query("SELECT duration_minutes FROM tests WHERE test_id = :testId")
    suspend fun getTestDuration(testId: Long): Int

    @Query("SELECT COUNT(*) FROM questions WHERE test_id = :testId")
    suspend fun getQuestionCount(testId: Long): Int

    @Query("SELECT remaining_seconds FROM test_results WHERE result_id = :resultId")
    suspend fun getRemainingSeconds(resultId: Long): Int?

    @Query(
        """
        SELECT * FROM test_results 
        WHERE status = 'in_progress'
        ORDER BY result_id DESC 
        LIMIT 1
    """
    )
    suspend fun getLastInProgressResult(): TestResultEntity?


    @Transaction
    @Query("""
    SELECT
      t.*,
      COUNT(q.question_id)               AS total_questions,
      tr.status                          AS last_status,
      tr.result_id                       AS last_result_id,
      COALESCE(tr.remaining_seconds, t.duration_minutes * 60) AS remaining_seconds,
      COALESCE(a.answered_cnt, 0)        AS answered_count
    FROM tests t
    LEFT JOIN questions q
      ON q.test_id = t.test_id
    LEFT JOIN (
      SELECT test_id, MAX(result_id) AS last_result_id
      FROM test_results
      GROUP BY test_id
    ) latest
      ON latest.test_id = t.test_id
    LEFT JOIN test_results tr
      ON tr.result_id = latest.last_result_id
    LEFT JOIN (
      SELECT result_id, COUNT(DISTINCT question_id) AS answered_cnt
      FROM user_answers
      GROUP BY result_id
    ) a
      ON a.result_id = tr.result_id
    WHERE t.test_id = :testId
    GROUP BY t.test_id
  """)
    suspend fun getTestWithStatsById(testId: Long): TestWithStats?

    @Transaction
    suspend fun seedAll() {
        insertTest(TestEntity(testName = "Java Core", durationMinutes = 20))
        insertTest(TestEntity(testName = "Основы C++", durationMinutes = 10))
        insertTest(TestEntity(testName = "React JS", durationMinutes = 10))

        listOf(
            QuestionEntity(
                testId = 1,
                questionText = "Как объявить класс в коде?",
                questionType = "single",
                minAnswers = 1,
                maxAnswers = 1,
                orderNumber = 1
            ),
            QuestionEntity(
                testId = 1,
                questionText = "Где правильно создан массив?",
                questionType = "single",
                minAnswers = 1,
                maxAnswers = 1,
                orderNumber = 2
            ),
            QuestionEntity(
                testId = 1,
                questionText = "Какой класс отвечает за получение информации от пользователя?",
                questionType = "single",
                minAnswers = 1,
                maxAnswers = 1,
                orderNumber = 3
            ),
            QuestionEntity(
                testId = 1,
                questionText = "Какие математические операции есть в Java?",
                questionType = "single",
                minAnswers = 1,
                maxAnswers = 1,
                orderNumber = 4
            ),
            QuestionEntity(
                testId = 1,
                questionText = "Где правильно осуществлен вывод?",
                questionType = "single",
                minAnswers = 1,
                maxAnswers = 1,
                orderNumber = 5
            ),
            QuestionEntity(
                testId = 1,
                questionText = "Где правильно создана простая переменная?",
                questionType = "single",
                minAnswers = 1,
                maxAnswers = 1,
                orderNumber = 6
            ),
            QuestionEntity(
                testId = 1,
                questionText = "Для чего можно использовать Java?",
                questionType = "single",
                minAnswers = 1,
                maxAnswers = 1,
                orderNumber = 7
            ),
            QuestionEntity(
                testId = 1,
                questionText = "Что общего у всех элементов массива?",
                questionType = "single",
                minAnswers = 1,
                maxAnswers = 1,
                orderNumber = 8
            ),
            QuestionEntity(
                testId = 1,
                questionText = "Сколько параметров может принимать функция?",
                questionType = "single",
                minAnswers = 1,
                maxAnswers = 1,
                orderNumber = 9
            ),
            QuestionEntity(
                testId = 1,
                questionText = "Каждый файл должен называется...",
                questionType = "single",
                minAnswers = 1,
                maxAnswers = 1,
                orderNumber = 10
            ),

            QuestionEntity(
                testId = 2,
                questionText = "Сколько аргументов можно передать в функцию?",
                questionType = "single",
                minAnswers = 1,
                maxAnswers = 1,
                orderNumber = 1
            ),
            QuestionEntity(
                testId = 2,
                questionText = "Каким будет x?\nint x = 2 + 1;",
                questionType = "single",
                minAnswers = 1,
                maxAnswers = 1,
                orderNumber = 2
            ),
            QuestionEntity(
                testId = 2,
                questionText = "Сколько параметров можно передать в деструктор?",
                questionType = "single",
                minAnswers = 1,
                maxAnswers = 1,
                orderNumber = 3
            ),
            QuestionEntity(
                testId = 2,
                questionText = "Что покажет код ниже?\nint const a = 5;\na++;\ncout << a;",
                questionType = "single",
                minAnswers = 1,
                maxAnswers = 1,
                orderNumber = 4
            ),
            QuestionEntity(
                testId = 2,
                questionText = "Что покажет код ниже?\nchar s[] = \"C++\";\ncout << s << \" \";\ns++;\ncout << s << \" \";",
                questionType = "single",
                minAnswers = 1,
                maxAnswers = 1,
                orderNumber = 5
            ),
            QuestionEntity(
                testId = 2,
                questionText = "Как указать комментарий?",
                questionType = "single",
                minAnswers = 1,
                maxAnswers = 1,
                orderNumber = 6
            ),
            QuestionEntity(
                testId = 2,
                questionText = "Что выдаст код ниже?\nchar s[] = \"hello\", t[] = \"hello\";\nif(s == t)\n   cout << \"True\";",
                questionType = "single",
                minAnswers = 1,
                maxAnswers = 1,
                orderNumber = 7
            ),
            QuestionEntity(
                testId = 2,
                questionText = "Где правильно указана переменная?",
                questionType = "single",
                minAnswers = 1,
                maxAnswers = 1,
                orderNumber = 8
            ),
            QuestionEntity(
                testId = 2,
                questionText = "Что покажет код ниже?\nchar *s = \"Fine\";\n*s = 'N';\ncout << s << endl;",
                questionType = "single",
                minAnswers = 1,
                maxAnswers = 1,
                orderNumber = 9
            ),
            QuestionEntity(
                testId = 2,
                questionText = "Как подключить стандартную библиотеку iostream?",
                questionType = "single",
                minAnswers = 1,
                maxAnswers = 1,
                orderNumber = 10
            ),

            QuestionEntity(
                testId = 3,
                questionText = "Чем свойства отличаются от состояний?",
                questionType = "single",
                minAnswers = 1,
                maxAnswers = 1,
                orderNumber = 1
            ),
            QuestionEntity(
                testId = 3,
                questionText = "Где правильно выведен компонент через рендер?",
                questionType = "single",
                minAnswers = 1,
                maxAnswers = 1,
                orderNumber = 2
            ),
            QuestionEntity(
                testId = 3,
                questionText = "Как обратится к свойству weight?\n<Test weight=\"203\" height=\"182\" />",
                questionType = "single",
                minAnswers = 1,
                maxAnswers = 1,
                orderNumber = 3
            ),
            QuestionEntity(
                testId = 3,
                questionText = "От какого класса идет наследование всех компонентов?",
                questionType = "single",
                minAnswers = 1,
                maxAnswers = 1,
                orderNumber = 4
            ),
            QuestionEntity(
                testId = 3,
                questionText = "Куда можно встроить готовый код из метода render()?",
                questionType = "single",
                minAnswers = 1,
                maxAnswers = 1,
                orderNumber = 5
            ),
            QuestionEntity(
                testId = 3,
                questionText = "React JS – это...",
                questionType = "single",
                minAnswers = 1,
                maxAnswers = 1,
                orderNumber = 6
            ),
            QuestionEntity(
                testId = 3,
                questionText = "Где правильно создан компонент?",
                questionType = "single",
                minAnswers = 1,
                maxAnswers = 1,
                orderNumber = 7
            ),
            QuestionEntity(
                testId = 3,
                questionText = "Где правильно передена функция в качестве свойства?",
                questionType = "single",
                minAnswers = 1,
                maxAnswers = 1,
                orderNumber = 8
            ),
            QuestionEntity(
                testId = 3,
                questionText = "Какой метод отвечает за вывод информации через React JS компонент?",
                questionType = "single",
                minAnswers = 1,
                maxAnswers = 1,
                orderNumber = 9
            ),
            QuestionEntity(
                testId = 3,
                questionText = "Как много компонентов может быть на сайте?",
                questionType = "single",
                minAnswers = 1,
                maxAnswers = 1,
                orderNumber = 10
            ),
            QuestionEntity(
                testId = 3,
                questionText = "Какая компания разработала React JS?",
                questionType = "single",
                minAnswers = 1,
                maxAnswers = 1,
                orderNumber = 11
            ),
            QuestionEntity(
                testId = 3,
                questionText = "Сколько родительских HTML тегов может быть выведено в React JS компоненте?",
                questionType = "single",
                minAnswers = 1,
                maxAnswers = 1,
                orderNumber = 12
            )
        ).forEach { insertQuestion(it) }

        listOf(
            AnswerEntity(questionId = 1, answerText = "class MyClass {}", isCorrect = true),
            AnswerEntity(questionId = 1, answerText = "new class MyClass {}", isCorrect = false),
            AnswerEntity(
                questionId = 1,
                answerText = "select * from class MyClass {}",
                isCorrect = false
            ),
            AnswerEntity(
                questionId = 1,
                answerText = "MyClass extends class {}",
                isCorrect = false
            ),
            AnswerEntity(
                questionId = 2,
                answerText = "int a[] = {1, 2, 3, 4, 5};",
                isCorrect = false
            ),
            AnswerEntity(
                questionId = 2,
                answerText = "int[] a = new int[] {1, 2, 3, 4, 5};",
                isCorrect = true
            ),
            AnswerEntity(
                questionId = 2,
                answerText = "int[] a = new int {1, 2, 3, 4, 5};",
                isCorrect = false
            ),
            AnswerEntity(
                questionId = 2,
                answerText = "int[] a = int[] {1, 2, 3, 4, 5};",
                isCorrect = false
            ),
            AnswerEntity(questionId = 3, answerText = "Get", isCorrect = false),
            AnswerEntity(questionId = 3, answerText = "Scanner", isCorrect = true),
            AnswerEntity(questionId = 3, answerText = "Scaner", isCorrect = false),
            AnswerEntity(questionId = 3, answerText = "System", isCorrect = false),
            AnswerEntity(questionId = 4, answerText = "+", isCorrect = false),
            AnswerEntity(questionId = 4, answerText = "-", isCorrect = false),
            AnswerEntity(questionId = 4, answerText = "*", isCorrect = false),
            AnswerEntity(questionId = 4, answerText = "Все перечисленные", isCorrect = true),
            AnswerEntity(questionId = 5, answerText = "System.out.println();", isCorrect = true),
            AnswerEntity(questionId = 5, answerText = "cin >> a;", isCorrect = false),
            AnswerEntity(questionId = 5, answerText = "Console.Write();", isCorrect = false),
            AnswerEntity(questionId = 5, answerText = "cout << a;", isCorrect = false),
            AnswerEntity(questionId = 6, answerText = "int a;", isCorrect = true),
            AnswerEntity(questionId = 6, answerText = "a = int;", isCorrect = false),
            AnswerEntity(questionId = 6, answerText = "a int;", isCorrect = false),
            AnswerEntity(questionId = 6, answerText = "int = a;", isCorrect = false),
            AnswerEntity(questionId = 7, answerText = "разработка мобильных приложений", isCorrect = true),
            AnswerEntity(questionId = 7, answerText = "разработка серверов", isCorrect = true),
            AnswerEntity(questionId = 7, answerText = "создание микроволновок", isCorrect = false),
            AnswerEntity(questionId = 7, answerText = "создание фильмов", isCorrect = false),
            AnswerEntity(questionId = 8, answerText = "тип данных", isCorrect = true),
            AnswerEntity(questionId = 8, answerText = "размер", isCorrect = false),
            AnswerEntity(questionId = 8, answerText = "название", isCorrect = false),
            AnswerEntity(questionId = 8, answerText = "индекс", isCorrect = false),
            AnswerEntity(questionId = 9, answerText = "любое", isCorrect = true),
            AnswerEntity(questionId = 9, answerText = "0", isCorrect = false),
            AnswerEntity(questionId = 9, answerText = "не более 3", isCorrect = false),
            AnswerEntity(questionId = 9, answerText = "1", isCorrect = false),
            AnswerEntity(questionId = 10, answerText = "по имени класса", isCorrect = true),
            AnswerEntity(questionId = 10, answerText = "abc.java", isCorrect = false),
            AnswerEntity(questionId = 10, answerText = "любое", isCorrect = false),
            AnswerEntity(questionId = 10, answerText = "Main.java", isCorrect = false),
            AnswerEntity(questionId = 11, answerText = "любое", isCorrect = true),
            AnswerEntity(questionId = 11, answerText = "не более 1", isCorrect = false),
            AnswerEntity(questionId = 11, answerText = "0", isCorrect = false),
            AnswerEntity(questionId = 11, answerText = "не более 3", isCorrect = false),
            AnswerEntity(questionId = 12, answerText = "3", isCorrect = true),
            AnswerEntity(questionId = 12, answerText = "0", isCorrect = false),
            AnswerEntity(questionId = 12, answerText = "1", isCorrect = false),
            AnswerEntity(questionId = 12, answerText = "2", isCorrect = false),
            AnswerEntity(questionId = 13, answerText = "0", isCorrect = true),
            AnswerEntity(questionId = 13, answerText = "1", isCorrect = false),
            AnswerEntity(questionId = 13, answerText = "любое", isCorrect = false),
            AnswerEntity(questionId = 13, answerText = "2", isCorrect = false),
            AnswerEntity(questionId = 14, answerText = "ошибку компиляции", isCorrect = true),
            AnswerEntity(questionId = 14, answerText = "6", isCorrect = false),
            AnswerEntity(questionId = 14, answerText = "5", isCorrect = false),
            AnswerEntity(questionId = 14, answerText = "4", isCorrect = false),
            AnswerEntity(questionId = 15, answerText = "C++ ++ ++", isCorrect = true),
            AnswerEntity(questionId = 15, answerText = "ошибка", isCorrect = false),
            AnswerEntity(questionId = 15, answerText = "C++", isCorrect = false),
            AnswerEntity(questionId = 15, answerText = "++", isCorrect = false),
            AnswerEntity(questionId = 16, answerText = "//", isCorrect = true),
            AnswerEntity(questionId = 16, answerText = "**", isCorrect = false),
            AnswerEntity(questionId = 16, answerText = "#", isCorrect = false),
            AnswerEntity(questionId = 16, answerText = "<!-- -->", isCorrect = false),
            AnswerEntity(questionId = 17, answerText = "ничего", isCorrect = true),
            AnswerEntity(questionId = 17, answerText = "True", isCorrect = false),
            AnswerEntity(questionId = 17, answerText = "False", isCorrect = false),
            AnswerEntity(questionId = 17, answerText = "ошибку", isCorrect = false),
            AnswerEntity(questionId = 18, answerText = "int a;", isCorrect = true),
            AnswerEntity(questionId = 18, answerText = "a = int;", isCorrect = false),
            AnswerEntity(questionId = 18, answerText = "int = a;", isCorrect = false),
            AnswerEntity(questionId = 18, answerText = "a int;", isCorrect = false),
            AnswerEntity(questionId = 19, answerText = "ошибка выполнения", isCorrect = true),
            AnswerEntity(questionId = 19, answerText = "Fine", isCorrect = false),
            AnswerEntity(questionId = 19, answerText = "Nine", isCorrect = false),
            AnswerEntity(questionId = 19, answerText = "N", isCorrect = false),
            AnswerEntity(questionId = 20, answerText = "#include <iostream>", isCorrect = true),
            AnswerEntity(questionId = 20, answerText = "#connect iostream", isCorrect = false),
            AnswerEntity(questionId = 20, answerText = "#define iostream", isCorrect = false),
            AnswerEntity(questionId = 20, answerText = "#using iostream", isCorrect = false),
            AnswerEntity(questionId = 21, answerText = "JavaScript", isCorrect = true),
            AnswerEntity(questionId = 21, answerText = "C#", isCorrect = false),
            AnswerEntity(questionId = 21, answerText = "Java", isCorrect = false),
            AnswerEntity(questionId = 21, answerText = "PHP", isCorrect = false),
            AnswerEntity(
                questionId = 22,
                answerText = "обновлять DOM без перезагрузки страницы",
                isCorrect = true
            ),
            AnswerEntity(
                questionId = 22,
                answerText = "делать резервное копирование",
                isCorrect = false
            ),
            AnswerEntity(questionId = 22, answerText = "шифровать данные", isCorrect = false),
            AnswerEntity(questionId = 22, answerText = "хранить пароли", isCorrect = false),
            AnswerEntity(questionId = 23, answerText = "ReactDOM.render()", isCorrect = true),
            AnswerEntity(questionId = 23, answerText = "ReactMount()", isCorrect = false),
            AnswerEntity(questionId = 23, answerText = "ReactRender()", isCorrect = false),
            AnswerEntity(questionId = 23, answerText = "renderComponent()", isCorrect = false),
            AnswerEntity(
                questionId = 24,
                answerText = "переменные состояния компонента",
                isCorrect = true
            ),
            AnswerEntity(questionId = 24, answerText = "CSS классы", isCorrect = false),
            AnswerEntity(questionId = 24, answerText = "HTML элементы", isCorrect = false),
            AnswerEntity(questionId = 24, answerText = "события", isCorrect = false),
            AnswerEntity(
                questionId = 25,
                answerText = "свойства, передаваемые в компонент",
                isCorrect = true
            ),
            AnswerEntity(questionId = 25, answerText = "встроенные стили", isCorrect = false),
            AnswerEntity(
                questionId = 25,
                answerText = "методы жизненного цикла",
                isCorrect = false
            ),
            AnswerEntity(questionId = 25, answerText = "внешние модули", isCorrect = false),
            AnswerEntity(
                questionId = 26,
                answerText = "объект с данными, которые могут меняться",
                isCorrect = true
            ),
            AnswerEntity(questionId = 26, answerText = "путь к API", isCorrect = false),
            AnswerEntity(questionId = 26, answerText = "импорт модуля", isCorrect = false),
            AnswerEntity(questionId = 26, answerText = "HTML разметку", isCorrect = false),
            AnswerEntity(
                questionId = 27,
                answerText = "компонент, не использующий state",
                isCorrect = true
            ),
            AnswerEntity(
                questionId = 27,
                answerText = "компонент, использующий только CSS",
                isCorrect = false
            ),
            AnswerEntity(questionId = 27, answerText = "объект", isCorrect = false),
            AnswerEntity(questionId = 27, answerText = "модуль", isCorrect = false),

            AnswerEntity(questionId = 28, answerText = "console", isCorrect = false),
            AnswerEntity(questionId = 28, answerText = "print", isCorrect = false),
            AnswerEntity(questionId = 28, answerText = "react", isCorrect = false),
            AnswerEntity(questionId = 28, answerText = "render", isCorrect = true),
            AnswerEntity(questionId = 29, answerText = "Не более 300", isCorrect = false),
            AnswerEntity(questionId = 29, answerText = "Не более 10", isCorrect = false),
            AnswerEntity(questionId = 29, answerText = "Не более 100", isCorrect = false),
            AnswerEntity(questionId = 29, answerText = "Неограниченное количество", isCorrect = true),
            AnswerEntity(questionId = 30, answerText = "позволяет использовать хуки", isCorrect = true),
            AnswerEntity(questionId = 30, answerText = "не требует импорта", isCorrect = false),
            AnswerEntity(questionId = 30, answerText = "работает только на сервере", isCorrect = false),
            AnswerEntity(questionId = 30, answerText = "поддерживает только class-компоненты", isCorrect = false),
            AnswerEntity(questionId = 31, answerText = "Github", isCorrect = false),
            AnswerEntity(questionId = 31, answerText = "Google", isCorrect = false),
            AnswerEntity(questionId = 31, answerText = "Facebook", isCorrect = true),
            AnswerEntity(questionId = 31, answerText = "Twitter", isCorrect = false),
            AnswerEntity(questionId = 32, answerText = "Не более 10", isCorrect = false),
            AnswerEntity(questionId = 32, answerText = "Не более 5", isCorrect = false),
            AnswerEntity(questionId = 32, answerText = "Всегда 1", isCorrect = true),
            AnswerEntity(questionId = 32, answerText = "Неограниченное количество", isCorrect = false)
        ).forEach { insertAnswer(it) }
    }
}