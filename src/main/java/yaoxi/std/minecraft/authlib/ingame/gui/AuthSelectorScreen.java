package yaoxi.std.minecraft.authlib.ingame.gui;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;

public class AuthSelectorScreen extends Screen {
    protected final Screen parent;
    private String tooltipText;
    private ButtonWidget deleteButton;
    private ButtonWidget selectButton;
    private ButtonWidget editButton;
    private ButtonWidget recreateButton;
    protected TextFieldWidget searchBox;
    private AuthListWidget levelList;

    public AuthSelectorScreen(Screen parent) {
        super(new TranslatableText("text.authlib-ingame.authselector.title"));
        this.parent = parent;
    }

    public boolean mouseScrolled(double d, double e, double amount) {
        return super.mouseScrolled(d, e, amount);
    }

    public void tick() {
        this.searchBox.tick();
    }

    protected void init() {
        this.minecraft.keyboard.enableRepeatEvents(true);
        this.searchBox = new TextFieldWidget(this.font, this.width / 2 - 100, 22, 200, 20, this.searchBox, I18n.translate("selectWorld.search", new Object[0]));
        this.searchBox.setChangedListener((string) -> {
            this.levelList.filter(() -> {
                return string;
            }, false);
        });
        this.levelList = new AuthListWidget(this, this.minecraft, this.width, this.height, 48,
                this.height - 64, 36, () -> {
            return this.searchBox.getText();
        }, this.levelList);
        this.children.add(this.searchBox);
        this.children.add(this.levelList);
        this.selectButton =
                (ButtonWidget)this.addButton(new ButtonWidget(this.width / 2 - 154,
                        this.height - 52, 150, 20, new TranslatableText("text.authlib-ingame.authselector.select").asString(),
                        (buttonWidget) -> {
            this.levelList.method_20159().ifPresent(AuthListWidget.Entry::play);
        }));
        this.addButton(new ButtonWidget(this.width / 2 + 4, this.height - 52, 150, 20,
                new TranslatableText("text.authlib-ingame.authselector.create").asString(), (buttonWidget) -> {
            this.minecraft.openScreen(new CreateAuthScreen(this));
        }));
        this.editButton =
                (ButtonWidget)this.addButton(new ButtonWidget(this.width / 2 - 154,
                        this.height - 28, 72, 20, new TranslatableText("text.authlib" +
                        "-ingame.authselector.edit").asString(), (buttonWidget) -> {
            this.levelList.method_20159().ifPresent(AuthListWidget.Entry::edit);
        }));
        this.deleteButton =
                (ButtonWidget)this.addButton(new ButtonWidget(this.width / 2 - 76,
                        this.height - 28, 72, 20, new TranslatableText("text.authlib" +
                        "-ingame.authselector.delete").asString(), (buttonWidget) -> {
            this.levelList.method_20159().ifPresent(AuthListWidget.Entry::delete);
        }));
        this.recreateButton =
                (ButtonWidget)this.addButton(new ButtonWidget(this.width / 2 + 4,
                        this.height - 28, 72, 20, new TranslatableText("text.authlib" +
                        "-ingame.authselector.recreate").asString(), (buttonWidget) -> {
            this.levelList.method_20159().ifPresent(AuthListWidget.Entry::recreate);
        }));
        this.addButton(new ButtonWidget(this.width / 2 + 82, this.height - 28, 72, 20,
                new TranslatableText("text.authlib-ingame.authselector.cancel").asString(), (buttonWidget) -> {
            this.minecraft.openScreen(this.parent);
        }));
        this.worldSelected(false);
        this.setInitialFocus(this.searchBox);
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return super.keyPressed(keyCode, scanCode, modifiers) ? true : this.searchBox.keyPressed(keyCode, scanCode, modifiers);
    }

    public boolean charTyped(char chr, int keyCode) {
        return this.searchBox.charTyped(chr, keyCode);
    }

    public void render(int mouseX, int mouseY, float delta) {
        this.tooltipText = null;
        this.levelList.render(mouseX, mouseY, delta);
        this.searchBox.render(mouseX, mouseY, delta);
        this.drawCenteredString(this.font, this.title.asFormattedString(), this.width / 2, 8, 16777215);
        super.render(mouseX, mouseY, delta);
        if (this.tooltipText != null) {
            this.renderTooltip(Lists.newArrayList(Splitter.on("\n").split(this.tooltipText)), mouseX, mouseY);
        }

    }

    public void setTooltip(String value) {
        this.tooltipText = value;
    }

    public void worldSelected(boolean active) {
        this.selectButton.active = active;
        this.deleteButton.active = active;
        this.editButton.active = active;
        this.recreateButton.active = active;
    }

    public void removed() {
        if (this.levelList != null) {
            this.levelList.children().forEach(AuthListWidget.Entry::close);
        }

    }
}
