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
