/**
 * The MIT License
 *
 *	Copyright (c) 2011-2013
 *	Colin Turner (github.com/koolspin)
 *	Guillaume Charhon (github.com/poiuytrez)
 *
 *	Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 *	The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 *	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 *
 */
package com.ksutopia.bbtalks.plugins;

import java.util.ArrayList;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import android.util.Log;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
/**
 * Style and such borrowed from the TTS and PhoneListener plugins
 */
public class P201SpeechToText extends CordovaPlugin {
    private static final String LOG_TAG = P201SpeechToText.class.getSimpleName();
    private static final String TAG = P201SpeechToText.class.getSimpleName();
    private SpeechRecognizer speech = null;
    private Intent recognizerIntent;
    private Context context= this.cordova.getActivity().getApplicationContext();

    private CallbackContext callbackContext;
    private P202LanguageDetailsChecker languageDetailsChecker;

    //@Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
		Boolean isValidAction = true;

    	this.callbackContext= callbackContext;

		// Action selector
    	if ("startRecognize".equals(action)) {
            // recognize speech
            startSpeechRecognitionActivity(args);
            System.out.println("A");
        } else if ("getSupportedLanguages".equals(action)) {
        	getSupportedLanguages();
            System.out.println("B");
        } else {
            // Invalid action
            System.out.println("C");
        	this.callbackContext.error("Unknown action: " + action);
        	isValidAction = false;
        }

        return isValidAction;

    }

    // Get the list of supported languages
    private void getSupportedLanguages() {
    	if (languageDetailsChecker == null){
    		languageDetailsChecker = new P202LanguageDetailsChecker(callbackContext);
    	}
    	// Create and launch get languages intent
    	Intent detailsIntent = new Intent(RecognizerIntent.ACTION_GET_LANGUAGE_DETAILS);
    	cordova.getActivity().sendOrderedBroadcast(detailsIntent, null, languageDetailsChecker, null, Activity.RESULT_OK, null, null);

	}

	/**
     * Fire an intent to start the speech recognition activity.
     *
     * @param args Argument array with the following string args: [req code][number of matches][prompt string]
     */
    private void startSpeechRecognitionActivity(JSONArray args) {
        int maxMatches = 0;
        String prompt = "";
        String language = Locale.getDefault().toString();

        try {
            if (args.length() > 0) {
            	// Maximum number of matches, 0 means the recognizer decides
                String temp = args.getString(0);
                maxMatches = Integer.parseInt(temp);
            }
            if (args.length() > 1) {
            	// Optional text prompt
                prompt = args.getString(1);
            }
            if (args.length() > 2) {
            	// Optional language specified
            	language = args.getString(2);
            }
        }
        catch (Exception e) {
            Log.e(LOG_TAG, String.format("startSpeechRecognitionActivity exception: %s", e.toString()));
        }

        // Create the intent and set parameters
        speech = SpeechRecognizer.createSpeechRecognizer(context);
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.getPackageName());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);

        if (maxMatches > 0)
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, maxMatches);
        if (!prompt.equals(""))
        	recognizerIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, prompt);
        speech.setRecognitionListener(listener);
        speech.startListening(recognizerIntent);
    }


    RecognitionListener listener = new RecognitionListener() {
        @Override
        public void onResults(Bundle results) {
            ArrayList<String> voiceResults = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            if (voiceResults == null) {
                Log.e(TAG, "No voice results");
            } else {
                Log.d(TAG, "Printing matches: ");
                for (String match : voiceResults) {
                    Log.d(TAG, match);
                }
            }
        }

        @Override
        public void onReadyForSpeech(Bundle params) {
            Log.d(TAG, "Ready for speech");
        }

        @Override
        public void onError(int error) {
            Log.d(TAG,
                    "Error listening for speech: " + error);
        }

        @Override
        public void onBeginningOfSpeech() {
            Log.d(TAG, "Speech starting");
        }

        @Override
        public void onBufferReceived(byte[] buffer) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onEndOfSpeech() {
            // TODO Auto-generated method stub

        }

        @Override
        public void onEvent(int eventType, Bundle params) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onPartialResults(Bundle partialResults) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onRmsChanged(float rmsdB) {
            // TODO Auto-generated method stub

        }
    };

}
