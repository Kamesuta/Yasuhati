package com.kamesuta.yasuhati;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.MoverType;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import javax.sound.sampled.LineUnavailableException;
import java.util.Optional;
import java.util.function.Supplier;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("yasuhati")
public class Yasuhati {
    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();

    private Supplier<Minecraft> minecraftSupplier;
    private boolean configured = false;

    public static double walkDb = 2;
    public static double jumpDb = 40;

    public static KeyBinding configKeyBinding;

    public Yasuhati() {
        // Register the doClientStuff method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        minecraftSupplier = event.getMinecraftSupplier();

        try {
            MicTester micTester = new MicTester();
            Thread micThread = new Thread(micTester);
            micThread.setName("Yasuhati Mic Thread");
            micThread.start();
        } catch (LineUnavailableException e) {
            LOGGER.warn("マイク入力の初期化に失敗しました", e);
        }

        ModLoadingContext.get().registerExtensionPoint(
                ExtensionPoint.CONFIGGUIFACTORY,
                () -> (mc, screen) -> new GuiVolume());

        configKeyBinding = new KeyBinding("key.yasuhati.voiceconfig", GLFW.GLFW_KEY_V, "key.yasuhati.category");
        ClientRegistry.registerKeyBinding(configKeyBinding);
    }

    @SubscribeEvent
    public void onMainMenu(final GuiScreenEvent event) {
        if (event.getGui() instanceof MainMenuScreen) {
            if (!configured) {
                Optional.ofNullable(minecraftSupplier)
                        .flatMap(e -> Optional.ofNullable(e.get()))
                        .ifPresent(minecraft -> {
                            minecraft.displayGuiScreen(new GuiVolume());
                            configured = true;
                        });
            }
        }
    }

    @SubscribeEvent
    public void onKeyInput(final InputEvent.KeyInputEvent event) {
        if (configKeyBinding.isPressed()) {
            Optional.ofNullable(minecraftSupplier)
                    .flatMap(e -> Optional.ofNullable(e.get()))
                    .ifPresent(minecraft -> {
                        minecraft.displayGuiScreen(new GuiVolume());
                    });
        }
    }

    @SubscribeEvent
    public void onClientTick(final TickEvent.ClientTickEvent event) {
        Optional.ofNullable(minecraftSupplier)
                .flatMap(e -> Optional.ofNullable(e.get()))
                .ifPresent(minecraft -> {
                    ClientPlayerEntity player = minecraft.player;
                    if (player != null) {
                        double nowDb = MicTester.micLev;
                        int state = walkDb < jumpDb
                                ? nowDb < walkDb ? 0 : nowDb < jumpDb ? 1 : 2
                                : nowDb < jumpDb ? 0 : nowDb < walkDb ? 2 : 1;
                        switch (state) {
                            default:
                            case 0:
                                minecraft.gameSettings.keyBindForward.setPressed(false);
                                break;
                            case 2:
                                if (player.isOnGround())
                                    player.jump();
                            case 1:
                                minecraft.gameSettings.keyBindForward.setPressed(true);
                                break;
                        }
                    }
                });
    }
}
