package rikka.searchbyimage.staticdata;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.databinding.Bindable;
import android.databinding.Observable;
import android.databinding.PropertyChangeRegistry;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

import rikka.searchbyimage.BuildConfig;
import rikka.searchbyimage.R;
import rikka.searchbyimage.database.DatabaseHelper;
import rikka.searchbyimage.database.table.CustomEngineTable;
import rikka.searchbyimage.utils.ParcelableUtils;

/**
 * Created by Rikka on 2016/1/25.
 */
public class SearchEngine implements Observable {
    private int id;
    private int enabled;
    private String name;
    private String upload_url;
    private String post_file_key;
    private int result_open_action = RESULT_OPEN_ACTION.DEFAULT;
    public List<String> post_text_key = new ArrayList<>();
    public List<String> post_text_value = new ArrayList<>();
    public List<Integer> post_text_type = new ArrayList<>();

    @Bindable
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Bindable
    public int getEnabled() {
        return enabled;
    }

    public void setEnabled(int enabled) {
        this.enabled = enabled;
    }

    @Bindable
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Bindable
    public String getUploadUrl() {
        return upload_url;
    }

    public void setUploadUrl(String upload_url) {
        this.upload_url = upload_url;
    }

    @Bindable
    public String getPostFileKey() {
        return post_file_key;
    }

    public void setPostFileKey(String post_file_key) {
        this.post_file_key = post_file_key;
    }

    @Bindable
    public int getResultOpenAction() {
        return result_open_action;
    }

    public void setResultOpenAction(int result_open_action) {
        this.result_open_action = result_open_action;
    }

    @Bindable
    public String getEngineIcon() {
        return getEngineHost() + "/favicon.ico";
    }

    @Bindable
    public String getEngineHost() {
        Uri uri = Uri.parse(upload_url);
        return new Uri.Builder().scheme(uri.getScheme()).authority(uri.getHost()).build().toString();
    }

    private PropertyChangeRegistry pcr = new PropertyChangeRegistry();

    @Override
    public void addOnPropertyChangedCallback(OnPropertyChangedCallback callback) {
        pcr.add(callback);
    }

    @Override
    public void removeOnPropertyChangedCallback(OnPropertyChangedCallback callback) {
        pcr.remove(callback);
    }

    public static class RESULT_OPEN_ACTION {
        public static final int DEFAULT = 0;
        public static final int OPEN_HTML_FILE = 1;
        public static final int BUILD_IN_IQDB = 2;
    }

    private static List<SearchEngine> sList;

    public static synchronized List<SearchEngine> getList(Context context) {
        if (sList == null) {
            sList = loadList(context);
        }
        return sList;
    }

    private static List<SearchEngine> loadList(Context context) {
        List<SearchEngine> list = new ArrayList<>();

        DatabaseHelper dbHelper = DatabaseHelper.instance(context);
        Cursor cursor = dbHelper.getReadableDatabase()
                .query(CustomEngineTable.TABLE_NAME, null, null, null, null, null, null);

        if (cursor.getCount() > 0) {
            int columnId = cursor.getColumnIndex(CustomEngineTable.COLUMN_ID);
            int columnData = cursor.getColumnIndex(CustomEngineTable.COLUMN_DATA);
            int columnEnabled = cursor.getColumnIndex(CustomEngineTable.COLUMN_ENABLED);

            cursor.moveToFirst();
            do {
                SearchEngine item = ParcelableUtils.unmarshall(cursor.getBlob(columnData), SearchEngineParcelable.CREATOR).data;
                item.id = cursor.getInt(columnId);
                item.enabled = cursor.getInt(columnEnabled);
                list.add(item);
            } while (cursor.moveToNext());
        }

        cursor.close();

        return list;
    }

    public static int getAvailableId() {
        if (sList.size() <= 6) {
            return 5 + 1;
        }

        int id = 5 + 1;
        while (true) {
            boolean do_next = false;
            for (SearchEngine item : sList) {
                if (item.id == id) {
                    do_next = true;
                    break;
                }
            }
            if (!do_next) {
                return id;
            } else {
                id++;
            }
        }
    }

    public static SearchEngine getItemById(int id) {
        for (SearchEngine item : sList) {
            if (item.id == id) {
                return item;
            }
        }
        return null;
    }

    private static final String BUILD_IN_ENGINE_URL[] = {
            "https://www.google.com/searchbyimage/upload",
            "http://image.baidu.com/pictureup/uploadwise",
            "https://iqdb.org/",
            "https://www.tineye.com/search",
            "http://saucenao.com/search.php",
            "http://www.ascii2d.net/search/file"
    };

    private static final String BUILD_IN_ENGINE_NAME[] = {
            "Google",
            "Baidu",
            "iqdb",
            "TinEye",
            "SauceNAO",
            "ascii2d"
    };

    private static final String BUILD_IN_ENGINE_FILE_KEY[] = {
            "encoded_image",
            "upload",
            "file",
            "image",
            "file",
            "file"
    };

    private static final int BUILD_IN_ENGINE_OPEN_ACTION[] = {
            RESULT_OPEN_ACTION.DEFAULT,
            RESULT_OPEN_ACTION.DEFAULT,
            RESULT_OPEN_ACTION.BUILD_IN_IQDB,
            RESULT_OPEN_ACTION.DEFAULT,
            RESULT_OPEN_ACTION.OPEN_HTML_FILE,
            RESULT_OPEN_ACTION.DEFAULT
    };

    public static final int DEFAULT_ENGINE_ICON = R.drawable.ic_icon_other_24dp;

    public static final int BUILD_IN_ENGINE_ICONS[] = {
            R.drawable.ic_icon_google_24dp,
            DEFAULT_ENGINE_ICON,
            DEFAULT_ENGINE_ICON,
            DEFAULT_ENGINE_ICON,
            DEFAULT_ENGINE_ICON,
            DEFAULT_ENGINE_ICON
    };

    public final static int SITE_GOOGLE = 0;
    public final static int SITE_BAIDU = 1;
    public final static int SITE_IQDB = 2;
    public final static int SITE_TINEYE = 3;
    public final static int SITE_SAUCENAO = 4;
    public final static int SITE_ASCII2D = 5;
    public final static int SITE_CUSTOM_START = 6;

    /**
     * add built-in engines to database
     * <p/>
     * if BuildConfig.hideOtherEngine is true, only add Google engine
     * otherwise add all the engines
     *
     * @param db the database
     */
    public static void addBuildInEngines(SQLiteDatabase db) {

        for (int i = 0; i < (BuildConfig.hideOtherEngine ? 1 : 6); i++) {
            SearchEngineParcelable parcelable = new SearchEngineParcelable();
            parcelable.data.id = i;
            parcelable.data.enabled = 1;
            parcelable.data.name = BUILD_IN_ENGINE_NAME[i];
            parcelable.data.upload_url = BUILD_IN_ENGINE_URL[i];
            parcelable.data.post_file_key = BUILD_IN_ENGINE_FILE_KEY[i];
            parcelable.data.result_open_action = BUILD_IN_ENGINE_OPEN_ACTION[i];

            switch (i) {
                case SITE_IQDB:
                    parcelable.data.post_text_key.add("service");
                    parcelable.data.post_text_value.add("");
                    parcelable.data.post_text_type.add(-1);

                    parcelable.data.post_text_key.add("forcegray");
                    parcelable.data.post_text_value.add("");
                    parcelable.data.post_text_type.add(-1);
                    break;
                case SITE_SAUCENAO:
                    parcelable.data.post_text_key.add("hide");
                    parcelable.data.post_text_value.add("");
                    parcelable.data.post_text_type.add(-1);

                    parcelable.data.post_text_key.add("database");
                    parcelable.data.post_text_value.add("");
                    parcelable.data.post_text_type.add(-1);
                    break;
            }

            addEngineToDb(db, parcelable, i);
        }

    }

    public static void addEngineToDb(Context context, SearchEngineParcelable parcelable, int id) {
        SQLiteDatabase db = DatabaseHelper.instance(context).getWritableDatabase();

        addEngineToDb(db, parcelable, id);
    }

    /**
     * add engine to database
     * if the id already exist,replace the old one
     *
     * @param db         SQLiteDatabase
     * @param parcelable engine need to add
     * @param id         engine id
     */
    public static void addEngineToDb(SQLiteDatabase db, SearchEngineParcelable parcelable, int id) {

        ContentValues values = new ContentValues();
        values.put(CustomEngineTable.COLUMN_ID, id);
        values.put(CustomEngineTable.COLUMN_ENABLED, 1);
        values.put(CustomEngineTable.COLUMN_DATA, ParcelableUtils.marshall(parcelable));

        if (isEngineIDExist(id, db)) {
            db.replace(CustomEngineTable.TABLE_NAME, null, values);
        } else {
            db.insert(CustomEngineTable.TABLE_NAME, null, values);
        }
    }

    public static void addEngineToList(SearchEngine data) {
        addEngineToList(data, sList);
    }

    public static void addEngineToList(SearchEngine data, List<SearchEngine> list) {
        list.add(data);
    }

    /**
     * declare the Engine ID already used
     *
     * @param id the engine ID
     * @param db SQLiteDatabase
     * @return if the ID exist return true,otherwise return false
     */
    private static boolean isEngineIDExist(int id, SQLiteDatabase db) {
        Cursor cursor = db.query(
                CustomEngineTable.TABLE_NAME,
                new String[]{CustomEngineTable.COLUMN_ID},
                CustomEngineTable.COLUMN_ID + "=?",
                new String[]{String.valueOf(id)},
                null, null, null);
        boolean ans = cursor.getCount() == 1;
        cursor.close();
        return ans;
    }
}
