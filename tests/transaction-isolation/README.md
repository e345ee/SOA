# Проверка транзакций в Payara

Из корня репозитория на Java 17:

```sh
mvn -pl second-service -am clean package
python3 tests/transaction-isolation/build.py
```

Результат: `second-service/target/transaction-probe/probe.war`. Развернуть отдельно от основного приложения под именем `soa-tx-probe` в Payara с `jdbc/SharedDB`. После проверки удалить приложение командой `asadmin undeploy soa-tx-probe`.

WAR исключает HTTP-контроллеры. Проверки запускаются через startup EJB, активированный CDI request context и отдельные CDI-бины. Данные не изменяются; читаются `txid_current()` и `transaction_isolation`.

Проверяется:

- Внешняя REQUIRED-транзакция выполняет SQL до вложенного вызова настоящего `TicketService.getTickets`.
- Вложенный CDI-вызов с REQUIRES_NEW и REPEATABLE_READ получает другую PostgreSQL-транзакцию.
- После возврата восстановлены ID и уровень внешней транзакции.
- Аннотации класса работают; настройки метода имеют приоритет.
- REQUIRED с TransactionIsolation отклоняется до входа в тело метода.

Успех: две строки `TX_PROBE guard and annotation precedence PASS` и `TX_PROBE PASS` в server.log. При неуспехе runner пишет `TX_PROBE FAIL` и stack trace; успешное развёртывание само по себе не является прохождением теста.
