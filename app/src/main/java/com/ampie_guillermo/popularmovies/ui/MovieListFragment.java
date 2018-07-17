package com.ampie_guillermo.popularmovies.ui;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CursorAdapter;
import com.ampie_guillermo.popularmovies.BuildConfig;
import com.ampie_guillermo.popularmovies.R;
import com.ampie_guillermo.popularmovies.model.Movie;
import com.ampie_guillermo.popularmovies.ui.BaseMovieListFragment.FetchMoviesListener;
import com.ampie_guillermo.popularmovies.ui.adapter.MovieAdapter;
import com.ampie_guillermo.popularmovies.utils.MyPMErrorUtils;
import com.ampie_guillermo.popularmovies.utils.UIErrorHelper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MovieListFragment
    extends BaseMovieListFragment
    implements LoaderManager.LoaderCallbacks<ArrayList<Movie>>,
    FetchMoviesListener {

  private static final String LOG_TAG = MovieListFragment.class.getSimpleName();
  private static final int MOVIES_RESULT_SIZE = 20;
  private static final int MOVIE_LIST_LOADER_ID = 1000;
  private ArrayList<Movie> mMovieList;
  private MovieAdapter mMovieAdapter;

  public MovieListFragment() {
    mMovieList = new ArrayList<>(MOVIES_RESULT_SIZE);
    registerFetchMoviesListener(this);
  }

  public void onClickRetry(View view) {
    if (mMovieList.isEmpty()) {
      final Bundle bundle = new Bundle();
      bundle.putString(EXTRA_MOVIE_SORTING_METHOD, getSortingMethodParam());
      Log.v(LOG_TAG, "++++++++++onResume(): restarting LOADER");
      getLoaderManager().restartLoader(MOVIE_LIST_LOADER_ID, bundle, this);
    }
  }

  /**
   * Instantiate and return a new Loader for the given ID.
   *
   * @param id The ID whose loader is to be created.
   * @param args Any arguments supplied by the caller.
   * @return Return a new Loader instance that is ready to start loading.
   */
  @Override
  public Loader<ArrayList<Movie>> onCreateLoader(int id, Bundle args) {

    switch (id) {
      case MOVIE_LIST_LOADER_ID:
        return new MovieListLoader(getActivity(), args);
      default:
        throw new IllegalArgumentException(
            String.format("%s: %d", getString(R.string.error_unknown_loader_id), id));
    }
  }

  /**
   * Called when a previously created loader has finished its load.  Note that normally an
   * application is <em>not</em> allowed to commit fragment transactions while in this call, since
   * it can happen after an activity's state is saved.  See {link FragmentManager#beginTransaction()
   * FragmentManager.openTransaction()} for further discussion on this.
   *
   * <p>This function is guaranteed to be called prior to the release of the last data that was
   * supplied for this Loader.  At this point you should remove all use of the old data (since it
   * will be released soon), but should not do your own release of the data since its Loader owns it
   * and will take care of that.  The Loader will take care of management of its data so you don't
   * have to.  In particular:
   *
   * <ul> <li> <p>The Loader will monitor for changes to the data, and report them to you through
   * new calls here.  You should not monitor the data yourself.  For example, if the data is a
   * {@link Cursor} and you place it in a {@link CursorAdapter}, use the {link
   * CursorAdapter#CursorAdapter(Context, * Cursor, int)} constructor <em>without</em> passing in
   * either {@link CursorAdapter#FLAG_AUTO_REQUERY} or {@link CursorAdapter#FLAG_REGISTER_CONTENT_OBSERVER}
   * (that is, use 0 for the flags argument).  This prevents the CursorAdapter from doing its own
   * observing of the Cursor, which is not needed since when a change happens you will get a new
   * Cursor throw another call here. <li> The Loader will release the data once it knows the
   * application is no longer using it.  For example, if the data is a {@link Cursor} from a {@link
   * CursorLoader}, you should not call close() on it yourself.  If the Cursor is being placed in a
   * {@link CursorAdapter}, you should use the {@link CursorAdapter#swapCursor(Cursor)} method so
   * that the old Cursor is not closed. </ul>
   *
   * @param loader The Loader that has finished.
   * @param data The data generated by the Loader.
   */
  @Override
  public void onLoadFinished(Loader<ArrayList<Movie>> loader, ArrayList<Movie> data) {
    // Hide the progress bar
    hideLoadingIndicator();

    Log.v(LOG_TAG, "++++++++++ onLoadFinished()");
    if (data != null) {
      Log.v(LOG_TAG, "++++++++++ Adding the new data fetched");
      showMovieListDisplay();
      mMovieList = new ArrayList<>(data);
      mMovieAdapter.setMovieList(data);
    } else {
      showErrorDisplay();
      // We could have an error, inspect the loader in order to get the error an give feedback
      // to the user
      final UIErrorHelper uiErrorHelper = new UIErrorHelper(
          ((MovieListLoader) loader).getUiError());
      if (uiErrorHelper.isErrorEnabled()) {
        if (uiErrorHelper.isExceptionErrorConditionEnabled()) {
          MyPMErrorUtils.showErrorMessage(LOG_TAG,
              Objects.requireNonNull(getContext()),
              uiErrorHelper.getErrorMsgResId(),
              uiErrorHelper.getExceptionErrorMsg());
        } else {
          MyPMErrorUtils.showErrorMessage(LOG_TAG,
              Objects.requireNonNull(getContext()),
              uiErrorHelper.getErrorMsgResId());
        }
      }
      // User has already gotten feedback about the error, let's clear it
//      uiErrorHelper.clear();
    }
  }

  /**
   * Called when a previously created loader is being reset, and thus
   * making its data unavailable.  The application should at this point
   * remove any references it has to the Loader's data.
   *
   * @param loader The Loader that is being reset.
   */
  @Override
  public void onLoaderReset(Loader<ArrayList<Movie>> loader) {
//    mMovieAdapter.setMovieList(null);
  }

  @Override
  public void getMovies() {
    final Bundle bundle = new Bundle();
    bundle.putString(EXTRA_MOVIE_SORTING_METHOD, getSortingMethodParam());

//    LoaderManager lm = getLoaderManager();
//    Loader<ArrayList<Movie>> loader = lm.getLoader(MOVIE_LIST_LOADER_ID);
//    if (loader == null) {
//      Log.d(LOG_TAG, "++++++++++ Initializing the loader");
//      lm.initLoader(MOVIE_LIST_LOADER_ID, bundle, this);
//    } else {
//      // restartLoader() will start a new or restart an existing loader
//      Log.d(LOG_TAG, "++++++++++ Restarting the loader");
//      lm.restartLoader(MOVIE_LIST_LOADER_ID, bundle, this);
//    }
    // Show the progress bar
//    showLoadingIndicator();

    /**
     * In this App, we want to persist and cache the movie list fetched from the Internet upon
     * configuration changes (rotation of device) and in onPause() / onResume(). The persistence
     * seems appropriate here because the data(movies and its values) more or less is not
     * "real-time" data. So we need to use initLoader() instead of restartLoader() because
     * initLoader() will reuse the last created loader if a loader with the specified ID already
     * exists or will trigger onCreateLoader() if a loader with the specified ID does not exist yet.
     */
    getLoaderManager().initLoader(MOVIE_LIST_LOADER_ID, bundle, this);
  }

  @Override
  public Movie getSelectedMovie(final int clickedItemIndex) {
    return mMovieList.get(clickedItemIndex);
  }

  @Override
  public int movieListSize() {
    return mMovieList.size();
  }

  @Override
  public void setupRecyclerViewAdapter() {
    // Set an empty adapter because the movies have not been fetched yet
    mMovieAdapter = new MovieAdapter(this);
    mRvMovieGrid.setAdapter(mMovieAdapter);
  }

  private static class MovieListLoader extends AsyncTaskLoader<ArrayList<Movie>> {

    private static final String LOG_TAG = MovieListLoader.class.getSimpleName();
    private final Bundle mArgs;
    private final UIErrorHelper mUiError;
    private ArrayList<Movie> mCachedMovieList;

    MovieListLoader(final Context context, final Bundle args) {
      super(context);
      mArgs = args;
      mCachedMovieList = new ArrayList<>(MovieListFragment.MOVIES_RESULT_SIZE);
      mUiError = new UIErrorHelper();
    }

    UIErrorHelper getUiError() {
      return mUiError;
    }

    @Nullable
    @Override
    public ArrayList<Movie> loadInBackground() {
      Log.v(MovieListLoader.LOG_TAG, "++++++++++ loadInBackground() ");
      final String sortingMethod = mArgs.getString(EXTRA_MOVIE_SORTING_METHOD);

      if (TextUtils.isEmpty(sortingMethod)) {
        mUiError.setErrorResId(R.string.error_missing_sorting_method);
        return null;
      }

      // These two need to be declared outside the try/catch
      // so that they can be closed in the finally block.
      HttpURLConnection urlConnection = null;
//      BufferedReader reader = null;

      // The raw JSON response as a string
      String movieJsonStr;
      // TODO: 7/17/18  Check the nested "try" and the catch statements and the various "return"
      try {
        // Construct the URL for TheMovieDB query
        final String GET = "GET";
        final String MOVIEDB_BASE_URL = "https://api.themoviedb.org/3/discover/movie?";
        final String SORT_BY_PARAM = "sort_by";
        final String API_KEY_PARAM = "api_key";
        final String VOTE_COUNT_PARAM = "vote_count.gte";
        final String VOTE_COUNT = "100"; // Let's get some sense from the valuations

        final Uri builtUri = Uri.parse(MOVIEDB_BASE_URL).buildUpon()
            .appendQueryParameter(SORT_BY_PARAM, sortingMethod)
            .appendQueryParameter(VOTE_COUNT_PARAM, VOTE_COUNT)
            .appendQueryParameter(API_KEY_PARAM, BuildConfig.MOVIE_DB_API_KEY)
            .build();

        final URL url = new URL(builtUri.toString());
        //Log.v(LOG_TAG, builtUri.toString());

        // Create the request to TheMovieDB, and open the connection
        urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod(GET);
        urlConnection.connect();

        // Read the input stream into a String
        try (InputStream inputStream = urlConnection.getInputStream()) {
          final StringBuilder buffer = new StringBuilder();
//        if (inputStream == null) {
//          mUiError.setErrorResId(R.string.error_empty_response);
//          return null;
//        }
          try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            String line;
            while ((line = reader.readLine()) != null) {
              // Append a newline for debugging purposes
              buffer.append(line).append('\n');
            }

            if (buffer.length() == 0) {
              // Stream was empty.  Nothing to do!
              mUiError.setErrorResId(R.string.error_empty_response);
              return null;
            }
            movieJsonStr = buffer.toString();
          }
        }
      } catch (MalformedURLException | ProtocolException e) {
        mUiError.setExceptionErrorMsg(R.string.error_contacting_server, e.getMessage());
        return null;
      } catch (IOException e) {
        mUiError.setExceptionErrorMsg(R.string.error_contacting_server, e.getMessage());
        return null;
      } finally {
        if (urlConnection != null) {
          urlConnection.disconnect();
        }
//        if (reader != null) {
//          try {
//            reader.close();
//          } catch (IOException e) {
//            mUiError.setExceptionErrorMsg(R.string.error_closing_stream, e.getMessage());
//          }
//        }
      }

      try {
        return getMoviesDataFromJson(movieJsonStr);
      } catch (JSONException e) {
        mUiError.setExceptionErrorMsg(R.string.error_processing_json_response, e.getMessage());
      }

      // This will only happen if there was an error getting or parsing the response.
      mUiError.setErrorResId(R.string.error_contacting_server);
      return null;
    }

    /**
     * Subclasses must implement this to take care of loading their data,
     * as per {@link #startLoading()}.  This is not called by clients directly,
     * but as a result of a call to {@link #startLoading()}.
     */
    @Override
    protected void onStartLoading() {
      if (mArgs == null) {
        return;
      }

      if (mCachedMovieList.isEmpty()) {
        Log.v(LOG_TAG, "++++++++++ Forcing a load");
        forceLoad();
      } else {
        Log.v(LOG_TAG, "++++++++++ Delivering a cached result");
        deliverResult(mCachedMovieList);
      }
    }

    /**
     * Sends the result of the load to the registered listener. Should only be called by subclasses.
     *
     * Must be called from the process's main thread.
     *
     * @param data the result of the load
     */
    @Override
    public void deliverResult(@Nullable ArrayList<Movie> data) {
      if (data != null) {
        mCachedMovieList = new ArrayList<>(data);
      }
      super.deliverResult(data);
    }

    private ArrayList<Movie> getMoviesDataFromJson(final String moviesJsonStr)
        throws JSONException {

//      /** Obsolete code!:
//       * We need to specify in the HTTPS request and the XML files
//       * the --movie poster width--, so to avoid a manual
//       * synchronization (so error prone!) of both files, we are using
//       * "res/values/dimens:movie_poster_width" resource for this purpose.
//       * In the https request we need to use the nominal value stored in
//       * "movie_poster_width", not the scaled value returned by
//       * getDimension(), so we must -adjust back- by the screen density factor
//       *
//       */
//            Resources res = getContext().getResources();
//      int moviePosterWidthInPixels = (int) (res.getDimension(R.dimen.movie_poster_width)
//          / res.getDisplayMetrics().density);

      final JSONObject movieJson = new JSONObject(moviesJsonStr);

      final String MOVIE_RESULTS = "results";
      final JSONArray moviesArray = movieJson.getJSONArray(MOVIE_RESULTS);

      final Movie[] movieList = new Movie[moviesArray.length()];

      // These are the names of the JSON objects we need to extract.
      final String MOVIE_ID = "id";
      final String MOVIE_ORIGINAL_TITLE = "original_title";
      final String MOVIE_RELEASE_DATE = "release_date";
      final String MOVIE_OVERVIEW = "overview";
      final String MOVIE_POSTER_PATH = "poster_path";
      final String MOVIE_BACKDROP_PATH = "backdrop_path";
      final String MOVIE_VOTE_AVERAGE = "vote_average";
      final String MOVIE_VOTE_COUNT = "vote_count";

      final String MOVIE_IMAGES_BASE_URI = "https://image.tmdb.org/t/p/w";
      final int moviePosterWidth =
          getContext().getResources().getInteger(R.integer.movie_poster_width);
      final Uri movieImagesBaseUri =
          Uri.parse(MOVIE_IMAGES_BASE_URI + String.valueOf(moviePosterWidth));

      final int moviesCount = moviesArray.length();
      for (int i = 0; i < moviesCount; ++i) {

        // Get the JSON object with the movie data

        final JSONObject currentMovieJson = moviesArray.getJSONObject(i);
        final String id = Integer.toString(currentMovieJson.getInt(MOVIE_ID));
        final String originalTitle = currentMovieJson.getString(MOVIE_ORIGINAL_TITLE);
        final String releaseDate = currentMovieJson.getString(MOVIE_RELEASE_DATE);
        final String overview = currentMovieJson.getString(MOVIE_OVERVIEW);
        final String posterRelativePath = currentMovieJson.getString(MOVIE_POSTER_PATH);
        final String backdropRelativePath = currentMovieJson.getString(MOVIE_BACKDROP_PATH);
        final float voteAverage = (float) currentMovieJson.getDouble(MOVIE_VOTE_AVERAGE);
        final int voteCount = currentMovieJson.getInt(MOVIE_VOTE_COUNT);

        final Uri posterCompleteUri =
            Uri.withAppendedPath(movieImagesBaseUri, posterRelativePath);
        final Uri backdropCompleteUri =
            Uri.withAppendedPath(movieImagesBaseUri, backdropRelativePath);
        /*
         * Store only the movie release year.
         * The format from TheMovieDB for release date: YYYY-MM-DD
         */
        final String releaseYear = (releaseDate.split("-"))[0];

        //Populate our array of movies
        movieList[i] = new Movie.MovieBuilder()
            .setId(id)
            .setOriginalTitle(originalTitle)
            .setReleaseDate(releaseYear)
            .setOverview(overview)
            .setPosterUri(posterCompleteUri)
            .setBackdropUri(backdropCompleteUri)
            .setVoteAverage(voteAverage)
            .setVoteCount(voteCount)
            .build();

//        Log.v (LOG_TAG, originalTitle);
      }
      return new ArrayList<>(Arrays.asList(movieList));
    }
  }

  public static class PopularMovieListFragment extends MovieListFragment {

    private static final String SORT_BY_POPULARITY = "popularity.desc";

    public PopularMovieListFragment() {
      setSortingMethodParam(SORT_BY_POPULARITY);
    }
  }

  public static class RatedMovieListFragment extends MovieListFragment {

    private static final String SORT_BY_RATING = "vote_average.desc";

    public RatedMovieListFragment() {
      setSortingMethodParam(SORT_BY_RATING);
    }
  }
}
