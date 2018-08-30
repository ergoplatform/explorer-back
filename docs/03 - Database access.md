## Database access

All database related code are based in `org.ergoplatform.explorer.db`
Which consists number of subpackages
* `dao` contains all dao and ops classes. Ops class is define operations for related models. Dao class is using this operations to cimbine or just to call them.
* `models` contains all models that being stored in db
* `mappings` contains mappings for custom data type. Right now there is only one mapping for storing `io.circe.Json` as `JSONB` in postgres
