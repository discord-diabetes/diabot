package com.dongtronic.diabot.commands;

import com.jagrosh.jdautilities.command.Command;

public abstract class DiabotCommand extends Command {
  protected String[] examples = new String[0];

  public String[] getExamples() {
    return examples;
  }
}
