# Nightscout tokens and Diabot

If your Nightscout instance is running
in [token authentication mode](https://nightscout.github.io/nightscout/security/#how-to-turn-off-unauthorized-access), and you want to use
the Nightscout features of the bot, you'll need to set an access token so Diabot access and read your Nightscout data.
You can read more about creating a token [here](https://nightscout.github.io/nightscout/security/#create-authentication-tokens-for-users)

## Quick Guide

1. Starting at your Nightscout page, click the 3 horizontal lines in top right -> "Admin Tools"
2. Click "Add new Subject". Put anything for name (e.g. `diabot`) and put `readable` for roles. Click "Save" in the bottom left.
3. In the "Access Token" column, right-click on the diabot token you just created and copy the link.
4. In Discord, type `/nightscout set url` and click on the command that appears above the chat box. Paste the link you just copied into
   the `url` input field for that command. Send the command.
5. Run `diabot ns` in the chat to confirm it's working.

## Regular Guide

1. Open your Nightscout page. In the top right, click on the 3 horizontal lines, and click "Admin Tools"

   ![Picture showing the location of the admin tools link](./images/token/step_1.png?raw=true)

2. Once in the admin tools page, click "Add new Subject" which will open a menu.

   ![Picture of the "Add new Subject" button](./images/token/step_2.png?raw=true)

3. You can put anything for the name, but in this example we'll use `diabot`. For the roles, put in `readable`. Once you've confirmed the
   values look correct, click "Save" in the bottom-left of the menu.

   ![Picture showing an example of how the subject fields should look](./images/token/step_3.png?raw=true)

4. After saving it, the new Diabot subject should appear in the table. You can see the new token under the "Access Token" column; it'll
   appear as a link to your Nightscout with the token filled in. Right-click on the token link and copy it to your clipboard.

   ![Picture of the access token link](./images/token/step_4.png?raw=true)

5. Go over to Discord. Make sure you're in a server with Diabot in it. In the chat box, type `/nightscout set url` and click on the command
   that appears above the chat box. Your cursor should be in a command option/field box now. Paste the link we copied in step 4 into the
   box.
   
   ![Picture of the command to select](./images/token/step_5-1.png?raw=true)
   ![Picture of the command with the URL option filled out](./images/token/step_5-2.png?raw=true)

6. Once you have the link in the command field as shown above, send the command by hitting enter or clicking on the send button. Diabot
   should update your URL and you can now test to make sure it works by running `diabot ns`.
