package mc.structspawn;

import com.google.common.collect.ImmutableList;
import mc.structspawn.command.RegenChunkCommand;
import mc.structspawn.command.SpawnStructureCommand;
import mc.structspawn.manager.StructurePackManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.apache.logging.log4j.Logger;

import static mc.structspawn.StructSpawnLibInfo.MODID;
import static mc.structspawn.StructSpawnLibInfo.MODVERSION;


@Mod(
        modid = MODID,
        version = MODVERSION,
        useMetadata = true
)
public class StructSpawnLib {
// TODO: reload structure info
//TODO: sgen:raw command, spawn local or mod structures without structureInfo

    @Mod.Instance
    public static StructSpawnLib instance;

    public static Logger logger;

    private StructurePackManager structurePackManager =new StructurePackManager();

    public StructurePackManager getStructurePackManager() {
        return structurePackManager;
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        //Here to guarantee the mechanism is working
        FMLInterModComms.sendMessage(MODID,"registerStructurePack","default_structure_pack");
    }


    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent e) {
        structurePackManager.initRegisteredPacks();
    }

    @Mod.EventHandler
    public void serverLoad(FMLServerStartingEvent event) {
        event.registerServerCommand(new RegenChunkCommand());
        event.registerServerCommand(new SpawnStructureCommand());
    }


    @Mod.EventHandler
    public void receiveMessages(FMLInterModComms.IMCEvent e) {
        ImmutableList<FMLInterModComms.IMCMessage> messageList = e.getMessages();
        for (FMLInterModComms.IMCMessage message : messageList) {
            String modId = message.getSender();
            if ("registerStructurePack".equals(message.key)) {
                String structurePackName=message.getStringValue();
                structurePackManager.addStructurePack(modId+":"+structurePackName);
            }

        }

    }


}
