package shine.com.doorscreen.viewmodel;

import android.databinding.BindingAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

/**
 * author:
 * 时间:2017/7/13
 * qq:1220289215
 * 类描述：处理xml中自定义的标签
 */

public class BindAdapter {

    @BindingAdapter("showImage")
    public static void showImage(ImageView view, String image) {
        Glide.with(view.getContext()).load(image).into(view);
    }
}
