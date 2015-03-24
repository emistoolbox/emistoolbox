package com.emistoolbox.client.admin.ui.mapping;

import com.google.gwt.user.client.ui.HTML;

class HTMLWithId extends HTML
{
    private String id;

    public HTMLWithId(String html, String id) {
        super(html);
        this.id = id;
    }

    public String getId()
    {
        return this.id;
    }
}
