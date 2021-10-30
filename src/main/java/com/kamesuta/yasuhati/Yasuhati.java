package com.kamesuta.yasuhati;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
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

    private Supplier<Minecraft> minecraftSupplier;
    private MicTester micTester;
    private Thread micThread;
    private boolean configured = false;

    public Yasuhati() {
        // Register the doClientStuff method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        minecraftSupplier = event.getMinecraftSupplier();

        try {
            micTester = new MicTester();
            micThread = new Thread(micTester);
            micThread.start();
        } catch (LineUnavailableException e) {
            LOGGER.warn("マイク入力の初期化に失敗しました", e);
        }
    }

    @SubscribeEvent
    public void onMainMenu(final GuiScreenEvent event) {
        if (event.getGui() instanceof MainMenuScreen) {
            if (!configured) {
                Optional.ofNullable(minecraftSupplier)
                        .flatMap(e -> Optional.ofNullable(e.get()))
                        .ifPresent(minecraft -> {
                            minecraft.displayGuiScreen(new GuiVolume(
                                    (walkDb, jumpDb) -> {
                                        minecraft.displayGuiScreen(new MainMenuScreen());
                                    },
                                    new StringTextComponent("音量設定")
                            ));
                            configured = true;
                        });
            }
        }
    }
}
