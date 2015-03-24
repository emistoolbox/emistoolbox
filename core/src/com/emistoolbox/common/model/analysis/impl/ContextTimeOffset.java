package com.emistoolbox.common.model.analysis.impl;

import com.emistoolbox.common.model.EmisEnumTupleValue;
import com.emistoolbox.common.model.analysis.EmisContext;
import com.emistoolbox.common.model.impl.EnumTupleValueImpl;
import com.emistoolbox.common.model.meta.EmisMetaDateEnum;
import com.emistoolbox.common.model.meta.EmisMetaEnum;
import com.emistoolbox.common.model.meta.EmisMetaEnumTuple;
import java.util.ArrayList;
import java.util.List;

public class ContextTimeOffset extends ContextAdaptor
{
    private int offset;

    public ContextTimeOffset(EmisContext context, int offset) {
        super(context);
        this.offset = offset;
    }

    public List<EmisEnumTupleValue> getDates()
    {
        return adapt(super.getDates());
    }

    public List<EmisEnumTupleValue> getDates(EmisMetaDateEnum dateEnum)
    {
        return adapt(super.getDates(dateEnum));
    }

    private List<EmisEnumTupleValue> adapt(List<EmisEnumTupleValue> tuples)
    {
        List result = new ArrayList();
        for (EmisEnumTupleValue tuple : tuples)
        {
            EmisEnumTupleValue newTuple = new EnumTupleValueImpl();
            EmisMetaEnumTuple tupleType = tuple.getEnumTuple();
            newTuple.setEnumTuple(tupleType);
            byte[] indexes = tuple.getIndex();
            indexes[0] = offset(indexes[0], tupleType.getEnums()[0]);
            if (indexes[0] == -1)
                continue;
            newTuple.setIndex(indexes);

            result.add(newTuple);
        }

        return result;
    }

    private byte offset(byte value, EmisMetaEnum enumType)
    {
        if (value + this.offset < 0)
        {
            return -1;
        }
        if (value + this.offset >= enumType.getSize())
        {
            return -1;
        }
        return (byte) (value + this.offset);
    }
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name: com.emistoolbox.common.model.analysis.impl.ContextTimeOffset
 * JD-Core Version: 0.6.0
 */