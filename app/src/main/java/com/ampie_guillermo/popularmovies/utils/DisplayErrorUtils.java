package com.ampie_guillermo.popularmovies.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import java.text.MessageFormat;

/**
 * DisplayErrorUtils: A few method to notify and register error conditions
 */

public class DisplayErrorUtils {

  private DisplayErrorUtils() {
  }

  // TODO: 3/3/18: Check that these functions are called from UI thread
  public static void showErrorMessage(String LOG_TAG, Context context, int errorResId,
      Exception exception) {
    String errorMessage = MessageFormat.format("{0}: {1}",
        context.getString(errorResId),
        exception.getMessage());

    // Show & log error message
    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show();
    Log.e(LOG_TAG, errorMessage);

  }

  public static void showErrorMessage(String LOG_TAG, Context context, int errorResId,
      String errorCondition) {
    String errorMessage = MessageFormat.format("{0}: {1}",
        context.getString(errorResId),
        errorCondition);

    // Show & log error message
    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show();
    Log.e(LOG_TAG, errorMessage);

  }

  public static void showErrorMessage(String LOG_TAG, Context context, int errorResId) {
    String errorMessage = MessageFormat.format("{0}",
        context.getString(errorResId));

    // Show & log error message
    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show();
    Log.e(LOG_TAG, errorMessage);

  }
}
