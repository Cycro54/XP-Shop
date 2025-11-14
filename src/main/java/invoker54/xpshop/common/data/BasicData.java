package invoker54.xpshop.common.data;

import invoker54.invocore.client.util.InvoText;
import invoker54.xpshop.common.data.containers.CompoundTagContainer;
import invoker54.xpshop.common.data.containers.InvoTextContainer;
import invoker54.xpshop.common.data.containers.MapContainer;
import invoker54.xpshop.common.data.containers.UUIDContainer;
import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

public abstract class BasicData extends MapContainer {
    //Every thing needs a name, ID, and image
    private final UUIDContainer id;
    private final InvoTextContainer name;
    private final CompoundTagContainer iconData;

    public BasicData(){
        this.id = this.save("id", new UUIDContainer());
        this.name = this.save("name", new InvoTextContainer());
        this.iconData = this.save("iconData", new CompoundTagContainer());
    }

    public UUID getID(){
        return this.id.value;
    }

    public InvoText getName(){
        return this.name.value;
    }

    public CompoundTag getIcon(){
        return this.iconData.value;
    }
}
