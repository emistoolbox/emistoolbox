package com.emistoolbox.client.ui.pdf;

import com.emistoolbox.client.EmisEditor;
import com.emistoolbox.client.Message;
import com.emistoolbox.client.admin.EmisToolbox;
import com.emistoolbox.client.admin.ui.EmisUtils;
import com.emistoolbox.client.admin.ui.ListBoxWithUserObjects;
import com.emistoolbox.client.ui.BlockingScreen;
import com.emistoolbox.client.ui.TextEditor;
import com.emistoolbox.common.model.meta.EmisMeta;
import com.emistoolbox.common.model.meta.EmisMetaEntity;
import com.emistoolbox.common.model.meta.EmisMetaHierarchy;
import com.emistoolbox.common.renderer.pdfreport.PdfContentConfig;
import com.emistoolbox.common.renderer.pdfreport.PdfReportConfig;
import com.emistoolbox.common.renderer.pdfreport.PdfText;
import com.emistoolbox.common.renderer.pdfreport.impl.PdfChartContentConfigImpl;
import com.emistoolbox.common.renderer.pdfreport.impl.PdfGisContentConfigImpl;
import com.emistoolbox.common.renderer.pdfreport.impl.PdfTableContentConfigImpl;
import com.emistoolbox.common.renderer.pdfreport.impl.PdfTextContentConfigImpl;
import com.emistoolbox.common.renderer.pdfreport.impl.PdfVariableContentConfigImpl;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

import java.util.ArrayList;
import java.util.List;

public class PdfReportEditor extends FlexTable implements EmisEditor<PdfReportConfig>
{
    private PdfReportConfig reportConfig;
    private EmisMeta meta;
    private ListBoxWithUserObjects<EmisMetaEntity> uiEntity = new ListBoxWithUserObjects<EmisMetaEntity>();
    private TextBox uiTitle = new TextBox();
    private TextBox uiFooter = new TextBox();
    private ListBox uiSize = new ListBox();
    private ListBoxWithUserObjects<int[]> uiLayout = new ListBoxWithUserObjects<int[]>();
    private ListBox uiOrientation = new ListBox();
    private PushButton uiDelContent;
    private PushButton uiMoveContentUp;
    private PushButton uiMoveContentDown;
    private PushButton uiAddText;
    private PushButton uiAddVariables;
    private PushButton uiEdit;
    private ListBoxWithUserObjects<PdfContentConfig> uiContents = new ListBoxWithUserObjects<PdfContentConfig>();
    private CheckBox uiShortTitles = new CheckBox(Message.messageReport().reportShortTitles()); 

    public PdfReportEditor(EmisMeta meta) 
    {
        this.meta = meta;

        uiEntity.addStyleName(EmisToolbox.CSS_TWO_THIRDS_TEXT);
        uiTitle.addStyleName(EmisToolbox.CSS_FULL_TEXT);
        uiFooter.addStyleName(EmisToolbox.CSS_FULL_TEXT); 
        uiSize.addStyleName(EmisToolbox.CSS_THIRD_TEXT); 
        uiOrientation.addStyleName(EmisToolbox.CSS_THIRD_TEXT); 
        uiLayout.addStyleName(EmisToolbox.CSS_TWO_THIRDS_TEXT); 
        uiContents.addStyleName(EmisToolbox.CSS_FULL_TEXT);
        
        int row = 0;
        for (EmisMetaEntity entity : meta.getEntities())
            this.uiEntity.addItem(entity.getName(), entity);
        uiEntity.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event)
            { updateButtons(); }
        }); 

        setHTML(row, 0, Message.messageAdmin().preHtmlForEntity() + ":"); 
        setWidget(row, 1, this.uiEntity);
        row++;

        setHTML(row, 0, Message.messageAdmin().preHtmlTitle() + ":"); 
        setWidget(row, 1, this.uiTitle);
        row++;

        setHTML(row, 0, Message.messageAdmin().preHtmlFooter() + ":"); 
        setWidget(row, 1, this.uiFooter);
        row++;

        setHTML(row, 0, Message.messageAdmin().preHtmlPageSetup() + ":"); 
        for (PdfReportConfig.PageSize size : PdfReportConfig.PageSize.values())
            this.uiSize.addItem(size.toString());

        for (PdfReportConfig.PageOrientation orientation : PdfReportConfig.PageOrientation.values())
            this.uiOrientation.addItem(EmisUtils.getI18n(orientation.toString()), orientation.toString());

        HorizontalPanel hp = new HorizontalPanel();
        hp.add(this.uiSize);
        hp.add(this.uiOrientation);
        setWidget(row, 1, hp);
        row++;

        this.uiLayout.add(Message.messageAdmin().preLayoutFullPage(), new int[] { 1, 1 });
        this.uiLayout.add(Message.messageAdmin().preLayoutTwoColumns(), new int[] { 1, 2 });
        this.uiLayout.add(Message.messageAdmin().preLayoutTwoRows(), new int[] { 2, 1 });
        this.uiLayout.add(Message.messageAdmin().preLayoutTwoByTwo(), new int[] { 2, 2 });
        this.uiLayout.add(Message.messageAdmin().preLayoutThreeByTwo(), new int[] { 3, 2 });
        this.uiLayout.add(Message.messageAdmin().preLayoutTwoByThree(), new int[] { 2, 3 });
        this.uiLayout.add(Message.messageAdmin().preLayoutFourByThree(), new int[] { 4, 3 });
        this.uiLayout.add(Message.messageAdmin().preLayoutThreeByFour(), new int[] { 3, 4 });
        this.uiLayout.add("Five by Two", new int[] { 2, 5});
        this.uiLayout.add("Two by Five", new int[] { 5, 2});
        this.uiLayout.add("Two by Six", new int[] { 6, 2});

        setHTML(row, 0, Message.messageAdmin().preHtmlLayout() + ":");
        setWidget(row, 1, this.uiLayout);
        row++;

        setWidget(row, 1, this.uiShortTitles); 
        row++; 
        
        getFlexCellFormatter().setVerticalAlignment(row, 0, HasVerticalAlignment.ALIGN_TOP);
        setHTML(row, 0, Message.messageAdmin().preHtmlContents() + ":");
        this.uiContents.setVisibleItemCount(15);
        this.uiContents.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event)
            { PdfReportEditor.this.updateButtons(); }
        });
        hp = new HorizontalPanel();
        hp.setSpacing(2);

        this.uiDelContent = new PushButton(Message.messageAdmin().preBtnDelContent(), new ClickHandler() {
            public void onClick(ClickEvent event)
            {
                if (PdfReportEditor.this.uiContents.getSelectedIndex() == -1)
                    return;

                int index = PdfReportEditor.this.uiContents.getSelectedIndex();
                PdfReportEditor.this.uiContents.removeItem(index);
                index--;
                if (index > PdfReportEditor.this.uiContents.getItemCount() - 1)
                    index = PdfReportEditor.this.uiContents.getItemCount() - 1;
                if (index >= 0)
                {
                    PdfReportEditor.this.uiContents.setSelectedIndex(index);
                }
                PdfReportEditor.this.updateButtons();
            }
        });
        hp.add(EmisUtils.init(this.uiDelContent, 80));

        this.uiMoveContentUp = new PushButton(Message.messageAdmin().preBtnMoveContentUp(), new ClickHandler() {
            public void onClick(ClickEvent event)
            {
                uiContents.moveUp(); 
            	updateButtons(); 
            }
        });
        hp.add(EmisUtils.init(this.uiMoveContentUp, 80));

        this.uiAddText = new PushButton("Add Text", new ClickHandler() { // i18n
            public void onClick(ClickEvent event)
            {
                PdfTextContentConfigImpl content = new PdfTextContentConfigImpl();
                content.setTitle("(title)"); 
                content.setText("(text)");
                uiContents.add(content.getTitle(), content);
                
                editText(uiContents.getItemCount() - 1); 
            }
        });
        hp.add(EmisUtils.init(this.uiAddText, 80));

        this.uiAddVariables = new PushButton("Add Variables", new ClickHandler() { // i18n
            public void onClick(ClickEvent event)
            {
                PdfVariableContentConfigImpl content = new PdfVariableContentConfigImpl();
                content.setTitle("(title)"); 
                content.setEntity(uiEntity.getUserObject()); 
                uiContents.add(content.getInfo(), content);
                
                editVariables(uiContents.getItemCount() - 1); 
            }
        });
        hp.add(EmisUtils.init(this.uiAddVariables, 100));

        this.uiEdit = new PushButton("Edit", new ClickHandler() { // i18n
            public void onClick(ClickEvent event)
            {
                if (uiContents.getUserObject() instanceof PdfTextContentConfigImpl)
                    editText(uiContents.getSelectedIndex()); 
                else if (uiContents.getUserObject() instanceof PdfVariableContentConfigImpl)
                    editVariables(uiContents.getSelectedIndex()); 
            } 
        });
        hp.add(EmisUtils.init(this.uiEdit, 80));

        this.uiMoveContentDown = new PushButton(Message.messageAdmin().preBtnMoveContentDown(), new ClickHandler() {
            public void onClick(ClickEvent event)
            { 
            	uiContents.moveDown();
            	updateButtons(); 
            }
        });
        hp.add(EmisUtils.init(this.uiMoveContentDown, 80));

        VerticalPanel vp = new VerticalPanel();
        vp.setSpacing(2);
        vp.add(new Label(Message.messageAdmin().preHtmlAddContentMessage()));
        vp.add(this.uiContents);
        vp.add(hp);
        setWidget(row, 1, vp);
        row++;

        for (int i = 0; i < row; i++) 
        	getFlexCellFormatter().addStyleName(i, 0, EmisToolbox.CSS_SECTION); 

        updateButtons();
    }

    private void editText(final int index)
    {
        final PdfContentConfig content = uiContents.getUserObject(index);
        if (content instanceof PdfTextContentConfigImpl)
        {
            final PdfTextContentConfigImpl textContent = (PdfTextContentConfigImpl) content; 
            final TextEditor editor = new TextEditor("Please enter text for report:", textContent.getTitle() == null ? "" : textContent.getTitle(), textContent.getText()); 
            final BlockingScreen block = new BlockingScreen(editor); 
            editor.addValueChangeHandler(new ValueChangeHandler<String>() {
                public void onValueChange(ValueChangeEvent<String> event)
                {
                    textContent.setText(event.getValue()); 
                    textContent.setTitle(editor.getTitle()); 
                    block.finished();
                    
                    uiContents.updateText(index, textContent.getInfo()); 
                }
            });
        }
    }
    
    private void editVariables(final int index)
    {
        final PdfContentConfig content = uiContents.getUserObject(index);
        if (content instanceof PdfVariableContentConfigImpl)
        {
            final PdfVariableContentConfigImpl varContent = (PdfVariableContentConfigImpl) content; 
            final VariableEditor editor = new VariableEditor(varContent.getEntityType());
            editor.set(varContent); 
            final BlockingScreen block = new BlockingScreen(editor); 
            editor.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
               public void onValueChange(ValueChangeEvent<Boolean> event)
                {
                   block.finished(); 
                   if (event.getValue())
                   {
                       editor.update(varContent); 
                       uiContents.updateText(index,  varContent.getInfo()); 
                   }
                }
            }); 
        }
    }

    public void updateButtons()
    {
        int index = this.uiContents.getSelectedIndex();
        this.uiMoveContentUp.setEnabled(index >= 1);
        this.uiMoveContentDown.setEnabled((index != -1) && (index < this.uiContents.getItemCount() - 1));
        this.uiEdit.setEnabled(index != -1 && (uiContents.getUserObject() instanceof PdfTextContentConfigImpl || uiContents.getUserObject() instanceof PdfVariableContentConfigImpl));
        this.uiAddVariables.setEnabled(uiEntity.getUserObject() != null); 
    }

    public void commit()
    {
        if (this.reportConfig == null)
        {
            return;
        }
        this.reportConfig.setEntityType((EmisMetaEntity) this.uiEntity.getUserObject());
        this.reportConfig.putText(PdfText.TEXT_TITLE, this.uiTitle.getText()); 
        this.reportConfig.putText(PdfText.TEXT_FOOTER, uiFooter.getText());
        
        this.reportConfig.setPage(PdfReportConfig.PageSize.values()[this.uiSize.getSelectedIndex()], PdfReportConfig.PageOrientation.values()[this.uiOrientation.getSelectedIndex()]);

        int[] layout = (int[]) this.uiLayout.getUserObject();
        if (layout == null)
            layout = new int[] { 1, 1 };

        this.reportConfig.setLayout(layout[0], layout[1]);
        this.reportConfig.setShortTitles(uiShortTitles.getValue()); 

        List<PdfContentConfig> contents = new ArrayList<PdfContentConfig>();
        for (int i = 0; i < this.uiContents.getItemCount(); i++)
            contents.add(this.uiContents.getUserObject(i));
        this.reportConfig.setContentConfigs(contents);
    }

    public PdfReportConfig get()
    {
        commit();
        return this.reportConfig;
    }

    public void set(PdfReportConfig config)
    {
        this.reportConfig = config;

        EmisMetaEntity entityType = config.getSeniorEntity();
        EmisMetaHierarchy hierarchy = config.getHierarchy();

        this.uiEntity.clear();
        for (EmisMetaEntity entity : this.meta.getEntities())
        {
            if ((entityType == null) || (hierarchy == null) || (!entity.isChildOf(entityType, hierarchy)))
            {
                this.uiEntity.addItem(entity.getName(), entity);
            }
        }
        this.uiEntity.setValue(config.getEntityType());
        this.uiTitle.setText(config.getText(PdfText.TEXT_TITLE));
        this.uiFooter.setText(config.getText(PdfText.TEXT_FOOTER));
        this.uiOrientation.setSelectedIndex(config.getOrientation().ordinal());
        this.uiSize.setSelectedIndex(config.getPageSize().ordinal());

        this.uiLayout.setSelectedIndex(getLayoutIndex(config.getRows(), config.getColumns()));
        this.uiShortTitles.setValue(config.hasShortTitles());

        this.uiContents.clear();
        for (PdfContentConfig content : config.getContentConfigs())
        {
            this.uiContents.addItem(getPrefix(content) + content.getInfo(), content);
        }
        if (this.uiContents.getItemCount() > 0)
        {
            this.uiContents.setSelectedIndex(0);
        }
        updateButtons();
    }

    private int getLayoutIndex(int row, int col)
    {
        for (int i = 0; i < this.uiLayout.getItemCount(); i++)
        {
            int[] layout = (int[]) this.uiLayout.getUserObject(i);
            if ((layout[0] == row) && (layout[1] == col))
            {
                return i;
            }
        }
        return 0;
    }

    private String getPrefix(PdfContentConfig contentConfig)
    {
        if ((contentConfig instanceof PdfGisContentConfigImpl))
            return Message.messageAdmin().preHtmlMap() + ": ";
        if ((contentConfig instanceof PdfTableContentConfigImpl))
            return Message.messageAdmin().preHtmlTable() + ": ";
        if ((contentConfig instanceof PdfChartContentConfigImpl))
        {
            switch (((PdfChartContentConfigImpl) contentConfig).getChartType()) {
            case 0:
                return Message.messageAdmin().mreHtmlBarGraphs() + ": ";
            case 1:
                return Message.messageAdmin().mreHtmlStackedBarChsrt() + ": ";
            case 2:
                return Message.messageAdmin().mreHtmlPieChart() + ": ";
            }
        }
        if ((contentConfig instanceof PdfTextContentConfigImpl))
            return "TEXT: "; // i18n

        return "";
    }
}
