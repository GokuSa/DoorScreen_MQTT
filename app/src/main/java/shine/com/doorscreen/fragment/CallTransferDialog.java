package shine.com.doorscreen.fragment;

import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Locale;

import shine.com.doorscreen.R;
import shine.com.doorscreen.databinding.ViewCallTransfer2Binding;
import shine.com.doorscreen.mqtt.MQTTClient;


/**
 * 呼叫转移提示对话框
 * 点击返回不退出，触摸其他区域不退出
 */
public class CallTransferDialog extends DialogFragment {
    private static final String TAG = "WaitingDialogFragment";
    private String mTip = "";
    private ViewCallTransfer2Binding mBinding;

    public static CallTransferDialog newInstance(String tip) {
        CallTransferDialog fragment = new CallTransferDialog();
        Bundle args = new Bundle();
        args.putString("tip", tip);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE,/* android.R.style.Theme_Holo_Light_Dialog*/R.style.dialog_waiting);
        mTip = getArguments().getString("tip");

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.view_call_transfer2, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        点击外部区域不消失
        getDialog().setCanceledOnTouchOutside(false);
//        点击返回不消失
        getDialog().setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                return keyCode == KeyEvent.KEYCODE_BACK;
            }
        });
        if (!TextUtils.isEmpty(mTip)) {
            mBinding.tvContent.setText(String.format(Locale.CHINA,"%s  服务",mTip));
        }
        mBinding.btnRemove.setOnClickListener(v -> {
            MQTTClient.INSTANCE().handleCancelCallTransfer();
            dismiss();

           /* FragmentActivity activity = getActivity();
            if (activity instanceof MainActivity) {
                MainActivity mainActivity = (MainActivity) activity;
                mainActivity.cancelCallTransfer();
                dismiss();
            }*/
        });
    }


}