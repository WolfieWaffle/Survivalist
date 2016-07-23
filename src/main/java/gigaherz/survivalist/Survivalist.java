package gigaherz.survivalist;

import gigaherz.survivalist.base.BlockRegistered;
import gigaherz.survivalist.base.ItemRegistered;
import gigaherz.survivalist.base.ItemRegisteredArmor;
import gigaherz.survivalist.base.ItemRegisteredFood;
import gigaherz.survivalist.chopblock.BlockChopping;
import gigaherz.survivalist.chopblock.TileChopping;
import gigaherz.survivalist.rack.BlockRack;
import gigaherz.survivalist.rack.Dryable;
import gigaherz.survivalist.rack.TileRack;
import gigaherz.survivalist.rocks.EntityRock;
import gigaherz.survivalist.rocks.ItemOreRock;
import gigaherz.survivalist.rocks.ItemRock;
import gigaherz.survivalist.rocks.RocksEventHandling;
import gigaherz.survivalist.scraping.EnchantmentScraping;
import gigaherz.survivalist.scraping.ItemBreakingTracker;
import gigaherz.survivalist.scraping.MessageScraping;
import gigaherz.survivalist.torchfire.TorchFireEventHandling;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import org.apache.logging.log4j.Logger;

import java.util.List;

@Mod(modid = Survivalist.MODID, version = Survivalist.VERSION, dependencies = "required-after:Forge@[12.17.0.1916,)")
public class Survivalist
{
    public static final String MODID = "survivalist";
    public static final String VERSION = "@VERSION@";
    private static final String CHANNEL = "survivalist";

    // The instance of your mod that Forge uses.
    @Mod.Instance(value = Survivalist.MODID)
    public static Survivalist instance;

    @SidedProxy(clientSide = "gigaherz.survivalist.client.ClientProxy", serverSide = "gigaherz.survivalist.server.ServerProxy")
    public static ISidedProxy proxy;

    public static Logger logger;

    private GuiHandler guiHandler = new GuiHandler();

    public static EnchantmentScraping scraping;

    public static Item chainmail;
    public static Item tanned_leather;
    public static Item jerky;
    public static Item iron_nugget;
    public static Item rock;
    public static Item rock_ore;
    public static Item dough;
    public static Item round_bread;

    public static Item tanned_helmet;
    public static Item tanned_chestplate;
    public static Item tanned_leggings;
    public static Item tanned_boots;

    public static ItemStack rock_normal;
    public static ItemStack rock_andesite;
    public static ItemStack rock_diorite;
    public static ItemStack rock_granite;

    public static ItemStack iron_ore_rock;
    public static ItemStack gold_ore_rock;

    public static BlockRegistered rack;

    public static BlockRegistered chopping_block;

    public static ItemArmor.ArmorMaterial TANNED_LEATHER =
            EnumHelper.addArmorMaterial("tanned_leather", MODID + ":tanned_leather", 12,
                    new int[]{2, 4, 3, 1}, 15, SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, 1);

    public static SimpleNetworkWrapper channel;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();

        ConfigManager.loadConfig(new Configuration(event.getSuggestedConfigurationFile()));

        if (ConfigManager.instance.enableScraping)
        {
            ItemBreakingTracker.register();

            scraping = EnchantmentScraping.register();
        }

        if (ConfigManager.instance.enableTorchFire)
        {
            TorchFireEventHandling.register();
        }

        if (ConfigManager.instance.enableChainmailCrafting)
        {
            chainmail = new ItemRegistered("chainmail").setCreativeTab(CreativeTabs.MATERIALS);
            GameRegistry.register(chainmail);
        }

        if (ConfigManager.instance.enableDryingRack)
        {
            rack = new BlockRack("rack");
            GameRegistry.register(rack);
            GameRegistry.register(rack.createItemBlock());
            GameRegistry.registerTileEntity(TileRack.class, "tileRack");
        }

        if (ConfigManager.instance.enableLeatherTanning)
        {
            tanned_leather = new ItemRegistered("tanned_leather").setCreativeTab(CreativeTabs.MATERIALS);
            GameRegistry.register(tanned_leather);
            OreDictionary.registerOre("materialLeather", tanned_leather);
            OreDictionary.registerOre("materialTannedLeather", tanned_leather);
            OreDictionary.registerOre("materialHardenedLeather", tanned_leather);

            tanned_helmet = new ItemRegisteredArmor("tanned_helmet", TANNED_LEATHER, 0, EntityEquipmentSlot.HEAD);
            GameRegistry.register(tanned_helmet);

            tanned_chestplate = new ItemRegisteredArmor("tanned_chestplate", TANNED_LEATHER, 0, EntityEquipmentSlot.CHEST);
            GameRegistry.register(tanned_chestplate);

            tanned_leggings = new ItemRegisteredArmor("tanned_leggings", TANNED_LEATHER, 0, EntityEquipmentSlot.LEGS);
            GameRegistry.register(tanned_leggings);

            tanned_boots = new ItemRegisteredArmor("tanned_boots", TANNED_LEATHER, 0, EntityEquipmentSlot.FEET);
            GameRegistry.register(tanned_boots);
        }

        if (ConfigManager.instance.enableJerky)
        {
            jerky = new ItemRegisteredFood("jerky", 4, 1, true);
            GameRegistry.register(jerky);
        }

        if (ConfigManager.instance.enableIronNugget)
        {
            iron_nugget = new ItemRegistered("iron_nugget").setCreativeTab(CreativeTabs.MATERIALS);
            GameRegistry.register(iron_nugget);
            OreDictionary.registerOre("nuggetIron", iron_nugget);
        }

        if (ConfigManager.instance.enableRocks)
        {
            RocksEventHandling.register();

            rock = new ItemRock("rock").setCreativeTab(CreativeTabs.MATERIALS);
            GameRegistry.register(rock);

            rock_ore = new ItemOreRock("rock_ore").setCreativeTab(CreativeTabs.MATERIALS);
            GameRegistry.register(rock_ore);

            iron_ore_rock = new ItemStack(rock_ore, 1, 0);
            gold_ore_rock = new ItemStack(rock_ore, 1, 1);
            OreDictionary.registerOre("rockOreIron", iron_ore_rock);
            OreDictionary.registerOre("rockOreGold", gold_ore_rock);

            rock_normal = new ItemStack(rock, 1, 0);
            rock_andesite = new ItemStack(rock, 1, 1);
            rock_diorite = new ItemStack(rock, 1, 2);
            rock_granite = new ItemStack(rock, 1, 3);
            OreDictionary.registerOre("rock", rock_normal);
            OreDictionary.registerOre("rock", rock_andesite);
            OreDictionary.registerOre("rock", rock_diorite);
            OreDictionary.registerOre("rock", rock_granite);
            OreDictionary.registerOre("rockAndesite", rock_andesite);
            OreDictionary.registerOre("rockDiorite", rock_diorite);
            OreDictionary.registerOre("rockGranite", rock_granite);
        }

        if (ConfigManager.instance.enableBread)
        {
            dough = new ItemRegisteredFood("dough", 5, 0.6f, true);
            GameRegistry.register(dough);

            round_bread = new ItemRegisteredFood("round_bread", 8, 0.6f, true);
            GameRegistry.register(round_bread);
        }

        if (ConfigManager.instance.enableChopping)
        {
            chopping_block = new BlockChopping("chopping_block");
            GameRegistry.register(chopping_block);
            GameRegistry.register(chopping_block.createItemBlock());
            GameRegistry.registerTileEntity(TileChopping.class, "tile_chopping_block");
        }

        registerNetwork();

        proxy.preInit();
    }

    private void registerNetwork()
    {
        logger.info("Registering network channel...");

        channel = NetworkRegistry.INSTANCE.newSimpleChannel(CHANNEL);

        int messageNumber = 0;
        channel.registerMessage(MessageScraping.Handler.class, MessageScraping.class, messageNumber++, Side.CLIENT);
        logger.debug("Final message number: " + messageNumber);
    }

    public static boolean hasOreName(ItemStack stack, String oreName)
    {
        int id = OreDictionary.getOreID(oreName);
        for (int i : OreDictionary.getOreIDs(stack))
        {
            if (i == id) return true;
        }
        return false;
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        int entityId = 1;

        NetworkRegistry.INSTANCE.registerGuiHandler(this, guiHandler);

        if (ConfigManager.instance.removeSticksFromPlanks)
        {
            List<IRecipe> recipes = CraftingManager.getInstance().getRecipeList();
            for (int i = 0; i < recipes.size(); )
            {
                boolean removed = false;
                IRecipe r = recipes.get(i);
                if (r instanceof ShapedOreRecipe)
                {
                    ItemStack output = r.getRecipeOutput();
                    if (output != null && output.getItem() == Items.STICK)
                    {
                        recipes.remove(r);
                        removed = true;
                    }
                }

                if (!removed) i++;
            }
        }

        if (ConfigManager.instance.enableChopping)
        {
            if (ConfigManager.instance.replacePlanksRecipes)
            {
                List<IRecipe> recipes = CraftingManager.getInstance().getRecipeList();
                for (int i = 0; i < recipes.size(); )
                {
                    boolean removed = false;
                    IRecipe r = recipes.get(i);

                    ItemStack output = r.getRecipeOutput();
                    if (output != null && hasOreName(output, "plankWood"))
                    {
                        if (r instanceof ShapedRecipes)
                        {
                            ShapedRecipes rcp = (ShapedRecipes) r;

                            ItemStack[] inputs = rcp.recipeItems;

                            ItemStack logInput = null;
                            for (ItemStack input : inputs)
                            {
                                if (!hasOreName(input, "logWood") || logInput != null)
                                {
                                    logInput = null;
                                    break;
                                }

                                logInput = input;
                            }

                            if (logInput != null)
                            {
                                recipes.remove(r);
                                TileChopping.registerRecipe(logInput.copy(), output.copy());
                                removed = true;
                            }
                        }
                    }

                    if (!removed) i++;
                }


                GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(chopping_block), "logWood"));
            }

            if (ConfigManager.instance.removeSticksFromPlanks)
            {
                TileChopping.registerStockRecipes();
            }
        }

        if (ConfigManager.instance.enableBread)
        {
            if (ConfigManager.instance.removeVanillaBread)
            {
                List<IRecipe> recipes = CraftingManager.getInstance().getRecipeList();
                for (int i = 0; i < recipes.size(); )
                {
                    boolean removed = false;
                    IRecipe r = recipes.get(i);
                    if (r instanceof ShapedOreRecipe)
                    {
                        ItemStack output = r.getRecipeOutput();
                        if (output != null && output.getItem() == Items.BREAD)
                        {
                            recipes.remove(r);
                            removed = true;
                        }
                    }

                    if (!removed) i++;
                }
            }

            GameRegistry.addShapelessRecipe(new ItemStack(dough), Items.WHEAT, Items.WHEAT, Items.WHEAT, Items.WHEAT);
            GameRegistry.addSmelting(dough, new ItemStack(round_bread), 0);
        }

        if (ConfigManager.instance.enableDryingRack)
        {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(rack),
                    "sss",
                    " p ",
                    "p p",
                    's', "stickWood",
                    'p', "plankWood"));

            Dryable.register();

            if (ConfigManager.instance.enableLeatherTanning &&
                    ConfigManager.instance.enableSaddleCrafting)
            {
                GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Items.SADDLE),
                        "ttt",
                        "tst",
                        "i i",
                        't', "materialTannedLeather",
                        's', new ItemStack(Items.STRING),
                        'i', "ingotIron"));
            }
        }

        if (ConfigManager.instance.sticksFromLeaves)
            GameRegistry.addRecipe(new ShapelessOreRecipe(Items.STICK, "treeLeaves"));

        if (ConfigManager.instance.sticksFromSaplings)
            GameRegistry.addRecipe(new ShapelessOreRecipe(Items.STICK, "treeSapling"));

        if (ConfigManager.instance.enableIronNugget)
        {
            GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(iron_nugget, 9), "ingotIron"));
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Items.IRON_INGOT),
                    "nnn",
                    "nnn",
                    "nnn",
                    'n', "nuggetIron"));
        }

        if (ConfigManager.instance.enableChainmailCrafting)
        {
            if (ConfigManager.instance.enableIronNugget)
            {
                GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(chainmail),
                        " n ",
                        "n n",
                        " n ",
                        'n', "nuggetIron"));
            }

            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(chainmail, 9),
                    " n ",
                    "n n",
                    " n ",
                    'n', "ingotIron"));

            GameRegistry.addRecipe(new ItemStack(Items.CHAINMAIL_HELMET),
                    "ccc",
                    "c c",
                    'c', chainmail);

            GameRegistry.addRecipe(new ItemStack(Items.CHAINMAIL_CHESTPLATE),
                    "c c",
                    "ccc",
                    "ccc",
                    'c', chainmail);

            GameRegistry.addRecipe(new ItemStack(Items.CHAINMAIL_LEGGINGS),
                    "ccc",
                    "c c",
                    "c c",
                    'c', chainmail);

            GameRegistry.addRecipe(new ItemStack(Items.CHAINMAIL_BOOTS),
                    "c c",
                    "c c",
                    'c', chainmail);
        }

        if (ConfigManager.instance.enableRocks)
        {
            EntityRegistry.registerModEntity(EntityRock.class, "ThrownRock", entityId++, this, 80, 3, true);
            logger.debug("Last used id: %i", entityId);

            GameRegistry.addSmelting(iron_ore_rock, new ItemStack(iron_nugget), 0.1f);
            GameRegistry.addSmelting(gold_ore_rock, new ItemStack(Items.GOLD_NUGGET), 0.1f);

            GameRegistry.addRecipe(new ItemStack(Blocks.COBBLESTONE),
                    "rrr",
                    "rcr",
                    "rrr",
                    'r', rock_normal,
                    'c', Items.CLAY_BALL);

            GameRegistry.addRecipe(new ItemStack(Blocks.STONE, 1, 5),
                    "rrr",
                    "rcr",
                    "rrr",
                    'r', rock_andesite,
                    'c', Items.CLAY_BALL);

            GameRegistry.addRecipe(new ItemStack(Blocks.STONE, 1, 3),
                    "rrr",
                    "rcr",
                    "rrr",
                    'r', rock_diorite,
                    'c', Items.CLAY_BALL);

            GameRegistry.addRecipe(new ItemStack(Blocks.STONE, 1, 1),
                    "rrr",
                    "rcr",
                    "rrr",
                    'r', rock_granite,
                    'c', Items.CLAY_BALL);

            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Blocks.GRAVEL),
                    "rr",
                    "rr",
                    'r', "rock"));

            GameRegistry.addShapelessRecipe(new ItemStack(rock, 4, 0), Blocks.GRAVEL);
            GameRegistry.addShapelessRecipe(new ItemStack(Items.FLINT), Blocks.GRAVEL, Blocks.GRAVEL, Blocks.GRAVEL, Blocks.GRAVEL);
        }
    }

    public static ResourceLocation location(String path)
    {
        return new ResourceLocation(MODID, path);
    }
}
