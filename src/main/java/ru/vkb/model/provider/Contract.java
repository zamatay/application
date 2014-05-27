package ru.vkb.model.provider;

import android.content.UriMatcher;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Zamuraev_av on 26.02.14.
 */
public class Contract {

    public static final UriMatcher sUriMatcher;

    static {
        sUriMatcher = new UriMatcher(0);
        sUriMatcher.addURI(Contract.disposals.AUTHORITY, Contract.disposals.CONTENT_PATH, Contract.disposals.PATH);
        sUriMatcher.addURI(Contract.disposal_comment.AUTHORITY, Contract.disposal_comment.CONTENT_PATH, Contract.disposal_comment.PATH);
    }

    public interface PATH{
        public String _CONTENT_PATH();
        public Uri _CONTENT_URI();
        public String _CONTENT_TYPE();

    }

    public static PATH getContractByUri(Uri uri){
        switch (sUriMatcher.match(uri)) {
            case Contract.disposals.PATH:
                return new Contract.disposals();
            case Contract.disposal_comment.PATH:
                return new Contract.disposal_comment();
            default: return null;
        }
    }
    public static class Base {
        public static final String AUTHORITY = "ru.vkb";
        public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);
    }

    public interface disposalsDescription {
        public static final String[] columnsName = {"_id","number", "theme", "task", "shortTask", "readed", "isExecute", "disabled", "sender","receiver"};
        public static final String[] columnsCaption = {"id", "Номер", "Тема", "Задача", "Задача...", "Прочитано", "Выполнено", "Отклонено", "Отправитель","Получатель"};
        public static final String[] columnsType = {"integer primary key autoincrement", "Text", "Text", "Text", "Text", "Text", "Text", "Text", "Text","Text"};
        public static final String[] columnsMeta = {"", "", "", "", "", "", "", "", "link:sender","link:receiver"};
        public static final Boolean[] filterEnabled = {false, true, true, true, true, true, true, true, true,true};
        public static final int id = 0;
        public static final int number = 1;
        public static final int theme = 2;
        public static final int task = 3;
        public static final int shortTask = 4;
        public static final int readed = 5;
        public static final int isExecute = 6;
        public static final int disabled = 7;
        public static final int sender = 8;
        public static final int receiver = 9;
    }

    public interface disposalCommentDescription {
        public static final String disposal_id = "disposal_id";
        public static final String note_text = "note_text";
        public static final String dateCreate = "dateCreate";
        public static final String userName = "userName";
    }

    public static final class disposals extends Base implements disposalsDescription, BaseColumns, PATH {
        public static final String[] PROJECTION;

        static {
            PROJECTION = disposalsDescription.columnsName;
        }
        public static final int PATH = 1;
        public static String CONTENT_PATH = "disposals";
        public static Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, CONTENT_PATH);
        public static String CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + CONTENT_PATH;

        @Override
        public String _CONTENT_PATH() {
            return CONTENT_PATH;
        }

        @Override
        public Uri _CONTENT_URI() {
            return CONTENT_URI;
        }

        @Override
        public String _CONTENT_TYPE() {
            return CONTENT_TYPE;
        }

        public static String getCreateTableSql() {
            StringBuilder sb = new StringBuilder("create table " + CONTENT_PATH + " (");
            for (int i = 0; i < PROJECTION.length; i++) {
                sb.append(PROJECTION[i] + " " + disposalsDescription.columnsType[i]);
                if (i != PROJECTION.length - 1)
                    sb.append(",");
            }
            sb.append(")");
            return sb.toString();
        }
    }

    public static final class disposal_comment extends Base implements disposalCommentDescription, BaseColumns, PATH{
        public static final String[] PROJECTION;

        static {
            PROJECTION = new String[]{_ID,disposal_id,note_text,dateCreate, userName};
        }

        public static final int PATH = 2;
        public static String CONTENT_PATH = "disposalCommentDescription";
        public static Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, CONTENT_PATH);
        public static String CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + CONTENT_PATH;

        @Override
        public String _CONTENT_PATH() {
            return CONTENT_PATH;
        }

        @Override
        public Uri _CONTENT_URI() {
            return CONTENT_URI;
        }

        @Override
        public String _CONTENT_TYPE() {
            return CONTENT_TYPE;
        }

        public static String getCreateTableSql(){
            return "create table " + CONTENT_PATH + " (" +
                    _ID + " integer primary key autoincrement, " +
                    disposal_id + " integer," +
                    note_text + " text," +
                    dateCreate + " text," +
                    userName + " text)";

        }
    }
}

