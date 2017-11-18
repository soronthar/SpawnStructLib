package mc.structspawn.manager;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import mc.structspawn.StructSpawnLib;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.ObjectUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class StructurePackManager {
    //TODO: register packs from config directory
    private List<String> registeredStructurePacks = new ArrayList<>();
    private Map<String, StructureInfo> structureMap = new HashMap<>();
    private Map<String, BlockPalette> paletteMap = new HashMap<>();

    public void addStructurePack(String structurePackName) {
        registeredStructurePacks.add(structurePackName);
    }

    public void initRegisteredPacks() {
        for (String registeredPack : registeredStructurePacks) {
            loadStructurePack(registeredPack);
        }
    }

    public void loadStructurePack(String structurePack) {
        JsonObject structurePackContent = readFile(structurePack);
        if (structurePackContent != null) {
            loadStructures(structurePackContent);
            loadPalette(structurePack,structurePackContent);
        }
    }

    private void loadStructures(JsonObject structurePackContent) {
        JsonArray structures = structurePackContent.getAsJsonArray("structures");
        if (structures != null) {
            for (JsonElement structureElement : structures) {
                JsonObject structureObject = structureElement.getAsJsonObject();
                StructureInfo info = createStructureInfo(structureObject);
                //TODO: Handle duplicates
                structureMap.put(info.getName(), info);
            }
        }
    }

    private StructureInfo createStructureInfo(JsonObject structureObject) {
        String name = structureObject.get("name").getAsString(); //TODO: Null name is an error
        //TODO: structure variations
        //TODO: Additional structure configurations (like.. conditions)
        JsonArray structure = structureObject.get("structure").getAsJsonArray(); //TODO: structure == null is an error
        String structurePath = structure.get(0).getAsString();

        JsonElement lootTablesElement = structureObject.get("lootTables");
        String lootTableName=null; //TODO: nultiple loot tables, with weights
        if (lootTablesElement!=null) {
            lootTableName = lootTablesElement.getAsJsonArray().get(0).getAsString();

        }

        //TODO: Palette Reference
        //TODO: weighted palette
        //TODO: multiple palette
        JsonElement paletteElement = structureObject.get("palette");
        BlockPalette palette = BlockPalette.DEFAULT;
        if (paletteElement != null) {
            JsonArray asJsonArray = paletteElement.getAsJsonArray();
            for (JsonElement next : asJsonArray) { //there will be only one.. of there are more, the last one wins.
                palette = createPalette(name, next.getAsJsonObject());
            }
        }

        return new StructureInfo(name, new ResourceLocation(structurePath), palette, lootTableName!=null?new ResourceLocation(lootTableName):null);
    }


    private void loadPalette(String structurePack, JsonObject structurePackContent) {
        JsonArray palettes = structurePackContent.getAsJsonArray("palettes");
        if (palettes != null) {
            for (JsonElement paletteElements : palettes) {
                JsonObject paletteObject = paletteElements.getAsJsonObject();
                BlockPalette palette= createPalette(structurePack,paletteObject);
                //TODO: Handle duplicates
                paletteMap.put(palette.getName(), palette);
            }
        }
    }

    private BlockPalette createPalette(String paletteSource, JsonObject paletteObject) {
        //TODO: weighted palette
        //TODO: missing name is an error
        BlockPalette palette = new BlockPalette(paletteObject.get("name").getAsString());

        JsonObject transformObject = paletteObject.get("transform").getAsJsonObject();
        for (Map.Entry<String, JsonElement> next : transformObject.entrySet()) {
            String originalBlock = next.getKey();
            String transformedBlock = next.getValue().getAsString();

            IBlockState originalState = getBlockState(originalBlock);
            IBlockState transformedState = getBlockState(transformedBlock);

            if (originalState == null) {
                StructSpawnLib.logger.error("Error reading palette %s:%s : Unrecognized Original Block %s", paletteSource,palette.getName(), originalBlock);
            }
            if (transformedState == null) {
                StructSpawnLib.logger.error("Error reading palette %s:%s palette: Unrecognized Transformed Block %s", paletteSource, palette.getName(), transformedBlock);
            }

            if (originalState !=null && transformedState != null) {
                palette.addTransform(originalState, transformedState);
            }
        }

        return palette;
    }


    private JsonObject readFile(String structurePack) {
        structurePack=structurePack.replace(':','/');
        try (
                InputStream inputstream = StructSpawn.class.getResourceAsStream("/assets/" + structurePack + ".json");
                BufferedReader br = new BufferedReader(new InputStreamReader(inputstream, "UTF-8"));
        ) {
            JsonParser parser = new JsonParser();
            JsonElement element = parser.parse(br);
            return element.getAsJsonObject();
        } catch (IOException|NullPointerException e) {
            StructSpawnLib.logger.error("Error reading " + structurePack, e);
            return null;
        }
    }

    private static IBlockState getBlockState(String blockName) {
        String[] split = blockName.split("@");
        Block block = Block.getBlockFromName(split[0]);
        if (block == null) return null;

        IBlockState state;
        if (split.length > 1) {
            state = block.getStateFromMeta(Integer.parseInt(split[1]));
        } else {
            state = block.getDefaultState();
        }
        return state;
    }

    public StructureInfo getStructureInfo(String structureName) {
        return structureMap.get(structureName);
    }

    public BlockPalette getBlockPalette(String paletteName) {
        return paletteMap.get(paletteName);
    }

}
