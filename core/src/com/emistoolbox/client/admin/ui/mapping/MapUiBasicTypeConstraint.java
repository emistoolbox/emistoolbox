package com.emistoolbox.client.admin.ui.mapping;

import com.emistoolbox.common.model.meta.EmisMetaData;
import com.emistoolbox.common.model.meta.EmisMetaData.EmisDataType;
import java.util.ArrayList;
import java.util.List;

public class MapUiBasicTypeConstraint implements MapUiConstraint
{

    private EmisMetaData.EmisDataType type;

    public MapUiBasicTypeConstraint(EmisMetaData.EmisDataType type) {
        this.type = type;
    }

    public List<String> getValues()
    {
        List result = new ArrayList();
        switch (type) {
        case BOOLEAN:
            result.add("true");
            result.add("false");
            return result;
        }

        return null;
    }

    public boolean isValid(String value)
    {
        try
        {
            switch (type) {
            case BYTE:
                Integer.parseInt(value);
                break;
            case INTEGER:
                Byte.parseByte(value);
                break;
            case BOOLEAN:
                Boolean.parseBoolean(value);
            }

            return true;
        }
        catch (Throwable err)
        {
        }
        return false;
    }
}
