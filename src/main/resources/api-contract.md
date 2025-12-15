# API contract — "Framework" (унифицированный контракт для ЛР6/ЛР7)

Базовый префикс: `/api`

Общие принципы:
- Формат: JSON.
- Пагинация: стандарт Spring Data `?page=0&size=20`.
- Сортировка: `?sort=field,asc` (несколько `sort` допустимы).
- Стандарты ответов:
    - Для списков — `Page<DTO>` (в JSON — объект со свойством `content`).
    - Для одиночных сущностей — DTO объект.
- Ошибки — HTTP статус + тело `{ "error": "...", "details": "..." }`.

## Сущности
- User: id, name, accessLvl
- Function: id, name, format, funcResult, user (owner)
- Point: id, indexInFunction, x, y, functionId

---

## Users
### GET /api/users
Параметры: `page`, `size`, `sort`, `name` (contains)
Пример: `GET /api/users?page=0&size=20&sort=name,asc&name=seed`
Ответ: `Page<UserDto>`

### GET /api/users/{id}
Возвращает `UserDto`

### POST /api/users
Тело: `CreateUserRequest { name, password, accessLvl }`
Создаёт пользователя → возвращает `UserDto` (201)

### PUT /api/users/{id}
Тело: `UpdateUserRequest { name?, accessLvl? }`
Обновляет → `UserDto`

### DELETE /api/users/{id}`

---

## Functions
### GET /api/functions
Параметры: `page,size,sort,name,userId,format,withPoints`
`withPoints=true` — включить поле pointsCount (не полные точки).
Ответ: `Page<FunctionDto>`

### GET /api/functions/{id}
Параметры: `?withPoints=true` — если true, в ответ добавится `pointsCount` (и может быть список точек по опциональному флагу)
Ответ: `FunctionDto`

### POST /api/functions
Тело: `CreateFunctionRequest { name, format, userId, funcResult }`
→ `FunctionDto`

### PUT /api/functions/{id}`

### DELETE /api/functions/{id}`

### GET /api/functions/by-user/{userId}
Возвращает `List<FunctionDto>`

---

## Points
### GET /api/points
Параметры: `functionId`, `page`, `size`, `sort`
Ответ: `Page<PointDto>`

### GET /api/points/{id}`
→ `PointDto`

### POST /api/points
Тело: `CreatePointRequest { functionId, indexInFunction, x, y }` → `PointDto`

### PUT /api/points/{id}`

### DELETE /api/points/{id}`

---

## Search (BFS / DFS)
- GET `/api/search/bfs`
- GET `/api/search/dfs`

Параметры (пример):
