package net.zyuiop.discordbot.commands;

import net.zyuiop.discordbot.DiscordBot;
import net.zyuiop.discordbot.Helpers;
import sx.blah.discord.handle.obj.IMessage;

import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

// Created by Loris Witschard on 28.10.16.

public class InsultCommand extends DiscordCommand {
	private List<String> nouns = new ArrayList<>();
	private List<String> intro = Arrays.asList(
			"Espèce de", "Sale", "Bande de", "Tête de", "Pauvre", "Connard de",
			"Saloperie de", "Saleté de", "Putain de", "Sous-merde de", "Couillon de",
			"Stupide", "Abruti de", "Enfoiré de"
	);

	public InsultCommand() throws Exception {
		super("insult", "génère une insulte aléatoire");

		String filename = "rsrc/noms.txt";

		File file = new File(filename);
		if (!file.exists()) {
			Helpers.extractFile("/rsrc/noms.txt", file);
			if (!file.exists()) {
				System.out.println("Error: could not open file *" + filename + "*.");
				return;
			}
		}

		Scanner fileStream = new Scanner(file);

		while (fileStream.hasNextLine()) { nouns.add(fileStream.nextLine()); }

		System.out.println("Successfully loaded " + nouns.size() + " nouns.");
	}

	@Override
	public void run(IMessage message) throws Exception {
		String answer = exec(message.getContent().toLowerCase().split("[\\s]+"));
		DiscordBot.sendMessage(message.getChannel(), answer);
	}

	public String exec(String[] args) {
		final String userRegex = "<@[\\d]+>";

		if (args.length == 1)
			return generateInsult();

		switch (args[1]) {
			case "count":
				if (args.length != 2)
					break;

				return "Il y a " +
						NumberFormat.getNumberInstance(Locale.FRENCH).format(
							intro.size() * nouns.size() * (nouns.size() - 1)) +
						" combinaisons d'insultes possibles (sans les villes).";

			case "city":
				if (args.length != 2 && args.length != 4)
					break;
				if (args.length == 4 && (!args[2].equals("to") || !args[3].matches(userRegex)))
					break;

				return (args.length == 4 ? args[3] + " " : "") + generateCityInsult();

			case "to":
				if (args.length != 3 && args.length != 4)
					break;
				if(args.length == 4 && (!args[3].equals("city")))
					break;
				if(!args[2].matches(userRegex))
					break;

				return args[2] + " " + (args.length == 4 ? generateCityInsult() : generateInsult());

			case "help":
				return	"*Générateur d'insulte aléatoire v1.0.2*\n" +
						"*par Loris Witschard*\n\n" +
						"**Utilisation** :\n" +
						"`!insult` : génère une insulte\n" +
						"`!insult to @user` : insulte l'utilisateur *@user*\n" +
						"`!insult city` : insulte dans une ville aléatoire\n" +
						"`!insult count` : affiche le nombre d'insultes possibles\n" +
						"`!insult help` : affiche l'aide";
		}

		return "*Erreur de syntaxe.*";
	}

	private String generateInsult() {
		String insult = Helpers.getRandomItem(intro);

		String w1 = Helpers.getRandomItem(nouns);
		if (Helpers.isVowel(w1.charAt(0)) && insult.substring(insult.length() - 3, insult.length()).equals(" de")) {
			insult = insult.substring(0, insult.length() - 1) + "'" + w1;
		} else { insult += " " + w1; }

		String w2 = Helpers.getRandomItem(nouns);
		insult += (Helpers.isVowel(w2.charAt(0)) ? " d'" : " de ") + w2 + " !";

		return insult;
	}

	private String generateCityInsult() {
		String insult = generateInsult();
		insult = insult.substring(0, insult.length() - 1);
		return insult + "à " + CityCommand.generateCityName() + " !";
	}
}