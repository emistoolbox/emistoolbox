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
    public partial class SelectItemDialog : Form
    {
        public SelectItemDialog(string title)
        {
            InitializeComponent();
            this.Text = title;
        }

        public void add(string item)
        { listBox.Items.Add(item); }

        public List<string> getValues()
        {
            List<string> result = new List<string>();
            for (int i = 0; i < listBox.Items.Count; i++)
            {
                if (listBox.GetSelected(i))
                    result.Add(listBox.Items[i].ToString());
            }

            return result;
        }
    }
}
