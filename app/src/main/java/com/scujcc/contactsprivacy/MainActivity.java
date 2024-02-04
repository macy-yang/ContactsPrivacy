package com.scujcc.contactsprivacy;

import android.Manifest;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.database.Cursor;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private ContactManager contactManager;
    private static final int CONTACTS_PERMISSION_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        contactManager = new ContactManager(this);
        contactManager.open();

        // 请求通讯录权限
        requestContactsPermission();

        // 新增联系人按钮点击事件
        Button addContactButton = findViewById(R.id.addContactButton);
        addContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddContactDialog();
            }
        });

        // 删除联系人按钮点击事件
        Button deleteContactButton = findViewById(R.id.deleteContactButton);
        deleteContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteContactDialog();
            }
        });

        // 修改联系人按钮点击事件
        Button updateContactButton = findViewById(R.id.updateContactButton);
        updateContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUpdateContactDialog();
            }
        });

        // 查询联系人按钮点击事件
        Button queryContactsButton = findViewById(R.id.queryContactsButton);
        queryContactsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 查询联系人并更新列表
                populateContactList();
            }
        });

        // 初始时加载联系人列表
        populateContactList();
    }


    private void requestContactsPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS},
                        CONTACTS_PERMISSION_REQUEST);
            } else {
                // 已经有权限，可以进行通讯录操作
                // TODO: 在此处调用导入通讯录的方法
                importContacts();
            }
        } else {
            // 在旧版本的 Android 中，权限在安装应用时确定，无需额外请求
            // TODO: 在此处调用导入通讯录的方法
        }
    }

    private void importContacts() {
        // TODO: 在此处实现导入通讯录的逻辑
        // 你可以使用 ContentResolver 查询手机通讯录，并将联系人信息导入你的应用数据库

        // 示例代码（仅供参考，具体实现可能需要更多逻辑和异常处理）：
        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(
                ContactsContract.Contacts.CONTENT_URI,
                null,
                null,
                null,
                null);

        if (cursor != null && cursor.moveToFirst()) {
            int idColumnIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID);
            int nameColumnIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);

            do {
                String contactId = cursor.getString(idColumnIndex);
                String displayName = cursor.getString(nameColumnIndex);

                // 获取联系人的手机号码
                Cursor phoneCursor = contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        new String[]{contactId},
                        null);

                if (phoneCursor != null && phoneCursor.moveToFirst()) {
                    int phoneColumnIndex = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

                    do {
                        String phoneNumber = phoneCursor.getString(phoneColumnIndex);

                        // 在此处将联系人信息插入你的应用数据库
                        long result = contactManager.addContact(displayName, phoneNumber);
                        if (result != -1) {
                            // 成功插入联系人数据
                        } else {
                            // 插入联系人数据失败
                        }
                    } while (phoneCursor.moveToNext());

                    phoneCursor.close();
                }
            } while (cursor.moveToNext());

            cursor.close();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CONTACTS_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 用户授予了通讯录权限，可以进行通讯录操作
                // TODO: 在此处调用导入通讯录的方法
                importContacts();
            } else {
                // 用户拒绝了通讯录权限，可以根据需要进行相应处理
                Toast.makeText(this, "您已拒绝通讯录权限，某些功能可能受到限制", Toast.LENGTH_SHORT).show();

                // 例如，显示一个按钮或者文本提示，点击后引导用户去设置中手动开启权限
                showPermissionDeniedUI();
            }
        }
    }

    private void showPermissionDeniedUI() {
        // 清空联系人列表或者显示一些提示信息，因为无法访问通讯录
        clearContactList();

        // 显示一个按钮，引导用户去应用设置中开启权限
        Button openSettingsButton = findViewById(R.id.openSettingsButton);
        openSettingsButton.setVisibility(View.VISIBLE);
        openSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAppSettings();
            }
        });
    }

    private void openAppSettings() {
        // 打开应用设置界面
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    // 清空联系人列表或显示提示信息的方法，根据具体需求自行实现
    private void clearContactList() {
        // 在这里可以清空联系人列表或显示一些提示信息
        // 根据实际需求进行操作
    }

    private void populateContactList() {
        Cursor cursor = contactManager.getAllContacts();

        String[] fromColumns = {DBHelper.COLUMN_NAME, DBHelper.COLUMN_PHONE};
        int[] toViews = {R.id.contactNameTextView, R.id.contactPhoneTextView};

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.contact_item, cursor, fromColumns, toViews, 0);

        ListView listView = findViewById(R.id.contactsListView);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Handle item click, e.g., launch dialer with selected contact
                Cursor selectedItemCursor = (Cursor) parent.getItemAtPosition(position);
                String phoneNumber = selectedItemCursor.getString(selectedItemCursor.getColumnIndexOrThrow(DBHelper.COLUMN_PHONE));

                // TODO: Implement dialer launching code
                launchDialer(phoneNumber);
            }
        });
    }

    private void launchDialer(String phoneNumber) {
        // TODO: Implement dialer launching code
        // Use Intent to launch the system dialer with the given phone number
        Intent dialIntent = new Intent(Intent.ACTION_DIAL);
        dialIntent.setData(android.net.Uri.parse("tel:" + phoneNumber));
        startActivity(dialIntent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        contactManager.close();
    }
    // 弹出对话框以输入联系人姓名和电话号码
    private void showAddContactDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("新增联系人");

        View view = getLayoutInflater().inflate(R.layout.dialog_add_contact, null);
        final EditText nameEditText = view.findViewById(R.id.nameEditText);
        final EditText phoneEditText = view.findViewById(R.id.phoneEditText);

        builder.setView(view);

        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = nameEditText.getText().toString().trim();
                String phone = phoneEditText.getText().toString().trim();

                if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(phone)) {
                    long result = contactManager.addContact(name, phone);
                    if (result != -1) {
                        Toast.makeText(MainActivity.this, "联系人添加成功", Toast.LENGTH_SHORT).show();
                        populateContactList();
                    } else {
                        Toast.makeText(MainActivity.this, "联系人添加失败", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "姓名和电话号码不能为空", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("取消", null);

        builder.show();
    }

    // 弹出对话框以输入联系人ID来删除联系人
    private void showDeleteContactDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("删除联系人");

        View view = getLayoutInflater().inflate(R.layout.dialog_delete_contact, null);
        final EditText contactIdEditText = view.findViewById(R.id.contactIdEditText);

        builder.setView(view);

        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String contactIdStr = contactIdEditText.getText().toString().trim();
                if (!TextUtils.isEmpty(contactIdStr)) {
                    long contactId = Long.parseLong(contactIdStr);
                    contactManager.deleteContact(contactId);
                    Toast.makeText(MainActivity.this, "联系人删除成功", Toast.LENGTH_SHORT).show();
                    populateContactList();
                } else {
                    Toast.makeText(MainActivity.this, "联系人ID不能为空", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("取消", null);

        builder.show();
    }

    // 弹出对话框以输入联系人ID和新的姓名和电话号码来更新联系人
    private void showUpdateContactDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("修改联系人");

        View view = getLayoutInflater().inflate(R.layout.dialog_update_contact, null);
        final EditText contactIdEditText = view.findViewById(R.id.contactIdEditText);
        final EditText newNameEditText = view.findViewById(R.id.newNameEditText);
        final EditText newPhoneEditText = view.findViewById(R.id.newPhoneEditText);

        builder.setView(view);

        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String contactIdStr = contactIdEditText.getText().toString().trim();
                String newName = newNameEditText.getText().toString().trim();
                String newPhone = newPhoneEditText.getText().toString().trim();

                if (!TextUtils.isEmpty(contactIdStr) && !TextUtils.isEmpty(newName) && !TextUtils.isEmpty(newPhone)) {
                    long contactId = Long.parseLong(contactIdStr);
                    contactManager.updateContact(contactId, newName, newPhone);
                    Toast.makeText(MainActivity.this, "联系人修改成功", Toast.LENGTH_SHORT).show();
                    populateContactList();
                } else {
                    Toast.makeText(MainActivity.this, "联系人ID、姓名和电话号码不能为空", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("取消", null);

        builder.show();
    }
}
