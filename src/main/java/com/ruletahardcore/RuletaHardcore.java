package com.ruletahardcore;

import com.ruletahardcore.client.ClientEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(RuletaHardcore.MODID)
public class RuletaHardcore {
    public static final String MODID = "ruletahardcore";

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MODID);

    public static final RegistryObject<Item> RULETA =
        ITEMS.register("ruleta", () -> new RuletaItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<SoundEvent> SND_CHAT = sound("chat_ping");
    public static final RegistryObject<SoundEvent> SND_SPIN = sound("spin");
    public static final RegistryObject<SoundEvent> SND_GLITCH = sound("glitch");

    private static RegistryObject<SoundEvent> sound(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(MODID, name)));
    }

    public RuletaHardcore() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(bus);
        SOUNDS.register(bus);
        bus.addListener((BuildCreativeModeTabContentsEvent e) -> {
            if (e.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) e.accept(RULETA.get());
        });
        MinecraftForge.EVENT_BUS.addListener((RegisterCommandsEvent e) -> RuletaCommand.register(e.getDispatcher()));
        if (FMLEnvironment.dist == Dist.CLIENT) ClientEvents.init();
    }
}
