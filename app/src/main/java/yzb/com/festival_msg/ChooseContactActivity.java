package yzb.com.festival_msg;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import yzb.com.festival_msg.bean.Contact;
import yzb.com.festival_msg.utils.CharacterParser;
import yzb.com.festival_msg.utils.ChineseToEnglish;
import yzb.com.festival_msg.utils.CommonAdapter;
import yzb.com.festival_msg.utils.PinyinComparator;
import yzb.com.festival_msg.utils.ToastUtil;
import yzb.com.festival_msg.utils.ViewHolder;

public class ChooseContactActivity extends AppCompatActivity {
    private ListView mListView;
    private Button mCancel,mEnsure;
    private Context mContext;

    private List<Contact> mList;
    private List<Contact> mSortList;
    private List<Contact> mSelectList;
    private LayoutInflater mInflater;
    private ArrayAdapter<Contact> mAdapter;
    private ContactAdapter mContactAdapter;
    private MyAdapter adapter;

    private CharacterParser mCharacterParser;
    /**
     * 根据拼音来排列ListView里面的数据类
     */
    private PinyinComparator mPinyinComparator;

    private Map<Integer, Boolean> isCheckMap =  new HashMap<Integer, Boolean>();//保存checkbox的状态

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_contact);
        mContext = this;
        mInflater = LayoutInflater.from(mContext);
        //实例化汉字转拼音类
        mCharacterParser = CharacterParser.getInstance();
        mPinyinComparator = new PinyinComparator();
        initViews();
        initDatas();
        initEvents();
    }

    private void initEvents() {
        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mEnsure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(int i=0;i<mSortList.size();i++){
                    if(mSortList.get(i).isSelectTag()){
                        mSelectList.add(mSortList.get(i));
                    }
                }
                Intent intent = new Intent();
                if(mSelectList!=null && mSelectList.size()>0){
                    Bundle b = new Bundle();
                    b.putSerializable("GET_CONTACT", (Serializable) mSelectList);
                    intent.putExtras(b);
                    setResult(RESULT_OK, intent);
                    finish();
                }else {
                    ToastUtil.showShortToast(ChooseContactActivity.this, "请选择联系人");
                }

            }
        });
    }

    private void initDatas() {
        getPhoneContacts();
        getSortContactList(mList);
        // 根据a-z进行排序源数据
        Collections.sort(mSortList, mPinyinComparator);
//        mContactAdapter = new ContactAdapter(mContext,mSortList);
//        mListView.setAdapter(mContactAdapter);
        adapter = new MyAdapter(mContext,mSortList);
        mListView.setAdapter(adapter);
    }

    /**
     * 将汉字转换成拼音
     * @param list
     * @return
     */
    private List<Contact> getSortContactList(List<Contact> list) {
        mSortList = new ArrayList<>();
        for(int i=0;i<list.size();i++){
            Contact contact = new Contact(list.get(i).getContactName(),list.get(i).getContactNumber());
            //汉字转换成拼音
//            String pinyin = mCharacterParser.getSelling(list.get(i).getContactName());
            String pinyin = ChineseToEnglish.getPingYin(list.get(i).getContactName());
            String sortString = pinyin.substring(0, 1).toUpperCase();
            // 正则表达式，判断首字母是否是英文字母
            if(sortString.matches("[a-zA-Z]")){
                contact.setSortLetters(sortString.toUpperCase());
            }else{
                contact.setSortLetters("#");
            }
            mSortList.add(contact);
        }
        return mSortList;

    }
    /**
     * 基于基类的适配器
     */
    class MyAdapter extends CommonAdapter<Contact>{
        public MyAdapter(Context context, List<Contact> lists) {
            super(context, lists,R.layout.contact_listview_item);
        }

        @Override
        public void conVert(ViewHolder viewHolder, final Contact contact) {
            viewHolder.setTvText(R.id.id_tv_conname,contact.getContactName());
            viewHolder.setTvText(R.id.id_tv_connumber,contact.getContactNumber());
            TextView id_tv_sort = viewHolder.getView(R.id.id_tv_sort);
            final CheckBox id_ck_select =  viewHolder.getView(R.id.id_ck_select);
            id_ck_select.setChecked(contact.isSelectTag());
            int position = viewHolder.getmPosition();
            if(position == 0){
                id_tv_sort.setVisibility(View.VISIBLE);
                id_tv_sort.setText(contact.getSortLetters());
            }else{
                String lastCatalog = mDatas.get(position - 1).getSortLetters();
                if(lastCatalog.equals(contact.getSortLetters())){//同一分组
                    id_tv_sort.setVisibility(View.GONE);
                }else {
                    id_tv_sort.setVisibility(View.VISIBLE);
                    id_tv_sort.setText(contact.getSortLetters());
                }
            }
            id_ck_select.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    contact.setSelectTag(id_ck_select.isChecked());
                }
            });
        }
    }
    /**
     * 适配器
     */
    class ContactAdapter extends BaseAdapter/* implements SectionIndexer*/ {
        private Context context;
        private LayoutInflater inflater;
        private List<Contact> contacts;
        private boolean[] checks; //用于保存checkBox的选择状态
        public ContactAdapter(Context context,List<Contact> list){
            this.context = context;
            inflater = LayoutInflater.from(context);
            this.contacts = list;
            checks = new boolean[getCount()];
        }

        @Override
        public int getCount() {
            return contacts.size();
        }

        @Override
        public Object getItem(int position) {
            return contacts.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHoder viewHoder = null;
            final Contact contact = contacts.get(position);
            if(convertView == null){
                viewHoder = new ViewHoder();
                convertView = mInflater.inflate(R.layout.contact_listview_item,parent,false);
                viewHoder.id_tv_conname = (TextView) convertView.findViewById(R.id.id_tv_conname);
                viewHoder.id_tv_connumber = (TextView) convertView.findViewById(R.id.id_tv_connumber);
                viewHoder.id_ck_select = (CheckBox) convertView.findViewById(R.id.id_ck_select);
                viewHoder.id_tv_sort = (TextView) convertView.findViewById(R.id.id_tv_sort);
                convertView.setTag(viewHoder);
            }else {
                viewHoder = (ViewHoder) convertView.getTag();
            }
           /* //根据position获取分类的首字母的char ascii值
            int section = getSectionForPosition(position);
//            如果当前位置等于该分类首字母的Char的位置 ，则认为是第一次出现
            if(position == getPositionForSection(section)){
                viewHoder.id_tv_sort.setVisibility(View.VISIBLE);
                viewHoder.id_tv_sort.setText(contact.getSortLetters());
            }else{
                viewHoder.id_tv_sort.setVisibility(View.GONE);
            }*/
            if(position == 0){
                viewHoder.id_tv_sort.setVisibility(View.VISIBLE);
                viewHoder.id_tv_sort.setText(contact.getSortLetters());
            }else{
                String lastCatalog = contacts.get(position - 1).getSortLetters();
                if(lastCatalog.equals(contact.getSortLetters())){//同一分组
                    viewHoder.id_tv_sort.setVisibility(View.GONE);
                }else {
                    viewHoder.id_tv_sort.setVisibility(View.VISIBLE);
                    viewHoder.id_tv_sort.setText(contact.getSortLetters());
                }
            }
            viewHoder.id_tv_sort.setText(contact.getSortLetters());
            viewHoder.id_tv_conname.setText(contact.getContactName());
            viewHoder.id_tv_connumber.setText(contact.getContactNumber());
            final int pos  = position; //pos必须声明为final
            viewHoder.id_ck_select.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
                @Override
                public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                    checks[pos] = isChecked;
                    contact.setSelectTag(isChecked);//设置是否选中
                }});
            viewHoder.id_ck_select.setChecked(checks[pos]);

            return convertView;
        }

       /* @Override
        public Object[] getSections() {
            return null;
        }
        *//**
         * 根据分类的首字母的Char ascii值获取其第一次出现该首字母的位置
         *//*
        @Override
        public int getPositionForSection(int sectionIndex) {
            for(int i=0;i<getCount();i++){
                String sortStr = contacts.get(sectionIndex).getSortLetters();
                char firstChar = sortStr.toUpperCase().charAt(0);
                if(firstChar == sectionIndex){
                    return i;
                }
            }
            return -1;
        }
        *//**
         * 根据ListView的当前位置获取分类的首字母的char ascii值
         *//*
        @Override
        public int getSectionForPosition(int position) {
            return contacts.get(position).getSortLetters().charAt(0);
        }*/
    }
    public final class ViewHoder{
        public TextView id_tv_conname,id_tv_connumber,id_tv_sort;
        public CheckBox id_ck_select;
    }

    /**
     * 得到通讯录联系人
     */
    private void getPhoneContacts() {
        mList = new ArrayList<>();
        mSelectList = new ArrayList<>();
        ContentResolver resolver = mContext.getContentResolver();
        // 获取手机联系人
        Cursor phoneCursor = resolver.query(ContactsContract.Contacts.CONTENT_URI,null, null, null, null);
        if(phoneCursor!=null){
            while (phoneCursor.moveToNext()){
                //得到联系人名称
                String contactName = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                String contactNum = getContactNum(phoneCursor);
                if(contactNum!=null && !contactNum.equals("")){
                    mList.add(new Contact(contactName,contactNum));
                }
            }
        }
        phoneCursor.close();
    }

    /**
     * 获取手机号
     * @param cursor
     * @return
     */
    private String getContactNum(Cursor cursor) {
        //通过cursor判断是否有phonenumber
        int numCount = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
        String phoneNum = null;
        if(numCount > 0){
            int contactId = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts._ID));
            Cursor phoneCursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId,null,null);
            phoneCursor.moveToFirst();
            phoneNum = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            phoneCursor.close();
        }
        return phoneNum;
    }
    private void initViews() {
        setTitle("选择联系人");
        mListView = (ListView) findViewById(R.id.id_list_contacts);
        mEnsure = (Button) findViewById(R.id.id_btn_ensure);
        mCancel = (Button) findViewById(R.id.id_btn_cancle);
    }


}
