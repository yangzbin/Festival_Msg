package yzb.com.festival_msg.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import yzb.com.festival_msg.R;
import yzb.com.festival_msg.bean.SendMsg;
import yzb.com.festival_msg.business.SmsBiz;
import yzb.com.festival_msg.view.FlowLayout;

/**
 * Created by Administrator on 2016/7/21.
 */
public class SendMsgFragment extends Fragment {
    private LayoutInflater mInflater;
    private ListView mListView;
    private ArrayAdapter<SendMsg> mAdapter;
    private SmsBiz smsBiz;
    private List<SendMsg> msgList;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        smsBiz = new SmsBiz(getContext());
        return inflater.inflate(R.layout.sendmsg_fragment_layout,container,false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mInflater = LayoutInflater.from(getActivity());
        mListView = (ListView) view.findViewById(R.id.id_lv_sendmsg);

    }
    private void addTag(String name,FlowLayout fl){
        TextView tv = (TextView) mInflater.inflate(R.layout.contact_msg_tag,fl,false);
        tv.setText(name);
        fl.addView(tv);
    }
    public final class ViewHoder{
        public TextView tv_sendmsg;
        public FlowLayout fl_sendname;
        public TextView tv_sendfesname;
        private TextView tv_senddate;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {//可见不可见都会执行
        super.setUserVisibleHint(isVisibleToUser);
        Log.e("yzb","=="+isVisibleToUser);
        if(isVisibleToUser){
            msgList = new ArrayList<>();
            msgList = smsBiz.getSendMsgAll();
            if(msgList!=null&&msgList.size()>0){
                mListView.setAdapter(mAdapter = new ArrayAdapter<SendMsg>(getActivity(),-1,msgList){
                    ViewHoder viewHoder = null;
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        if(convertView == null){
                            viewHoder = new ViewHoder();
                            convertView = mInflater.inflate(R.layout.msg_send_item,parent,false);
                            viewHoder.tv_sendmsg = (TextView) convertView.findViewById(R.id.id_tv_sendmsg);
                            viewHoder.fl_sendname = (FlowLayout) convertView.findViewById(R.id.id_fl_sendname);
                            viewHoder.tv_sendfesname = (TextView) convertView.findViewById(R.id.id_tv_sendfesname);
                            viewHoder.tv_senddate = (TextView) convertView.findViewById(R.id.id_tv_senddate);
                            convertView.setTag(viewHoder);
                        }else {
                            viewHoder = (ViewHoder) convertView.getTag();
                        }
                        SendMsg msg = getItem(position);
                        viewHoder.tv_sendmsg.setText(msg.getMsg());
                        viewHoder.tv_sendfesname.setText(msg.getFesName());
                        viewHoder.tv_senddate.setText(msg.getDateStr());
                        viewHoder.fl_sendname.removeAllViews();//因为是复用，添加前进行移除
                        if(!TextUtils.isEmpty(msg.getName())){
                            for (String name : msg.getName().split(",")){
                                addTag(name,viewHoder.fl_sendname);
                            }
                        }
                        return convertView;
                    }
                });
            }
        }
    }
}
