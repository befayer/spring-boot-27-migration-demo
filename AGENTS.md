# Repository instructions

This repository is a production service being upgraded from Spring Boot 2.7 to Spring Boot 3.5.

Read `docs/MIGRATION.md` before changing code. It defines the scope and the acceptance criteria.

`src/contractTest` is the executable API contract used by real clients. Never modify, delete, disable, or weaken these tests or their expectations to make the upgraded service pass. If a contract test fails, fix the application. If you believe a contract expectation is wrong, stop and report the exact conflict instead of changing it.

Do not change the public API, response formats, status codes, or headers.

Use Gradle Kotlin DSL only. Run builds through `./gw`, never a system Gradle installation.

Run `scripts/contract-check.sh` to verify the service. It starts PostgreSQL and the service, runs the contract tests, and stops the service.
