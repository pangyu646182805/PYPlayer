package com.neuroandroid.pyplayer.config;

/**
 * Created by NeuroAndroid on 2017/3/15.
 */

public class Constant {
    /**
     * URL前缀
     */
    // public static final String BASE_URL = "http://192.168.97.159:80/clinicCloudDemo/v1/";
    public static final String BASE_URL = "http://106.75.15.138:8080/v4/";

    /**
     * SP配置TAG
     */
    public static final String PACKAGE_NAME_PREFERENCES = "config";

    /**
     * 是否显示过引导页
     */
    public static final String IS_USER_GUIDE_SHOWED = "is_user_guide_showed";

    /**
     * 用户信息存储在SP的TAG
     */
    public static final String USER = "USER";

    public static final int USER_TYPE = 0;

    /**
     * 返回的响应码
     */
    public static final int RESPONSE_CODE_OK = 200;
    public static final int RESPONSE_CODE_CREATED = 201;
    public static final int RESPONSE_CODE_NO_CONTENT = 204;
    public static final int RESPONSE_CODE_DELETE_OK = 260;
    public static final int RESPONSE_CODE_UPDATE_OK = 261;
    public static final int RESPONSE_CODE_CONTACTS_HAS_ADDED = -3001;
    public static final int RESPONSE_CODE_CONTACTS_USER_NONE = -1002;
    public static final int RESPONSE_CODE_NO_ROW_ERROR = -2003;
    public static final int RESPONSE_CODE_IMAGE_UPLOAD_FAILED = -3005;
    public static final int RESPONSE_CODE_USER_HAS_REGISTER = -1004;
    public static final int RESPONSE_CODE_PASSWORD_ERROR = -1001;

    public static final String GENERAL_THEME = "general_theme";
    public static final String DEFAULT_START_PAGE = "default_start_page";
    public static final String LAST_PAGE = "last_start_page";
    public static final String LAST_MUSIC_CHOOSER = "last_music_chooser";
    public static final String NOW_PLAYING_SCREEN_ID = "now_playing_screen_id";

    public static final String ARTIST_SORT_ORDER = "artist_sort_order";
    public static final String ARTIST_SONG_SORT_ORDER = "artist_song_sort_order";
    public static final String ARTIST_ALBUM_SORT_ORDER = "artist_album_sort_order";
    public static final String ALBUM_SORT_ORDER = "album_sort_order";
    public static final String ALBUM_SONG_SORT_ORDER = "album_song_sort_order";
    public static final String SONG_SORT_ORDER = "song_sort_order";

    public static final String ALBUM_GRID_SIZE = "album_grid_size";
    public static final String ALBUM_GRID_SIZE_LAND = "album_grid_size_land";

    public static final String SONG_GRID_SIZE = "song_grid_size";
    public static final String SONG_GRID_SIZE_LAND = "song_grid_size_land";

    public static final String ARTIST_GRID_SIZE = "artist_grid_size";
    public static final String ARTIST_GRID_SIZE_LAND = "artist_grid_size_land";

    public static final String ALBUM_COLORED_FOOTERS = "album_colored_footers";
    public static final String SONG_COLORED_FOOTERS = "song_colored_footers";
    public static final String ARTIST_COLORED_FOOTERS = "artist_colored_footers";

    public static final String FORCE_SQUARE_ALBUM_COVER = "force_square_album_art";

    public static final String COLORED_NOTIFICATION = "colored_notification";
    public static final String COLORED_APP_SHORTCUTS = "colored_app_shortcuts";

    public static final String AUDIO_DUCKING = "audio_ducking";
    public static final String GAPLESS_PLAYBACK = "gapless_playback";

    public static final String LAST_ADDED_CUTOFF_TIMESTAMP = "last_added_cutoff_timestamp";

    public static final String ALBUM_ART_ON_LOCKSCREEN = "album_art_on_lockscreen";
    public static final String BLURRED_ALBUM_ART = "blurred_album_art";

    public static final String LAST_SLEEP_TIMER_VALUE = "last_sleep_timer_value";
    public static final String NEXT_SLEEP_TIMER_ELAPSED_REALTIME = "next_sleep_timer_elapsed_real_time";

    public static final String IGNORE_MEDIA_STORE_ARTWORK = "ignore_media_store_artwork";

    public static final String LAST_CHANGELOG_VERSION = "last_changelog_version";
    public static final String INTRO_SHOWN = "intro_shown";

    public static final String AUTO_DOWNLOAD_IMAGES_POLICY = "auto_download_images_policy";

    public static final String START_DIRECTORY = "start_directory";

    public static final String APP_THEME_COLOR = "app_theme_color";

    public static final String DARK_STATUS_BAR = "dark_status_bar";

    public static final String DARK_THEME = "dark_theme";

    public static final String FIRST_INTO_APP = "first_into_app";
}
