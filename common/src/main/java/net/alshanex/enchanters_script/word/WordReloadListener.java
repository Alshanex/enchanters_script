package net.alshanex.enchanters_script.word;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.alshanex.enchanters_script.Constants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.*;

public class WordReloadListener extends SimpleJsonResourceReloadListener {
    public WordReloadListener() {
        super(new Gson(), "enchanter_words");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> files, ResourceManager resourceManager, ProfilerFiller profiler) {
        // Enchantment ID -> word, which is what EnchantmentWords stores
        Map<ResourceLocation, String> wordsByEnchantment = new HashMap<>();
        // Enchantment ID -> the file that defined it, to detect duplicates
        Map<ResourceLocation, ResourceLocation> fileByEnchantment = new HashMap<>();

        // Sorted so that when two files define the same enchantment, the same one always wins
        List<ResourceLocation> fileIds = new ArrayList<>(files.keySet());
        fileIds.sort(null);

        for (ResourceLocation fileId : fileIds) {
            DataResult<EnchantmentWord> result = EnchantmentWord.CODEC.parse(JsonOps.INSTANCE, files.get(fileId));
            Optional<EnchantmentWord> word = result.result();

            if (word.isEmpty()) {
                String message = result.error().map(DataResult.Error::message).orElse("Unknown error");
                Constants.LOG.error("Couldn't load word file {}: {}", fileId, message);
                continue;
            }

            ResourceLocation enchantment = word.get().enchantment();

            ResourceLocation previousFile = fileByEnchantment.put(enchantment, fileId);
            if (previousFile != null) {
                Constants.LOG.warn("Word files {} and {} both define {}; using {}", previousFile, fileId, enchantment, fileId);
            }

            wordsByEnchantment.put(enchantment, word.get().word());
        }

        EnchantmentWords.SERVER.replaceAll(wordsByEnchantment);
        Constants.LOG.info("Loaded {} enchantment word overrides", wordsByEnchantment.size());
    }
}
