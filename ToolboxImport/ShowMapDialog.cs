using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;

namespace com.emistoolbox.import
{
    public partial class ShowMapDialog : Form
    {
        public ShowMapDialog(string title)
        {
            InitializeComponent();
            Text = title;
        }

        public void add(string key, string value)
        { add(key, value, null); } 

        public void add(string key, string value, string name)
        {
            DataGridViewRow newRow = new DataGridViewRow();

            DataGridViewTextBoxCell newCell = new DataGridViewTextBoxCell();
            newCell.Value = key;
            newRow.Cells.Add(newCell);

            newCell = new DataGridViewTextBoxCell();
            newCell.Value = value;
            newRow.Cells.Add(newCell);

            if (name != null)
            {
                newCell = new DataGridViewTextBoxCell();
                newCell.Value = name;
                newRow.Cells.Add(newCell);
            }

            dataGrid.Rows.Add(newRow);
        }

        public void hideInfo()
        { Info.Visible = false; }
    }
}
