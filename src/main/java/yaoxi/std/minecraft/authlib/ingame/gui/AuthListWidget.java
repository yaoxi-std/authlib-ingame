package yaoxi.std.minecraft.authlib.ingame.gui;

import com.google.common.hash.Hashing;
import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.*;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.EditWorldScreen;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.WorldSaveHandler;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.LevelProperties;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.level.storage.LevelStorageException;
import net.minecraft.world.level.storage.LevelSummary;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Supplier;

public class AuthListWidget  extends AlwaysSelectedEntryListWidget<AuthListWidget.Entry> {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat();
    private static final Identifier UNKNOWN_SERVER_LOCATION = new Identifier("textures/misc/unknown_server.png");
    private static final Identifier WORLD_SELECTION_LOCATION = new Identifier("textures/gui/world_selection.png");
    private final AuthSelectorScreen parent;
    private List<LevelSummary> levels;

    public AuthListWidget(AuthSelectorScreen parent, MinecraftClient client, int width, int height, int top, int bottom, int itemHeight, Supplier<String> searchFilter, AuthListWidget list) {
        super(client, width, height, top, bottom, itemHeight);
        this.parent = parent;
        if (list != null) {
            this.levels = list.levels;
        }

        this.filter(searchFilter, false);
    }

    public void filter(Supplier<String> filter, boolean load) {
        this.clearEntries();
        LevelStorage levelStorage = this.minecraft.getLevelStorage();
        if (this.levels == null || load) {
            try {
                this.levels = levelStorage.getLevelList();
            } catch (LevelStorageException var7) {
                LOGGER.error("Couldn't change player ", var7);
                this.minecraft.openScreen(new FatalErrorScreen(new TranslatableText("text.authlib-ingame.authlistwidget.unable_to_load", new Object[0]), var7.getMessage()));
                return;
            }

            Collections.sort(this.levels);
        }

        String string = ((String)filter.get()).toLowerCase(Locale.ROOT);
        Iterator var5 = this.levels.iterator();

        while(true) {
            LevelSummary levelSummary;
            do {
                if (!var5.hasNext()) {
                    return;
                }

                levelSummary = (LevelSummary)var5.next();
            } while(!levelSummary.getDisplayName().toLowerCase(Locale.ROOT).contains(string) && !levelSummary.getName().toLowerCase(Locale.ROOT).contains(string));

            this.addEntry(new AuthListWidget.Entry(this, levelSummary, this.minecraft.getLevelStorage()));
        }
    }

    protected int getScrollbarPosition() {
        return super.getScrollbarPosition() + 20;
    }

    public int getRowWidth() {
        return super.getRowWidth() + 50;
    }

    protected boolean isFocused() {
        return this.parent.getFocused() == this;
    }

    public void setSelected(AuthListWidget.Entry entry) {
        super.setSelected(entry);
        if (entry != null) {
            LevelSummary levelSummary = entry.level;
            NarratorManager.INSTANCE.narrate((new TranslatableText("narrator.select",
                    new TranslatableText("text.authlib-ingame" +
                            ".authlistwidget.selected",
                            levelSummary.getDisplayName(),
                            new Date(levelSummary.getLastPlayed()),
                            levelSummary.isHardcore() ? I18n.translate("gameMode.hardcore", new Object[0]) : I18n.translate("gameMode." + levelSummary.getGameMode().getName(), new Object[0]), levelSummary.hasCheats() ? I18n.translate("selectWorld.cheats", new Object[0]) : "", levelSummary.getVersion()))).getString());
        }

    }

    protected void moveSelection(int i) {
        super.moveSelection(i);
        this.parent.worldSelected(true);
    }

    public Optional<AuthListWidget.Entry> method_20159() {
        return Optional.ofNullable(this.getSelected());
    }

    public AuthSelectorScreen getParent() {
        return this.parent;
    }

    @Environment(EnvType.CLIENT)
    public final class Entry extends net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget.Entry<AuthListWidget.Entry> implements AutoCloseable {
        private final MinecraftClient client;
        private final AuthSelectorScreen screen;
        private final LevelSummary level;
        private final Identifier iconLocation;
        private File iconFile;
        private final NativeImageBackedTexture icon;
        private long time;

        public Entry(AuthListWidget levelList, LevelSummary level, LevelStorage levelStorage) {
            this.screen = levelList.getParent();
            this.level = level;
            this.client = MinecraftClient.getInstance();
            this.iconLocation = new Identifier("worlds/" + Hashing.sha1().hashUnencodedChars(level.getName()) + "/icon");
            this.iconFile = levelStorage.resolveFile(level.getName(), "icon.png");
            if (!this.iconFile.isFile()) {
                this.iconFile = null;
            }

            this.icon = this.getIconTexture();
        }

        public void render(int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
            String string = this.level.getDisplayName();
            String string2 = this.level.getName() + " (" + AuthListWidget.DATE_FORMAT.format(new Date(this.level.getLastPlayed())) + ")";
            if (StringUtils.isEmpty(string)) {
                string =
                        new TranslatableText("text.authlib-ingame.authlistwidget" +
                                ".player").asString() + " " + (i + 1);
            }

            String string3 = "";
            if (this.level.requiresConversion()) {
                string3 = I18n.translate("selectWorld.conversion", new Object[0]) + " " + string3;
            } else {
                string3 = I18n.translate("gameMode." + this.level.getGameMode().getName(), new Object[0]);
                if (this.level.isHardcore()) {
                    string3 = Formatting.DARK_RED + I18n.translate("gameMode.hardcore", new Object[0]) + Formatting.RESET;
                }

                if (this.level.hasCheats()) {
                    string3 = string3 + ", " + I18n.translate("selectWorld.cheats", new Object[0]);
                }

                String string4 = this.level.getVersion().asFormattedString();
                if (this.level.isDifferentVersion()) {
                    if (this.level.isFutureLevel()) {
                        string3 = string3 + ", " + I18n.translate("selectWorld.version", new Object[0]) + " " + Formatting.RED + string4 + Formatting.RESET;
                    } else {
                        string3 = string3 + ", " + I18n.translate("selectWorld.version", new Object[0]) + " " + Formatting.ITALIC + string4 + Formatting.RESET;
                    }
                } else {
                    string3 = string3 + ", " + I18n.translate("selectWorld.version", new Object[0]) + " " + string4;
                }
            }

            this.client.textRenderer.draw(string, (float)(k + 32 + 3), (float)(j + 1), 16777215);
            TextRenderer var10000 = this.client.textRenderer;
            float var10002 = (float)(k + 32 + 3);
            this.client.textRenderer.getClass();
            var10000.draw(string2, var10002, (float)(j + 9 + 3), 8421504);
            var10000 = this.client.textRenderer;
            var10002 = (float)(k + 32 + 3);
            this.client.textRenderer.getClass();
            int var10003 = j + 9;
            this.client.textRenderer.getClass();
            var10000.draw(string3, var10002, (float)(var10003 + 9 + 3), 8421504);
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.client.getTextureManager().bindTexture(this.icon != null ? this.iconLocation : AuthListWidget.UNKNOWN_SERVER_LOCATION);
            GlStateManager.enableBlend();
            DrawableHelper.blit(k, j, 0.0F, 0.0F, 32, 32, 32, 32);
            GlStateManager.disableBlend();
            if (this.client.options.touchscreen || bl) {
                this.client.getTextureManager().bindTexture(AuthListWidget.WORLD_SELECTION_LOCATION);
                DrawableHelper.fill(k, j, k + 32, j + 32, -1601138544);
                GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                int p = n - k;
                int q = p < 32 ? 32 : 0;
                if (this.level.isDifferentVersion()) {
                    DrawableHelper.blit(k, j, 32.0F, (float)q, 32, 32, 256, 256);
                    if (this.level.isLegacyCustomizedWorld()) {
                        DrawableHelper.blit(k, j, 96.0F, (float)q, 32, 32, 256, 256);
                        if (p < 32) {
                            Text text = (new TranslatableText("selectWorld.tooltip.unsupported", new Object[]{this.level.getVersion()})).formatted(Formatting.RED);
                            this.screen.setTooltip(this.client.textRenderer.wrapStringToWidth(text.asFormattedString(), 175));
                        }
                    } else if (this.level.isFutureLevel()) {
                        DrawableHelper.blit(k, j, 96.0F, (float)q, 32, 32, 256, 256);
                        if (p < 32) {
                            this.screen.setTooltip(Formatting.RED + I18n.translate("selectWorld.tooltip.fromNewerVersion1", new Object[0]) + "\n" + Formatting.RED + I18n.translate("selectWorld.tooltip.fromNewerVersion2", new Object[0]));
                        }
                    } else if (!SharedConstants.getGameVersion().isStable()) {
                        DrawableHelper.blit(k, j, 64.0F, (float)q, 32, 32, 256, 256);
                        if (p < 32) {
                            this.screen.setTooltip(Formatting.GOLD + I18n.translate("selectWorld.tooltip.snapshot1", new Object[0]) + "\n" + Formatting.GOLD + I18n.translate("selectWorld.tooltip.snapshot2", new Object[0]));
                        }
                    }
                } else {
                    DrawableHelper.blit(k, j, 0.0F, (float)q, 32, 32, 256, 256);
                }
            }

        }

        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            AuthListWidget.this.setSelected(this);
            this.screen.worldSelected(AuthListWidget.this.method_20159().isPresent());
            if (mouseX - (double)AuthListWidget.this.getRowLeft() <= 32.0D) {
                this.play();
                return true;
            } else if (Util.getMeasuringTimeMs() - this.time < 250L) {
                this.play();
                return true;
            } else {
                this.time = Util.getMeasuringTimeMs();
                return false;
            }
        }

        public void play() {

        }

        public void delete() {

        }

        public void edit() {

        }

        public void recreate() {

        }

        private void start() {

        }

        private NativeImageBackedTexture getIconTexture() {
            boolean bl = this.iconFile != null && this.iconFile.isFile();
            if (bl) {
                try {
                    InputStream inputStream = new FileInputStream(this.iconFile);
                    Throwable var3 = null;

                    NativeImageBackedTexture var6;
                    try {
                        NativeImage nativeImage = NativeImage.read(inputStream);
                        Validate.validState(nativeImage.getWidth() == 64, "Must be 64 pixels wide", new Object[0]);
                        Validate.validState(nativeImage.getHeight() == 64, "Must be 64 pixels high", new Object[0]);
                        NativeImageBackedTexture nativeImageBackedTexture = new NativeImageBackedTexture(nativeImage);
                        this.client.getTextureManager().registerTexture(this.iconLocation, nativeImageBackedTexture);
                        var6 = nativeImageBackedTexture;
                    } catch (Throwable var16) {
                        var3 = var16;
                        throw var16;
                    } finally {
                        if (inputStream != null) {
                            if (var3 != null) {
                                try {
                                    inputStream.close();
                                } catch (Throwable var15) {
                                    var3.addSuppressed(var15);
                                }
                            } else {
                                inputStream.close();
                            }
                        }

                    }

                    return var6;
                } catch (Throwable var18) {
                    AuthListWidget.LOGGER.error("Invalid icon for player {}",
                            this.level.getName(), var18);
                    this.iconFile = null;
                    return null;
                }
            } else {
                this.client.getTextureManager().destroyTexture(this.iconLocation);
                return null;
            }
        }

        public void close() {
            if (this.icon != null) {
                this.icon.close();
            }

        }
    }
}
