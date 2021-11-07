package com.kamesuta.yasuhati;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AbstractSlider;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class GuiVolume extends Screen {
    /**
     * The text shown for the first button in GuiYesNo
     */
    protected ITextComponent confirmButtonText;
    private int ticksUntilEnable;
    private double walkDb;
    private double jumpDb;

    public GuiVolume() {
        super(new StringTextComponent("音量設定"));
        confirmButtonText = new StringTextComponent("セーブ");
        walkDb = Yasuhati.walkDb;
        jumpDb = Yasuhati.jumpDb;
    }

    private static double logSlider(double position) {
        // position will be between 0 and 1
        double minp = 0;
        double maxp = 1;

        // The result should be between 0 an 500
        double minv = Math.log(0.01);
        double maxv = Math.log(1000);

        // calculate adjustment factor
        double scale = (maxv - minv) / (maxp - minp);

        return Math.exp(minv + scale * (position - minp));
    }

    private static double unLogSlider(double volume) {
        // position will be between 0 and 1
        double minp = 0;
        double maxp = 1;

        // The result should be between 0 an 500
        double minv = Math.log(0.01);
        double maxv = Math.log(1000);

        // calculate adjustment factor
        double scale = (maxv - minv) / (maxp - minp);

        return (Math.log(volume) - minv) / scale + minp;
    }

    protected void init() {
        super.init();
        int i = 4;
        this.addButton(new AbstractSlider(
                this.width / 2 - 155,
                this.height / 6 - 12 + 24 * i,
                270, 20,
                new StringTextComponent("歩くときの声の大きさ"), unLogSlider(walkDb)) {

            {
                func_230979_b_();
            }

            @Override
            protected void func_230979_b_() {
                // 毎チック
                walkDb = logSlider(sliderValue);
            }

            @Override
            protected void func_230972_a_() {
                // 変更時のみ
            }
        });
        i = 5;
        this.addButton(new AbstractSlider(
                this.width / 2 - 155,
                this.height / 6 - 12 + 24 * i,
                270, 20,
                new StringTextComponent("ジャンプする時の声の大きさ"), unLogSlider(jumpDb)) {

            {
                func_230979_b_();
            }

            @Override
            protected void func_230979_b_() {
                // 毎チック
                jumpDb = logSlider(sliderValue);
            }

            @Override
            protected void func_230972_a_() {
                // 変更時のみ
            }
        });
        this.addButton(new Button(this.width / 2 - 75, this.height / 6 + 96 + 50, 150, 20, this.confirmButtonText, (p_213002_1_) -> {
            closeScreen();
        }));
    }

    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        double nowDb = MicTester.micLev;

        this.renderBackground(matrixStack);
        drawCenteredString(matrixStack, this.font, this.title, this.width / 2, 40, 16777215);
        int i = 4;
        drawString(matrixStack, font,
                String.format("%.2f", walkDb),
                this.width / 2 + 120, this.height / 6 - 12 + 24 * i + 7, 0xffffff);
        i = 5;
        drawString(matrixStack, font,
                String.format("%.2f", jumpDb),
                this.width / 2 + 120, this.height / 6 - 12 + 24 * i + 7, 0xffffff);
        drawString(matrixStack, font,
                String.format("マイク音量: %.2f", nowDb),
                this.width / 2 - 150, 110, 0xffffff);
        int state = walkDb < jumpDb
                ? nowDb < walkDb ? 0 : nowDb < jumpDb ? 1 : 2
                : nowDb < jumpDb ? 0 : nowDb < walkDb ? 2 : 1;
        drawString(matrixStack, font,
                state == 1 ? "歩く"
                        : state == 2 ? "ジャンプ"
                        : "止まる",
                this.width / 2, 110,
                state == 1 ? 0xffff00
                        : state == 2 ? 0xff000
                        : 0xffffff);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    public void tick() {
        super.tick();
        if (--this.ticksUntilEnable == 0) {
            for (Widget widget : this.buttons) {
                widget.active = true;
            }
        }
    }

    @Override
    public void onClose() {
        Yasuhati.walkDb = walkDb;
        Yasuhati.jumpDb = jumpDb;
    }

    public boolean shouldCloseOnEsc() {
        return false;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            closeScreen();
            return true;
        } else {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
    }
}
