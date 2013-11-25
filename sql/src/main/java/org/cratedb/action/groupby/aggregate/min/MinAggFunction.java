package org.cratedb.action.groupby.aggregate.min;

import com.google.common.collect.ImmutableSet;
import org.cratedb.action.groupby.aggregate.AggFunction;
import org.cratedb.sql.types.SQLTypes;
import org.cratedb.sql.types.StringSQLType;
import org.cratedb.sql.types.TimeStampSQLType;

import java.util.Set;

public class MinAggFunction extends AggFunction<MinAggState> {

    public static final String NAME = "MIN";
    public static final Set<String> supportedColumnTypes = new ImmutableSet.Builder<String>()
            .addAll(SQLTypes.NUMERIC_TYPES.keySet())
            .add(StringSQLType.NAME)
            .add(TimeStampSQLType.NAME)
            .build();

    @Override
    public void iterate(MinAggState state, Object columnValue) {
        if (state.compareValue(columnValue) == 1) {
            state.value = columnValue;
        }

    }

    @Override
    public MinAggState createAggState() {
        return new MinAggState();
    }

    @Override
    public Set<String> supportedColumnTypes() {
        return supportedColumnTypes;
    }

}
