package online.farmsteadminecraft.farttogether;

import net.minecraft.resources.ResourceLocation;
import online.farmsteadminecraft.farttogether.brand.FarmsteadBrand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FartTogether {
    public static final String MOD_ID = "farmstead_fart_together";
    public static final String NAME = "Fart Together";
    /** Project slug on Modrinth: the start-up line links there and the update check asks it. */
    public static final String MODRINTH_SLUG = "farmstead-fart-together";
    /** CurseForge project id: the update check asks CurseForge when Modrinth cannot answer ("" until the project exists: skipped). */
    public static final String CURSEFORGE_ID = "1800702";
    public static final String REPO = "https://github.com/shtanko-michael/farmstead-fart-together";
    public static final Logger LOGGER = LoggerFactory.getLogger("Fart Together");
    public static final String VERSION = /*$ mod_version*/"0.1.0";
    public static final String MINECRAFT = /*$ minecraft*/"1.21.1";

    private FartTogether() {
    }

    public static void init() {
        String loader = ModLoaderAccess.get().loaderName();
        FarmsteadBrand.started(LOGGER, NAME, VERSION, loader, MINECRAFT, MODRINTH_SLUG);
        // A client checks after its settings load (they can turn it off); a server has only the global switch
        if (ModLoaderAccess.get().isServer()) checkForUpdate();
    }

    public static void checkForUpdate() {
        FarmsteadBrand.checkForUpdate(LOGGER, new FarmsteadBrand.Project(NAME, VERSION, ModLoaderAccess.get().loaderName(),
            MINECRAFT, MODRINTH_SLUG, CURSEFORGE_ID));
    }

    /** The player switched the check off in the settings: one line saying so. */
    public static void updateCheckOff() {
        FarmsteadBrand.off(LOGGER, NAME);
    }

    /** {@code "minecraft:sand"} or {@code "sand"} to an id; null if malformed. */
    public static ResourceLocation parseId(String text) {
        try {
            //? if <1.21 {
            /*return new ResourceLocation(text);
             *///?} else
            return ResourceLocation.parse(text);
        } catch (RuntimeException e) {
            return null;
        }
    }

    public static ResourceLocation id(String path) {
        //? if <1.21 {
        /*return new ResourceLocation(MOD_ID, path);
         *///?} else
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
