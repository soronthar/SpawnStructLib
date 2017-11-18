package mc.structspawn.command;


import mc.structspawn.StructSpawnLib;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.gen.IChunkGenerator;

import java.util.ArrayList;
import java.util.List;

public class RegenChunkCommand extends CommandBase {
    @Override
    public String getName() {
        return "regen";
    }

    @Override
    public String getUsage(ICommandSender iCommandSender) {
        return "regen";
    }

    @Override
    public void execute(MinecraftServer minecraftServer, ICommandSender sender, String[] strings) throws CommandException {

        World world = sender.getEntityWorld();
        if (world.isRemote) return;

        Entity commandSenderEntity = sender.getCommandSenderEntity();

        if (!(commandSenderEntity instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) commandSenderEntity;

        int radius = 0;
        if (strings.length > 0) {
            try {
                radius = Integer.parseInt(strings[0]);
            } catch (NumberFormatException e) {
                radius = 0;
            }
        }

        List<EntityPlayerMP> oldWatchers = new ArrayList<>();
        WorldServer server = minecraftServer.getWorld(world.provider.getDimension());
        ChunkProviderServer provider = server.getChunkProvider();
        PlayerChunkMap playerManager = server.getPlayerChunkMap();
        ChunkProviderServer chunkServer = (ChunkProviderServer) provider;

        try {

            BlockPos position = sender.getPosition();
            int currentChunkXPos = position.getX() >> 4;
            int currentChunkZPos = position.getZ() >> 4;

            int starXPos = currentChunkXPos - radius;
            int startZPos = currentChunkZPos - radius;
            int endXPos = currentChunkXPos + radius;
            int endZPos = currentChunkZPos + radius;

            for (int chunkXPos = starXPos; chunkXPos <= endXPos; chunkXPos++) {
                for (int chunkZPos = startZPos; chunkZPos <= endZPos; chunkZPos++) {
                    removeChunk(world, oldWatchers, playerManager, chunkServer, chunkXPos, chunkZPos);
                    regenChunk(oldWatchers, playerManager, chunkServer, chunkXPos, chunkZPos);
                }
            }
            sender.sendMessage(new TextComponentString("Chunks Regenerated"));

        } catch (Exception e) {
            StructSpawnLib.logger.warn("Failed to generate chunk", e);
            sender.sendMessage(new TextComponentString("Failed to generate chunk"));
        }
    }

    private void regenChunk(List<EntityPlayerMP> oldWatchers, PlayerChunkMap playerManager, ChunkProviderServer chunkServer, int chunkXPos, int chunkZPos) {
        IChunkGenerator gen = chunkServer.chunkGenerator;
        Chunk mcChunk = gen.generateChunk(chunkXPos, chunkZPos);
        long pos = ChunkPos.asLong(chunkXPos, chunkZPos);
        chunkServer.id2ChunkMap.put(pos, mcChunk);
        mcChunk.onLoad();
        mcChunk.populate(chunkServer, chunkServer.chunkGenerator);

        // We don't need to recreate the ChunkMapEntry unless there are players
        // but addPlayer handles that for us
        if (!oldWatchers.isEmpty()) {
            for (EntityPlayerMP entityPlayerMP : oldWatchers) {
                playerManager.addPlayer(entityPlayerMP);
            }
        }
    }

    private void removeChunk(World world, List<EntityPlayerMP> oldWatchers, PlayerChunkMap playerManager, ChunkProviderServer chunkServer, int chunkXPos, int chunkZPos) {
        Chunk mcChunk = null;
        if (chunkServer.chunkExists(chunkXPos, chunkZPos)) {
            mcChunk = chunkServer.loadChunk(chunkXPos, chunkZPos);
            if (mcChunk!=null) {
                saveWatcherPlayers(oldWatchers, playerManager, world.playerEntities, chunkXPos, chunkZPos);
                chunkServer.queueUnload(mcChunk);
                chunkServer.tick();
            }
        }
    }

    private void saveWatcherPlayers(List<EntityPlayerMP> oldWatchers, PlayerChunkMap playerManager, List<EntityPlayer> playerEntities, int chunkXPos, int chunkZPos) {
        for (EntityPlayer entityPlayer : playerEntities) {
            if (playerManager.isPlayerWatchingChunk((EntityPlayerMP) entityPlayer, chunkXPos, chunkZPos)) {
                oldWatchers.add((EntityPlayerMP) entityPlayer);
                PlayerChunkMapEntry entry = playerManager.getEntry(chunkXPos, chunkZPos);
                if (entry !=null) {
                    playerManager.removeEntry(entry);
                }
            }
        }
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        EntityPlayer commandSenderEntity = (EntityPlayer) sender.getCommandSenderEntity();
        return commandSenderEntity!=null && commandSenderEntity.isCreative();
    }
}
