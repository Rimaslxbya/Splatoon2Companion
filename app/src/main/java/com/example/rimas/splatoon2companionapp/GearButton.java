package com.example.rimas.splatoon2companionapp;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.StringTokenizer;

/**
 * Created by Rimas on 9/27/2017.
 */

public class GearButton extends android.support.v7.widget.AppCompatImageButton implements Comparable<GearButton>{

    private String name;
    private String brand;
    private String acquisitionMethod;
    private boolean availability;
    private String ability;
    private String type;
    private int rarity;
    private int imageIndex;

    public GearButton(Context context) {
        super(context);
    }

    /*======================**
     * Getters and Setters
    **======================*/

    public String getName() {
        return name;
    }

    public void setName(String _name) {
        name = _name;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String _brand) {
        brand = _brand;
    }

    public String getAcquisitionMethod() {
        return acquisitionMethod;
    }

    public void setAcquisitionMethod(String _acquisitionMethod){
        acquisitionMethod = _acquisitionMethod;
    }

    public boolean getAvailability() {
        return availability;
    }

    public void setAvailability(boolean _availability) {
        availability = _availability;
    }

    public String getAbility() {
        return ability;
    }

    public void setAbility(String _ability) {
        ability = _ability;
    }

    public String getType() {
        return type;
    }

    public void setType(String _type) {
        type = _type;
    }

    public int getRarity() {
        return rarity;
    }

    public void setRarity(int _rarity) {
        rarity = _rarity;
    }

    public int getImageIndex() {
        return imageIndex;
    }

    public void setImageIndex(int _imageIndex) {
        imageIndex = _imageIndex;
    }

    @Override
    public void setImageResource(int _imageIndex) {
        super.setImageResource(_imageIndex);
        setImageIndex(_imageIndex);
    }

    @Override
    public int compareTo(@NonNull GearButton gearButton) {

        return gearButton.getName().compareToIgnoreCase(name);
    }

    public void setToggledState(boolean state){
        if(state) {
            setBackgroundResource(R.drawable.splat);
            setTag("Checked");
        }
        else {
            setBackgroundResource(0);
            setTag("Unchecked");
        }
    }

    public boolean getToggledState(){
        return getTag() == "Checked";
    }

    public boolean flipToggledState(){
        if(getTag() == "Unchecked") {
            setBackgroundResource(R.drawable.splat);
            setTag("Checked");
        }
        else {
            setBackgroundResource(0);
            setTag("Unchecked");
        }

        return getToggledState();
    }
}
