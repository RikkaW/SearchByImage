package rikka.searchbyimage.service;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Rikka on 2017/1/21.
 */

public class UploadResult implements Parcelable {

    public final static int NO_ERROR = 0;
    public final static int CANCELED = -1;
    public final static int ERROR_FILE_NOT_FOUND = 1;
    public final static int ERROR_UNKNOWN_HOST = 2;
    public final static int ERROR_TIMEOUT = 3;
    public final static int ERROR_IO = 4;
    public final static int ERROR_UNKNOWN = 5;

    private final int mEngineId;
    private final String mFileUri;
    private final String mFilename;
    private final String mUrl;
    private final String mHtmlUri;
    private final int mResultOpenAction;
    private final int mErrorCode;
    private final String mErrorMessage;

    public int getEngineId() {
        return mEngineId;
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

    public String getHtmlUri() {
        return mHtmlUri;
    }

    public int getResultOpenAction() {
        return mResultOpenAction;
    }

    public int getErrorCode() {
        return mErrorCode;
    }

    public String getErrorMessage() {
        return mErrorMessage;
    }

    public UploadResult(int engineId, String fileUri, String filename, String url, String htmlUri, int resultOpenAction) {
        mEngineId = engineId;
        mFileUri = fileUri;
        mFilename = filename;
        mUrl = url;
        mHtmlUri = htmlUri;
        mResultOpenAction = resultOpenAction;
        mErrorCode = NO_ERROR;
        mErrorMessage = null;
    }

    public UploadResult(int errorCode, String errorMessage, UploadParam param) {
        mErrorCode = errorCode;
        mErrorMessage = errorMessage;
        mEngineId = param == null ? 0 : param.getEngineId();
        mFileUri = param == null ? null : param.getFileUri();
        mFilename = param == null ? null : param.getFilename();
        mUrl = null;
        mHtmlUri = null;
        mResultOpenAction = 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mErrorCode);
        if (mErrorCode != 0) {
            dest.writeString(mErrorMessage);
        }
        dest.writeInt(mEngineId);
        dest.writeString(mFileUri);
        dest.writeString(mFilename);
        dest.writeString(mUrl);
        dest.writeString(mHtmlUri);
        dest.writeInt(mResultOpenAction);
    }

    public UploadResult(Parcel in) {
        mErrorCode = in.readInt();
        mErrorMessage = mErrorCode != 0 ? in.readString() : null;
        mEngineId = in.readInt();
        mFileUri = in.readString();
        mFilename = in.readString();
        mUrl = in.readString();
        mHtmlUri = in.readString();
        mResultOpenAction = in.readInt();
    }


    public static final Parcelable.Creator<UploadResult> CREATOR = new Parcelable.Creator<UploadResult>() {
        @Override
        public UploadResult createFromParcel(Parcel in) {
            return new UploadResult(in);
        }

        @Override
        public UploadResult[] newArray(int size) {
            return new UploadResult[size];
        }
    };
}
