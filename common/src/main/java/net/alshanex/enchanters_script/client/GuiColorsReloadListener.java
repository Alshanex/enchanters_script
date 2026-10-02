package net.alshanex.enchanters_script.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.alshanex.enchanters_script.Constants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.GsonHelper;

import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

/**
 * Reloads the GUI colors whenever resource packs change.
 */
public class GuiColorsReloadListener implements ResourceManagerReloadListener {
    private static final ResourceLocation FILE = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "gui/colors.json");

    @Override
    public void onResourceManagerReload(ResourceManager manager) {
        // Start from the defaults, so a pack only needs the colors it changes
        Map<String, Integer> loaded = new HashMap<>(GuiColors.DEFAULTS);

        manager.getResource(FILE).ifPresent(resource -> {
            try (Reader reader = resource.openAsReader()) {
                JsonObject json = GsonHelper.parse(reader);
                for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                    String key = entry.getKey();
                    if (!GuiColors.isKnown(key)) {
                        Constants.LOG.warn("Unknown GUI color '{}' in {}", key, FILE);
                        continue;
                    }
                    Integer color = entry.getValue().isJsonPrimitive()
                            ? GuiColors.parse(entry.getValue().getAsString())
                            : null;
                    if (color == null) {
                        Constants.LOG.warn("GUI color '{}' should be #RRGGBB or #AARRGGBB, but is {}", key, entry.getValue());
                        continue;
                    }
                    loaded.put(key, color);
                }
            } catch (Exception e) {
                Constants.LOG.error("Couldn't read {}, using the default colors", FILE, e);
            }
        });

        GuiColors.replace(loaded);
    }
}
