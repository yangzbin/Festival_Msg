package yzb.com.festival_msg;

import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import yzb.com.festival_msg.bean.FestivalLab;
import yzb.com.festival_msg.bean.Msg;
import yzb.com.festival_msg.fragment.FestivalFragment;

public class ChooseMsgActivity extends AppCompatActivity {
    private ListView mListView;
    private FloatingActionButton mFloatButton;
    private LayoutInflater mInflater;
    private ArrayAdapter<Msg> mAdapter;
    private int fesId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_msg);
        fesId = getIntent().getIntExtra(FestivalFragment.ID_FESTIVAL,-1);
        setTitle(FestivalLab.getmInstance(this).getFestivalById(fesId).getName());
        mInflater = LayoutInflater.from(this);
        initViews();
        initEvent();
    }

    private void initEvent() {
        mFloatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendMsgActivity.toActivity(ChooseMsgActivity.this,fesId,-1);
            }
        });
    }

    private void initViews() {
        mListView = (ListView) findViewById(R.id.id_list_msgs);
        mFloatButton = (FloatingActionButton) findViewById(R.id.id_fab_tosend);

        mListView.setAdapter(mAdapter = new ArrayAdapter<Msg>(this,-1, FestivalLab.getmInstance(this).getmMsgsByfesId(fesId)){
            ViewHoder mViewHoder = null;
            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                if(convertView == null){
                    mViewHoder = new ViewHoder();
                    convertView = mInflater.inflate(R.layout.msg_listview_item,parent,false);
                    mViewHoder.msg_content = (TextView) convertView.findViewById(R.id.msg_content);
                    mViewHoder.id_btn_tosend = (Button) convertView.findViewById(R.id.id_btn_tosend);
                    convertView.setTag(mViewHoder);
                }else{
                    mViewHoder = (ViewHoder) convertView.getTag();
                }
                mViewHoder.msg_content.setText(getItem(position).getMsgContent());
                mViewHoder.id_btn_tosend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SendMsgActivity.toActivity(ChooseMsgActivity.this,fesId,getItem(position).getMsgId());
                    }
                });
                return convertView;
            }
        });
    }
    public final class ViewHoder{
        public TextView msg_content;
        public Button id_btn_tosend;
    }
}
