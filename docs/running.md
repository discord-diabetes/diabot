# Running Diabot

To run Diabot, you need access to the following: 
1. A Discord bot account
2. A MongoDB database
3. A test server on Discord

Diabot primarily uses environment variables for core configuration. A list of them can be found below.

<details>
<summary>Environment variables</summary>
  
| Environment variable            | Default        | Required | Description                                                                                                                                                                  |
|---------------------------------|----------------|----------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| DIABOTTOKEN                     | N/A            | Yes      | Discord bot token.                                                                                                                                                           |
| MONGO_URI                       | N/A            | Yes      | [MongoDB connection string](https://docs.mongodb.com/manual/reference/connection-string/).                                                                                   |
| MONGO_DATABASE                  | diabot         | No       | The MongoDB database name used by Diabot.                                                                                                                                    |
| MONGO_CONNECTIONS               | 30             | No       | The maximum amount of connections that can be opened by the connection pool.                                                                                                 |
| MONGO_CHANNELS_COLLECTION       | channels       | No       | The MongoDB collection name for channel attribute storage.                                                                                                                   |
| MONGO_GRAPH_DISABLE_COLLECTION  | graph-disable  | No       | The MongoDB collection name for nightscout graph disables per guild.                                                                                                         |
| MONGO_NAME_RULES_COLLECTION     | name-rules     | No       | The MongoDB collection name for guild username rules.                                                                                                                        |
| MONGO_NIGHTSCOUT_COLLECTION     | nightscout     | No       | The MongoDB collection name for nightscout data.                                                                                                                             |
| MONGO_PROJECTS_COLLECTION       | projects       | No       | The MongoDB collection name for project/information storage.                                                                                                                 |
| MONGO_QUOTE_INDEX_COLLECTION    | quote-index    | No       | The MongoDB collection name for guilds' quote indexes.                                                                                                                       |
| MONGO_QUOTES_COLLECTION         | quotes         | No       | The MongoDB collection name for quotes.                                                                                                                                      |
| MONGO_REWARDS_COLLECTION        | rewards        | No       | The MongoDB collection name for guild reward storage.                                                                                                                        |
| MONGO_REWARDS_OPTOUT_COLLECTION | rewards-optout | No       | The MongoDB collection name for users who have opted-out of a guild's rewards.                                                                                               |
| REDIS_MONGO_MIGRATE             | N/A            | No       | Controls whether the Redis migration system is enabled. If this is enabled (set to `true`), you must also set `REDIS_URL` to a Redis server.                                 |
| QUOTE_ENABLE_GUILDS             | N/A            | No       | Comma-separated list of Discord guild IDs which is used to grant permission to the quote system on guilds. By default, all guilds are forbidden from using the quote system. |
| QUOTE_MAX                       | 5000           | No       | Sets the maximum amount of quotes each guild can store.                                                                                                                      |
| QUOTE_MAX_SEARCH_DISPLAY        | 10             | No       | Sets the maximum number of quotes a search will show.
| HOME_GUILD_ID                   | N/A            | No       | Grants a guild, provided by its ID, permission to run certain commands (`info set`, `info delete`, `na delete`, `na set`).                                                   |
| HOME_GUILD_MESSAGE              | N/A            | No       | If users attempt to run the above commands, this message will be sent in response.                                                                                           |
| DIABOT_DEBUG                    | N/A            | No       | If this is set, the bot's command prefix will be changed to `dl` from `diabot`. This is to help with running a test instance alongside the main Diabot.                      |
| superusers                      | N/A            | No       | Comma-separated list of Discord user IDs which are permitted to use `diabot shutdown`.                                                                                       |
| nutritionixappid                | N/A            | No       | Sets the Nutritionix app ID for `diabot nutrition`.                                                                                                                          |
| nutritionixsecret               | N/A            | No       | Sets the Nutritionix secret for `diabot nutrition`.                                                                                                                          |
</details>

### Creating a Discord bot account
1. Visit https://discordapp.com/developers/applications/
2. Create a new application ![](/docs/images/create_application.png)
3. In the menu to the side, click the "bot button"
4. On the bot page, click the "create bot account" button ![](/docs/images/build_a_bot.png)
5. Copy the newly created token into an environment variable called `DIABOTTOKEN` ![](/docs/images/copy_token.png)

### Setting up a MongoDB database
Instructions for installing a MongoDB community edition server [can be found here](https://docs.mongodb.com/manual/administration/install-community/).

In the end, you will need a [connection URL](https://docs.mongodb.com/manual/reference/connection-string/#connections-standard-connection-string-format) like this: `mongodb://<username>:<password>@<host>:<port>/`. Store this connection string in an environment variable named `MONGO_URI`

### Inviting Diabot to your server
To invite Diabot to your test server, create an invitation URL by replacing `<client_id>` in the following URL with the client ID 

`https://discordapp.com/oauth2/authorize?&client_id=<client_id>&scope=bot&permissions=0`

![](/docs/images/copy_id.png)

## Logging

To change your local logging configuration, duplicate `logback.xml` (in the main resources folder) to a file called `logback-text.xml` and modify that file. 
Please do not include this modified file in any packaged jar for deployment.
