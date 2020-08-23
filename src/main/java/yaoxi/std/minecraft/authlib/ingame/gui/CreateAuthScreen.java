package yaoxi.std.minecraft.authlib.ingame.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.MinecraftClientGame;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;

public class CreateAuthScreen extends Screen {
    private Screen lastScreen;
    private TextFieldWidget usernameField, passwordField, serverUrlField;
    private String username, password;
    private String serverUrl;
    private String authtype;
    private ButtonWidget apiTypeBtn;
    public CreateAuthScreen(Screen lastScreen){
        super(new LiteralText(""));
        this.lastScreen = lastScreen;
        this.authtype = "Mojang_API";
    }
    private String getNextAPI(String src){
        switch(src){
            case "Mojang_API": return "Authlib_Injector";
            case "Authlib_Injector": return "Offline";
            case "Offline": return "Mojang_API";
            default: return "";
        }
    }
    @Override
    public void init(){
        this.usernameField = new TextFieldWidget(this.font, this.width / 2 - 100, 40, 200,
                20, new TranslatableText("text.authlib-ingame.createauth.button" +
                ".username").asString());
        this.passwordField = new TextFieldWidget(this.font, this.width / 2 - 100, 80,
                200,
                20, new TranslatableText("text.authlib-ingame.createauth.button" +
                ".password").asString());
        this.serverUrlField = new TextFieldWidget(this.font, this.width / 2 - 100,
                this.height / 2 + 40,
                200,
                20, new TranslatableText("text.authlib-ingame.createauth.button" +
                ".server").asString());
        this.usernameField.setText(new TranslatableText("text.authlib-ingame.createauth.button" +
                ".username").asString());
        this.usernameField.setChangedListener(str -> {
            this.username = this.usernameField.getText();
        });
        this.children.add(this.usernameField);
        this.passwordField.setText(new TranslatableText("text.authlib-ingame.createauth.button" +
                ".password").asString());
        this.passwordField.setChangedListener(str -> {
            this.password = this.passwordField.getText();
        });
        this.children.add(this.passwordField);
        apiTypeBtn = new ButtonWidget(this.width / 2 - 50, this.height / 2, 100,
                20,
                new TranslatableText("text.authlib-ingame.createauth.api." + authtype).asString(),
                action -> {
                    authtype = getNextAPI(authtype);
                    apiTypeBtn.setMessage(new TranslatableText("text.authlib-ingame.createauth.api." + authtype).asString());

        });
        this.addButton(apiTypeBtn);
        this.serverUrlField.setText(new TranslatableText("text.authlib-ingame.createauth.button" +
                ".server").asString());
        this.serverUrlField.setChangedListener(str -> {
            this.serverUrl = this.serverUrlField.getText();
        });
        this.children.add(this.serverUrlField);
        this.addButton(new ButtonWidget(this.width / 2 - 98, this.height - 40, 96,
                20,
                new TranslatableText("text.authlib-ingame.authselector.create").asString(), action -> {

        }));
        this.addButton(new ButtonWidget(this.width / 2 + 2, this.height - 40, 96, 20
            , new TranslatableText("text.authlib-ingame.authlistwidget.back").asString(),
            action -> {
                MinecraftClient.getInstance().openScreen(lastScreen);
        }));
    }
    @Override
    public void render(int x, int y, float delta){
        renderBackground();
        this.usernameField.render(x, y, delta);
        if(!authtype.equals("Offline"))
            this.passwordField.render(x, y, delta);
        if(authtype.equals("Authlib_Injector"))
            this.serverUrlField.render(x, y, delta);
        this.apiTypeBtn.render(x, y, delta);
        super.render(x, y, delta);
    }
    public void tick(){
        this.usernameField.tick();
        this.passwordField.tick();
        this.serverUrlField.tick();
        if(authtype.equals("Mojang_API")){
            this.usernameField.setVisible(true);
            this.passwordField.setVisible(true);
            this.serverUrlField.setVisible(false);
        }else if(authtype.equals("Authlib_Injector")){
            this.usernameField.setVisible(true);
            this.passwordField.setVisible(true);
            this.serverUrlField.setVisible(true);
        }else{
            this.usernameField.setVisible(true);
            this.passwordField.setVisible(false);
            this.serverUrlField.setVisible(false);
        }
    }
}
