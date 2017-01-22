package rikka.searchbyimage.staticdata;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by Rikka on 2016/1/24.
 */
public class SearchEngineParcelable implements Parcelable {
    public SearchEngine data = new SearchEngine();

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(data.getName());
        dest.writeString(data.getUploadUrl());
        dest.writeString(data.getPostFileKey());
        dest.writeInt(data.getResultOpenAction());
        dest.writeList(data.post_text_key);
        dest.writeList(data.post_text_value);
        dest.writeList(data.post_text_type);
    }

    public static final Parcelable.Creator<SearchEngineParcelable> CREATOR = new Parcelable.Creator<SearchEngineParcelable>() {
        @Override
        public SearchEngineParcelable createFromParcel(Parcel source) {
            SearchEngineParcelable r = new SearchEngineParcelable();
            r.data.setName(source.readString());
            r.data.setUploadUrl(source.readString());
            r.data.setPostFileKey(source.readString());
            r.data.setResultOpenAction(source.readInt());
            r.data.post_text_key = new ArrayList<>();
            r.data.post_text_value = new ArrayList<>();
            r.data.post_text_type = new ArrayList<>();
            source.readList(r.data.post_text_key, null);
            source.readList(r.data.post_text_value, null);
            source.readList(r.data.post_text_type, null);
            return r;
        }

        @Override
        public SearchEngineParcelable[] newArray(int size) {
            return new SearchEngineParcelable[size];
        }
    };
}
