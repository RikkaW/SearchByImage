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
import rikka.searchbyimage.database.DatabaseHelper;
import rikka.searchbyimage.database.table.CustomEngineTable;
import rikka.searchbyimage.utils.ParcelableUtils;

/**
 * Created by Rikka on 2016/1/25.
 */
public class CustomEngine implements Observable {
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
    public String getUpload_url() {
        return upload_url;
    }

    public void setUpload_url(String upload_url) {
        this.upload_url = upload_url;
    }

    @Bindable
    public String getPost_file_key() {
        return post_file_key;
    }

    public void setPost_file_key(String post_file_key) {
        this.post_file_key = post_file_key;
    }

    @Bindable
    public int getResult_open_action() {
        return result_open_action;
    }

    public void setResult_open_action(int result_open_action) {
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

    private static List<CustomEngine> sList;

    public static synchronized List<CustomEngine> getList(Context context) {
        if (sList == null) {
            sList = loadList(context);
        }
        return sList;
    }

    private static List<CustomEngine> loadList(Context context) {
        List<CustomEngine> list = new ArrayList<>();

        addBuildInEngines(context, list);

        DatabaseHelper dbHelper = DatabaseHelper.instance(context);
        Cursor cursor = dbHelper.getReadableDatabase()
                .query(CustomEngineTable.TABLE_NAME, null, null, null, null, null, null);

        if (cursor.getCount() > 0) {
            int columnId = cursor.getColumnIndex(CustomEngineTable.COLUMN_ID);
            int columnData = cursor.getColumnIndex(CustomEngineTable.COLUMN_DATA);
            int columnEnabled = cursor.getColumnIndex(CustomEngineTable.COLUMN_ENABLED);

            cursor.moveToFirst();
            do {
                CustomEngine item = ParcelableUtils.unmarshall(cursor.getBlob(columnData), CustomEngineParcelable.CREATOR).data;
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
            for (CustomEngine item : sList) {
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

    public static CustomEngine getItemById(int id) {
        for (CustomEngine item : sList) {
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

    public final static int SITE_GOOGLE = 0;
    public final static int SITE_BAIDU = 1;
    public final static int SITE_IQDB = 2;
    public final static int SITE_TINEYE = 3;
    public final static int SITE_SAUCENAO = 4;
    public final static int SITE_ASCII2D = 5;

    private static void addBuildInEngines(Context context, List<CustomEngine> list) {
        boolean ids[] = new boolean[6];
        for (CustomEngine item : list) {
            ids[item.id] = true;
        }

        for (int i = 0; i < (BuildConfig.hideOtherEngine ? 1 : 6); i++) {
            if (!ids[i]) {
                CustomEngineParcelable parcelable = new CustomEngineParcelable();
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

                addEngineToDb(context, parcelable, i);
                //addEngineToList(parcelable, list);
            }
        }
    }

    public static void addEngineToDb(Context context, CustomEngineParcelable parcelable, int id) {
        SQLiteDatabase db = DatabaseHelper.instance(context).getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(CustomEngineTable.COLUMN_ID, id);
        values.put(CustomEngineTable.COLUMN_ENABLED, 1);
        values.put(CustomEngineTable.COLUMN_DATA, ParcelableUtils.marshall(parcelable));

        db.insert(CustomEngineTable.TABLE_NAME, null, values);
    }

    public static void addEngineToList(CustomEngine data) {
        addEngineToList(data, sList);
    }

    public static void addEngineToList(CustomEngine data, List<CustomEngine> list) {
        list.add(data);
    }
}
