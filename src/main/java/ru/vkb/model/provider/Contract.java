package ru.vkb.model.provider;

import android.content.ContentValues;
import android.content.UriMatcher;
import android.net.Uri;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.Collection;


/**
 * Created by Zamuraev_av on 26.02.14.
 */
public class Contract {

    //region Description
    public static final UriMatcher sUriMatcher;

    public static final String PATH_DISPOSALS = "disposals";
    public static final Uri URI_DISPOSALS = Uri.withAppendedPath(Base.AUTHORITY_URI, PATH_DISPOSALS);
    public static final int ID_DISPOSALS = 1;

    public static final String PATH_DISPOSALS_COMMENT = "disposalCommentDescription";
    public static final Uri URI_DISPOSALS_COMMENT = Uri.withAppendedPath(Base.AUTHORITY_URI, PATH_DISPOSALS_COMMENT);
    public static final int ID_DISPOSALS_COMMENT = 2;

    public static final String PATH_DISPOSALS_GROUP = "disposal_group";
    public static final int ID_DISPOSALS_GROUP = 3;
    public static final Uri URI_DISPOSALS_GROUP = Uri.withAppendedPath(Base.AUTHORITY_URI, PATH_DISPOSALS_GROUP);

    public static final String PATH_DISPOSALS_CHILD = "disposal_child";
    public static final int ID_DISPOSALS_CHILD = 4;
    public static final Uri URI_DISPOSALS_CHILD = Uri.withAppendedPath(Base.AUTHORITY_URI, PATH_DISPOSALS_CHILD);

    public static final String PATH_STAFF = "staff";
    public static final int ID_STAFF = 5;
    public static final Uri URI_STAFF = Uri.withAppendedPath(Base.AUTHORITY_URI, PATH_STAFF);

    public static final String PATH_DISPOSALS_REGISTER = "disposal_register";
    public static final int ID_DISPOSALS_REGISTER = 6;
    public static final Uri URI_DISPOSALS_REGISTER = Uri.withAppendedPath(Base.AUTHORITY_URI, PATH_DISPOSALS_REGISTER);

    public static final String PATH_DISTINCT_STAFF = "distinct_staff";
    public static final int ID_DISTINCT_STAFF = 7;
    public static final Uri URI_DISTINCT_STAFF = Uri.withAppendedPath(Base.AUTHORITY_URI, PATH_DISTINCT_STAFF);
    //endregion

    static {
        sUriMatcher = new UriMatcher(0);
        sUriMatcher.addURI(Base.AUTHORITY, PATH_DISPOSALS, ID_DISPOSALS);
        sUriMatcher.addURI(Base.AUTHORITY, PATH_DISPOSALS_COMMENT, ID_DISPOSALS_COMMENT);
        sUriMatcher.addURI(Base.AUTHORITY, PATH_DISPOSALS_GROUP, ID_DISPOSALS_GROUP);
        sUriMatcher.addURI(Base.AUTHORITY, PATH_DISPOSALS_CHILD, ID_DISPOSALS_CHILD);
        sUriMatcher.addURI(Base.AUTHORITY, PATH_STAFF, ID_STAFF);
        sUriMatcher.addURI(Base.AUTHORITY, PATH_DISTINCT_STAFF, ID_DISTINCT_STAFF);
    }

    public static class ContractFactory{
        public static final disposals Disposals = new disposals();
        public static final disposal_comment DisposalsComment = new disposal_comment();
        public static final staff Staff = new staff();

        public static Base getContractByUri(Uri uri) {
            switch (sUriMatcher.match(uri)) {
                case ID_DISPOSALS:
                    return Disposals;
                case ID_DISPOSALS_COMMENT:
                    return DisposalsComment;
                case ID_STAFF:
                    return Staff;
                default: return null;
            }
        }

        public static Uri getUriByTable(String table_name){
            if (table_name.equals(PATH_DISPOSALS))
                return URI_DISPOSALS;
            else if (table_name.equals(PATH_DISPOSALS_COMMENT))
                return URI_DISPOSALS_COMMENT;
            else if (table_name.equals(PATH_STAFF))
                return URI_STAFF;
            else
                return null;
        }
    }

    public interface PATH{
        public static final String[] ALL = {"*"};
        public String getTableName();
        public Base.ColumnList getColumns();
        public String[] getColumnsName();
        public Uri getURI();
        public Integer getID();
    }

    public static class Base implements PATH{
        public static final String AUTHORITY = "ru.vkb";
        public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);

        public String TableName;
        public String[] PROJECTION;
        public String CONTENT_PATH;
        public Uri CONTENT_URI;

        public static class Column{
            public String Name, Caption, Type, Meta;
            public Boolean filterEnabled;
            public Integer Index;
            public Object[] defaultValues;

            public Column(String name, String caption, String type, String meta, Boolean filterEnabled, Integer index) {
                Name = name;
                Caption = caption;
                Type = type;
                Meta = meta;
                this.filterEnabled = filterEnabled;
                Index = index;
            }
        }

        public static class ColumnList extends ArrayList<Column>{
            public ColumnList(int capacity) {
                super(capacity);
            }

            public ColumnList() {
            }

            public ColumnList(Collection<? extends Column> collection) {
                super(collection);
            }

            public Integer getIndexByName(String Name){
                for (int i = 0; i < size(); i++) {
                    if (get(i).Name.equalsIgnoreCase(Name)){
                        return i;
                    }
                }
                return -1;
            }

            public String[] columnsName() {
                String[] result = new String[size()];
                for (int i = 0; i < size(); i++) {
                    result[i] = get(i).Name;
                }
                return result;
            }
        }

        protected ColumnList Columns;

        public String getTableName() {
            return null;
        }

        @Override
        public ColumnList getColumns() {
            return Columns;
        }

        @Override
        public String[] getColumnsName() {
            return getColumns().columnsName();
        }

        @Override
        public Uri getURI() {
            return null;
        }

        @Override
        public Integer getID() {
            return null;
        }
/*
        public String _CONTENT_TYPE() {
            return "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + _CONTENT_PATH();
        }*/

        public String getCreateTableSql() {
            StringBuilder sb = new StringBuilder("create table " + getTableName() + " (");
            for (int i = 0; i < getColumns().size(); i++) {
                sb.append(getColumns().get(i).Name + " " + getColumns().get(i).Type);
                if (i != getColumns().size() - 1)
                    sb.append(",");
            }
            sb.append(")");
            return sb.toString();
        }

    }

    public static final class disposals extends Base implements BaseColumns {

        @Override
        public Integer getID() {
            return ID_DISPOSALS;
        }

        @Override
        public Uri getURI() {
            return URI_DISPOSALS;
        }

        @Override
        public String getTableName() {
            return PATH_DISPOSALS;
        }

        @Override
        public ColumnList getColumns() {
            if (Columns == null){
                Columns = new ColumnList();
                Columns.add(new Column("_id", "id", "integer primary key autoincrement", "", false, 0));
                Columns.add(new Column("number", "Номер", "Text", "Text", true, 1));
                Columns.add(new Column("theme", "Тема", "Text", "Text", true, 2));
                Columns.add(new Column("task", "Задача", "Text", "Text", true, 3));
                Columns.add(new Column("shortTask", "Задача...", "Text", "Text", true, 4));
                Columns.add(new Column("readed", "Прочитано", "integer", "bit", true, 5));
                Column column = new Column("isExecute", "Выполнено", "integer", "bit", true, 6);
                column.defaultValues = new Boolean[]{false};
                Columns.add(column);
                Columns.add(new Column("disabled", "Отклонено", "integer", "bit", true, 7));
                Columns.add(new Column("sender_id", "Отправитель", "integer", "link:staff", true, 8));
                Columns.add(new Column("receiver_id", "Получатель", "integer", "link:staff", true, 9));
            }
            return super.getColumns();
        }
    }

    public static final class disposal_comment extends Base implements  BaseColumns{

        public static ContentValues getContentValue(int disposal_id, String note_text, String dateCreate, String userName){
            ContentValues result = new ContentValues();
            result.put("disposal_id", disposal_id);
            result.put("note_text", note_text);
            result.put("dateCreate", dateCreate);
            result.put("userName", userName);
            return result;
        }

        @Override
        public Integer getID() {
            return ID_DISPOSALS_COMMENT;
        }

        @Override
        public Uri getURI() {
            return URI_DISPOSALS_COMMENT;
        }

        @Override
        public String getTableName() {
            return PATH_DISPOSALS_COMMENT;
        }

        @Override
        public ColumnList getColumns() {
            if (Columns == null){
                Columns = new ColumnList();
                Columns.add(new Column("_id", "id","integer primary key autoincrement","",false,0));
                Columns.add(new Column("disposal_id", "Задача","Text","link:disposals",true,1));
                Columns.add(new Column("note_text", "Примечание","Text","Text",true,2));
                Columns.add(new Column("dateCreate", "Дата","Text","Text",true,3));
                Columns.add(new Column("userName", "Пользователь","Text","Text",true,4));
            }
            return super.getColumns();
        }
    }

    public static final class staff extends Base implements BaseColumns{

        @Override
        public Integer getID() {
            return ID_STAFF;
        }

        @Override
        public Uri getURI() {
            return URI_STAFF;
        }

        @Override
        public String getTableName() {
            return PATH_STAFF;
        }

        @Override
        public ColumnList getColumns() {
            if (Columns == null){
                Columns = new ColumnList();
                Columns.add(new Column("_id", "id","integer primary key autoincrement","",false,0));
                Columns.add(new Column("userName", "Пользователь","Text","Text",true,1));
            }
            return super.getColumns();
        }
    }
}

