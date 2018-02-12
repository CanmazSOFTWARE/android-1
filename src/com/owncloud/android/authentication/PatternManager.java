/**
 *   ownCloud Android client application
 *
 *   @author Shashvat Kedia
 *   Copyright (C) 2018 ownCloud GmbH.
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License version 2,
 *   as published by the Free Software Foundation.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.owncloud.android.authentication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.view.WindowManager;

import com.owncloud.android.BuildConfig;
import com.owncloud.android.MainApp;
import com.owncloud.android.ui.activity.PatternLockActivity;

import java.util.HashSet;
import java.util.Set;

public class PatternManager{

    private static final Set<Class> sExemptOfPatternActivites;
    private static int PATTERN_TIMEOUT = 1000;
    private Long timeStamp = 0l;
    private int visibleAcctivitiesCounter = 0;

    static {
        sExemptOfPatternActivites = new HashSet<Class>();
        sExemptOfPatternActivites.add(PatternLockActivity.class);
    }

    public static PatternManager mPatternManagerInstance = null;

    public static PatternManager getPatternManager(){
        if(mPatternManagerInstance == null){
            mPatternManagerInstance = new PatternManager();
        }
        return mPatternManagerInstance;
    }

    protected PatternManager(){
    }

    public void onActivityCreated(Activity activity){
        if(!BuildConfig.DEBUG){
            if(patternIsEnabled()){
                activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
            }
            else{
                activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
            }
        }
    }

    private boolean patternIsEnabled(){
        SharedPreferences appPrefs = PreferenceManager.getDefaultSharedPreferences(MainApp.getAppContext());
        return appPrefs.getBoolean(PatternLockActivity.PREFERENCE_SET_PATTERN,false);
    }

    public void onActivityStarted(Activity activity){
        if (!sExemptOfPatternActivites.contains(activity.getClass()) &&
                patternShouldBeRequested()
                ){
            Intent i = new Intent(MainApp.getAppContext(), PatternLockActivity.class);
            i.setAction(PatternLockActivity.ACTION_CHECK);
            activity.startActivity(i);
        }
        visibleAcctivitiesCounter++;
    }

    private boolean patternShouldBeRequested(){
        if ((System.currentTimeMillis() - timeStamp) > PATTERN_TIMEOUT &&
                visibleAcctivitiesCounter <= 0
                ){
            return patternIsEnabled();
        }
        return false;
    }

    private void setUnlockTimestamp() {
        timeStamp = System.currentTimeMillis();
    }

    public void onActivityStopped(Activity activity) {
        if (visibleAcctivitiesCounter > 0) {
            visibleAcctivitiesCounter--;
        }
        setUnlockTimestamp();
        PowerManager powerMgr = (PowerManager) activity.getSystemService(Context.POWER_SERVICE);
        if (patternIsEnabled() && powerMgr != null && !powerMgr.isScreenOn()) {
            activity.moveTaskToBack(true);
        }
    }

}
