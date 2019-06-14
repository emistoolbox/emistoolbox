package com.emistoolbox.client.ui;

import com.emistoolbox.client.admin.EmisToolbox;
import com.emistoolbox.client.admin.ui.EmisUtils;
import com.emistoolbox.common.model.priolist.PriorityListItem;
import com.emistoolbox.common.results.MetaResultValue;
import com.emistoolbox.common.results.PriorityMetaResult;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.Widget;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PriorityListTable extends FlexTable implements Comparator<PriorityListItem>
{
    private int PAGE_SIZE = 50;
    private int currentIndex = 0;
    private int[] orderIndexes;
    private int[] sortOrder;
    private PriorityMetaResult metaResult;
    private List<MetaResultValue> values;
    private List<PriorityListItem> entries;
    private PushButton uiNextButton = new PushButton("Next >");
    private PushButton uiPrevButton = new PushButton("< Prev");

    private Image uiExportCSV = new Image("css/icon_csv.gif");
    private Image uiExportExcel = new Image("css/icon_xls.gif");

    public PriorityListTable(final EmisToolbox toolbox, PriorityMetaResult metaResult, List<PriorityListItem> prioList) {
        this.metaResult = metaResult;
        this.entries = prioList;
        this.values = metaResult.getMetaResultValues();

        EmisUtils.init(this.uiNextButton, 60);
        EmisUtils.init(this.uiPrevButton, 60);

        this.uiExportCSV.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event)
            {
                PriorityListTable.this.export(toolbox, ".csv", PriorityListTable.this.uiExportCSV);
            }
        });
        this.uiExportCSV.setTitle("Export to CSV Format.");

        this.uiExportExcel.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event)
            {
                PriorityListTable.this.export(toolbox, ".xls", PriorityListTable.this.uiExportExcel);
            }
        });
        this.uiExportExcel.setTitle("Export to MS Excel format.");

        addStyleName("priolist");

        if ((this.values == null) || (this.values.size() == 0))
        {
            setText(0, 0, "Priority list without any data");
            return;
        }

        this.uiNextButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event)
            {
                currentIndex += PAGE_SIZE;
                if (currentIndex + PAGE_SIZE >= entries.size())
                    currentIndex = entries.size() - PAGE_SIZE;

                updateUi();
            }
        });

        this.uiPrevButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event)
            {
                currentIndex -= PAGE_SIZE;
                if (currentIndex < 0)
                    currentIndex = 0;

                updateUi();
            }
        });
        this.orderIndexes = new int[this.values.size()];
        for (int i = 0; i < this.orderIndexes.length; i++)
        {
            this.orderIndexes[i] = i;
        }
        this.sortOrder = new int[this.values.size()];
        for (int i = 0; i < this.sortOrder.length; i++)
        {
            this.sortOrder[i] = -1;
        }
        Collections.sort(prioList, this);
        updateUi();
    }

    public int compare(PriorityListItem item1, PriorityListItem item2)
    {
        double[] values1 = item1.getValues();
        double[] values2 = item2.getValues();

        for (int i = 0; i < this.orderIndexes.length; i++)
        {
            int index = Math.abs(this.orderIndexes[i]);
            if ((Double.isNaN(values1[index])) && (Double.isNaN(values2[index])))
                return 0;
            if (Double.isNaN(values1[index]))
                return -1 * this.sortOrder[index];
            if (Double.isNaN(values2[index]))
                return this.sortOrder[index];

            int result = this.sortOrder[index] * Double.compare(values1[index], values2[index]);
            if (result != 0)
            {
                return result;
            }
        }
        return 0;
    }

    private void updateUi()
    {
        updateHeaders();
        updateContent();

        int row = getRowCount();
        getFlexCellFormatter().setColSpan(row, 0, 2);
        getFlexCellFormatter().setHorizontalAlignment(row, 0, HasHorizontalAlignment.ALIGN_LEFT);
        getFlexCellFormatter().setColSpan(row, 2, this.values.size());
        getFlexCellFormatter().setHorizontalAlignment(row, 2, HasHorizontalAlignment.ALIGN_RIGHT);

        setWidget(row, 0, this.uiPrevButton);
        setWidget(row, 2, this.uiNextButton);

        int maxIndex = this.entries.size();
        this.uiPrevButton.setEnabled(this.currentIndex > 0);
        this.uiNextButton.setEnabled(this.currentIndex + this.PAGE_SIZE < maxIndex);

        row++;
        HorizontalPanel hp = new HorizontalPanel();
        hp.setSpacing(3);
        hp.add(this.uiExportCSV);
        hp.add(this.uiExportExcel);
        getFlexCellFormatter().setHorizontalAlignment(row, 0, HasHorizontalAlignment.ALIGN_RIGHT);
        getFlexCellFormatter().setColSpan(row, 0, this.orderIndexes.length + 3);
        setWidget(row, 0, hp);
    }

    private void export(EmisToolbox toolbox, String extension, Widget anchor)
    {
        DownloadPanel download = new DownloadPanel(new Label("Please wait while the file with the priority list is prepared."), "Priority List", anchor);
        toolbox.getService().savePriorityList(this.metaResult, this.entries, extension, download.getDownloadCallback(null));
    }

    private void updateHeaders()
    {
        clear();
        setText(0, 0, "ID");
        
        String[] fields = metaResult.getAdditionalFields(); 
        for (int i = 0; i < fields.length; i++)
        {
        	if (fields[i].equals("name"))
        		setText(0, i + 1, "Name");
        	else
        		setText(0, i + 1, fields[i]);
        }
        
        int col = fields.length + 1;
        for (int i = 0; i < this.orderIndexes.length; i++)
        {
            MetaResultValue value = (MetaResultValue) this.values.get(this.orderIndexes[i]);

            HorizontalPanel hp = new HorizontalPanel();
            hp.setSpacing(2);
            Image img = new Image(this.sortOrder[i] == 1 ? "css/down.png" : "css/up.png");
            final int pos = i;
            ClickHandler handler = new ClickHandler() {
                public void onClick(ClickEvent event)
                {
                    PriorityListTable.this.sortOrder[pos] *= -1;
                    Collections.sort(PriorityListTable.this.entries, PriorityListTable.this);

                    PriorityListTable.this.updateUi();
                }
            };
            img.addClickHandler(handler);

            Label label = new Label(value.getName(false));
            label.addClickHandler(handler);

            hp.add(label);
            hp.add(img);

            setWidget(0, col, hp);
            col++;
        }

        getRowFormatter().addStyleName(0, "prioHeader");
    }

    private void updateContent()
    {
        while (getRowCount() > 1)
            removeRow(1);

        int maxIndex = Math.min(this.PAGE_SIZE, this.entries.size() - this.currentIndex);
        for (int i = 0; i < maxIndex; i++)
            update(i + 1, (PriorityListItem) this.entries.get(i + this.currentIndex));
    }

    private void update(int row, PriorityListItem item)
    {
        setText(row, 0, "" + item.getId());
        int col = 1; 
        for (String value : item.getEntityValues())
        {
        	setText(row, col, value == null ? "" : value); 
        	col++; 
        }

        double[] values = item.getValues();
        for (int i = 0; i < this.orderIndexes.length; i++)
        {
            if (Double.isNaN(values[this.orderIndexes[i]]))
                setText(row, col, "");
            else
            {
                String format = metaResult.getMetaResultValue(i).getFormat();
                setText(row, col, EmisUtils.getFormattedValue(format, values[this.orderIndexes[i]]));
            }
            
            getCellFormatter().setHorizontalAlignment(row, col, HasHorizontalAlignment.ALIGN_RIGHT);
            col++; 
        }
    }
}
