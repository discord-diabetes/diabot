import javax.security.auth.login.LoginException;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

  private static Logger logger = LoggerFactory.getLogger(Main.class);

  public static void main(String[] args) {

    //TODO: configure logging levels

    String token = System.getenv("discord-rolebot-token");

    try {
      JDA jda = new JDABuilder(token).addEventListener(new RoleListener()).build().awaitReady();
    } catch (LoginException | InterruptedException ex) {
      logger.error(ex.getMessage());
    }

  }

}
