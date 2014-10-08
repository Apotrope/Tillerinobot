package tillerino.tillerinobot.lang;

import java.util.List;
import java.util.Random;

import org.tillerino.osuApiModel.Mods;
import org.tillerino.osuApiModel.OsuApiUser;

import tillerino.tillerinobot.BeatmapMeta;
import tillerino.tillerinobot.BeatmapMeta.PercentageEstimates;
import tillerino.tillerinobot.IRCBot.IRCBotUser;
import tillerino.tillerinobot.RecommendationsManager.Recommendation;

public class Tsundere implements Language {
	
	//Random object, used in StringShuffler
	static final Random rnd = new Random();
	//Recent counters, reset if it's been over a day
	int recentRecommendations = 0;
	int recentHugs = 0;
	
	@Override
	public void welcomeUser(IRCBotUser user, OsuApiUser apiUser, long inactiveTime) {
		//TODO potentially more replies + shuffler
		if (inactiveTime < 60 * 1000) {
			user.message("What is this? Peekaboo?");
		} else if (inactiveTime < 24 * 60 * 60 * 1000) {
			user.message("Back again? I'm just here because I have nothing else to do! Don't read into it!");
		} else {
			recentRecommendations = 0;
			recentHugs = 0;
			user.message("Where have you been, " + apiUser.getUsername()
					+ "!? I-it's not like I missed you or anything...");
		}
	}

	@Override
	public String unknownBeatmap() {
		return "Are you stupid? No one plays that map!";
	}

	@Override
	public String internalException(String marker) {
		return "Huh? Why isn't this working? I can't imagine this being anything other than your fault."
		+ " Mention incident " + marker + " to @Tillerino or /u/Tillerino if this keeps happening.";
	}

	@Override
	public String externalException(String marker) {
		return "Sorry, the osu! server was saying some idiotic nonsense and I felt like slapping them instead of you. Try asking whatever it was again."
		+ " If the server doesn't shut up, ask @Tillerino or /u/Tillerino (reference " + marker + ") to take care of it.";
	}

	@Override
	public String noInformationForModsShort() {
		return "Those mods? You wish!";
	}

	@Override
	public String noInformationForMods() {
		//TODO potentially more replies + shuffler
		return "What!? You can't possibly expect me to know the answer to that!";
	}

	@Override
	public String unknownCommand(String command) {
		return command + "? I think you've got the hierarchy backwards. You do what I tell you, and I respond if I feel like it. Type !help if you're too stupid to even tell what I want.";
	}

	@Override
	public String malformattedMods(String mods) {
		return "You dummy... you can't just make up your own mods. If you can't write normal things like !with HR or !with HDDT, I won't even to bother trying to interpret.";
	}

	@Override
	public String noLastSongInfo() {
		return "You didn't even mention a song. Wait, were you trying to use those mods on ME!?";
	}

	StringShuffler anyMods = new StringShuffler(rnd);

	@Override
	public String tryWithMods() {
		return anyMods.get(
			"An idiot like you wouldn't know to try this with mods. You should thank me.",
			"I almost think you could use mods here without making a complete fool of yourself."
		);
	}

	@Override
	public String tryWithMods(List<Mods> mods) {
		return "Use " + Mods.toShortNamesContinuous(mods) + "... or else.";
	}

	@Override
	public String excuseForError() {
		return "Did you say something? It's not l-like I care what you have to say, but you should say it again so you can pretend I do.";
	}

	@Override
	public String complaint() {
		return "Whaaaat!? How could you say something like... oh, that beatmap? Actually that's there because I hated it and wanted to test you. Aren't you glad having something in common with me?";
	}
	
	@Override
	public void hug(IRCBotUser user, OsuApiUser apiUser) {
		recentHugs++;
		/*TODO Responses move from tsun to dere with more hug attempts (and maybe recommendations)
		here's something on your back, you slob. Here, let me get that
		Wow, you suck at hugs. Someone needs to teach you. 
		I w-wasn't trying to hug you! I just lost my balance for a second and fell onto you.
		I'm not hugging you, I'm taking a rough chest measurement, because you clearly don't know what size to wear. 
		Come here, you! /me slaps [user]
		/me slaps. Sorry, that was just a reflex.
		*/
		user.action("slaps " + apiUser.getUsername());
	}

	@Override
	public String help() {
		return "Feeling helpless (as always)?  Check https://twitter.com/Tillerinobot for status and updates, and https://github.com/Tillerino/Tillerinobot/wiki for commands. Where would you be without me here to rescue you?";
	}

	@Override
	public String faq() {
		return "Really, every answer on this list should be intuitively obvious, but it's understandable if -you- need to read it: https://github.com/Tillerino/Tillerinobot/wiki/FAQ";
	}

	StringShuffler fakeRecommendationInsult = new StringShuffler(rnd);
	StringShuffler fakeRecommendationTroll = new StringShuffler(rnd);
	boolean fakeRecommendationSwitch = false;
	
	@Override
	public String unknownRecommendationParameter(String param) {
		//TODO Fill in with fake recomendation list
		fakeRecommendationSwitch = !fakeRecommendationSwitch;
		if (fakeRecommendationSwitch) {
			return fakeRecommendationInsult.get(
				"a",
				"b",
				"c"
			);
		} else {
			return fakeRecommendationTroll.get(
				"d",
				"e",
				"f"
			);		
		}
	}

	@Override
	public String featureRankRestricted(String feature, int minRank, OsuApiUser user) {
		return "Sorry, " + feature + " is only available for people that can actually play osu!. Passing rank " + minRank + " will work, not that you have any hope of ever getting there.";
	}

	@Override
	public String mixedNomodAndMods() {
		return "What is this? Schrodinger's mod? I have a recommendation, but the superposition would collapse as soon as it was observed. It's not like I like you enough to break the laws of physics anyways!";
	}

	@Override
	public String outOfRecommendations() {
		return "Whaaat? Did you really just play all of those? You c-couldn't be asking me for recommendations just to hear me talk... W-we should go through the list again. You're free anyways, right?";
	}

	@Override
	public String notRanked() {
		return "Hmph. That beatmap isn't going to make anyone's pp bigger.";
	}

	@Override
	public void optionalCommentOnNP(IRCBotUser user, OsuApiUser apiUser, BeatmapMeta meta) {
		//TODO potentially more replies + shuffler	
		if (!(meta.getEstimates() instanceof PercentageEstimates)) {
			return;
		}
		PercentageEstimates estimates = (PercentageEstimates) meta.getEstimates();
		double typicalPP = (apiUser.getPp() / 20.0);
		if (estimates.getPPForAcc(.95) / typicalPP > 2.0) {
			user.message("Are you serious!? If that map doesn't kill you, I will.");
		} else if (estimates.getPPForAcc(1) / typicalPP < 0.333) {
			user.message("Playing that won't impress me much... n-n-not that I'd want you to.");

		}
	}
	
	@Override
	public void optionalCommentOnWith(IRCBotUser user, OsuApiUser apiUser, BeatmapMeta meta) {
		//TODO potentially more replies + shuffler	
		 //The following checks are probably redundant, but they don't hurt anyone either.
		if (!(meta.getEstimates() instanceof PercentageEstimates)) {
			return;
		}
		PercentageEstimates estimates = (PercentageEstimates) meta.getEstimates();
		double typicalPP = (apiUser.getPp() / 20);
		if (estimates.getPPForAcc(.95) / typicalPP > 2.0) {
			user.message("You idiot! You're going to get hurt trying mods like that!");
		} else if (estimates.getPPForAcc(1) / typicalPP < 0.5) {
			user.message("If you wanted to be treated like a baby, you could just ask... no, go ahead and play.");
		}
	}
	
	@Override
	public void optionalCommentOnRecommendation(IRCBotUser user, OsuApiUser apiUser, Recommendation meta) {
		recentRecommendations++;
		if(recentRecommendations == 7) {
			user.message("I have lots of free time. I would never pick out maps just because I liked you... h-h-hypothetically speaking.");
		} else if(recentRecommendations == 17) {
			user.message("You know, it's a privilege to talk to me this much, not a right.");
		} else if(recentRecommendations == 37) {
			user.message("How would you even play this game if I wasn't telling you what to do?");
		} else if(recentRecommendations == 73) {
			user.message("I would have had you arrested for harassment a long time ago if I didn't lov... I wasn't saying anything.");
		} else if(recentRecommendations == 173) {
			user.message("Just can't leave me alone, huh? I guess t-that's okay. But don't you dare tell anyone!");
		}
	}
	
	transient boolean changed;

	@Override
	public boolean isChanged() {
		return changed;
	}

	@Override
	public void setChanged(boolean changed) {
		this.changed = changed;
	}

	@Override
	public String invalidAccuracy(String acc) {
		return "Not even thelewa is capable of that kind of accuracy.";
	}

	@Override
	public String noPercentageEstimates() {
		return "I don't know. I'm not your personal genie. You probably don't even have a lamp for me!";
	}

	@Override
	public void optionalCommentOnLanguage(IRCBotUser user, OsuApiUser apiUser) {
		user.message("H-hey... don't listen to anyone that says I'm tsundere! Those idiots don't even know how nice I can be!");
	}

	@Override
	public String invalidChoice(String invalid, String choices) {
		return "What does \"" + invalid + "\" even mean!? If using two fingers is too much, you could always try singletapping each letter: " + choices + "!";
	}

}
