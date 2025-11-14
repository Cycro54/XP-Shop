package invoker54.xpshop.common.data.sections;

import invoker54.xpshop.common.data.BasicData;

public abstract class Section extends BasicData {

    @Override
    public String getGeneralType() {
        return Section.class.getSimpleName();
    }
}
