package rikka.searchbyimage.staticdata;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by Rikka on 2016/1/24.
 */
public class CustomEngineParcelable implements Parcelable {
    public CustomEngine data = new CustomEngine();

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(data.getName());
        dest.writeString(data.getUpload_url());
        dest.writeString(data.getPost_file_key());
        dest.writeInt(data.getResult_open_action());
        dest.writeList(data.post_text_key);
        dest.writeList(data.post_text_value);
        dest.writeList(data.post_text_type);
    }

    public static final Parcelable.Creator<CustomEngineParcelable> CREATOR = new Parcelable.Creator<CustomEngineParcelable>() {
        @Override
        public CustomEngineParcelable createFromParcel(Parcel source) {
            CustomEngineParcelable r = new CustomEngineParcelable();
            r.data.setName(source.readString());
            r.data.setUpload_url(source.readString());
            r.data.setPost_file_key(source.readString());
            r.data.setResult_open_action(source.readInt());
            r.data.post_text_key = new ArrayList<>();
            r.data.post_text_value = new ArrayList<>();
            r.data.post_text_type = new ArrayList<>();
            source.readList(r.data.post_text_key, null);
            source.readList(r.data.post_text_value, null);
            source.readList(r.data.post_text_type, null);
            return r;
        }

        @Override
        public CustomEngineParcelable[] newArray(int size) {
            return new CustomEngineParcelable[size];
        }
    };
}
