package tillerino.tillerinobot;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.sql.SQLException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.tillerino.osuApiModel.OsuApiUser;

import tillerino.tillerinobot.IRCBot.IRCBotUser;
import tillerino.tillerinobot.IRCBot.Pinger;
<<<<<<< HEAD
import tillerino.tillerinobot.RecommendationsManager.BareRecommendation;
import tillerino.tillerinobot.RecommendationsManager.GivenRecommendation;
import tillerino.tillerinobot.RecommendationsManager.Model;
import tillerino.tillerinobot.lang.Language;

public class IRCBotTest {
	public abstract class TestBackend implements BotBackend {
		/*
		 * user 1 = normal user rank 10000
		 * 
		 * user 2 = donator rank 5000
		 * 
		 * user 3 = normal user rank 20000
		 */
		
		@Override
		public Integer resolveIRCName(String ircName) throws SQLException,
				IOException {
			switch (ircName) {
			case "user":
				return 1;
			case "donator":
				return 2;
			case "lowrank":
				return 3;
			default:
				return null;
			}
		}
		
		@Override
		public int getDonator(OsuApiUser user) throws SQLException, IOException {
			return user.getUserId() == 2 ? 1 : 0;
		}
		
		@Override
		public OsuApiUser getUser(int userid, long maxAge) throws SQLException,
				IOException {
			OsuApiUser apiUser = new OsuApiUser();
			
			apiUser.setUserId(userid);
			
			if(userid == 1) {
				apiUser.setUsername("user");
				apiUser.setRank(10000);
			} else if(userid == 2) {
				apiUser.setUsername("donator");
				apiUser.setRank(5000);
			} else if(userid == 3) {
				apiUser.setUsername("lowrank");
				apiUser.setRank(20000);
			} else {
				return null;
			}
			
			return apiUser;
		}
		
		@Override
		public List<GivenRecommendation> loadGivenRecommendations(int userId)
				throws SQLException {
			return new ArrayList<>();
		}
		
		int lastVisitedVersion = 1;
		
		@Override
		public int getLastVisitedVersion(String nick) {
			return lastVisitedVersion;
		}
		
		@Override
		public void setLastVisitedVersion(String nick, int version)
				throws SQLException {
			lastVisitedVersion = version;
		}
		
		@Override
		public Collection<BareRecommendation> loadRecommendations(int userid,
				Collection<Integer> exclude, Model model, boolean nomod,
				long requestMods) throws SQLException, IOException, UserException {
			Collection<BareRecommendation> ret = new ArrayList<>();
			
			for(int i = 1; i <= 10; i++) {
				BareRecommendation bareRecommendation = mock(BareRecommendation.class);
				when(bareRecommendation.getBeatmapId()).thenReturn(i);
				when(bareRecommendation.getCauses()).thenReturn(new long[] { 2l });
				when(bareRecommendation.getPersonalPP()).thenReturn(100);
				when(bareRecommendation.getProbability()).thenReturn(1d);
				when(bareRecommendation.getMods()).thenReturn(requestMods);
				
				ret.add(bareRecommendation);
			}
			return ret;
		}
		
		@Override
		public BeatmapMeta loadBeatmap(int beatmapid, final long mods, Language lang)
				throws SQLException, IOException, UserException {
			OsuApiBeatmap beatmap = new OsuApiBeatmap();
			
			beatmap.setArtist("artist");
			beatmap.setVersion("version");
			beatmap.setId(beatmapid);
			beatmap.setTitle("title");
			
			BeatmapMeta beatmapMeta = new BeatmapMeta(beatmap, null, new PercentageEstimates() {
				@Override
				public double getPPForAcc(double acc) {
					return 100 * acc;
				}
				
				@Override
				public long getMods() {
					return mods;
				}
				
				@Override
				public boolean isShaky() {
					return false;
				}
			});
			
			return beatmapMeta;
		}
		
		@Override
		public void registerActivity(int userid) throws SQLException {
			
		}
	}
	
=======
import tillerino.tillerinobot.rest.BotInfoService;

public class IRCBotTest {
>>>>>>> upstream/tsundere
	@Test
	public void testVersionMessage() throws IOException, SQLException, UserException {
		IRCBot bot = getTestBot(backend);
		
		backend.hintUser("user", false, 0, 0);

		IRCBotUser user = mock(IRCBotUser.class);
		when(user.getNick()).thenReturn("user");
		when(user.message(anyString())).thenReturn(true);
		
		bot.processPrivateMessage(user, "!recommend");
		verify(user).message(IRCBot.versionMessage);
		verify(backend, times(1)).setLastVisitedVersion(anyString(), eq(IRCBot.currentVersion));
		
		user = mock(IRCBotUser.class);
		when(user.getNick()).thenReturn("user");
		
		bot.processPrivateMessage(user, "!recommend");
		verify(user, never()).message(IRCBot.versionMessage);
	}
	
	@Test
	public void testWrongStrings() throws IOException, SQLException, UserException {
		IRCBot bot = getTestBot(backend);
		
		backend.hintUser("user", false, 100, 1000);
		doReturn(IRCBot.currentVersion).when(backend).getLastVisitedVersion(anyString());

		IRCBotUser user = mock(IRCBotUser.class);
		when(user.getNick()).thenReturn("user");
		
		InOrder inOrder = inOrder(user);

		bot.processPrivateMessage(user, "!recommend");
		inOrder.verify(user).message(contains("http://osu.ppy.sh"));
		
		bot.processPrivateMessage(user, "!r");
		inOrder.verify(user).message(contains("http://osu.ppy.sh"));
		
		bot.processPrivateMessage(user, "!recccomend");
		inOrder.verify(user).message(contains("!help"));
		
		bot.processPrivateMessage(user, "!halp");
		inOrder.verify(user).message(contains("twitter"));
		
		bot.processPrivateMessage(user, "!feq");
		inOrder.verify(user).message(contains("FAQ"));
	}

	@Test
	public void testWelcomeIfDonator() throws Exception {
		BotBackend backend = mock(BotBackend.class);
		
		OsuApiUser osuApiUser = mock(OsuApiUser.class);
		when(osuApiUser.getUsername()).thenReturn("TheDonator");

		when(backend.resolveIRCName(anyString())).thenReturn(1);
		when(backend.getUser(eq(1), anyLong())).thenReturn(osuApiUser);
		when(backend.getDonator(any(OsuApiUser.class))).thenReturn(1);
		
		IRCBotUser user = mock(IRCBotUser.class);
		when(user.getNick()).thenReturn("TheDonator");
		
		IRCBot bot = getTestBot(backend);
		
		when(backend.getLastActivity(any(OsuApiUser.class))).thenReturn(System.currentTimeMillis() - 1000);
		bot.welcomeIfDonator(user);
		verify(user).message(startsWith("beep boop"));

		when(backend.getLastActivity(any(OsuApiUser.class))).thenReturn(System.currentTimeMillis() - 10 * 60 * 1000);
		bot.welcomeIfDonator(user);
		verify(user).message("Welcome back, TheDonator.");
		
		when(backend.getLastActivity(any(OsuApiUser.class))).thenReturn(System.currentTimeMillis() - 2l * 24 * 60 * 60 * 1000);
		bot.welcomeIfDonator(user);
		verify(user).message(startsWith("TheDonator, "));
		
		when(backend.getLastActivity(any(OsuApiUser.class))).thenReturn(System.currentTimeMillis() - 8l * 24 * 60 * 60 * 1000);
		bot.welcomeIfDonator(user);
		verify(user).message(contains("so long"));
	}
	
	IRCBot getTestBot(BotBackend backend) {
		IRCBot ircBot = new IRCBot(backend, spy(new RecommendationsManager(
				backend)), mock(BotInfoService.class), new UserDataManager(
				backend), mock(Pinger.class), false);
		return ircBot;
	}
	
	@Before
	public void mockBackend() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Spy
	TestBackend backend = new TestBackend(false);
	
	@Test
	public void testHugs() throws Exception {
		IRCBot bot = getTestBot(backend);

		backend.hintUser("donator", true, 0, 0);

		IRCBotUser botUser = mock(IRCBotUser.class);
		when(botUser.getNick()).thenReturn("donator");
	
		bot.processPrivateMessage(botUser, "I need a hug :(");
		
		verify(botUser, times(1)).message("Come here, you!");
		verify(botUser).action("hugs donator");
	}
	
	@Test
	public void testNP() throws Exception {
		
	}
}
