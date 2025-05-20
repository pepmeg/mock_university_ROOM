PRAGMA foreign_keys = ON;

BEGIN TRANSACTION;

-- 1. Таблица tests
CREATE TABLE IF NOT EXISTS tests (
    test_id INTEGER PRIMARY KEY AUTOINCREMENT,
    test_name TEXT NOT NULL,
    description TEXT,
    duration_minutes INTEGER NOT NULL,
    created_at TEXT DEFAULT CURRENT_TIMESTAMP
);

-- 2. Таблица questions
CREATE TABLE IF NOT EXISTS questions (
    question_id INTEGER PRIMARY KEY AUTOINCREMENT,
    test_id INTEGER NOT NULL,
    question_text TEXT NOT NULL,
    question_type TEXT NOT NULL,
    min_answers INTEGER DEFAULT 1,
    max_answers INTEGER DEFAULT 1,
    order_number INTEGER NOT NULL,
    FOREIGN KEY(test_id) REFERENCES tests(test_id) ON DELETE CASCADE,
    UNIQUE(test_id, order_number)
);

-- 3. Таблица answers
CREATE TABLE IF NOT EXISTS answers (
    answer_id INTEGER PRIMARY KEY AUTOINCREMENT,
    question_id INTEGER NOT NULL,
    answer_text TEXT NOT NULL,
    is_correct INTEGER DEFAULT 0 CHECK(is_correct IN (0,1)),
    FOREIGN KEY(question_id) REFERENCES questions(question_id) ON DELETE CASCADE
);

-- 4. Таблица demo
CREATE TABLE IF NOT EXISTS demo (
    ID INTEGER PRIMARY KEY,
    Name TEXT,
    Hint TEXT
);

-- 5. Таблица test_metadata
CREATE TABLE IF NOT EXISTS test_metadata (
    test_id INTEGER PRIMARY KEY,
    average_success_rate REAL DEFAULT 0.0,
    total_attempts INTEGER DEFAULT 0,
    FOREIGN KEY(test_id) REFERENCES tests(test_id) ON DELETE CASCADE
);

-- 6. Таблица test_results
CREATE TABLE IF NOT EXISTS test_results (
    result_id INTEGER PRIMARY KEY AUTOINCREMENT,
    test_id INTEGER NOT NULL,
    started_at TEXT DEFAULT CURRENT_TIMESTAMP,
    finished_at TEXT,
    current_question_order INTEGER DEFAULT 1,
    status TEXT DEFAULT 'in_progress',
    correct_percentage REAL DEFAULT 0.0,
    remaining_seconds INTEGER DEFAULT 0,
    FOREIGN KEY(test_id) REFERENCES tests(test_id) ON DELETE CASCADE
);

-- 7. Таблица user_answers
CREATE TABLE IF NOT EXISTS user_answers (
    user_answer_id INTEGER PRIMARY KEY AUTOINCREMENT,
    result_id INTEGER NOT NULL,
    question_id INTEGER NOT NULL,
    answer_id INTEGER,
    free_text_answer TEXT,
    is_correct INTEGER DEFAULT 0 CHECK(is_correct IN (0,1)),
    FOREIGN KEY(result_id)   REFERENCES test_results(result_id) ON DELETE CASCADE,
    FOREIGN KEY(question_id) REFERENCES questions(question_id),
    FOREIGN KEY(answer_id)   REFERENCES answers(answer_id)
);

-- Пример наполнения первых записей (остальные по аналогии)
INSERT INTO tests(test_id, test_name, description, duration_minutes, created_at) VALUES
  (1,'Java Core',NULL,20,'2025-04-26 07:46:24'),
  (2,'Основы C++',NULL,10,'2025-04-30 05:53:51'),
  (3,'React JS',NULL,10,'2025-05-07 07:06:56');

INSERT INTO questions(question_id, test_id, question_text, question_type, min_answers, max_answers, order_number) VALUES
  (1,1,'Как объявить класс в коде?','single',1,1,1),
  (2,1,'Где правильно создан массив?','single',1,1,2),
  (3,1,'Какой класс отвечает за получение информации от пользователя?','single',1,1,3),
  (4,1,'Какие математические операции есть в Java?','single',1,1,4);

INSERT INTO answers(answer_id, question_id, answer_text, is_correct) VALUES
  (1,1,'class MyClass {}',1),
  (2,1,'new class MyClass {}',0),
  (3,1,'select * from class MyClass {}',0),
  (4,1,'MyClass extends class {}',0),
  (5,2,'int a[] = {1, 2, 3, 4, 5};',0),
  (6,2,'int[] a = new int[] {1, 2, 3, 4, 5};',1),
  (7,2,'int[] a = new int {1, 2, 3, 4, 5};',0),
  (8,2,'int[] a = int[] {1, 2, 3, 4, 5};',0);

INSERT INTO answers(answer_id, question_id, answer_text, is_correct) VALUES
  (9, 3, 'Get', 0);
INSERT INTO answers(answer_id, question_id, answer_text, is_correct) VALUES
  (10, 3, 'Scanner', 1);
INSERT INTO answers(answer_id, question_id, answer_text, is_correct) VALUES
  (11, 3, 'Scaner', 0);
INSERT INTO answers(answer_id, question_id, answer_text, is_correct) VALUES
  (12, 3, 'System', 0);

INSERT INTO answers(answer_id, question_id, answer_text, is_correct) VALUES
  (13, 4, '+, -, *, /, %', 0);
INSERT INTO answers(answer_id, question_id, answer_text, is_correct) VALUES
  (14, 4, '+, -, *, /, --, ++', 0);
INSERT INTO answers(answer_id, question_id, answer_text, is_correct) VALUES
  (15, 4, '+, -, *, /', 0);
INSERT INTO answers(answer_id, question_id, answer_text, is_correct) VALUES
  (16, 4, 'Все перечисленные', 1);

INSERT INTO answers(answer_id, question_id, answer_text, is_correct) VALUES
  (17, 5, 'System.out.print("Hello World!");', 1);
INSERT INTO answers(answer_id, question_id, answer_text, is_correct) VALUES
  (18, 5, 'print("Hello World!");', 0);
INSERT INTO answers(answer_id, question_id, answer_text, is_correct) VALUES
  (19, 5, 'System.print("Hello World!");', 0);
INSERT INTO answers(answer_id, question_id, answer_text, is_correct) VALUES
  (20, 5, 'System.out("Hello World!");', 0);

INSERT INTO answers(answer_id, question_id, answer_text, is_correct) VALUES
  (21, 6, 'byte x = 100000;', 0);
INSERT INTO answers(answer_id, question_id, answer_text, is_correct) VALUES
  (22, 6, 'float x = 23.3f;', 1);
INSERT INTO answers(answer_id, question_id, answer_text, is_correct) VALUES
  (23, 6, 'bool isDone = true;', 0);
INSERT INTO answers(answer_id, question_id, answer_text, is_correct) VALUES
  (24, 6, 'char str = ''ab'';', 0);

INSERT INTO answers(answer_id, question_id, answer_text, is_correct) VALUES
  (25, 7, 'Для создания программ под ПК', 0);
INSERT INTO answers(answer_id, question_id, answer_text, is_correct) VALUES
  (26, 7, 'Для создания игр', 0);
INSERT INTO answers(answer_id, question_id, answer_text, is_correct) VALUES
  (27, 7, 'Для создания сайтов', 0);
INSERT INTO answers(answer_id, question_id, answer_text, is_correct) VALUES
  (28, 7, 'Для всего перечисленного', 1);

INSERT INTO answers(answer_id, question_id, answer_text, is_correct) VALUES
  (29, 8, 'Их тип данных', 1);
INSERT INTO answers(answer_id, question_id, answer_text, is_correct) VALUES
  (30, 8, 'Их названия', 0);
INSERT INTO answers(answer_id, question_id, answer_text, is_correct) VALUES
  (31, 8, 'Их адрес в памяти', 0);
INSERT INTO answers(answer_id, question_id, answer_text, is_correct) VALUES
  (32, 8, 'Их размер', 0);

-- Вопрос 9–16
INSERT INTO answers(answer_id, question_id, answer_text, is_correct) VALUES
  (33, 9, 'Не более 10', 0);
INSERT INTO answers(answer_id, question_id, answer_text, is_correct) VALUES
  (34, 9, 'Не более 20', 0);
INSERT INTO answers(answer_id, question_id, answer_text, is_correct) VALUES
  (35, 9, 'Не более 5', 0);
INSERT INTO answers(answer_id, question_id, answer_text, is_correct) VALUES
  (36, 9, 'Неограниченное количество', 1);

INSERT INTO answers(answer_id, question_id, answer_text, is_correct) VALUES
  (37, 10, 'по имени класса в нём', 1);
INSERT INTO answers(answer_id, question_id, answer_text, is_correct) VALUES
  (38, 10, 'по имени названия пакета', 0);
INSERT INTO answers(answer_id, question_id, answer_text, is_correct) VALUES
  (39, 10, 'по имени основного метода в нем', 0);
INSERT INTO answers(answer_id, question_id, answer_text, is_correct) VALUES
  (40, 10, 'как вам захочется', 0);

-- Вопросы из теста 2 (question_id 11–20)
INSERT INTO answers(answer_id, question_id, answer_text, is_correct) VALUES
  (41, 11, 'До 30', 0),
  (42, 11, 'До 5', 0),
  (43, 11, 'Не более 50', 0),
  (44, 11, 'Неограниченное количество', 1),
  (45, 12, '2', 0),
  (46, 12, '1', 0),
  (47, 12, '3', 1),
  (48, 12, '0', 0),
  (49, 13, 'Не более 15', 0),
  (50, 13, 'Не более 10', 0),
  (51, 13, 'Нельзя передавать параметры в деструктор', 1),
  (52, 13, 'Не более 3', 0),
  (53, 14, '0', 0),
  (54, 14, '6', 0),
  (55, 14, 'Ошибку', 1),
  (56, 14, '5', 0),
  (57, 15, 'C++', 0),
  (58, 15, 'C++ ++', 0),
  (59, 15, 'C++ C++', 0),
  (60, 15, 'Ошибку', 1),
  (61, 16, '/* здесь комментарий', 0),
  (62, 16, '/* здесь комментарий /*', 0),
  (63, 16, '// здесь комментарий', 1),
  (64, 16, '# здесь комментарий', 0),
  (65, 17, 'True', 0),
  (66, 17, 'Будет ошибка в коде', 0),
  (67, 17, 'Нельзя сравнивать массивы на равенство', 0),
  (68, 17, 'Ничего не выведет, так как идет сравнение указателей', 1),
  (69, 18, 'char sym = ''a'';', 1),
  (70, 18, 'done = true;', 0),
  (71, 18, 'int num = "1";', 0),
  (72, 18, 'float x = 32,14;', 0),
  (73, 19, 'Ошибку', 1),
  (74, 19, 'Fine', 0),
  (75, 19, 'Nine', 0),
  (76, 19, '’N’', 0),
  (77, 20, '#include <iostream.h>', 0),
  (78, 20, '#include “iostream.h”', 0),
  (79, 20, '#include <iostream> ', 1),
  (80, 20, '#include iostream', 0),
  (81, 21, 'Состояния для работы со значениями, свойства для работы с функциям', 0),
  (82, 21, 'Свойства можно изменять, состояния нельзя', 0),
  (83, 21, 'Состояния можно изменять, свойства нельзя', 1),
  (84, 21, 'Свойства для работы со значениями, состояния для работы с функциями', 0),
  (85, 22, '<Test >', 0),
  (86, 22, '</Test>', 0),
  (87, 22, '</ Test>', 0),
  (88, 22, '<Test />', 1),
  (89, 23, '{this.prop.weight}', 0),
  (90, 23, '{this.weight}', 0),
  (91, 23, '{prop.weight}', 0),
  (92, 23, '{this.props.weight}', 1),
  (93, 24, 'ComponentReact', 0),
  (94, 24, 'React.Component', 1),
  (95, 24, 'Component', 0),
  (96, 24, 'ReactJS.Component', 0),
  (97, 25, 'Только в div', 0),
  (98, 25, 'Только в тег, у которого есть id', 0),
  (99, 25, 'В div или же в span', 0),
  (100, 25, 'В любой тег', 1),
  (101, 26, 'MVC-фреймворк', 0),
  (102, 26, 'Back-end платформа', 0),
  (103, 26, 'фреймворк', 0),
  (104, 26, 'JavaScript библиотека', 1),
  (105, 27, 'class App extends React.Component {}', 1),
  (106, 27, 'class App extends Component {}', 0),
  (107, 27, 'class App extends React.Component ({})', 0),
  (108, 27, 'class App {}', 0),
  (109, 28, 'argument={this.someFunction}', 1),
  (110, 28, 'argument=(this.someFunction)', 0),
  (111, 28, 'argument={this.someFunction ()}', 0),
  (112, 28, 'argument={this.someFunction {}}', 0),
  (113, 29, 'console', 0),
  (114, 29, 'print', 0),
  (115, 29, 'react', 0),
  (116, 29, 'render', 1),
  (117, 30, 'Не более 300', 0),
  (118, 30, 'Не более 10', 0),
  (119, 30, 'Не более 100', 0),
  (120, 30, 'Неограниченное количество', 1),
  (121, 31, 'GitHub', 0),
  (122, 31, 'Google', 0),
  (123, 31, 'Facebook', 1),
  (124, 31, 'Twitter', 0),
  (125, 32, 'Не более 10', 0),
  (126, 32, 'Не более 5', 0),
  (127, 32, 'Всегда 1', 1),
  (128, 32, 'Неограниченное количество', 0);

COMMIT;
