package yaoxi.std.minecraft.authlib.ingame.mixins;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yaoxi.std.minecraft.authlib.ingame.gui.AuthSelectorScreen;

@Mixin(net.minecraft.client.gui.screen.TitleScreen.class)
public class AuthTitleScreen extends Screen {
    private AuthTitleScreen(Text title){
        super(title);
    }

    @Inject(at = @At("HEAD"), method = "init()V")
    private void init(CallbackInfo info) {
        this.addButton(new ButtonWidget(
                20, this.height / 4 + 132, 60, 20, new TranslatableText("text.authlib" +
                "-ingame.title.login").asString(),
                action -> {
                    MinecraftClient.getInstance().openScreen(new AuthSelectorScreen(this));
                }
        ));
    }
}
