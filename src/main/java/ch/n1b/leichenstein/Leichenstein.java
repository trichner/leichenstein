package ch.n1b.leichenstein;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Created on 16.01.2015.
 *
 * @author Thomas
 */
public class Leichenstein extends JavaPlugin{

    private static final String STRINGS_FILE = "strings.properties";

    @Override
    public void onEnable() {
        getLogger().warning("ALL HAIL N1B!");
        //---- load strings
        Properties stringProperties = new Properties();

        if(!Files.exists(Paths.get(getDataFolder().getPath() + File.separator + STRINGS_FILE))){
            try {
                Files.createFile(Paths.get(getDataFolder().getPath() + File.separator + STRINGS_FILE),null);
            } catch (IOException e) {
                getLogger().warning("Cannot create '" + STRINGS_FILE + "' file");
            }
        }

        try {
            stringProperties.load(new FileReader(Paths.get(getDataFolder().getPath() + File.separator +
                    STRINGS_FILE).toFile()));
        } catch (IOException e) {
            getLogger().warning("Cannot load '" + STRINGS_FILE + "' file");
        }

        // ---- register listeners
        Totengraeber listener = new Totengraeber(stringProperties);
        getServer().getPluginManager().registerEvents(listener, this);
    }
}
