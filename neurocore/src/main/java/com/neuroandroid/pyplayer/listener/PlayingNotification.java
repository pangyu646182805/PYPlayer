package com.neuroandroid.pyplayer.listener;

import com.neuroandroid.pyplayer.service.PYMusicService;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */

public interface PlayingNotification {
    int NOTIFICATION_ID = 1;

    void init(PYMusicService service);

    void update();

    void stop();
}
