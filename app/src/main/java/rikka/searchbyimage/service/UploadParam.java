package rikka.searchbyimage.service;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.util.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rikka on 2017/1/21.
 */

public class UploadParam implements Parcelable {

    private final int mEngineId;
    private final int mType;
    private final String mFileUri;
    private final String mFilename;
    private final String mUrl;
    private final String mPostFileKey;
    private final List<Pair<String, String>> mHeaders;
    private final List<Pair<String, String>> mBodies;
    private final int mResultOpenAction;

    public int getEngineId() {
        return mEngineId;
    }

    public int getType() {
        return mType;
    }

    public String getFileUri() {
        return mFileUri;
    }

    public String getFilename() {
        return mFilename;
    }

    public String getUrl() {
        return mUrl;
    }

    public String getPostFileKey() {
        return mPostFileKey;
    }

    public List<Pair<String, String>> getHeaders() {
        return mHeaders;
    }

    public List<Pair<String, String>> getBodies() {
        return mBodies;
    }

    public int getResultOpenAction() {
        return mResultOpenAction;
    }

    public UploadParam(int engineId, int type, String fileUri, String fileName, String url, String postFileKey, List<Pair<String, String>> headers, List<Pair<String, String>> bodies, int resultOpenAction) {
        mEngineId = engineId;
        mType = type;
        mFileUri = fileUri;
        mFilename = fileName;
        mUrl = url;
        mPostFileKey = postFileKey;
        mHeaders = headers;
        mBodies = bodies;
        mResultOpenAction = resultOpenAction;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mEngineId);
        dest.writeInt(mType);
        dest.writeString(mFileUri);
        dest.writeString(mFilename);
        dest.writeString(mUrl);
        dest.writeString(mPostFileKey);
        dest.writeInt(mHeaders.size());
        for (Pair<String, String> pair : mHeaders) {
            dest.writeString(pair.first);
            dest.writeString(pair.second);
        }
        dest.writeInt(mBodies.size());
        for (Pair<String, String> pair : mBodies) {
            dest.writeString(pair.first);
            dest.writeString(pair.second);
        }
        dest.writeInt(mResultOpenAction);
    }

    public UploadParam(Parcel in) {
        mEngineId = in.readInt();
        mType = in.readInt();
        mFileUri = in.readString();
        mFilename = in.readString();
        mUrl = in.readString();
        mPostFileKey = in.readString();

        int N;
        N = in.readInt();
        mHeaders = new ArrayList<>(N);
        for (int i=0; i<N; i++) {
            mHeaders.add(new Pair<>(in.readString(), in.readString()));
        }

        N = in.readInt();
        mBodies = new ArrayList<>(N);
        for (int i=0; i<N; i++) {
            mBodies.add(new Pair<>(in.readString(), in.readString()));
        }
        mResultOpenAction = in.readInt();
    }


    public static final Creator<UploadParam> CREATOR = new Creator<UploadParam>() {
        @Override
        public UploadParam createFromParcel(Parcel in) {
            return new UploadParam(in);
        }

        @Override
        public UploadParam[] newArray(int size) {
            return new UploadParam[size];
        }
    };
}
