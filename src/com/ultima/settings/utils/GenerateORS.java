/*
 * Copyright (C) 2014 Matt Booth (Kryten2k35).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ultima.settings.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.ultima.settings.R;

public class GenerateORS extends AsyncTask<Void, String, Boolean> implements Constants{
    
    public final String TAG = this.getClass().getSimpleName();
    
    private Context mContext;
    ProgressDialog mLoadingDialog;
    private StringBuilder mScript = new StringBuilder();
    private static String mScriptFile = "/cache/recovery/openrecoveryscript";
    private static String NEW_LINE = "\n";   
    private String mFileLocation;
    private String mScriptOutput;
    private String mTempFolder;

    public GenerateORS(Context context, String fileLocation){
        mContext = context;
        mFileLocation = fileLocation;
    }

    protected void onPreExecute() {
        // Show dialog
        mLoadingDialog = new ProgressDialog(mContext);
        mLoadingDialog.setCancelable(false);
        mLoadingDialog.setIndeterminate(true);
        mLoadingDialog.setMessage(mContext.getString(R.string.rebooting));
        mLoadingDialog.show();

        // Replace extSdCard for external_sd on TWRP recoveries
        mFileLocation = mFileLocation.replaceAll("extSdCard", "external_sd");
        if(Preferences.getBootAniWipeCache()){
        	mScript.append("wipe cache" + NEW_LINE);
        	mScript.append("wipe dalvik" + NEW_LINE);
        }
        mScript.append("install " + mFileLocation);
        mScriptOutput = mScript.toString();
        
        mTempFolder = mContext.getResources().getString(R.string.bootani_temp_folder);
    }
    
    @Override
    protected Boolean doInBackground(Void... params) {
        FileWriter fstream;
        BufferedWriter out;
        String tempFile = SDCARD + mTempFolder + "openrecoveryscript";
        try {
            File file = new File(tempFile);
            File tempFolder = new File(SDCARD + mTempFolder);
            if(!tempFolder.exists()){
            	tempFolder.mkdirs();
            }
            if(!file.exists()){
                file.createNewFile();
            }
            fstream = new FileWriter(tempFile);
            out = new BufferedWriter(fstream);
            out.append(mScriptOutput);
            out.close();
            
            Root.shell("mkdir -p /cache/recovery/" + " && cp " + tempFile + " " + mScriptFile + " && rm -rf " + tempFile);
            return true;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
        
    }
    @Override
    protected void onPostExecute(Boolean value) {
        mLoadingDialog.cancel();
        Root.backupBootAnimation();
        Root.recovery();
    }
}
