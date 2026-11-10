# Логи и обращения к БД

## Где искать на kronva-stat.ru

| Источник | Файл на сервере |
| --- | --- |
| ticket-service, Hibernate SQL, ошибки CDI/JTA | `/opt/payara7/glassfish/domains/domain1/logs/server.log` |
| booking-service, обращения к ticket-service | `/opt/payara7/glassfish/domains/domain2/logs/server.log` |
| Nginx HTTP-запросы | `/var/log/nginx/access.log` (для некоторых location выключен) |
| Nginx ошибки проксирования | `/var/log/nginx/error.log` |
| PostgreSQL | `/var/log/postgresql/postgresql-16-main.log` |

Просмотр с учётом ротации:

```sh
ssh root@kronva-stat.ru 'tail -F /opt/payara7/glassfish/domains/domain1/logs/server.log'
ssh root@kronva-stat.ru 'tail -F /opt/payara7/glassfish/domains/domain2/logs/server.log'
```

Запуск/остановка служб: `journalctl -u payara-domain1 -u payara-domain2 -u nginx`. Настройки Java logging: `<domain>/config/logging.properties`.

Lombok `@Slf4j` создаёт поле логгера. SLF4J через `slf4j-jdk14` передаёт сообщения в JUL/Payara. Уровень сервера INFO; DEBUG обычно скрыт. Сообщения поиска списков теперь INFO. Ошибки mapper-ов пишутся со stack trace. SQL печатается через Hibernate show_sql; format_sql разбивает один запрос на много строк. Число строк журнала не равно числу запросов.

## Число запросов

11 ноября 2026 проверены последовательные HTTP-чтения на сервере. Подсчёт SELECT выполнен по новым фрагментам server.log; это диагностическая выборка, не нагрузочный тест.

| Операция | SELECT в измерении | Комментарий |
| --- | --- | --- |
| Билеты, size=1 | 2 | COUNT + страница |
| Билеты, size=20 (вернулось 9) | 2 | Количество SELECT не растёт с числом строк |
| Люди, size=20 (вернулось 16) | 2 | COUNT + страница |
| Количество билетов человека | 1 | COUNT по person_id |

Поиск билетов дополнительно выполняет один `SET TRANSACTION ISOLATION LEVEL REPEATABLE READ` через JDBC. Эта команда не выводится через Hibernate show_sql. Итого для непустой страницы — три SQL-команды, без учёта транзакционного протокола BEGIN/COMMIT. Для пустой страницы запрос данных пропускается.

`TicketRepository.search` использует fetch join человека, поэтому маппинг страницы не вызывает отдельный SELECT для каждого билета.

Оценка операций записи по коду, без измерения на тестовых данных:

| Операция | Ожидаемые обращения |
| --- | --- |
| Создать билет существующему человеку | SELECT человека + INSERT билета |
| Создать билет с новым человеком | INSERT человека + INSERT билета |
| Обновить человека | SELECT + UPDATE |
| Удалить билет | Один DELETE с проверкой числа удалённых строк |
| Удалить билеты события | Один DELETE |
| Удалить человека | SELECT существования + COUNT билетов + DELETE (если нет связей) |

Это не выглядит избыточным для текущего приложения. COUNT нужен для точного totalElements/totalPages. При росте данных следует измерять время и планы запросов, проверять индексы фильтров, а не убирать проверки существования и связей ради уменьшения числа запросов. Проверка удаления человека сама по себе не устраняет гонку: окончательную целостность обеспечивает внешний ключ БД.
