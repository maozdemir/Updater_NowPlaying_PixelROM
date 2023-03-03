package com.contested.zeroiq.sense.utils;

import android.provider.BaseColumns;

public final class DBConstructor {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private DBConstructor() {}

    /* Inner class that defines the table contents */
    public static class FeedEntry implements BaseColumns {
        public static final String TABLE_NAME = "Songs";
        public static final String COLUMN_NAME_TRACK = "track";
        public static final String COLUMN_NAME_ARTIST = "artist";
        public static final String COLUMN_NAME_DATE = "date";
        public static final String COLUMN_NAME_ACRID = "acrid";
    }
}