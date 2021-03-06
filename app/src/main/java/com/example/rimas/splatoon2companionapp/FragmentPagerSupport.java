package com.example.rimas.splatoon2companionapp;

import android.app.ActionBar;
import android.content.ContentValues;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;

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
        if(!mDbHelper.doesTableExist(db, GearContract.GearEntry.TABLE_GEAR)
                || mDbHelper.isTableEmpty(db, GearContract.GearEntry.TABLE_GEAR)) {
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


        AssetManager am = getApplicationContext().getAssets();

        Typeface splatFont = Typeface.createFromAsset(am,
                String.format(Locale.US, "fonts/%s", "Splatfont2.ttf"));

        // Change font on tabs
        int tabbyChildCount = tabby.getChildCount();
        for(int k = 0; k < tabbyChildCount; k++) {
            ViewGroup vg = (ViewGroup) tabby.getChildAt(0);
            int tabsCount = vg.getChildCount();
            for (int j = 0; j < tabsCount; j++) {
                ViewGroup vgTab = (ViewGroup) vg.getChildAt(j);
                int tabChildCount = vgTab.getChildCount();
                for (int i = 0; i < tabChildCount; i++) {
                    View tabViewChild = vgTab.getChildAt(i);
                    if (tabViewChild instanceof TextView) {
                        ((TextView) tabViewChild).setTypeface(splatFont, Typeface.NORMAL);
                    }
                }
            }
        }
    }

    private void createGear(){
        AssetManager am = this.getAssets();

        // Initialize all of the maps
        headgearButtons = new TreeMap<>();
        clothingButtons = new TreeMap<>();
        shoeButtons = new TreeMap<>();

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

        //Save the dimensions of splat.png to use as the dimensions for the buttons
        final BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(), R.drawable.splat, opt);
        final float scale = getResources().getDisplayMetrics().density;

        // Convert from px to dp during assignment
        final int buttonHeight = opt.outHeight * (int) (scale + 0.5f);
        final int buttonWidth = opt.outWidth * (int) (scale + 0.5f);

        int id;

        try {
            line = buffR.readLine();

            // Add headgear
            for(id = nextId; line != null; id++){
                StringTokenizer tokens = new StringTokenizer(line, ",");
                createGearButton(id, tokens, type, gearMap,buttonWidth,buttonHeight);
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
                                  TreeMap<String, GearButton> gearMap, int buttonWidth, int buttonHeight) {
        String name = tokens.nextToken();
        String brand = tokens.nextToken();
        String acquisition = tokens.nextToken();
        String ability = tokens.nextToken();
        String rarity = tokens.nextToken();

        String drawableName = type + "_" + name.replace(" ", "_").replace("'", "").replace("&","").toLowerCase();
        int drawableResource = getResources().getIdentifier(drawableName, "drawable", getPackageName());

        // If no drawable resource is found, use the resource at the top
        if(drawableResource == 0) drawableResource = R.drawable.ability_doubler;

        // Set the properties for button
        GearButton btnTag = new GearButton(this);
        btnTag.setLayoutParams(new LinearLayout.LayoutParams(buttonWidth, buttonHeight));
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

        // Add the gear button to the given map
        gearMap.put(name, btnTag);
    }

    /**
     * @brief Populates the abilities table
     *
     * @param db    The database with the abilities table that needs to be populated
     */
    private void populateAbilitiesTable(SQLiteDatabase db){
        ArrayList<GearButton> gearList = new ArrayList<>();
        gearList.addAll(headgearButtons.values());
        gearList.addAll(clothingButtons.values());
        gearList.addAll(shoeButtons.values());

        TreeSet<String> abilitiesSet = new TreeSet<>();

        for(GearButton gearItem : gearList)
            abilitiesSet.add(gearItem.getAbility());

        for(String ability : abilitiesSet) {
            ContentValues values = new ContentValues();
            values.put(GearContract.GearEntry.COLUMN_ABILITY, ability);

            // Insert into database
            db.insert(GearContract.GearEntry.TABLE_ABILITIES, null, values);
        }
    }

    /**
     * @brief Populates the acquisitions table
     *
     * @param db    The database with the acquisitions table that needs to be populated
     */
    private void populateAcquisitionsTable(SQLiteDatabase db){
        ArrayList<GearButton> gearList = new ArrayList<>();
        gearList.addAll(headgearButtons.values());
        gearList.addAll(clothingButtons.values());
        gearList.addAll(shoeButtons.values());

        TreeSet<String> acquisitionsSet = new TreeSet<>();

        for(GearButton gearItem : gearList)
            acquisitionsSet.add(gearItem.getAcquisitionMethod());

        for(String acquisitionType : acquisitionsSet) {
            ContentValues values = new ContentValues();
            values.put(GearContract.GearEntry.COLUMN_ACQUISITION, acquisitionType);

            // Insert into database
            db.insert(GearContract.GearEntry.TABLE_ACQUISITION_METHODS, null, values);
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
            while(line != null){
                StringTokenizer tokens = new StringTokenizer(line, ",");

                String brand = tokens.nextToken();
                String commonAbility = tokens.nextToken();
                String uncommonAbility = tokens.nextToken();

                Cursor commonCursor = db.rawQuery("SELECT " + GearContract.GearEntry._ID +
                        " FROM " + GearContract.GearEntry.TABLE_ABILITIES + " WHERE " +
                        GearContract.GearEntry.COLUMN_ABILITY + " = ?", new String[] {commonAbility});

                Cursor uncommonCursor = db.rawQuery("SELECT " + GearContract.GearEntry._ID +
                        " FROM " + GearContract.GearEntry.TABLE_ABILITIES + " WHERE " +
                        GearContract.GearEntry.COLUMN_ABILITY + " = ?", new String[] {uncommonAbility});

                commonCursor.moveToNext();
                int commonId = commonCursor.getInt(0);

                uncommonCursor.moveToNext();
                int uncommonId = uncommonCursor.getInt(0);


                ContentValues values = new ContentValues();
                values.put(GearContract.GearEntry.COLUMN_BRAND, brand);
                values.put(GearContract.GearEntry.COLUMN_COMMON_ABILITY, commonId);
                values.put(GearContract.GearEntry.COLUMN_UNCOMMON_ABILITY, uncommonId);

                // Insert into database
                db.insert(GearContract.GearEntry.TABLE_BRANDS, null, values);

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

        // Insert clothing
        values.clear();
        values.put(GearContract.GearEntry.COLUMN_TYPE_NAME, "clothing");
        db.insert(GearContract.GearEntry.TABLE_TYPES, null, values);

        // Insert shoes
        values.clear();
        values.put(GearContract.GearEntry.COLUMN_TYPE_NAME, "shoes");
        db.insert(GearContract.GearEntry.TABLE_TYPES, null, values);
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

            // Get the row IDs for Ability, Acquisition, Brand and Type values
            int abilityRowId = 0;
            Cursor abilityCursor = db.rawQuery("SELECT " + GearContract.GearEntry._ID +
                    " FROM " + GearContract.GearEntry.TABLE_ABILITIES +
                    " WHERE " + GearContract.GearEntry.COLUMN_ABILITY +
                    " = ?", new String[] {ability} );
            abilityCursor.moveToNext();
            abilityRowId = abilityCursor.getInt(0);
            abilityCursor.close();

            int acquisitionMethodRowId = 0;
            Cursor acquisitionCursor = db.rawQuery("SELECT " + GearContract.GearEntry._ID +
                    " FROM " + GearContract.GearEntry.TABLE_ACQUISITION_METHODS +
                    " WHERE " + GearContract.GearEntry.COLUMN_ACQUISITION +
                    " = ?", new String[] {acquisitionType} );
            acquisitionCursor.moveToNext();
            acquisitionMethodRowId = acquisitionCursor.getInt(0);
            acquisitionCursor.close();

            int brandRowId = 0;
            Cursor brandCursor = db.rawQuery("SELECT " + GearContract.GearEntry._ID +
                    " FROM " + GearContract.GearEntry.TABLE_BRANDS +
                    " WHERE " + GearContract.GearEntry.COLUMN_BRAND +
                    " = ?", new String[] {brand} );
            brandCursor.moveToNext();
            brandRowId = brandCursor.getInt(0);
            brandCursor.close();

            int typeRowId = 0;
            Cursor typeCursor = db.rawQuery("SELECT " + GearContract.GearEntry._ID +
                    " FROM " + GearContract.GearEntry.TABLE_TYPES +
                    " WHERE " + GearContract.GearEntry.COLUMN_TYPE_NAME +
                    " = ?", new String[] {type} );
            typeCursor.moveToNext();
            typeRowId = typeCursor.getInt(0);
            typeCursor.close();

            // Add all values to a content values object
            ContentValues values = new ContentValues();
            values.put(GearContract.GearEntry.COLUMN_GNAME, name);
            values.put(GearContract.GearEntry.COLUMN_TYPE_ID, typeRowId);
            values.put(GearContract.GearEntry.COLUMN_BRAND_ID, brandRowId);
            values.put(GearContract.GearEntry.COLUMN_ABILITY_ID, abilityRowId);
            values.put(GearContract.GearEntry.COLUMN_ACQUISITION_METHOD_ID, acquisitionMethodRowId);
            values.put(GearContract.GearEntry.COLUMN_RARITY, rarity);
            values.put(GearContract.GearEntry.COLUMN_AVAILABLE, availability);
            values.put(GearContract.GearEntry.COLUMN_SELECTED, false);

            // Insert into database
            db.insert(GearContract.GearEntry.TABLE_GEAR, null, values);
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

        private final float GEAR_NAME_SIZE = 24;
        private final float PLUS_MINUS_SIZE = 24;
        private final String PLUS_COLOR = "#1A8934";
        private final String MINUS_COLOR = "#A02B37";

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

            if(gearButtons != null){
                for(GearButton gearButt : gearButtons.values()){
                    ((ViewGroup)gearButt.getParent()).removeView(gearButt);
                }
            }

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
        public void setUserVisibleHint(boolean isVisibleToUser){
            super.setUserVisibleHint(isVisibleToUser);

            if(!isVisibleToUser && getContext() != null)
                saveGearSelection();
        }

        /**
         * @brief Adds all of the gear in the passed map of gear items, into the fragment.
         *
         * @param mapOfGearItems    The map of gear buttons to add to the fragment.
         * @param layout            The layout in the fragment to add them to.
         * @return                  The passed map of gear items
         */
        private TreeMap<String, GearButton> addAllGear(TreeMap<String, GearButton> mapOfGearItems, final LinearLayout layout) {
            // the layout on which you are working
            layout.setOrientation(LinearLayout.VERTICAL);

            // The position of the button in a row
            int rowPosition = 0;

            // Open the database
            GearDbHelper mDbHelper = new GearDbHelper(getActivity().getApplicationContext());
            SQLiteDatabase db = mDbHelper.getReadableDatabase();

            // Stores the current row
            LinearLayout row = new LinearLayout(this.getContext());
            row.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));

            for(GearButton btnTag : mapOfGearItems.values()) {

                // If there are less than 4 buttons in the row, keep adding to this row
                if(rowPosition < 4){
                    rowPosition++;
                }

                // Otherwise, finish the row and start a new one
                else{
                    // Add the row to the layout
                    layout.addView(row);

                    int[] pos = new int[2];
                    row.getLocationOnScreen(pos);

                    // Create the next row
                    row = new LinearLayout(this.getContext());
                    row.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));

                    rowPosition = 1;
                }

                // Add button to the layout
                row.addView(btnTag);

                final PopupWindow gearPopup = new PopupWindow(getActivity());
                gearPopup.setOutsideTouchable(true);

                btnTag.setOnClickListener(new View.OnClickListener(){

                    public void onClick(View v){
                        GearButton button = (GearButton) v;

                        button.flipToggledState();
                    }
                });

                btnTag.setOnLongClickListener(new View.OnLongClickListener(){

                    public boolean onLongClick(View v){
                        GearButton button = (GearButton) v;
                        gearPopup.showAsDropDown(button);
                        
                        return true;
                    }
                });

                setupGearPopup(gearPopup, btnTag, db);
            }

            // Add the final row to the layout if it was not already added
            if(rowPosition != 0)
                layout.addView(row);

            db.close();

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

            // Grab the row ID for the type prefix
            Cursor typeCursor = db.rawQuery("SELECT " + GearContract.GearEntry._ID +
                    " FROM " + GearContract.GearEntry.TABLE_TYPES +
                    " WHERE " + GearContract.GearEntry.COLUMN_TYPE_NAME +
                    " = ?", new String[] {prefix} );
            typeCursor.moveToNext();
            int typeRowId = typeCursor.getInt(0);
            typeCursor.close();

            // Requested columns
            String[] projection = {
                    GearContract.GearEntry.COLUMN_GNAME,
                    GearContract.GearEntry.COLUMN_SELECTED
            };

            // Selection for WHERE statement
            String selection = GearContract.GearEntry.COLUMN_TYPE_ID + " = ?";

            // Argument for WHERE statement
            String[] selectionArgs = { Integer.toString(typeRowId) };

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

                // If the gear button is registered as toggled, toggle the button
                btn.setToggledState(toggled == 1);
            }

            // Free the cursor
            cursor.close();

            db.close();
        }

        /**
         * Creates an ImageView that uses the resource found by formatting $imageName
         *
         * @param imageName     The name of the image resource to use for the ImageView
         * @param resize        If true, resizes the image view to 100 x 100
         * @return              The new ImageView that displays the resource with $imageName
         */
        private ImageView createImageFromName(String imageName, boolean resize) {
            String resStr = imageName.toLowerCase().replace(' ', '_');
            resStr = resStr.replace("(", "").replace(")", "");
            if (resStr.equals("---"))
                resStr = "random";

            int resId = getResources().getIdentifier(resStr, "drawable", getActivity().getPackageName());
            ImageView img = new ImageView(getContext());
            img.setImageResource(resId);

            if (resize){
                Bitmap bitmap = Macros.resize(img.getDrawable(), 100, 100);
                Drawable drawable = new BitmapDrawable(getResources(), bitmap);
                img.setImageDrawable(drawable);
            }

            return img;
        }

        /**
         * Creates the GUI for the gear popup
         *
         * @param gearPopup     The popup to create the GUI for
         * @param btnTag        The gearButton whose information will be displayed
         * @param db            A database containing gear information
         */
        private void setupGearPopup(PopupWindow gearPopup, GearButton btnTag, SQLiteDatabase db){
            // Create label for Gear name
            TextView gearLabel = Macros.createSplatoonTextView(btnTag.getName(), getContext());
            gearLabel.setTextSize(GEAR_NAME_SIZE);

            // Create a layout for the brand information
            LinearLayout brandLayout = createBrandLayout(btnTag, db);

            // Create a layout for the rarity
            LinearLayout rarityLayout = createRarityLayout(btnTag);

            // Create a layout for the acquisition method
            LinearLayout acquisitionLayout = createAcquisitionMethodLayout(btnTag);

            // Create a layout for the main ability
            LinearLayout mainAbilityLayout = createMainAbilityLayout(btnTag);

            // Set up the layout params object
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.gravity = Gravity.CENTER_VERTICAL;

            // Set up the layout params object
            RelativeLayout.LayoutParams leftParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            leftParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

            // Set up the layout params object
            RelativeLayout.LayoutParams rightParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            rightParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

            // Set up the gear label params
            LinearLayout.LayoutParams gearLabelParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            gearLabelParams.gravity = Gravity.CENTER_HORIZONTAL;

            // Combine rarity and acquisition layouts
            RelativeLayout rareAcquiLayout = new RelativeLayout(getContext());
            rareAcquiLayout.addView(rarityLayout, leftParams);
            rareAcquiLayout.addView(acquisitionLayout, rightParams);

            // Layout for gear popup
            LinearLayout containerLayout = new LinearLayout(getContext());
            containerLayout.setOrientation(LinearLayout.VERTICAL);
            containerLayout.addView(gearLabel, gearLabelParams);
            containerLayout.addView(brandLayout, layoutParams);
            containerLayout.addView(rareAcquiLayout, leftParams);
            containerLayout.addView(mainAbilityLayout, layoutParams);
            gearPopup.setContentView(containerLayout);
        }

        /**
         * Creates a linear layout set up with the brand logo and label. Tapping the logo spawns
         *  another popup with brand bias information.
         *
         * @param btnTag    The Gear Button whose brand to display
         * @param db        A database with brand information
         * @return          A linear layout with the brand logo and label
         */
        private LinearLayout createBrandLayout(GearButton btnTag, SQLiteDatabase db){
            // Create label for brand
            TextView brandLabel = Macros.createSplatoonTextView(btnTag.getBrand(), getContext());

            // Get the brand's logo
            String brandResourceStr = btnTag.getBrand().toLowerCase().replace(' ','_');
            ImageButton brandButton = new ImageButton(getContext());
            int brandDrawable = getResources().getIdentifier(brandResourceStr, "drawable", getActivity().getPackageName());
            brandButton.setImageResource(brandDrawable);

            // Create the popup for the brand button
            final PopupDialog brandBiasPopup = new PopupDialog(getActivity());

            brandButton.setOnClickListener(new View.OnClickListener(){

                public void onClick(View v){
                    if(brandBiasPopup.isShowing())
                        brandBiasPopup.dismiss();
                    else
                        brandBiasPopup.showAsDropdown(v);
                }
            });

            setupBrandPopup(brandBiasPopup, btnTag.getBrand(), db);

            // Set up the layout params object
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.gravity = Gravity.CENTER_VERTICAL;

            // Layout for brand icon and name
            LinearLayout brandLayout = new LinearLayout(getContext());
            brandLayout.setOrientation(LinearLayout.HORIZONTAL);
            brandLayout.addView(brandButton, layoutParams);
            brandLayout.addView(brandLabel, layoutParams);

            return brandLayout;
        }

        private LinearLayout createMainAbilityLayout(GearButton btnTag){
            // Create label for main ability
            TextView mainAbilityLabel = Macros.createSplatoonTextView(btnTag.getAbility(), getContext());

            // Get the main ability's icon
            ImageView mainAbilityImg = createImageFromName(btnTag.getAbility(), true);

            // Set up the layout params object
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.gravity = Gravity.CENTER_VERTICAL;

            // Layout for main ability
            LinearLayout mainAbilityLayout = new LinearLayout(getContext());
            mainAbilityLayout.setOrientation(LinearLayout.HORIZONTAL);
            mainAbilityLayout.addView(mainAbilityImg, layoutParams);
            mainAbilityLayout.addView(mainAbilityLabel, layoutParams);

            return mainAbilityLayout;
        }

        private LinearLayout createRarityLayout(GearButton btnTag){
            // Set up the layout params object
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.gravity = Gravity.LEFT;

            // Create the layout for the rarity
            LinearLayout rarityLayout = new LinearLayout(getContext());

            // Get the rarity
            int rarity = btnTag.getRarity();

            // Place filled stars equal to the rarity
            for(int i = 0; i < rarity; i++){
                ImageView filledStar = createImageFromName("star_full", false);
                rarityLayout.addView(filledStar, layoutParams);
            }

            // Fill in the rest of the layout with empty stars
            for(int i = 0; i < GearButton.RARITY_MAX - rarity; i++){
                ImageView filledStar = createImageFromName("star_empty", false);
                rarityLayout.addView(filledStar, layoutParams);
            }

            return rarityLayout;
        }

        /**
         * Creates a layout with the acquisition method and an icon to go with it
         *
         * @param btnTag    The gear whose acquisition to display
         * @return          A layout formatted with the acquisition method information and icon
         */
        private LinearLayout createAcquisitionMethodLayout(GearButton btnTag){
            // Get the acquisition method
            String acquisitionMethod = btnTag.getAcquisitionMethod();

            // Set up the icon and text
            ImageView icon;

            if(acquisitionMethod.contains("Cash")){
                // Tokenize the acquisition method
                StringTokenizer tokens = new StringTokenizer(acquisitionMethod, " ");

                acquisitionMethod = tokens.nextToken();
                icon = createImageFromName("cash", false);
                acquisitionMethod = tokens.nextToken();
            }
            else if(acquisitionMethod.equals("---")){
                icon = createImageFromName("random", true);
            }
            else if(btnTag.getBrand().equals("amiibo") || acquisitionMethod.equals("Callie") || acquisitionMethod.equals("Marie")){
                String formattedAcquisition = "amiibo_" + acquisitionMethod.toLowerCase().replace(' ', '_');
                icon = createImageFromName(formattedAcquisition, false);
            }
            else{
                icon = createImageFromName(acquisitionMethod.toLowerCase().replace(' ', '_'), false);
            }

            acquisitionMethod = acquisitionMethod.replace("Splatoon 2 ", "").replace("Splatoon ", "");

            TextView label = Macros.createSplatoonTextView(acquisitionMethod, getContext());

            // Create the layout for the rarity
            LinearLayout acquisitionLayout = new LinearLayout(getContext());

            // Set up the layout params object
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.gravity = Gravity.RIGHT;

            acquisitionLayout.addView(icon, layoutParams);
            acquisitionLayout.addView(label, layoutParams);

            return acquisitionLayout;

        }

        /**
         * Creates the GUI for the brand bias popup
         *
         * @param brandBiasPopup    The popup to create the GUI for
         * @param brand             The brand whose information will be displayed
         * @param db                A databse containing brand information
         */
        private void setupBrandPopup(PopupDialog brandBiasPopup, String brand, SQLiteDatabase db){
            // Get the brand's common and uncommon abilities
            Cursor biasCursor = db.rawQuery("SELECT " + GearContract.GearEntry.COLUMN_COMMON_ABILITY +
                    ", " + GearContract.GearEntry.COLUMN_UNCOMMON_ABILITY + " FROM " +
                    GearContract.GearEntry.TABLE_BRANDS + " WHERE " +
                    GearContract.GearEntry.COLUMN_BRAND + " = ?",new String[] {brand});
            biasCursor.moveToNext();
            int commonRowId = biasCursor.getInt(0);
            int uncommonRowId = biasCursor.getInt(1);

            // Create an ImageView for the common ability
            Cursor commonCursor = db.rawQuery("SELECT " + GearContract.GearEntry.COLUMN_ABILITY +
                    " FROM " + GearContract.GearEntry.TABLE_ABILITIES + " WHERE " +
                    GearContract.GearEntry._ID + " = ?", new String[] {Integer.toString(commonRowId)});
            commonCursor.moveToNext();
            String commonAbility = commonCursor.getString(0);
            ImageView commonImg = createImageFromName(commonAbility, true);
            commonCursor.close();

            // Create the labels for the common ability
            TextView commonLabel = Macros.createSplatoonTextView(commonAbility, getContext());
            commonLabel.setTextColor(Color.parseColor(PLUS_COLOR));

            TextView plusLabel = Macros.createSplatoonTextView("+",getContext());
            plusLabel.setTextSize(PLUS_MINUS_SIZE);
            plusLabel.setTextColor(Color.parseColor(PLUS_COLOR));

            // Create the ImageView for the uncommon ability
            Cursor uncommonCursor = db.rawQuery("SELECT " + GearContract.GearEntry.COLUMN_ABILITY +
                    " FROM " + GearContract.GearEntry.TABLE_ABILITIES + " WHERE " +
                    GearContract.GearEntry._ID + " = ?", new String[] {Integer.toString(uncommonRowId)});
            uncommonCursor.moveToNext();
            String uncommonAbility = uncommonCursor.getString(0);
            ImageView uncommonImg = createImageFromName(uncommonAbility, true);
            uncommonCursor.close();

            // Create the labels for the uncommon ability
            TextView uncommonLabel = Macros.createSplatoonTextView(uncommonAbility, getContext());
            uncommonLabel.setTextColor(Color.parseColor(MINUS_COLOR));

            TextView minusLabel = Macros.createSplatoonTextView("-", getContext());
            minusLabel.setTextSize(PLUS_MINUS_SIZE);
            minusLabel.setTextColor(Color.parseColor(MINUS_COLOR));

            // Set up the layout params object
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.gravity = Gravity.CENTER_VERTICAL;

            // Layout for brand's common ability
            LinearLayout commonLayout = new LinearLayout(getContext());
            commonLayout.setOrientation(LinearLayout.HORIZONTAL);
            commonLayout.addView(plusLabel, layoutParams);
            commonLayout.addView(commonImg, layoutParams);
            commonLayout.addView(commonLabel, layoutParams);

            // Layout for brand's uncommon ability
            LinearLayout uncommonLayout = new LinearLayout(getContext());
            uncommonLayout.setOrientation(LinearLayout.HORIZONTAL);
            uncommonLayout.addView(minusLabel, layoutParams);
            uncommonLayout.addView(uncommonImg, layoutParams);
            uncommonLayout.addView(uncommonLabel, layoutParams);

            // Layout for brand bias popup
            LinearLayout biasContainerLayout = new LinearLayout(getContext());
            biasContainerLayout.setOrientation(LinearLayout.VERTICAL);
            biasContainerLayout.addView(commonLayout, layoutParams);
            biasContainerLayout.addView(uncommonLayout, layoutParams);
            brandBiasPopup.setContentView(biasContainerLayout);
        }

        private void debugReadDatabase(SQLiteDatabase db){

            String[] projection = {
                    GearContract.GearEntry._ID,
                    GearContract.GearEntry.COLUMN_GNAME,
                    GearContract.GearEntry.COLUMN_TYPE_ID,
                    GearContract.GearEntry.COLUMN_SELECTED
            };

            Cursor cursor = db.query(GearContract.GearEntry.TABLE_GEAR, projection, null, null, null, null, null);

            //Cursor cursor = db.rawQuery("PRAGMA table_info(" + GearContract.GearEntry.TABLE_GEAR + ")", null);

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