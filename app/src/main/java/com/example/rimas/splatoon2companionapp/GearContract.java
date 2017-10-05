package com.example.rimas.splatoon2companionapp;

import android.provider.BaseColumns;

/**
 * Created by Rimas on 9/25/2017.
 */

public final class GearContract {
    /**
     * Private constructor to prevent someone (myself) from accidentally instantiating the contract
     * class. (According to https://developer.android.com/training/basics/data-storage/databases.html )
     */
    private GearContract() {}

    /* Inner class that defines the table contents */
    public static class GearEntry implements BaseColumns {
        public static final String TABLE_GEAR = "gear";
        public static final String COLUMN_GNAME = "gear_name";
        public static final String COLUMN_TYPE_ID = "gear_type_id";
        public static final String COLUMN_BRAND_ID = "brand_id";
        public static final String COLUMN_ACQUISITION_METHOD_ID = "acquisition_method_id";
        public static final String COLUMN_ABILITY_ID = "ability_id";
        public static final String COLUMN_AVAILABLE = "available";
        public static final String COLUMN_RARITY = "rarity";
        public static final String COLUMN_SELECTED = "selected";

        public static final String TABLE_ABILITIES = "abilties";
        public static final String COLUMN_ABILITY = "ability_name";

        public static final String TABLE_ACQUISITION_METHODS = "aquisition_methods";
        public static final String COLUMN_ACQUISITION = "acquisition_method";

        public static final String TABLE_BRANDS = "brands";
        public static final String COLUMN_BRAND = "brand_name";
        public static final String COLUMN_COMMON_ABILITY = "common_ability_id";
        public static final String COLUMN_UNCOMMON_ABILITY = "uncommon_ability_id";

        public static final String TABLE_GEAR_TO_ABILITIES = "gear_to_abilities";
        public static final String COLUMN_GEAR_ID = "gear_id";

        public static final String TABLE_TYPES = "gear_types";
        public static final String COLUMN_TYPE_NAME = "type_name";
    }
}
