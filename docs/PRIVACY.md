# Diabot privacy information

By default, Diabot does not store any information about used commands or received messages.

In the following cases, data is stored:

1. You use any of the `/nightscout set ...` commands. This instructs the bot to store nightscout data such as your URL or an access token.
2. You or someone else creates a quote from a message you sent. The bot will then store the message content, as well as your name and user ID. Use `diabot quote list` to view your quotes, and `diabot quote delete` to remove them.

You can always remove your data using the commands in the bot. If this doesn't work, please join the support server at https://discord.gg/VJTqWxXeXf and ask someone there to remove the data for you.

Diabot will NEVER store your blood glucose values, A1c estimations, or any other medical data. The data may be retrieved from your Nightscout if you ask Diabot to do this, but will not be stored after Diabot has responded to your query.
