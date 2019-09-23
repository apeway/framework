package org.liws.framework.datasource.cache;

import oracle.jdbc.driver.DatabaseError;
import oracle.jdbc.rowset.OracleCachedRowSet;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.*;
import java.util.Calendar;
import java.util.Map;
import java.util.TreeMap;

public class AECachedRowSet extends OracleCachedRowSet implements java.io.Serializable{

    private transient Map<String, Integer>  indexCache = new TreeMap<>(String::compareToIgnoreCase);

    public AECachedRowSet() throws SQLException {
    }

    @Override
    public synchronized Object getObject(String paramString)
            throws SQLException {
        return getObject(getColumnIndex(paramString));
    }

    @Override
    public boolean getBoolean(String paramString)
            throws SQLException {
        return getBoolean(getColumnIndex(paramString));
    }

    public byte getByte(String paramString)
            throws SQLException {
        return getByte(getColumnIndex(paramString));
    }

    public short getShort(String paramString)
            throws SQLException {
        return getShort(getColumnIndex(paramString));
    }
    @Override
    public int getInt(String paramString)
            throws SQLException {
        return getInt(getColumnIndex(paramString));
    }

    public long getLong(String paramString)
            throws SQLException {
        return getLong(getColumnIndex(paramString));
    }

    public float getFloat(String paramString)
            throws SQLException {
        return getFloat(getColumnIndex(paramString));
    }

    public double getDouble(String paramString)
            throws SQLException {
        return getDouble(getColumnIndex(paramString));
    }

    public BigDecimal getBigDecimal(String paramString, int paramInt)
            throws SQLException {
        return getBigDecimal(getColumnIndex(paramString), paramInt);
    }

    public byte[] getBytes(String paramString)
            throws SQLException {
        return getBytes(getColumnIndex(paramString));
    }

    public Date getDate(String paramString)
            throws SQLException {
        return getDate(getColumnIndex(paramString));
    }

    public Time getTime(String paramString)
            throws SQLException {
        return getTime(getColumnIndex(paramString));
    }

    public Timestamp getTimestamp(String paramString)
            throws SQLException {
        return getTimestamp(getColumnIndex(paramString));
    }

    public Time getTime(String paramString, Calendar paramCalendar)
            throws SQLException {
        return getTime(getColumnIndex(paramString), paramCalendar);
    }

    public Date getDate(String paramString, Calendar paramCalendar)
            throws SQLException {
        return getDate(getColumnIndex(paramString), paramCalendar);
    }

    public InputStream getAsciiStream(String paramString)
            throws SQLException {
        return getAsciiStream(getColumnIndex(paramString));
    }

    public InputStream getUnicodeStream(String paramString)
            throws SQLException {
        return getUnicodeStream(getColumnIndex(paramString));
    }

    public String getString(String paramString)
            throws SQLException {
        return getString(getColumnIndex(paramString));
    }

    public InputStream getBinaryStream(String paramString)
            throws SQLException {
        return getBinaryStream(getColumnIndex(paramString));
    }

    public Reader getCharacterStream(String paramString)
            throws SQLException {
        return getCharacterStream(getColumnIndex(paramString));
    }

    public BigDecimal getBigDecimal(String paramString)
            throws SQLException {
        return getBigDecimal(getColumnIndex(paramString));
    }

    public Timestamp getTimestamp(String paramString, Calendar paramCalendar)
            throws SQLException {
        return getTimestamp(getColumnIndex(paramString), paramCalendar);
    }

    public Object getObject(String paramString, Map paramMap)
            throws SQLException {
        return getObject(getColumnIndex(paramString), paramMap);
    }

    public Ref getRef(String paramString)
            throws SQLException {
        return getRef(getColumnIndex(paramString));
    }

    public Blob getBlob(String paramString)
            throws SQLException {
        return getBlob(getColumnIndex(paramString));
    }

    public Clob getClob(String paramString)
            throws SQLException {
        return getClob(getColumnIndex(paramString));
    }

    public Array getArray(String paramString)
            throws SQLException {
        return getArray(getColumnIndex(paramString));
    }

    private int getColumnIndex(String columnName)
            throws SQLException {
        if ((columnName == null) || (columnName.equals(""))) {
            SQLException localSQLException1 = DatabaseError.createSqlException(getConnectionDuringExceptionHandling(), 6, columnName);
            localSQLException1.fillInStackTrace();
            throw localSQLException1;
        }
        if(indexCache == null){
            indexCache = new TreeMap<>(String::compareToIgnoreCase);
        }
        if(indexCache.containsKey(columnName)){
            return indexCache.get(columnName);
        }else{
            int len = getMetaData().getColumnCount();
            int i = 0;
            for (; i < len; i++) {
                if (columnName.equalsIgnoreCase(getMetaData().getColumnName(i + 1))) {
                    break;
                }
            }
            if (i >= len) {
                SQLException localSQLException2 = DatabaseError.createSqlException(getConnectionDuringExceptionHandling(), 6, columnName);
                localSQLException2.fillInStackTrace();
                throw localSQLException2;
            }
            indexCache.put(columnName,i+1);
            return i + 1;
        }

    }
}
