using System;
using System.IO;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;
using com.emistoolbox.import;

namespace ImportTool
{
    public partial class LoadForm : Form
    {
        private List<ToolboxImportConfig> g_importConfigs = null; 

        public LoadForm()
        {
            InitializeComponent();
        }

        private void LoadForm_Load(object sender, EventArgs e)
        {}

        private void loadConfigXml()
        {
            fileOpenDialog.Filter = "Import Config|*.xml";
            fileOpenDialog.Title = "Open Import Configuration";

            while (true)
            {
                DialogResult dlgResult = fileOpenDialog.ShowDialog();
                if (DialogResult.OK == dlgResult)
                {
                    Cursor.Current = Cursors.WaitCursor;
                    Application.DoEvents();

                    try
                    {
                        List<ToolboxImportConfig> importConfigs = ImportConfigIO.parseToolboxImportConfigs(fileOpenDialog.FileName);
                        if (importConfigs == null || importConfigs.Count == 0)
                            setError("No imports defined.");
                        else
                            updateTreeView(importConfigs);
                        return;
                    }
                    finally
                    { Cursor.Current = Cursors.Default; }
                }
                else if (DialogResult.Cancel == dlgResult)
                {
                    setStatus("No data loaded.");
                    updateTreeView(null);
                }
            }
        }

        private void updateTreeView(List<ToolboxImportConfig> importConfigs)
        {
            while (treeView.Nodes.Count > 0)
                treeView.Nodes.RemoveAt(0);

            if (importConfigs == null)
            {
                g_importConfigs = new List<ToolboxImportConfig>();
                return;
            }

            g_importConfigs = importConfigs;

            TreeNode root = null;
            TreeNode firstNode = null;
            foreach (ToolboxImportConfig config in importConfigs)
            {
                TreeNode configNode = new TreeNode(config.getDataSet().getName());
                configNode.Tag = config;
                if (root == null)
                {
                    if (firstNode == null)
                        firstNode = configNode;
                    else
                    {
                        root = new TreeNode("(All Datasets)");
                        root.Tag = "*";
                        root.Nodes.Add(firstNode);
                        root.Nodes.Add(configNode);
                        firstNode = null; 
                    }
                }
                else
                    root.Nodes.Add(configNode);

                TreeNode contextNode = new TreeNode("Context");
                Dictionary<string, string> context = config.getDataSet().getContext();
                contextNode.Tag = context;
                if (context.Count > 0)
                    configNode.Nodes.Add(contextNode);

                TreeNode dbNode = new TreeNode("Databases");
                foreach (ConfigDatabase db in config.getDataSet().getDatabases())
                {
                    TreeNode child = new TreeNode(db.getId());
                    child.Tag = db;
                    dbNode.Nodes.Add(child); 
                }
                if (dbNode.Nodes.Count > 0)
                    configNode.Nodes.Add(dbNode); 

                TreeNode mappingNode = new TreeNode("Mappings");
                foreach (ImportMapping mapping in config.getMappings())
                {
                    TreeNode child = new TreeNode(mapping.getId());
                    child.Tag = mapping;
                    mappingNode.Nodes.Add(child); 
                }
                if (mappingNode.Nodes.Count > 0)
                    configNode.Nodes.Add(mappingNode);

                TreeNode importNode = new TreeNode("Imports");
                foreach (ImportConfig import in config.getImports())
                {
                    TreeNode child = new TreeNode(import.getName());
                    child.Tag = import;
                    importNode.Nodes.Add(child); 
                }
                if (importNode.Nodes.Count > 0)
                    configNode.Nodes.Add(importNode); 
            }

            if (firstNode != null)
                treeView.Nodes.Add(firstNode);

            if (root != null)
            {
                if (root.Nodes.Count > 0)
                    treeView.Nodes.Add(root);
                else
                    setError("No datasets defined");
            }
        }

        private void setStatus(string message)
        {
            statusLbl.Text = message;
            statusLbl.ForeColor = Color.DarkGreen; 
        }


        private void setWarning(string message)
        {
            statusLbl.Text = message;
            statusLbl.ForeColor = Color.Orange;
        }

        private void setError(string message)
        {
            statusLbl.Text = message;
            statusLbl.ForeColor = Color.DarkRed;
        }

        private void loadBtn_Click(object sender, EventArgs e)
        { loadConfigXml(); }

        private void treeView_AfterSelect(object sender, TreeViewEventArgs e)
        { updateButtons(getSelectedObject()); }

        private void updateButtons(Object obj)
        {
            if ("*".Equals(obj))
            {
                viewBtn.Text = "Test Import All"; 
                viewBtn.Enabled = true;
                importBtn.Text = "Run Import All";
                importBtn.Visible = true; 
            }
            else if (obj is ToolboxImportConfig || obj is ImportConfig)
            {
                viewBtn.Text = "Test Import";
                viewBtn.Enabled = true;
                importBtn.Text = "Run Import";
                importBtn.Visible = true;
            }
            else if (obj is ConfigDatabase || obj is ImportMapping || obj is Dictionary<string, string>)
            {
                viewBtn.Text = "View";
                viewBtn.Enabled = true;
                importBtn.Visible = false; 
            }
            else
            {
                viewBtn.Text = "View";
                viewBtn.Enabled = false;
                importBtn.Visible = false; 
            }
        }

        private void viewBtn_Click(object sender, EventArgs e)
        {
            Object obj = getSelectedObject();
            if (obj is ImportMapping)
            {
                ImportMapping mapping = (ImportMapping) obj;
                ShowMapDialog mapDlg = new ShowMapDialog("Mapping '" + mapping + "'");
                foreach (string key in mapping.getKeys())
                {
                    string name = mapping.getName(key);
                    if (String.IsNullOrEmpty(name))
                        mapDlg.add(key, mapping.getValue(key));
                    else
                        mapDlg.add(key, mapping.getValue(key), name);
                }

                mapDlg.Show();
            }
            else if (obj is ConfigDatabase)
            {
                ConfigDatabase dbConfig = (ConfigDatabase) obj; 
                ShowMapDialog mapDlg = new ShowMapDialog("Database '" + dbConfig.getId() + "'");
                mapDlg.add("Vendor", dbConfig.getVendor());
                string connection = dbConfig.getConnection();
                if (!String.IsNullOrEmpty(connection))
                {
                    string[] values = connection.Split(';');
                    foreach (string value in values)
                    {
                        int pos = value.IndexOf("=");
                        if (pos == -1)
                            continue;

                        mapDlg.add(value.Substring(0, pos), value.Substring(pos + 1)); 
                    }
                }
                mapDlg.Show(); 
            }
            else if (obj is Dictionary<string, string>)
            {

                ShowMapDialog dlg = new ShowMapDialog("Context");
                dlg.hideInfo();
                foreach (KeyValuePair<string, string> entry in (Dictionary<string, string>)obj)
                    dlg.add(entry.Key, entry.Value);

                dlg.ShowDialog();
            }
            else
            {
                Cursor.Current = Cursors.WaitCursor;
                Application.DoEvents();

                try
                {
                    MemoryExceptionTreeRenderer renderer = getRenderer();

                    if (obj is ToolboxImportConfig)
                        runImport(renderer, (ToolboxImportConfig)obj, null, false);
                    else if (obj is ImportConfig)
                        runImport(renderer, getToolboxImportConfig(), (ImportConfig)obj, false);
                    else if ("*".Equals(obj))
                    {
                        foreach (ToolboxImportConfig config in g_importConfigs)
                            runImport(renderer, config, null, false);
                    }
                    else
                        renderer = null;

                    show(renderer);
                }
                finally
                { Cursor.Current = Cursors.Default; }
            }

        }

        private void importBtn_Click(object sender, EventArgs e)
        {
            MemoryExceptionTreeRenderer renderer = getRenderer(); 
            Object obj = getSelectedObject();
            if ("*".Equals(obj))
            {
                foreach (ToolboxImportConfig config in g_importConfigs)
                    runImport(renderer, config, null, true);
            }
            else if (obj is ToolboxImportConfig)
                runImport(renderer, (ToolboxImportConfig) obj, null, true);
            else if (obj is ImportConfig)
                runImport(renderer, getToolboxImportConfig(), (ImportConfig)obj, true);

            show(renderer); 
        }

        private MemoryExceptionTreeRenderer getRenderer()
        { return new MemoryExceptionTreeRenderer(); }

        private void show(MemoryExceptionTreeRenderer renderer)
        {
            if (renderer == null)
                return;

            HtmlView view = new HtmlView();
            view.setHtml(renderer.getResult());
            view.Show(); 
        }

        private void runImport(ExceptionTreeRenderer renderer, ToolboxImportConfig config, ImportConfig importConfig, bool commit)
        { ToolboxImport.runImport(config, importConfig, renderer, commit); }

        private Object getSelectedObject()
        { return treeView.SelectedNode == null ? null : treeView.SelectedNode.Tag; }

        private ToolboxImportConfig getToolboxImportConfig()
        {
            TreeNode node = treeView.SelectedNode;
            while (node != null)
            {
                if (node.Tag is ToolboxImportConfig)
                    return (ToolboxImportConfig) node.Tag;

                node = node.Parent;
            }

            return null; 
        }
    }

    class MemoryExceptionTreeRenderer : HtmlExceptionTreeRenderer
    {
        public MemoryExceptionTreeRenderer()
            : base(new StringWriter(), false)
        {}

        public String getResult()
        {
            StringWriter writer = (StringWriter) getTextWriter();
            return writer.ToString(); 
        }
    }
}
