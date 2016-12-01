package yzb.com.festival_msg.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.TextView;

import yzb.com.festival_msg.ChooseMsgActivity;
import yzb.com.festival_msg.R;
import yzb.com.festival_msg.bean.Festival;
import yzb.com.festival_msg.bean.FestivalLab;

/**
 * Created by Administrator on 2016/7/19.
 */
public class FestivalFragment extends Fragment {
    private GridView mGridView;
    private ArrayAdapter<Festival> mAdapter;
    private LayoutInflater mInflater;
    public static final String ID_FESTIVAL = "fest_id";

    @Override
    public View onCreateView(LayoutInflater inflater,  ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.festival_fragment_layout,container,false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mInflater = LayoutInflater.from(getActivity());
        mGridView = (GridView) view.findViewById(R.id.id_gridview);
        mGridView.setAdapter(mAdapter = new ArrayAdapter<Festival>(getActivity(),-1,FestivalLab.getmInstance(getActivity()).getmFestivals()){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if(convertView == null){
                    convertView = mInflater.inflate(R.layout.fes_gridview_item,parent,false);
                }
                TextView textView = (TextView) convertView.findViewById(R.id.id_textview);
                textView.setText(getItem(position).getName());
                return convertView;
            }
        });

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), ChooseMsgActivity.class);
                intent.putExtra(ID_FESTIVAL,mAdapter.getItem(position).getId());
                startActivity(intent);
            }
        });
    }
}
