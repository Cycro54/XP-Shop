package invoker54.xpshop.client.widgets;

import invoker54.invocore.client.util.InvoZone;
import invoker54.xpshop.client.screens.InvoScreen;
import invoker54.xpshop.client.widgets.buttons.ToggleButton;

import java.util.ArrayList;
import java.util.function.Consumer;

public class ListGroup extends InvoList.InvoListEntry{

    public static final int height = 20;
    public static final int padding = 2;
    protected final ToggleButton groupButton;

    public ListGroup(InvoScreen screen, InvoZone widgetZone, InvoList parent, ToggleButton groupButton) {
        super(screen, widgetZone, parent, getResizer(), new ArrayList<>());
        this.groupButton = groupButton;

    }

    @Override
    public InvoZone getZoneCopy() {
        if (!this.groupButton.isToggled()) return this.groupButton.getZoneCopy();
        InvoZone mergeZone = this.groupButton.getZoneCopy();
        this.entryList.forEach(widget -> mergeZone.merge(widget.getZoneCopy()));
        return mergeZone;
    }

    public static Consumer<InvoList.InvoListEntry> getResizer(){
        return (entry -> {
            ListGroup group = ((ListGroup)entry);
            group.groupButton.setZone(group.getZoneCopy().setHeight(height));

            InvoZone groupButtonZone = group.groupButton.getZoneCopy();
            InvoZone previousZone = group.groupButton.getZoneCopy();
            for (InvoWidget widget : group.entryList){
                widget.setZone(widget.getZoneCopy().setWidth(groupButtonZone.width()).setX(groupButtonZone.x()).
                        setY(previousZone.down() + padding).inflate(-padding,  0));

                previousZone = widget.getZoneCopy();
            }
        });
    }


}
