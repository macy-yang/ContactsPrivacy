package com.scujcc.contactsprivacy;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
//联系人管理类
public class ContactManager {
    private SQLiteDatabase database;
    private DBHelper dbHelper;

    public ContactManager(Context context) {
        dbHelper = new DBHelper(context);
    }

    public void open() {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public long addContact(String name, String phone) {
        ContentValues values = new ContentValues();
        values.put(DBHelper.COLUMN_NAME, name);
        values.put(DBHelper.COLUMN_PHONE, phone);
        return database.insert(DBHelper.TABLE_CONTACTS, null, values);
    }

    public void insertTestData() {
        addContact("John Doe", "1234567890");
        addContact("Jane Smith", "9876543210");
        // 添加更多测试数据...
    }

    public void deleteContact(long contactId) {
        database.delete(DBHelper.TABLE_CONTACTS, DBHelper.COLUMN_ID + " = ?", new String[]{String.valueOf(contactId)});
    }

    public Cursor getAllContacts() {
        return database.query(DBHelper.TABLE_CONTACTS, null, null, null, null, null, null);
    }
    // 更新联系人
    public void updateContact(long contactId, String newName, String newPhone) {
        ContentValues values = new ContentValues();
        values.put(DBHelper.COLUMN_NAME, newName);
        values.put(DBHelper.COLUMN_PHONE, newPhone);

        database.update(DBHelper.TABLE_CONTACTS, values, DBHelper.COLUMN_ID + " = " + contactId, null);
    }
}

