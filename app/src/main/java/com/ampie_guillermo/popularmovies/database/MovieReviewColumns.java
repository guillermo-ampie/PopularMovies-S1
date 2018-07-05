package com.ampie_guillermo.popularmovies.database;

import net.simonvt.schematic.annotation.AutoIncrement;
import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.PrimaryKey;
import net.simonvt.schematic.annotation.References;

public interface MovieReviewColumns {

  @DataType(DataType.Type.INTEGER) @PrimaryKey
  @AutoIncrement
  String ID = "_id";

  @DataType(DataType.Type.TEXT) @References(table = MoviesDatabase.MOVIES_TABLE, column = MovieColumns.MOVIE_ID)
  String MOVIE_ID = "movie_id";

  @DataType(DataType.Type.TEXT)
  String AUTHOR = "author";

  @DataType(DataType.Type.TEXT)
  String CONTENT = "content";

}
