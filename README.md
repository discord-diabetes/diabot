# Diabot

A diabetes bot for Discord

## Adding Diabot to your server
Use [this invite link](https://discordapp.com/oauth2/authorize?client_id=260721031038238720&scope=bot&permissions=403008576&guild_id=0)


## Running Diabot
To run Diabot, you need access to the following: 
1. A Discord bot account
2. A Redis database
3. A test server on Discord

### Creating a Discord bot account
1. Visit https://discordapp.com/developers/applications/
2. Create a new application ![](/docs/create_application.png)
3. In the menu to the side, click the "bot button"
4. On the bot page, click the "create bot account" button ![](/docs/build_a_bot.png)
5. Copy the newly created token into an environment variable called `DIABOTTOKEN` ![](/docs/copy_token.png)

### Setting up a Redis database
TODO.

In the end, you will need a connection URL like this: `redis://<username>:<password>@<host>:<port>`. Store this connection string in an environment variable named `REDIS_URL` or `DIABOT_REDIS_URL`

### Inviting Diabot to your server
To invite Diabot to your test server, create an invitation URL by replacing `<client_id>` in the following URL with the client ID 

`https://discordapp.com/oauth2/authorize?&client_id=<client_id>&scope=bot&permissions=0`

![](/docs/copy_id.png)

## Logging

To change your local logging configuration, duplicate `logback.xml` (in the main resources folder) to a file called `logback-text.xml` and modify that file. 
Please do not include this modified file in any packaged jar for deployment.
