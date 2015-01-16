package ch.n1b.leichenstein;

import org.bukkit.Bukkit;
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
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

/**
 * Created on 16.01.2015.
 *
 * @author Thomas
 */
public class Totengraeber implements Listener {

    private static final int SIGN_LINES = 4;

    private Properties deathCauses;

    public Totengraeber(Properties deathCauses) {
        this.deathCauses = deathCauses;
    }

    private static SimpleDateFormat dformat = new SimpleDateFormat("dd. MMM. yyyy");

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Entity e = event.getEntity();

        if(e instanceof Player){
            Player player = (Player) e;
            String line1 = player.getDisplayName();
            String line2 = "[RIP] " + dformat.format(new Date());
            String line3 = "cause of death:";
            String line4 = "";
            Player killer = player.getKiller();
            if(killer!=null){
                line4 = killer.getDisplayName();
            }else {
                DamageCause cause = player.getLastDamageCause().getCause();
                line4 = deathCauseToString(cause);
            }
            setGravestone(player.getLocation(),line1,line2,line3,line4);
        }
    }

    private String deathCauseToString(DamageCause cause){
        String mapped = deathCauses.getProperty(cause.name());
        if(mapped==null){
            mapped = cause.name();
        }
        return mapped;
    }

    private static void setGravestone(Location location,String... lines){
        // go up until we reach air
        Block block = location.getBlock();
        while (block.getType()!=Material.AIR){
            block = block.getRelative(BlockFace.UP);
        }

        // go down until we reach solid ground
        while (block.isLiquid() || block.getType()==Material.AIR){
            block = block.getRelative(BlockFace.DOWN);
        }

        block = block.getRelative(BlockFace.UP);
        block.setType(Material.SIGN_POST);

        BlockState state = block.getState();

        if(state instanceof Sign){
            Sign sign = (Sign) state;
            for (int i = 0; i < Math.min(lines.length,SIGN_LINES); i++) {
                sign.setLine(i,lines[i]);
            }
        }else {
            Bukkit.getLogger().warning("Could not place deathsign.");
        }

    }
}
