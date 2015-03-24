using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;

namespace ImportTool
{
    public partial class HtmlView : Form
    {
        public HtmlView()
        { InitializeComponent(); }

        private void menuSave_Click(object sender, EventArgs e)
        {
            saveDialog.Filter = "HTML Document|*.html";
            if (DialogResult.OK == saveDialog.ShowDialog())
                System.IO.File.WriteAllText(saveDialog.FileName, htmlCtrl.DocumentText);
        }

        public void setHtml(string html)
        { htmlCtrl.DocumentText = html; }
    }
}
