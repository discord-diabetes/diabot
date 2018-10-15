package com.dongtronic.diabot.commands;

import com.dongtronic.diabot.exceptions.NoSuchEpisodeException;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.doc.standard.CommandInfo;
import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import net.dv8tion.jda.core.EmbedBuilder;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Attribute;
import org.jdom2.Element;

import java.io.IOException;
import java.net.URL;
import java.util.List;

@CommandInfo(
    name = {"Diacast"},
    description = "Get information about a diacast episode"
)
public class DiacastCommand extends DiabotCommand {

  public DiacastCommand(Category category) {
    this.name = "diacast";
    this.help = "Get information about a diacast episode";
    this.guildOnly = true;
    this.category = category;
    this.examples = new String[]{"diabot diacast", "diabot diacast 6"};
  }

  private static String url = "https://diacast.xyz/?format=rss";

  @Override
  protected void execute(CommandEvent event) {
    try {
      String[] args = event.getArgs().split("\\s+");
      int episodeNumber = 0;

      if(args.length > 0 && StringUtils.isNumeric(args[0])) {
        episodeNumber = Integer.valueOf(args[0]);
      }

      SyndEntry episode = getEpisode(episodeNumber);

      EmbedBuilder builder = new EmbedBuilder();

      buildEpisodeCard(episode, builder);

      event.reply(builder.build());

    } catch (Exception ex) {
      event.replyError("Something went wrong: " + ex.getMessage());
    }
  }

  private void buildEpisodeCard(SyndEntry episode, EmbedBuilder builder) {
    builder.setTitle(episode.getTitle(), episode.getLink());
    builder.setAuthor("Diacast");

    for(Element element : episode.getForeignMarkup()) {
      if (element.getName().equals("summary")) {
        builder.setDescription(element.getValue());
      }

      if (element.getName().equals("image")) {
        String imageUrl = element.getAttributeValue("href");
        builder.setThumbnail(imageUrl);
      }

    }
  }

  private List<SyndEntry> getEpisodes() throws FeedException, IOException {
    URL feedSource = new URL(url);
    SyndFeedInput input = new SyndFeedInput();
    SyndFeed feed = input.build(new XmlReader(feedSource));
    return feed.getEntries();
  }

  private SyndEntry getEpisode(int episode) throws NoSuchEpisodeException, IOException, FeedException {
    List<SyndEntry> episodes = getEpisodes();
    if(episode == 0) {
      return episodes.get(0);
    }

    for(SyndEntry entry : episodes) {
      for(Element element : entry.getForeignMarkup()) {
        if(element.getName().equals("episode")) {
          String number = element.getValue();
          if(Integer.valueOf(number).equals(episode)) {
            return entry;
          }
        }
      }
    }

    throw new NoSuchEpisodeException();
  }


}
