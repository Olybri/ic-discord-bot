package net.zyuiop.discordbot.commands;

import net.zyuiop.discordbot.DiscordBot;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import sx.blah.discord.handle.obj.IMessage;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static org.junit.Assert.assertEquals;

// Created by Saralfddin on 31.10.16.
// Edited by Loris Witschard on 01.11.16.

enum Source { AJ, ETI }

public class HorseHeadCommand extends DiscordCommand
{
    private Map<Source, String> next = new HashMap<>();
    private Source src = Source.AJ;

    public HorseHeadCommand() throws Exception
    {
        super("hhh", "affiche un contenu de Horse Head Huffer");

        next.put(Source.AJ, initURL("http://www.anti-joke.com", 5));
        next.put(Source.ETI, initURL("http://www.explainthisimage.com", 6));
    }

    @Override
    public void run(IMessage message) throws Exception
    {
        String answer = exec(message.getContent().toLowerCase().split("[\\s]+"));
        DiscordBot.sendMessage(message.getChannel(), answer);
    }

    public String exec(String[] args) throws Exception
    {
        if(args.length > 2)
            args = new String[]{"", "err"};

        else if(args.length == 1)
            args = new String[]{"", "help"};

        switch(args[1])
        {
            case "aj":
                src = Source.AJ;
                break;

            case "eti":
                src = Source.ETI;
                break;

            case "help":
                return	"*Afficheur de contenu Horse Head Huffer v1.0.1*\n" +
                        "*par Saralfddin & Loris Witschard*\n\n" +
                        "**Utilisation** :\n" +
                        "`!hhh aj` : affiche une blague d'*anti-joke.com*\n" +
                        "`!hhh eti` : affiche une image d'*explainthisimage.com*\n" +
                        "`!hhh help` : affiche l'aide";

            default:
                return "*Erreur de syntaxe.*";
        }

        Document doc = Jsoup.connect(next.get(src)).get();

        String content = getContent(doc) + "\n\nScore: " + getScore(doc);
        next.replace(src, getNext(doc));

        return content;
    }

    private String initURL(String url, int postNbScale) throws Exception
    {
        boolean success = false;
        String nextUrl = "";
        int postNb = 0;
        int attempt = 1;

        Random rand = new Random();
        int max = (int)Math.pow(10, postNbScale);
        int min = (int)Math.pow(10, postNbScale-1);

        while(!success)
        {
            System.out.print("\rLooking for a valid post in " + url + "... ");
            if(attempt > 1)
                System.out.print("(attempt " + attempt + ") ");

            postNb = rand.nextInt(max - min) + min;
            nextUrl = url + "/posts/" + postNb;
            try
            {
                HttpURLConnection connexion = (HttpURLConnection) new URL(nextUrl).openConnection();
                connexion.connect();
                assertEquals(HttpURLConnection.HTTP_OK, connexion.getResponseCode());
                success = true;
            }
            catch(AssertionError e)
            {
                ++attempt;
            }
        }

        System.out.println("Success! (" + postNb + ")");

        return nextUrl;
    }

    private String getNext(Document doc)
    {
        return doc.select("a:contains(Random)").first().attr("abs:href");
    }

    private String getContent(Document doc)
    {
        switch(src)
        {
            case AJ:
                return "*" + doc.select("h3.content").first().text() + "*";

            case ETI:
                return doc.select("link[rel=image_src]").first().attr("abs:href") + "\n\n"
                        + "Top comment: *" + doc.select("h1.h1").first().text() + "*";

        }
        return "";
    }

    private String getScore(Document doc)
    {
        String score;

        switch(src)
        {
            case AJ:
                score = doc.select("span.value").first().text();
                if(score.length() == 0)
                    return "**0**";
                int value = Integer.parseInt(score);
                return value < 0 ? "**" + value + "** :thumbsdown:" : "**+" + value + "** :thumbsup:";

            case ETI:
                score = doc.select("small.value").first().text();
                if(score.length() == 0)
                    return "**0/5**";
                double average = Double.parseDouble(score.split(" ")[0]);
                return "**" + average + "/5** (" + score.split(" ")[2] + " votes)";
        }
        return "";
    }
}
