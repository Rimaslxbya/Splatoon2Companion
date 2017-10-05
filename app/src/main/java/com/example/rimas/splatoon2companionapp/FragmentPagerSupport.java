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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActionBar actionBar = getActionBar();
        setContentView(R.layout.fragment_pager);

        mAdapter = new MyAdapter(getSupportFragmentManager());

        mPager = (ViewPager)findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);

        // Creates all the gear buttons using the csv files
        createGear();

        // Open the database
        GearDbHelper mDbHelper = new GearDbHelper(getApplicationContext());
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // If the database is empty, create and populate the database
        if(!mDbHelper.doesTableExist(db, GearContract.GearEntry.TABLE_GEAR)) {
            mDbHelper.onCreate(db);
            db.close();
            db = mDbHelper.getWritableDatabase();
        }

        // Close the database
        db.close();

        // Set up tab layout
        TabLayout tabby = (TabLayout) findViewById(R.id.tab_layout);
        tabby.setupWithViewPager(mPager);
    }

    private void createGear(){
        AssetManager am = this.getAssets();
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

        try {
            line = buffR.readLine();
        }
        catch (IOException e) {
            e.printStackTrace();
            return nextId;
        }

        int id;

        // Add headgear
        for(id = nextId; line != null; id++){
            StringTokenizer tokens = new StringTokenizer(line, ",");
            createGearButton(id, tokens, type, gearMap);
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

        int drawableResource = getId(type + "_" + name.replace(" ", "_"), R.drawable.class);

        // If no drawable resource is found, use the resource at the top
        if(drawableResource == -1) drawableResource = 0;

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
    }

    public static int getId(String resName, Class<?> c) {

        try {
            Field idField = c.getDeclaredField(resName.toLowerCase());
            return idField.getInt(idField);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
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
            gearButtons = new TreeMap<>();
            switch(mNum)
            {
                case 0:
                    addAllGear(R.drawable.head_18k_aviators, R.drawable.head_white_headband, "head_", layout);
                    break;
                case 1:
                    addAllGear(R.drawable.clothing_anchor_sweat, R.drawable.clothing_zink_layered_ls, "clothing_", layout);
                    break;
                case 2:
                    addAllGear(R.drawable.shoes_acerola_rain_boots, R.drawable.shoes_yellow_mesh_sneakers, "shoes_", layout);
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

        private void addAllGear(int firstDrawable, int lastDrawable, String prefix, LinearLayout layout) {
            //the layout on which you are working
            layout.setOrientation(LinearLayout.VERTICAL);

            //Save the dimensions of splat.png to use as the dimensions for the buttons
            final BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inJustDecodeBounds = true;
            BitmapFactory.decodeResource(getResources(), R.drawable.splat, opt);
            final float scale = getResources().getDisplayMetrics().density;

            // Convert from px to dp during assignment
            final int buttonHeight = opt.outHeight * (int) (scale + 0.5f);
            final int buttonWidth = opt.outWidth * (int) (scale + 0.5f);

            for(int j = 0; j <= (lastDrawable)/4; j++) {
                LinearLayout row = new LinearLayout(this.getContext());
                row.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));

                for (int i = firstDrawable + (j * 4); i < (firstDrawable + (j * 4)) + 4 && i <= lastDrawable; i++) {
                    // Identify if Headgear
                    String name = getResources().getResourceEntryName(i);

                    if (!name.contains(prefix))
                        continue;

                    //set the properties for button
                    GearButton btnTag = new GearButton(this.getContext());
                    btnTag.setLayoutParams(new LinearLayout.LayoutParams(buttonWidth, buttonHeight));
                    btnTag.setName(name);
                    btnTag.setType(prefix);
                    btnTag.setImageResource(i);
                    btnTag.setBackgroundResource(0);
                    btnTag.setId(i+1 + (j*4));
                    btnTag.setTag("Unchecked");

                    gearButtons.put(btnTag.getName(), btnTag);

                    //add button to the layout
                    row.addView(btnTag);
                }

                layout.addView(row);

                if(j > 50) break;
            }

            // Load the checked state of all the gear buttons created in this method
            loadGearCheckedState(prefix.substring(0, prefix.length()-1));

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

        private void insertGearEntries() {
            GearDbHelper mDbHelper = new GearDbHelper(getContext());
            SQLiteDatabase db = mDbHelper.getWritableDatabase();

            for(GearButton gear : gearButtons.values()) {
                int toggled = gear.getTag() == "Checked" ? 1 : 0;

                ContentValues values = new ContentValues();
                values.put(GearContract.GearEntry.COLUMN_GNAME, gear.getName());
                values.put(GearContract.GearEntry.COLUMN_TYPE_ID, gear.getType());
                values.put(GearContract.GearEntry.COLUMN_SELECTED, toggled);

                db.insert(GearContract.GearEntry.TABLE_GEAR, null, values);
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

            // If no results were found, populate the database and return
            if(cursor.getCount() == 0) {
                cursor.close();
                db.close();
                insertGearEntries();
                return;
            }

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