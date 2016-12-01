package yzb.com.festival_msg.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import yzb.com.festival_msg.R;
import yzb.com.festival_msg.utils.ToastUtil;
import yzb.com.festival_msg.view.WuziqiPanel;

/**
 * Created by Administrator on 2016/7/27.
 */
public class WuziqiFragment extends Fragment {
    private WuziqiPanel mPanel;
    private Button mStart,mPtop,mPtoc;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.wuziqi_fragment_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        initEvents();

    }

    private void initEvents() {
        mStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPanel.startAgin();
            }
        });
        mPtop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPtop.setTextColor(getResources().getColor(R.color.colorMain));
                mPtoc.setTextColor(getResources().getColor(R.color.colorConName));
                mPanel.ptop();
                ToastUtil.showShortToast(getActivity(),"已切换为人人对战模式");
            }
        });
        mPtoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPtoc.setTextColor(getResources().getColor(R.color.colorMain));
                mPtop.setTextColor(getResources().getColor(R.color.colorConName));
                mPanel.ptoc();
                ToastUtil.showShortToast(getActivity(),"已切换为人机对战模式");
            }
        });
    }

    private void initViews(View view) {
        mPanel = (WuziqiPanel) view.findViewById(R.id.id_wuziqi);
        mStart = (Button) view.findViewById(R.id.id_start);
        mPtop = (Button) view.findViewById(R.id.id_ptop);
        mPtoc = (Button) view.findViewById(R.id.id_ptoc);
    }
}
