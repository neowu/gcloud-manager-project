### 0.0.4 (09/23/2022 - 10/17/2022)

* finalize User design, with "auth" and "role"

> e.g.
> {"name": "viewer", "auth": "PASSWORD", "secret": "viewer-password", "role": "VIEWER"},
> {"name": "build", "auth": "IAM", "role": "MIGRATION"},
> {"name": "data-warehouse-service", "auth": "IAM", "role": "APP", "db": "data_warehouse"},
> {"name": "data-stream", "auth": "PASSWORD", "secret": "data-stream-password", "role": "REPLICATION"}

### 0.0.3

* add "IAM" type user, to grant permission only
  > in the future, each user has type (IAM or not), role (privileges), and db (specific or '*')
* not grant permission for "*" db anymore, grant for each db explicitly
  > gcloud mysql will mess up the permission and produces warnings like
  > [Warning] [MY-013371] [Server] For user `build`@`%`, ignored restrictions for privilege(s) 'CREATE TEMPORARY TABLES, LOCK TABLES, CREATE VIEW, CREATE ROUTINE, ALTER ROUTINE' for database 'sys' as corresponding global privilege(s) are not granted."

### 0.0.2

* add "env" on db config, to label secret
* add "db-migrate-secret-label" command, to add labels to existing secret
