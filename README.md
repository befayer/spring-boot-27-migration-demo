# Billing Service

Сервис выставления и оплаты счетов. Работает в продакшене на **Spring Boot 2.7**, и его нужно перевести на **Spring Boot 3.5**, не сломав ни одного клиента.

## Стек

- Java 11, Spring Boot 2.7.18, Gradle 8.14 (Kotlin DSL);
- Spring Web MVC, Bean Validation (`javax.*`);
- Spring Data JPA, Hibernate 5.6, Flyway, PostgreSQL 16;
- Spring Security (`WebSecurityConfigurerAdapter`, HTTP Basic);
- Spring Cloud Sleuth для трассировки, Actuator.

## API

| Метод | Путь | Доступ | Назначение |
| --- | --- | --- | --- |
| `POST` | `/api/invoices` | Basic | Выставить счёт |
| `GET` | `/api/invoices` | Basic | Список счетов, новые первыми |
| `GET` | `/api/invoices/{id}` | Basic | Счёт по id |
| `POST` | `/api/invoices/{id}/payments` | Basic | Отметить оплату |
| `GET` | `/api/public/pay/{token}` | публичный | Страница оплаты для плательщика |
| `GET` | `/actuator/health` | публичный | Health check |

Учётные данные по умолчанию: `billing` / `billing-secret`.

## Клиенты

- Веб-кабинет ходит в API напрямую.
- Мобильное приложение 3.x запрашивает списки со слэшем на конце (`/api/invoices/`).
- API gateway передаёт контекст трассировки в заголовках B3 и подписанный контекст клиента в `X-Client-Context` (до 16 КБ).
- Плательщики открывают `/api/public/pay/{token}` из письма без авторизации.

## Запуск

Нужны Docker и JDK 17–21 для запуска Gradle (JDK 11 для сборки Gradle найдёт сам через toolchain).

```bash
docker compose up -d --wait
./gw bootRun
```

```bash
curl -u billing:billing-secret http://localhost:8080/api/invoices \
  -H 'Content-Type: application/json' \
  -d '{"customerId":"ACME-42","currency":"EUR","lines":[{"description":"Hosting","quantity":3,"unitPrice":19.99}]}'
```

## Тесты

- `./gw test` — unit-тесты;
- `scripts/contract-check.sh` — поднимает PostgreSQL и сервис, прогоняет контрактные тесты `src/contractTest` и останавливает сервис;
- `TARGET_BASE_URL=http://host:port ./gw contractTest` — контрактные тесты против уже запущенного сервиса.

Контрактные тесты — внешний HTTP-клиент без знания о Spring. Они фиксируют поведение API, на которое опираются клиенты. Это критерий приёмки миграции.

## Остановка

```bash
docker compose down -v
```
