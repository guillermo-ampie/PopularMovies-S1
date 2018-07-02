package com.ampie_guillermo.popularmovies.database;

import android.net.Uri;
import net.simonvt.schematic.annotation.ContentProvider;
import net.simonvt.schematic.annotation.ContentUri;
import net.simonvt.schematic.annotation.InexactContentUri;
import net.simonvt.schematic.annotation.TableEndpoint;

/**
 * Database & Content Provider support with Schematic
 * https://github.com/SimonVT/schematic.git - Automatically generate a
 * ContentProvider backed by an SQLite database.
 */

@ContentProvider(authority = MoviesProvider.AUTHORITY, database = MoviesDatabase.class)
public final class MoviesProvider {

  public static final String AUTHORITY = "com.ampie_guillermo.popularmovies.MoviesProvider";
  static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

  private MoviesProvider() {
  }

  static Uri buildUri(String... paths) {
    Uri.Builder builder = BASE_CONTENT_URI.buildUpon();
    for (String path : paths) {
      builder.appendPath(path);
    }
    return builder.build();
  }

  interface Path {

    String MOVIES = "movies";
  }

  @TableEndpoint(table = MoviesDatabase.MOVIES_TABLE)
  public static final class Movies {

    private Movies() {
    }

    @ContentUri(path = Path.MOVIES,
        type = "vnd.android.cursor.dir/movie")
    public static final Uri CONTENT_URI = buildUri(Path.MOVIES);

    @InexactContentUri(name = "MOVIE_ID",
        path = Path.MOVIES + "/#",
        type = "vnd.android.cursor.item/movie",
        whereColumn = MovieColumns.MOVIE_ID,
        pathSegment = 1)
    public static Uri withId(String id) {
      return buildUri(Path.MOVIES, id);
    }
  }
}
