package rikka.searchbyimage.database.table;

/**
 * Created by Rikka on 2016/1/24.
 */
public class CustomEngineTable {
    public static final String TABLE_NAME = "search_engine";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_ENABLED = "enabled";
    public static final String COLUMN_DATA = "data";

    public static final String SQL_CREATE_ENTRIES = "create table " + TABLE_NAME
            + "("
            + COLUMN_ID + " integer primary key,"
            + COLUMN_ENABLED + " integer,"
            + COLUMN_DATA + " blob"
            + ");";
}
