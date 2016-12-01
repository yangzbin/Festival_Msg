package yzb.com.festival_msg;

import android.annotation.TargetApi;
import android.app.ActivityOptions;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import yzb.com.festival_msg.bean.Contact;
import yzb.com.festival_msg.bean.Festival;
import yzb.com.festival_msg.bean.FestivalLab;
import yzb.com.festival_msg.bean.Msg;
import yzb.com.festival_msg.bean.SendMsg;
import yzb.com.festival_msg.business.SmsBiz;
import yzb.com.festival_msg.utils.ToastUtil;
import yzb.com.festival_msg.view.FlowLayout;

public class SendMsgActivity extends AppCompatActivity {
    private int mFestivalId;
    private int mMsgId;
    public static final String ID_FESTIVAL = "fes_id";
    public static final String ID_MSG = "msg_id";
    public static final int CODE_REQUEST = 1;

    private Festival festival;
    private Msg msg;

    private EditText mEdMsg;
    private Button mAddCon;
    private FlowLayout mFlContacts;
    private FloatingActionButton mFabSend;
    private View mLoadView;
    //分别用来存选中的联系人以及电话号码
    private HashSet<String> mContactNames = new HashSet<>();
    private HashSet<String> mContactNums = new HashSet<>();

    private LayoutInflater mInflater;

    public static final String ACTION_SEND_MSG = "ACTION_SEND_MSG";
    public static final String ACTION_DELIVER_MSG = "ACTION_DELIVER_MSG";

    private PendingIntent mSendPi;
    private PendingIntent mDeliverPi;
    private BroadcastReceiver mSendBroadcastReceiver;
    private BroadcastReceiver mDeliverBroadcastReceiver;

    private SmsBiz smsBiz;
    private int mSendMsgCount;
    private int mTotalCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_msg);
        mInflater = LayoutInflater.from(this);
        smsBiz = new SmsBiz(this);
        initDatas();
        initViews();
        initEvents();
        initRecivers();
    }

    private void initRecivers() {
        Intent sendIntent = new Intent(ACTION_SEND_MSG);
        mSendPi = PendingIntent.getBroadcast(this,0,sendIntent,0);
        Intent deliverIntent = new Intent(ACTION_DELIVER_MSG);
        mDeliverPi = PendingIntent.getBroadcast(this,0,deliverIntent,0);

        registerReceiver(mSendBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mSendMsgCount++;
                if(getResultCode() == RESULT_OK){
//                    Log.d("YZB","短信发送成功");
//                    Toast.makeText(SendMsgActivity.this,(mSendMsgCount+"/"+mTotalCount)+"短信发送成功",Toast.LENGTH_LONG).show();
                    ToastUtil.showShortToast(SendMsgActivity.this,(mSendMsgCount+"/"+mTotalCount)+"短信发送成功");
                }else {
//                    Log.d("YZB","短信发送失败");
//                    Toast.makeText(SendMsgActivity.this,"短信发送失败",Toast.LENGTH_SHORT).show();
                    ToastUtil.showShortToast(SendMsgActivity.this,"短信发送失败");
                }
                if(mSendMsgCount == mTotalCount){
                    finish();
                }
            }
        },new IntentFilter(ACTION_SEND_MSG));

        registerReceiver(mDeliverBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
//                Log.d("YZB","联系人成功接收到短信");
//                Toast.makeText(SendMsgActivity.this,"联系人成功接收到短信",Toast.LENGTH_SHORT).show();
                ToastUtil.showShortToast(SendMsgActivity.this,"联系人成功接收到短信");
            }
        },new IntentFilter(ACTION_DELIVER_MSG));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mSendBroadcastReceiver);
        unregisterReceiver(mDeliverBroadcastReceiver);
    }

    private void initEvents() {
        //添加联系人
        mAddCon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
//                startActivityForResult(intent,CODE_REQUEST);
                Intent intent = new Intent(SendMsgActivity.this,ChooseContactActivity.class);
                startActivityForResult(intent,CODE_REQUEST);
            }
        });
        //发送短信
        mFabSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mContactNums.size() == 0){
//                    Toast.makeText(SendMsgActivity.this,"请先选择联系人",Toast.LENGTH_SHORT).show();
                    ToastUtil.showShortToast(SendMsgActivity.this,"请先选择联系人");
                    return;
                }
                String msgContent = mEdMsg.getText().toString();
                if(TextUtils.isEmpty(msgContent)){
//                    Toast.makeText(SendMsgActivity.this,"短信内容不能为空",Toast.LENGTH_SHORT).show();
                    ToastUtil.showShortToast(SendMsgActivity.this,"短信内容不能为空");
                    return;
                }
                mLoadView.setVisibility(View.VISIBLE);
                mTotalCount = smsBiz.sendMsg(mContactNums,buidSenMsg(msgContent),mSendPi,mDeliverPi);
                mSendMsgCount = 0;
            }


        });
        //删除所选联系人

    }

    private SendMsg buidSenMsg(String msg){
        SendMsg sendMsg = new SendMsg();
        sendMsg.setMsg(msg);
        sendMsg.setFesName(festival.getName());
//        String names = "";
        StringBuilder names = new StringBuilder();
        for(String name:mContactNames){
//            names += name + ",";//多人以逗号隔开
            names.append(name).append(",");
        }
//        String numbers = "";
        StringBuilder numbers = new StringBuilder();
        for(String number:mContactNums){
//            numbers += number + ",";
            numbers.append(number).append(",");//用拼接效率高
        }
        sendMsg.setName(names.toString().substring(0,names.length()-1));//去掉最后一个逗号
        sendMsg.setNumber(numbers.toString().substring(0,numbers.length()-1));
        return sendMsg;
    }

    private void initViews() {
        mEdMsg = (EditText) findViewById(R.id.id_msg_edit);
        mAddCon = (Button) findViewById(R.id.id_btn_addcon);
        mFlContacts = (FlowLayout) findViewById(R.id.id_msg_flow);
        mFabSend = (FloatingActionButton) findViewById(R.id.id_fab_send);
        mLoadView = findViewById(R.id.id_layout_loading);
        mLoadView.setVisibility(View.GONE);
        if(mMsgId != -1){
            msg = FestivalLab.getmInstance(this).getmMsgsById(mMsgId);
            mEdMsg.setText(msg.getMsgContent());
        }
    }

    private void initDatas() {
        mFestivalId = getIntent().getIntExtra(ID_FESTIVAL,-1);
        mMsgId = getIntent().getIntExtra(ID_MSG,-1);
        festival = FestivalLab.getmInstance(this).getFestivalById(mFestivalId);
        setTitle(festival.getName());
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void toActivity(Context context, int fesId, int msgId){
        Intent intent = new Intent(context,SendMsgActivity.class);
        intent.putExtra(ID_FESTIVAL,fesId);
        intent.putExtra(ID_MSG,msgId);
        context.startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CODE_REQUEST){
            if(resultCode == RESULT_OK){
//                Uri contactUri = data.getData();
//                Cursor cursor = getContentResolver().query(contactUri,null,null,null,null);
//                cursor.moveToFirst();
//                String contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
//                String contactNum = getContactNum(cursor);
//                if(!TextUtils.isEmpty(contactNum)){
//                    mContactNames.add(contactName);
//                    mContactNums.add(contactNum);
//                    addTag(contactName);
//                }
                List<Contact> mList = new ArrayList<>();
                mList = (List<Contact>) data.getSerializableExtra("GET_CONTACT");
                for(Contact contact:mList){
                    String contactName = contact.getContactName();
                    String contactNum = contact.getContactNumber();
                    if(!TextUtils.isEmpty(contactNum)){
                        mContactNames.add(contactName);
                        mContactNums.add(contactNum);
                        addTag(contactName,contact);
                    }
                }

            }
        }
    }

    private void addTag(String contactName, final Contact contact) {

        TextView mtextView = (TextView) mInflater.inflate(R.layout.contact_tag, mFlContacts, false);
        mtextView.setText(contactName);
        mFlContacts.addView(mtextView);
        mtextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFlContacts.removeView(v);
                mContactNames.remove(contact.getContactName());
                mContactNums.remove(contact.getContactNumber());
            }
        });
    }

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
        cursor.close();
        return phoneNum;
    }
}
