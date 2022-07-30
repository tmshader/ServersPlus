package me.tmshader.serversplus.mixin;

import me.tmshader.serversplus.ServersPlus;
import me.tmshader.serversplus.screens.ServersScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.configurate.ConfigurateException;

@Mixin(TitleScreen.class)
public class TitleScreenMixin extends Screen {
    protected TitleScreenMixin(Text title) {
        super(title);
    }

    @Inject(
            method = "switchToRealms",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    private void switchToRealms(CallbackInfo callbackInfo) {
        assert this.client != null;
        Screen screen = new ServersScreen(this);
        this.client.setScreen(screen);
        callbackInfo.cancel();
    }

    @ModifyArg(
            method = "initWidgetsNormal",
            slice = @Slice(from = @At(value = "CONSTANT", args = "stringValue=menu.online")),
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/widget/ButtonWidget;<init>(IIIILnet/minecraft/text/Text;Lnet/minecraft/client/gui/widget/ButtonWidget$PressAction;Lnet/minecraft/client/gui/widget/ButtonWidget$TooltipSupplier;)V",
                    ordinal = 0
            )
    )
    private Text modifyText(Text original) throws ConfigurateException {
        return Text.of(ServersPlus.config.node("button_text").getString());
    }
}
