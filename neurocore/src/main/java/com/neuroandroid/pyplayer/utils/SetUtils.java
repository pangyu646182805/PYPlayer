package com.neuroandroid.pyplayer.utils;

import java.util.Collection;

/**
 * Created by NeuroAndroid on 2017/5/16.
 */

public class SetUtils {
    public static boolean equals(Collection<?> set1, Collection<?> set2) {
        if (set1 == null || set2 == null) {
            return false;
        }

        if (set1.size() != set2.size()) {
            return false;
        }

        return set1.containsAll(set2);
    }
}
