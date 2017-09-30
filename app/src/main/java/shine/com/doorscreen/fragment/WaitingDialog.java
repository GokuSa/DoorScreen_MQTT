package shine.com.doorscreen.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.View;

/**
 * author:
 * 时间:2017/8/21
 * qq:1220289215
 * 类描述：显示信息的等待对话框 简陋版
 */

public class WaitingDialog extends DialogFragment {
    private static final String TAG = "WaitingDialogFragment";
    private static final String ARG_MESSAGE = "message";

    public static WaitingDialog newInstance(String message) {
        WaitingDialog fragment = new WaitingDialog();
        Bundle args = new Bundle();
        args.putString(ARG_MESSAGE, message);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Activity activity = getActivity();
        return new AlertDialog.Builder(activity)
                .setTitle("与服务器断开连接")
                .setMessage("正在连接服务器")
//                .setMessage(getArguments().getString(ARG_MESSAGE))
//                .setView(new ProgressBar(activity))
                .create();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //
        getDialog().setCanceledOnTouchOutside(false);
    }
}
