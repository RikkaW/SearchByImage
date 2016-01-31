package rikka.searchbyimage.utils;

import android.databinding.BindingAdapter;
import android.support.v7.widget.SwitchCompat;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

/**
 * Created by Yulan on 2016/1/30.
 */
public class DatabindingHelper {

    @BindingAdapter("bind:imageUrl")
    public static void setimageUrl(ImageView imageView, String imageUrl) {
        Glide.with(imageView.getContext())
                .load(imageUrl)
                .crossFade()
                .into(imageView);
    }

    @BindingAdapter("bind:OnCheckedChangeListener")
    public static void setOnCheckedChangeListener(SwitchCompat switchCompat, SwitchCompat.OnCheckedChangeListener onCheckedChangeListener) {
        switchCompat.setOnCheckedChangeListener(onCheckedChangeListener);
    }

}
