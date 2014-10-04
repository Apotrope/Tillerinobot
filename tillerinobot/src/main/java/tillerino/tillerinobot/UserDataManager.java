package tillerino.tillerinobot;

import java.sql.SQLException;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;

import tillerino.tillerinobot.lang.Default;
import tillerino.tillerinobot.lang.Language;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Manager for serializing and caching user data. Since user data can be
 * extremely volatile, we'll keep the data in cache and only serialize it when
 * the entry is being invalidated or the VM is being shut down. This might be a
 * bad idea.
 * 
 * @author Tillerino
 */
@Slf4j
@Singleton
public class UserDataManager {
	public static class UserData {
		public enum LanguageIdentifier {
			Default(Default.class);
			
			Class<? extends Language> cls;

			private LanguageIdentifier(Class<? extends Language> cls) {
				this.cls = cls;
			}
		}
		
		@Data
		@NoArgsConstructor
		@AllArgsConstructor
		public static class BeatmapWithMods {
			int beatmap;

			long mods;
		}

		transient boolean changed = false;

		public void setChanged(boolean changed) {
			this.changed = changed;
			
			if(!changed) {
				language.setChanged(changed);
			}
		}
		
		public boolean isChanged() {
			return changed || language.isChanged();
		}
		
		@Getter(onMethod = @__({ @Nonnull }))
		LanguageIdentifier languageIdentifier = LanguageIdentifier.Default;
		
		public void setLanguage(@Nonnull LanguageIdentifier languagePack) {
			if (this.languageIdentifier != languagePack) {
				changed = true;

				this.languageIdentifier = languagePack;

				this.language = null;
				this.serializedLanguage = null;
			}
		}
		
		transient Language language;
		
		public Language getLanguage() {
			if (language == null) {
				if (serializedLanguage != null && !serializedLanguage.isEmpty()) {
					language = gson.fromJson(serializedLanguage,
							languageIdentifier.cls);
				} else {
					try {
						language = languageIdentifier.cls.newInstance();
					} catch (InstantiationException | IllegalAccessException e) {
						throw new RuntimeException(languageIdentifier.cls
								+ " needs an accessible no-arg constructor", e);
					}
				}
			}
			return language;
		}

		@Getter(onMethod = @__({ @CheckForNull }))
		BeatmapWithMods lastSongInfo = null;
		
		public void setLastSongInfo(BeatmapWithMods lastSongInfo) {
			changed |= !Objects.equals(this.lastSongInfo, lastSongInfo);

			this.lastSongInfo = lastSongInfo;
		}

		/*
		 * we only use this field for serialization purposes. it should not be
		 * accessed from outside of UserDataManager. This field should be kept
		 * at the end because it may get large.
		 */
		String serializedLanguage;
	}
	
	BotBackend backend;
	
	@Inject
	public UserDataManager(BotBackend backend) {
		super();
		this.backend = backend;

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				cache.invalidateAll();
			}
		});
	}

	public UserData getData(int userid) throws SQLException {
		try {
			return cache.get(userid);
		} catch (ExecutionException e) {
			try {
				throw e.getCause();
			} catch (SQLException f) {
				throw f;
			} catch (RuntimeException f) {
				throw f;
			} catch (Throwable f) {
				throw new RuntimeException(e);
			}
		}
	}

	LoadingCache<Integer, UserData> cache = CacheBuilder.newBuilder()
			.expireAfterAccess(1, TimeUnit.HOURS).maximumSize(1000)
			.removalListener(new RemovalListener<Integer, UserData>() {
				@Override
				public void onRemoval(RemovalNotification<Integer, UserData> notification) {
					try {
						saveOptions(notification.getKey(), notification.getValue());
					} catch (SQLException e) {
						log.error("error saving user data", e);
					}
				}
			}).build(new CacheLoader<Integer, UserData>() {
				@Override
				public UserData load(Integer key) throws SQLException {
					return UserDataManager.this.load(key);
				}
			});
	
	static Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting()
			.create();
	
	private UserData load(Integer key) throws SQLException {
		String rawOptions = backend.getOptions(key);
		
		UserData options;
		if(rawOptions == null || rawOptions.isEmpty()) {
			options = new UserData();
		} else {
			options = gson.fromJson(rawOptions, UserData.class);
		}
		
		return options;
	}

	void saveOptions(int userid, UserData options) throws SQLException {
		if(!options.isChanged()) {
			return;
		}
		
		preSerialization(options);
		String serialized = gson.toJson(options);
		postSerialization(options);
		
		backend.saveOptions(userid, serialized);
		options.setChanged(false);
	}

	private void preSerialization(UserData options) {
		options.serializedLanguage = gson.toJson(options.language);
	}
	
	private void postSerialization(UserData options) {
		options.serializedLanguage = null;
	}
}
