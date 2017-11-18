package mc.structspawn.manager;

import net.minecraft.block.state.IBlockState;

import java.util.HashMap;
import java.util.Map;


public class BlockPalette {
    public static final BlockPalette DEFAULT = new BlockPalette("STRUCTGENLIB_DEFAULT_PALETTE") {
        @Override
        public void addTransform(IBlockState original, IBlockState replacement) {
            throw new UnsupportedOperationException("Cannot add Transforms to the default BlockPalette");
        }
    };

    private String name;
    private Map<IBlockState,IBlockState> transforms=new HashMap<>();

    public BlockPalette(String name) {
        this.name = name;
    }

    public void addTransform(IBlockState original, IBlockState replacement) {
        transforms.put(original,replacement);
    }

    public IBlockState transform(IBlockState original) {
        return transforms.getOrDefault(original,original);
    }

    public String getName() {
        return name;
    }
}
