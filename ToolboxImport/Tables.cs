using System;
using System.Text; 
using System.IO;
using System.Collections.Generic;
using System.Data;
using System.Data.SqlClient;
using Syncfusion.XlsIO;

namespace es.jbauer.lib.tables
{
    public interface TableReader : IDisposable
    {
        /* List of headers. */
        string[] getHeaders();

        /* True if next row is the first row. */
        bool isFirstRow();

        /* If there is another row. */
        bool hasNextRow();

        /* Next row to display. */
        TableRow getNextRow();

        /* Information about the source from which we read data. */
        string getInfo();

        /* Information about the source from which we read data. */
        void setInfo(string info);

        /* Sets the date formatting to be used when we parse string dates to Date. Default uses US formatting. */
        void setDateFormat(string dateFormat, string dateTimeFormat);
    }

    public interface TableWriter : IDisposable
    {
        void writeHeaders(string[] headers); 
	    
        void writeRow(string[] values); 
	
        void writeRow(TableRow row); 
    }

    public interface TableRow
    {
        string[] getHeaders();

        int getCount();

        string[] getValues();

        string getSourceInfo();

        string get(int i);

        string get(string col);

        Int32 getInt(int i);
        Int32 getInt(string col);

        Int64 getInt64(int i);
        Int64 getInt64(string col);

        DateTime getDate(int i);
        DateTime getDate(string col);

        //        Float getFloat(int i); 
        //        Float getFloat(string col); 

        Boolean getBoolean(int i);
        Boolean getBoolean(string col);

        //        Object get(int col, Class type); 
        //        Object get(string col, Class type); 
    }

    public class JoinTableReader : TableReader
    {
        private string[] headers;
        private string info;

        int index = -1; 
        private List<TableRow> rows = new List<TableRow>(); 

        public JoinTableReader(TableReader reader1, string prefix1, string col1, TableReader reader2, string prefix2, string col2)
        {
            headers = append(reader1.getHeaders(), reader2.getHeaders());
            int size1 = reader1.getHeaders().Length; 
            for (int i = 0; i < size1; i++) 
                headers[i] = prefix1 + "." + headers[i]; 

            for (int i = size1; i < headers.Length; i++) 
                headers[i] = prefix2 + "." + headers[i]; 

            info = "Join: " + reader1.getInfo() + "(" + col1 + "), " + reader2.getInfo() + " (" + col2 + ")";

            Dictionary<string, TableRow> row1Lookup = new Dictionary<string, TableRow>(); 
            while (reader1.hasNextRow())
            {
                TableRow row = reader1.getNextRow();
                string key = row.get(col1); 
                if (key != null)
                {
                    if (!row1Lookup.ContainsKey(key))
                        row1Lookup.Add(key, row); 
//                    else
//                        Console.Out.WriteLine(key + " => " + row.ToString()); 
                }
            }

            while (reader2.hasNextRow())
            {
                TableRow row2 = reader2.getNextRow();
                TableRow row1 = null;
                if (row1Lookup.TryGetValue(row2.get(col2), out row1))
                    rows.Add(new StringArrayTableRow(headers, append(row1.getValues(), row2.getValues()))); 
            }
        }

        private string[] append(string[] arr1, string[] arr2)
        {
            string[] result = new string[arr1.Length + arr2.Length];
            Array.Copy(arr1, result, arr1.Length);
            Array.Copy(arr2, 0, result, arr1.Length, arr2.Length);
            return result;
        }

        public string[] getHeaders()
        { return headers; }

        public bool isFirstRow()
        { return index == 0;  }

        public bool hasNextRow()
        { return index + 1< rows.Count; } 

        public TableRow getNextRow()
        {
            index++; 
            return rows[index]; 
        }

        public void Dispose()
        {}

        public string getInfo()
        { return info; }

        public void setInfo(string info)
        {}
        
        public void setDateFormat(string dateFormat, string dateTimeFormat)
        {}
    }

    public class TableUtil
    {
        public static TableReader getTableReader(string vendor, string connection, string query)
        {
            if ("MSSQL".Equals(vendor) || "OLEDB".Equals(vendor) || "MYSQL".Equals(vendor))
                return new DbTableReader(vendor, connection, query); 
                
            if ("EXCEL".Equals(vendor))
                return new ExcelTableReader(connection, query); 

//            if ("CSV".Equals(vendor))
//                return new ExcelTableReader(connection); 
                
            return null; 
        }
        
    }
    
    public class DbUtil
    {
        public static IDbConnection getConnection(string vendor, string connection)
        {
            if ("MSSQL".Equals(vendor))
                return new SqlConnection(connection);
            else if ("OLEDB".Equals(vendor))
                return new System.Data.OleDb.OleDbConnection(connection); 
            else if ("MYSQL".Equals(vendor))
                return new MySql.Data.MySqlClient.MySqlConnection(connection); 
            else
                throw new Exception("Unknown vendor type '" + vendor + "'");
        }
    }

    public class ExcelTableReader : TableReader
    {
        private string[] headers; 
        private ExcelEngine xlsEngine; 
        private IApplication xlsApp; 
        private IWorksheet xlsSheet; 
        private int nextRow = 0; 
        private int skipRows = 0; 

        private string info; 
        
        public ExcelTableReader(string filename)
            : this(filename, null)
        {}
        
        public ExcelTableReader(string filename, string worksheetName)
        {
            if (worksheetName != null)
            {
                int pos = worksheetName.IndexOf("!"); 
                if (pos != -1)
                {
                    skipRows = Int32.Parse(worksheetName.Substring(pos + 1)); 
                    if (pos > 1)
                        worksheetName = worksheetName.Substring(0, pos); 
                    else
                        worksheetName = null; 
                }
            }

            xlsEngine = new ExcelEngine();
            xlsApp = xlsEngine.Excel;
            
            if (filename.EndsWith(".tsv"))
                xlsApp.Workbooks.Open(filename, "\t"); 
            else if (filename.EndsWith(".csv"))
                xlsApp.Workbooks.Open(filename, ","); 
            else
                xlsApp.Workbooks.Open(filename);

            IWorkbook wb = xlsApp.Workbooks[0];
            if (String.IsNullOrEmpty(worksheetName))
                xlsSheet = wb.Worksheets[0];
            else
            {
                int i = 0; 
                while (xlsSheet == null && i < wb.Worksheets.Count)
                {
                    if (worksheetName.Equals(wb.Worksheets[i].Name))
                        xlsSheet = wb.Worksheets[i]; 
                        
                    i++; 
                }
                
                if (xlsSheet == null)
                    xlsSheet = wb.Worksheets[0]; 
            }

            List<string> tmpHeaders = new List<string>(); 
            foreach (IRange cell in xlsSheet.Rows[skipRows].Cells)
                tmpHeaders.Add(cell.Value2.ToString());
                
            nextRow = skipRows; 

            headers = getArray(tmpHeaders);
        }
        
        private string[] getArray(IList<string> values)
        {
            string[] result = new string[values.Count]; 
            for (int i = 0; i < result.Length; i++)
                result[i] = values[i]; 
                
            return result; 
        }
        
        public string[] getHeaders()
        { return headers; } 

        /** @return True if next row is the first row. */  
        public bool isFirstRow()
        { return nextRow == skipRows; } 
        
        /** @return If there is another row. */
        public bool hasNextRow()
        { return nextRow + 1 < xlsSheet.Rows.Length; }

        /** @return Next row to display. */
        public TableRow getNextRow()
        {
            if (!hasNextRow())
                return null; 
                
            List<string> values = new List<string>(); 
            foreach (IRange cell in xlsSheet.Rows[nextRow + 1].Cells)
                values.Add(cell.Value2.ToString());

            nextRow++; 

            return new StringArrayTableRow(headers, getArray(values)); 
        }

        public string getInfo()
        { return info; } 
        
        public void setInfo(string info)
        { this.info = info; }
        
        public void setDateFormat(string dateFormat, string dateTimeFormat)
        {}
        
        public void Dispose()
        {
            xlsApp.Workbooks.Close();
            xlsEngine.Dispose();
        }
    }
    
    public class DbTableReader : TableReader
    {
        private string[] headers;
        private string query;

        private IDbConnection conn;
        private IDataReader reader;
        private TableRow row;
        private TableRow nextRow;
        private string info; 
        private bool firstRow = true; 

        public DbTableReader(string vendor, string connection, string query)
        {
            this.query = query;

            conn = DbUtil.getConnection(vendor, connection); 
            conn.Open();

            IDbCommand cmd = conn.CreateCommand();
            cmd.CommandText = query;
            Console.Out.WriteLine("==> " + query); 
            Console.Out.Flush(); 

            reader = null;
            reader = cmd.ExecuteReader();
            headers = new string[reader.FieldCount];
            for (int i = 0; i < headers.Length; i++)
                headers[i] = reader.GetName(i); 
        }

        public string[] getHeaders()
        { return headers; } 

        public bool isFirstRow()
        { return firstRow; } 

        private void ensureNextRow()
        {
            if (nextRow != null)
                return;

            if (reader.Read())
            {
                object[] v = new object[headers.Length]; 
                reader.GetValues(v);
                string[] values = new string[headers.Length];
                for (int i = 0; i < values.Length; i++)
                    values[i] = v[i] == null ? null : v[i].ToString(); 

                nextRow = new StringArrayTableRow(headers, values); 
            }
        }

        public bool hasNextRow()
        {
            ensureNextRow();
            return nextRow != null;
        }

        public TableRow getNextRow()
        {
            if (!hasNextRow())
                return null;

            firstRow = row == null; 
            row = nextRow;
            nextRow = null;

            return row; 
        }

        public void Dispose()
        {
            reader.Close();  
            conn.Close(); 
            GC.SuppressFinalize(this);
        }

        public string getInfo()
        { return info; }

        public void setInfo(string info)
        { this.info = info; } 

        public void setDateFormat(string dateFormat, string dateTimeFormat)
        { throw new NotImplementedException(); }
    }

    abstract class TableRowBase : TableRow
    {
        private string[] headers;
        private string info;

        public abstract string get(int i);

        public abstract string[] getValues(); 

        public TableRowBase(string[] headers)
        { this.headers = headers; }

        public string[] getHeaders()
        { return headers; }

        public int getCount()
        { return headers.Length; }

        public string getSourceInfo()
        { return info; }

        public void setSourceInfo(string info)
        { this.info = info; }

        public string get(string col)
        { return get(getIndex(col)); }

        public int getInt(int i)
        {
            if (!isValid(i))
                return Int32.MinValue;

            return Int32.Parse(get(i));
        }

        public int getInt(string col)
        { return getInt(getIndex(col)); }

        public long getInt64(int i)
        { return Int64.Parse(get(i)); }

        public long getInt64(string col)
        { return getInt64(getIndex(col)); }

        public DateTime getDate(int i)
        {
            if (!isValid(i))
                return DateTime.MinValue;

            return DateTime.Parse(get(i));
        }

        public DateTime getDate(string col)
        { return getDate(getIndex(col)); }

        public bool getBoolean(int i)
        {
            if (!isValid(i))
                return false;

            return Boolean.Parse(get(i));
        }

        public bool getBoolean(string col)
        { return getBoolean(getIndex(col)); }

        protected bool isValid(int index)
        { return index >= 0 && index < headers.Length; }

        protected int getIndex(string col)
        {
            for (int i = 0; i < headers.Length; i++)
                if (headers[i].Equals(col))
                    return i;

            throw new ArgumentException("Unknown column '" + col + "'");
        }
    }

    class DictionaryTableRow : StringArrayTableRow
    {
        public DictionaryTableRow(string[] headers, Dictionary<string, string> values)
            : base(headers, DictionaryTableRow.getValues(headers, null, values))
        {}

        public DictionaryTableRow(TableRow row, Dictionary<string, string> values)
            : base(row.getHeaders(), DictionaryTableRow.getValues(row.getHeaders(), row.getValues(), values))
        {}

        private static string[] getValues(string[] headers, string[] values, Dictionary<string, string> mapValues)
        {
            string[] result = new string[headers.Length];
            for (int i = 0; i < headers.Length; i++)
            {
                if (values != null)
                    result[i] = values[i];

                string value = "";
                if (mapValues.TryGetValue(headers[i], out value))
                    result[i] = value; 
            }

            return result; 
        }
    }

    class StringArrayTableRow : TableRowBase
    {
        private string[] values; 

        public StringArrayTableRow(string[] headers, string[] values)
            : base(headers)
        { this.values = values; }

        public override string[] getValues()
        { return values; }

        override public string get(int i)
        { return values[i]; } 

    }

    class LogTableWriter : TableWriter
    {
        private TextWriter writer;
        private string[] headers = null;
        private bool headersWritten = false; 
        private string prefix = ""; 
        
        public LogTableWriter(TextWriter writer, string prefix)
        { 
            this.writer = writer; 
            this.prefix = prefix; 
        }

        public LogTableWriter(TextWriter writer)
        { this.writer = writer; }

        public void writeHeaders(string[] headers)
        {
            if (this.headers == null)
                this.headers = headers;
            else
            {
                if (headers.Length != this.headers.Length)
                    throw new Exception("Mismatching header count: " + headers.Length + " vs " + this.headers.Length);

                for (int i = 0; i < headers.Length; i++)
                    if (!headers[i].Equals(this.headers[i]))
                        throw new Exception("Mismatching header[" + i + "]: " + headers[i] + " vs " + this.headers[i]);
            }
        }

        public void writeRow(string[] values)
        {
            if (!headersWritten)
            {
                if (writer != null)
                    writer.WriteLine(prefix + String.Join(", ", headers));
                headersWritten = true; 
            }

            if (writer != null)
                writer.WriteLine(prefix + String.Join(", ", values)); 
        }

        public void writeRow(TableRow row)
        { writeRow(row.getValues()); }

        public void Dispose()
        { 
            if (writer != null)
                writer.Flush(); 
        }
    }

    class NullTableWriter : TableWriter
    {
        public void writeHeaders(string[] headers)
        { }

        public void writeRow(string[] values)
        { }

        public void writeRow(TableRow row)
        { }

        public void Dispose()
        { }
    }


    class DbTableWriter : TableWriter
    {
        private IDbConnection conn; 
        private string table; 
        private string[] headers; 
        
        public DbTableWriter(string vendor, string connection, string table)
        {
            conn = DbUtil.getConnection(vendor, connection); 
            conn.Open(); 
            this.table = table; 
        } 
        
        public void writeHeaders(string[] headers)
        { this.headers = headers; }

        public void writeRow(string[] values)
        {
            IDbCommand cmd = conn.CreateCommand();
            try {
                cmd.CommandText = getInsertSql();
                for (int i = 0; i < headers.Length; i++)
                {
                    IDbDataParameter param = cmd.CreateParameter();
                    param.ParameterName = headers[i];
                    if (String.IsNullOrEmpty(values[i]))
                        param.Value = DBNull.Value;
                    else
                        param.Value = values[i];
                        
                    cmd.Parameters.Add(param); 
                }
                
                cmd.ExecuteNonQuery();
            }
            finally 
            { cmd.Dispose(); }
        }

        public void writeRow(TableRow row)
        {
            if (headers == null)
                headers = row.getHeaders(); 
                
            writeRow(row.getValues()); 
        }

        /** Finish writing table. */ 
        public void Dispose()
        {
            if (conn != null)
                conn.Close(); 
                
            conn = null; 
            GC.SuppressFinalize(this);
        }
        
        private string getInsertSql()
        {
            StringBuilder result = new StringBuilder(); 
            result.Append("INSERT INTO "); 
            result.Append(table); 
            result.Append(" ("); 
            result.Append(String.Join(",", headers)); 
            result.Append(") VALUES (@"); 
            result.Append(String.Join(",@", headers)); 
            result.Append(")"); 
            
            return result.ToString(); 
        }
    }
    
}
