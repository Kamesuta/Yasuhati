package com.kamesuta.yasuhati;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sound.sampled.LineUnavailableException;
import java.util.Optional;
import java.util.function.Supplier;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("yasuhati")
public class Yasuhati {
    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();

    private DecibelInput input;
    private Supplier<Minecraft> minecraftSupplier;

    public Yasuhati() {
        // Register the doClientStuff method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        minecraftSupplier = event.getMinecraftSupplier();

        try {
            input = new DecibelInput();
        } catch (LineUnavailableException e) {
            LOGGER.warn("マイク入力の初期化に失敗しました", e);
        }
    }

    @SubscribeEvent
    public void onMainMenu(final GuiScreenEvent event) {
        if (event.getGui() instanceof MainMenuScreen) {
            Optional.ofNullable(minecraftSupplier)
                    .flatMap(e -> Optional.ofNullable(e.get()))
                    .ifPresent(minecraft -> {
                        minecraft.displayGuiScreen(new GuiVolume(
                                (walkDb, jumpDb) -> {
                                    minecraft.displayGuiScreen(new MainMenuScreen());
                                },
                                new StringTextComponent("音量設定"),
                                input
                        ));
                    });
        }
    }

    @SubscribeEvent
    public void onTick(final TickEvent.ClientTickEvent event) {
        if (input != null) {
            Optional.ofNullable(minecraftSupplier)
                    .flatMap(e -> Optional.ofNullable(e.get()))
                    .ifPresent(minecraft -> {
                        DecibelInput.Decibel decibel = input.getDecibel();
                        // LOGGER.info("decibel, ave: {}, max: {}", decibel.average, decibel.peak);

                    });
        }
    }
}
