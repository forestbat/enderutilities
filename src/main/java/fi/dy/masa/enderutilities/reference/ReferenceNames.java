package fi.dy.masa.enderutilities.reference;

public class ReferenceNames
{
    public static final String NAME_ENTITY_ENDER_ARROW          = "enderarrow";
    public static final String NAME_ENTITY_ENDER_PEARL_REUSABLE = "enderpearlreusable";
    public static final String NAME_ENTITY_ENDERMAN_FIGHTER     = "endermanfighter";
    public static final String NAME_ENTITY_CHAIR                = "chair";
    public static final String NAME_ENTITY_FALLING_BLOCK        = "fallingblock";

    public static final String NAME_ITEM_ENDERPART                  = "enderpart";
    public static final String NAME_ITEM_ENDERPART_BARREL_UPGRADE   = "barrel_upgrade";
    public static final String NAME_ITEM_ENDERPART_CREATIVE_BREAKING = "creative_breaking";
    public static final String NAME_ITEM_ENDERPART_ENDERALLOY       = "enderalloy";
    public static final String NAME_ITEM_ENDERPART_ENDERCAPACITOR   = "endercapacitor";
    public static final String NAME_ITEM_ENDERPART_ENDERCORE        = "endercore";
    public static final String NAME_ITEM_ENDERPART_ENDERRELIC       = "enderrelic";
    public static final String NAME_ITEM_ENDERPART_ENDERROPE        = "enderrope";
    public static final String NAME_ITEM_ENDERPART_ENDERSTICK       = "enderstick";
    public static final String NAME_ITEM_ENDERPART_JAILER           = "jailer";
    public static final String NAME_ITEM_ENDERPART_LINKCRYSTAL      = "linkcrystal";
    public static final String NAME_ITEM_ENDERPART_MEMORY_CARD      = "memorycard"; // Not actual item, used for all memory cards in places
    public static final String NAME_ITEM_ENDERPART_MEMORY_CARD_MISC = "memorycard_misc";
    public static final String NAME_ITEM_ENDERPART_MEMORY_CARD_ITEMS  = "memorycard_items";
    public static final String NAME_ITEM_ENDERPART_STORAGE_KEY      = "storage_key";

    public static final String NAME_ITEM_BUILDERS_WAND          = "builderswand";
    public static final String NAME_ITEM_CHAIR_WAND             = "chairwand";
    public static final String NAME_ITEM_DOLLY                  = "dolly";
    public static final String NAME_ITEM_ENDERTOOL              = "endertool";
    public static final String NAME_ITEM_ENDER_PICKAXE          = "enderpickaxe";
    public static final String NAME_ITEM_ENDER_AXE              = "enderaxe";
    public static final String NAME_ITEM_ENDER_SHOVEL           = "endershovel";
    public static final String NAME_ITEM_ENDER_HOE              = "enderhoe";
    public static final String NAME_ITEM_ENDER_SWORD            = "endersword";

    public static final String NAME_ITEM_ENDER_ARROW            = "enderarrow";
    public static final String NAME_ITEM_ENDER_BAG              = "enderbag";
    public static final String NAME_ITEM_ENDER_BOW              = "enderbow";
    public static final String NAME_ITEM_ENDER_BUCKET           = "enderbucket";
    public static final String NAME_ITEM_ENDER_FURNACE          = "enderfurnace";
    public static final String NAME_ITEM_ENDER_LASSO            = "enderlasso";
    public static final String NAME_ITEM_ENDER_PEARL_REUSABLE   = "enderpearlreusable";
    public static final String NAME_ITEM_ENDER_PORTER           = "enderporter";
    public static final String NAME_ITEM_HANDY_BAG              = "handybag";
    public static final String NAME_ITEM_ICE_MELTER             = "icemelter";
    public static final String NAME_ITEM_INVENTORY_SWAPPER      = "inventoryswapper";
    public static final String NAME_ITEM_LIVING_MANIPULATOR     = "livingmanipulator";
    public static final String NAME_ITEM_MOB_HARNESS            = "mobharness";
    public static final String NAME_ITEM_NULLIFIER              = "nullifier";
    public static final String NAME_ITEM_PICKUP_MANAGER         = "pickupmanager";
    public static final String NAME_ITEM_QUICK_STACKER          = "quickstacker";
    public static final String NAME_ITEM_PORTAL_SCALER          = "portalscaler";
    public static final String NAME_ITEM_RULER                  = "ruler";
    public static final String NAME_ITEM_SYRINGE                = "syringe";
    public static final String NAME_ITEM_VOID_PICKAXE           = "void_pickaxe";


    /*******************
     * NOTE: Remember to keep the data fixers (in util.datafixers.*) up-to-date!
     ******************/
    public static final String NAME_TILE_ASU                    = "asu";
    public static final String NAME_TILE_BARREL                 = "barrel";
    public static final String NAME_TILE_DRAW_BRIDGE            = "draw_bridge";
    public static final String NAME_TILE_ENDER_ELEVATOR         = "ender_elevator";
    public static final String NAME_TILE_ENDER_ELEVATOR_SLAB    = "ender_elevator_slab";
    public static final String NAME_TILE_ENDER_ELEVATOR_LAYER   = "ender_elevator_layer";
    public static final String NAME_TILE_ENERGY_BRIDGE          = "energy_bridge";
    public static final String NAME_TILE_FLOOR                  = "floor";
    public static final String NAME_TILE_FRAME                  = "frame";
    public static final String NAME_TILE_INSERTER               = "inserter";
    public static final String NAME_TILE_MACHINE_0              = "machine_0";
    public static final String NAME_TILE_MACHINE_1              = "machine_1";
    public static final String NAME_TILE_MOLECULAR_EXCITER      = "molecular_exciter";
    public static final String NAME_TILE_MSU                    = "msu";
    public static final String NAME_TILE_PHASING                = "phasing";
    public static final String NAME_TILE_PORTAL                 = "portal";
    public static final String NAME_TILE_PORTAL_PANEL           = "portal_panel";
    public static final String NAME_TILE_QUICK_STACKER_ADVANCED = "quick_stacker_advanced";
    public static final String NAME_TILE_SOUND_BLOCK            = "sound_block";
    public static final String NAME_TILE_STORAGE_0              = "storage_0";

    public static final String NAME_TILE_ENTITY_CREATION_STATION    = "creation_station";
    public static final String NAME_TILE_ENTITY_ENDER_FURNACE       = "ender_furnace";
    public static final String NAME_TILE_ENTITY_ENDER_INFUSER       = "ender_infuser";
    public static final String NAME_TILE_ENTITY_HANDY_CHEST         = "handy_chest";
    public static final String NAME_TILE_ENTITY_JSU                 = "jsu";
    public static final String NAME_TILE_ENTITY_MEMORY_CHEST        = "memory_chest";
    public static final String NAME_TILE_ENTITY_TOOL_WORKSTATION    = "tool_workstation";


    public static final String NAME_MATERIAL_ENDERALLOY_ADVANCED = Reference.MOD_ID + "_enderalloy_advanced";


    public static String getPrefixedName(String name)
    {
        return Reference.MOD_ID + "_" + name;
    }

    public static String getDotPrefixedName(String name)
    {
        return Reference.MOD_ID + "." + name;
    }
}
