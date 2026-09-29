package net.alshanex.enchanters_script.data;

import com.mojang.serialization.DataResult;
import net.alshanex.enchanters_script.Constants;
import net.alshanex.enchanters_script.cipher.WorldCipher;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.saveddata.SavedData;

public class CipherSavedData extends SavedData {
    private static final String FILE_NAME = Constants.MOD_ID + "_cipher";
    private static final String FORWARD_KEY = "forward";

    public static final SavedData.Factory<CipherSavedData> FACTORY = new SavedData.Factory<>(
            CipherSavedData::new,
            CipherSavedData::load,
            null
    );

    private final WorldCipher cipher;

    private CipherSavedData() {
        cipher = WorldCipher.random(RandomSource.create());
        setDirty();
    }

    private CipherSavedData(WorldCipher cipher) {
        this.cipher = cipher;
    }

    private static CipherSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        int[] symbols = tag.getIntArray(FORWARD_KEY);
        DataResult<WorldCipher> cipher = WorldCipher.fromArray(symbols);

        if(cipher.isSuccess()){
            return new CipherSavedData(cipher.getOrThrow());
        } else {
            return new CipherSavedData();
        }
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putIntArray(FORWARD_KEY, cipher.toArray());
        return tag;
    }

    public WorldCipher cipher() {
        return cipher;
    }

    public static WorldCipher get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(FACTORY, FILE_NAME).cipher();
    }
}
