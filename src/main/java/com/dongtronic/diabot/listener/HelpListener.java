package com.dongtronic.diabot.listener;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

import com.dongtronic.diabot.commands.DiabotCommand;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ChannelType;

public class HelpListener implements Consumer<CommandEvent> {
	@Override
	public void accept(CommandEvent event) {
		if(!event.isFromType(ChannelType.TEXT)) {
			// Don't accept DM help requests, since we can't check permissions there
			event.replyError("Help command can only be used inside a server");
			return;
		}

		EmbedBuilder embedBuilder = new EmbedBuilder();

		// event == com.jagrosh.jdautilities.command.CommandEvent
		List<Command> allCommands = event.getClient().getCommands();

		if(event.getArgs().length() == 0) {
		  buildGeneralHelp(embedBuilder, allCommands, event);
    } else {
		  buildSpecificHelp(embedBuilder, allCommands, event);
    }




		event.reply(embedBuilder.build());
	}

	private void buildGeneralHelp(EmbedBuilder builder, List<Command> allCommands, CommandEvent event) {
    List<Command> allowedCommands = filterAllowedCommands(allCommands, event);
    TreeMap<String, ArrayList<Command>> categorizedCommands = groupCommands(allowedCommands);


    for(Map.Entry<String,ArrayList<Command>> category : categorizedCommands.entrySet()) {
      buildCategoryHelp(builder, category);
    }
  }

  private void buildSpecificHelp(EmbedBuilder builder, List<Command> allCommands, CommandEvent event) {
	  String[] args = event.getArgs().split("\\s+");
	  String commandName = args[0];
	  boolean found = false;

	  for(Command command : allCommands) {
	    if(command.getName().toUpperCase().equals(commandName.toUpperCase())) {
	      buildExtendedCommandHelp(builder, command);
	      found = true;
	      break;
      }
    }

    if(!found) {
      builder.setTitle("error");
      builder.addField("Error", "Command " + commandName + " does not exist", false);
      builder.setColor(Color.red);
    }
  }

	private void buildCategoryHelp(EmbedBuilder builder, Map.Entry<String, ArrayList<Command>> category) {
		String categoryName = category.getKey();
		ArrayList<Command> commands = category.getValue();

		builder.appendDescription("**" + categoryName + "**\n");

		for(Command command : commands) {
			buildCommandHelp(builder, command);
		}

		builder.appendDescription("\n");
	}

  /**
   * Build a basic command help line
   */
	private void buildCommandHelp(EmbedBuilder builder, Command command) {
		builder.appendDescription(command.getName());
		if(command.getArguments() != null ) {
			builder.appendDescription(command.getArguments());
		}

		builder.appendDescription(" => ");
		builder.appendDescription(command.getHelp());
		builder.appendDescription("\n");
	}

  /**
   * Build extended command help card. This includes arguments, permissions, aliases, and examples
   */
	private void buildExtendedCommandHelp(EmbedBuilder builder, Command command) {
	  boolean isExtendedCommand = command instanceof DiabotCommand;
	  DiabotCommand extendedCommand = null;
	  if(isExtendedCommand) {
      extendedCommand = (DiabotCommand) command;
    }

    builder.setTitle("Help");
	  builder.setColor(Color.magenta);
	  builder.addField("Name", command.getName(), false);
	  builder.addField("Description", command.getHelp(), false);

	  if(command.getArguments() != null) {
      builder.addField("Arguments", command.getArguments(), false);
    }

	  if(command.getUserPermissions().length > 0) {
	    builder.addField("Required permissions", Arrays.toString(command.getUserPermissions()), false);
    }

	  if(command.getAliases().length > 0) {
	    builder.addField("Aliases", Arrays.toString(command.getAliases()), false);
    }

    if(isExtendedCommand && extendedCommand.getExamples().length > 0) {
      StringBuilder examples = new StringBuilder();

      for(String example : extendedCommand.getExamples()) {
        examples.append("`").append(example).append("`\n");
      }

      builder.addField("Examples", examples.toString(), true);
    }
	}

	/**
	 * Returns a list of only the commands a user has permission to use
	 * @param commands list of all commands
	 * @param event original CommandEvent. Used for checking permissions
	 * @return list of commands the user is authorized to use
	 */
	private ArrayList<Command> filterAllowedCommands(List<Command> commands, CommandEvent event) {
		ArrayList<Command> allowedCommands = new ArrayList<>();

		for (Command command : commands) {
			Permission[] requiredPermissions = command.getUserPermissions();

			if(requiredPermissions.length == 0) {
				allowedCommands.add(command);
				continue;
			}

			List<Permission> userPermissions = event.getMember().getPermissions();

			boolean userIsAllowedToUseCommand = true;

			for(Permission requiredPermission: requiredPermissions) {
				if(!userPermissions.contains(requiredPermission)) {
					userIsAllowedToUseCommand = false;
				}
			}

			if(userIsAllowedToUseCommand) {
				allowedCommands.add(command);
			}

		}

		return allowedCommands;
	}

	/**
	 * Returns a collection of commands grouped by category
	 * @param commands list of commands that need to be sorted
	 * @return collection of grouped and sorted commands
	 */
	private TreeMap<String, ArrayList<Command>> groupCommands(List<Command> commands) {
		TreeMap<String, ArrayList<Command>> categorizedCommands = new TreeMap<>();

		for (Command command : commands) {
			String categoryName = "Misc";
			try {
				categoryName = command.getCategory().getName();
			} catch (NullPointerException ex) {
				// Ignored on purpose
			}
			ArrayList<Command> categoryCommands = categorizedCommands.get(categoryName);

			if(categoryCommands == null) {
				categoryCommands = new ArrayList<>();
			}

			categoryCommands.add(command);

			categorizedCommands.put(categoryName, categoryCommands);
		}
		return categorizedCommands;

	}
}
