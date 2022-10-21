# Diabot

A diabetes bot for Discord

## Adding Diabot to your server
Use [this invite link](https://discord.com/oauth2/authorize?client_id=260721031038238720&scope=applications.commands+bot&permissions=2550492224)

## Administration documentation
See documentation for admin commands [here](docs/administration.md).

## Sponsors
Diabot is a public Discord bot. This means that anyone can invite the bot to their server. 
Hosting a public bot isn't free. Diabot requires a machine to run on, and a database. Both of these things cost money. 
While there is a budget to ensure Diabot can continue running without any financial support from others, we do appreciate contributions. 
Financial contributions are used to pay for hosting costs, and (when the budget allows this) run a separate test version of Diabot, so we can better test it. 
When money is left over after all hosting fees are paid, this money will be saved for when contributions can't fully support the project.

If you wish to help pay for diabot hosting and development, you can [sponsor cascer1 on GitHub](https://github.com/sponsors/cascer1).

Any support received will be used to pay for the hosting and improvement of Diabot. This is not a for-profit project.

## Running Diabot

### As Java application

Development for Diabot is aimed at a Java 11 environment. Diabot may not function correctly when using other version of java.

If you have Gradle installed, you can run the following two commands to quickly start Diabot:

```shell
gradle stage
java -jar build/libs/diabot.jar
```

For detailed instructions, see [Running Diabot](docs/running.md)

### As Docker container
Diabot releases are automatically published to the GitHub Container Registry. So, you can simply launch a new container to get up and running quickly:

```shell
docker run -e DIABOTTOKEN='token' ghcr.io/reddit-diabetes/diabot:latest
```

For detailed instructions and information about other environment variables, see [Running Diabot](docs/running.md)

## Contributing
Thank you for your interest in contributing to the development of Diabot. 

We use GitHub issues to keep track of bugs and feature requests. Before opening a new issue, please check whether that issue has already been reported.

Pull Requests are welcome. When you submit a pull request, please make sure that you have tested your changes to ensure nothing is broken. 
On top of that, please describe your changes in the pull request. When your pull request is related to an issue, please mention this in the description. 
The description doesn't have to be anything fancy, so long as it helps the maintainers understand what the changes do.

If you have found a security issue, please report them immediately. See [SECURITY.md](SECURITY.md) for more information
