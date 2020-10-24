# Migrating data from Redis to MongoDB

Diabot formerly used Redis as a storage backend, however it now uses MongoDB in place of it.
As a result, the data structure used for persistent storage was modified and is not directly compatible with Redis data.

If you used to have data stored in Redis and would like to convert it, Diabot comes bundled with a migration tool to automate the process of moving your Redis data to a MongoDB database.

There are a few caveats to consider when using the migration tool:
- Although the migration tool has been tested, there may still be issues with reading data from Redis and/or writing data to MongoDB. It is **highly** recommended that you take a backup of your Redis data before proceeding. 
- Data which is already present in MongoDB may be skipped and produce errors if their snowflakes (message, user, guild, etc) are equal to the ones stored in Redis. For this reason, you should ensure the database used by Diabot is empty prior to migrating.
- If your Redis database is large, the migration process may take a while and impact other programs using the same Redis instance.
- Migrating the data may take a fair amount of network usage on both the Redis and Mongo ends. If you are running them locally then this shouldn't be an issue.
- Due to how the migration process works, most data must be loaded into memory before it can be converted and written to MongoDB.

The migration tool is disabled by default and can be enabled by setting an environment variable named `REDIS_MONGO_MIGRATE` to `true`. Ensure you have both `REDIS_URL` **and** `MONGO_URI` set as environment variables. 

Once the bot has launched, the migration should start. After it finishes, the bot will continue launching. You should remove the `REDIS_MONGO_MIGRATE` variable at this point otherwise it may attempt to migrate data at each launch.
