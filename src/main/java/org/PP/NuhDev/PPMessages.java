package org.PP.NuhDev;

import org.powernukkitx.utils.Config;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class PPMessages {
    private String language;
    private Config messages;
    private List<String> langList = new ArrayList<>();
    private PurePerms plugin;

    public PPMessages(PurePerms plugin) {
        this.plugin = plugin;
        this.registerLanguages();
        this.loadMessages();
    }

    public void registerLanguages() {
        List<String> result = new ArrayList<>();
        
        try {
            File jarFile = new File(this.plugin.getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
            if (jarFile.isFile()) {
                try (JarFile jar = new JarFile(jarFile)) {
                    Enumeration<JarEntry> entries = jar.entries();
                    while (entries.hasMoreElements()) {
                        String name = entries.nextElement().getName();
                        if (name.contains("messages-") && name.endsWith(".yml")) {
                            String langCode = name.substring(name.length() - 6, name.length() - 4);
                            result.add(langCode);
                        }
                    }
                }
            }
        } catch (Exception e) {
            this.plugin.getLogger().error("Failed to register languages", e);
        }

        this.langList = result;
    }

    public String getMessage(String node, String... vars) {
        Object obj = this.messages.get(node);

        if (obj instanceof String) {
            String msg = (String) obj;
            int number = 0;

            for (String v : vars) {
                msg = msg.replace("%var" + number + "%", v);
                number++;
            }

            return msg;
        }

        return null;
    }

    public String getVersion() {
        return this.messages.getString("messages-version");
    }

    public void loadMessages() {
        String defaultLang = this.plugin.getConfig().getString("default-language");

        for (String langName : this.langList) {
            if (defaultLang.toLowerCase().equals(langName)) {
                this.language = langName;
                break; 
            }
        }

        if (this.language == null) {
            String author = "Unknown";
            if (!this.plugin.getDescription().getAuthors().isEmpty()) {
                author = this.plugin.getDescription().getAuthors().get(0);
            }
            
            this.plugin.getLogger().warning("Language resource " + defaultLang + " not found. Using default language resource by " + author);
            this.language = "en";
        }

        this.plugin.saveResource("messages-" + this.language + ".yml", false);

        this.messages = new Config(
                new File(this.plugin.getDataFolder(), "messages-" + this.language + ".yml"),
                Config.YAML
        );

        this.plugin.getLogger().info("Setting default language to '" + defaultLang + "'");

        if (compareVersions(this.getVersion(), this.plugin.getPPVersion()) < 0) {
            this.plugin.saveResource("messages-" + this.language + ".yml", true);
            
            this.messages = new Config(
                    new File(this.plugin.getDataFolder(), "messages-" + this.language + ".yml"),
                    Config.YAML
            );
        }
    }

    public void reloadMessages() {
        this.messages.reload();
    }

    private int compareVersions(String v1, String v2) {
        if (v1 == null) v1 = "0";
        if (v2 == null) v2 = "0";
        
        String[] parts1 = v1.split("\\.");
        String[] parts2 = v2.split("\\.");
        int length = Math.max(parts1.length, parts2.length);
        
        for (int i = 0; i < length; i++) {
            int p1 = i < parts1.length ? Integer.parseInt(parts1[i]) : 0;
            int p2 = i < parts2.length ? Integer.parseInt(parts2[i]) : 0;
            
            if (p1 < p2) return -1;
            if (p1 > p2) return 1;
        }
        
        return 0;
    }
  }
                                                             
