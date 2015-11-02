package ch.n1b.leichenstein;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created on 16.01.2015.
 *
 * @author Thomas
 */
public class Totengraeber implements Listener {

    private static final int SIGN_LINES = 4;

    private static final Set<String> DEVS =
            Sets.newHashSet("n1bblonian",
                    "xzosimusx",
                    "rePeted",
                    "ronfkingswanson",
                    "AthmosPrime",
                    "mrhappyoz");

    private static final List<String> CHAT_COLORS_CHAR;

    static {
        List<ChatColor> CHAT_COLORS =
                Lists.newArrayList(
                        ChatColor.AQUA,
                        ChatColor.BLUE,
                        ChatColor.DARK_AQUA,
                        ChatColor.DARK_BLUE,
                        ChatColor.DARK_GREEN,
                        ChatColor.DARK_PURPLE,
                        ChatColor.GREEN,
                        ChatColor.GOLD,
                        ChatColor.LIGHT_PURPLE,
                        ChatColor.RED,
                        ChatColor.YELLOW
                );

        CHAT_COLORS_CHAR = CHAT_COLORS.stream().map(new Function<ChatColor, String>() {
            @Override
            public String apply(ChatColor c) {
                return Character.toString(c.getChar());
            }
        }).collect(Collectors.<String>toList());
    }

    private Properties deathCauses;

    private Random rand = new Random();

    public Totengraeber(Properties deathCauses) {
        this.deathCauses = deathCauses;
    }

    private static SimpleDateFormat dformat = new SimpleDateFormat("dd. MMM. yyyy");

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Entity e = event.getEntity();

        if (e instanceof Player) {
            Player player = (Player) e;
            String color = Character.toString(ChatColor.BOLD.getChar());
            String name = player.getDisplayName();
            if (DEVS.contains(name)) {
                color += ChatColor.DARK_RED.toString();
            } else {
                color += stringToColor(name);
            }
            String line1 = color + player.getDisplayName();

            String line2 = "[RIP] " + dformat.format(new Date());
            String line3 = "cause of death:";
            String line4 = "";
            Player killer = player.getKiller();
            if (killer != null) {
                line4 = killer.getDisplayName();
            } else {
                EntityDamageEvent devent = player.getLastDamageCause();
                if (devent != null) {
                    DamageCause cause = devent.getCause();
                    line4 = deathCauseToString(cause);
                }
            }
            setGravestone(player.getLocation(), line1, line2, line3, line4);
        }
    }

    private String stringToColor(String str) {
        int hashed = hash(str.getBytes(StandardCharsets.UTF_8));
        return CHAT_COLORS_CHAR.get(hashed % CHAT_COLORS_CHAR.size());
    }

    private String deathCauseToString(DamageCause cause) {
        String mapped = deathCauses.getProperty(cause.name());
        if (mapped == null) {
            mapped = cause.name();
        }
        return mapped;
    }

    private void setGravestone(Location location, String... lines) {
        // go up until we reach air
        Block block = location.getBlock();
        while (block.getType() != Material.AIR) {
            block = block.getRelative(BlockFace.UP);
        }

        // go down until we reach solid ground or reach the void
        while ((block.isLiquid() || block.getType() == Material.AIR) && (block.getY() > 0)) {
            block = block.getRelative(BlockFace.DOWN);
        }

        block = block.getRelative(BlockFace.UP);
        block.setType(Material.SIGN_POST);
        block.setData((byte) (rand.nextInt() & 0x0F));
        BlockState state = block.getState();

        if (state instanceof Sign) {
            Sign sign = (Sign) state;
            for (int i = 0; i < Math.min(lines.length, SIGN_LINES); i++) {
                sign.setLine(i, lines[i]);
            }

            sign.update(true);
        } else {
            Bukkit.getLogger().warning("Could not place deathsign.");
        }

    }

    private static int hash(byte[] bytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(bytes);
            ByteBuffer buf = ByteBuffer.wrap(digest.digest());
            return Math.abs(buf.getInt());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hashing failed.", e);
        }
    }
}
