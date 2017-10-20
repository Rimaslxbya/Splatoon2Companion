package com.example.rimas.splatoon2companionapp;

import android.app.ActionBar;
import android.content.ContentValues;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.StringTokenizer;
import java.util.TreeMap;

/**
 * Created by Rimas on 9/19/2017.
 */

public class FragmentPagerSupport extends FragmentActivity {
    static final int NUM_ITEMS = 3;

    MyAdapter mAdapter;                                 // Adapter for ViewPager
    ViewPager mPager;                                   // Holds the fragments
    static TreeMap<String, GearButton> headgearButtons; // Stores head gear buttons and their names
    static TreeMap<String, GearButton> clothingButtons; // Stores clothing buttons and their names
    static TreeMap<String, GearButton> shoeButtons;     // Stores shoes buttons and their names
    TreeMap<String, Integer> abilitiesMap;              // Stores ability names and their row id's
    TreeMap<String, Integer> acquisitionsTypeMap;       // Stores acquisition types and their row id's
    TreeMap<String, Integer> brandsMap;                 // Stores brand names and their row id's
    TreeMap<String, Integer> typesMap;                  // Stores types and their row id's
    TreeMap<GearButton, Integer> gearToDbId;            // Stores gear buttons and their row id's

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActionBar actionBar = getActionBar();
        setContentView(R.layout.fragment_pager);

        mAdapter = new MyAdapter(getSupportFragmentManager());

        mPager = findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);

        // Creates all the gear buttons using the csv files
        createGear();

        // Open the database
        GearDbHelper mDbHelper = new GearDbHelper(getApplicationContext());
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // If the database is empty, create and populate the database
        if(!mDbHelper.doesTableExist(db, GearContract.GearEntry.TABLE_GEAR) || mDbHelper.isTableEmpty(db, GearContract.GearEntry.TABLE_GEAR)) {
            mDbHelper.onCreate(db);
            db.close();
            db = mDbHelper.getWritableDatabase();
            populateAbilitiesTable(db);
            populateAcquisitionsTable(db);
            populateBrandsTable(db);
            populateTypes(db);
            populateGear(db);
        }

        // Close the database
        db.close();

        // Set up tab layout
        TabLayout tabby = findViewById(R.id.tab_layout);
        tabby.setupWithViewPager(mPager);
    }

    private void createGear(){
        AssetManager am = this.getAssets();

        // Initialize all of the maps
        headgearButtons = new TreeMap<>();
        clothingButtons = new TreeMap<>();
        shoeButtons = new TreeMap<>();
        abilitiesMap = new TreeMap<>();
        acquisitionsTypeMap = new TreeMap<>();
        brandsMap = new TreeMap<>();
        typesMap = new TreeMap<>();
        gearToDbId = new TreeMap<>();

        int nextId = createGearSet(am, "Splatoon2Headgear.csv", "head", 0, headgearButtons);
        nextId = createGearSet(am, "Splatoon2Clothing.csv", "clothing", nextId, clothingButtons);
        createGearSet(am, "Splatoon2Shoes.csv", "shoes", nextId, shoeButtons);
    }

    private int createGearSet(AssetManager am, String filename, String type, int nextId,
                              TreeMap<String, GearButton> gearMap) {
        InputStream is;

        try {
            is = am.open(filename);
        }
        catch(IOException e) {
            e.printStackTrace();
            return nextId;
        }

        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader buffR = new BufferedReader(isr);

        String line;

        int id;

        try {
            line = buffR.readLine();

            // Add headgear
            for(id = nextId; line != null; id++){
                StringTokenizer tokens = new StringTokenizer(line, ",");
                createGearButton(id, tokens, type, gearMap);
                line = buffR.readLine();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            return nextId;
        }

        return id;
    }

    private void createGearButton(int id, StringTokenizer tokens, String type,
                                  TreeMap<String, GearButton> gearMap) {
        String name = tokens.nextToken();
        String brand = tokens.nextToken();
        String acquisition = tokens.nextToken();
        String ability = tokens.nextToken();
        String rarity = tokens.nextToken();

        String drawableName = type + "_" + name.replace(" ", "_").toLowerCase();
        int drawableResource = getResources().getIdentifier(drawableName, "drawable", getPackageName());

        // If no drawable resource is found, use the resource at the top
        if(drawableResource == 0) drawableResource = R.drawable.ability_doubler;

        // Set the properties for button
        GearButton btnTag = new GearButton(this);
        btnTag.setName(name);
        btnTag.setType(type);
        btnTag.setBrand(brand);
        btnTag.setAcquisitionMethod(acquisition);
        btnTag.setAbility(ability);
        btnTag.setRarity(Integer.parseInt(rarity));
        btnTag.setAvailability(true);
        btnTag.setImageResource(drawableResource);
        btnTag.setBackgroundResource(0);
        btnTag.setId(id);
        btnTag.setTag("Unchecked");
        btnTag.setOnClickListener(new View.OnClickListener(){

            public void onClick(View v){
                ImageButton button = (ImageButton) v;

                flipGearButtonState(button);
            }
        });

        // Add the gear button to the given map
        gearMap.put(name, btnTag);

        // Populate Maps
        abilitiesMap.put(ability, 0);
        acquisitionsTypeMap.put(acquisition, 0);
    }

    private static void flipGearButtonState(ImageButton button) {
        if(button.getTag() == "Unchecked") {
            button.setBackgroundResource(R.drawable.splat);
            button.setTag("Checked");
        }
        else {
            button.setBackgroundResource(0);
            button.setTag("Unchecked");
        }
    }

    /**
     * @brief Populates the abilities table
     *
     * @param db    The database with the abilities table that needs to be populated
     */
    private void populateAbilitiesTable(SQLiteDatabase db){
        Integer idCounter = 0;

        for(String ability : abilitiesMap.keySet()) {
            ContentValues values = new ContentValues();
            values.put(GearContract.GearEntry.COLUMN_ABILITY, ability);

            // Insert into database
            db.insert(GearContract.GearEntry.TABLE_ABILITIES, null, values);

            // Insert the ability's id into the table and increment the id counter
            abilitiesMap.put(ability, idCounter);
            idCounter++;
        }
    }

    /**
     * @brief Populates the acquisitions table
     *
     * @param db    The database with the acquisitions table that needs to be populated
     */
    private void populateAcquisitionsTable(SQLiteDatabase db){
        Integer idCounter = 0;

        for(String acquisitionType : acquisitionsTypeMap.keySet()) {
            ContentValues values = new ContentValues();
            values.put(GearContract.GearEntry.COLUMN_ACQUISITION, acquisitionType);

            // Insert into database
            db.insert(GearContract.GearEntry.TABLE_ACQUISITION_METHODS, null, values);

            // Insert the acquisition type's id into the table and increment the id counter
            abilitiesMap.put(acquisitionType, idCounter);
            idCounter++;
        }
    }

    /**
     * @brief Reads in Splatoon2BrandBias.csv and uses it to populate the brands table
     *
     * @param db    The database with the brands table that needs to be populated
     */
    private void populateBrandsTable(SQLiteDatabase db){
        AssetManager am = this.getAssets();
        InputStream is;

        try {
            is = am.open("Splatoon2BrandBias.csv");
        }
        catch(IOException e) {
            e.printStackTrace();
            return;
        }

        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader buffR = new BufferedReader(isr);

        String line;

        try {
            line = buffR.readLine();

            // Add brands and their biases
            for(int id = 0; line != null; id++){
                StringTokenizer tokens = new StringTokenizer(line, ",");

                String brand = tokens.nextToken();
                String commonAbility = tokens.nextToken();
                String uncommonAbility = tokens.nextToken();

                ContentValues values = new ContentValues();
                values.put(GearContract.GearEntry.COLUMN_BRAND, brand);
                values.put(GearContract.GearEntry.COLUMN_COMMON_ABILITY, abilitiesMap.get(commonAbility));
                values.put(GearContract.GearEntry.COLUMN_UNCOMMON_ABILITY, abilitiesMap.get(uncommonAbility));

                // Insert into database
                db.insert(GearContract.GearEntry.TABLE_BRANDS, null, values);

                brandsMap.put(brand, id);

                line = buffR.readLine();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            return;
        }

        return;
    }

    private void populateTypes(SQLiteDatabase db){

        ContentValues values = new ContentValues();

        // Insert head
        values.put(GearContract.GearEntry.COLUMN_TYPE_NAME, "head");
        db.insert(GearContract.GearEntry.TABLE_TYPES, null, values);
        typesMap.put("head", 0);

        // Insert clothes
        values.clear();
        values.put(GearContract.GearEntry.COLUMN_TYPE_NAME, "clothes");
        db.insert(GearContract.GearEntry.TABLE_TYPES, null, values);
        typesMap.put("clothes", 1);

        // Insert shoes
        values.clear();
        values.put(GearContract.GearEntry.COLUMN_TYPE_NAME, "shoes");
        db.insert(GearContract.GearEntry.TABLE_TYPES, null, values);
        typesMap.put("shoes", 2);
    }

    /**
     * @brief Populates the Gear table
     *
     * @param db    The database to populate
     */
    private void populateGear(SQLiteDatabase db){
        int id = 0;
        id = populateGearFromMap(db, headgearButtons, id);
        id = populateGearFromMap(db, clothingButtons, id);
        populateGearFromMap(db, shoeButtons, id);
    }

    /**
     * @brief: Populates the Gear table with the gear information from a TreeMap of gear
     *
     * @param db    A database with a Gear table that needs entries
     * @param gMap  A map of gear to be inserted into the table
     * @param id    The row id for the next entry in the table
     * */
    private int populateGearFromMap(SQLiteDatabase db, TreeMap<String, GearButton> gMap, int id){

        for(GearButton gButt : gMap.values()){
            String name = gButt.getName();
            String brand = gButt.getBrand();
            String ability = gButt.getAbility();
            String acquisitionType = gButt.getAcquisitionMethod();
            String type = gButt.getType();
            int rarity = gButt.getRarity();
            boolean availability = gButt.getAvailability();

            ContentValues values = new ContentValues();
            values.put(GearContract.GearEntry.COLUMN_GNAME, name);
            values.put(GearContract.GearEntry.COLUMN_TYPE_ID, typesMap.get(type));
            values.put(GearContract.GearEntry.COLUMN_BRAND_ID, brandsMap.get(brand));
            values.put(GearContract.GearEntry.COLUMN_ABILITY_ID, abilitiesMap.get(ability));
            values.put(GearContract.GearEntry.COLUMN_ACQUISITION_METHOD_ID, acquisitionsTypeMap.get(acquisitionType));
            values.put(GearContract.GearEntry.COLUMN_RARITY, rarity);
            values.put(GearContract.GearEntry.COLUMN_AVAILABLE, availability);
            values.put(GearContract.GearEntry.COLUMN_SELECTED, false);

            // Insert into database
            db.insert(GearContract.GearEntry.TABLE_GEAR, null, values);
            gearToDbId.put(gButt, id);
            id++;
        }

        return id;
    }

    public static class MyAdapter extends FragmentPagerAdapter {
        public MyAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        @Override
        public Fragment getItem(int position) {
            return GearFragment.newInstance(position);
        }

        @Override
        public CharSequence getPageTitle(int position)
        {
            String mTitle;
            switch(position)
            {
                case 0:
                    mTitle = "Head";
                    break;
                case 1:
                    mTitle = "Clothes";
                    break;
                case 2:
                    mTitle = "Shoes";
                    break;
                default:
                    mTitle = "Default";
            }

            return mTitle;
        }
    }

    public static class GearFragment extends Fragment {

        int mNum;
        TreeMap<String, GearButton> gearButtons;

        /**
         * Create a new instance of CountingFragment, providing "num"
         * as an argument.
         */
        static GearFragment newInstance(int num) {
            GearFragment f = new GearFragment();

            // Supply num input as an argument.
            Bundle args = new Bundle();
            args.putInt("num", num);
            f.setArguments(args);

            return f;
        }

        /**
         * When creating, retrieve this instance's number from its arguments.
         */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mNum = getArguments() != null ? getArguments().getInt("num") : -1;
        }

        /**
         * The Fragment's UI is a collection of buttons
         */
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_pager_list, container, false);
            LinearLayout layout = v.findViewById(R.id.linear_layout_id);

            switch(mNum)
            {
                case 0:
                    gearButtons = addAllGear(headgearButtons, layout);
                    loadGearCheckedState("head");
                    break;
                case 1:
                    gearButtons = addAllGear(clothingButtons, layout);
                    loadGearCheckedState("clothing");
                    break;
                case 2:
                    gearButtons = addAllGear(shoeButtons, layout);
                    loadGearCheckedState("shoes");
                    break;
            }

            return v;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

        }

        @Override
        public void onPause() {
            super.onPause();
            saveGearSelection();
        }

        /**
         * @brief Adds all of the gear in the passed map of gear items, into the fragment.
         *
         * @param mapOfGearItems    The map of gear buttons to add to the fragment.
         * @param layout            The layout in the fragment to add them to.
         * @return                  The passed map of gear items
         */
        private TreeMap<String, GearButton> addAllGear(TreeMap<String, GearButton> mapOfGearItems, LinearLayout layout) {
            // the layout on which you are working
            layout.setOrientation(LinearLayout.VERTICAL);

            // The position of the button in a row
            int rowPosition = 0;

            // Stores the current row
            LinearLayout row = new LinearLayout(this.getContext());

            for(GearButton btnTag : mapOfGearItems.values()) {

                // If there are less than 4 buttons in the row, keep adding to this row
                if(rowPosition < 4){
                    rowPosition++;
                }

                // Otherwise, finish the row and start a new one
                else{
                    // Add the row to the layout
                    layout.addView(row);

                    // Create the next row
                    row = new LinearLayout(this.getContext());
                    row.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));

                    rowPosition = 0;
                }

                // Add button to the layout
                row.addView(btnTag);
            }

            // Add the final row to the layout if it was not already added
            if(rowPosition != 0)
                layout.addView(row);

            return mapOfGearItems;

        }

        private void saveGearSelection() {
            // Open the database
            GearDbHelper mDbHelper = new GearDbHelper(getContext());
            SQLiteDatabase db = mDbHelper.getWritableDatabase();

            for(GearButton gear : gearButtons.values()) {
                int toggled = gear.getTag() == "Checked" ? 1 : 0;

                ContentValues values = new ContentValues();
                values.put(GearContract.GearEntry.COLUMN_SELECTED, toggled);

                String selection = GearContract.GearEntry.COLUMN_GNAME + " = ?";
                String[] selectionArgs = {gear.getName()};

                db.update(GearContract.GearEntry.TABLE_GEAR, values, selection, selectionArgs);
            }

            db.close();
        }

        /**
         * @brief Checks the given database to see if the given button should be toggled.
         *
         * @param prefix    The type of button to load
         */
        private void loadGearCheckedState(String prefix){
            // Grab the database
            GearDbHelper mDbHelper = new GearDbHelper(getContext());
            SQLiteDatabase db = mDbHelper.getReadableDatabase();

            // Requested columns
            String[] projection = {
                    GearContract.GearEntry.COLUMN_GNAME,
                    GearContract.GearEntry.COLUMN_SELECTED
            };

            // Selection for WHERE statement
            String selection = GearContract.GearEntry.COLUMN_TYPE_ID + " = ?";

            // Argument for WHERE statement
            String[] selectionArgs = { prefix };

            // Get the query result
            Cursor cursor = db.query(GearContract.GearEntry.TABLE_GEAR, projection, selection,
                    selectionArgs, null, null, null);

            while(cursor.moveToNext()) {
                // Get the name of the gear button to check
                String btnName = cursor.getString(0);

                // Get the gear button object by using the name
                GearButton btn = gearButtons.get(btnName);

                // Get whether or not the gear button should be toggled
                int toggled = cursor.getInt(1);

                // If the gear button is registered as toggled, toggle the button by flipping its state
                if (toggled == 1) flipGearButtonState(btn);
            }

            // Free the cursor
            cursor.close();
        }

        private void debugReadDatabase(SQLiteDatabase db){

            String[] projection = {
                    GearContract.GearEntry._ID,
                    GearContract.GearEntry.COLUMN_GNAME,
                    GearContract.GearEntry.COLUMN_SELECTED
            };

            //Cursor cursor = db.query(GearContract.GearEntry.TABLE_NAME_GEAR, projection, null, null, null, null, null);

            Cursor cursor = db.rawQuery("PRAGMA table_info(" + GearContract.GearEntry.TABLE_GEAR + ")", null);

            int cnt = cursor.getColumnCount();

            while(cursor.moveToNext()){

                String result = "";

                for(int i = 0; i < cnt; i++)
                    result += cursor.getString(i) + " ";

                boolean breakHere = true;

                if(breakHere)
                    result = result;
            }

            cursor.close();
        }

    }
}