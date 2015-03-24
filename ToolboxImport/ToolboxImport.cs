using System; 
using System.Text;
using System.IO;
using System.Xml;
using System.Data.Common;
using System.Collections.Generic;
using System.Windows;

using es.jbauer.lib.tables; 

namespace com.emistoolbox.import
{
    public class ToolboxImport 
    {
        private static int showConfigListMenu(List<ToolboxImportConfig> configs, bool commit)
        {
            List<string> choices = new List<string>();
            for (int i = 0; i < configs.Count; i++)
                choices.Add("[" + i + "] View " + configs[i].getDataSet().getName());
            choices.Add("[*] Import All");

            return displayMenu("Data Set Menu", choices.ToArray(), null, commit); 
        }

        private enum DEBUG_MENU { RUN_IMPORT, EXIT }; 

        private static DEBUG_MENU showDebugMenu(ToolboxImportConfig config, bool commitData)
        {
            while (true)
            {
                int index = displayMenu("RunConfig > ImportConfig Menu", new string[] { "[0] Review mappings.", "[1] Run single import.", "[*] Run full import.", "[x] Back" }, config, commitData);
                switch (index)
                { 
                    case 0:
                        showMappingMenu(config, commitData);
                        break; 
                    case 1:
                        showSingleImportMenu(config, commitData);
                        break;
                    case 2:
                        return DEBUG_MENU.RUN_IMPORT;
                    case 3: 
                        return DEBUG_MENU.EXIT; 
                }
            }
        }

        private static int displayMenu(String menu, string[] options, ToolboxImportConfig config, bool commit)
        {
            Console.WriteLine(); 
            Console.WriteLine("== " + menu + " ==");
            if (config != null)
                Console.Write(" Data Set: " + config.getDataSet().getName() + "\n----\n");

            foreach (string option in options)
                Console.WriteLine(option);

            Console.WriteLine(); 
            if (commit)
                Console.WriteLine("** IMPORTANT **: Commit is ON!");

            while (true)
            {
                string line = Console.ReadLine();
                for (int i = 0; i < options.Length; i++) 
                {
                    if (options[i].StartsWith("[" + line + "]"))
                        return i; 
                }

                Console.WriteLine("Invalid choice."); 
            }
        }

        private static void showMappingMenu(ToolboxImportConfig config, bool commit)
        {
            List<ImportMapping> mappings = config.getMappings(); 

            List<string> choices = new List<string>();
            for (int i = 0; i < mappings.Count; i++) 
                choices.Add("[" + i + "] " + mappings[i].getId());
            choices.Add("[x] Back"); 

            while (true)
            {
                int index = displayMenu("RunConfig > ImportConfig > Mapping Menu", choices.ToArray(), config, commit);
                if (index == mappings.Count)
                    return;

                try
                {
                    ImportMapping mapping = mappings[index];
                    Console.WriteLine("== Mapping: " + mapping.getId() + " ==");
                    output(mapping, Console.Out);
                    Console.WriteLine("press any key ...");
                    Console.ReadKey(); 
                }
                catch (Exception)
                {}
            }
        }

        private static void showSingleImportMenu(ToolboxImportConfig config, bool commit)
        {
            List<string> choices = new List<string>();
            for (int i = 0; i < config.getImports().Count; i++)
                choices.Add("[" + i + "] " + config.getImports()[i].getName()); 
            choices.Add("[x] Back");

            int index = displayMenu("RunConfig > ImportConfig > Single Import", choices.ToArray(), config, commit); 
            if (index == config.getImports().Count)
                return;

            ExceptionTreeRenderer renderer = getRenderer(config, config.getImports()[index]);
            try { runImport(config, config.getImports()[index], renderer, commit); }
            finally { renderer.close(); }
        }

        private static ExceptionTreeRenderer getRenderer(ToolboxImportConfig config, ImportConfig importConfig)
        {
            MultipleExceptionTreeRenderer result = new MultipleExceptionTreeRenderer(); 
            if (importConfig != null)
            {
                TextWriter singleFile = new StreamWriter("./" + config.getDataSet().getName() + "_" + importConfig.getName() + ".html"); 
                result.add(new HtmlExceptionTreeRenderer(singleFile, true)); 
            }
            else
            {
                DirectoryInfo dir = Directory.CreateDirectory("./" + config.getDataSet().getName()); 
                result.add(new HtmlExceptionTreeRenderer(dir)); 
            }
            
            result.add(new TextExceptionTreeRenderer(Console.Out)); 

            return result;
        }

        [STAThread]
        public static void MainExcel(string[] args)
        {
            ExcelTableReader reader = new ExcelTableReader(args[0]); 
            output(reader.getHeaders()); 
            
            int count = 0; 
            while (reader.hasNextRow())
            {
                TableRow row = reader.getNextRow(); 
                output(row.getValues()); 

                count++; 
                if (count >= 10)
                    return; 
            }
        }

        private static void output(string[] values)
        { Console.Out.WriteLine(String.Join(", ", values)); } 
        
        [STAThread]
        public static void Main(string[] args)
        {
            // Allow command line options. 
            if (args.Length > 0)
            {
                IList<string> arguments = new List<string>(); 
                for (int i = 0; i < args.Length; i++) 
                    arguments.Add(args[i]); 

                processImport(arguments); 
            }
            else
            {
                ImportTool.LoadForm form = new ImportTool.LoadForm();
                form.ShowDialog();
            }
        }
        
        private static void processImport(IList<string> args)
        {
            bool commit = null != findAndRemove(args, "-commit"); 
            
            ToolboxImportConfig mainConfig = null; 
            ExceptionTreeRenderer renderer = null; 
            try { 
                List<ToolboxImportConfig> items = ImportConfigIO.parseToolboxImportConfigs(args[0]);
                if (items != null && items.Count == 1)
                    mainConfig = items[0]; 

                renderer = getRenderer(mainConfig, null); 
                runImport(mainConfig, null, renderer, commit);
            }
            finally 
            { renderer.close(); }
        }
        
        private static string findAndRemove(IList<string> values, string needle)
        {
            for (int i = 0; i < values.Count; i++)
            {
                if (values[i].StartsWith(needle))
                {
                    try { return values[i]; }
                    finally { values.RemoveAt(i); }
                }
            }
            
            return null; 
        }
    
        public static void MainX(string[] args)
        {
            Console.Write("Usage: toolboxImport.exe [-commit] <config.xml> \n\n"); 
            if (args.Length < 1)
                return; 

            string configPath = args[0]; 
            bool commit = configPath.Equals("-commit"); 
            if (commit)
                configPath = args[1]; 
            
            Console.Write("Reading configuration " + configPath);
            if (commit)
                Console.WriteLine("** IMPORTANT **: Commit is ON!");
            List<ToolboxImportConfig> importConfigs = ImportConfigIO.parseToolboxImportConfigs(configPath);
            Console.WriteLine();

            while (true)
            {
                ToolboxImportConfig config = null;
                if (importConfigs.Count == 1)
                    config = importConfigs[0];
                else
                {
                    int configIndex = showConfigListMenu(importConfigs, commit);
                    if (configIndex < importConfigs.Count)
                        config = importConfigs[configIndex];
                    else
                        config = null; 
                }

                if (config != null)
                {
                    if (DEBUG_MENU.RUN_IMPORT == showDebugMenu(config, commit))
                    {
                        ExceptionTreeRenderer renderer = getRenderer(config, null);
                        try { runImport(config, null, renderer, commit); }
                        finally { renderer.close(); } 
                    }
                }
                else
                {
                    foreach (ToolboxImportConfig c in importConfigs)
                    {
                        ExceptionTreeRenderer renderer = getRenderer(c, null);
                        try { runImport(c, null, renderer, commit); }
                        finally { renderer.close(); } 
                    }
                }
            }
        }

        public static void runImport(ToolboxImportConfig config, ImportConfig importConfig, ExceptionTreeRenderer renderer, bool commit)
        {
            renderer.section(config.getDataSet().getName()); 
            List<ImportConfig> configs = new List<ImportConfig>();
            if (importConfig == null)
            {
                foreach (ImportConfig item in config.getImports())
                    configs.Add(item);
            }
            else
                configs.Add(importConfig); 

            foreach (ImportConfig import in configs)
            {
                Console.Out.WriteLine("Starting: " + import.getName()); 
                try {
                    int importedCount = 0;
                    List<ImportException> errors = import.run(config, commit, Console.Out, out importedCount);
                    int errorCount = errors.Count;
                    renderer.render(import.getName(), errors);
                    renderer.text(importedCount + " rows successfully imported. " + errorCount + " error reported.");
                }
                catch (DbException ex)
                { renderer.render(ex); }    
            }
        }

        private static void output(ImportMapping mapping, TextWriter writer)
        {
            writer.WriteLine("== " + mapping.getId() + " ==");
            foreach (string key in mapping.getKeys())
                writer.WriteLine("  " + key + " : " + mapping.getValue(key) + ", " + mapping.getName(key));
        }
    }

    /** List<ImportException> contains a flat list of errors that can be shown much better in a tree by Error Type, Error Column, Error Value
      * ImportExceptionTreeItem implements a tree  of error messages and static methods to generate the tree from a flat list of ImportExceptions. */ 
    public class ImportExceptionTreeItem
    {
        private string name; 
        private List<ImportExceptionTreeItem> children = new List<ImportExceptionTreeItem>();
        private int count = 1; 

        public ImportExceptionTreeItem(String name)
        { this.name = name; }

        public string getName()
        { return name; }

        public int getCount()
        { return count; }

        public void setCount(int count)
        { this.count = count; } 

        public void add(ImportExceptionTreeItem node)
        { children.Add(node); } 

        public List<ImportExceptionTreeItem> getChildren()
        { return children; }

        public static List<ImportExceptionTreeItem> getTree(List<ImportException> errors, ImportExceptionInfo[] valueInfos, int index)
        {
            List<ImportExceptionTreeItem> result = new List<ImportExceptionTreeItem>();
            if (index >= valueInfos.Length)
                return result;
 
            foreach (string value in getAll(errors, valueInfos[index]))
            {
                ImportExceptionTreeItem node = new ImportExceptionTreeItem(value);
                result.Add(node);

                List<ImportException> filteredErrors = filter(errors, valueInfos[index], value); 
                node.setCount(filteredErrors.Count); 
                foreach (ImportExceptionTreeItem child in getTree(filteredErrors, valueInfos, index + 1))
                    node.add(child); 
            }

            return result; 
        }

        /** Top grouping - put all errors resulting from the same SQL query together. */ 
        public static List<ImportExceptionTreeItem> getTree(List<ImportException> errors)
        {
            ImportExceptionInfo[] levelValueInfo = new ImportExceptionInfo[] { 
                new ImportExceptionInfoSql(), 
                new ImportExceptionInfoError(), 
                new ImportExceptionInfoColumn(), 
                new ImportExceptionInfoMapping(),  
                new ImportExceptionInfoValue()
            }; 

            return getTree(errors, levelValueInfo, 0); 
        }

        public static List<ImportException> filter(List<ImportException> items, ImportExceptionInfo valueAccess, string value)
        {
            List<ImportException> result = new List<ImportException>();
            foreach (ImportException item in items)
            {
                if (value.Equals(valueAccess.get(item)))
                    result.Add(item);
            }

            return result; 
        }

        public static List<string> getAll(List<ImportException> items, ImportExceptionInfo value)
        {
            Dictionary<string, bool> values = new Dictionary<string, bool>();
            foreach (ImportException item in items)
            {
                string v = value.get(item); 
                if (v != null)
                    values[v] = true;
            }

            return new List<string>(values.Keys);
        }
    }

    public abstract class ExceptionTreeRenderer
    {
        abstract public void section(string section);

        abstract public void text(string text); 

        public void render(string title, List<ImportException> items)
        { render(title, items, ImportExceptionTreeItem.getTree(items)); }

        abstract public void render(string title, List<ImportException> items, List<ImportExceptionTreeItem> treeItems);

        abstract public void render(Exception ex);

        abstract public void close(); 
    }

    public abstract class MappingSummaryExceptionTreeRenderer : ExceptionTreeRenderer
    {
        private Dictionary<string, ISet<string>> values = new Dictionary<string, ISet<string>>();

        public override void render(string title, List<ImportException> items, List<ImportExceptionTreeItem> treeItems)
        {
            foreach (ImportException ex in items)
                process(ex); 
        }

        private void process(ImportException ex)
        {
            if (!ex.GetType().Equals(typeof(MissingMappingImportException)))
                return; 

            MissingMappingImportException mapEx = (MissingMappingImportException) ex; 
            string key = mapEx.getMapping(); 
            if (!values.ContainsKey(key))
                values.Add(key, new HashSet<string>()); 

            if (!values[key].Contains(mapEx.getValue()))
                values[key].Add(mapEx.getValue());
        }

        public void writeMappingSummary(string filename)
        {
            // Open mapping summary file. 
            TextWriter writer = new StreamWriter(filename);
            using (writer)
            {
                writer.WriteLine("MappingId,Value"); 
                foreach (string key in values.Keys)
                {
                    foreach (string value in values[key])
                        writer.WriteLine(key + "," + value); 
                }

                writer.Flush(); 
            }
        }
    }


    public class HtmlExceptionTreeRenderer : MappingSummaryExceptionTreeRenderer
    {
        private DirectoryInfo dir; // directory for HTML files (uses multiple HTML files). 
        private TextWriter writer; // output stream. 
        private bool closeWriter = false; 
        private List<string> datasets = null; 

        public HtmlExceptionTreeRenderer(TextWriter outStream, Boolean closeWriter)
        {
            this.writer = outStream; 
            this.closeWriter = closeWriter;
            header(writer); 
        }

        public HtmlExceptionTreeRenderer(DirectoryInfo dir)
        {
            this.dir = dir;
            datasets = new List<string>(); 
        }

        public TextWriter getTextWriter()
        { return writer; }

        override public void section(string section)
        {
            if (dir != null)
            {
                closeStream();

                // Open new file
                writer = new StreamWriter(dir.FullName + "/" + section + ".html");
                closeWriter = true;
                header(writer); 
                writer.WriteLine("<h1>" + section + "</h1>");
                datasets.Add(section); 
            }
            else
            {
                writer.WriteLine("<hr>");
                writer.WriteLine("<h1>" + section + "</h1>");
            }
        }

        override public void text(string text)
        { writer.WriteLine(text + "<p/>"); } 

        private void header(TextWriter writer)
        {
            if (writer == null)
                return; 

            writer.WriteLine("<!DOCTYPE html><html><style>");
            writer.WriteLine("table { color:#333333; border-collapse:collapse; }"); 
            writer.WriteLine("table th { padding: 4px; border: 1px solid #666666; background-color: #dedede; border-collapse:collapse; }"); 
            writer.WriteLine("table td { padding: 4px; border: 1px solid #ffffff; background-color: #eeeeee; border-collapse: collapse; }"); 
            writer.WriteLine("</style><body>"); 
        }

        private void closeStream()
        {
            if (writer == null)
                return;

            writer.WriteLine("</body></html>");
            writer.Flush(); 
            if (closeWriter)
                writer.Close();
        }

        override public void close()
        {
            if (dir != null)
            {
                closeStream();

                writeMappingSummary(dir.FullName + "/mapErrors.csv");

                // Open index file. 
                TextWriter indexOut = new StreamWriter(dir.FullName + "/index.html");
                using (indexOut)
                {
                    header(indexOut); 
                    indexOut.WriteLine("<h2>Errors of Datasets</h2>");

                    indexOut.WriteLine("<ul>");
                    foreach (string dataset in datasets)
                        indexOut.WriteLine("<li><a href='" + dataset + ".html'>" + dataset + "</a></li>");
                    indexOut.WriteLine("</ul>");

                    indexOut.WriteLine("</body></html>");
                    indexOut.Close();
                }
            }
            else
                writeMappingSummary("mapErrors.csv"); 
        }

        override public void render(string title, List<ImportException> items, List<ImportExceptionTreeItem> treeItems)
        {
            base.render(title, items, treeItems); 
            writer.WriteLine("<h3>" + title + "</h3>");
            foreach (ImportExceptionTreeItem item in treeItems)
            {
                writer.WriteLine("<code>" + item.getName() + "</code><p />");
                writer.WriteLine("<table>");
                writer.WriteLine("  <tr><th>Error</th><th>Column</th><th>Mapping</th><th>Value</th><th>Count</th></tr>");
                render(item.getChildren(), 0, getDepth(item.getChildren()), new List<string>());
                writer.WriteLine("</table>");
            }

            writer.Flush();
        }

        private int getDepth(List<ImportExceptionTreeItem> items)
        {
            int result = 1; 
            foreach (ImportExceptionTreeItem item in items)
            {
                if (item.getChildren().Count > 0)
                    result = Math.Max(result, 1 + getDepth(item.getChildren()));
            }

            return result; 
        }

        private Boolean hasAnyChildren(List<ImportExceptionTreeItem> items)
        {
            foreach (ImportExceptionTreeItem item in items)
            {
                if (item.getChildren().Count > 0)
                    return true;
            }

            return false; 
        }

        private void renderRow(List<string> values, int totalCells)
        {
            writer.WriteLine("<tr>"); 
            for (int i = 0; i < totalCells; i++) 
            {
                writer.Write("<td>");
                if (i < values.Count)
                    writer.Write(values[i]); 
                writer.WriteLine("</td>");
            }

            writer.WriteLine("</tr>");
        }

        private void render(List<ImportExceptionTreeItem> items, int index, int maxLevel, List<string> values)
        {
            bool first = true; 
            foreach (ImportExceptionTreeItem item in items)
            {
                values.Add(item.getName());
                if (item.getChildren().Count == 0)
                {
                    values.Add("" + item.getCount());
                    renderRow(values, maxLevel + 1);
                    values.RemoveAt(values.Count - 1);

                    values.RemoveAt(values.Count - 1);
                    if (first)
                    {
                        int count = values.Count;
                        values = new List<string>();
                        for (int i = 0; i < count; i++)
                            values.Add("");
                        first = false;
                    }
                }
                else
                {
                    render(item.getChildren(), index + 1, maxLevel, values);
                    values.RemoveAt(values.Count - 1);
                }
            }
        }

        override public void render(Exception ex)
        {
            writer.WriteLine("<div class='exception'>" + ex.Message + "<div class='stack'>"); 
            writer.WriteLine(ex.StackTrace); 
            writer.WriteLine("</div></div>"); 
        }
    }

    public class MultipleExceptionTreeRenderer : ExceptionTreeRenderer
    {
        List<ExceptionTreeRenderer> renderers = new List<ExceptionTreeRenderer>(); 

        public MultipleExceptionTreeRenderer()
        {}

        public MultipleExceptionTreeRenderer(ExceptionTreeRenderer[] renderers)
        { 
            foreach (ExceptionTreeRenderer renderer in renderers)
               this.renderers.Add(renderer);
        }

        public void add(ExceptionTreeRenderer renderer)
        { renderers.Add(renderer); }

        public override void text(string text)
        {
            foreach (ExceptionTreeRenderer renderer in renderers)
                renderer.text(text); 
        }

        override public void section(string title)
        { 
            foreach (ExceptionTreeRenderer renderer in renderers)
                renderer.section(title); 
        }

        override public void render(string title, List<ImportException> items, List<ImportExceptionTreeItem> treeItems)
        {
            foreach (ExceptionTreeRenderer renderer in renderers)
                renderer.render(title, items, treeItems); 
        }

        override public void render(Exception ex)
        {
            foreach (ExceptionTreeRenderer renderer in renderers)
                renderer.render(ex); 
        }

        override public void close()
        {
            foreach (ExceptionTreeRenderer renderer in renderers)
                renderer.close(); 
        }
    }

    public class TextExceptionTreeRenderer : ExceptionTreeRenderer
    {
        private TextWriter outStream; 

        public TextExceptionTreeRenderer(TextWriter outStream)
        { this.outStream = outStream; }

        override public void render(string title, List<ImportException> items, List<ImportExceptionTreeItem> treeItems)
        {
            outStream.WriteLine("-- " + title + " --"); 
            render(treeItems, ""); 
        }

        private void render(List<ImportExceptionTreeItem> items, string indent)
        {
            foreach (ImportExceptionTreeItem item in items)
            {
                outStream.WriteLine(indent + item.getName());
                if (item.getChildren() != null && item.getChildren().Count > 0)
                    render(item.getChildren(), indent + "    "); 
            }
        }

        override public void render(Exception ex)
        {
            outStream.WriteLine("== Exception =="); 
            outStream.WriteLine(ex.Message); 
            outStream.WriteLine(ex.StackTrace); 
        }

        override public void section(string sectionTitle)
        { outStream.WriteLine(" ==== " + sectionTitle + " ===="); }

        public override void text(string text)
        { outStream.WriteLine(text); } 

        override public void close()
        {
            Console.Out.WriteLine("Press any key!");
            Console.ReadLine(); 
        }
    }

    public interface ImportExceptionInfo
    {
        string get(ImportException ex);
    }

    public class ImportExceptionInfoSql : ImportExceptionInfo
    {
        public string get(ImportException ex)
        { return ex.getSql();  }
    }
    
    public class ImportExceptionInfoColumn : ImportExceptionInfo
    {
        public string get(ImportException ex)
        { return ex.getColumn() == null ? "" : ex.getColumn(); }
    }
    
    public class ImportExceptionInfoError : ImportExceptionInfo
    {
        public string get(ImportException ex)
        { return ex.getName(); }
    }

    public class ImportExceptionInfoValue : ImportExceptionInfo
    {
        public string get(ImportException ex)
        {
            if (ex.GetType().Equals(typeof(MissingMappingImportException)))
                return ((MissingMappingImportException)ex).getValue();
            else if (ex.GetType().Equals(typeof(MissingValueImportException)))
                return "null";
            else if (ex.GetType().Equals(typeof(UnknownFieldImportException)))
                return ((UnknownFieldImportException) ex).getColumn(); 
            else if (ex.GetType().Equals(typeof(ExceptionImportException)))
                return ((ExceptionImportException) ex).getException().Message;

            return null; 
        }
    }

    public class ImportExceptionInfoMapping : ImportExceptionInfo
    {
        public string get(ImportException ex)
        {
            if (!ex.GetType().Equals(typeof(MissingMappingImportException)))
                return "";

            return ((MissingMappingImportException)ex).getMapping();
        }
    }

    public class ImportConfigIO
    {
        public static List<ToolboxImportConfig> parseToolboxImportConfigs(string filename)
        {
            XmlDocument xml = new XmlDocument();
            xml.Load(filename);

            XmlElement root = xml.DocumentElement;
            if (root.Name != "dataImport")
                throw new Exception("Invalid root tag. Expected 'dataImport'");

            // Parse different DataSets - these consist of variable and database settings. 
            //
            DataSet defaultContext = parseDataSet(root, null);
            List<DataSet> contexts = new List<DataSet>();
            foreach (XmlNode n in root.SelectNodes("./dataSet"))
                contexts.Add(parseDataSet((XmlElement)n, defaultContext));

            if (contexts.Count == 0)
                contexts.Add(defaultContext); 

            // Now parse ToolboxImportConfig for each. The configurations (esp. mappings) can change 
            // depending on the database settings. 
            // 
            List<ToolboxImportConfig> result = new List<ToolboxImportConfig>();
            foreach (DataSet context in contexts)
                result.Add(parseToolboxImportConfig(root, context)); 

            return result; 
        }

        public static ToolboxImportConfig parseToolboxImportConfig(XmlElement root, DataSet context)
        {
            ToolboxImportConfig config = new ToolboxImportConfig();
            config.setDataSet(context); 

            int count = 0;
            foreach (XmlNode n in root.SelectNodes("./mapping"))
            {
                config.addMapping(parseMapping((XmlElement)n, config));
                count++;
            }
            Console.Out.Write("- Parsed " + count + " mappings.\n");

            count = 0;
            foreach (XmlNode n in root.SelectNodes("./import"))
            {
                config.addImport(parseImportConfig((XmlElement)n, config));
                count++;
            }
            Console.Out.Write("- Parsed " + count + " imports.\n");

            return config; 
        }

        private static DataSet parseDataSet(XmlElement root, DataSet defaultContext)
        {
            RunContextImpl result = new RunContextImpl();
            if (root.HasAttribute("name"))
                result.setName(root.GetAttribute("name"));
            else
                result.setName("default"); 

            foreach (XmlNode n in root.SelectNodes("./value"))
            {
                XmlElement tag = (XmlElement)n; 
                result.addContext(tag.GetAttribute("key"), tag.GetAttribute("value")); 
            }

            int count = 0;
            foreach (XmlNode n in root.SelectNodes("./database"))
            {
                result.addDatabase(parseDatabase((XmlElement)n));
                count++; 
            }
            Console.Out.Write("- Parsed " + count + " databases.\n");

            if (defaultContext != null && defaultContext.getDatabases().Count > 0 && defaultContext.getContext().Count > 0)
                return new RunContextJoin(result, defaultContext); 

            return result; 
        }
        
        public static ConfigDatabase parseDatabase(XmlElement tag)
        { return new ConfigDatabase(tag.GetAttribute("id"), tag.GetAttribute("vendor"), tag.GetAttribute("connection")); }

        public static ImportMapping parseMapping(XmlElement tag, ToolboxImportConfig config)
        {
            ImportMapping mapping = new ImportMapping();
            mapping.setId(tag.GetAttribute("id"));
            
            foreach (XmlNode n in tag.SelectNodes("./value"))
            {
                XmlElement child = (XmlElement) n; 
                mapping.add(child.GetAttribute("key").ToUpper().Trim(), child.GetAttribute("value").Trim(), child.GetAttribute("name")); 
            }

            foreach (XmlNode n in tag.SelectNodes("./read"))
            {
                XmlElement readTag = (XmlElement)n;

                string keyColumn = readTag.GetAttribute("keyColumn");
                string valueColumn = readTag.GetAttribute("valueColumn");
                string nameColumn = readTag.GetAttribute("nameColumn"); 

                TableReader reader = parseTableReader(readTag, config.getDataSet());
                while (reader.hasNextRow())
                {
                    TableRow row = reader.getNextRow();
                    mapping.add(row.get(keyColumn).ToUpper().Trim(), row.get(valueColumn).Trim(), String.IsNullOrEmpty(nameColumn) ? null : row.get(nameColumn));
                }
            }

            return mapping; 
        }

        public static TableReader getTableReader(XmlElement sqlTag, DataSet context)
        {
            if (sqlTag == null)
                return null;

            string query = getSqlQuery(sqlTag); 
            if (sqlTag.GetAttributeNode("db") == null)
                throw new Exception("<sql> without required attribute 'db'");

            ConfigDatabase db = context.getDatabase(sqlTag.GetAttribute("db")); 
            if (String.IsNullOrEmpty(query) && !"EXCEL".Equals(db.getVendor()) && !"CSV".Equals(db.getVendor()))
                throw new Exception("<sql> without query (either attribute 'query' or tag content)."); 

            TableReader result = TableUtil.getTableReader(db.getVendor(), db.getConnection(), query);
            if (sqlTag.GetAttributeNode("name") != null)
                result.setInfo(sqlTag.GetAttribute("name"));
            else
                result.setInfo(query); 

                return result; 
        }

        private static String getSqlQuery(XmlElement sqlTag)
        {
            if (sqlTag.HasAttribute("query"))
                return sqlTag.GetAttribute("query");
            else
                return sqlTag.InnerText;
        }

        public static TableReader parseTableReader(XmlElement readTag, DataSet context)
        {
            if (String.IsNullOrEmpty(readTag.GetAttribute("join")))
                return getTableReader((XmlElement) readTag.SelectSingleNode("./sql"), context); 
            else
            {
                string join = readTag.GetAttribute("join");

                string join1 = getStringPart(join, '=', 0);
                string join2 = getStringPart(join, '=', 1);

                string db1 = getStringPart(join1, '.', 0);
                string db2 = getStringPart(join2, '.', 0);
                string col1 = getStringPart(join1, '.', 1);
                string col2 = getStringPart(join2, '.', 1);

                using (TableReader reader1 = getTableReader((XmlElement)readTag.SelectSingleNode("./sql[@db='" + db1 + "']"), context))
                using (TableReader reader2 = getTableReader((XmlElement)readTag.SelectSingleNode("./sql[@db='" + db2 + "']"), context))
                { return new JoinTableReader(reader1, db1, col1, reader2, db2, col2); }
            }
        }

        private static string getStringPart(string value, char delim, int index)
        {
            string[] values = value.Split(delim);
            if (index < values.Length)
                return values[index].Trim();
            return null;
        }


        public static ImportConfig parseImportConfig(XmlElement tag, ToolboxImportConfig config)
        {
            ImportConfig import = new ImportConfig();
            import.setName(tag.GetAttribute("name")); 
            foreach (XmlNode n in tag.SelectNodes("./sql"))
                import.addSqlTag((XmlElement) n);

            foreach (XmlNode n in tag.SelectNodes("./insert"))
                import.addImport(parseImportTable((XmlElement) n, config));

            return import; 
        }

        public static ImportTable parseImportTable(XmlElement tag, ToolboxImportConfig config)
        {
            if (!tag.HasAttribute("db") || !tag.HasAttribute("table"))
                throw new Exception("<import> missings required 'db' or 'table' attribute"); 
            ImportTable table = new ImportTable(config, tag.GetAttribute("db"), tag.GetAttribute("table"));
            foreach (XmlNode n in tag.SelectNodes("./column"))
                table.addColumn(parseImportColumn((XmlElement) n, config, false));

            table.setExpandRow(parseExpandRow((XmlElement) tag.SelectSingleNode("./expandRow"), config)); 
            foreach (XmlNode n in tag.SelectNodes("./contextColumn"))
                table.addContextColumn(parseImportContextColumn((XmlElement)n, config));

            return table; 
        }

        public static ImportExpandRow parseExpandRow(XmlElement tag, ToolboxImportConfig config)
        {
            if (tag == null)
                return null;

            ImportExpandRow result = new ImportExpandRow(tag.GetAttribute("valueHeader").Split(','), tag.GetAttribute("headers").Split(','));
            foreach (XmlNode n in tag.SelectNodes("./expand"))
            {
                XmlElement expandTag = (XmlElement)n;
                result.addExpand(expandTag.GetAttribute("column").Split(','), expandTag.GetAttribute("values").Split(',')); 
            }

            return result; 
        }

        public static ImportColumn parseImportColumn(XmlElement tag, ToolboxImportConfig config, bool contextColumn)
        {
            if (!tag.HasAttribute("name"))
                throw new Exception("<column> misses required attribute 'name'");

            if (!tag.HasAttribute("const") && !tag.HasAttribute("value") && !tag.HasAttribute("set"))
                throw new Exception("<column> misses either 'const' or 'value' or 'set' attribute."); 

            ImportColumn col;
            if (tag.GetAttributeNode("const") != null)
                col = new ImportColumn(tag.GetAttribute("name"), "'" + tag.GetAttribute("const") + "'");
            else if (tag.GetAttributeNode("value") != null)
            {
                validateExpr(tag.GetAttribute("value"), config, contextColumn); 
                col = new ImportColumn(tag.GetAttribute("name"), tag.GetAttribute("value"), tag.GetAttribute("default"));
            }
            else
                col = new ImportColumn(tag.GetAttribute("name"), tag.GetAttribute("set").Split(',')); 

            col.setMissingMode(ImportColumn.getMissingMode(tag.GetAttribute("missing")));

            return col; 
        }

        private static void validateExpr(string value, ToolboxImportConfig config, bool contextColumn)
        {
            if (String.IsNullOrWhiteSpace(value))
                throw new Exception("Value is null or empty"); 

            value = value.Trim();
            if (value.StartsWith("'") && value.EndsWith("'"))
            {
                if (value.Substring(1, value.Length - 2).IndexOf("'") != -1)
                    throw new Exception("Parse error - constant value contains a quote: " + value);

                return; 
            }

            if (value.StartsWith("$"))
            {
                if (contextColumn && value.Equals("$fieldValue"))
                    return;
 
                if (config.getDataSet().getContext(value.Substring(1)) == null)
                    throw new Exception("Unknown context variable '" + value + "'");

                return; 
            }

            int start = value.IndexOf("(");
            int end = value.IndexOf(")");

            if (start == -1)
            {
                bool first = true; 
                foreach (char c in value)
                {
                    if (!Char.IsLetter(c) && c != '_')
                    {
                        if (first) 
                            throw new Exception("Invalid row identifier '" + value + "'"); 

                        if (!Char.IsNumber(c) && c != '.' && c != ' ')
                            throw new Exception("Invalid row identifier '" + value + "'"); 
                    }
                    
                    first = false; 
                }
                
                return;
            }

            string fn = value.Substring(0, start); 
            if (hasFn(fn))
                return; 

            if (null == config.getMapping(fn))
                throw new Exception("Unknown mapping '" + fn + "'");

            if (!value.EndsWith(")"))
                throw new Exception("Invalid mapping specification - there shouldn't be any content after the closing ')'."); 
        }

        public static bool hasFn(string fn)
        { return fn.Equals("timeToFloat"); } 

        public static string applyFn(string fn, string value)
        {
            if (fn.Equals("timeToFloat"))
            {
                if (value == null)
                    return null; 
                    
                int pos = value.IndexOf(":"); 
                if (pos == -1)
                    return value; 

                try { 
                    Double result = ((Double) Int32.Parse(value.Substring(0, pos))) + (Int32.Parse(value.Substring(pos + 1)) / 60.0); 
                    return "" + result; 
                }
                catch (FormatException)
                { throw new MissingMappingImportException(fn, value); } 
            }
            
            return value; 
        }

        private static bool hasContextVariable(List<DataSet> contexts, string value)
        {
            if (contexts == null || contexts.Count == 0)
                return false;

            foreach (DataSet context in contexts)
            {
                if (context.getContext(value) == null)
                    return false;
            }

            return true; 
        }

        public static ImportContextColumn parseImportContextColumn(XmlElement tag, ToolboxImportConfig config)
        {
            ImportContextColumn col = new ImportContextColumn(tag.GetAttribute("name"));

            col.setMissingMode(ImportColumn.getMissingMode(tag.GetAttribute("missing"))); 
            foreach (XmlNode n in tag.SelectNodes("./column"))
                col.addColumn(parseImportColumn((XmlElement)n, config, true)); 

            return col; 
        }
    }

    public interface DataSet
    {
        string getName(); 

        void addContext(string key, string value);
        void removeContext(string key);
        string getContext(string key);
        Dictionary<string, string> getContext();

        void addDatabase(ConfigDatabase database);
        ConfigDatabase getDatabase(string id);
        List<ConfigDatabase> getDatabases();

        string getInfo(); 
    }

    public class RunContextJoin : DataSet
    {
        private DataSet context;
        private DataSet defaultContext;

        public RunContextJoin(DataSet context, DataSet defaultContext)
        {
            this.context = context;
            this.defaultContext = defaultContext;
        }

        public string getName()
        { return context.getName(); } 

        public string getInfo()
        { return context.getInfo(); } 

        public void addContext(string key, string value)
        { context.addContext(key, value);  }

        public void removeContext(string key)
        { context.removeContext(key); } 

        public string getContext(string key)
        {
            string result = context.getContext(key); 
            if (String.IsNullOrEmpty(result))
                result = defaultContext.getContext(key);

            return result; 
        }

        public Dictionary<string, string> getContext()
        {
            Dictionary<string, string> result = new Dictionary<string, string>();
            foreach (KeyValuePair<string, string> kvp in context.getContext())
                result.Add(kvp.Key, kvp.Value);

            foreach (KeyValuePair<string, string> kvp in defaultContext.getContext())
                if (!result.ContainsKey(kvp.Key))
                    result.Add(kvp.Key, kvp.Value);

            return result; 
        }

        public void addDatabase(ConfigDatabase database)
        { context.addDatabase(database); }

        public ConfigDatabase getDatabase(string id)
        {
            ConfigDatabase result = context.getDatabase(id);
            if (result == null)
                result = defaultContext.getDatabase(id);

            return result;
        }

        public List<ConfigDatabase> getDatabases()
        {
            List<ConfigDatabase> result = new List<ConfigDatabase>();
            foreach (ConfigDatabase db in context.getDatabases())
                result.Add(db);

            foreach (ConfigDatabase db in defaultContext.getDatabases())
                if (!contains(result, db))
                    result.Add(db);

            return result; 
        }

        private bool contains(List<ConfigDatabase> dbs, ConfigDatabase needle)
        {
            foreach (ConfigDatabase db in dbs)
                if (needle.getId().Equals(db.getId()))
                    return true; 

            return false; 
        }
    }

    public class RunContextImpl : DataSet
    {
        private Dictionary<string, string> context = new Dictionary<string, string>(); 
        private List<ConfigDatabase> databases = new List<ConfigDatabase>();
        private string name; 

        public string getName()
        { return name; }

        public void setName(string name)
        { this.name = name; }

        public string getInfo()
        {
            StringBuilder result = new StringBuilder();
            foreach (string key in context.Keys)
            {
                result.Append(" var: "); 
                result.Append(key);
                result.Append("=");
                result.Append(context[key]);
                result.Append("\n"); 
            }

            foreach (ConfigDatabase db in databases)
            {
                result.Append(" db:  "); 
                result.Append(db.getId());
                result.Append("=");
                result.Append(db.getVendor() + ", " + db.getConnection());
                result.Append("\n");
            }

            if (databases.Count > 0)
                    result.Append(". "); 
            return result.ToString(); 
        }

        public void addContext(string key, string value)
        { context[key] = value; }

        public void removeContext(string key)
        { context.Remove(key); } 

        public string getContext(string key)
        { 
            string value = ""; 
            if (context.TryGetValue(key, out value))
                return value; 

            return null;
        }
        
        public Dictionary<string, string> getContext()
        { return context; } 

        public void addDatabase(ConfigDatabase database)
        {
            if (database != null)
                databases.Add(database);
        }

        public ConfigDatabase getDatabase(string id)
        {
            foreach (ConfigDatabase db in databases)
                if (db.getId().Equals(id))
                    return db;

            return null;
        }

        public List<ConfigDatabase> getDatabases()
        { return databases; }
    }

    public class ToolboxImportConfig
    {
        private DataSet runContext = null; 

        private List<ImportConfig> imports = new List<ImportConfig>();
        private List<ImportMapping> mappings = new List<ImportMapping>();

        public void setDataSet(DataSet runContext)
        { this.runContext = runContext; }

        public DataSet getDataSet()
        { return runContext; }

        public void addMapping(ImportMapping mapping)
        {
            if (mapping != null)
                mappings.Add(mapping);
        }

        public void addImport(ImportConfig import)
        {
            if (import != null)
                imports.Add(import);
        }

        public List<ImportMapping> getMappings()
        { return mappings; }

        public ImportMapping getMapping(string id)
        {
            foreach (ImportMapping map in mappings)
                if (map.getId().Equals(id))
                    return map;

            return null;
        }

        public List<ImportConfig> getImports()
        { return imports; }

        public ImportConfig getImport(string name)
        {
            foreach (ImportConfig i in imports)
                if (i.getName().Equals(name))
                    return i;

            return null;
        }
    }

    public class ConfigDatabase
    {
        private string id; 
        private string vendor;
        private string connection;

        public ConfigDatabase(string id, string vendor, string connection)
        {
            this.id = id; 
            this.vendor = vendor;
            this.connection = connection;
        }

        public string getId()
        { return id; } 

        public string getVendor()
        { return vendor; }

        public string getConnection()
        { return connection; } 
    }

    public class ImportConfig
    {
        private List<XmlElement> sqlTags = new List<XmlElement>();
        private List<ImportTable> imports = new List<ImportTable>();
        private List<TableWriter> writers = new List<TableWriter>(); 
        private string name;
        
        public void setName(string name)
        { this.name = name; }

        public string getName()
        { return name; }

        public void addSqlTag(XmlElement sqlTag)
        { sqlTags.Add(sqlTag); }

        public void addImport(ImportTable import)
        { imports.Add(import); }

        public List<ImportTable> getImports()
        { return imports; } 
        
        public List<ImportException> run(ToolboxImportConfig config, bool commitData, TextWriter log, out int importedRows)
        {
            importedRows = 0;

            writers.Clear(); 
            int id = 0; 
            foreach (ImportTable import in imports)
            {
                writers.Add(commitData ? import.getTableWriter() : new NullTableWriter()); 
                id++; 
            }

            try { 
                List<ImportException> errors = new List<ImportException>(); 

                log.WriteLine("Processing import " + getName()); 
                foreach (XmlElement sqlTag in sqlTags)
                {
                    string[] idColumns = null; 
                    if (!String.IsNullOrEmpty(sqlTag.GetAttribute("idColumns")))
                        idColumns = sqlTag.GetAttribute("idColumns").Split(','); 

                    TableReader reader = ImportConfigIO.getTableReader(sqlTag, config.getDataSet());
                    log.WriteLine("Reading from '" + reader.getInfo() + "'");
                    while (reader.hasNextRow())
                    {
                        TableRow row = reader.getNextRow();
                        
                        List<ImportException> newErrors = new List<ImportException>();
                        int tmpCount = 0; 
                        processRow(row, config, commitData, log, newErrors, out tmpCount);
                        importedRows += tmpCount; 
                        foreach (ImportException error in newErrors)
                        {
                            error.setSql(reader.getInfo()); 
                            if (idColumns != null)
                                error.setRowId(getValues(row, idColumns));
                            errors.Add(error);  
                        }
                    }

                }
            
                return errors;
            }
            finally 
            { 
                foreach (TableWriter writer in writers)
                    writer.Dispose(); 
            }
        }

        private String getValues(TableRow row, string[] idColumns)
        {
            StringBuilder result = new StringBuilder();
            foreach (string key in idColumns)
            {
                if (result.Length > 0)
                    result.Append(",");
                result.Append(key);
                result.Append("="); 
                result.Append(row.get(key));
            }

            return result.ToString(); 
        }

        private List<TableRow> readRows(TableReader reader, int maxCount)
        {
            List<TableRow> result = new List<TableRow>();
            while (reader.hasNextRow() && result.Count < maxCount)
                result.Add(reader.getNextRow());

            return result; 
        }

        private void processRow(TableRow row, ToolboxImportConfig config, bool commitData, TextWriter log, List<ImportException> errors, out int importedCount)
        {
            importedCount = 0; 
            for (int i = 0; i < imports.Count; i++)
            {
                ImportTable import = imports[i]; 
                writers[i].writeHeaders(import.getColumnNames());
                foreach (TableRow expandedRow in import.expand(row))
                {
                    foreach (TableRow outRow in import.getResultRows(expandedRow, config, errors))
                    {
                        try { 
                            writers[i].writeRow(outRow);
                            importedCount++; 
                        }
                        catch (Exception ex)
                        { errors.Add(new ExceptionImportException(ex)); } 
                    }
                }
            }
        }
    }

    public class ImportTable
    {
        private ToolboxImportConfig config; 
        private string db;
        private string table;
        private ImportExpandRow expandRow = null; 
        private List<ImportColumn> columns = new List<ImportColumn>();
        private List<ImportContextColumn> contextColumns = new List<ImportContextColumn>();

        public ImportTable(ToolboxImportConfig config, string db, string table)
        {
            this.config = config; 
            this.db = db;
            this.table = table;
        }

        public void setExpandRow(ImportExpandRow expandRow)
        { this.expandRow = expandRow; }

        public ImportExpandRow getExpandRow()
        { return expandRow;  }

        public List<TableRow> expand(TableRow row)
        {
            if (expandRow == null)
            {
                List<TableRow> result = new List<TableRow>();
                result.Add(row); 
                return result; 
            } 
            else 
                return expandRow.expand(row); 
        }

        public TableWriter getTableWriter()
        { 
            ConfigDatabase database = config.getDataSet().getDatabase(db); 
            if (database == null)
                throw new Exception("Failed to find database config: " + db); 

            return new DbTableWriter(database.getVendor(), database.getConnection(), table); 
        }
 
        public string getTable()
        { return table; }

        public string getDb()
        { return db; }

        public void addColumn(ImportColumn col)
        { columns.Add(col); } 

        public void addContextColumn(ImportContextColumn col)
        { contextColumns.Add(col); } 

        public string[] getColumnNames()
        {
            string[] result; 
            if (contextColumns.Count > 0)
                result = new string[columns.Count + contextColumns[0].getColumns().Count]; 
            else
                result = new string[columns.Count]; 

            int index = 0; 
            foreach (ImportColumn col in columns)
            {
                result[index] = col.getName(); 
                index++; 
            }

            if (contextColumns.Count > 0)
            {
                foreach (ImportColumn col in contextColumns[0].getColumns())
                {
                    result[index] = col.getName(); 
                    index++; 
                }
            }

            return result; 
        }
    
        public List<TableRow> getResultRows(TableRow row, ToolboxImportConfig config, List<ImportException> errors)
        {
            List<TableRow> result = new List<TableRow>();

            TableRow baseResult = null;
            try { baseResult = new DictionaryTableRow(getColumnNames(), ImportTable.getValues(row, columns, config, errors)); }
            catch (ImportException)
            { }

            if (contextColumns.Count == 0)
            {
                if (baseResult != null)
                    result.Add(baseResult);
            }
            else
                foreach (ImportContextColumn contextColumn in contextColumns)
                {
                    string fieldValue = row.get(contextColumn.getName());
                    if (String.IsNullOrEmpty(fieldValue))
                    {
                        if (contextColumn.getMissingMode() == ImportColumn.MISSING_MODE.IGNORE_RECORD)
                            continue;

                        if (contextColumn.getMissingMode() == ImportColumn.MISSING_MODE.REPORT_ERROR)
                            throw new MissingValueImportException(contextColumn.getName());
                    }

                    config.getDataSet().addContext("fieldValue", fieldValue);

                    try { 
                        Dictionary<string, string> values = ImportTable.getValues(row, contextColumn.getColumns(), config, errors); 
                        if (baseResult != null)
                            result.Add(new DictionaryTableRow(baseResult, values)); 
                    }
                    catch (ImportException)
                    { }
                    finally
                    { config.getDataSet().removeContext("fieldValue"); }
                }

            return result; 
        }

        public static Dictionary<string, string> getValues(TableRow row, List<ImportColumn> columns, ToolboxImportConfig config, List<ImportException> exceptions)
        {
            ImportException lastException = null; 
            Dictionary<string, string> result = new Dictionary<string, string>();
            foreach (ImportColumn col in columns)
            {
                try { result[col.getName()] = col.getValue(row, config, col.getMissingMode()); }
                catch (ImportException ex)
                {
                    lastException = ex;
                    if (lastException.getColumn() == null)
                        lastException.setColumn(col.getSourceColumn());
                    exceptions.Add(ex); 
                }
            }

            if (lastException != null)
                throw lastException; 

            return result; 
        }
    }

    public class ImportExpandRow
    { 
        private string[] valueHeaders;
        private string[] headers;
        private List<Entry> items = new List<Entry>(); 

        class Entry 
        {
            string[] columns; 
            string[] values; 

            public Entry(string[] columns, string[] values)
            {
                this.columns = columns;
                this.values = values; 
            }
            
            public string[] getValues()
            { return values; }

            public string[] getColumns()
            { return columns; } 
        }

        public ImportExpandRow(string[] valueHeaders, string[] headers)
        {
            this.valueHeaders = valueHeaders;
            this.headers = headers;
        }

        public void addExpand(string[] columns, string[] values)
        {
            if (headers.Length != values.Length)
                throw new Exception("Value count '" + values.Length + "' doesn't match header count '" + headers.Length);
                
            if (columns.Length != valueHeaders.Length)
                throw new Exception("Column count '" + columns.Length + "' doesn't match value header count '" + valueHeaders.Length); 

            items.Add(new Entry(columns, values));   
        }

        public List<TableRow> expand(TableRow row)
        {
            string[] rowHeaders = row.getHeaders();
            string[] rowValues = row.getValues(); 

            string[] newHeaders = new string[rowHeaders.Length + headers.Length + valueHeaders.Length];
            Array.Copy(rowHeaders, newHeaders, rowHeaders.Length);
            Array.Copy(headers, 0, newHeaders, rowHeaders.Length, headers.Length);
            Array.Copy(valueHeaders, 0, newHeaders, rowHeaders.Length + headers.Length, valueHeaders.Length); 

            List<TableRow> result = new List<TableRow>();
            foreach (Entry item in items)
            {
                // Copy original data values. 
                string[] newValues = new string[newHeaders.Length];
                Array.Copy(rowValues, newValues, rowValues.Length);

                // Copy constant values for this item. 
                string[] values = item.getValues(); 
                Array.Copy(values, 0, newValues, rowValues.Length, values.Length);

                // Copy value columns. 
                string[] valueColumns = item.getColumns(); 
                for (int i = 0; i < valueColumns.Length; i++) 
                    newValues[i + rowValues.Length + values.Length] = row.get(valueColumns[i]); 

                result.Add(new StringArrayTableRow(newHeaders, newValues)); 
            }

            return result; 
        }
    }
    

    public class ImportColumn
    {
        public enum MISSING_MODE { USE_NULL, IGNORE_RECORD, REPORT_ERROR };

        public static MISSING_MODE getMissingMode(string value)
        {
            switch (value)
            {
                case "allowNull": 
                    return MISSING_MODE.USE_NULL;

                case "skip": 
                    return MISSING_MODE.IGNORE_RECORD;

                case "error": 
                case "": 
                case null: 
                    return MISSING_MODE.REPORT_ERROR;

                default: 
                    throw new Exception("Unknown value for 'missing' attribute: " + value); 
            }
        }

        private string name;
        private string value;
        private string defaultValue;
        private string[] setFields; 

        private MISSING_MODE missingMode; 

        public ImportColumn(string name, string value)
            : this(name, value, null)
        {}

        public ImportColumn(string name, string value, string defaultValue)
        {
            this.name = name;
            this.value = value;
            this.defaultValue = defaultValue; 
        }
        
        public ImportColumn(string name, string[] setFields)
        {
            this.name = name; 
            this.setFields = setFields; 
        }
        

        public void setMissingMode(MISSING_MODE missingMode)
        { this.missingMode = missingMode; }

        public MISSING_MODE getMissingMode()
        { return missingMode; } 

        public string getName()
        { return name; } 

        public string getValue(TableRow row, ToolboxImportConfig config, MISSING_MODE missing)
        { 
            if (setFields != null)
            {
                string[] values = new string[setFields.Length]; 
                for (int i = 0; i < setFields.Length; i++) 
                {
                    try { values[i] = ImportColumn.getValue(row, setFields[i], config, MISSING_MODE.USE_NULL); }
                    catch (ImportException ex)
                    { 
                        ex.setColumn(setFields[i]); 
                        throw ex; 
                    } 
                }
                    
                Int64 result = 0; 
                for (int i = 0; i < values.Length; i++)
                {
                    result <<= 1; 
                    if (!String.IsNullOrEmpty(values[i]) && !values[i].Equals("0"))
                        result += 1; 
                }
                
                return "" + result; 
            }
            else
                return ImportColumn.getValue(row, value, config, missing);
        }

        static string getValue(TableRow row, string expr, ToolboxImportConfig config, MISSING_MODE missing)
        { return getValue(row, expr, null, config, missing); }
        
        static string getValue(TableRow row, string expr, string defaultValue, ToolboxImportConfig config, MISSING_MODE missing)
        {
            if (!String.IsNullOrEmpty(defaultValue))
                missing = MISSING_MODE.USE_NULL; 
                
            string value = getValueInternal(row, expr, config, missing); 
            if (String.IsNullOrEmpty(value))
                return defaultValue; 

            return value; 
        }
        
        static string getValueInternal(TableRow row, string expr, ToolboxImportConfig config, MISSING_MODE missing)
        {
            // Check if it is a constant value. 
            expr = expr.Trim(); 
            if (expr.StartsWith("'") && expr.EndsWith("'"))
                return expr.Substring(1, expr.Length - 2); 

            int start = expr.IndexOf("(");
            int end = expr.LastIndexOf(")");

            if (start != -1)
            {
                // Map lookup
                string key = getValue(row, expr.Substring(start + 1, end - start - 1), config, missing);
                key = key == null ? null : key.Trim(); 

                string fn = expr.Substring(0, start); 
                if (ImportConfigIO.hasFn(fn))
                    return ImportConfigIO.applyFn(fn, key); 

                key = key == null ? null : key.ToUpper(); 
                    
                ImportMapping map = config.getMapping(fn);
                if (map == null)
                    throw new Exception("Unknown mapping - should have been picked up during config parsing: " + fn);

                if (key != null && map.hasKey(key))
                    return map.getValue(key.ToUpper());
                else
                {
                    if (missing == MISSING_MODE.USE_NULL && key == null)
                        return null;

                    throw new MissingMappingImportException(expr.Substring(0, start), key);
                }
            }
            else if (expr.StartsWith("$"))
                return config.getDataSet().getContext(expr.Substring(1));
            else
            {
                if (-1 == Array.IndexOf(row.getHeaders(), expr))
                    throw new UnknownFieldImportException(expr);

                string value = row.get(expr);
                if (String.IsNullOrEmpty(value))
                {
                    if (missing == MISSING_MODE.USE_NULL)
                        return null;

                    throw new MissingValueImportException(expr);
                }

                return value; 
            }
        }

        public string getSourceColumn()
        {
            if (value != null)
                return getSourceColumn(value); 
            else
                return null; 
        }

        private string getSourceColumn(string value)
        {
            if (value.StartsWith("$"))
                return value;

            if (value.StartsWith("'") && value.EndsWith("'"))
                return value; 

            int start = value.IndexOf("(");
            int end = value.LastIndexOf(")");
            if (start == -1 && end == -1)
                return value;

            return getSourceColumn(value.Substring(start + 1, end - start - 1)); 
        }
    }

    public class ImportException : Exception 
    {
        private string sql;
        private string rowId; 
        private string column;
        private string name;

        public ImportException(string name)
        { this.name = name; } 

        public void setSql(string sql)
        { this.sql = sql; }

        public void setRowId(string rowId)
        { this.rowId = rowId; }

        public void setColumn(string col)
        { this.column = col; }

        public string getSql()
        { return sql; }

        public string getRowId()
        { return rowId; }

        public string getColumn()
        { return column; }

        public string getName()
        { return name; } 

        override public string ToString()
        { return "SQL: " + sql + ", ROW:" + rowId + ((column == null) ? "" : ", COL: " + column); } 
    }

    public class MissingMappingImportException : ImportException
    {
        private string mapping;
        private string value;

        public MissingMappingImportException(string mapping, string value)
            : base("Missing Mapping")
        {
            this.mapping = mapping;
            this.value = value;
        }

        public string getMapping()
        { return mapping;  }

        public string getValue()
        { return value; } 

        override public string ToString()
        { return base.ToString() + ":  Value '" + value + "' not found in mapping '" + mapping + "'."; }
    }

    public class MissingValueImportException : ImportException
    {
        public MissingValueImportException(string column)
            : base("Missing Value")
        { setColumn(column);  }

        override public string ToString()
        { return base.ToString() + ":  No value found."; }
    }

    public class UnknownFieldImportException : ImportException
    {
        public UnknownFieldImportException(string column)
            : base("Unknown Field")
        { setColumn(column);  }

        override public string ToString()
        { return base.ToString() + ":  Unknown column: " + getColumn(); } 
    }

    public class ExceptionImportException : ImportException
    {
        private Exception ex; 

        public ExceptionImportException(Exception ex)
            : base("Exception thrown")
        { this.ex = ex; }

        public override string ToString()
        { return base.ToString() + ": Exception: " + ex.ToString(); }

        public Exception getException()
        { return ex; } 
    }

    public class ImportContextColumn
    {
        private string name;
        private ImportColumn.MISSING_MODE missing; 
        private List<ImportColumn> columns = new List<ImportColumn>(); 

        public ImportContextColumn(string name)
        { this.name = name; }

        public void setMissingMode(ImportColumn.MISSING_MODE mode)
        { this.missing = mode; }

        public ImportColumn.MISSING_MODE getMissingMode()
        { return missing; } 

        public string getName()
        { return name; } 

        public void addColumn(ImportColumn column)
        { columns.Add(column); }

        public List<ImportColumn> getColumns()
        { return columns; } 
    }

    public class ImportMapping
    {
        private string id;
        private Dictionary<string, string> values = new Dictionary<string, string>();
        private Dictionary<string, string> names = new Dictionary<string, string>(); 

        public void setId(string id)
        { this.id = id; }

        public string getId()
        { return id; }

        public bool hasKey(string key)
        {
            string tmp = ""; 
            return names.TryGetValue(key, out tmp); 
        }

        public void add(string key, string value, string name)
        {
            values[key] = value;
            names[key] = name;
        }

        public string getValue(string key)
        { return values[key]; }

        public string getName(string key)
        { return names[key]; } 

        public Dictionary<string, string>.KeyCollection getKeys()
        { return names.Keys; }
    }

/*    
    public interface StatusLog 
    {
        void section(string title); 

        void info(string msg); 
        void ok(string title, string message);
        void warn(string title, string message);
        void error(string title, string message);
        void error(string title, Exception ex);
        
        void reset(); 
    }
    
    public class ConsoleStatusLog : StatusLog
    {
        public void section(string title)
        { Console.Out.WriteLine("=== " + title + " ==="); }

        public void info(string msg)
        { Console.Out.WriteLine("INFO: " + msg); }
        
        public void ok(string title, string message)
        { Console.Out.WriteLine("  OK: " + title + ": " + message); }
        
        public void warn(string title, string message)
        { Console.Out.WriteLine("WARN: " + title + ": " + message); } 
        
        public void error(string title, string message)
        { Console.Out.WriteLine(" ERR: " + title + ": " + message); } 

        public void error(string title, Exception ex)
        { error(title, ex.Message); } 
        
        public void reset()
        {}
    }
    
    public class ToolboxImportProcess
    {
        private ToolboxImportConfig mainConfig;
        private StatusLog statusLog;
        private StatusLog itemsLog;

        public ToolboxImportProcess(string filename, StatusLog statusLog, StatusLog itemsLog)
        {
            this.statusLog = statusLog; 
            this.itemsLog = itemsLog; 
            
            statusLog.reset();
            itemsLog.reset();
           
            try {
                List<ToolboxImportConfig> items = ImportConfigIO.parseToolboxImportConfigs(filename);
                if (items != null && items.Count == 1)
                {
                    mainConfig = items[0]; 
                    statusLog.ok("Config - Open", "Loaded successfully file: " + filename);
                }
                else
                    statusLog.error("Loading Config", "Configuration contains more than one configuration."); 
            }
            catch (Exception ex)
            { statusLog.error("Loading Import Configuration", ex); }
        }

        public ToolboxImportConfig getConfig()
        { return mainConfig; }
        
        public void testDatabase(string dbName)
        {
            ConfigDatabase dbConfig = mainConfig.getDataSet().getDatabase(dbName);
            if (dbConfig == null)
            {
                itemsLog.error(dbName, "DB config not found.");
                return;
            }

            try {
                using (TableReader reader = TableUtil.getTableReader(dbConfig.getVendor(), dbConfig.getConnection(), "SELECT 1"))
                {
                    if (!reader.hasNextRow())
                        throw new Exception("No results returned");
                }

                itemsLog.ok(dbName, "Database ok."); 
            }
            catch (Exception ex)
            { itemsLog.error(dbName, ex); }
        }
        
        public void runImports(bool insertData)
        {
            foreach (ImportConfig importConfig in mainConfig.getImports())
                runImport(importConfig, insertData); 
        }

        public void runImport(ImportConfig importConfig, bool insertData)
        {
            if (importConfig == null)
                return;

            itemsLog.section((insertData ? "Importing " : "Testing ") + importConfig.getName());
            try
            {
                int count = 0; 
                List<ImportException> errors = importConfig.run(mainConfig, insertData, Console.Out, out count);
                if (errors == null || errors.Count == 0)
                    itemsLog.ok(importConfig.getName(), "Import succeeded");
                else
                {
                    foreach (ImportException error in errors)
                    {
                        string msg = ""; 
                        if (error.getColumn() != null)
                            msg += "Col: " + error.getColumn() + ". ";
                        if (error.GetBaseException() != null)
                            msg += error.GetBaseException().Message;

                        itemsLog.error(error.getRowId(), msg);
                    }
                }
            }
            catch (Exception ex)
            { itemsLog.error("Failed to import", ex); }
        }
    }
*/
}
