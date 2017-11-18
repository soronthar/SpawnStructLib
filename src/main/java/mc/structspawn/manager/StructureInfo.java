package mc.structspawn.manager;

import net.minecraft.util.ResourceLocation;

import java.util.Optional;

class StructureInfo {
    private String name;
    private BlockPalette palette;
    private ResourceLocation structureLocation;
    private ResourceLocation lootTable;

    public StructureInfo(String name, ResourceLocation structureLocation) {
        this(name, structureLocation, BlockPalette.DEFAULT,null);
    }

    public StructureInfo(String name, ResourceLocation structureLocation, BlockPalette palette, ResourceLocation lootTable) {
        this.structureLocation = structureLocation;
        this.palette = palette;
        this.name = name;
        this.lootTable = lootTable;
    }

    public StructureInfo(ResourceLocation structureLocation) {
        this(structureLocation.getResourcePath(), structureLocation,BlockPalette.DEFAULT,null);
    }

    public ResourceLocation getStructureLocation() {
        return structureLocation;
    }

    public BlockPalette getPalette() {
        return palette;
    }

    public Optional<ResourceLocation> getLootTable() {
        return Optional.ofNullable(lootTable);
    }

    public String getName() {
        return name;
    }
}
