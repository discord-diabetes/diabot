# Diabot

A diabetes bot for Discord

## Adding Diabot to your server
Use [this invite link](https://discordapp.com/oauth2/authorize?client_id=260721031038238720&scope=bot&permissions=403008576&guild_id=0)

## Sponsors
Since making Diabot public, we have set higher quality goals for the bot. Ensuring we meet this quality requires us to run a separate testing version of the bot. Furthermore, since the bot was made public it costs more to host.
If you want to support the development efforts and cover the hosting fees, you can choose to [sponsor me](https://github.com/sponsors/cascer1) (@cascer1) on github, or [send crypto.](https://reddit-diabetes.github.io/diabot/crypto).

Any support received will be used to pay for the hosting and improvement of Diabot. This is not a for-profit project.

## Running Diabot
To run Diabot, you need access to the following: 
1. A Discord bot account
2. A MongoDB database
3. A test server on Discord

### Creating a Discord bot account
1. Visit https://discordapp.com/developers/applications/
2. Create a new application ![](/docs/create_application.png)
3. In the menu to the side, click the "bot button"
4. On the bot page, click the "create bot account" button ![](/docs/build_a_bot.png)
5. Copy the newly created token into an environment variable called `DIABOTTOKEN` ![](/docs/copy_token.png)

### Setting up a MongoDB database
Instructions for installing a MongoDB community edition server [can be found here](https://docs.mongodb.com/manual/administration/install-community/).

In the end, you will need a [connection URL](https://docs.mongodb.com/manual/reference/connection-string/#connections-standard-connection-string-format) like this: `mongodb://<username>:<password>@<host>:<port>/`. Store this connection string in an environment variable named `MONGO_URI`

### Inviting Diabot to your server
To invite Diabot to your test server, create an invitation URL by replacing `<client_id>` in the following URL with the client ID 

`https://discordapp.com/oauth2/authorize?&client_id=<client_id>&scope=bot&permissions=0`

![](/docs/copy_id.png)

## Logging

To change your local logging configuration, duplicate `logback.xml` (in the main resources folder) to a file called `logback-text.xml` and modify that file. 
Please do not include this modified file in any packaged jar for deployment.

## Migrating data from Redis to MongoDB
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
